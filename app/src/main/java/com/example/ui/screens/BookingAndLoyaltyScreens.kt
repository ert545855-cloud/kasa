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
import com.example.data.model.Appointment
import com.example.data.model.AppointmentStatus
import com.example.data.model.LoyaltyReward
import com.example.data.model.PaymentMethod
import com.example.data.model.ServiceItem
import com.example.data.repository.NexoRepository
import com.example.ui.components.StatCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen() {
    val appointments by NexoRepository.appointments.collectAsState()
    val services by NexoRepository.services.collectAsState()
    val business = NexoRepository.getActiveBusiness()

    var selectedCalendarView by remember { mutableStateOf("Bugün") }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = NexoIndigoPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_appointment_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Yeni Randevu")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Rezervasyon & Randevu",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Hizmet takvimi, masa ve randevu yönetimi",
                        style = MaterialTheme.typography.bodySmall,
                        color = NexoDarkTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Calendar Range Toggle
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Bugün", "Bu Hafta", "Bu Ay", "Tüm Randevular").forEach { label ->
                    FilterChip(
                        selected = selectedCalendarView == label,
                        onClick = { selectedCalendarView = label },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(appointments) { app ->
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
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = NexoIndigoPrimary.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "${app.dateString} ${app.timeString}",
                                            color = NexoIndigoLight,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(app.customerName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when (app.status) {
                                        AppointmentStatus.CONFIRMED -> NexoEmerald.copy(alpha = 0.15f)
                                        AppointmentStatus.PENDING -> NexoAmber.copy(alpha = 0.15f)
                                        AppointmentStatus.COMPLETED -> NexoDarkBorder
                                        else -> NexoRose.copy(alpha = 0.15f)
                                    }
                                ) {
                                    Text(
                                        text = app.status.titleTr,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = when (app.status) {
                                            AppointmentStatus.CONFIRMED -> NexoEmeraldLight
                                            AppointmentStatus.PENDING -> NexoAmber
                                            AppointmentStatus.COMPLETED -> NexoDarkTextSecondary
                                            else -> NexoRose
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Hizmet: ${app.serviceName} • Görevli: ${app.staffName}",
                                fontSize = 12.sp,
                                color = NexoDarkTextSecondary
                            )

                            if (app.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Not: ${app.notes}", fontSize = 11.sp, color = NexoDarkTextMuted)
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (app.status != AppointmentStatus.COMPLETED) {
                                    Button(
                                        onClick = { NexoRepository.updateAppointmentStatus(app.id, AppointmentStatus.COMPLETED) },
                                        modifier = Modifier.weight(1f).height(34.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = NexoEmeraldDark)
                                    ) {
                                        Text("Tamamlandı", fontSize = 11.sp)
                                    }
                                }
                                if (app.status != AppointmentStatus.CANCELLED) {
                                    OutlinedButton(
                                        onClick = { NexoRepository.updateAppointmentStatus(app.id, AppointmentStatus.CANCELLED) },
                                        modifier = Modifier.weight(1f).height(34.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NexoRose)
                                    ) {
                                        Text("İptal Et", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var custName by remember { mutableStateOf("") }
        var custPhone by remember { mutableStateOf("+90 ") }
        var timeStr by remember { mutableStateOf("15:00") }
        var selectedSrv = services.firstOrNull()

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Yeni Randevu / Rezervasyon") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = custName,
                        onValueChange = { custName = it },
                        label = { Text("Müşteri Adı") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = custPhone,
                        onValueChange = { custPhone = it },
                        label = { Text("Telefon") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = timeStr,
                        onValueChange = { timeStr = it },
                        label = { Text("Saat (Örn: 15:30)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (custName.isNotBlank() && selectedSrv != null) {
                            NexoRepository.addAppointment(
                                Appointment(
                                    businessId = business.id,
                                    customerName = custName.trim(),
                                    customerPhone = custPhone.trim(),
                                    serviceId = selectedSrv!!.id,
                                    serviceName = selectedSrv!!.name,
                                    staffName = "Genel Personel",
                                    dateString = "Bugün",
                                    timeString = timeStr,
                                    status = AppointmentStatus.CONFIRMED
                                )
                            )
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexoIndigoPrimary)
                ) {
                    Text("Oluştur")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("İptal") }
            }
        )
    }
}

@Composable
fun LoyaltyScreen() {
    val rewards by NexoRepository.loyaltyRewards.collectAsState()
    val customers by NexoRepository.customers.collectAsState()
    val business = NexoRepository.getActiveBusiness()

    var showRedeemDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Sadakat & Ödül Sistemi", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Puan biriktirme ve hediye ürün ödül kataloğu", style = MaterialTheme.typography.bodySmall, color = NexoDarkTextSecondary)

        // Point Rule Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NexoIndigoPrimary.copy(alpha = 0.15f)),
            border = ButtonDefaults.outlinedButtonBorder
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(NexoAmber.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = NexoAmber, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Puan Kazanım Kuralı", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Her 100 ₺ harcamada 10 Puan kazanılır (%10 İade).", fontSize = 12.sp, color = NexoDarkTextSecondary)
                    Text("Müşteriler QR menüden sipariş verdikçe otomatik birikir.", fontSize = 11.sp, color = NexoEmeraldLight)
                }
            }
        }

        Text("Ödül Kataloğu & İkramlar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
            items(rewards) { rew ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(rew.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(rew.description, fontSize = 11.sp, color = NexoDarkTextSecondary)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = NexoAmber.copy(alpha = 0.2f)
                        ) {
                            Text(
                                "${rew.requiredPoints} Puan",
                                color = NexoAmber,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
