package com.example.data.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.auth.FirebaseAuthService
import com.example.data.local.NexoRoomDatabase
import com.example.data.local.entity.*
import com.example.data.model.Category
import com.example.data.model.Product
import com.example.data.model.TableQr
import com.example.data.model.TableStatus
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

data class UserProfileData(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String? = null
)

class AuthRepository(private val context: Context) {

    private val authService = FirebaseAuthService(context)
    private val roomDb = NexoRoomDatabase.getInstance(context)
    private val businessDao = roomDb.businessDao()
    private val branchDao = roomDb.branchDao()
    private val memberDao = roomDb.memberDao()
    private val categoryDao = roomDb.categoryDao()
    private val menuItemDao = roomDb.menuItemDao()
    private val tableDao = roomDb.restaurantTableDao()
    private val cachedOrderDao = roomDb.cachedOrderDao()

    // Firestore instance with custom database ID from config
    private val databaseId by lazy { context.getString(R.string.firestore_database_id) }
    private val firestore by lazy { FirebaseFirestore.getInstance(databaseId) }

    val currentUser = authService.currentUser
    val isAuthenticated: Boolean
        get() = authService.isAuthenticated

    fun getRequiredUserId(): String = authService.getRequiredUserId()

    suspend fun loginWithGoogle(activity: Activity): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        val result = authService.signInWithGoogle(activity)
        result.onSuccess { user ->
            syncUserProfile(user)
        }
        result
    }

    suspend fun loginWithEmail(email: String, password: String): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        val result = authService.signInWithEmail(email, password)
        result.onSuccess { user ->
            syncUserProfile(user)
        }
        result
    }

    suspend fun registerWithEmail(
        email: String,
        password: String,
        displayName: String,
        restaurantName: String,
        phone: String = "",
        address: String = "İstanbul, Türkiye",
        currency: String = "₺"
    ): Result<BusinessEntity> = withContext(Dispatchers.IO) {
        val authResult = authService.signUpWithEmail(email, password, displayName)
        if (authResult.isFailure) {
            return@withContext Result.failure(authResult.exceptionOrNull() ?: Exception("Kayıt oluşturulamadı."))
        }
        val user = authResult.getOrThrow()
        syncUserProfile(user)

        registerBusiness(
            businessName = restaurantName,
            businessType = "RESTAURANT",
            phone = phone,
            address = address,
            currency = currency
        )
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        authService.sendPasswordReset(email)
    }

    suspend fun silentAutoLogin(): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        val result = authService.attemptSilentSignIn()
        result.onSuccess { user ->
            syncUserProfile(user)
        }
        result
    }

    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        authService.signOut()
    }

    fun observeUserBusinesses(ownerId: String): Flow<List<BusinessEntity>> {
        return businessDao.getBusinessesByOwner(ownerId)
    }

    private suspend fun syncUserProfile(user: FirebaseUser) {
        try {
            val userDocRef = firestore.collection("users").document(user.uid)
            val userMap = mapOf(
                "uid" to user.uid,
                "email" to (user.email ?: ""),
                "displayName" to (user.displayName ?: "NEXO Restoran Kullanıcısı"),
                "photoUrl" to (user.photoUrl?.toString() ?: ""),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            userDocRef.set(userMap).await()
        } catch (e: Exception) {
            Log.w("AuthRepository", "Failed to sync user profile to Firestore: ${e.message}")
        }
    }

    suspend fun registerBusiness(
        businessName: String,
        businessType: String,
        phone: String,
        address: String,
        currency: String
    ): Result<BusinessEntity> = withContext(Dispatchers.IO) {
        val uid = try {
            getRequiredUserId()
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }

        val businessId = UUID.randomUUID().toString()
        val branchId = UUID.randomUUID().toString()
        val memberId = UUID.randomUUID().toString()
        val slug = businessName.lowercase().replace(" ", "-").filter { it.isLetterOrDigit() || it == '-' }

        val businessEntity = BusinessEntity(
            id = businessId,
            name = businessName.trim(),
            slug = slug,
            businessType = businessType,
            ownerId = uid,
            phone = phone.trim(),
            address = address.trim(),
            currency = currency,
            language = "tr",
            plan = "PRO",
            isSuspended = false,
            createdAt = System.currentTimeMillis()
        )

        val branchEntity = BranchEntity(
            id = branchId,
            businessId = businessId,
            name = "Merkez Şube",
            address = address.trim(),
            isMain = true,
            createdAt = System.currentTimeMillis()
        )

        val user = currentUser.value
        val memberEntity = MemberEntity(
            id = memberId,
            businessId = businessId,
            userId = uid,
            role = "OWNER",
            name = user?.displayName ?: "İşletme Sahibi",
            email = user?.email ?: "",
            phone = phone.trim(),
            isActive = true,
            createdAt = System.currentTimeMillis()
        )

        try {
            // 1. Save to Room local persistence
            businessDao.insertBusiness(businessEntity)
            branchDao.insertBranch(branchEntity)
            memberDao.insertMember(memberEntity)

            // 2. Save to Firestore remote database
            val bizDoc = firestore.collection("businesses").document(businessId)
            val bizData = mapOf(
                "id" to businessId,
                "name" to businessEntity.name,
                "slug" to businessEntity.slug,
                "businessType" to businessEntity.businessType,
                "ownerId" to uid,
                "phone" to businessEntity.phone,
                "address" to businessEntity.address,
                "currency" to businessEntity.currency,
                "language" to businessEntity.language,
                "plan" to businessEntity.plan,
                "createdAt" to FieldValue.serverTimestamp()
            )
            bizDoc.set(bizData).await()

            // Subcollections
            bizDoc.collection("branches").document(branchId).set(
                mapOf(
                    "id" to branchId,
                    "businessId" to businessId,
                    "name" to branchEntity.name,
                    "address" to branchEntity.address,
                    "isMain" to true,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            bizDoc.collection("members").document(memberId).set(
                mapOf(
                    "id" to memberId,
                    "businessId" to businessId,
                    "userId" to uid,
                    "role" to "OWNER",
                    "name" to memberEntity.name,
                    "email" to memberEntity.email,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            Result.success(businessEntity)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Failed to register business in Firestore / Room: ${e.message}", e)
            Result.success(businessEntity)
        }
    }

    // ==========================================
    // ROOM OFFLINE CACHING FOR MENU ITEMS & DATA
    // ==========================================

    suspend fun cacheMenuCategories(businessId: String, categories: List<Category>) = withContext(Dispatchers.IO) {
        try {
            val entities = categories.map { cat ->
                CategoryEntity(
                    id = cat.id,
                    businessId = businessId,
                    nameTr = cat.name,
                    nameEn = cat.name,
                    sortOrder = cat.sortOrder,
                    iconName = cat.iconName,
                    isActive = cat.isActive
                )
            }
            categoryDao.insertCategories(entities)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error caching categories: ${e.message}")
        }
    }

    suspend fun cacheMenuItems(businessId: String, products: List<Product>) = withContext(Dispatchers.IO) {
        try {
            val entities = products.map { prod ->
                MenuItemEntity(
                    id = prod.id,
                    businessId = businessId,
                    categoryId = prod.categoryId,
                    name = prod.name,
                    description = prod.description,
                    price = prod.price,
                    imageUrl = prod.imageUrl,
                    isAvailable = prod.isAvailable,
                    isFeatured = prod.isFeatured,
                    preparationMinutes = 15,
                    calories = null,
                    allergens = prod.allergens.joinToString(","),
                    foodCost = prod.ingredientCost,
                    lastUpdated = System.currentTimeMillis()
                )
            }
            menuItemDao.insertMenuItems(entities)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error caching menu items: ${e.message}")
        }
    }

    suspend fun cacheRestaurantTables(businessId: String, tables: List<TableQr>) = withContext(Dispatchers.IO) {
        try {
            val entities = tables.map { tbl ->
                RestaurantTableEntity(
                    id = tbl.id,
                    businessId = businessId,
                    tableNumber = tbl.tableNumber,
                    label = tbl.label,
                    capacity = tbl.capacity,
                    isOccupied = tbl.status != TableStatus.EMPTY,
                    currentOrderId = null,
                    secureToken = tbl.secureToken
                )
            }
            tableDao.insertTables(entities)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error caching restaurant tables: ${e.message}")
        }
    }

    fun observeCachedCategories(businessId: String): Flow<List<CategoryEntity>> {
        return categoryDao.getCategoriesForBusiness(businessId)
    }

    fun observeCachedMenuItems(businessId: String): Flow<List<MenuItemEntity>> {
        return menuItemDao.getAllMenuItemsForBusiness(businessId)
    }

    fun observeCachedTables(businessId: String): Flow<List<RestaurantTableEntity>> {
        return tableDao.getTablesForBusiness(businessId)
    }
}
