package com.example.data.repository

import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.OrderItemRecord
import com.example.data.model.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class CartItemInput(
    val productId: String,
    val variantId: String? = null,
    val selectedOptionIds: List<String> = emptyList(),
    val quantity: Int = 1
)

object NexoRepository {

    private var boundOrderRepository: OrderRepository? = null

    fun bindOrderRepository(repo: OrderRepository) {
        boundOrderRepository = repo
    }

    val defaultBusinessId = "biz-casa-cafe"

    val casaCafeBusiness = Business(
        id = defaultBusinessId,
        name = "Casa Cafe",
        slug = "casa-cafe",
        businessType = BusinessType.CAFE,
        websiteDomain = "casa-cafe.nexo.business",
        brandColorHex = "#D97706",
        tagline = "Özel Kahveler & Taze Lezzetler",
        phone = "+90 216 450 8899",
        address = "Kalamış Marina Cad. No: 12, Kadıköy, İstanbul",
        currency = "₺",
        language = "tr",
        plan = PlanType.PRO,
        enabledModules = NexoModule.entries.toSet(),
        branches = listOf(
            Branch(id = "branch-01", name = "Kalamış Marina (Merkez)", address = "Kalamış Marina Cad. No: 12", isMain = true),
            Branch(id = "branch-02", name = "Moda Şubesi", address = "Moda Cad. No: 44", isMain = false)
        )
    )

    val istanbulCoffeeBusiness = Business(
        id = "biz-istanbul-coffee",
        name = "İstanbul Coffee Roastery",
        slug = "istanbul-coffee",
        businessType = BusinessType.CAFE,
        websiteDomain = "istanbul-coffee.nexo.business",
        brandColorHex = "#78350F",
        tagline = "3. Nesil Nitelikli Kahve Kavurucusu",
        phone = "+90 212 245 1020",
        address = "Galata Kulesi Sok. No: 8, Beyoğlu, İstanbul",
        currency = "₺",
        language = "tr",
        plan = PlanType.PRO,
        enabledModules = NexoModule.entries.toSet(),
        branches = listOf(
            Branch(id = "branch-ist-01", name = "Galata Roastery", address = "Galata Kulesi Sok. No: 8", isMain = true)
        )
    )

    val burgerHouseBusiness = Business(
        id = "biz-burger-house",
        name = "Burger House Co.",
        slug = "burger-house",
        businessType = BusinessType.RESTAURANT,
        websiteDomain = "burger-house.nexo.business",
        brandColorHex = "#DC2626",
        tagline = "Smash Burgerler & El Yapımı Soslar",
        phone = "+90 212 334 5566",
        address = "Beşiktaş Çarşı No: 19, Beşiktaş, İstanbul",
        currency = "₺",
        language = "tr",
        plan = PlanType.BUSINESS,
        enabledModules = NexoModule.entries.toSet(),
        branches = listOf(
            Branch(id = "branch-bh-01", name = "Beşiktaş Çarşı", address = "Beşiktaş Çarşı No: 19", isMain = true)
        )
    )

    private val initialBusiness = casaCafeBusiness
    private val initialBusinessesList = listOf(casaCafeBusiness, istanbulCoffeeBusiness, burgerHouseBusiness)

    private val _activeTheme = MutableStateFlow(com.example.ui.theme.RestaurantTheme.MEDITERRANEAN)
    val activeTheme: StateFlow<com.example.ui.theme.RestaurantTheme> = _activeTheme.asStateFlow()

    fun setRestaurantTheme(theme: com.example.ui.theme.RestaurantTheme) {
        _activeTheme.value = theme
    }

    private val _businesses = MutableStateFlow<List<Business>>(initialBusinessesList)
    val businesses: StateFlow<List<Business>> = _businesses.asStateFlow()

    private val _activeBusinessId = MutableStateFlow(defaultBusinessId)
    val activeBusinessId: StateFlow<String> = _activeBusinessId.asStateFlow()

    private val _latestPushNotification = MutableStateFlow<NexoPushNotification?>(null)
    val latestPushNotification: StateFlow<NexoPushNotification?> = _latestPushNotification.asStateFlow()

    fun dismissPushNotification() {
        _latestPushNotification.value = null
    }

    fun acceptLatestNotificationOrder() {
        val notif = _latestPushNotification.value ?: return
        updateOrderStatus(notif.orderId, OrderStatus.ACCEPTED)
        _latestPushNotification.value = null
    }

    fun getBusinessBySlug(slug: String): Business? {
        return _businesses.value.firstOrNull { it.slug.equals(slug, ignoreCase = true) }
    }

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _tables = MutableStateFlow<List<TableQr>>(emptyList())
    val tables: StateFlow<List<TableQr>> = _tables.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _customers = MutableStateFlow<List<Customer>>(emptyList())
    val customers: StateFlow<List<Customer>> = _customers.asStateFlow()

    private val _stockItems = MutableStateFlow<List<StockItem>>(emptyList())
    val stockItems: StateFlow<List<StockItem>> = _stockItems.asStateFlow()

    private val _stockMovements = MutableStateFlow<List<StockMovement>>(emptyList())
    val stockMovements: StateFlow<List<StockMovement>> = _stockMovements.asStateFlow()

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

    private val _services = MutableStateFlow<List<ServiceItem>>(emptyList())
    val services: StateFlow<List<ServiceItem>> = _services.asStateFlow()

    private val _loyaltyRewards = MutableStateFlow<List<LoyaltyReward>>(emptyList())
    val loyaltyRewards: StateFlow<List<LoyaltyReward>> = _loyaltyRewards.asStateFlow()

    private val _sales = MutableStateFlow<List<SaleRecord>>(emptyList())
    val sales: StateFlow<List<SaleRecord>> = _sales.asStateFlow()

    private val _quotes = MutableStateFlow<List<Quote>>(emptyList())
    val quotes: StateFlow<List<Quote>> = _quotes.asStateFlow()

    private val _campaigns = MutableStateFlow<List<Campaign>>(emptyList())
    val campaigns: StateFlow<List<Campaign>> = _campaigns.asStateFlow()

    private val _staff = MutableStateFlow<List<Employee>>(emptyList())
    val staff: StateFlow<List<Employee>> = _staff.asStateFlow()

    private val _staffInvitations = MutableStateFlow<List<StaffInvitation>>(emptyList())
    val staffInvitations: StateFlow<List<StaffInvitation>> = _staffInvitations.asStateFlow()

