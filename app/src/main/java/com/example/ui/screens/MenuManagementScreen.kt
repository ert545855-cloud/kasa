package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.data.model.Category
import com.example.data.model.Product
import com.example.data.model.ProductOption
import com.example.data.model.ProductVariant
import com.example.data.remote.GeminiClient
import com.example.data.repository.NexoRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuManagementScreen() {
    val categories by NexoRepository.categories.collectAsState()
    val products by NexoRepository.products.collectAsState()
    val business = NexoRepository.getActiveBusiness()
    val coroutineScope = rememberCoroutineScope()

    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }

    val activeCategory = selectedCategoryId ?: categories.firstOrNull()?.id
    val filteredProducts = products.filter {
        activeCategory == null || it.categoryId == activeCategory
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddProductDialog = true },
                containerColor = NexoIndigoPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Yeni Ürün Ekle")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Menü & Ürün Kataloğu",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${products.size} ürün, ${categories.size} kategori",
                        style = MaterialTheme.typography.bodySmall,
                        color = NexoDarkTextSecondary
                    )
                }

                Button(
                    onClick = { showAddCategoryDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = NexoIndigoLight, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kategori Ekle", color = NexoIndigoLight, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Categories list
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = cat.id == activeCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategoryId = cat.id },
                        label = { Text(cat.name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        trailingIcon = {
                            if (categories.size > 1) {
                                IconButton(
                                    onClick = { NexoRepository.deleteCategory(cat.id) },
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Kategori Sil", modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Products list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredProducts) { prod ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(NexoIndigoPrimary.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Coffee, contentDescription = null, tint = NexoIndigoLight, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = prod.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = "%.2f %s".format(prod.price, business.currency),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = NexoIndigoLight
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Switch(
                                        checked = prod.isAvailable,
                                        onCheckedChange = { NexoRepository.toggleProductAvailability(prod.id) },
                                        modifier = Modifier.testTag("toggle_product_${prod.id}")
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { NexoRepository.deleteProduct(prod.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Sil", tint = NexoRose, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = prod.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = NexoDarkTextSecondary,
                                maxLines = 2
                            )

                            if (prod.variants.isNotEmpty() || prod.options.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (prod.variants.isNotEmpty()) {
                                        Surface(shape = RoundedCornerShape(4.dp), color = NexoEmerald.copy(alpha = 0.15f)) {
                                            Text(
                                                "${prod.variants.size} Varyant",
                                                color = NexoEmeraldLight,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    if (prod.options.isNotEmpty()) {
                                        Surface(shape = RoundedCornerShape(4.dp), color = NexoPurple.copy(alpha = 0.15f)) {
                                            Text(
                                                "${prod.options.size} Opsiyon",
                                                color = NexoPurple,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(72.dp)) }
            }
        }
    }

    // Add Category Dialog
    if (showAddCategoryDialog) {
        var categoryName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Yeni Kategori Ekle") },
            text = {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Kategori Adı") },
                    placeholder = { Text("Örn: Sıcak İçecekler") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (categoryName.isNotBlank()) {
                            NexoRepository.addCategory(
                                Category(
                                    businessId = business.id,
                                    name = categoryName.trim()
                                )
                            )
                            showAddCategoryDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexoIndigoPrimary)
                ) {
                    Text("Ekle")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    // Add Product Dialog with Gemini AI description generator
    if (showAddProductDialog) {
        var prodName by remember { mutableStateOf("") }
        var prodPrice by remember { mutableStateOf("") }
        var prodDesc by remember { mutableStateOf("") }
        var selectedCatId by remember { mutableStateOf(activeCategory ?: categories.firstOrNull()?.id ?: "") }
        var isAiGenerating by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddProductDialog = false },
            title = { Text("Yeni Ürün Ekle") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = prodName,
                        onValueChange = { prodName = it },
                        label = { Text("Ürün Adı") },
                        placeholder = { Text("Örn: Karamel Macchiato") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = prodPrice,
                        onValueChange = { prodPrice = it },
                        label = { Text("Fiyat (${business.currency})") },
                        placeholder = { Text("Örn: 125") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Gemini AI Description Generator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Açıklama:", fontSize = 12.sp, color = NexoDarkTextSecondary)
                        TextButton(
                            onClick = {
                                if (prodName.isNotBlank()) {
                                    isAiGenerating = true
                                    coroutineScope.launch {
                                        val prompt = "İşletme dijital menüsü için '$prodName' adlı ürünün iştah kabartan, profesyonel ve 1-2 cümlelik açıklamasını yaz."
                                        val aiResult = GeminiClient.generateAiContent(prompt)
                                        prodDesc = aiResult
                                        isAiGenerating = false
                                        NexoRepository.recordAiGeneration("menu_description", prompt, aiResult)
                                    }
                                }
                            },
                            enabled = prodName.isNotBlank() && !isAiGenerating,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NexoIndigoLight, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isAiGenerating) "Yazılıyor..." else "✨ AI ile Yaz", fontSize = 11.sp, color = NexoIndigoLight)
                        }
                    }

                    OutlinedTextField(
                        value = prodDesc,
                        onValueChange = { prodDesc = it },
                        placeholder = { Text("Ürün içerik bilgisi ve sunum detayı...") },
                        modifier = Modifier.fillMaxWidth().height(90.dp),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val priceVal = prodPrice.toDoubleOrNull() ?: 0.0
                        if (prodName.isNotBlank() && priceVal > 0) {
                            NexoRepository.addProduct(
                                Product(
                                    businessId = business.id,
                                    categoryId = selectedCatId,
                                    name = prodName.trim(),
                                    description = prodDesc.trim(),
                                    price = priceVal,
                                    variants = listOf(
                                        ProductVariant(name = "Standart", priceDiff = 0.0),
                                        ProductVariant(name = "Büyük Boy", priceDiff = 25.0)
                                    ),
                                    options = listOf(
                                        ProductOption(name = "Ekstra Şurup", priceDiff = 15.0),
                                        ProductOption(name = "Bitkisel Süt", priceDiff = 20.0)
                                    )
                                )
                            )
                            showAddProductDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexoIndigoPrimary)
                ) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProductDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
}
