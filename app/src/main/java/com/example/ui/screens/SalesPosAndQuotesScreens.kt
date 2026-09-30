package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.data.model.*
import com.example.data.repository.NexoRepository
import com.example.ui.components.StatCard
import com.example.ui.theme.*

@Composable
fun SalesPosScreen() {
    val products by NexoRepository.products.collectAsState()
    val categories by NexoRepository.categories.collectAsState()
    val sales by NexoRepository.sales.collectAsState()
    val business = NexoRepository.getActiveBusiness()

    var activeCatId by remember { mutableStateOf<String?>(null) }
    val posCart = remember { mutableStateMapOf<String, Int>() } // productId -> quantity
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.CARD) }
    var customerNameInput by remember { mutableStateOf("") }
    var lastReceiptModal by remember { mutableStateOf<SaleRecord?>(null) }

    val filteredProducts = products.filter {
        activeCatId == null || it.categoryId == activeCatId
    }

    val cartTotal = posCart.entries.sumOf { (prodId, qty) ->
        val p = products.firstOrNull { it.id == prodId }
        (p?.price ?: 0.0) * qty
    }

    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Kasa & Hızlı POS", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Tezgah satışı, hızlı sipariş alma ve tahsilat", style = MaterialTheme.typography.bodySmall, color = NexoDarkTextSecondary)
            }
            Text("Bugün: ${sales.size} Satış", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = NexoEmeraldLight)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category filter chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            item {
                FilterChip(
                    selected = activeCatId == null,
                    onClick = { activeCatId = null },
                    label = { Text("Tümü") }
                )
            }
            items(categories) { cat ->
                FilterChip(
                    selected = activeCatId == cat.id,
                    onClick = { activeCatId = cat.id },
                    label = { Text(cat.name) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // Products Touch Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1.3f).fillMaxHeight()
            ) {
                items(filteredProducts) { prod ->
                    Card(
                        onClick = {
                            val curr = posCart[prod.id] ?: 0
                            posCart[prod.id] = curr + 1
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "%.2f %s".format(prod.price, business.currency),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = NexoIndigoLight
                            )
                        }
                    }
                }
            }

            // POS Register Cart
            Card(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp).fillMaxHeight()) {
                    Text("Adisyon / Fiş", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(posCart.entries.toList()) { (pId, qty) ->
                            val prod = products.firstOrNull { it.id == pId } ?: return@items
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(prod.name, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                    Text("${qty}x • %.0f₺".format(prod.price * qty), fontSize = 10.sp, color = NexoDarkTextSecondary)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            if (qty > 1) posCart[pId] = qty - 1 else posCart.remove(pId)
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(12.dp))
                                    }
                                    IconButton(
                                        onClick = { posCart[pId] = qty + 1 },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                    Text("Ödeme Yöntemi:", fontSize = 11.sp, color = NexoDarkTextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(PaymentMethod.CARD, PaymentMethod.CASH, PaymentMethod.TRANSFER).forEach { m ->
                            FilterChip(
                                selected = selectedPaymentMethod == m,
                                onClick = { selectedPaymentMethod = m },
                                label = { Text(m.titleTr.take(5), fontSize = 10.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Toplam: %.2f %s".format(cartTotal, business.currency),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = NexoIndigoLight
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (cartTotal > 0) {
                                val summary = posCart.entries.joinToString(", ") { (pId, q) ->
                                    val pr = products.firstOrNull { it.id == pId }?.name ?: "Ürün"
                                    "${q}x $pr"
                                }
                                val sale = NexoRepository.createPosSale(
                                    itemsSummary = summary,
                                    amount = cartTotal,
                                    method = selectedPaymentMethod,
                                    customerName = customerNameInput.ifBlank { "Tezgah Müşterisi" }
                                )
                                posCart.clear()
                                lastReceiptModal = sale
                            }
                        },
                        enabled = cartTotal > 0,
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NexoEmeraldDark),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Tahsil Et", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }

    lastReceiptModal?.let { sale ->
        AlertDialog(
            onDismissRequest = { lastReceiptModal = null },
            confirmButton = {
                Button(onClick = { lastReceiptModal = null }, colors = ButtonDefaults.buttonColors(containerColor = NexoIndigoPrimary)) {
                    Text("Tamam")
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = NexoEmerald)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Satış Fişi (#${sale.receiptNo})")
                }
            },
            text = {
                Column {
                    Text("İşletme: ${business.name}")
                    Text("Tarih: Bugün")
                    Text("Ödeme: ${sale.paymentMethod.titleTr}")
                    Text("Detay: ${sale.itemsSummary}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Tahsil Edilen: %.2f %s".format(sale.totalAmount, business.currency), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NexoEmeraldLight)
                }
            }
        )
    }
}

@Composable
fun QuotesScreen() {
    val quotes by NexoRepository.quotes.collectAsState()
    val business = NexoRepository.getActiveBusiness()
    var selectedQuoteForPreview by remember { mutableStateOf<Quote?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("B2B Teklif Yönetimi", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Kurumsal müşteriler için teklif hazırlama ve onay takibi", style = MaterialTheme.typography.bodySmall, color = NexoDarkTextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(quotes) { quote ->
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
                            Text(quote.quoteNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (quote.status) {
                                    QuoteStatus.ACCEPTED -> NexoEmerald.copy(alpha = 0.2f)
                                    QuoteStatus.SENT -> NexoIndigoPrimary.copy(alpha = 0.2f)
                                    else -> NexoDarkBorder
                                }
                            ) {
                                Text(
                                    quote.status.titleTr,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (quote.status == QuoteStatus.ACCEPTED) NexoEmeraldLight else NexoIndigoLight,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Müşteri: ${quote.customerName}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("${quote.items.size} Kalem Hizmet/Ürün • Geçerlilik: ${quote.validityDate}", fontSize = 11.sp, color = NexoDarkTextSecondary)

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Toplam (KDV Dahil):", fontSize = 12.sp, color = NexoDarkTextSecondary)
                            Text(
                                "%.2f %s".format(quote.total, business.currency),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = NexoEmeraldLight
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { selectedQuoteForPreview = quote },
                                modifier = Modifier.weight(1f).height(34.dp)
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Görüntüle", fontSize = 11.sp)
                            }
                            if (quote.status != QuoteStatus.ACCEPTED) {
                                Button(
                                    onClick = { NexoRepository.updateQuoteStatus(quote.id, QuoteStatus.ACCEPTED) },
                                    modifier = Modifier.weight(1f).height(34.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = NexoEmeraldDark)
                                ) {
                                    Text("Onayla", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedQuoteForPreview?.let { q ->
        AlertDialog(
            onDismissRequest = { selectedQuoteForPreview = null },
            confirmButton = {
                Button(onClick = { selectedQuoteForPreview = null }, colors = ButtonDefaults.buttonColors(containerColor = NexoIndigoPrimary)) {
                    Text("PDF Olarak İndir / Kapat")
                }
            },
            title = { Text(q.quoteNumber) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Müşteri: ${q.customerName}", fontWeight = FontWeight.Bold)
                    Text("E-posta: ${q.customerEmail}")
                    Text("Telefon: ${q.customerPhone}")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    q.items.forEach { itm ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${itm.quantity.toInt()}x ${itm.name}", fontSize = 11.sp, modifier = Modifier.weight(1f))
                            Text("%.0f %s".format(itm.total, business.currency), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("İndirim: %${q.discountPercent.toInt()} • KDV: %${q.taxPercent.toInt()}", fontSize = 11.sp, color = NexoDarkTextSecondary)
                    Text("Genel Toplam: %.2f %s".format(q.total, business.currency), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NexoEmeraldLight)
                }
            }
        )
    }
}

@Composable
fun CampaignsScreen() {
    val campaigns by NexoRepository.campaigns.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Pazarlama & Kampanyalar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("İndirimler, VIP kurguları ve duyurular", style = MaterialTheme.typography.bodySmall, color = NexoDarkTextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(campaigns) { cmp ->
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(6.dp), color = NexoAmber.copy(alpha = 0.2f)) {
                                    Text(
                                        cmp.discountValue,
                                        color = NexoAmber,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(cmp.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            Switch(
                                checked = cmp.isActive,
                                onCheckedChange = { NexoRepository.toggleCampaign(cmp.id) }
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(cmp.description, fontSize = 12.sp, color = NexoDarkTextSecondary)

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Hedef Kitle: ${cmp.targetCustomerSegment} • Tarih: ${cmp.startDate} - ${cmp.endDate}", fontSize = 11.sp, color = NexoDarkTextMuted)
                    }
                }
            }
        }
    }
}