    private val _aiLogs = MutableStateFlow<List<AiGenerationLog>>(emptyList())
    val aiLogs: StateFlow<List<AiGenerationLog>> = _aiLogs.asStateFlow()

    // Current logged in user profile simulation
    private val _currentStaffRole = MutableStateFlow(UserRole.OWNER)
    val currentStaffRole: StateFlow<UserRole> = _currentStaffRole.asStateFlow()

    init {
        loadSeedData(defaultBusinessId)
    }

    fun getActiveBusiness(): Business {
        return _businesses.value.firstOrNull { it.id == _activeBusinessId.value } ?: initialBusiness
    }

    fun switchBusiness(businessId: String) {
        if (_businesses.value.any { it.id == businessId }) {
            _activeBusinessId.value = businessId
        }
    }

    fun setStaffRole(role: UserRole) {
        _currentStaffRole.value = role
    }

    fun resetToDemoData() {
        _businesses.value = listOf(initialBusiness)
        _activeBusinessId.value = defaultBusinessId
        loadSeedData(defaultBusinessId)
    }

    // ========================================================
    // SUBSCRIPTION & PERMISSION RULES (CENTRALIZED SERVICE)
    // ========================================================

    fun canUseFeature(businessId: String, module: NexoModule): Boolean {
        val biz = _businesses.value.firstOrNull { it.id == businessId } ?: return false
        if (biz.isSuspended) return false
        if (!biz.enabledModules.contains(module)) return false

        // Starter plan exclusions unless purchased as add-on
        if (biz.plan == PlanType.STARTER) {
            val starterAllowed = setOf(NexoModule.MENU, NexoModule.ORDERS, NexoModule.CRM, NexoModule.ANALYTICS, NexoModule.AI, NexoModule.SETTINGS)
            if (!starterAllowed.contains(module) && !biz.enabledModules.contains(module)) {
                return false
            }
        }
        return true
    }

    fun checkLimit(businessId: String, resourceType: String): Boolean {
        val biz = _businesses.value.firstOrNull { it.id == businessId } ?: return false
        return when (resourceType) {
            "products" -> {
                val currentCount = _products.value.count { it.businessId == businessId }
                currentCount < biz.plan.maxProducts
            }
            "tables" -> {
                val currentCount = _tables.value.count { it.businessId == businessId }
                currentCount < biz.plan.maxTables
            }
            "staff" -> {
                val currentCount = _staff.value.count { it.businessId == businessId }
                currentCount < biz.plan.maxEmployees
            }
            "ai" -> {
                biz.aiCreditsUsed < biz.plan.aiCredits
            }
            else -> true
        }
    }

    fun toggleModule(businessId: String, module: NexoModule) {
        _businesses.value = _businesses.value.map { biz ->
            if (biz.id == businessId) {
                val updatedModules = if (biz.enabledModules.contains(module)) {
                    biz.enabledModules - module
                } else {
                    biz.enabledModules + module
                }
                biz.copy(enabledModules = updatedModules)
            } else biz
        }
    }

    fun upgradePlan(businessId: String, newPlan: PlanType) {
        _businesses.value = _businesses.value.map { biz ->
            if (biz.id == businessId) {
                biz.copy(plan = newPlan)
            } else biz
        }
    }

    fun activatePaidSubscription(businessId: String, plan: PlanType) {
        _businesses.value = _businesses.value.map { biz ->
            if (biz.id == businessId) {
                biz.copy(
                    plan = plan,
                    subscriptionStatus = SubscriptionStatus.PAID_ACTIVE,
                    isPaid = true
                )
            } else biz
        }
    }

    fun start15DayTrial(businessId: String) {
        val now = System.currentTimeMillis()
        val trialEnd = now + 15L * 24 * 60 * 60 * 1000L
        _businesses.value = _businesses.value.map { biz ->
            if (biz.id == businessId) {
                biz.copy(
                    subscriptionStatus = SubscriptionStatus.TRIAL_ACTIVE,
                    trialStartDate = now,
                    trialEndDate = trialEnd,
                    isPaid = false
                )
            } else biz
        }
    }

    fun simulateTrialDaysRemaining(businessId: String, daysLeft: Int) {
        val now = System.currentTimeMillis()
        val fakeEnd = now + (daysLeft.toLong() * 24L * 60L * 60L * 1000L)
        _businesses.value = _businesses.value.map { biz ->
            if (biz.id == businessId) {
                biz.copy(
                    subscriptionStatus = if (daysLeft <= 0) SubscriptionStatus.EXPIRED else SubscriptionStatus.TRIAL_ACTIVE,
                    trialEndDate = fakeEnd,
                    isPaid = false
                )
            } else biz
        }
    }

    fun registerOrUpdateBusinessModel(biz: Business) {
        val exists = _businesses.value.any { it.id == biz.id }
        if (exists) {
            _businesses.value = _businesses.value.map { if (it.id == biz.id) biz else it }
        } else {
            _businesses.value = _businesses.value + biz
            loadSeedData(biz.id)
        }
        _activeBusinessId.value = biz.id
    }

    // ========================================================
    // ORDER CREATION WITH SERVER-SIDE PRICE CALCULATION
    // ========================================================

