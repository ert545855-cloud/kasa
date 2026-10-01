package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.OrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    @Query("SELECT * FROM orders WHERE restaurant_id = :restaurantId ORDER BY created_at DESC")
    fun getOrdersForRestaurant(restaurantId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE restaurant_id = :restaurantId AND status = :status ORDER BY created_at DESC")
    fun getOrdersByStatus(restaurantId: String, status: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE restaurant_id = :restaurantId AND table_id = :tableId ORDER BY created_at DESC")
    fun getOrdersForTable(restaurantId: String, tableId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    fun getOrderByIdFlow(orderId: String): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: String): OrderEntity?

    @Query("SELECT * FROM orders WHERE order_number = :orderNumber LIMIT 1")
    fun getOrderByOrderNumber(orderNumber: String): Flow<OrderEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<OrderEntity>)

    @Query("UPDATE orders SET status = :newStatus, updated_at = :updatedAt WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: String, newStatus: String, updatedAt: Long = System.currentTimeMillis())

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Delete
    suspend fun deleteOrder(order: OrderEntity)

    @Query("DELETE FROM orders WHERE restaurant_id = :restaurantId")
    suspend fun deleteAllOrdersForRestaurant(restaurantId: String)
}
