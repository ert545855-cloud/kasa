package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.R
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NexoModule
import com.example.data.model.OrderStatus
import com.example.data.repository.NexoRepository
import com.example.ui.components.TrialStatusBanner
import com.example.ui.components.SubscriptionPaywallDialog
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    onNavigateToModule: (NexoModule) -> Unit,
    onOpenCustomerView: () -> Unit
) {
    val business = NexoRepository.getActiveBusiness()
    val allBusinesses by NexoRepository.businesses.collectAsState()
    val activeBiz = allBusinesses.firstOrNull { it.id == business.id } ?: business
    var showPaywallDialog by remember { mutableStateOf(false) }

    LaunchedEffect(activeBiz.isTrialExpired) {
        if (activeBiz.isTrialExpired) {
            showPaywallDialog = true
        }
    }

    val allOrders by NexoRepository.orders.collectAsState()
    val allSales by NexoRepository.sales.collectAsState()
    val allCustomers by NexoRepository.customers.collectAsState()
    val allStockItems by NexoRepository.stockItems.collectAsState()
    val allAppointments by NexoRepository.appointments.collectAsState()
    val allQuotes by NexoRepository.quotes.collectAsState()

    val orders = allOrders.filter { it.businessId == business.id }
    val sales = allSales.filter { it.businessId == business.id }
    val customers = allCustomers.filter { it.businessId == business.id }
    val stockItems = allStockItems.filter { it.businessId == business.id }
    val appointments = allAppointments.filter { it.businessId == business.id }
    val quotes = allQuotes.filter { it.businessId == business.id }

    val pendingOrders = orders.filter { it.status == OrderStatus.PENDING }
    val todayRevenue = orders.sumOf { it.total } + sales.sumOf { it.totalAmount }
    val lowStockCount = stockItems.count { it.isLowStock }
    val openQuotesCount = quotes.count { it.status == com.example.data.model.QuoteStatus.SENT }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 15-Day Free Trial & Subscription Status Banner
        item {
            TrialStatusBanner(
                business = activeBiz,
                onOpenPaywall = { showPaywallDialog = true }
            )
        }

        // Welcome Banner
        item {
            val activeTheme by NexoRepository.activeTheme.collectAsState()
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
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
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.82f))
                                )
                            )
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = business.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(activeTheme.primaryColor)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = activeTheme.titleTr,
                                            fontSize = 10.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Restoranınızın tüm operasyonu tek platformda.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Text(
                                text = "Menüden siparişe, masadan mutfağa.",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }

                        Button(
                            onClick = onOpenCustomerView,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("dashboard_qr_preview_btn")
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("QR Menü", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 7 Key KPI Cards
        item {
            Text("İşletme Özeti (Bugün)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard(
                        title = "Bugünkü Ciro",
                        value = "%.0f %s".format(todayRevenue, business.currency),
                        subtitle = "QR & Kasa Satışları",
                        icon = Icons.Default.Payments,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Sipariş Sayısı",
                        value = "${orders.size}",
                        subtitle = "${pendingOrders.size} Yeni Onay Bekliyor",
                        icon = Icons.Default.ReceiptLong,
                        iconColor = if (pendingOrders.isNotEmpty()) NexoAmber else NexoEmeraldLight,
                        badgeColor = if (pendingOrders.isNotEmpty()) NexoAmber.copy(alpha = 0.15f) else NexoEmerald.copy(alpha = 0.15f),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard(
                        title = "Müşteri Portföyü",
                        value = "${customers.size}",
                        subtitle = "Aktif Kayıtlı Müşteri",
                        icon = Icons.Default.People,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Kritik Stok Uyarısı",
                        value = "$lowStockCount Kalem",
                        subtitle = if (lowStockCount > 0) "Sipariş verilmeli" else "Stoklar güvende",
                        icon = Icons.Default.WarningAmber,
                        iconColor = if (lowStockCount > 0) NexoRose else NexoEmeraldLight,
                        badgeColor = if (lowStockCount > 0) NexoRose.copy(alpha = 0.15f) else NexoEmerald.copy(alpha = 0.15f),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard(
                        title = "Randevular",
                        value = "${appointments.size}",
                        subtitle = "Bugünkü Takvim",
                        icon = Icons.Default.CalendarToday,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Açık Teklifler",
                        value = "$openQuotesCount",
                        subtitle = "Yanıt Bekleyen",
                        icon = Icons.Default.Description,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Gemini AI Daily Performance Summary Card
        item {
            var isGeneratingSummary by remember { mutableStateOf(false) }
            var dailySummaryText by remember { mutableStateOf<String?>(null) }
            var summaryError by remember { mutableStateOf<String?>(null) }
            val coroutineScope = rememberCoroutineScope()

            Card(
                modifier = Modifier.fillMaxWidth().testTag("gemini_daily_summary_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "AI Günlük Performans Özeti",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Gemini ile anlık veri analizi & yönetici özeti",
                                    fontSize = 11.sp,
                                    color = NexoTextSecondary
                                )
                            }
                        }

                        Button(
                            onClick = {
                                isGeneratingSummary = true
                                summaryError = null
                                coroutineScope.launch {
                                    val snapshot = com.example.data.service.DailyBusinessSnapshot(
                                        businessName = business.name,
                                        businessType = business.businessType.titleTr,
                                        currency = business.currency,
                                        totalOrders = orders.size,
                                        todayRevenue = todayRevenue,
                                        dineInOrders = orders.count { (it.tableNumber ?: 0) > 0 },
                                        takeawayOrders = orders.count { (it.tableNumber ?: 0) <= 0 },
                                        lowStockCount = lowStockCount,
                                        lowStockItems = stockItems.filter { it.isLowStock }.map { it.name },
                                        totalCustomers = customers.size,
                                        topSellingItems = orders.flatMap { it.items }.groupBy { it.productName }.keys.take(3).toList()
                                    )
                                    val result = com.example.data.service.GeminiDailySummaryService().generateDailyPerformanceSummary(snapshot)
                                    isGeneratingSummary = false
                                    result.onSuccess { summary ->
                                        dailySummaryText = summary
                                    }.onFailure { err ->
                                        summaryError = err.localizedMessage ?: "Özet oluşturulamadı."
                                    }
                                }
                            },
                            enabled = !isGeneratingSummary,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("generate_daily_summary_btn")
                        ) {
                            if (isGeneratingSummary) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Analiz Ediliyor...", fontSize = 11.sp)
                            } else {
                                Icon(Icons.Default.Insights, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Analiz Et", fontSize = 11.sp)
                            }
                        }
                    }

                    if (dailySummaryText != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = NexoBorderLight)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = dailySummaryText ?: "",
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (summaryError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = summaryError ?: "", color = NexoRoseRed, fontSize = 11.sp)
                    }
                }
            }
        }

        // Pending Live Orders Feed (if any)
        if (pendingOrders.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NexoAmber.copy(alpha = 0.1f)),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(NexoAmber)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
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
                                        .background(NexoAmber)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "${pendingOrders.size} Yeni Masa Siparişi Var!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            TextButton(onClick = { onNavigateToModule(NexoModule.ORDERS) }) {
                                Text("Mutfak Ekranına Git", fontSize = 12.sp, color = NexoAmber, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        pendingOrders.take(2).forEach { ord ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Masa ${ord.tableNumber ?: "Paket"} • ${ord.orderNumber}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("%.0f %s".format(ord.total, business.currency), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = { NexoRepository.updateOrderStatus(ord.id, OrderStatus.ACCEPTED) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = NexoIndigoPrimary)
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

        // Dynamic Enabled Modules Quick Grid
        item {
            Text("İşletme Modülleri", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            val enabledModulesList = NexoModule.entries.filter { business.enabledModules.contains(it) || it.isCore }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(enabledModulesList) { mod ->
                    Card(
                        onClick = { onNavigateToModule(mod) },
                        modifier = Modifier.width(130.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NexoIndigoPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (mod) {
                                        NexoModule.MENU -> Icons.Default.RestaurantMenu
                                        NexoModule.ORDERS -> Icons.Default.SoupKitchen
                                        NexoModule.CRM -> Icons.Default.People
                                        NexoModule.STOCK -> Icons.Default.Inventory2
                                        NexoModule.BOOKING -> Icons.Default.CalendarToday
                                        NexoModule.LOYALTY -> Icons.Default.CardGiftcard
                                        NexoModule.SALES -> Icons.Default.PointOfSale
                                        NexoModule.STAFF -> Icons.Default.Badge
                                        NexoModule.AI -> Icons.Default.AutoAwesome
                                        NexoModule.ANALYTICS -> Icons.Default.Assessment
                                        NexoModule.CAMPAIGNS -> Icons.Default.Campaign
                                        NexoModule.QUOTES -> Icons.Default.Description
                                        NexoModule.SETTINGS -> Icons.Default.Settings
                                    },
                                    contentDescription = null,
                                    tint = NexoIndigoLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = mod.titleTr,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
