package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
    val invitations by NexoRepository.staffInvitations.collectAsState()
    val activeRole by NexoRepository.currentStaffRole.collectAsState()
    var showAddStaffDialog by remember { mutableStateOf(false) }
    var showInviteDialog by remember { mutableStateOf(false) }
    var inviteSuccessFeedback by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Personel Listesi, 1: E-posta Davetleri

    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Personel & Roller", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Ekip üyeleri, e-posta davetleri ve Firebase senkronizasyonu", style = MaterialTheme.typography.bodySmall, color = NexoDarkTextSecondary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = { showInviteDialog = true },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.MailOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("E-posta Daveti", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { showAddStaffDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NexoIndigoPrimary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Personel Ekle", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (inviteSuccessFeedback != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = NexoEmerald.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NexoEmeraldLight, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(inviteSuccessFeedback ?: "", fontSize = 12.sp, color = NexoEmeraldLight, fontWeight = FontWeight.SemiBold)
                    }
                    IconButton(onClick = { inviteSuccessFeedback = null }, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = NexoEmeraldLight, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tab Selector (Aktif Personel vs E-posta Davetleri)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(3.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedTab == 0) NexoIndigoPrimary else Color.Transparent)
                    .clickable { selectedTab = 0 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aktif Personel (${staff.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selectedTab == 0) Color.White else NexoDarkTextSecondary
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedTab == 1) NexoIndigoPrimary else Color.Transparent)
                    .clickable { selectedTab = 1 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "E-posta Davetleri (${invitations.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selectedTab == 1) Color.White else NexoDarkTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == 0) {
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Telefon: ${emp.phone}", fontSize = 11.sp, color = NexoDarkTextMuted)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = NexoEmeraldLight, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Firebase Senkron", fontSize = 10.sp, color = NexoEmeraldLight)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // E-posta Davetleri Listesi
            if (invitations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.MarkEmailRead, contentDescription = null, tint = NexoDarkTextMuted, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Bekleyen davet bulunmuyor", fontWeight = FontWeight.Bold, color = NexoDarkTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Yukarıdaki 'E-posta Daveti' butonundan ekibinize davet gönderebilirsiniz.", fontSize = 11.sp, color = NexoDarkTextMuted)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(invitations) { inv ->
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
                                    Column {
                                        Text(inv.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(inv.email, fontSize = 12.sp, color = NexoIndigoLight)
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = NexoAmber.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "Davet Gönderildi",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NexoAmber,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Rol: ${inv.role.titleTr}", fontSize = 11.sp, color = NexoDarkTextMuted)
                                    Text("Kod: ${inv.token}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NexoDarkTextSecondary)
                                }

                                if (inv.note.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Not: \"${inv.note}\"", fontSize = 11.sp, color = NexoDarkTextSecondary)
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = NexoEmeraldLight, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Firebase altyapısına kaydedildi", fontSize = 10.sp, color = NexoEmeraldLight)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Direct Add Staff Dialog
    if (showAddStaffDialog) {
        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("+90 ") }
        var role by remember { mutableStateOf(UserRole.WAITER) }

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

                    Text("Rol Seçimi:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(UserRole.WAITER, UserRole.KITCHEN, UserRole.CASHIER, UserRole.MANAGER).forEach { r ->
                            val isSel = role == r
                            FilterChip(
                                selected = isSel,
                                onClick = { role = r },
                                label = { Text(r.titleTr, fontSize = 10.sp) }
                            )
                        }
                    }
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
                            inviteSuccessFeedback = "$name personeli eklendi ve Firebase veritabanına kaydedildi."
                            showAddStaffDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexoIndigoPrimary)
                ) {
                    Text("Kaydet & Senkronize Et")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStaffDialog = false }) { Text("İptal") }
            }
        )
    }

    // Email Invitation Dialog (Firebase Altyapısına Kaydeder)
    if (showInviteDialog) {
        var inviteName by remember { mutableStateOf("") }
        var inviteEmail by remember { mutableStateOf("") }
        var inviteRole by remember { mutableStateOf(UserRole.WAITER) }
        var inviteNote by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showInviteDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MailOutline, contentDescription = null, tint = NexoIndigoPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("E-posta ile Ekip Daveti Gönder")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Davet edilen kişi Firebase altyapısına kaydedilecek ve özel davet bağlantısı oluşturulacaktır.",
                        fontSize = 11.sp,
                        color = NexoDarkTextSecondary
                    )

                    OutlinedTextField(
                        value = inviteName,
                        onValueChange = { inviteName = it },
                        label = { Text("Davet Edilen Kişinin Adı") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = inviteEmail,
                        onValueChange = { inviteEmail = it },
                        label = { Text("E-posta Adresi") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Yetki & Rol:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(UserRole.WAITER, UserRole.KITCHEN, UserRole.CASHIER, UserRole.MANAGER).forEach { r ->
                            val isSel = inviteRole == r
                            FilterChip(
                                selected = isSel,
                                onClick = { inviteRole = r },
                                label = { Text(r.titleTr, fontSize = 10.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = inviteNote,
                        onValueChange = { inviteNote = it },
                        label = { Text("Davet Notu (İsteğe bağlı)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Surface(
                        color = NexoIndigoPrimary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudQueue, contentDescription = null, tint = NexoIndigoLight, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Veriler Firestore bulut koleksiyonlarına anında işlenir.", fontSize = 10.sp, color = NexoIndigoLight)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inviteEmail.isNotBlank()) {
                            val activeBiz = NexoRepository.getActiveBusiness()
                            val invitation = NexoRepository.sendStaffInvitation(
                                businessId = activeBiz.id,
                                email = inviteEmail.trim(),
                                name = inviteName.ifBlank { "Yeni Personel" },
                                role = inviteRole,
                                note = inviteNote.trim()
                            )
                            // Also add as member record
                            NexoRepository.addStaff(
                                Employee(
                                    businessId = activeBiz.id,
                                    name = inviteName.ifBlank { "Yeni Personel" },
                                    email = inviteEmail.trim(),
                                    phone = "+90",
                                    role = inviteRole
                                )
                            )
                            inviteSuccessFeedback = "${inviteEmail.trim()} adresine davet gönderildi (Kod: ${invitation.token}) ve Firebase'e kaydedildi."
                            selectedTab = 1
                            showInviteDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexoIndigoPrimary)
                ) {
                    Text("Davet Gönder & Firebase'e Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInviteDialog = false }) { Text("İptal") }
            }
        )
    }
}
