package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.BusinessType
import com.example.data.model.NexoModule
import com.example.data.model.PlanType
import com.example.data.repository.AuthRepository
import com.example.data.repository.NexoRepository
import com.example.ui.components.GoogleSignInCard
import com.example.ui.components.StatCard
import com.example.ui.theme.*

@Composable
fun SettingsAndPlansScreen(
    authRepository: AuthRepository = AuthRepository(LocalContext.current)
) {
    val business = NexoRepository.getActiveBusiness()
    var selectedPlan by remember { mutableStateOf(business.plan) }
    var showPlanUpgradeSuccess by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("İşletme Ayarları & Abonelik", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Profil, para birimi, aktif modüller ve SaaS planı", style = MaterialTheme.typography.bodySmall, color = NexoDarkTextSecondary)
        }

        // Firebase Auth & Cloud Sync Card
        item {
            GoogleSignInCard(authRepository = authRepository)
        }

        // Business Profile Details Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("İşletme Profili", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Adı: ${business.name}", fontSize = 13.sp)
                    Text("Sektör: ${business.businessType.titleTr}", fontSize = 12.sp, color = NexoDarkTextSecondary)
                    Text("Telefon: ${business.phone}", fontSize = 12.sp, color = NexoDarkTextSecondary)
                    Text("Adres: ${business.address}", fontSize = 12.sp, color = NexoDarkTextSecondary)
                    Text("Para Birimi: ${business.currency} • Dil: Türkçe (TR)", fontSize = 12.sp, color = NexoDarkTextSecondary)
                }
            }
        }

        // Restaurant Theme Engine & Brand Identity Card
        item {
            val activeTheme by NexoRepository.activeTheme.collectAsState()
            var brandBorderRadius by remember { mutableIntStateOf(12) }
            var brandMenuLayout by remember { mutableStateOf("Görsel Kartlar (Modern)") }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Restoran Tema Motoru & Marka Profili", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Text("Her restorana özel renk paleti, tipografi ve operasyonel arayüz", fontSize = 11.sp, color = NexoTextSecondary)
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = activeTheme.titleTr,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text("Kullanılabilir Restoran Temaları:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(RestaurantTheme.entries) { theme ->
                            val isSelected = activeTheme == theme
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else NexoBorderLight
                                ),
                                modifier = Modifier
                                    .width(160.dp)
                                    .clickable {
                                        NexoRepository.setRestaurantTheme(theme)
                                    }
                                    .testTag("theme_card_${theme.id}")
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = theme.titleTr,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        if (isSelected) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = "Seçili",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Color swatch preview
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(theme.primaryColor)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(theme.secondaryColor)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(theme.accentColor)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(theme.surfaceColor)
                                                .border(0.5.dp, Color.Gray, CircleShape)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = theme.description,
                                        fontSize = 10.sp,
                                        color = NexoTextSecondary,
                                        lineHeight = 13.sp,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = NexoBorderSubtle)

                    Text("Görsel & Operasyonel Detaylar (restaurant_branding):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedCard(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Köşe Yuvarlama", fontSize = 10.sp, color = NexoTextSecondary)
                                Text("${brandBorderRadius}dp", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) {
                                    listOf(6, 12, 20).forEach { r ->
                                        FilterChip(
                                            selected = brandBorderRadius == r,
                                            onClick = { brandBorderRadius = r },
                                            label = { Text("${r}p", fontSize = 9.sp) },
                                            modifier = Modifier.height(28.dp)
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedCard(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Menü Stili", fontSize = 10.sp, color = NexoTextSecondary)
                                Text(brandMenuLayout, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) {
                                    listOf("Kart", "Liste").forEach { st ->
                                        FilterChip(
                                            selected = brandMenuLayout.startsWith(st),
                                            onClick = { brandMenuLayout = if (st == "Kart") "Görsel Kartlar (Modern)" else "Kompakt Liste (Hızlı)" },
                                            label = { Text(st, fontSize = 9.sp) },
                                            modifier = Modifier.height(28.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Modular Add-ons Store
        item {
            Text("Modül Yönetimi & Mağaza", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text("İşletmenizde kullanmak istediğiniz araçları açıp kapatabilirsiniz", fontSize = 11.sp, color = NexoDarkTextSecondary)
        }

        items(NexoModule.entries) { mod ->
            val isEnabled = business.enabledModules.contains(mod) || mod.isCore
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(mod.titleTr, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            if (mod.isCore) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(shape = RoundedCornerShape(4.dp), color = NexoIndigoPrimary.copy(alpha = 0.2f)) {
                                    Text("TEMEL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NexoIndigoLight, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            } else if (mod.monthlyPriceTl > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("+${mod.monthlyPriceTl}₺/ay", fontSize = 10.sp, color = NexoEmeraldLight, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Text(mod.description, fontSize = 11.sp, color = NexoDarkTextSecondary)
                    }

                    Switch(
                        checked = isEnabled,
                        onCheckedChange = {
                            if (!mod.isCore) {
                                NexoRepository.toggleModule(business.id, mod)
                            }
                        },
                        enabled = !mod.isCore
                    )
                }
            }
        }

        // SaaS Plans Comparison & Upgrade
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("SaaS Abonelik Planı", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text("Mevcut Planınız: ${business.plan.title}", fontSize = 12.sp, color = NexoIndigoLight, fontWeight = FontWeight.SemiBold)
        }

        items(PlanType.entries) { plan ->
            val isCurrent = business.plan == plan
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) NexoIndigoPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                ),
                border = if (isCurrent) ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(NexoIndigoLight)
                ) else null
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(plan.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("${plan.priceTl} ₺/ay", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = NexoEmeraldLight)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "• ${if (plan.maxProducts == Int.MAX_VALUE) "Sınırsız" else "${plan.maxProducts}"} Ürün • ${if (plan.maxTables == Int.MAX_VALUE) "Sınırsız" else "${plan.maxTables}"} Masa • ${plan.aiCredits} AI Kredisi • ${plan.maxEmployees} Personel",
                        fontSize = 11.sp,
                        color = NexoDarkTextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    if (isCurrent) {
                        Surface(shape = RoundedCornerShape(6.dp), color = NexoEmerald.copy(alpha = 0.2f)) {
                            Text("✓ Aktif Kullanılan Plan", fontSize = 11.sp, color = NexoEmeraldLight, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                    } else {
                        Button(
                            onClick = {
                                NexoRepository.upgradePlan(business.id, plan)
                                showPlanUpgradeSuccess = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NexoIndigoPrimary),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(if (plan.priceTl > business.plan.priceTl) "Yükselt" else "Plana Geç", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }

    if (showPlanUpgradeSuccess) {
        AlertDialog(
            onDismissRequest = { showPlanUpgradeSuccess = false },
            confirmButton = {
                Button(onClick = { showPlanUpgradeSuccess = false }, colors = ButtonDefaults.buttonColors(containerColor = NexoIndigoPrimary)) {
                    Text("Tamam")
                }
            },
            title = { Text("Plan Başarıyla Güncellendi!") },
            text = { Text("Yeni plan limitleriniz ve modül yetkileriniz anında hesabınıza tanımlandı.") }
        )
    }
}

@Composable
fun SuperAdminScreen(
    onBack: () -> Unit
) {
    val businesses by NexoRepository.businesses.collectAsState()
    val totalMrr = businesses.sumOf { it.plan.priceTl }
    val totalAiUsage = businesses.sumOf { it.aiCreditsUsed }

    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("NEXO Platform Yönetimi (/admin)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("SaaS platform istatistikleri ve kiracı (tenant) kontrolü", style = MaterialTheme.typography.bodySmall, color = NexoDarkTextSecondary)
            }
            IconButton(onClick = onBack) {
                Icon(Icons.Default.Close, contentDescription = "Kapat")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Platform KPIs
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(
                title = "Toplam Kiracı (Tenant)",
                value = "${businesses.size} İşletme",
                subtitle = "%100 Aktif",
                icon = Icons.Default.Business,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Aylık Gelir (MRR)",
                value = "$totalMrr ₺/ay",
                subtitle = "SaaS Ciro",
                icon = Icons.Default.AttachMoney,
                iconColor = NexoEmeraldLight,
                badgeColor = NexoEmerald.copy(alpha = 0.15f),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text("Kayıtlı İşletmeler", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(businesses) { b ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(b.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Slug: ${b.slug} • Plan: ${b.plan.title}", fontSize = 11.sp, color = NexoDarkTextSecondary)
                            }
                            Switch(
                                checked = !b.isSuspended,
                                onCheckedChange = { NexoRepository.toggleBusinessSuspension(b.id) }
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (b.isSuspended) "İşletme askıya alındı." else "Aktif İşletme • AI Kredisi: ${b.aiCreditsUsed}/${b.plan.aiCredits}",
                            fontSize = 11.sp,
                            color = if (b.isSuspended) NexoRose else NexoEmeraldLight
                        )
                    }
                }
            }
        }
    }
}