    fun createCustomerOrder(
        businessId: String,
        tableNumber: Int?,
        cartItems: List<CartItemInput>,
        customerName: String?,
        customerPhone: String?,
        notes: String?
    ): Result<Order> {
        val biz = _businesses.value.firstOrNull { it.id == businessId }
            ?: return Result.failure(Exception("İşletme bulunamadı"))

        if (cartItems.isEmpty()) {
            return Result.failure(Exception("Sepetiniz boş olamaz"))
        }

        val allBizProducts = _products.value.filter { it.businessId == businessId }.associateBy { it.id }

        val orderItems = mutableListOf<OrderItem>()
        var subtotal = 0.0

        for (cartItem in cartItems) {
            val product = allBizProducts[cartItem.productId] ?: continue
            val variant = product.variants.firstOrNull { it.id == cartItem.variantId }
            val variantExtra = variant?.priceDiff ?: 0.0

            val selectedOptions = product.options.filter { cartItem.selectedOptionIds.contains(it.id) }
            val optionsExtra = selectedOptions.sumOf { it.priceDiff }

            val unitPrice = product.price + variantExtra + optionsExtra
            val itemTotal = unitPrice * cartItem.quantity
            subtotal += itemTotal

            orderItems.add(
                OrderItem(
                    productId = product.id,
                    productName = product.name,
                    variantName = variant?.name,
                    selectedOptions = selectedOptions.map { it.name },
                    unitPrice = unitPrice,
                    quantity = cartItem.quantity,
                    total = itemTotal
                )
            )
        }

        if (orderItems.isEmpty()) {
            return Result.failure(Exception("Geçerli ürün bulunamadı"))
        }

        val orderCountForBiz = _orders.value.count { it.businessId == businessId }
        val orderNo = "NX-${10482 + orderCountForBiz}"

        val newOrder = Order(
            businessId = businessId,
            orderNumber = orderNo,
            tableNumber = tableNumber,
            customerName = customerName?.ifBlank { null } ?: if (tableNumber != null) "Masa $tableNumber Misafiri" else "Misafir",
            customerPhone = customerPhone?.ifBlank { null },
            items = orderItems,
            subtotal = subtotal,
            discount = 0.0,
            total = subtotal,
            status = OrderStatus.PENDING,
            paymentMethod = PaymentMethod.UNPAID,
            notes = notes
        )

        _orders.value = listOf(newOrder) + _orders.value

        // Update corresponding table status
        if (tableNumber != null) {
            val table = _tables.value.firstOrNull { it.businessId == businessId && it.tableNumber == tableNumber }
            if (table != null) {
                updateTableStatus(table.id, TableStatus.ORDER_PENDING)
            }
        }

        // Dispatch real-time Push Notification to Restaurant Mobile App
        val targetBiz = _businesses.value.firstOrNull { it.id == businessId } ?: initialBusiness
        val pushNotif = NexoPushNotification(
            businessId = businessId,
            orderId = newOrder.id,
            orderNumber = newOrder.orderNumber,
            tableInfo = if (tableNumber != null) "Table $tableNumber" else "Paket Sipariş",
            itemsSummary = orderItems.joinToString("\n") { "${it.quantity} × ${it.productName}" },
            totalAmountFormatted = "${targetBiz.currency}${String.format("%.0f", newOrder.total)}",
            total = newOrder.total
        )
        _latestPushNotification.value = pushNotif

        // Update customer statistics if known phone provided
        if (!customerPhone.isNullOrBlank()) {
            val existing = _customers.value.firstOrNull { it.businessId == businessId && it.phone == customerPhone }
            if (existing != null) {
                val updated = existing.copy(
                    totalSpent = existing.totalSpent + subtotal,
                    orderCount = existing.orderCount + 1,
                    lastOrderAt = System.currentTimeMillis(),
                    loyaltyPoints = existing.loyaltyPoints + (subtotal * 0.1).toInt() // 10% points
                )
                _customers.value = _customers.value.map { if (it.id == existing.id) updated else it }
            } else {
                val newCust = Customer(
                    businessId = businessId,
                    name = customerName?.ifBlank { "Misafir" } ?: "Misafir",
                    phone = customerPhone,
                    totalSpent = subtotal,
                    orderCount = 1,
                    lastOrderAt = System.currentTimeMillis(),
                    loyaltyPoints = (subtotal * 0.1).toInt()
                )
                _customers.value = listOf(newCust) + _customers.value
            }
        }

        // Sync to Room + Firestore OrderRepository
        boundOrderRepository?.let { repo ->
            val entity = OrderEntity(
                id = newOrder.id,
                restaurant_id = newOrder.businessId,
                table_id = newOrder.tableNumber?.toString() ?: "0",
                items = orderItems.map { oi ->
                    OrderItemRecord(
                        product_id = oi.productId,
                        name = oi.productName,
                        price = oi.unitPrice,
                        quantity = oi.quantity,
                        notes = oi.selectedOptions.joinToString(", "),
                        total_price = oi.total
                    )
                },
                total_amount = newOrder.total,
                status = newOrder.status.name,
                order_number = newOrder.orderNumber,
                created_at = newOrder.createdAtTimestamp,
                updated_at = System.currentTimeMillis(),
                customer_name = newOrder.customerName ?: "",
                customer_phone = newOrder.customerPhone ?: "",
                notes = newOrder.notes ?: "",
                payment_status = newOrder.paymentMethod.name,
                order_type = if ((newOrder.tableNumber ?: 0) > 0) "DINE_IN" else "TAKEAWAY"
            )
            CoroutineScope(Dispatchers.IO).launch {
                repo.submitOrder(entity)
            }
        }

        return Result.success(newOrder)
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        val order = _orders.value.firstOrNull { it.id == orderId } ?: return
        val updated = order.copy(status = newStatus)
        _orders.value = _orders.value.map { if (it.id == orderId) updated else it }

        // Sync status to Room + Firestore OrderRepository
        boundOrderRepository?.let { repo ->
            CoroutineScope(Dispatchers.IO).launch {
                repo.updateOrderStatus(orderId, newStatus.name)
            }
        }

        // Update corresponding table status
        if (order.tableNumber != null) {
            val table = _tables.value.firstOrNull { it.businessId == order.businessId && it.tableNumber == order.tableNumber }
            if (table != null) {
                val newTableStatus = when (newStatus) {
                    OrderStatus.PENDING -> TableStatus.ORDER_PENDING
                    OrderStatus.ACCEPTED -> TableStatus.OCCUPIED
                    OrderStatus.PREPARING -> TableStatus.PREPARING
                    OrderStatus.READY -> TableStatus.PREPARING
                    OrderStatus.COMPLETED -> TableStatus.CLEANING
                    OrderStatus.CANCELLED -> TableStatus.EMPTY
                }
                updateTableStatus(table.id, newTableStatus)
            }
        }

        // Side-effects on completion: Deduct stock, log sale in analytics, update CRM
        if (newStatus == OrderStatus.COMPLETED) {
            order.items.forEach { item ->
                val matchingStock = _stockItems.value.firstOrNull {
                    it.businessId == order.businessId && it.name.contains(item.productName, ignoreCase = true)
                }
                if (matchingStock != null) {
                    adjustStock(matchingStock.id, -item.quantity.toDouble(), StockMovementType.SALE_ORDER, "Sipariş #${order.orderNumber}")
                }
            }
            createPosSale(
                itemsSummary = order.items.joinToString(", ") { "${it.quantity}x ${it.productName}" },
                amount = order.total,
                method = PaymentMethod.CARD,
                customerName = order.customerName,
                bizId = order.businessId
            )
        }
    }

