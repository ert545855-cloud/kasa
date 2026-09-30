package com.example.ui.screens

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BusinessType
import com.example.data.model.NexoModule
import com.example.data.model.PlanType
import com.example.data.repository.AuthRepository
import com.example.data.repository.NexoRepository
import com.example.ui.components.GoogleSignInCard
import com.example.ui.theme.*

@Composable
fun LandingScreen(
    authRepository: AuthRepository,
    onStartFree: () -> Unit,
    onOpenLiveDemo: () -> Unit,
    onLoginClick: () -> Unit
) {
    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(NexoIndigoPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("N", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "NEXO BUSINESS OS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = onLoginClick,
                            modifier = Modifier.testTag("landing_login_button")
                        ) {
                            Text("Giriş Yap", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = onStartFree,
                            colors = ButtonDefaults.buttonColors(containerColor = NexoIndigoPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("landing_start_free_button")
                        ) {
                            Text("Ücretsiz Başla", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Hero Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = NexoIndigoPrimary.copy(alpha = 0.15f),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.linearGradient(listOf(NexoIndigoLight, NexoEmeraldLight))
                            )
                        ) {
                            Text(
                                text = "🚀 KOBİ'ler İçin Yeni Nesil SaaS Platformu",
                                color = NexoIndigoLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "İşletmenizin dijital operasyon merkezi.",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 34.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Menüden siparişe, müşteriden stoğa kadar işletmenizi tek panelden yönetin. QR kod ile masa siparişi alın, mutfağı canlı yönetin.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = NexoDarkTextSecondary,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onStartFree,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("hero_start_free_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = NexoIndigoPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Ücretsiz Başla", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            OutlinedButton(
                                onClick = onOpenLiveDemo,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("hero_live_demo_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NexoEmeraldLight),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = Brush.linearGradient(listOf(NexoEmerald, NexoCyan))
                                )
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Canlı Demo", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

            // Firebase Authentication & Google Sign-In Card
            item {
                GoogleSignInCard(authRepository = authRepository)
            }

            // Key Modules Overview
            item {
                Column {
                    Text(
                        text = "13 Güçlü İşletme Modülü",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "İşletmenizin ihtiyacına göre modülleri açın veya kapatın",
                        style = MaterialTheme.typography.bodySmall,
                        color = NexoDarkTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val modules = NexoModule.entries
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        modules.chunked(2).forEach { rowModules ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowModules.forEach { mod ->
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(NexoIndigoPrimary.copy(alpha = 0.2f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = mod.key.take(2).uppercase(),
                                                        color = NexoIndigoLight,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = mod.titleTr,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = mod.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = NexoDarkTextSecondary,
                                                fontSize = 11.sp,
                                                maxLines = 2
                                            )
                                        }
                                    }
                                }
                                if (rowModules.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // Pricing Plans
            item {
                Column {
                    Text(
                        text = "Şeffaf SaaS Fiyatlandırması",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "İşletmeniz büyüdükçe esnek plan geçişleri yapın",
                        style = MaterialTheme.typography.bodySmall,
                        color = NexoDarkTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    PlanType.entries.forEach { plan ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (plan == PlanType.PRO) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                            ),
                            border = if (plan == PlanType.PRO) ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.linearGradient(listOf(NexoIndigoLight, NexoEmeraldLight))
                            ) else null
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(plan.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text(
                                            text = when (plan) {
                                                PlanType.STARTER -> "Yeni başlayan kafeler ve küçük işletmeler"
                                                PlanType.PRO -> "Yoğun restoran, bar ve büyüyen işletmeler"
                                                PlanType.BUSINESS -> "Çok şubeli zincirler ve büyük operasyonlar"
                                            },
                                            fontSize = 11.sp,
                                            color = NexoDarkTextSecondary
                                        )
                                    }
                                    Text(
                                        text = "${plan.priceTl} ₺/ay",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = NexoIndigoLight
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(
                                        "• ${if (plan.maxProducts == Int.MAX_VALUE) "Sınırsız" else "${plan.maxProducts}"} Ürün",
                                        fontSize = 11.sp,
                                        color = NexoDarkTextSecondary
                                    )
                                    Text(
                                        "• ${if (plan.maxTables == Int.MAX_VALUE) "Sınırsız" else "${plan.maxTables}"} Masa",
                                        fontSize = 11.sp,
                                        color = NexoDarkTextSecondary
                                    )
                                    Text(
                                        "• ${plan.aiCredits} AI Kredisi",
                                        fontSize = 11.sp,
                                        color = NexoDarkTextSecondary
                                    )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingWizardDialog(
    onDismiss: () -> Unit,
    onCompleted: (String, BusinessType, String, String, String, Set<NexoModule>) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var businessName by remember { mutableStateOf("") }
    var businessType by remember { mutableStateOf(BusinessType.CAFE) }
    var phone by remember { mutableStateOf("+90 5") }
    var address by remember { mutableStateOf("İstanbul") }
    var currency by remember { mutableStateOf("₺") }
    var selectedModules by remember {
        mutableStateOf(
            setOf(
                NexoModule.MENU,
                NexoModule.ORDERS,
                NexoModule.CRM,
                NexoModule.STOCK,
                NexoModule.SALES,
                NexoModule.AI,
                NexoModule.ANALYTICS,
                NexoModule.SETTINGS
            )
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "NEXO Kurulum Sihirbazı",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Adım $step / 4: ${
                                when (step) {
                                    1 -> "İşletme Kimliği"
                                    2 -> "İşletme Türü"
                                    3 -> "İletişim & Para Birimi"
                                    else -> "Modül Seçimi"
                                }
                            }",
                            style = MaterialTheme.typography.bodySmall,
                            color = NexoIndigoLight
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Steps
                when (step) {
                    1 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("İşletmenizin Adı:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            OutlinedTextField(
                                value = businessName,
                                onValueChange = { businessName = it },
                                placeholder = { Text("Örn: Artisan Bakery & Coffee") },
                                modifier = Modifier.fillMaxWidth().testTag("onboarding_business_name_input"),
                                singleLine = true
                            )
                            Text(
                                "Bu isim dijital QR menünüzde ve müşterilere gönderilen fiş/tekliflerde görünecektir.",
                                fontSize = 11.sp,
                                color = NexoDarkTextSecondary
                            )
                        }
                    }
                    2 -> {
                        Column {
                            Text("İşletme Sektörünüz:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyColumn(modifier = Modifier.height(220.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(BusinessType.entries) { type ->
                                    Surface(
                                        onClick = { businessType = type },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (businessType == type) NexoIndigoPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                        border = if (businessType == type) ButtonDefaults.outlinedButtonBorder.copy(
                                            brush = Brush.linearGradient(listOf(NexoIndigoLight, NexoIndigoLight))
                                        ) else null,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(type.titleTr, fontWeight = if (businessType == type) FontWeight.Bold else FontWeight.Normal)
                                            if (businessType == type) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NexoIndigoLight, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    3 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("İletişim & Lokasyon:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Telefon") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = { Text("Adres") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Text("Para Birimi:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("₺", "$", "€", "£").forEach { c ->
                                    FilterChip(
                                        selected = currency == c,
                                        onClick = { currency = c },
                                        label = { Text(c, fontWeight = FontWeight.Bold) }
                                    )
                                }
                            }
                        }
                    }
                    4 -> {
                        Column {
                            Text("Aktif Edilecek Modülleri Seçin:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyColumn(modifier = Modifier.height(240.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(NexoModule.entries) { mod ->
                                    val isSelected = selectedModules.contains(mod) || mod.isCore
                                    Surface(
                                        onClick = {
                                            if (!mod.isCore) {
                                                selectedModules = if (selectedModules.contains(mod)) {
                                                    selectedModules - mod
                                                } else {
                                                    selectedModules + mod
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) NexoIndigoPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(mod.titleTr, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                                Text(mod.description, fontSize = 10.sp, color = NexoDarkTextSecondary)
                                            }
                                            Checkbox(
                                                checked = isSelected,
                                                onCheckedChange = {
                                                    if (!mod.isCore) {
                                                        selectedModules = if (isSelected) selectedModules - mod else selectedModules + mod
                                                    }
                                                },
                                                enabled = !mod.isCore
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (step > 1) {
                        OutlinedButton(onClick = { step-- }) {
                            Text("Geri")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Button(
                        onClick = {
                            if (step < 4) {
                                if (step == 1 && businessName.isBlank()) {
                                    businessName = "İşletmem"
                                }
                                step++
                            } else {
                                val nameToUse = businessName.ifBlank { "NEXO İşletmesi" }
                                onCompleted(nameToUse, businessType, phone, address, currency, selectedModules)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NexoIndigoPrimary),
                        modifier = Modifier.testTag("onboarding_next_button")
                    ) {
                        Text(if (step < 4) "İleri" else "Kurulumu Tamamla")
                    }
                }
            }
        }
    }
}
