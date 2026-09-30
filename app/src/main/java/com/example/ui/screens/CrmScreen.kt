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
import com.example.data.model.Customer
import com.example.data.repository.NexoRepository
import com.example.ui.components.StatCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrmScreen() {
    val customers by NexoRepository.customers.collectAsState()
    val business = NexoRepository.getActiveBusiness()

    var searchQuery by remember { mutableStateOf("") }
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var selectedCustomerForDetail by remember { mutableStateOf<Customer?>(null) }

    val filteredCustomers = customers.filter {
        searchQuery.isBlank() ||
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.phone.contains(searchQuery) ||
        it.tags.any { tag -> tag.contains(searchQuery, ignoreCase = true) }
    }

    val totalSpentSum = customers.sumOf { it.totalSpent }
    val vipCount = customers.count { it.tags.contains("VIP") }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCustomerDialog = true },
                containerColor = NexoIndigoPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_customer_fab")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Müşteri Ekle")
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
                text = "Müşteri İlişkileri (CRM)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Müşteri profilleri, sadakat puanları ve harcama geçmişi",
                style = MaterialTheme.typography.bodySmall,
                color = NexoDarkTextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // KPI Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Kayıtlı Müşteri",
                    value = "${customers.size}",
                    subtitle = "$vipCount VIP Müşteri",
                    icon = Icons.Default.People,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Toplam Harcama",
                    value = "%.0f %s".format(totalSpentSum, business.currency),
                    subtitle = "Sadakat Portföyü",
                    icon = Icons.Default.Loyalty,
                    iconColor = NexoEmeraldLight,
                    badgeColor = NexoEmerald.copy(alpha = 0.15f),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("İsim, telefon veya etiket ara...") },
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
                items(filteredCustomers) { cust ->
                    Card(
                        onClick = { selectedCustomerForDetail = cust },
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
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(NexoIndigoPrimary.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = cust.name.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = NexoIndigoLight
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(cust.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(cust.phone, fontSize = 12.sp, color = NexoDarkTextSecondary)
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "%.0f %s".format(cust.totalSpent, business.currency),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = NexoEmeraldLight
                                    )
                                    Text(
                                        text = "${cust.loyaltyPoints} Puan",
                                        fontSize = 11.sp,
                                        color = NexoAmber,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            if (cust.tags.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(cust.tags) { tag ->
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (tag == "VIP") NexoAmber.copy(alpha = 0.2f) else NexoDarkBorder
                                        ) {
                                            Text(
                                                text = tag,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (tag == "VIP") NexoAmber else NexoDarkTextPrimary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            if (!cust.notes.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Not: ${cust.notes}",
                                    fontSize = 11.sp,
                                    color = NexoDarkTextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(72.dp)) }
            }
        }
    }

    // Customer Detail Sheet
    selectedCustomerForDetail?.let { cust ->
        ModalBottomSheet(onDismissRequest = { selectedCustomerForDetail = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(cust.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(cust.phone, fontSize = 13.sp, color = NexoDarkTextSecondary)
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NexoAmber.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "${cust.loyaltyPoints} Sadakat Puanı",
                            color = NexoAmber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(14.dp))

                Text("Harcama Geçmişi & İstatistikler:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Toplam Ciro", fontSize = 11.sp, color = NexoDarkTextSecondary)
                            Text("%.0f %s".format(cust.totalSpent, business.currency), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                    Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Sipariş Adedi", fontSize = 11.sp, color = NexoDarkTextSecondary)
                            Text("${cust.orderCount} Sipariş", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }

                if (!cust.notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Özel Not:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Text(cust.notes, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { selectedCustomerForDetail = null },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NexoIndigoPrimary)
                ) {
                    Text("Kapat")
                }
            }
        }
    }

    // Add Customer Dialog
    if (showAddCustomerDialog) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("+90 ") }
        var email by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddCustomerDialog = false },
            title = { Text("Yeni Müşteri Ekle") },
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
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Telefon") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-posta (İsteğe bağlı)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Özel Tercih / Not") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank() && phone.isNotBlank()) {
                            NexoRepository.addCustomer(
                                Customer(
                                    businessId = business.id,
                                    name = name.trim(),
                                    phone = phone.trim(),
                                    email = email.ifBlank { null },
                                    notes = notes.ifBlank { null },
                                    tags = listOf("Yeni Müşteri")
                                )
                            )
                            showAddCustomerDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexoIndigoPrimary)
                ) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomerDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
}
