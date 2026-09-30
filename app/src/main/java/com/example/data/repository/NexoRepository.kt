package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class CartItemInput(
    val productId: String,
    val variantId: String? = null,
    val selectedOptionIds: List<String> = emptyList(),
    val quantity: Int = 1
)

object NexoRepository {

    private val defaultBusinessId = "biz-casa-verde-01"

    private val initialBusiness = Business(
        id = defaultBusinessId,
        name = "Casa Verde Restaurant",
        slug = "casa-verde-restaurant",
        businessType = BusinessType.RESTAURANT,
        logoUrl = null,
        phone = "+90 216 450 8899",
        address = "Kalamış Marina Cad. No: 12, Kadıköy, İstanbul",
        currency = "₺",
        language = "tr",
        plan = PlanType.PRO,
        enabledModules = NexoModule.entries.toSet(),
        branches = listOf(
            Branch(id = "branch-01", name = "Kalamış Marina (Merkez)", address = "Kalamış Marina Cad. No: 12", isMain = true),
            Branch(id = "branch-02", name = "Bodrum Yalıkavak Şubesi", address = "Yalıkavak Marina No: 4", isMain = false)
        )
    )

    private val _activeTheme = MutableStateFlow(com.example.ui.theme.RestaurantTheme.MEDITERRANEAN)
    val activeTheme: StateFlow<com.example.ui.theme.RestaurantTheme> = _activeTheme.asStateFlow()

    fun setRestaurantTheme(theme: com.example.ui.theme.RestaurantTheme) {
        _activeTheme.value = theme
    }

    private val _businesses = MutableStateFlow<List<Business>>(listOf(initialBusiness))
    val businesses: StateFlow<List<Business>> = _businesses.asStateFlow()

    private val _activeBusinessId = MutableStateFlow(defaultBusinessId)
    val activeBusinessId: StateFlow<String> = _activeBusinessId.asStateFlow()

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

        val orderCountForBiz = _orders.value.count { it.businessId == businessId } + 1
        val orderNo = "ORD-${String.format("%04d", orderCountForBiz)}"

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

        return Result.success(newOrder)
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        val order = _orders.value.firstOrNull { it.id == orderId } ?: return
        val updated = order.copy(status = newStatus)
        _orders.value = _orders.value.map { if (it.id == orderId) updated else it }

        // Deduct inventory when completed if items match stock
        if (newStatus == OrderStatus.COMPLETED) {
            order.items.forEach { item ->
                val matchingStock = _stockItems.value.firstOrNull {
                    it.businessId == order.businessId && it.name.contains(item.productName, ignoreCase = true)
                }
                if (matchingStock != null) {
                    adjustStock(matchingStock.id, -item.quantity.toDouble(), StockMovementType.SALE_ORDER, "Sipariş #${order.orderNumber}")
                }
            }
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
    }

