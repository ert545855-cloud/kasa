package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Item record embedded in an order list.
 * Aligns with Firestore structure:
 * { "product_id": "...", "name": "...", "price": 110.0, "quantity": 2, "notes": "", "total_price": 220.0 }
 */
data class OrderItemRecord(
    val product_id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1,
    val notes: String = "",
    val total_price: Double = 0.0
)

/**
 * Room database schema for orders as requested:
 * Fields: restaurant_id, table_id, items (as a list), total_amount, and status,
 * ensuring it aligns with the Firestore structure for real-time synchronization.
 */
@Entity(
    tableName = "orders",
    indices = [
        Index(value = ["restaurant_id"]),
        Index(value = ["table_id"]),
        Index(value = ["status"]),
        Index(value = ["created_at"])
    ]
)
data class OrderEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "restaurant_id")
    val restaurant_id: String,

    @ColumnInfo(name = "table_id")
    val table_id: String,

    @ColumnInfo(name = "items")
    val items: List<OrderItemRecord> = emptyList(),

    @ColumnInfo(name = "total_amount")
    val total_amount: Double = 0.0,

    @ColumnInfo(name = "status")
    val status: String = "NEW", // NEW, CONFIRMED, PREPARING, READY, COMPLETED, CANCELLED

    @ColumnInfo(name = "order_number")
    val order_number: String = "",

    @ColumnInfo(name = "created_at")
    val created_at: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updated_at: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "customer_name")
    val customer_name: String = "",

    @ColumnInfo(name = "customer_phone")
    val customer_phone: String = "",

    @ColumnInfo(name = "notes")
    val notes: String = "",

    @ColumnInfo(name = "payment_status")
    val payment_status: String = "UNPAID", // UNPAID, PAID

    @ColumnInfo(name = "order_type")
    val order_type: String = "DINE_IN" // DINE_IN, TAKEAWAY, DELIVERY
)

/**
 * Room TypeConverter to persist List<OrderItemRecord> as JSON string in SQLite.
 */
class OrderTypeConverters {

    @TypeConverter
    fun fromOrderItemList(items: List<OrderItemRecord>?): String {
        if (items.isNullOrEmpty()) return "[]"
        val jsonArray = JSONArray()
        for (item in items) {
            val jsonObject = JSONObject().apply {
                put("product_id", item.product_id)
                put("name", item.name)
                put("price", item.price)
                put("quantity", item.quantity)
                put("notes", item.notes)
                put("total_price", item.total_price)
            }
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }

    @TypeConverter
    fun toOrderItemList(jsonString: String?): List<OrderItemRecord> {
        if (jsonString.isNullOrBlank()) return emptyList()
        return try {
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<OrderItemRecord>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    OrderItemRecord(
                        product_id = obj.optString("product_id", ""),
                        name = obj.optString("name", ""),
                        price = obj.optDouble("price", 0.0),
                        quantity = obj.optInt("quantity", 1),
                        notes = obj.optString("notes", ""),
                        total_price = obj.optDouble("total_price", 0.0)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }
}