    fun syncFromOrderEntity(entity: OrderEntity) {
        val mappedStatus = when (entity.status.uppercase()) {
            "CONFIRMED", "ACCEPTED" -> OrderStatus.ACCEPTED
            "PREPARING" -> OrderStatus.PREPARING
            "READY" -> OrderStatus.READY
            "COMPLETED" -> OrderStatus.COMPLETED
            "CANCELLED" -> OrderStatus.CANCELLED
            else -> OrderStatus.PENDING
        }

        val existing = _orders.value.firstOrNull { it.id == entity.id }
        if (existing != null) {
            if (existing.status != mappedStatus) {
                updateOrderStatus(entity.id, mappedStatus)
            }
        } else {
            val order = Order(
                id = entity.id,
                businessId = entity.restaurant_id,
                orderNumber = entity.order_number,
                tableNumber = entity.table_id.toIntOrNull(),
                customerName = entity.customer_name.ifBlank { "Misafir" },
                customerPhone = entity.customer_phone.ifBlank { null },
                items = entity.items.map { item ->
                    OrderItem(
                        productId = item.product_id,
                        productName = item.name,
                        unitPrice = item.price,
                        quantity = item.quantity,
                        total = item.total_price
                    )
                },
                subtotal = entity.total_amount,
                total = entity.total_amount,
                status = mappedStatus,
                notes = entity.notes.ifBlank { null },
                createdAtTimestamp = entity.created_at
            )
            _orders.value = listOf(order) + _orders.value
        }
    }

    // ========================================================
    // STOCK OPERATIONS
    // ========================================================

    fun adjustStock(stockItemId: String, delta: Double, type: StockMovementType, reason: String) {
        val item = _stockItems.value.firstOrNull { it.id == stockItemId } ?: return
        val newQty = (item.stockQuantity + delta).coerceAtLeast(0.0)
        val updated = item.copy(stockQuantity = newQty)
        _stockItems.value = _stockItems.value.map { if (it.id == stockItemId) updated else it }

        val movement = StockMovement(
            stockItemId = stockItemId,
            productName = item.name,
            type = type,
            quantity = delta,
            reason = reason
        )
        _stockMovements.value = listOf(movement) + _stockMovements.value
    }

    fun addStockItem(item: StockItem) {
        _stockItems.value = listOf(item) + _stockItems.value
    }

    // ========================================================
    // MENU & PRODUCT CRUD
    // ========================================================

    fun addCategory(cat: Category) {
        _categories.value = _categories.value + cat
    }

    fun updateCategory(cat: Category) {
        _categories.value = _categories.value.map { if (it.id == cat.id) cat else it }
    }

    fun deleteCategory(catId: String) {
        _categories.value = _categories.value.filter { it.id != catId }
        _products.value = _products.value.filter { it.categoryId != catId }
    }

    fun addProduct(prod: Product) {
        _products.value = listOf(prod) + _products.value
    }

    fun updateProduct(prod: Product) {
        _products.value = _products.value.map { if (it.id == prod.id) prod else it }
    }

    fun deleteProduct(prodId: String) {
        _products.value = _products.value.filter { it.id != prodId }
    }

    fun toggleProductAvailability(prodId: String) {
        _products.value = _products.value.map {
            if (it.id == prodId) it.copy(isAvailable = !it.isAvailable) else it
        }
    }

    // ========================================================
    // TABLE & QR MANAGEMENT
    // ========================================================

    fun addTable(label: String, tableNum: Int, color: String = "#4F46E5") {
        val bizId = _activeBusinessId.value
        val newTable = TableQr(
            businessId = bizId,
            tableNumber = tableNum,
            label = label,
            qrDesignColor = color
        )
        _tables.value = _tables.value + newTable
    }

    fun deleteTable(tableId: String) {
        _tables.value = _tables.value.filter { it.id != tableId }
    }

    fun regenerateQrToken(tableId: String) {
        _tables.value = _tables.value.map {
            if (it.id == tableId) it.copy(secureToken = UUID.randomUUID().toString().take(8)) else it
        }
    }

    fun updateTableStatus(tableId: String, status: TableStatus) {
        _tables.value = _tables.value.map {
            if (it.id == tableId) it.copy(status = status) else it
        }
    }

    // ========================================================
    // CRM CUSTOMERS
    // ========================================================

    fun addCustomer(customer: Customer) {
        _customers.value = listOf(customer) + _customers.value
        syncCustomerToFirebase(customer)
    }

    fun updateCustomer(customer: Customer) {
        _customers.value = _customers.value.map { if (it.id == customer.id) customer else it }
        syncCustomerToFirebase(customer)
    }

    fun sendStaffInvitation(
        businessId: String,
        email: String,
        name: String,
        role: UserRole,
        note: String = ""
    ): StaffInvitation {
        val invite = StaffInvitation(
            businessId = businessId,
            email = email.trim(),
            name = name.trim(),
            role = role,
            note = note.trim()
        )
        _staffInvitations.value = listOf(invite) + _staffInvitations.value
        syncInvitationToFirebase(invite)
        return invite
    }

    fun cancelStaffInvitation(inviteId: String) {
        _staffInvitations.value = _staffInvitations.value.filter { it.id != inviteId }
    }

