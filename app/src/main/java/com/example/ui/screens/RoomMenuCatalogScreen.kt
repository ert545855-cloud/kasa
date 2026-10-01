package com.example.ui.screens

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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.NexoRoomDatabase
import com.example.data.local.entity.BusinessEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.MenuItemEntity
import com.example.data.repository.NexoRepository
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * High-performance Composable screen that displays menu items retrieved directly
 * from local Room SQLite database, with category-based filtering, search, and availability controls.
 */
@Composable
fun RoomMenuCatalogScreen(
    onNavigateBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val business = NexoRepository.getActiveBusiness()
    val roomDb = remember { NexoRoomDatabase.getInstance(context) }

    // Fetch live from Room Database Flow
    val categories by roomDb.categoryDao().getCategoriesForBusiness(business.id).collectAsState(initial = emptyList())
    val menuItems by roomDb.menuItemDao().getAllMenuItemsForBusiness(business.id).collectAsState(initial = emptyList())

    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var onlyAvailableFilter by remember { mutableStateOf(false) }

    // Seed Room database safely once per business without foreign key crashes or infinite loops
    LaunchedEffect(business.id) {
        withContext(Dispatchers.IO) {
            try {
                val bizEntity = BusinessEntity(
                    id = business.id,
                    name = business.name,
                    slug = business.slug,
                    businessType = business.businessType.name,
                    phone = business.phone,
                    address = business.address,
                    currency = business.currency,
                    language = business.language,
                    plan = business.plan.name
                )
                roomDb.businessDao().insertBusiness(bizEntity)

                val repoCategories = NexoRepository.categories.value.filter { it.businessId == business.id }
                val repoProducts = NexoRepository.products.value.filter { it.businessId == business.id }

                if (repoCategories.isNotEmpty()) {
                    val catEntities = repoCategories.map { c ->
                        CategoryEntity(
                            id = c.id,
                            businessId = business.id,
                            nameTr = c.name,
                            nameEn = c.name,
                            sortOrder = c.sortOrder,
                            iconName = c.iconName,
                            isActive = c.isActive
                        )
                    }
                    roomDb.categoryDao().insertCategories(catEntities)
                }

                if (repoProducts.isNotEmpty()) {
                    val itemEntities = repoProducts.map { p ->
                        MenuItemEntity(
                            id = p.id,
                            businessId = business.id,
                            categoryId = p.categoryId,
                            name = p.name,
                            description = p.description,
                            price = p.price,
                            imageUrl = p.imageUrl,
                            isAvailable = p.isAvailable,
                            isFeatured = p.isFeatured,
                            preparationMinutes = 15,
                            allergens = p.allergens.joinToString(","),
                            foodCost = p.ingredientCost,
                            lastUpdated = System.currentTimeMillis()
                        )
                    }
                    roomDb.menuItemDao().insertMenuItems(itemEntities)
                }
            } catch (e: Exception) {
                android.util.Log.e("RoomMenuCatalog", "Room catalog initial sync notice: ${e.message}")
            }
        }
    }

    // Filter items with derived state to maximize scrolling performance
    val filteredItems = remember(menuItems, selectedCategoryId, searchQuery, onlyAvailableFilter) {
        menuItems.filter { item ->
            val matchesCategory = selectedCategoryId == null || item.categoryId == selectedCategoryId
            val matchesQuery = searchQuery.isBlank() ||
                    item.name.contains(searchQuery, ignoreCase = true) ||
                    item.description.contains(searchQuery, ignoreCase = true)
            val matchesAvailability = !onlyAvailableFilter || item.isAvailable
            matchesCategory && matchesQuery && matchesAvailability
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = "Veritabanı Menü Kataloğu",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = "Room SQLite & KSP ile yerel depolanan menü",
                    style = MaterialTheme.typography.bodySmall,
                    color = NexoTextSecondary
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = NexoSoftGreen.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(NexoSoftGreen))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Room Aktif", color = NexoSoftGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Menüde ara (Örn: Latte, Cheesecake, Burger)...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Temizle", modifier = Modifier.size(18.dp))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("room_menu_search_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedCategoryId == null,
                    onClick = { selectedCategoryId = null },
                    label = { Text("Tüm Ürünler (${menuItems.size})", fontSize = 12.sp) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("cat_chip_all")
                )
            }
            items(categories, key = { it.id }) { cat ->
                val isSelected = selectedCategoryId == cat.id
                val count = menuItems.count { it.categoryId == cat.id }
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategoryId = if (isSelected) null else cat.id },
                    label = { Text("${cat.nameTr} ($count)", fontSize = 12.sp) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("cat_chip_${cat.id}")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Items Count & Quick Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filteredItems.size} ürün listeleniyor",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = NexoTextSecondary
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Sadece Mevcutlar", fontSize = 11.sp, color = NexoTextSecondary)
                Spacer(modifier = Modifier.width(6.dp))
                Switch(
                    checked = onlyAvailableFilter,
                    onCheckedChange = { onlyAvailableFilter = it },
                    modifier = Modifier.scale(0.8f).testTag("available_filter_switch")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Card-based Menu Items List with optimizations
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (filteredItems.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(40.dp), tint = NexoTextMuted)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Aranan kritere uygun ürün bulunamadı.", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Arama terimini değiştirin veya kategori filtresini kaldırın.", fontSize = 11.sp, color = NexoTextSecondary)
                        }
                    }
                }
            } else {
                items(filteredItems, key = { it.id }) { item ->
                    RoomMenuItemCard(
                        item = item,
                        currency = business.currency,
                        onToggleAvailability = {
                            coroutineScope.launch(Dispatchers.IO) {
                                roomDb.menuItemDao().updateMenuItem(item.copy(isAvailable = !item.isAvailable))
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RoomMenuItemCard(
    item: MenuItemEntity,
    currency: String,
    onToggleAvailability: () -> Unit
) {
    val productDrawableId = when {
        item.name.contains("Latte", ignoreCase = true) -> R.drawable.img_latte
        item.name.contains("Cheesecake", ignoreCase = true) -> R.drawable.img_cheesecake
        item.name.contains("Burger", ignoreCase = true) || item.name.contains("Sandviç", ignoreCase = true) -> R.drawable.img_burger
        else -> null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("menu_card_${item.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isAvailable) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, NexoBorderLight)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail image or icon
            if (productDrawableId != null) {
                Image(
                    painter = painterResource(id = productDrawableId),
                    contentDescription = item.name,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (item.categoryId.contains("drink") || item.categoryId.contains("coffee"))
                            Icons.Default.LocalCafe
                        else if (item.categoryId.contains("dessert"))
                            Icons.Default.Cake
                        else
                            Icons.Default.RestaurantMenu,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (item.isFeatured) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = NexoGold.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "ŞEFİN SEÇİMİ",
                                color = NexoGold,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (item.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.description,
                        fontSize = 11.sp,
                        color = NexoTextSecondary,
                        lineHeight = 14.sp,
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Detail badges (Preparation time, Allergens, Food cost)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = RoundedCornerShape(4.dp), color = NexoBorderSubtle) {
                        Text(
                            text = "⏱ ${item.preparationMinutes} dk",
                            fontSize = 10.sp,
                            color = NexoTextSecondary,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }

                    if (item.allergens.isNotBlank()) {
                        Surface(shape = RoundedCornerShape(4.dp), color = NexoRoseRed.copy(alpha = 0.1f)) {
                            Text(
                                text = "Alerjen: ${item.allergens}",
                                fontSize = 9.sp,
                                color = NexoRoseRed,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (item.foodCost > 0) {
                        val margin = if (item.price > 0) ((item.price - item.foodCost) / item.price * 100).toInt() else 0
                        Surface(shape = RoundedCornerShape(4.dp), color = NexoSoftGreen.copy(alpha = 0.1f)) {
                            Text(
                                text = "Maliyet: %.0f %s (%%%d Kâr)".format(item.foodCost, currency, margin),
                                fontSize = 9.sp,
                                color = NexoSoftGreen,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Price & Availability Toggle
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "%.2f %s".format(item.price, currency),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    onClick = onToggleAvailability,
                    shape = RoundedCornerShape(6.dp),
                    color = if (item.isAvailable) NexoSoftGreen.copy(alpha = 0.15f) else NexoRoseRed.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (item.isAvailable) "Mevcut" else "Tükendi",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isAvailable) NexoSoftGreen else NexoRoseRed,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}
