package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.data.repository.NexoRepository
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersAndKdsScreen() {
    val orders by NexoRepository.orders.collectAsState()
    val business = NexoRepository.getActiveBusiness()

    var isKdsMode by remember { mutableStateOf(false) }
    var selectedStatusFilter by remember { mutableStateOf<OrderStatus?>(null) }
    var selectedOrderForDetail by remember { mutableStateOf<Order?>(null) }

    val filteredOrders = orders.filter { order ->
        order.businessId == business.id && if (isKdsMode) {
            // KDS mode only shows active in-progress orders
            order.status != OrderStatus.COMPLETED && order.status != OrderStatus.CANCELLED
        } else {
            selectedStatusFilter == null || order.status == selectedStatusFilter
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isKdsMode) "Mutfak Ekranı (KDS)" else "Sipariş Operasyonları",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${filteredOrders.size} aktif sipariş listeleniyor",
                                style = MaterialTheme.typography.bodySmall,
                                color = NexoDarkTextSecondary
                            )
                        }

                        // Mode toggle: Normal list vs KDS Full Kitchen Mode
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "KDS Mutfak Modu",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isKdsMode) NexoEmeraldLight else NexoDarkTextSecondary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Switch(
                                checked = isKdsMode,
                                onCheckedChange = { isKdsMode = it },
                                modifier = Modifier.testTag("kds_mode_switch")
                            )
                        }
                    }

                    if (!isKdsMode) {
                        Spacer(modifier = Modifier.height(10.dp))
                        // Filter chips
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedStatusFilter == null,
                                    onClick = { selectedStatusFilter = null },
                                    label = { Text("Tümü (${orders.size})") }
                                )
                            }
                            items(OrderStatus.entries) { st ->
                                val count = orders.count { it.status == st }
                                FilterChip(
                                    selected = selectedStatusFilter == st,
                                    onClick = { selectedStatusFilter = st },
                                    label = { Text("${st.titleTr} ($count)") }
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.SoupKitchen, contentDescription = null, tint = NexoDarkTextMuted, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Şu anda bu filtrede sipariş bulunmuyor.", color = NexoDarkTextSecondary)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                items(filteredOrders) { order ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when (order.status) {
                                OrderStatus.PENDING -> NexoAmber.copy(alpha = 0.08f)
                                OrderStatus.PREPARING -> NexoPurple.copy(alpha = 0.08f)
                                OrderStatus.READY -> NexoEmerald.copy(alpha = 0.08f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                        border = if (order.status == OrderStatus.PENDING) ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(NexoAmber)
                        ) else null
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Order Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (order.tableNumber != null) NexoIndigoPrimary else NexoEmeraldDark
                                    ) {
                                        Text(
                                            text = if (order.tableNumber != null) "MASA ${order.tableNumber}" else "PAKET",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = order.orderNumber,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp
                                    )
                                }

                                StatusBadge(status = order.status)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Customer & notes
                            if (!order.customerName.isNullOrBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = NexoDarkTextSecondary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(order.customerName, fontSize = 12.sp, color = NexoDarkTextSecondary)
                                }
                            }

                            if (!order.notes.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = NexoAmber.copy(alpha = 0.15f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.WarningAmber, contentDescription = null, tint = NexoAmber, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Not: ${order.notes}", fontSize = 11.sp, color = NexoAmber, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = NexoDarkBorder.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(10.dp))

                            // Items Breakdown
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                order.items.forEach { item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${item.quantity}x",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = NexoIndigoLight
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text(item.productName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                                if (item.variantName != null || item.selectedOptions.isNotEmpty()) {
                                                    val extras = listOfNotNull(item.variantName) + item.selectedOptions
                                                    Text(
                                                        text = extras.joinToString(" • "),
                                                        fontSize = 10.sp,
                                                        color = NexoDarkTextSecondary
                                                    )
                                                }
                                            }
                                        }

                                        Text(
                                            text = "%.2f %s".format(item.total, business.currency),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Toplam Tutar:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NexoDarkTextSecondary
                                )
                                Text(
                                    text = "%.2f %s".format(order.total, business.currency),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Status Workflow Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                when (order.status) {
                                    OrderStatus.PENDING -> {
                                        Button(
                                            onClick = { NexoRepository.updateOrderStatus(order.id, OrderStatus.ACCEPTED) },
                                            modifier = Modifier.weight(1f).height(38.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = NexoIndigoPrimary)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Siparişi Onayla", fontSize = 12.sp)
                                        }
                                        OutlinedButton(
                                            onClick = { NexoRepository.updateOrderStatus(order.id, OrderStatus.CANCELLED) },
                                            modifier = Modifier.height(38.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NexoRose)
                                        ) {
                                            Text("İptal", fontSize = 12.sp)
                                        }
                                    }
                                    OrderStatus.ACCEPTED -> {
                                        Button(
                                            onClick = { NexoRepository.updateOrderStatus(order.id, OrderStatus.PREPARING) },
                                            modifier = Modifier.weight(1f).height(38.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = NexoPurple)
                                        ) {
                                            Icon(Icons.Default.SoupKitchen, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Hazırlanıyor", fontSize = 12.sp)
                                        }
                                    }
                                    OrderStatus.PREPARING -> {
                                        Button(
                                            onClick = { NexoRepository.updateOrderStatus(order.id, OrderStatus.READY) },
                                            modifier = Modifier.weight(1f).height(38.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = NexoEmeraldDark)
                                        ) {
                                            Icon(Icons.Default.RoomService, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Servise Hazır!", fontSize = 12.sp)
                                        }
                                    }
                                    OrderStatus.READY -> {
                                        Button(
                                            onClick = { NexoRepository.updateOrderStatus(order.id, OrderStatus.COMPLETED) },
                                            modifier = Modifier.weight(1f).height(38.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = NexoEmerald)
                                        ) {
                                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Masaya Teslim Edildi", fontSize = 12.sp)
                                        }
                                    }
                                    OrderStatus.COMPLETED -> {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            modifier = Modifier.fillMaxWidth().height(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text("✓ Tamamlandı ve Stoktan Düşüldü", fontSize = 11.sp, color = NexoDarkTextSecondary)
                                            }
                                        }
                                    }
                                    OrderStatus.CANCELLED -> {
                                        Text("Sipariş iptal edildi.", fontSize = 11.sp, color = NexoRose)
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}