    fun updateCustomer(customer: Customer) {
        _customers.value = _customers.value.map { if (it.id == customer.id) customer else it }
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
        customerName: String?
    ): SaleRecord {
        val bizId = _activeBusinessId.value
        val receiptNo = "FIS-${System.currentTimeMillis().toString().takeLast(6)}"
        val sale = SaleRecord(
            businessId = bizId,
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
    }

    fun updateStaff(employee: Employee) {
        _staff.value = _staff.value.map { if (it.id == employee.id) employee else it }
    }

    fun deleteStaff(staffId: String) {
        _staff.value = _staff.value.filter { it.id != staffId }
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
        // Restaurant Categories
        val catStarters = Category(id = "cat-starters", businessId = bizId, name = "Başlangıçlar & Mezeler", iconName = "Tapas", sortOrder = 1)
        val catBurgers = Category(id = "cat-burgers", businessId = bizId, name = "Gurme Burgerler", iconName = "LunchDining", sortOrder = 2)
        val catPizzas = Category(id = "cat-pizzas", businessId = bizId, name = "Taş Fırın Pizza", iconName = "LocalPizza", sortOrder = 3)
        val catPastas = Category(id = "cat-pastas", businessId = bizId, name = "Ev Yapımı Makarna", iconName = "DinnerDining", sortOrder = 4)
        val catGrill = Category(id = "cat-grill", businessId = bizId, name = "Izgaralar & Şef Spesiyalleri", iconName = "OutdoorGrill", sortOrder = 5)
        val catDesserts = Category(id = "cat-dessert", businessId = bizId, name = "Tatlılar", iconName = "Cake", sortOrder = 6)
        val catDrinks = Category(id = "cat-drinks", businessId = bizId, name = "İçecekler & Kahveler", iconName = "LocalCafe", sortOrder = 7)

        _categories.value = listOf(catStarters, catBurgers, catPizzas, catPastas, catGrill, catDesserts, catDrinks)

        _products.value = listOf(
            Product(
                id = "prod-01",
                businessId = bizId,
                categoryId = "cat-burgers",
                name = "Trüflü Dana Burger (180 gr)",
                description = "Dinlendirilmiş dana kaburga kıyması, karamelize soğan, çift kat cheddar ve trüflü mayonez",
                price = 295.0,
                ingredientCost = 82.0,
                isFeatured = true,
                allergens = listOf("Gluten", "Laktoz", "Yumurta"),
                recipe = listOf(
                    RecipeIngredient("Dana Kıyması (180g)", "180 gr", 52.0),
                    RecipeIngredient("Brioche Ekmek", "1 adet", 12.0),
                    RecipeIngredient("Cheddar Peyniri", "2 dilim", 10.0),
                    RecipeIngredient("Trüf Mayonez & Karamel Soğan", "40 gr", 8.0)
                ),
                options = listOf(
                    ProductOption(name = "Ekstra Çıtır Patates", priceDiff = 35.0),
                    ProductOption(name = "Ekstra Füme Kaburga", priceDiff = 55.0)
                )
            ),
            Product(
                id = "prod-02",
                businessId = bizId,
                categoryId = "cat-pizzas",
                name = "Margherita Verace Pizza",
                description = "San Marzano domates sosu, manda mozzarella, taze fesleğen ve sızma zeytinyağı",
                price = 260.0,
                ingredientCost = 58.0,
                isFeatured = true,
                allergens = listOf("Gluten", "Laktoz"),
                recipe = listOf(
                    RecipeIngredient("Ekşi Maya Pizza Hamuru", "250 gr", 14.0),
                    RecipeIngredient("San Marzano Domates", "90 gr", 12.0),
                    RecipeIngredient("Manda Mozzarella", "120 gr", 26.0),
                    RecipeIngredient("Fesleğen & Zeytinyağı", "20 ml", 6.0)
                )
            ),
            Product(
                id = "prod-03",
                businessId = bizId,
                categoryId = "cat-pastas",
                name = "Deniz Mahsüllü Linguine",
                description = "Karides, kalamar, midye, çeri domates ve beyaz şarap sarımsak sosu ile",
                price = 360.0,
                ingredientCost = 96.0,
                isFeatured = true,
                allergens = listOf("Gluten", "Kabuklu Deniz Canlısı"),
                recipe = listOf(
                    RecipeIngredient("Taze Yumurtalı Linguine", "160 gr", 18.0),
                    RecipeIngredient("Karides & Kalamar", "120 gr", 65.0),
                    RecipeIngredient("Sarımsak & Çeri Sos", "60 gr", 13.0)
                )
            ),
            Product(
                id = "prod-04",
                businessId = bizId,
                categoryId = "cat-grill",
                name = "Izgara Somon Fileto (220 gr)",
                description = "Taze kuşkonmaz sote, fırınlanmış bebek patates ve kapari tereyağı sosu",
                price = 420.0,
                ingredientCost = 145.0,
                isFeatured = true,
                allergens = listOf("Balık", "Laktoz"),
                recipe = listOf(
                    RecipeIngredient("Norveç Somon Fileto", "220 gr", 115.0),
                    RecipeIngredient("Taze Kuşkonmaz", "4 dal", 18.0),
                    RecipeIngredient("Bebek Patates & Kapari Sos", "100 gr", 12.0)
                )
            ),
            Product(
                id = "prod-05",
                businessId = bizId,
                categoryId = "cat-starters",
                name = "Akdeniz Burrata & İncir",
                description = "Taze İtalyan burrata peyniri, fırın incir, ceviz, taze roka ve nar ekşili balzamik glaze",
                price = 240.0,
                ingredientCost = 65.0,
                isFeatured = false,
                allergens = listOf("Laktoz", "Ceviz")
            ),
            Product(
                id = "prod-06",
                businessId = bizId,
                categoryId = "cat-dessert",
                name = "San Sebastian Cheesecake",
                description = "Karamelize üst yüzey, akışkan fırın içi ve Belçika çikolatası sosu ile",
                price = 185.0,
                ingredientCost = 38.0,
                isFeatured = true,
                allergens = listOf("Gluten", "Yumurta", "Laktoz"),
                options = listOf(
                    ProductOption(name = "Ekstra Sıcak Çikolata Sosu", priceDiff = 35.0),
                    ProductOption(name = "Antep Fıstığı Parçacıkları", priceDiff = 40.0)
                )
            ),
            Product(
                id = "prod-07",
                businessId = bizId,
                categoryId = "cat-drinks",
                name = "Iced Salted Caramel Latte",
                description = "Espresso, soğuk süt, buz ve ev yapımı tuzlu karamel sosu katmanı",
                price = 125.0,
                ingredientCost = 22.0,
                isFeatured = false,
                allergens = listOf("Laktoz")
            )
        )

        // 20 Visual Restaurant Tables
        val demoStatuses = listOf(
            TableStatus.OCCUPIED, TableStatus.EMPTY, TableStatus.ORDER_PENDING, TableStatus.EMPTY,
            TableStatus.PREPARING, TableStatus.BILL_REQUESTED, TableStatus.EMPTY, TableStatus.OCCUPIED,
            TableStatus.EMPTY, TableStatus.EMPTY, TableStatus.BILL_REQUESTED, TableStatus.OCCUPIED,
            TableStatus.RESERVED, TableStatus.EMPTY, TableStatus.PREPARING, TableStatus.EMPTY,
            TableStatus.RESERVED, TableStatus.RESERVED, TableStatus.CLEANING, TableStatus.EMPTY
        )

        _tables.value = (1..20).map { num ->
            val st = demoStatuses.getOrElse(num - 1) { TableStatus.EMPTY }
            val cap = if (num % 5 == 0) 6 else if (num % 2 == 0) 4 else 2
            TableQr(
                businessId = bizId,
                tableNumber = num,
                label = "Masa $num",
                secureToken = "tk-m$num-casa88",
                status = st,
                capacity = cap,
                qrDesignColor = "#0284C7"
            )
        }

        // Demo Customers
        _customers.value = listOf(
            Customer(
                id = "cust-01",
                businessId = bizId,
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
                businessId = bizId,
                name = "Mehmet Kaya",
                phone = "+90 544 333 4455",
                email = "mehmet@techcorp.com",
                tags = listOf("Kurumsal", "Hafta İçi"),
                notes = "Öğle toplantıları için 4-6 kişilik masa rezerve ediyor.",
                totalSpent = 5200.0,
                orderCount = 12,
                loyaltyPoints = 520,
                lastOrderAt = System.currentTimeMillis() - 86400000
            ),
            Customer(
                id = "cust-03",
                businessId = bizId,
                name = "Selin Demir",
                phone = "+90 555 777 8899",
                email = "selin@studio.design",
                tags = listOf("Yeni Müşteri"),
                notes = "San Sebastian cheesecake müdavimi.",
                totalSpent = 680.0,
                orderCount = 3,
                loyaltyPoints = 68,
                lastOrderAt = System.currentTimeMillis() - 172800000
            )
        )

        // Demo Orders
        _orders.value = listOf(
            Order(
                id = "ord-101",
                businessId = bizId,
                orderNumber = "ORD-0001",
                tableNumber = 3,
                customerName = "Masa 3 - Selin Hanım",
                items = listOf(
                    OrderItem(productId = "prod-06", productName = "San Sebastian Cheesecake", unitPrice = 185.0, quantity = 1, total = 185.0),
                    OrderItem(productId = "prod-05", productName = "Iced Latte", variantName = "Orta (Double)", unitPrice = 140.0, quantity = 1, total = 140.0)
                ),
                subtotal = 325.0,
                total = 325.0,
                status = OrderStatus.PENDING,
                notes = "Çikolata sosu sıcak ve ayrı kapta rica ediliyor."
            ),
            Order(
                id = "ord-102",
                businessId = bizId,
                orderNumber = "ORD-0002",
                tableNumber = 5,
                customerName = "Masa 5 - Canan Hanım",
                items = listOf(
                    OrderItem(productId = "prod-03", productName = "Caffe Latte", variantName = "Büyük (Venti)", selectedOptions = listOf("Yulaf Sütü"), unitPrice = 160.0, quantity = 2, total = 320.0),
                    OrderItem(productId = "prod-08", productName = "Tereyağlı Kruvasan", unitPrice = 120.0, quantity = 2, total = 240.0)
                ),
                subtotal = 560.0,
                total = 560.0,
                status = OrderStatus.PREPARING
            ),
            Order(
                id = "ord-103",
                businessId = bizId,
                orderNumber = "ORD-0003",
                tableNumber = 1,
                customerName = "Masa 1",
                items = listOf(
                    OrderItem(productId = "prod-01", productName = "Espresso", variantName = "Double", unitPrice = 100.0, quantity = 2, total = 200.0)
                ),
                subtotal = 200.0,
                total = 200.0,
                status = OrderStatus.READY
            ),
            Order(
                id = "ord-104",
                businessId = bizId,
                orderNumber = "ORD-0004",
                tableNumber = 8,
                customerName = "Masa 8",
                items = listOf(
                    OrderItem(productId = "prod-04", productName = "Cappuccino", unitPrice = 115.0, quantity = 1, total = 115.0),
                    OrderItem(productId = "prod-07", productName = "Fudge Brownie", unitPrice = 160.0, quantity = 1, total = 160.0)
                ),
                subtotal = 275.0,
                total = 275.0,
                status = OrderStatus.COMPLETED,
                paymentMethod = PaymentMethod.CARD
            )
        )

        // Demo Stock Items
        _stockItems.value = listOf(
            StockItem(
                id = "stk-01",
                businessId = bizId,
                sku = "KAF-ARB-1KG",
                name = "Arabica Kahve Çekirdeği (1 Kg)",
                category = "Hammadde",
                purchasePrice = 380.0,
                sellingPrice = 750.0,
                stockQuantity = 4.5,
                minimumStock = 10.0,
                unit = "Kg",
                supplier = "İstanbul Kahve İthalat A.Ş."
            ),
            StockItem(
                id = "stk-02",
                businessId = bizId,
                sku = "SUT-TAM-1LT",
                name = "Barista Tam Yağlı Süt (1 L)",
                category = "Süt Ürünleri",
                purchasePrice = 28.5,
                sellingPrice = 60.0,
                stockQuantity = 8.0,
                minimumStock = 15.0,
                unit = "Litre",
                supplier = "Sütaş Kurumsal"
            ),
            StockItem(
                id = "stk-03",
                businessId = bizId,
                sku = "SUT-YUL-1LT",
                name = "Barista Yulaf Sütü (1 L)",
                category = "Bitkisel Süt",
                purchasePrice = 65.0,
                sellingPrice = 130.0,
                stockQuantity = 22.0,
                minimumStock = 8.0,
                unit = "Litre",
                supplier = "Oatly Distribütör"
            ),
            StockItem(
                id = "stk-04",
                businessId = bizId,
                sku = "TAT-SAN-TEK",
                name = "Hazır San Sebastian Cheesecake Porsiyon",
                category = "Tatlı",
                purchasePrice = 85.0,
                sellingPrice = 185.0,
                stockQuantity = 3.0,
                minimumStock = 6.0,
                unit = "Porsiyon",
                supplier = "Artisan Pastane Atölyesi"
            ),
            StockItem(
                id = "stk-05",
                businessId = bizId,
                sku = "FIR-KRU-DON",
                name = "Dondurulmuş Fransız Kruvasan",
                category = "Unlu Mamul",
                purchasePrice = 45.0,
                sellingPrice = 120.0,
                stockQuantity = 35.0,
                minimumStock = 15.0,
                unit = "Adet",
                supplier = "BakeArt Bakery"
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
