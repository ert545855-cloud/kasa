package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.NexoApplication
import com.example.R
import com.example.data.local.NexoRoomDatabase
import com.example.data.local.dao.OrderDao
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.OrderItemRecord
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

enum class SyncState {
    IDLE,
    SYNCING,
    LIVE_REALTIME,
    OFFLINE_LOCAL,
    ERROR
}

/**
 * OrderRepository that facilitates real-time data sync between local Room storage
 * and Firestore, providing reactive flows for observing status updates and new orders.
 */
class OrderRepository(
    private val context: Context,
    private val orderDao: OrderDao = NexoRoomDatabase.getInstance(context).orderDao()
) {

    private val tag = "OrderRepository"

    private val databaseId by lazy {
        try {
            context.getString(R.string.firestore_database_id)
        } catch (e: Exception) {
            ""
        }
    }

    private val firestore: FirebaseFirestore? by lazy {
        try {
            NexoApplication.initializeFirebaseSafely(context)
            if (databaseId.isNotBlank()) {
                try {
                    FirebaseFirestore.getInstance(databaseId)
                } catch (e: Throwable) {
                    FirebaseFirestore.getInstance(com.google.firebase.FirebaseApp.getInstance(), databaseId)
                }
            } else {
                FirebaseFirestore.getInstance()
            }
        } catch (e: Throwable) {
            Log.w(tag, "Firestore not available, operating in offline Room mode: ${e.message}")
            null
        }
    }

    // Reactive SharedFlow for observing newly placed orders (triggers push notifications & kitchen tickets)
    private val _newOrdersFlow = MutableSharedFlow<OrderEntity>(replay = 1, extraBufferCapacity = 64)
    val newOrdersFlow: SharedFlow<OrderEntity> = _newOrdersFlow.asSharedFlow()

    // Reactive SharedFlow for observing status updates (e.g. NEW -> CONFIRMED -> PREPARING -> READY -> COMPLETED)
    private val _statusUpdatesFlow = MutableSharedFlow<OrderEntity>(replay = 1, extraBufferCapacity = 64)
    val statusUpdatesFlow: SharedFlow<OrderEntity> = _statusUpdatesFlow.asSharedFlow()

    // State of the sync engine
    private val _syncState = MutableStateFlow(SyncState.IDLE)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private var activeListener: ListenerRegistration? = null
    private var syncedRestaurantId: String? = null

    // ==========================================
    // REACTIVE LOCAL FLOWS (Backed by Room SQLite)
    // ==========================================

    fun observeOrders(restaurantId: String): Flow<List<OrderEntity>> {
        return orderDao.getOrdersForRestaurant(restaurantId)
    }

    fun observeOrdersByStatus(restaurantId: String, status: String): Flow<List<OrderEntity>> {
        return orderDao.getOrdersByStatus(restaurantId, status)
    }

    fun observeOrdersForTable(restaurantId: String, tableId: String): Flow<List<OrderEntity>> {
        return orderDao.getOrdersForTable(restaurantId, tableId)
    }

    fun observeOrder(orderId: String): Flow<OrderEntity?> {
        return orderDao.getOrderByIdFlow(orderId)
    }

    fun observeOrderByOrderNumber(orderNumber: String): Flow<OrderEntity?> {
        return orderDao.getOrderByOrderNumber(orderNumber)
    }

    suspend fun getOrderById(orderId: String): OrderEntity? = withContext(Dispatchers.IO) {
        orderDao.getOrderById(orderId)
    }

    // ==========================================
    // REAL-TIME FIRESTORE <-> ROOM SYNCHRONIZATION
    // ==========================================

    /**
     * Subscribes to real-time order updates from Firestore for the given restaurantId.
     * Automatically stores updates into Room database, and pushes reactive events
     * to newOrdersFlow and statusUpdatesFlow.
     */
    fun startRealtimeSync(restaurantId: String, scope: CoroutineScope) {
        if (syncedRestaurantId == restaurantId && activeListener != null) {
            return
        }

        stopRealtimeSync()
        syncedRestaurantId = restaurantId

        val db = firestore
        if (db == null) {
            _syncState.value = SyncState.OFFLINE_LOCAL
            Log.d(tag, "Firestore not available. Using local Room reactive storage.")
            return
        }

        _syncState.value = SyncState.SYNCING

        try {
            activeListener = db.collection("orders")
                .whereEqualTo("restaurant_id", restaurantId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(tag, "Firestore snapshot listener error: ${error.message}", error)
                        _syncState.value = SyncState.OFFLINE_LOCAL
                        return@addSnapshotListener
                    }

                    if (snapshot == null) return@addSnapshotListener

                    _syncState.value = SyncState.LIVE_REALTIME

                    scope.launch(Dispatchers.IO) {
                        try {
                            for (change in snapshot.documentChanges) {
                                val doc = change.document
                                val order = documentToOrderEntity(doc.id, doc.data) ?: continue

                                when (change.type) {
                                    DocumentChange.Type.ADDED -> {
                                        orderDao.insertOrder(order)
                                        // Emit new order if created recently (last 10 minutes)
                                        if (System.currentTimeMillis() - order.created_at < 10 * 60 * 1000) {
                                            _newOrdersFlow.emit(order)
                                        }
                                    }
                                    DocumentChange.Type.MODIFIED -> {
                                        orderDao.insertOrder(order)
                                        _statusUpdatesFlow.emit(order)
                                    }
                                    DocumentChange.Type.REMOVED -> {
                                        orderDao.deleteOrder(order)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(tag, "Error processing Firestore snapshot documents: ${e.message}", e)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e(tag, "Failed to start Firestore listener: ${e.message}", e)
            _syncState.value = SyncState.OFFLINE_LOCAL
        }
    }

    fun stopRealtimeSync() {
        activeListener?.remove()
        activeListener = null
        syncedRestaurantId = null
        _syncState.value = SyncState.IDLE
    }

    // ==========================================
    // ORDER MUTATIONS (LOCAL FIRST + CLOUD SYNC)
    // ==========================================

    /**
     * Creates and submits a new customer or waiter order.
     * Persists immediately to Room, emits reactive notification, and pushes to Firestore.
     */
    suspend fun submitOrder(order: OrderEntity): Result<OrderEntity> = withContext(Dispatchers.IO) {
        try {
            // 1. Immediately persist to Room local storage
            orderDao.insertOrder(order)

            // 2. Emit to reactive new orders flow
            _newOrdersFlow.emit(order)

            // 3. Sync to Firestore in cloud
            val db = firestore
            if (db != null) {
                try {
                    val orderMap = orderEntityToMap(order)
                    db.collection("orders").document(order.id).set(orderMap).await()
                    Log.d(tag, "Order ${order.order_number} successfully synced to Firestore.")
                } catch (e: Exception) {
                    Log.w(tag, "Firestore cloud push delayed/failed; order safely retained in Room: ${e.message}")
                }
            }

            Result.success(order)
        } catch (e: Exception) {
            Log.e(tag, "Failed to submit order: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Updates an order's status (e.g., NEW -> CONFIRMED -> PREPARING -> READY -> COMPLETED).
     * Immediately updates Room local DB, emits status update flow for customer/kitchen, and syncs to Firestore.
     */
    suspend fun updateOrderStatus(orderId: String, newStatus: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val now = System.currentTimeMillis()

            // 1. Update Room DB
            orderDao.updateOrderStatus(orderId, newStatus, now)

            // 2. Emit reactive status update flow
            val updatedOrder = orderDao.getOrderById(orderId)
            if (updatedOrder != null) {
                _statusUpdatesFlow.emit(updatedOrder)
            }

            // 3. Update Firestore
            val db = firestore
            if (db != null) {
                try {
                    db.collection("orders").document(orderId).update(
                        mapOf(
                            "status" to newStatus,
                            "updated_at" to now
                        )
                    ).await()
                    Log.d(tag, "Order $orderId status updated to $newStatus in Firestore.")
                } catch (e: Exception) {
                    Log.w(tag, "Firestore status update sync error: ${e.message}")
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Failed to update order status: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ==========================================
    // FIRESTORE MAP CONVERSION UTILITIES
    // ==========================================

    private fun orderEntityToMap(order: OrderEntity): Map<String, Any?> {
        return mapOf(
            "id" to order.id,
            "restaurant_id" to order.restaurant_id,
            "table_id" to order.table_id,
            "items" to order.items.map { item ->
                mapOf(
                    "product_id" to item.product_id,
                    "name" to item.name,
                    "price" to item.price,
                    "quantity" to item.quantity,
                    "notes" to item.notes,
                    "total_price" to item.total_price
                )
            },
            "total_amount" to order.total_amount,
            "status" to order.status,
            "order_number" to order.order_number,
            "created_at" to order.created_at,
            "updated_at" to order.updated_at,
            "customer_name" to order.customer_name,
            "customer_phone" to order.customer_phone,
            "notes" to order.notes,
            "payment_status" to order.payment_status,
            "order_type" to order.order_type
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun documentToOrderEntity(docId: String, data: Map<String, Any?>?): OrderEntity? {
        if (data == null) return null
        return try {
            val rawItems = data["items"] as? List<Map<String, Any?>> ?: emptyList()
            val parsedItems = rawItems.map { map ->
                OrderItemRecord(
                    product_id = map["product_id"]?.toString() ?: "",
                    name = map["name"]?.toString() ?: "",
                    price = (map["price"] as? Number)?.toDouble() ?: 0.0,
                    quantity = (map["quantity"] as? Number)?.toInt() ?: 1,
                    notes = map["notes"]?.toString() ?: "",
                    total_price = (map["total_price"] as? Number)?.toDouble() ?: 0.0
                )
            }

            OrderEntity(
                id = docId,
                restaurant_id = data["restaurant_id"]?.toString() ?: "",
                table_id = data["table_id"]?.toString() ?: "",
                items = parsedItems,
                total_amount = (data["total_amount"] as? Number)?.toDouble() ?: 0.0,
                status = data["status"]?.toString() ?: "NEW",
                order_number = data["order_number"]?.toString() ?: "",
                created_at = (data["created_at"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updated_at = (data["updated_at"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                customer_name = data["customer_name"]?.toString() ?: "",
                customer_phone = data["customer_phone"]?.toString() ?: "",
                notes = data["notes"]?.toString() ?: "",
                payment_status = data["payment_status"]?.toString() ?: "UNPAID",
                order_type = data["order_type"]?.toString() ?: "DINE_IN"
            )
        } catch (e: Exception) {
            Log.e(tag, "Failed to parse order from document $docId", e)
            null
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: OrderRepository? = null

        fun getInstance(context: Context): OrderRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: OrderRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
