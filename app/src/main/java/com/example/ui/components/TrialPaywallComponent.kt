package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Business
import com.example.data.model.PlanType
import com.example.data.model.SubscriptionStatus
import com.example.data.repository.NexoRepository
import com.example.ui.theme.*

@Composable
fun TrialStatusBanner(
    business: Business,
    onOpenPaywall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPaid = business.isPaid || business.subscriptionStatus == SubscriptionStatus.PAID_ACTIVE
    val isExpired = business.isTrialExpired
    val daysRemaining = business.trialDaysRemaining

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isPaid -> NexoEmerald.copy(alpha = 0.12f)
                isExpired -> NexoRose.copy(alpha = 0.15f)
                else -> NexoWarmBeige.copy(alpha = 0.8f)
            }
        ),
        border = BorderStroke(
            1.dp,
            when {
                isPaid -> NexoEmeraldLight.copy(alpha = 0.4f)
                isExpired -> NexoRose.copy(alpha = 0.5f)
                else -> NexoTerracotta.copy(alpha = 0.35f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isPaid -> NexoEmerald.copy(alpha = 0.2f)
                                    isExpired -> NexoRose.copy(alpha = 0.2f)
                                    else -> NexoTerracotta.copy(alpha = 0.2f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                isPaid -> Icons.Default.Stars
                                isExpired -> Icons.Default.Warning
                                else -> Icons.Default.HourglassTop
                            },
                            contentDescription = null,
                            tint = when {
                                isPaid -> NexoEmeraldLight
                                isExpired -> NexoRose
                                else -> NexoTerracotta
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = when {
                                isPaid -> "Aktif Lisans: ${business.plan.title}"
                                isExpired -> "Deneme Süreniz Sona Erdi"
                                else -> "15 Günlük Ücretsiz Deneme"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = NexoDarkText
                        )
                        Text(
                            text = when {
                                isPaid -> "Tüm modüller ve sınırsız özellikler aktif"
                                isExpired -> "Restoran operasyonlarınıza devam etmek için bir plan seçin"
                                else -> "$daysRemaining gün kaldı • Süre bitiminde ücretli pakete geçilir"
                            },
                            fontSize = 11.sp,
                            color = NexoDarkTextSecondary
                        )
                    }
                }

                Button(
                    onClick = onOpenPaywall,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPaid) NexoEmerald else NexoBurgundy
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("trial_action_button")
                ) {
                    Text(
                        text = if (isPaid) "Planı Yönet" else if (isExpired) "Hemen Abone Ol" else "Plan Seç",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (!isPaid) {
                // Progress Bar for Trial Days
                val progress = if (isExpired) 1f else ((15 - daysRemaining) / 15f).coerceIn(0f, 1f)
                Column(modifier = Modifier.fillMaxWidth().padding(top = 2.dp)) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (isExpired) NexoRose else NexoTerracotta,
                        trackColor = NexoLightGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Başlangıç (15 Gün)", fontSize = 10.sp, color = NexoDarkTextSecondary)
                        Text(if (isExpired) "0 Gün Kaldı (Kilitli)" else "$daysRemaining Gün Kaldı", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isExpired) NexoRose else NexoTerracotta)
                    }
                }

                // Interactive Simulator for Testing (0 days / 5 days / 15 days)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Test Simülatörü:", fontSize = 10.sp, color = NexoDarkTextSecondary)
                    listOf(
                        "15 Gün" to 15,
                        "3 Gün" to 3,
                        "0 Gün (Süre Bitti)" to 0
                    ).forEach { (label, days) ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = NexoLightGray,
                            modifier = Modifier.clickable {
                                NexoRepository.simulateTrialDaysRemaining(business.id, days)
                            }
                        ) {
                            Text(
                                text = label,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NexoDarkTextSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionPaywallDialog(
    business: Business,
    onDismiss: () -> Unit
) {
    var selectedPlan by remember { mutableStateOf(business.plan.takeIf { it != PlanType.STARTER } ?: PlanType.PRO) }
    var isYearlyBilling by remember { mutableStateOf(false) }
    var isProcessingPayment by remember { mutableStateOf(false) }
    var paymentSuccess by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = NexoWarmBeige
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = NexoBurgundy, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("NEXO OS Restoran Aboneliği", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = NexoTextPrimary)
                    }
                    Text("15 günlük ücretsiz denemenizi kesintisiz profesyonel sürüme yükseltin", fontSize = 12.sp, color = NexoTextSecondary)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Kapat")
                }
            }

            if (paymentSuccess) {
                // Success state
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NexoEmerald.copy(alpha = 0.15f)),
                    border = BorderStroke(1.5.dp, NexoEmeraldLight)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NexoEmeraldLight, modifier = Modifier.size(48.dp))
                        Text("Tebrikler! Aboneliğiniz Aktifleştirildi", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NexoDarkText)
                        Text(
                            "${business.name} için ${selectedPlan.title} planı başarıyla tanımlandı. 15 günlük deneme sonrası lisansınız aktif.",
                            fontSize = 12.sp,
                            color = NexoDarkTextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = NexoEmerald)
                        ) {
                            Text("Restoran Paneline Dön")
                        }
                    }
                }
            } else {
                // Billing Toggle: Monthly vs Yearly
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(NexoLightGray)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (!isYearlyBilling) Color.White else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isYearlyBilling = false }
                    ) {
                        Text(
                            text = "Aylık Ödeme",
                            fontWeight = if (!isYearlyBilling) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = NexoTextPrimary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isYearlyBilling) Color.White else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isYearlyBilling = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Yıllık (2 Ay Hediye)",
                                fontWeight = if (isYearlyBilling) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp,
                                color = NexoTextPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(shape = RoundedCornerShape(4.dp), color = NexoEmerald.copy(alpha = 0.2f)) {
                                Text("%20 İndirim", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NexoEmeraldLight, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                    }
                }

                // Plan Cards
                PlanType.entries.forEach { plan ->
                    val isSelected = selectedPlan == plan
                    val monthlyPrice = if (isYearlyBilling) (plan.priceTl * 0.8).toInt() else plan.priceTl

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPlan = plan }
                            .testTag("plan_card_${plan.name.lowercase()}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color.White else NexoWarmBeige.copy(alpha = 0.6f)
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) NexoBurgundy else NexoBorderLight
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedPlan = plan },
                                        colors = RadioButtonDefaults.colors(selectedColor = NexoBurgundy)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(plan.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            if (plan == PlanType.PRO) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(shape = RoundedCornerShape(4.dp), color = NexoGold.copy(alpha = 0.25f)) {
                                                    Text("EN POPÜLER", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NexoWarmDark, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                                                }
                                            }
                                        }
                                        Text(
                                            when (plan) {
                                                PlanType.STARTER -> "Butik kafeler ve küçük işletmeler için"
                                                PlanType.PRO -> "Restoranlar, QR Menü & Tam KDS Mutfak Ekranı"
                                                PlanType.BUSINESS -> "Çoklu şubeler, zincir restoranlar ve franchising"
                                            },
                                            fontSize = 11.sp,
                                            color = NexoTextSecondary
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "₺$monthlyPrice",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 17.sp,
                                        color = NexoBurgundy
                                    )
                                    Text("/ay", fontSize = 10.sp, color = NexoTextSecondary)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Features in plan
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(start = 36.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("• ${if (plan.maxProducts == Int.MAX_VALUE) "Sınırsız" else "${plan.maxProducts}"} Ürün", fontSize = 11.sp, color = NexoTextPrimary)
                                Text("• ${if (plan.maxTables == Int.MAX_VALUE) "Sınırsız" else "${plan.maxTables}"} Masa", fontSize = 11.sp, color = NexoTextPrimary)
                                Text("• ${plan.maxEmployees} Personel", fontSize = 11.sp, color = NexoTextPrimary)
                                Text("• ${plan.aiCredits} AI", fontSize = 11.sp, color = NexoTextPrimary)
                            }
                        }
                    }
                }

                // What's included checklist
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        "15 günlük deneme süresi bitiminde otomatik faturalandırılır.",
                        "İstediğiniz zaman tek tıkla iptal edebilir veya plan değiştirebilirsiniz.",
                        "Firebase gerçek zamanlı veri senkronizasyonu ve sınırsız QR Menü dahildir."
                    ).forEach { feature ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = NexoEmerald, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(feature, fontSize = 11.sp, color = NexoTextSecondary)
                        }
                    }
                }

                // Payment & Activation CTA
                Button(
                    onClick = {
                        isProcessingPayment = true
                        // Simulate payment processing and instant activation
                        NexoRepository.activatePaidSubscription(business.id, selectedPlan)
                        isProcessingPayment = false
                        paymentSuccess = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("confirm_subscription_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = NexoBurgundy),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isProcessingPayment
                ) {
                    if (isProcessingPayment) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        val monthlyPrice = if (isYearlyBilling) (selectedPlan.priceTl * 0.8).toInt() else selectedPlan.priceTl
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${selectedPlan.title} Aboneliğini Başlat (₺$monthlyPrice / ay)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
