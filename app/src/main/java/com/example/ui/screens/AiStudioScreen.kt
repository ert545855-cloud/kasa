package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.GeminiClient
import com.example.data.repository.NexoRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

data class AiTool(
    val id: String,
    val title: String,
    val subtitle: String,
    val defaultPrompt: String,
    val iconName: String
)

val nexoAiTools = listOf(
    AiTool("prod_desc", "1. Ürün Açıklaması Yazıcı", "Menü ve vitrin için iştah kabartan tanıtım", "Karamelli San Sebastian Cheesecake için lezzet odaklı 2 cümlelik açıklama yaz.", "MenuBook"),
    AiTool("menu_gen", "2. Menü Önerisi & Tasarımı", "Yeni sezona uygun 4 parçalık menü konsepti", "Sonbahar sezonuna özel 2 kahve ve 2 tatlıdan oluşan gurme bir menü konsepti öner.", "Restaurant"),
    AiTool("cat_suggest", "3. Kategori Önerisi", "Menü organizasyonu ve kategori yapısı", "Butik kafe için en verimli satış yapacak 5 ana menü kategorisi listele.", "Category"),
    AiTool("translate", "4. Çok Dilli Menü Çevirisi", "Turistler ve yabancı misafirler için İngilizce", "Menü ürünümüz 'Taze Fırın Kruvasan & Çikolata Sosu' ifadesini profesyonel restoran İngilizcesine çevir.", "Translate"),
    AiTool("insta_caption", "5. Instagram Paylaşımı & Caption", "Sosyal medya gönderisi, hashtag ve kanca", "Pazar kahvaltısı ve taze kahve için dikkat çekici bir Instagram metni ve 5 hashtag yaz.", "PhotoCamera"),
    AiTool("whatsapp_msg", "6. WhatsApp Satış & Kampanya", "Müdavim misafirler için doğrudan mesaj", "Müdavim müşterilerimize bu hafta içi %15 indirim olduğunu belirten samimi bir WhatsApp davet mesajı yaz.", "Chat"),
    AiTool("campaign_gen", "7. Kampanya Tasarlayıcı", "Hafta ortası satışlarını artıracak strateji", "Çarşamba ve Perşembe öğleden sonra saatlerinde ciro artıracak bir kafe kampanyası kurgula.", "Campaign"),
    AiTool("cust_msg", "8. Müşteri Teşekkür Mesajı", "Sipariş sonrası memnuniyet ve sadakat", "İlk kez QR menüden sipariş veren misafirimize nazik bir teşekkür ve sadakat puanı bilgilendirmesi hazırla.", "Favorite"),
    AiTool("quote_text", "9. Teklif Metni Oluşturucu", "B2B kurumsal ikram ve toplantı teklifi", "50 kişilik kurumsal şirket toplantısı catering ikramı için resmi ve güven veren bir teklif özet metni yaz.", "Description"),
    AiTool("biz_report", "10. Günlük Rapor Özeti", "Yöneticiye tek bakışta durum özeti", "Bugünkü 12.450 TL ciro ve 64 siparişlik satışı özetleyen yönetici işletme raporu yaz.", "Assessment"),
    AiTool("stock_insight", "11. Akıllı Stok Analizi", "Kritik malzeme tahmini ve tedarik uyarısı", "Kahve çekirdeği ve süt tüketim hızına göre hafta sonu için stok risk analizi ve öneri sun.", "Inventory"),
    AiTool("sales_insight", "12. Satış & Gelir Analizi", "En çok satanlar ve sepet büyüteçleri", "Tatlı siparişi veren müşterilere soğuk kahve çapraz satışı yapmak için 3 taktik öner.", "TrendingUp")
)