    private fun syncCustomerToFirebase(customer: Customer) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("customers").document(customer.id).set(
                    mapOf(
                        "id" to customer.id,
                        "businessId" to customer.businessId,
                        "name" to customer.name,
                        "phone" to customer.phone,
                        "email" to (customer.email ?: ""),
                        "notes" to (customer.notes ?: ""),
                        "totalSpent" to customer.totalSpent,
                        "orderCount" to customer.orderCount,
                        "loyaltyPoints" to customer.loyaltyPoints,
                        "lastOrderAt" to customer.lastOrderAt,
                        "tags" to customer.tags,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            } catch (e: Exception) {
                android.util.Log.w("NexoRepository", "Firestore customer sync skipped: ${e.message}")
            }
        }
    }

    private fun syncInvitationToFirebase(invitation: StaffInvitation) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("invitations").document(invitation.id).set(
                    mapOf(
                        "id" to invitation.id,
                        "businessId" to invitation.businessId,
                        "email" to invitation.email,
                        "name" to invitation.name,
                        "role" to invitation.role.name,
                        "note" to invitation.note,
                        "token" to invitation.token,
                        "status" to invitation.status,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            } catch (e: Exception) {
                android.util.Log.w("NexoRepository", "Firestore invitation sync skipped: ${e.message}")
            }
        }
    }

    // ========================================================
    // APPOINTMENTS & SERVICES
    // ========================================================

    fun addAppointment(app: Appointment) {
        _appointments.value = listOf(app) + _appointments.value
    }

    fun updateAppointmentStatus(appId: String, status: AppointmentStatus) {
        _appointments.value = _appointments.value.map {
            if (it.id == appId) it.copy(status = status) else it
        }
    }

    fun addService(service: ServiceItem) {
        _services.value = _services.value + service
    }

    // ========================================================
    // LOYALTY & REWARDS
    // ========================================================

    fun addLoyaltyReward(reward: LoyaltyReward) {
        _loyaltyRewards.value = _loyaltyRewards.value + reward
    }

    fun redeemReward(customerId: String, rewardId: String): Boolean {
        val cust = _customers.value.firstOrNull { it.id == customerId } ?: return false
        val reward = _loyaltyRewards.value.firstOrNull { it.id == rewardId } ?: return false
        if (cust.loyaltyPoints >= reward.requiredPoints) {
            val updated = cust.copy(loyaltyPoints = cust.loyaltyPoints - reward.requiredPoints)
            _customers.value = _customers.value.map { if (it.id == customerId) updated else it }
            return true
        }
        return false
    }

    // ========================================================
    // POS SALES
    // ========================================================

    fun createPosSale(
        itemsSummary: String,
        amount: Double,
        method: PaymentMethod,
        customerName: String?,
        bizId: String? = null
    ): SaleRecord {
        val targetBizId = bizId ?: _activeBusinessId.value
        val receiptNo = "FIS-${System.currentTimeMillis().toString().takeLast(6)}"
        val sale = SaleRecord(
            businessId = targetBizId,
            receiptNo = receiptNo,
            itemsSummary = itemsSummary,
            totalAmount = amount,
            paymentMethod = method,
            customerName = customerName
        )
        _sales.value = listOf(sale) + _sales.value
        return sale
    }

    // ========================================================
    // QUOTES
    // ========================================================

    fun addQuote(quote: Quote) {
        _quotes.value = listOf(quote) + _quotes.value
    }

    fun updateQuoteStatus(quoteId: String, status: QuoteStatus) {
        _quotes.value = _quotes.value.map {
            if (it.id == quoteId) it.copy(status = status) else it
        }
    }

    // ========================================================
    // CAMPAIGNS
    // ========================================================

    fun addCampaign(campaign: Campaign) {
        _campaigns.value = listOf(campaign) + _campaigns.value
    }

    fun toggleCampaign(campaignId: String) {
        _campaigns.value = _campaigns.value.map {
            if (it.id == campaignId) it.copy(isActive = !it.isActive) else it
        }
    }

    // ========================================================
    // STAFF & ROLES
    // ========================================================

    fun addStaff(employee: Employee) {
        _staff.value = _staff.value + employee
        syncStaffToFirebase(employee)
    }

    fun updateStaff(employee: Employee) {
        _staff.value = _staff.value.map { if (it.id == employee.id) employee else it }
        syncStaffToFirebase(employee)
    }

    fun deleteStaff(staffId: String) {
        _staff.value = _staff.value.filter { it.id != staffId }
    }

    private fun syncStaffToFirebase(employee: Employee) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("staff").document(employee.id).set(
                    mapOf(
                        "id" to employee.id,
                        "businessId" to employee.businessId,
                        "name" to employee.name,
                        "email" to employee.email,
                        "phone" to employee.phone,
                        "role" to employee.role.name,
                        "permissions" to employee.permissions.map { it.name },
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            } catch (e: Exception) {
                android.util.Log.w("NexoRepository", "Firestore staff sync skipped: ${e.message}")
            }
        }
    }

    // ========================================================
    // AI LOGS & USAGE
    // ========================================================

    fun recordAiGeneration(toolType: String, prompt: String, result: String) {
        val bizId = _activeBusinessId.value
        val log = AiGenerationLog(
            businessId = bizId,
            toolType = toolType,
            prompt = prompt,
            result = result
        )
        _aiLogs.value = listOf(log) + _aiLogs.value

        // Increment credit usage
        _businesses.value = _businesses.value.map {
            if (it.id == bizId) it.copy(aiCreditsUsed = it.aiCreditsUsed + 1) else it
        }
    }

    // ========================================================
    // BUSINESS ONBOARDING / CREATION
    // ========================================================

    fun createNewBusiness(
        name: String,
        type: BusinessType,
        phone: String,
        address: String,
        currency: String,
        selectedModules: Set<NexoModule>
    ): Business {
        val slug = name.lowercase().replace(" ", "-").filter { it.isLetterOrDigit() || it == '-' }
        val newBiz = Business(
            name = name,
            slug = slug,
            businessType = type,
            phone = phone,
            address = address,
            currency = currency,
            plan = PlanType.STARTER,
            enabledModules = selectedModules + NexoModule.MENU + NexoModule.ORDERS + NexoModule.SETTINGS,
            aiCreditsUsed = 0
        )
        _businesses.value = _businesses.value + newBiz
        _activeBusinessId.value = newBiz.id
        loadSeedData(newBiz.id)
        return newBiz
    }

    // ========================================================
    // PLATFORM SUPER ADMIN ACTIONS
    // ========================================================

    fun toggleBusinessSuspension(businessId: String) {
        _businesses.value = _businesses.value.map {
            if (it.id == businessId) it.copy(isSuspended = !it.isSuspended) else it
        }
    }

    fun updateBusinessPlanAdmin(businessId: String, plan: PlanType) {
        _businesses.value = _businesses.value.map {
            if (it.id == businessId) it.copy(plan = plan) else it
        }
    }

    // ========================================================
    // SEED DATA INITIALIZER
    // ========================================================

    private fun loadSeedData(bizId: String) {
        val cafeBizId = defaultBusinessId
        val istBizId = "biz-istanbul-coffee"
        val burgerBizId = "biz-burger-house"

        // Restaurant Categories across tenants
        val cafeCategories = listOf(
            Category(id = "cat-drinks", businessId = cafeBizId, name = "Kahveler & İçecekler", iconName = "LocalCafe", sortOrder = 1),
            Category(id = "cat-dessert", businessId = cafeBizId, name = "Tatlılar & Fırın", iconName = "Cake", sortOrder = 2),
            Category(id = "cat-burgers", businessId = cafeBizId, name = "Gurme Burger & Sandviç", iconName = "LunchDining", sortOrder = 3),
            Category(id = "cat-pizzas", businessId = cafeBizId, name = "Taş Fırın Pizza", iconName = "LocalPizza", sortOrder = 4),
            Category(id = "cat-pastas", businessId = cafeBizId, name = "Ev Yapımı Makarna", iconName = "DinnerDining", sortOrder = 5),
            Category(id = "cat-starters", businessId = cafeBizId, name = "Başlangıç & Kahvaltı", iconName = "Tapas", sortOrder = 6)
        )

        val istCategories = listOf(
            Category(id = "cat-ist-v60", businessId = istBizId, name = "Single Origin Demleme", iconName = "LocalCafe", sortOrder = 1),
            Category(id = "cat-ist-espresso", businessId = istBizId, name = "Espresso Bar", iconName = "Coffee", sortOrder = 2),
            Category(id = "cat-ist-pastry", businessId = istBizId, name = "Artisan Pastane", iconName = "Cake", sortOrder = 3)
        )

        val burgerCategories = listOf(
            Category(id = "cat-bh-smash", businessId = burgerBizId, name = "Smash Burgerler", iconName = "LunchDining", sortOrder = 1),
            Category(id = "cat-bh-sides", businessId = burgerBizId, name = "Çıtır Yan Lezzetler", iconName = "Tapas", sortOrder = 2),
            Category(id = "cat-bh-drinks", businessId = burgerBizId, name = "Shake & İçecekler", iconName = "LocalCafe", sortOrder = 3)
        )

        _categories.value = cafeCategories + istCategories + burgerCategories

        // Multi-tenant Products
        val cafeProducts = listOf(
            Product(
                id = "prod-latte",
                businessId = cafeBizId,
                categoryId = "cat-drinks",
                name = "Latte",
                description = "Taze çekilmiş espresso ve kadifemsi sıcak barista sütü köpüğü",
                price = 140.0,
                ingredientCost = 25.0,
                isFeatured = true,
                allergens = listOf("Laktoz"),
                recipe = listOf(
                    RecipeIngredient("Barista Sütü", "200 ml", 7.0),
                    RecipeIngredient("Arabica Espresso", "18 gr", 12.0)
                )
            ),
            Product(
                id = "prod-cheesecake",
                businessId = cafeBizId,
                categoryId = "cat-dessert",
                name = "Cheesecake",
                description = "San Sebastian yanık cheesecake, Belçika çikolatası sosu ile",
                price = 140.0,
                ingredientCost = 35.0,
                isFeatured = true,
                allergens = listOf("Gluten", "Laktoz", "Yumurta"),
                recipe = listOf(
                    RecipeIngredient("Cheesecake Dilimi", "1 porsiyon", 28.0),
                    RecipeIngredient("Belçika Çikolatası", "30 gr", 7.0)
                ),
                options = listOf(
                    ProductOption(name = "Ekstra Sıcak Çikolata", priceDiff = 25.0)
                )
            ),
            Product(
                id = "prod-01",
                businessId = cafeBizId,
                categoryId = "cat-burgers",
                name = "Trüflü Dana Burger (180 gr)",
                description = "Dinlendirilmiş dana kaburga kıyması, karamelize soğan, çift kat cheddar ve trüflü mayonez",
                price = 295.0,
                ingredientCost = 82.0,
                isFeatured = true,
                allergens = listOf("Gluten", "Laktoz", "Yumurta")
            ),
            Product(
                id = "prod-02",
                businessId = cafeBizId,
                categoryId = "cat-pizzas",
                name = "Margherita Verace Pizza",
                description = "San Marzano domates sosu, manda mozzarella, taze fesleğen ve sızma zeytinyağı",
                price = 260.0,
                ingredientCost = 58.0,
                isFeatured = true,
                allergens = listOf("Gluten", "Laktoz")
            ),
            Product(
                id = "prod-03",
                businessId = cafeBizId,
                categoryId = "cat-pastas",
                name = "Deniz Mahsüllü Linguine",
                description = "Karides, kalamar, midye, çeri domates ve beyaz şarap sarımsak sosu ile",
                price = 360.0,
                ingredientCost = 96.0,
                isFeatured = true,
                allergens = listOf("Gluten", "Kabuklu Deniz Canlısı")
            ),
            Product(
                id = "prod-07",
                businessId = cafeBizId,
                categoryId = "cat-drinks",
                name = "Iced Salted Caramel Latte",
                description = "Espresso, soğuk süt, buz ve ev yapımı tuzlu karamel sosu katmanı",
                price = 125.0,
                ingredientCost = 22.0,
                isFeatured = false,
                allergens = listOf("Laktoz")
            )
        )

        val istProducts = listOf(
            Product(
                id = "prod-ist-v60",
                businessId = istBizId,
                categoryId = "cat-ist-v60",
                name = "V60 Filtre Kahve (Etiyopya)",
                description = "Floral, bergamot ve şeftali notaları barındıran taze hasat çekirdek",
                price = 160.0,
                isFeatured = true
            ),
            Product(
                id = "prod-ist-cortado",
                businessId = istBizId,
                categoryId = "cat-ist-espresso",
                name = "Cortado",
                description = "Eşit oranda duble ristretto espresso ve buharda ısıtılmış süt",
                price = 130.0,
                isFeatured = true
            ),
            Product(
                id = "prod-ist-cheesecake",
                businessId = istBizId,
                categoryId = "cat-ist-pastry",
                name = "Lotus Biscoff Cheesecake",
                description = "Karamelize bisküvi tabanı ve yoğun lotus kreması",
                price = 165.0,
                isFeatured = true
            )
        )

        val burgerProducts = listOf(
            Product(
                id = "prod-bh-smash",
                businessId = burgerBizId,
                categoryId = "cat-bh-smash",
                name = "Double Smash Cheeseburger",
                description = "2x 100gr ezilmiş sulu köfte, eritilmiş Amerikan cheddar, özel relish sos",
                price = 320.0,
                isFeatured = true
            ),
            Product(
                id = "prod-bh-truffle",
                businessId = burgerBizId,
                categoryId = "cat-bh-smash",
                name = "Truffle Mushroom Burger",
                description = "Tütsülenmiş dana bacon, karamelize mantar ve trüf mayonez",
                price = 360.0,
                isFeatured = true
            ),
            Product(
                id = "prod-bh-fries",
                businessId = burgerBizId,
                categoryId = "cat-bh-sides",
                name = "Cajun Baharatlı Patates",
                description = "Çıtır fırınlanmış patates dilimleri, özel sarımsaklı dip sos",
                price = 110.0,
                isFeatured = false
            ),
            Product(
                id = "prod-bh-shake",
                businessId = burgerBizId,
                categoryId = "cat-bh-drinks",
                name = "Craft Çilekli Milkshake",
                description = "Gerçek Maraş dondurması ve taze çilek püresi",
                price = 150.0,
                isFeatured = true
            )
        )

        _products.value = cafeProducts + istProducts + burgerProducts

        // Tables for Casa Cafe (including Table 12)
        val cafeTables = (1..20).map { num ->
            TableQr(
                id = "tbl-casa-$num",
                businessId = cafeBizId,
                tableNumber = num,
                label = "Masa $num",
                secureToken = "tk-casa-$num",
                status = if (num == 12) TableStatus.EMPTY else if (num % 3 == 0) TableStatus.OCCUPIED else TableStatus.EMPTY,
                capacity = if (num == 12) 4 else if (num % 2 == 0) 4 else 2,
                qrDesignColor = "#D97706"
            )
        }

        val istTables = (1..10).map { num ->
            TableQr(
                id = "tbl-ist-$num",
                businessId = istBizId,
                tableNumber = num,
                label = "Masa $num",
                secureToken = "tk-ist-$num",
                status = TableStatus.EMPTY,
                capacity = 2,
                qrDesignColor = "#78350F"
            )
        }

        val burgerTables = (1..15).map { num ->
            TableQr(
                id = "tbl-bh-$num",
                businessId = burgerBizId,
                tableNumber = num,
                label = "Masa $num",
                secureToken = "tk-bh-$num",
                status = TableStatus.EMPTY,
                capacity = 4,
                qrDesignColor = "#DC2626"
            )
        }

        _tables.value = cafeTables + istTables + burgerTables

        // Initial Orders
        _orders.value = listOf(
            Order(
                id = "ord-initial-01",
                businessId = cafeBizId,
                orderNumber = "ORD-0001",
                tableNumber = 5,
                customerName = "Masa 5 - Canan Hanım",
                items = listOf(
                    OrderItem(productId = "prod-latte", productName = "Latte", unitPrice = 140.0, quantity = 1, total = 140.0),
                    OrderItem(productId = "prod-cheesecake", productName = "Cheesecake", unitPrice = 140.0, quantity = 1, total = 140.0)
                ),
                subtotal = 280.0,
                total = 280.0,
                status = OrderStatus.COMPLETED,
                paymentMethod = PaymentMethod.CARD
            )
        )

        // Demo Customers
        _customers.value = listOf(
            Customer(
                id = "cust-01",
                businessId = cafeBizId,
                name = "Canan Yılmaz",
                phone = "+90 532 111 2233",
                email = "canan@example.com",
                tags = listOf("VIP", "Müdavim", "Tatlı Sever"),
                notes = "Latte'sini mutlaka yulaf sütlü tercih ediyor.",
                totalSpent = 3450.0,
                orderCount = 19,
                loyaltyPoints = 345,
                lastOrderAt = System.currentTimeMillis() - 7200000
            ),
            Customer(
                id = "cust-02",
                businessId = cafeBizId,
                name = "Mehmet Kaya",
                phone = "+90 544 333 4455",
                email = "mehmet@techcorp.com",
                tags = listOf("Kurumsal", "Hafta İçi"),
                notes = "Öğle toplantıları için masa rezerve ediyor.",
                totalSpent = 5200.0,
                orderCount = 12,
                loyaltyPoints = 520,
                lastOrderAt = System.currentTimeMillis() - 86400000
            )
        )

        // Demo Stock
        _stockItems.value = listOf(
            StockItem(
                id = "stk-01",
                businessId = cafeBizId,
                sku = "KAF-ARB-1KG",
                name = "Arabica Kahve Çekirdeği (1 Kg)",
                category = "Hammadde",
                purchasePrice = 380.0,
                sellingPrice = 750.0,
                stockQuantity = 8.5,
                minimumStock = 5.0,
                unit = "Kg",
                supplier = "İstanbul Kahve İthalat A.Ş."
            ),
            StockItem(
                id = "stk-02",
                businessId = cafeBizId,
                sku = "SUT-BAR-1L",
                name = "Barista Sütü",
                category = "İçecek Malzemesi",
                purchasePrice = 28.0,
                sellingPrice = 45.0,
                stockQuantity = 32.0,
                minimumStock = 15.0,
                unit = "Litre",
                supplier = "Taze Çiftlik Süt"
            ),
            StockItem(
                id = "stk-03",
                businessId = cafeBizId,
                sku = "TAT-CHK-POR",
                name = "San Sebastian Cheesecake Porsiyon",
                category = "Pastane",
                purchasePrice = 40.0,
                sellingPrice = 140.0,
                stockQuantity = 22.0,
                minimumStock = 8.0,
                unit = "Adet",
                supplier = "Gourmet Fırın Atölyesi"
            )
        )

        // Demo Stock Movements
        _stockMovements.value = listOf(
            StockMovement(stockItemId = "stk-01", productName = "Arabica Kahve Çekirdeği", type = StockMovementType.INCREASE, quantity = 20.0, reason = "Haftalık Sevkiyat Kabulü"),
            StockMovement(stockItemId = "stk-02", productName = "Barista Tam Yağlı Süt", type = StockMovementType.DECREASE, quantity = 5.0, reason = "Tarih Geçme Fire"),
            StockMovement(stockItemId = "stk-04", productName = "San Sebastian Cheesecake", type = StockMovementType.SALE_ORDER, quantity = 8.0, reason = "Gün İçi Satış Düşümü")
        )

        // Demo Services & Appointments
        _services.value = listOf(
            ServiceItem(id = "srv-01", businessId = bizId, name = "Özel Kahve Tadım & Cupping Workshop", durationMinutes = 60, price = 450.0, description = "3 farklı orijin çekirdeğin barista eşliğinde analizi"),
            ServiceItem(id = "srv-02", businessId = bizId, name = "Toplantı Masası Rezervasyonu (1 Saat)", durationMinutes = 60, price = 250.0, description = "Projeksiyon ve priz donanımlı sessiz çalışma alanı"),
            ServiceItem(id = "srv-03", businessId = bizId, name = "Barista Başlangıç Eğitimi", durationMinutes = 120, price = 1200.0, description = "Espresso ekstraksiyonu ve latte art temel teknikleri")
        )

        _appointments.value = listOf(
            Appointment(
                businessId = bizId,
                customerName = "Mehmet Kaya",
                customerPhone = "+90 544 333 4455",
                serviceId = "srv-02",
                serviceName = "Toplantı Masası Rezervasyonu (1 Saat)",
                staffName = "Ahmet Yılmaz (Şef Barista)",
                dateString = "Bugün",
                timeString = "14:30",
                status = AppointmentStatus.CONFIRMED,
                notes = "6 kişi katılım sağlayacak, HDMI kablo rica ediliyor."
            ),
            Appointment(
                businessId = bizId,
                customerName = "Zeynep Aktaş",
                customerPhone = "+90 530 888 9900",
                serviceId = "srv-01",
                serviceName = "Özel Kahve Tadım & Cupping Workshop",
                staffName = "Ahmet Yılmaz (Şef Barista)",
                dateString = "Yarın",
                timeString = "16:00",
                status = AppointmentStatus.PENDING,
                notes = "2 kişilik katılım."
            )
        )

        // Demo Loyalty Rewards
        _loyaltyRewards.value = listOf(
            LoyaltyReward(id = "rew-01", businessId = bizId, title = "Hediye Sıcak Kahve (Espresso / Americano)", requiredPoints = 250, description = "250 puan karşılığı dilediğiniz sıcak kahve ücretsiz."),
            LoyaltyReward(id = "rew-02", businessId = bizId, title = "Hediye Dilim Tatlı veya Kruvasan", requiredPoints = 500, description = "Brownie, cheesecake veya taze kruvasan ikramı."),
            LoyaltyReward(id = "rew-03", businessId = bizId, title = "Hesapta 150 TL Anında İndirim", requiredPoints = 1000, description = "Toplam sipariş tutarından 150 TL indirim uygulanır.")
        )

        // Demo POS Sales
        _sales.value = listOf(
            SaleRecord(businessId = bizId, receiptNo = "FIS-892110", itemsSummary = "2x Caffe Latte, 1x San Sebastian", totalAmount = 405.0, paymentMethod = PaymentMethod.CARD, customerName = "Canan Yılmaz"),
            SaleRecord(businessId = bizId, receiptNo = "FIS-892109", itemsSummary = "1x Espresso Double, 1x Kruvasan", totalAmount = 220.0, paymentMethod = PaymentMethod.CASH, customerName = "Ayhan Demir"),
            SaleRecord(businessId = bizId, receiptNo = "FIS-892108", itemsSummary = "3x Americano, 2x Brownie", totalAmount = 590.0, paymentMethod = PaymentMethod.CARD, customerName = "TechCorp Ekibi")
        )

        // Demo Quotes
        _quotes.value = listOf(
            Quote(
                id = "qt-01",
                businessId = bizId,
                quoteNumber = "NEXO-TK-2026-001",
                customerName = "TechCorp Yazılım Ltd. Şti.",
                customerEmail = "satinalma@techcorp.com",
                customerPhone = "+90 216 444 0102",
                items = listOf(
                    QuoteItem(name = "Kurumsal Haftalık Ofis Kahve Paketi (5kg Çekirdek)", description = "%100 Colombia Supremo Çekirdek", quantity = 4.0, unitPrice = 650.0, total = 2600.0),
                    QuoteItem(name = "Aylık Profesyonel Espresso Makinesi Bakımı", description = "Grup başlığı temizliği ve su filtresi değişimi", quantity = 1.0, unitPrice = 1800.0, total = 1800.0),
                    QuoteItem(name = "Etkinlik İkram Hizmeti (50 Kişilik Kruvasan & Kahve Barı)", description = "Yerinde barista hizmeti dahil 4 saat", quantity = 1.0, unitPrice = 8500.0, total = 8500.0)
                ),
                subtotal = 12900.0,
                discountPercent = 10.0,
                taxPercent = 20.0,
                total = 13932.0,
                validityDate = "15 Ekim 2026",
                status = QuoteStatus.SENT
            )
        )

        // Demo Campaigns
        _campaigns.value = listOf(
            Campaign(
                id = "cmp-01",
                businessId = bizId,
                name = "Hafta İçi Tatlı Molası",
                description = "14:00 - 17:00 saatleri arası tüm tatlıların yanında Americano sadece 40 TL!",
                type = CampaignType.PERCENTAGE,
                discountValue = "%25 İndirim",
                targetCustomerSegment = "Tüm Misafirler",
                startDate = "01 Ekim 2026",
                endDate = "31 Ekim 2026",
                isActive = true
            ),
            Campaign(
                id = "cmp-02",
                businessId = bizId,
                name = "VIP Müşteri Sadakat Bonusu",
                description = "Bu hafta yapacağınız her QR masa siparişinde 2 katı sadakat puanı kazanın.",
                type = CampaignType.LOYALTY_BONUS,
                discountValue = "2x Puan",
                targetCustomerSegment = "VIP Müşteriler",
                startDate = "05 Ekim 2026",
                endDate = "12 Ekim 2026",
                isActive = true
            )
        )

        // Demo Staff
        _staff.value = listOf(
            Employee(id = "emp-01", businessId = bizId, name = "Barış Özkan", email = "baris@nexocafe.com", phone = "+90 532 999 1100", role = UserRole.OWNER),
            Employee(id = "emp-02", businessId = bizId, name = "Ayşe Kaya", email = "ayse@nexocafe.com", phone = "+90 533 888 2211", role = UserRole.MANAGER),
            Employee(id = "emp-03", businessId = bizId, name = "Ahmet Yılmaz", email = "ahmet@nexocafe.com", phone = "+90 535 777 3322", role = UserRole.KITCHEN),
            Employee(id = "emp-04", businessId = bizId, name = "Elif Çetin", email = "elif@nexocafe.com", phone = "+90 536 666 4433", role = UserRole.CASHIER),
            Employee(id = "emp-05", businessId = bizId, name = "Burak Şen", email = "burak@nexocafe.com", phone = "+90 537 555 5544", role = UserRole.EMPLOYEE)
        )
    }
}
