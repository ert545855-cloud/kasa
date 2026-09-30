package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessDao {
    @Query("SELECT * FROM businesses ORDER BY createdAt DESC")
    fun getAllBusinesses(): Flow<List<BusinessEntity>>

    @Query("SELECT * FROM businesses WHERE id = :id LIMIT 1")
    suspend fun getBusinessById(id: String): BusinessEntity?

    @Query("SELECT * FROM businesses WHERE ownerId = :ownerId ORDER BY createdAt DESC")
    fun getBusinessesByOwner(ownerId: String): Flow<List<BusinessEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusiness(business: BusinessEntity)

    @Update
    suspend fun updateBusiness(business: BusinessEntity)

    @Delete
    suspend fun deleteBusiness(business: BusinessEntity)

    @Query("DELETE FROM businesses WHERE id = :id")
    suspend fun deleteBusinessById(id: String)
}

@Dao
interface BranchDao {
    @Query("SELECT * FROM branches WHERE businessId = :businessId ORDER BY isMain DESC, createdAt ASC")
    fun getBranchesForBusiness(businessId: String): Flow<List<BranchEntity>>

    @Query("SELECT * FROM branches WHERE id = :id LIMIT 1")
    suspend fun getBranchById(id: String): BranchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBranch(branch: BranchEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBranches(branches: List<BranchEntity>)

    @Delete
    suspend fun deleteBranch(branch: BranchEntity)
}

@Dao
interface MemberDao {
    @Query("SELECT * FROM members WHERE businessId = :businessId ORDER BY createdAt ASC")
    fun getMembersForBusiness(businessId: String): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members WHERE userId = :userId")
    fun getMembershipsForUser(userId: String): Flow<List<MemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: MemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<MemberEntity>)

    @Delete
    suspend fun deleteMember(member: MemberEntity)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM menu_categories WHERE businessId = :businessId ORDER BY sortOrder ASC, nameTr ASC")
    fun getCategoriesForBusiness(businessId: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM menu_categories WHERE id = :id LIMIT 1")
    suspend fun getCategoryById(id: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("DELETE FROM menu_categories WHERE businessId = :businessId")
    suspend fun deleteAllCategoriesForBusiness(businessId: String)
}

@Dao
interface MenuItemDao {
    @Query("SELECT * FROM menu_items WHERE businessId = :businessId ORDER BY name ASC")
    fun getAllMenuItemsForBusiness(businessId: String): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE businessId = :businessId AND categoryId = :categoryId ORDER BY name ASC")
    fun getMenuItemsByCategory(businessId: String, categoryId: String): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE businessId = :businessId AND isAvailable = 1 ORDER BY name ASC")
    fun getAvailableMenuItems(businessId: String): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE id = :id LIMIT 1")
    suspend fun getMenuItemById(id: String): MenuItemEntity?

    @Query("SELECT * FROM menu_items WHERE businessId = :businessId AND (name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')")
    fun searchMenuItems(businessId: String, query: String): Flow<List<MenuItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItem(item: MenuItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItems(items: List<MenuItemEntity>)

    @Update
    suspend fun updateMenuItem(item: MenuItemEntity)

    @Delete
    suspend fun deleteMenuItem(item: MenuItemEntity)

    @Query("DELETE FROM menu_items WHERE businessId = :businessId")
    suspend fun deleteAllMenuItemsForBusiness(businessId: String)
}

@Dao
interface RestaurantTableDao {
    @Query("SELECT * FROM restaurant_tables WHERE businessId = :businessId ORDER BY tableNumber ASC")
    fun getTablesForBusiness(businessId: String): Flow<List<RestaurantTableEntity>>

    @Query("SELECT * FROM restaurant_tables WHERE id = :id LIMIT 1")
    suspend fun getTableById(id: String): RestaurantTableEntity?

    @Query("SELECT * FROM restaurant_tables WHERE businessId = :businessId AND tableNumber = :tableNumber LIMIT 1")
    suspend fun getTableByNumber(businessId: String, tableNumber: Int): RestaurantTableEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTable(table: RestaurantTableEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTables(tables: List<RestaurantTableEntity>)

    @Query("UPDATE restaurant_tables SET isOccupied = :isOccupied, currentOrderId = :currentOrderId WHERE id = :id")
    suspend fun updateTableOccupancy(id: String, isOccupied: Boolean, currentOrderId: String?)

    @Delete
    suspend fun deleteTable(table: RestaurantTableEntity)
}

@Dao
interface CachedOrderDao {
    @Query("SELECT * FROM cached_orders WHERE businessId = :businessId ORDER BY createdAt DESC")
    fun getOrdersForBusiness(businessId: String): Flow<List<CachedOrderEntity>>

    @Query("SELECT * FROM cached_orders WHERE businessId = :businessId AND status = :status ORDER BY createdAt DESC")
    fun getOrdersByStatus(businessId: String, status: String): Flow<List<CachedOrderEntity>>

    @Query("SELECT * FROM cached_orders WHERE id = :id LIMIT 1")
    suspend fun getOrderById(id: String): CachedOrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: CachedOrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<CachedOrderEntity>)

    @Query("UPDATE cached_orders SET status = :newStatus WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: String, newStatus: String)

    @Delete
    suspend fun deleteOrder(order: CachedOrderEntity)
}
