package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.TableQr
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
    initialTableNumber: Int = 3,
    onBackToDashboard: () -> Unit
) {
    val business = NexoRepository.getActiveBusiness()
    val categories by NexoRepository.categories.collectAsState()
    val products by NexoRepository.products.collectAsState()
    val tables by NexoRepository.tables.collectAsState()
    val orders by NexoRepository.orders.collectAsState()

    var selectedTableNum by remember { mutableIntStateOf(initialTableNumber) }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Cart state
    val cartItems = remember { mutableStateListOf<CustomerCartItem>() }
    var showCartSheet by remember { mutableStateOf(false) }
    var showProductModal by remember { mutableStateOf<Product?>(null) }
    var customerNameInput by remember { mutableStateOf("") }
    var customerPhoneInput by remember { mutableStateOf("") }
    var orderNotesInput by remember { mutableStateOf("") }

    var lastPlacedOrder by remember { mutableStateOf<Order?>(null) }
    var orderSuccessDialog by remember { mutableStateOf(false) }

    val activeCategory = selectedCategoryId ?: categories.firstOrNull()?.id

    val filteredProducts = products.filter {
        (activeCategory == null || it.categoryId == activeCategory) &&
        (searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true))
    }

    val cartTotal = cartItems.sumOf { it.totalPrice }
    val cartCount = cartItems.sumOf { it.quantity }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onBackToDashboard) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                            }
                            Column {
                                Text(
                                    text = business.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Müşteri QR Dijital Menü",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NexoEmeraldLight
                                )
                            }
                        }

                        // Table selector chip
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = NexoIndigoPrimary.copy(alpha = 0.15f),
                            border = ButtonDefaults.outlinedButtonBorder
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.TableRestaurant, contentDescription = null, tint = NexoIndigoLight, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Masa $selectedTableNum",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = NexoIndigoLight
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Ürün veya tatlı ara...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("customer_menu_search"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        },
        bottomBar = {
            if (cartCount > 0) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "$cartCount Ürün Seçildi",
                                style = MaterialTheme.typography.bodySmall,
                                color = NexoDarkTextSecondary
                            )
                            Text(
                                text = "%.2f %s".format(cartTotal, business.currency),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Button(
                            onClick = { showCartSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NexoEmeraldDark),
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

            // Active order tracker banner if an order is active
            if (lastPlacedOrder != null) {
                item {
                    val currentOrder = orders.firstOrNull { it.id == lastPlacedOrder?.id } ?: lastPlacedOrder!!
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = NexoIndigoPrimary.copy(alpha = 0.15f)),
                        border = ButtonDefaults.outlinedButtonBorder
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Canlı Siparişiniz (#${currentOrder.orderNumber})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${currentOrder.items.size} çeşit ürün hazırlanıyor", fontSize = 11.sp, color = NexoDarkTextSecondary)
                            }
                            StatusBadge(status = currentOrder.status)
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
                    items(categories) { cat ->
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
                                selectedContainerColor = NexoIndigoPrimary,
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
                        .clickable { showProductModal = prod },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(NexoIndigoPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.RestaurantMenu, contentDescription = null, tint = NexoIndigoLight, modifier = Modifier.size(24.dp))
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
                                            "ÖNERİLEN",
                                            color = NexoAmber,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = prod.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = NexoDarkTextSecondary,
                                maxLines = 2
                            )
                            if (prod.allergens.isNotEmpty()) {
                                Text(
                                    text = "Alerjen: " + prod.allergens.joinToString(", "),
                                    fontSize = 10.sp,
                                    color = NexoRose.copy(alpha = 0.9f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "%.2f %s".format(prod.price, business.currency),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = NexoIndigoLight
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FilledIconButton(
                                onClick = { showProductModal = prod },
                                modifier = Modifier.size(32.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = NexoIndigoPrimary)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Ekle", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // Product Customization Modal (Variants & Modifiers)
    showProductModal?.let { prod ->
        var selectedVariant by remember { mutableStateOf(prod.variants.firstOrNull()?.id) }
        val selectedOptions = remember { mutableStateListOf<String>() }
        var quantity by remember { mutableIntStateOf(1) }

        val currentUnitPrice = remember(selectedVariant, selectedOptions.toList()) {
            val varDiff = prod.variants.firstOrNull { it.id == selectedVariant }?.priceDiff ?: 0.0
            val optDiff = prod.options.filter { selectedOptions.contains(it.id) }.sumOf { it.priceDiff }
            prod.price + varDiff + optDiff
        }

        AlertDialog(
            onDismissRequest = { showProductModal = null },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(prod.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(prod.description, fontSize = 12.sp, color = NexoDarkTextSecondary)

                    Spacer(modifier = Modifier.height(14.dp))

                    // Variants
                    if (prod.variants.isNotEmpty()) {
                        Text("Porsiyon / Boyut Seçin:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            prod.variants.forEach { v ->
                                val isVSelected = selectedVariant == v.id
                                FilterChip(
                                    selected = isVSelected,
                                    onClick = { selectedVariant = v.id },
                                    label = {
                                        Text(
                                            text = if (v.priceDiff > 0) "${v.name} (+${v.priceDiff.toInt()}₺)" else v.name,
                                            fontSize = 12.sp
                                        )
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Options / Modifiers
                    if (prod.options.isNotEmpty()) {
                        Text("Ekstra / Tercih:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            prod.options.forEach { opt ->
                                val isChecked = selectedOptions.contains(opt.id)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isChecked) selectedOptions.remove(opt.id) else selectedOptions.add(opt.id)
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            if (checked) selectedOptions.add(opt.id) else selectedOptions.remove(opt.id)
                                        }
                                    )
                                    Text("${opt.name} (+${opt.priceDiff.toInt()}₺)", fontSize = 12.sp)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

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
                        colors = ButtonDefaults.buttonColors(containerColor = NexoIndigoPrimary),
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Sepete Ekle (%.2f %s)".format(currentUnitPrice * quantity, business.currency), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Checkout Bottom Sheet
    if (showCartSheet) {
        ModalBottomSheet(onDismissRequest = { showCartSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Masa $selectedTableNum Siparişiniz",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(modifier = Modifier.heightIn(max = 240.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(cartItems) { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${item.quantity}x ${item.product.name}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                val variantName = item.product.variants.firstOrNull { it.id == item.selectedVariantId }?.name
                                if (variantName != null) {
                                    Text("Boyut: $variantName", fontSize = 11.sp, color = NexoDarkTextSecondary)
                                }
                            }
                            Text("%.2f %s".format(item.totalPrice, business.currency), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            IconButton(onClick = { cartItems.remove(item) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Kaldır", tint = NexoRose, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Guest Info Inputs
                OutlinedTextField(
                    value = customerNameInput,
                    onValueChange = { customerNameInput = it },
                    label = { Text("Adınız (İsteğe bağlı)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = customerPhoneInput,
                    onValueChange = { customerPhoneInput = it },
                    label = { Text("Telefon (Sadakat puanı biriktirmek için)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = orderNotesInput,
                    onValueChange = { orderNotesInput = it },
                    label = { Text("Mutfak Notu (Örn: Çikolata sosu ılık olsun)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ödenecek Toplam:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "%.2f %s".format(cartTotal, business.currency),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = NexoIndigoLight
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Server-side calculation & order submission
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
                            businessId = business.id,
                            tableNumber = selectedTableNum,
                            cartItems = cartInputs,
                            customerName = customerNameInput,
                            customerPhone = customerPhoneInput,
                            notes = orderNotesInput
                        )
                        result.onSuccess { newOrder ->
                            lastPlacedOrder = newOrder
                            cartItems.clear()
                            showCartSheet = false
                            orderSuccessDialog = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_order_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NexoEmeraldDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Siparişi Onayla & Mutfağa İlet", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Success Confirmation Dialog
    if (orderSuccessDialog) {
        AlertDialog(
            onDismissRequest = { orderSuccessDialog = false },
            confirmButton = {
                Button(
                    onClick = { orderSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = NexoIndigoPrimary)
                ) {
                    Text("Harika!")
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Celebration, contentDescription = null, tint = NexoEmerald)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Siparişiniz Alındı!")
                }
            },
            text = {
                Text(
                    "Siparişiniz (#${lastPlacedOrder?.orderNumber}) anında işletme mutfağına ve servis ekranına iletildi. Barista ve mutfak ekibimiz hazırlığa başladı!"
                )
            }
        )
    }
}
