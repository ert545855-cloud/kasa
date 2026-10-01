package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "businesses",
    indices = [Index(value = ["slug"], unique = true), Index(value = ["ownerId"])]
)
data class BusinessEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val slug: String = "",
    val businessType: String = "RESTAURANT",
    val ownerId: String = "",
    val logoUrl: String? = null,
    val coverUrl: String? = null,
    val phone: String = "",
    val address: String = "",
    val currency: String = "₺",
    val language: String = "tr",
    val plan: String = "PRO",
    val isSuspended: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "branches",
    indices = [Index(value = ["businessId"])]
)
data class BranchEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "businessId")
    val businessId: String = "",
    val name: String = "",
    val address: String = "",
    val isMain: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "members",
    indices = [Index(value = ["businessId"]), Index(value = ["userId"])]
)
data class MemberEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "businessId")
    val businessId: String = "",
    @ColumnInfo(name = "userId")
    val userId: String = "",
    val role: String = "OWNER",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "menu_categories",
    indices = [Index(value = ["businessId"])]
)
data class CategoryEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val businessId: String = "",
    val nameTr: String = "",
    val nameEn: String = "",
    val sortOrder: Int = 0,
    val iconName: String = "Restaurant",
    val isActive: Boolean = true
)

@Entity(
    tableName = "menu_items",
    indices = [Index(value = ["businessId"]), Index(value = ["categoryId"])]
)
data class MenuItemEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val businessId: String = "",
    val categoryId: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String? = null,
    val isAvailable: Boolean = true,
    val isFeatured: Boolean = false,
    val preparationMinutes: Int = 15,
    val calories: Int? = null,
    val allergens: String = "", // Comma-separated
    val variantsJson: String = "",
    val optionsJson: String = "",
    val foodCost: Double = 0.0,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "restaurant_tables",
    indices = [Index(value = ["businessId"])]
)
data class RestaurantTableEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val businessId: String = "",
    val tableNumber: Int = 1,
    val label: String = "Masa 1",
    val capacity: Int = 4,
    val isOccupied: Boolean = false,
    val currentOrderId: String? = null,
    val secureToken: String = UUID.randomUUID().toString()
)

@Entity(
    tableName = "cached_orders",
    indices = [Index(value = ["businessId"])]
)
data class CachedOrderEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val businessId: String = "",
    val orderNumber: String = "",
    val tableNumber: Int = 0,
    val status: String = "PENDING",
    val orderType: String = "DINE_IN",
    val itemsSummary: String = "",
    val subtotal: Double = 0.0,
    val tax: Double = 0.0,
    val total: Double = 0.0,
    val customerName: String? = null,
    val customerPhone: String? = null,
    val paymentStatus: String = "UNPAID",
    val createdAt: Long = System.currentTimeMillis()
)
