package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.StockItem
import com.example.data.model.StockMovementType
import com.example.data.repository.NexoRepository
import com.example.ui.components.StatCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockScreen() {
    val stockItems by NexoRepository.stockItems.collectAsState()
    val stockMovements by NexoRepository.stockMovements.collectAsState()
    val business = NexoRepository.getActiveBusiness()

    var searchQuery by remember { mutableStateOf("") }
    var selectedStockItemForAdjust by remember { mutableStateOf<StockItem?>(null) }
    var showAddStockDialog by remember { mutableStateOf(false) }

    val lowStockCount = stockItems.count { it.isLowStock }
    val totalStockValue = stockItems.sumOf { it.stockQuantity * it.purchasePrice }

    val filteredItems = stockItems.filter {
        searchQuery.isBlank() ||
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.sku.contains(searchQuery, ignoreCase = true) ||
        it.category.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddStockDialog = true },
                containerColor = NexoIndigoPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_stock_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Yeni Stok Kalemi Ekle")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(14.dp)
        ) {
            Text(
                text = "Stok & Envanter Takibi",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Kritik stok uyarıları, birim maliyetler ve depo hareketleri",
                style = MaterialTheme.typography.bodySmall,
                color = NexoDarkTextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // KPI Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Kritik Stok Uyarısı",
                    value = "$lowStockCount Kalem",
                    subtitle = if (lowStockCount > 0) "Acil sipariş gerekiyor" else "Stoklar yeterli",
                    icon = Icons.Default.WarningAmber,
                    iconColor = if (lowStockCount > 0) NexoRose else NexoEmeraldLight,
                    badgeColor = if (lowStockCount > 0) NexoRose.copy(alpha = 0.15f) else NexoEmerald.copy(alpha = 0.15f),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Depo Değeri",
                    value = "%.0f %s".format(totalStockValue, business.currency),
                    subtitle = "${stockItems.size} Aktif SKU",
                    icon = Icons.Default.Inventory2,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("SKU, ürün adı veya kategori ara...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredItems) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.isLowStock) NexoRose.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = if (item.isLowStock) ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(NexoRose)
                        ) else null
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(item.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        if (item.isLowStock) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = NexoRose.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    "KRİTİK",
                                                    color = NexoRose,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text("SKU: ${item.sku} • ${item.category}", fontSize = 11.sp, color = NexoDarkTextSecondary)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${item.stockQuantity} ${item.unit}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = if (item.isLowStock) NexoRose else NexoEmeraldLight
                                    )
                                    Text(
                                        text = "Min: ${item.minimumStock} ${item.unit}",
                                        fontSize = 10.sp,
                                        color = NexoDarkTextMuted
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Alış: %.2f %s • Tedarikçi: %s".format(item.purchasePrice, business.currency, item.supplier),
                                    fontSize = 11.sp,
                                    color = NexoDarkTextSecondary
                                )

                                Button(
                                    onClick = { selectedStockItemForAdjust = item },
                                    colors = ButtonDefaults.buttonColors(containerColor = NexoIndigoPrimary),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Stok Düzelt", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(72.dp)) }
            }
        }
    }

    // Stock Adjust Dialog
    selectedStockItemForAdjust?.let { item ->
        var deltaStr by remember { mutableStateOf("") }
        var isAddition by remember { mutableStateOf(true) }
        var reason by remember { mutableStateOf("Depo Sevkiyatı") }

        AlertDialog(
            onDismissRequest = { selectedStockItemForAdjust = null },
            title = { Text("Stok Miktarı Güncelle") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Mevcut: ${item.stockQuantity} ${item.unit}", fontSize = 12.sp, color = NexoDarkTextSecondary)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = isAddition,
                            onClick = { isAddition = true },
                            label = { Text("+ Stok Girişi", color = if (isAddition) Color.White else NexoEmeraldLight) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NexoEmeraldDark)
                        )
                        FilterChip(
                            selected = !isAddition,
                            onClick = { isAddition = false },
                            label = { Text("- Stok Çıkışı / Fire", color = if (!isAddition) Color.White else NexoRose) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NexoRose)
                        )
                    }

                    OutlinedTextField(
                        value = deltaStr,
                        onValueChange = { deltaStr = it },
                        label = { Text("Miktar (${item.unit})") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Açıklama / Fatura No") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = deltaStr.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            val finalDelta = if (isAddition) amount else -amount
                            val type = if (isAddition) StockMovementType.INCREASE else StockMovementType.DECREASE
                            NexoRepository.adjustStock(item.id, finalDelta, type, reason)
                            selectedStockItemForAdjust = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexoIndigoPrimary)
                ) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedStockItemForAdjust = null }) {
                    Text("İptal")
                }
            }
        )
    }

    // Add Stock Item Dialog
    if (showAddStockDialog) {
        var name by remember { mutableStateOf("") }
        var sku by remember { mutableStateOf("STK-${(100..999).random()}") }
        var cat by remember { mutableStateOf("Hammadde") }
        var purPrice by remember { mutableStateOf("") }
        var qty by remember { mutableStateOf("") }
        var minQty by remember { mutableStateOf("5") }

        AlertDialog(
            onDismissRequest = { showAddStockDialog = false },
            title = { Text("Yeni Envanter Kalemi") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Ürün / Hammadde Adı") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        label = { Text("SKU Kodu") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = cat,
                        onValueChange = { cat = it },
                        label = { Text("Kategori") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = purPrice,
                            onValueChange = { purPrice = it },
                            label = { Text("Alış (${business.currency})") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = qty,
                            onValueChange = { qty = it },
                            label = { Text("Başlangıç Stok") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val p = purPrice.toDoubleOrNull() ?: 0.0
                        val q = qty.toDoubleOrNull() ?: 0.0
                        if (name.isNotBlank()) {
                            NexoRepository.addStockItem(
                                StockItem(
                                    businessId = business.id,
                                    sku = sku,
                                    name = name.trim(),
                                    category = cat,
                                    purchasePrice = p,
                                    sellingPrice = p * 1.8,
                                    stockQuantity = q,
                                    minimumStock = minQty.toDoubleOrNull() ?: 5.0
                                )
                            )
                            showAddStockDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexoIndigoPrimary)
                ) {
                    Text("Ekle")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStockDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
}