@Composable
fun AiStudioScreen() {
    val business = NexoRepository.getActiveBusiness()
    val aiLogs by NexoRepository.aiLogs.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTool by remember { mutableStateOf(nexoAiTools.first()) }
    var promptInput by remember { mutableStateOf(nexoAiTools.first().defaultPrompt) }
    var generatedResult by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var copySnackbar by remember { mutableStateOf(false) }
    var activeAiTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("NEXO Restoran AI", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            "Gemini 3.5 Flash",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    "Operasyonel zeka, menü optimizasyonu ve yönetici asistanı",
                    style = MaterialTheme.typography.bodySmall,
                    color = NexoDarkTextSecondary
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = NexoEmeraldDark.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "${business.plan.aiCredits - business.aiCreditsUsed} Kredi",
                    color = NexoEmeraldLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // AI Modes Tab Switcher
        TabRow(
            selectedTabIndex = activeAiTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.clip(RoundedCornerShape(10.dp))
        ) {
            Tab(
                selected = activeAiTab == 0,
                onClick = { activeAiTab = 0 },
                text = { Text("Restoran Asistanı (Sohbet)", fontSize = 11.sp, fontWeight = if (activeAiTab == 0) FontWeight.Bold else FontWeight.Normal) },
                icon = { Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = activeAiTab == 1,
                onClick = { activeAiTab = 1 },
                text = { Text("12 Akıllı Araç Stüdyosu", fontSize = 11.sp, fontWeight = if (activeAiTab == 1) FontWeight.Bold else FontWeight.Normal) },
                icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (activeAiTab == 0) {
            AiRestaurantManagerChatScreen()
        } else {
            // 12 Tools Selector Carousel
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(nexoAiTools) { tool ->
                val isSelected = tool.id == selectedTool.id
                Surface(
                    onClick = {
                        selectedTool = tool
                        promptInput = tool.defaultPrompt
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) NexoIndigoPrimary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.width(160.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = tool.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tool.subtitle,
                            fontSize = 9.sp,
                            color = if (isSelected) Color.White.copy(alpha = 0.8f) else NexoDarkTextSecondary,
                            maxLines = 2
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Prompt Input
        OutlinedTextField(
            value = promptInput,
            onValueChange = { promptInput = it },
            label = { Text("AI Talimatınız:") },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            maxLines = 4,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                if (promptInput.isNotBlank()) {
                    isLoading = true
                    coroutineScope.launch {
                        val sysInstruction = "Sen NEXO Business OS platformunun kurumsal yapay zeka asistanısın. Asla reçete, fiyat, yasal ve finansal bilgi uydurma. Verilen parametrelere tam sadık, profesyonel, modern ve çekici Türkçe içerik üret."
                        val response = GeminiClient.generateAiContent(promptInput, sysInstruction)
                        generatedResult = response
                        isLoading = false
                        NexoRepository.recordAiGeneration(selectedTool.id, promptInput, response)
                    }
                }
            },
            enabled = !isLoading && promptInput.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("ai_generate_button"),
            colors = ButtonDefaults.buttonColors(containerColor = NexoIndigoPrimary),
            shape = RoundedCornerShape(10.dp)
        ) {
            if (isLoading) {
                CircularProgressProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gemini Üretiyor...", fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("İçerik Üret (1 Kredi)", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // AI Response Container
        Card(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp).fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Oluşturulan Çıktı:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    if (generatedResult.isNotBlank()) {
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(generatedResult))
                                copySnackbar = true
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Kopyala", tint = NexoIndigoLight, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    item {
                        if (generatedResult.isNotBlank()) {
                            Text(
                                text = generatedResult,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Text(
                                text = "Yukarıdaki talimatı seçip 'İçerik Üret' butonuna dokunun. Google Gemini yapay zekası anında işletmenize özel metin oluşturacaktır.",
                                fontSize = 12.sp,
                                color = NexoDarkTextSecondary
                            )
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
fun CircularProgressProgressIndicator(modifier: Modifier = Modifier, color: Color = Color.White) {
    CircularProgressIndicator(modifier = modifier, color = color, strokeWidth = 2.dp)
}
