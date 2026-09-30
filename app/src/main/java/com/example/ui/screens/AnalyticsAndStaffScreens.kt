package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Employee
import com.example.data.model.NexoPermission
import com.example.data.model.UserRole
import com.example.data.repository.NexoRepository
import com.example.ui.components.StatCard
import com.example.ui.theme.*

@Composable
fun AnalyticsScreen() {
    val orders by NexoRepository.orders.collectAsState()
    val sales by NexoRepository.sales.collectAsState()
    val business = NexoRepository.getActiveBusiness()
    var selectedTimeframe by remember { mutableStateOf("7 Gün") }

    val totalRevenue = orders.sumOf { it.total } + sales.sumOf { it.totalAmount }
    val totalOrdersCount = orders.size + sales.size
    val aov = if (totalOrdersCount > 0) totalRevenue / totalOrdersCount else 0.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Raporlar & Analitik", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Ciro, sipariş hacmi ve kategori performansları", style = MaterialTheme.typography.bodySmall, color = NexoDarkTextSecondary)
                }
            }
        }

        // Timeframe selector
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("7 Gün", "30 Gün", "90 Gün", "12 Ay").forEach { tf ->
                    FilterChip(
                        selected = selectedTimeframe == tf,
                        onClick = { selectedTimeframe = tf },
                        label = { Text(tf) }
                    )
                }
            }
        }

        // Stat Cards
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(
                    title = "Toplam Ciro",
                    value = "%.0f %s".format(totalRevenue, business.currency),
                    subtitle = "+%18.4 Geçen Döneme Göre",
                    icon = Icons.Default.TrendingUp,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Ortalama Sepet (AOV)",
                    value = "%.0f %s".format(aov, business.currency),
                    subtitle = "$totalOrdersCount Sipariş / Fiş",
                    icon = Icons.Default.ShoppingBag,
                    iconColor = NexoEmeraldLight,
                    badgeColor = NexoEmerald.copy(alpha = 0.15f),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Revenue Bar Chart Simulation
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Günlük Ciro Dağılımı (Son 7 Gün)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(14.dp))

                    val dailyValues = listOf(1450f, 2100f, 1850f, 2900f, 3400f, 4800f, 3950f)
                    val days = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")
                    val maxVal = dailyValues.maxOrNull() ?: 1f

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        val barWidth = size.width / (dailyValues.size * 2f)
                        val step = size.width / dailyValues.size

                        dailyValues.forEachIndexed { i, value ->
                            val barHeight = (value / maxVal) * (size.height - 30f)
                            val x = i * step + (step - barWidth) / 2f
                            val y = size.height - barHeight - 20f

                            // Draw Bar
                            drawRoundRect(
                                color = if (i == 5) NexoIndigoLight else NexoIndigoPrimary,
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(6f, 6f)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        days.forEach { day ->
                            Text(day, fontSize = 11.sp, color = NexoDarkTextSecondary)
                        }
                    }
                }
            }
        }

        // Category Share Breakdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Kategori Bazlı Satış Oranları", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    val categoriesShare = listOf(
                        Triple("Kahveler", "%45 Pay", 0.45f),
                        Triple("Tatlılar", "%30 Pay", 0.30f),
                        Triple("Soğuk İçecekler", "%15 Pay", 0.15f),
                        Triple("Kahvaltı & Fırın", "%10 Pay", 0.10f)
                    )

                    categoriesShare.forEach { (catName, shareText, ratio) ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(catName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text(shareText, fontSize = 12.sp, color = NexoIndigoLight)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { ratio },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = NexoIndigoPrimary,
                                trackColor = NexoDarkBorder
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StaffScreen() {
    val staff by NexoRepository.staff.collectAsState()
    val activeRole by NexoRepository.currentStaffRole.collectAsState()
    var showAddStaffDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Personel & Roller", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Çalışan hesapları, yetkiler ve mutfak ekranı erişimi", style = MaterialTheme.typography.bodySmall, color = NexoDarkTextSecondary)
            }
            Button(
                onClick = { showAddStaffDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = NexoIndigoPrimary),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Personel Ekle", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(staff) { emp ->
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
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(NexoIndigoPrimary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(emp.name.take(1), fontWeight = FontWeight.Bold, color = NexoIndigoLight)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(emp.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(emp.email, fontSize = 11.sp, color = NexoDarkTextSecondary)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = NexoIndigoPrimary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = emp.role.titleTr,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NexoIndigoLight,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Telefon: ${emp.phone}", fontSize = 11.sp, color = NexoDarkTextMuted)
                    }
                }
            }
        }
    }

    if (showAddStaffDialog) {
        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("+90 ") }
        var role by remember { mutableStateOf(UserRole.EMPLOYEE) }

        AlertDialog(
            onDismissRequest = { showAddStaffDialog = false },
            title = { Text("Yeni Personel Tanımla") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Ad Soyad") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-posta") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Telefon") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            NexoRepository.addStaff(
                                Employee(
                                    businessId = NexoRepository.getActiveBusiness().id,
                                    name = name.trim(),
                                    email = email.trim(),
                                    phone = phone.trim(),
                                    role = role
                                )
                            )
                            showAddStaffDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexoIndigoPrimary)
                ) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStaffDialog = false }) { Text("İptal") }
            }
        )
    }
}
