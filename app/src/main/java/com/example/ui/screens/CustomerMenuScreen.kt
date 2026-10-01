package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import com.example.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.Product
import com.example.data.model.TableStatus
import com.example.data.repository.CartItemInput
import com.example.data.repository.NexoRepository
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

data class CustomerCartItem(
    val product: Product,
    var selectedVariantId: String? = null,
    var selectedOptionIds: List<String> = emptyList(),
    var quantity: Int = 1
) {
    val unitPrice: Double
        get() {
            val variantExtra = product.variants.firstOrNull { it.id == selectedVariantId }?.priceDiff ?: 0.0
            val optionsExtra = product.options.filter { selectedOptionIds.contains(it.id) }.sumOf { it.priceDiff }
            return product.price + variantExtra + optionsExtra
        }
    val totalPrice: Double get() = unitPrice * quantity
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerMenuScreen(
    initialTableNumber: Int = 12,
    onBackToDashboard: () -> Unit
) {
    val allBusinesses by NexoRepository.businesses.collectAsState()
    val allCategories by NexoRepository.categories.collectAsState()
    val allProducts by NexoRepository.products.collectAsState()
    val allOrders by NexoRepository.orders.collectAsState()

    var activeWebsiteBusinessId by remember {
        mutableStateOf(NexoRepository.getActiveBusiness().id)
    }

    val currentBiz = allBusinesses.firstOrNull { it.id == activeWebsiteBusinessId }
        ?: NexoRepository.getActiveBusiness()

    val tenantCategories = allCategories.filter { it.businessId == currentBiz.id }
    val tenantProducts = allProducts.filter { it.businessId == currentBiz.id }
    val tenantOrders = allOrders.filter { it.businessId == currentBiz.id }

    var selectedTableNum by remember { mutableIntStateOf(if (initialTableNumber > 0) initialTableNumber else 12) }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Cart state
    val cartItems = remember { mutableStateListOf<CustomerCartItem>() }
    var showCartSheet by remember { mutableStateOf(false) }
    var showProductModal by remember { mutableStateOf<Product?>(null) }
    var customerNameInput by remember { mutableStateOf("") }
    var customerPhoneInput by remember { mutableStateOf("") }
    var orderNotesInput by remember { mutableStateOf("") }

    var lastPlacedOrderId by remember { mutableStateOf<String?>(null) }
    var showTenantSwitcherDialog by remember { mutableStateOf(false) }
    var showTablePickerDialog by remember { mutableStateOf(false) }
    var showOrderHistorySheet by remember { mutableStateOf(false) }

    // Real-time order tracker subscription (Zero page refresh needed!)
    val liveActiveOrder = tenantOrders.firstOrNull { it.id == lastPlacedOrderId }

    val activeCategory = selectedCategoryId ?: tenantCategories.firstOrNull()?.id

    val filteredProducts = tenantProducts.filter { prod ->
        val matchesQuery = searchQuery.isBlank() ||
                prod.name.contains(searchQuery, ignoreCase = true) ||
                prod.description.contains(searchQuery, ignoreCase = true)
        val matchesCategory = if (searchQuery.isNotBlank() && selectedCategoryId == null) {
            true
        } else {
            activeCategory == null || prod.categoryId == activeCategory
        }
        matchesQuery && matchesCategory
    }

    val cartTotal = cartItems.sumOf { it.totalPrice }
    val cartCount = cartItems.sumOf { it.quantity }

    val brandAccentColor = try {
        Color(android.graphics.Color.parseColor(currentBiz.brandColorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
            ) {
                // Public Browser Address Simulation Bar (casa-cafe.nexo.business)
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = onBackToDashboard,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", modifier = Modifier.size(18.dp))
                                }
                                Text(
                                    text = "Müşteri Web Sitesi",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NexoTextSecondary
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                // Restaurant Switcher button
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = brandAccentColor.copy(alpha = 0.15f),
                                    modifier = Modifier.clickable { showTenantSwitcherDialog = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Storefront, contentDescription = null, tint = brandAccentColor, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(currentBiz.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = brandAccentColor)
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = brandAccentColor, modifier = Modifier.size(14.dp))
                                    }
                                }

                                // Table selector button
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = NexoIndigoPrimary.copy(alpha = 0.15f),
                                    modifier = Modifier.clickable { showTablePickerDialog = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.QrCode2, contentDescription = null, tint = NexoIndigoLight, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Masa $selectedTableNum", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NexoIndigoLight)
                                    }
                                }
                            }
                        }

                        // URL input bar
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, NexoBorderSubtle),
                            modifier = Modifier.fillMaxWidth().height(32.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = "SSL Secure", tint = NexoEmeraldLight, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "https://${currentBiz.websiteDomain}/?table=$selectedTableNum",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = NexoTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Yenile",
                                    tint = NexoTextSecondary,
                                    modifier = Modifier.size(14.dp).clickable {
                                        searchQuery = ""
                                    }
                                )
                            }
                        }
                    }
                }

                // Restaurant Public Header & Search
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(brandAccentColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    currentBiz.name.take(1),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = currentBiz.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = currentBiz.tagline,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NexoTextSecondary
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = brandAccentColor.copy(alpha = 0.12f),
                                modifier = Modifier
                                    .clickable { showOrderHistorySheet = true }
                                    .testTag("customer_order_history_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.History,
                                        contentDescription = "Sipariş Geçmişim",
                                        modifier = Modifier.size(14.dp),
                                        tint = brandAccentColor
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Geçmişim (${tenantOrders.size})",
                                        color = brandAccentColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = brandAccentColor.copy(alpha = 0.18f),
                                modifier = Modifier.clickable { showTablePickerDialog = true }
                            ) {
                                Text(
                                    text = "Masa $selectedTableNum",
                                    color = brandAccentColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Real-Time Search Bar with Instant Result Feedback
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Menüde ara (Örn: Latte, Cheesecake, Burger)...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Aramayı Temizle", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("customer_menu_search"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (searchQuery.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "\"$searchQuery\" için ${filteredProducts.size} sonuç listeleniyor",
                                fontSize = 11.sp,
                                color = brandAccentColor,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Aramayı Sıfırla",
                                fontSize = 11.sp,
                                color = NexoTextSecondary,
                                modifier = Modifier.clickable { searchQuery = "" }
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (cartCount > 0) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    border = BorderStroke(1.dp, NexoBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "$cartCount Ürün Sepette",
                                style = MaterialTheme.typography.bodySmall,
                                color = NexoTextSecondary
                            )
                            Text(
                                text = "%.0f %s".format(cartTotal, currentBiz.currency),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = brandAccentColor
                            )
                        }

                        Button(
                            onClick = { showCartSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = brandAccentColor),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("open_cart_sheet_button")
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sepeti Gör & Sipariş Ver", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // ========================================================
            // 1. REAL-TIME ORDER STATUS SYNCHRONIZATION CARD (CUJ)
            // ========================================================
            if (liveActiveOrder != null) {
                item {
                    RealTimeOrderTrackerCard(
                        order = liveActiveOrder,
                        currency = currentBiz.currency,
                        onDismiss = { lastPlacedOrderId = null }
                    )
                }
            }

            // ========================================================
            // 2. 1-CLICK PROMPT TEST ORDER (CUJ SHORTCUT)
            // ========================================================
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("one_click_test_order_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = brandAccentColor.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, brandAccentColor.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = brandAccentColor, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Prompt Senaryosu Hızlı Test (Masa $selectedTableNum)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = brandAccentColor
                                )
                            }
                            Surface(shape = RoundedCornerShape(6.dp), color = brandAccentColor.copy(alpha = 0.15f)) {
                                Text("CANLI SENKRON", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = brandAccentColor, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• 2 × Latte (₺280)\n• 1 × Cheesecake (₺140)\nToplam: ₺420 (Masa $selectedTableNum)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                val latte = tenantProducts.firstOrNull { it.id == "prod-latte" }
                                    ?: tenantProducts.firstOrNull { it.name.contains("Latte", ignoreCase = true) }
                                    ?: tenantProducts.first()

                                val cheesecake = tenantProducts.firstOrNull { it.id == "prod-cheesecake" }
                                    ?: tenantProducts.firstOrNull { it.name.contains("Cheesecake", ignoreCase = true) }
                                    ?: tenantProducts.last()

                                val testInputs = listOf(
                                    CartItemInput(productId = latte.id, quantity = 2),
                                    CartItemInput(productId = cheesecake.id, quantity = 1)
                                )

                                val result = NexoRepository.createCustomerOrder(
                                    businessId = currentBiz.id,
                                    tableNumber = selectedTableNum,
                                    cartItems = testInputs,
                                    customerName = "Masa $selectedTableNum Misafiri",
                                    customerPhone = "+90 532 999 1048",
                                    notes = "Kahveler sıcak olsun lütfen"
                                )
                                result.onSuccess { order ->
                                    lastPlacedOrderId = order.id
                                    cartItems.clear()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = brandAccentColor),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(40.dp).testTag("quick_order_latte_cheesecake_btn")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SİPARİŞ VER (2x Latte, 1x Cheesecake • ₺420)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Visual Hero Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, brandAccentColor.copy(alpha = 0.25f))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_restaurant),
                            contentDescription = "Restoran Görseli",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(14.dp)
                        ) {
                            Text(
                                text = currentBiz.name,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "${currentBiz.tagline} • Masa $selectedTableNum Menüsü",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Categories horizontal bar
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(tenantCategories) { cat ->
                        val isSelected = cat.id == activeCategory
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategoryId = cat.id },
                            label = {
                                Text(
                                    cat.name,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = brandAccentColor,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Products list
            items(filteredProducts) { prod ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showProductModal = prod }
                        .testTag("product_card_${prod.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, NexoBorderSubtle)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val productDrawableId = when {
                            prod.name.contains("Latte", ignoreCase = true) || prod.id.contains("latte") -> R.drawable.img_latte
                            prod.name.contains("Cheesecake", ignoreCase = true) || prod.id.contains("cheesecake") -> R.drawable.img_cheesecake
                            prod.name.contains("Burger", ignoreCase = true) || prod.name.contains("Sandviç", ignoreCase = true) -> R.drawable.img_burger
                            else -> null
                        }

                        if (productDrawableId != null) {
                            Image(
                                painter = painterResource(id = productDrawableId),
                                contentDescription = prod.name,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(brandAccentColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (prod.categoryId.contains("drink") || prod.categoryId.contains("coffee") || prod.categoryId.contains("v60"))
                                        Icons.Default.LocalCafe
                                    else if (prod.categoryId.contains("dessert") || prod.categoryId.contains("pastry"))
                                        Icons.Default.Cake
                                    else
                                        Icons.Default.RestaurantMenu,
                                    contentDescription = null,
                                    tint = brandAccentColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = prod.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (prod.isFeatured) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = NexoAmber.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            "POPÜLER",
                                            color = NexoAmber,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = prod.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = NexoTextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "%.0f %s".format(prod.price, currentBiz.currency),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = brandAccentColor
                            )
                        }

                        Button(
                            onClick = { showProductModal = prod },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = brandAccentColor),
                            modifier = Modifier.testTag("add_product_${prod.id}")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ekle", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(60.dp)) }
        }
    }

    // Product Customization Modal
    if (showProductModal != null) {
        val prod = showProductModal!!
        var quantity by remember { mutableIntStateOf(1) }
        var selectedVariant by remember { mutableStateOf(prod.variants.firstOrNull()?.id) }
        val selectedOptions = remember { mutableStateListOf<String>() }

        val currentUnitPrice = remember(selectedVariant, selectedOptions.toList()) {
            val variantExtra = prod.variants.firstOrNull { it.id == selectedVariant }?.priceDiff ?: 0.0
            val optionsExtra = prod.options.filter { selectedOptions.contains(it.id) }.sumOf { it.priceDiff }
            prod.price + variantExtra + optionsExtra
        }

        AlertDialog(
            onDismissRequest = { showProductModal = null },
            confirmButton = {},
            title = null,
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        IconButton(onClick = { showProductModal = null }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Kapat")
                        }
                    }
                    Text(prod.description, fontSize = 12.sp, color = NexoTextSecondary)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Quantity selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Adet:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedIconButton(
                                onClick = { if (quantity > 1) quantity-- },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = "$quantity",
                                modifier = Modifier.padding(horizontal = 14.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            OutlinedIconButton(
                                onClick = { quantity++ },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val newItem = CustomerCartItem(
                                product = prod,
                                selectedVariantId = selectedVariant,
                                selectedOptionIds = selectedOptions.toList(),
                                quantity = quantity
                            )
                            cartItems.add(newItem)
                            showProductModal = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = brandAccentColor),
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Sepete Ekle (%.0f %s)".format(currentUnitPrice * quantity, currentBiz.currency), fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }

    // Checkout Bottom Sheet
    if (showCartSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCartSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Masa $selectedTableNum Sipariş Özeti",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "${currentBiz.name} • ${cartItems.sumOf { it.quantity }} Ürün",
                            fontSize = 12.sp,
                            color = NexoTextSecondary
                        )
                    }
                    IconButton(onClick = { showCartSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (cartItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.RemoveShoppingCart, contentDescription = null, tint = NexoTextMuted, modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Sepetiniz boş", fontWeight = FontWeight.Bold, color = NexoTextSecondary)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(cartItems) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            item.product.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            "Birim: %.0f %s".format(item.unitPrice, currentBiz.currency),
                                            fontSize = 11.sp,
                                            color = NexoTextSecondary
                                        )
                                    }

                                    // Quantity +/- Controls
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.surface,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clickable {
                                                    if (item.quantity > 1) {
                                                        item.quantity--
                                                    } else {
                                                        cartItems.remove(item)
                                                    }
                                                }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.Remove, contentDescription = "Azalt", modifier = Modifier.size(14.dp))
                                            }
                                        }

                                        Text(
                                            text = "${item.quantity}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(horizontal = 10.dp)
                                        )

                                        Surface(
                                            shape = CircleShape,
                                            color = brandAccentColor.copy(alpha = 0.15f),
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clickable { item.quantity++ }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.Add, contentDescription = "Artır", tint = brandAccentColor, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }

                                    Text(
                                        "%.0f %s".format(item.totalPrice, currentBiz.currency),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = brandAccentColor
                                    )

                                    IconButton(
                                        onClick = { cartItems.remove(item) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Sil", tint = NexoRose, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Guest Info Inputs
                    OutlinedTextField(
                        value = customerNameInput,
                        onValueChange = { customerNameInput = it },
                        label = { Text("Adınız (İsteğe bağlı)", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = customerPhoneInput,
                        onValueChange = { customerPhoneInput = it },
                        label = { Text("Telefon (Sadakat puanı için)", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = orderNotesInput,
                        onValueChange = { orderNotesInput = it },
                        label = { Text("Mutfak / Garson Notu (Az buzlu, ekstra sos vb.)", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Toplam Ödenecek:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("KDV Dahil", fontSize = 10.sp, color = NexoTextSecondary)
                        }
                        Text(
                            "%.0f %s".format(cartTotal, currentBiz.currency),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = brandAccentColor
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val cartInputs = cartItems.map {
                                CartItemInput(
                                    productId = it.product.id,
                                    variantId = it.selectedVariantId,
                                    selectedOptionIds = it.selectedOptionIds,
                                    quantity = it.quantity
                                )
                            }
                            val result = NexoRepository.createCustomerOrder(
                                businessId = currentBiz.id,
                                tableNumber = selectedTableNum,
                                cartItems = cartInputs,
                                customerName = customerNameInput.ifBlank { "Masa $selectedTableNum Misafiri" },
                                customerPhone = customerPhoneInput,
                                notes = orderNotesInput
                            )
                            result.onSuccess { newOrder ->
                                lastPlacedOrderId = newOrder.id
                                cartItems.clear()
                                showCartSheet = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_order_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = brandAccentColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "SİPARİŞİ ONAYLA VE GÖNDER (%.0f %s)".format(cartTotal, currentBiz.currency),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

    // Customer Order History Bottom Sheet
    if (showOrderHistorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showOrderHistorySheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            tint = brandAccentColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sipariş Geçmişim",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    IconButton(onClick = { showOrderHistorySheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat")
                    }
                }

                Text(
                    text = "${currentBiz.name} — Masa $selectedTableNum ve geçmiş siparişleriniz",
                    fontSize = 12.sp,
                    color = NexoTextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (tenantOrders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = NexoTextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "Henüz verilmiş bir siparişiniz yok.",
                                fontWeight = FontWeight.Bold,
                                color = NexoTextPrimary
                            )
                            Text(
                                "Menüden lezzetleri sepete ekleyip hemen sipariş verebilirsiniz.",
                                fontSize = 12.sp,
                                color = NexoTextSecondary
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(tenantOrders.reversed()) { pastOrder ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Sipariş #${pastOrder.orderNumber}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        StatusBadge(status = pastOrder.status)
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Masa ${pastOrder.tableNumber} • ${pastOrder.customerName ?: "Misafir"}",
                                        fontSize = 11.sp,
                                        color = NexoTextSecondary
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(color = NexoBorderSubtle)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    pastOrder.items.forEach { orderItem ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                "${orderItem.quantity}x ${orderItem.productName}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                "%.0f %s".format(orderItem.total, currentBiz.currency),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Toplam Tutar:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(
                                            "%.0f %s".format(pastOrder.total, currentBiz.currency),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp,
                                            color = brandAccentColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Tenant Switcher Dialog (Testing different restaurants)
    if (showTenantSwitcherDialog) {
        AlertDialog(
            onDismissRequest = { showTenantSwitcherDialog = false },
            confirmButton = {},
            title = { Text("Restoran Web Sitesi Seçin", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    allBusinesses.forEach { b ->
                        val isSelected = b.id == currentBiz.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    activeWebsiteBusinessId = b.id
                                    NexoRepository.switchBusiness(b.id)
                                    showTenantSwitcherDialog = false
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) brandAccentColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = if (isSelected) BorderStroke(1.5.dp, brandAccentColor) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    b.name,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(b.websiteDomain, fontSize = 11.sp, color = NexoTextSecondary)
                            }
                        }
                    }
                }
            }
        )
    }

    // Table Picker Dialog (Testing Table 12 or others)
    if (showTablePickerDialog) {
        AlertDialog(
            onDismissRequest = { showTablePickerDialog = false },
            confirmButton = {},
            title = { Text("Masa QR Kodu Seçin", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 280.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val tablesList = listOf(12, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20)
                    items(tablesList) { num ->
                        val isSel = num == selectedTableNum
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) NexoIndigoPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSel) BorderStroke(1.dp, NexoIndigoLight) else null,
                            modifier = Modifier.fillMaxWidth().clickable {
                                selectedTableNum = num
                                showTablePickerDialog = false
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Masa $num", fontWeight = FontWeight.Bold)
                                if (num == 12) {
                                    Text("Örnek Masa", fontSize = 11.sp, color = NexoEmeraldLight, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}

// ========================================================
// REAL-TIME ORDER STATUS TRACKER COMPOSABLE
// ========================================================
@Composable
fun RealTimeOrderTrackerCard(
    order: Order,
    currency: String,
    onDismiss: () -> Unit
) {
    val statusColor by animateColorAsState(
        targetValue = when (order.status) {
            OrderStatus.PENDING -> NexoAmber
            OrderStatus.ACCEPTED -> NexoIndigoLight
            OrderStatus.PREPARING -> NexoPurple
            OrderStatus.READY -> NexoEmeraldLight
            OrderStatus.COMPLETED -> NexoEmerald
            OrderStatus.CANCELLED -> NexoRose
        },
        label = "statusColor"
    )

    val statusTitle = when (order.status) {
        OrderStatus.PENDING -> "Siparişiniz Restorana İletildi"
        OrderStatus.ACCEPTED -> "Siparişiniz Onaylandı"
        OrderStatus.PREPARING -> "Siparişiniz Hazırlanıyor"
        OrderStatus.READY -> "Siparişiniz Hazır!"
        OrderStatus.COMPLETED -> "Sipariş Tamamlandı"
        OrderStatus.CANCELLED -> "Sipariş İptal Edildi"
    }

    val statusSubtitle = when (order.status) {
        OrderStatus.PENDING -> "Restoran sistemine anında düştü. Mutfak onayı bekleniyor..."
        OrderStatus.ACCEPTED -> "Siparişiniz onaylandı. Barista ve şef hazırlığa başladı."
        OrderStatus.PREPARING -> "Siparişiniz mutfakta özenle hazırlanıyor."
        OrderStatus.READY -> "Siparişiniz hazır. Garson masanıza servis ediyor."
        OrderStatus.COMPLETED -> "Sipariş masanıza teslim edildi. Afiyet olsun!"
        OrderStatus.CANCELLED -> "Sipariş restoranca iptal edildi."
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("real_time_order_tracker_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, statusColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Visual Order Status Banner
            Image(
                painter = painterResource(id = R.drawable.img_order_success),
                contentDescription = "Sipariş Takibi",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Header: Order ID & Real-time Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ORDER #${order.orderNumber}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = when (order.status) {
                            OrderStatus.PENDING -> "NEW"
                            OrderStatus.ACCEPTED -> "CONFIRMED"
                            OrderStatus.PREPARING -> "PREPARING"
                            OrderStatus.READY -> "READY"
                            OrderStatus.COMPLETED -> "COMPLETED"
                            OrderStatus.CANCELLED -> "CANCELLED"
                        },
                        color = statusColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Prominent Status Message Banner
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = statusColor.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (order.status) {
                            OrderStatus.PENDING -> Icons.Default.Schedule
                            OrderStatus.ACCEPTED -> Icons.Default.ThumbUp
                            OrderStatus.PREPARING -> Icons.Default.SoupKitchen
                            OrderStatus.READY -> Icons.Default.RoomService
                            OrderStatus.COMPLETED -> Icons.Default.Celebration
                            OrderStatus.CANCELLED -> Icons.Default.Close
                        },
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = statusTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = statusColor
                        )
                        Text(
                            text = statusSubtitle,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 5-Stage Stepper Bar
            val currentStep = when (order.status) {
                OrderStatus.PENDING -> 1
                OrderStatus.ACCEPTED -> 2
                OrderStatus.PREPARING -> 3
                OrderStatus.READY -> 4
                OrderStatus.COMPLETED -> 5
                OrderStatus.CANCELLED -> 0
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val stepNames = listOf("İletildi", "Onaylandı", "Hazırlanıyor", "Hazır", "Tamamlandı")
                stepNames.forEachIndexed { index, name ->
                    val stepNum = index + 1
                    val isDone = currentStep >= stepNum
                    val isCurrent = currentStep == stepNum

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isDone) statusColor else NexoBorderSubtle
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDone) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            } else {
                                Text("$stepNum", fontSize = 10.sp, color = NexoTextSecondary, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = name,
                            fontSize = 9.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrent) statusColor else NexoTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = NexoBorderSubtle)
            Spacer(modifier = Modifier.height(10.dp))

            // Order items summary
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                order.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${item.quantity} × ${item.productName}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("%.0f %s".format(item.total, currency), fontSize = 12.sp, color = NexoTextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Toplam Tutar:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("%.0f %s".format(order.total, currency), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = statusColor)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚡ Sayfa yenilemeye gerek yoktur (Canlı Dinleyici Aktif)",
                    fontSize = 10.sp,
                    color = NexoTextSecondary
                )
                TextButton(
                    onClick = onDismiss,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Kapat", fontSize = 11.sp)
                }
            }
        }
    }
}
