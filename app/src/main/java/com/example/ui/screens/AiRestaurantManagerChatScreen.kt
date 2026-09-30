package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.data.remote.GeminiClient
import com.example.data.repository.NexoRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "user" or "model"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun AiRestaurantManagerChatScreen() {
    val business = NexoRepository.getActiveBusiness()
    val orders by NexoRepository.orders.collectAsState()
    val products by NexoRepository.products.collectAsState()
    val stockItems by NexoRepository.stockItems.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var inputPrompt by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var selectedModel by remember { mutableStateOf("gemini-3.5-flash") }

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                sender = "model",
                text = "Merhaba! Ben NEXO AI Restoran Yöneticinizim. 👨‍🍳\n\nMenünüz, sipariş hacminiz, reçete food cost oranlarınız ve kritik stok durumunuz hakkında bana dilediğiniz soruyu sorabilirsiniz."
            )
        )
    }

    val quickQuestions = listOf(
        "Bugün kaç sipariş aldım?",
        "Hangi ürünlerin food cost'u yüksek?",
        "Bu ay en çok satan ürünler hangileri?",
        "Stokta kritik ürün var mı?",
        "Bugün hangi ürünleri kampanyaya sokmalıyım?"
    )

    val systemInstruction = remember(business.name, orders.size, stockItems.size) {
        """
        Sen NEXO RESTAURANT OS platformunun kıdemli AI Restoran Operasyon Yöneticisisin (AI Restaurant Manager).
        İşletme Adı: ${business.name}
        Aktif Masa Siparişleri: ${orders.size} adet
        Kayıtlı Menü Ürünleri: ${products.size} adet
        Takip Edilen Stok Kalemleri: ${stockItems.size} adet
        
        Kurallar:
        1. Asla içerik, reçete, fiyat veya finansal veri uydurma.
        2. Restoran sahibine profesyonel, yapıcı, misafirperverlik sektörüne uygun pratik ve kârlılık odaklı tavsiyeler ver.
        3. Food cost, masa devir hızı, menü mühendisliği ve atık azaltımı konularında uzman gibi davran.
        """.trimIndent()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NexoWarmBeige)
            .padding(14.dp)
    ) {
        // Header
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
                        .background(NexoTerracotta),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "AI Restoran Müdürü",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NexoTextPrimary
                    )
                    Text(
                        text = "Doğal dille canlı işletme ve food cost asistanı",
                        style = MaterialTheme.typography.bodySmall,
                        color = NexoTextSecondary
                    )
                }
            }

            // Model Switcher Chip (gemini-3.5-flash vs gemini-3.1-pro-preview)
            Surface(
                onClick = {
                    selectedModel = if (selectedModel == "gemini-3.5-flash") "gemini-3.1-pro-preview" else "gemini-3.5-flash"
                },
                shape = RoundedCornerShape(8.dp),
                color = Color.White,
                border = ButtonDefaults.outlinedButtonBorder(enabled = true)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedModel == "gemini-3.5-flash") "Flash 3.5" else "Pro 3.1",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NexoTerracotta
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Suggestion Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(quickQuestions) { q ->
                Surface(
                    onClick = {
                        inputPrompt = q
                    },
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true)
                ) {
                    Text(
                        text = q,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = NexoTextSecondary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Chat Message Thread (Scrollable)
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { msg ->
                    val isUser = msg.sender == "user"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 14.dp,
                                topEnd = 14.dp,
                                bottomStart = if (isUser) 14.dp else 2.dp,
                                bottomEnd = if (isUser) 2.dp else 14.dp
                            ),
                            color = if (isUser) NexoTerracotta else NexoLightGray,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Text(
                                text = msg.text,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                color = if (isUser) Color.White else NexoTextPrimary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }
                }

                if (isLoading) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = NexoTerracotta,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Restoran verileri taranıyor...", fontSize = 12.sp, color = NexoTextSecondary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Input Field
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputPrompt,
                onValueChange = { inputPrompt = it },
                placeholder = { Text("Restoran hakkında bir soru sorun...", fontSize = 13.sp) },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_chat_input"),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            FloatingActionButton(
                onClick = {
                    val promptToSend = inputPrompt.trim()
                    if (promptToSend.isNotBlank() && !isLoading) {
                        inputPrompt = ""
                        messages.add(ChatMessage(sender = "user", text = promptToSend))
                        isLoading = true

                        coroutineScope.launch {
                            listState.animateScrollToItem(messages.size - 1)
                            val conversationPairs = messages.map { it.sender to it.text }
                            val aiAnswer = GeminiClient.sendMultiTurnChat(
                                messages = conversationPairs,
                                systemInstruction = systemInstruction,
                                model = selectedModel
                            )
                            messages.add(ChatMessage(sender = "model", text = aiAnswer))
                            isLoading = false
                            listState.animateScrollToItem(messages.size - 1)
                            NexoRepository.recordAiGeneration("ai_chat", promptToSend, aiAnswer)
                        }
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("ai_chat_send_btn"),
                containerColor = NexoTerracotta,
                contentColor = Color.White,
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Gönder", modifier = Modifier.size(20.dp))
            }
        }
    }
}
