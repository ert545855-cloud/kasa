package com.example.data.service

import com.example.data.model.*
import com.example.data.remote.GeminiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class DailyBusinessSnapshot(
    val businessName: String,
    val businessType: String,
    val currency: String,
    val totalOrders: Int,
    val todayRevenue: Double,
    val dineInOrders: Int,
    val takeawayOrders: Int,
    val lowStockCount: Int,
    val lowStockItems: List<String>,
    val totalCustomers: Int,
    val topSellingItems: List<String>
)

class GeminiDailySummaryService {

    suspend fun generateDailyPerformanceSummary(snapshot: DailyBusinessSnapshot): Result<String> = withContext(Dispatchers.IO) {
        try {
            val systemInstruction = """
                Sen NEXO Business OS platformunun kıdemli AI restoran ve işletme danışmanısın.
                Sana sunulan günlük işletme performans verilerini analiz ederek işletme sahibine net, motive edici ve operasyonel kararlar almasını sağlayacak bir 'Günlük Yönetici Özeti' sunacaksın.
                Kullanıcıya gereksiz teknik terimler yerine doğrudan ciro, kârlılık, sipariş hızı, stok emniyeti ve müşteri sadakati odaklı hap bilgiler ver.
            """.trimIndent()

            val prompt = """
                İşletme Adı: ${snapshot.businessName} (${snapshot.businessType})
                Para Birimi: ${snapshot.currency}
                Bugünkü Sipariş Sayısı: ${snapshot.totalOrders} adet
                Bugünkü Toplam Ciro: %.2f ${snapshot.currency}
                Masa Siparişi: ${snapshot.dineInOrders}, Paket/Gel-Al: ${snapshot.takeawayOrders}
                Kritik Seviyedeki Stok/Malzemeler (${snapshot.lowStockCount} adet): ${snapshot.lowStockItems.joinToString(", ").ifEmpty { "Kritik stok yok" }}
                Kayıtlı Misafir Sayısı: ${snapshot.totalCustomers}
                En Çok Satan Ürünler: ${snapshot.topSellingItems.joinToString(", ").ifEmpty { "Standart menü dağılımı" }}

                Lütfen şu başlıklar altında Türkçe profesyonel bir özet oluştur:
                1. 📊 Günlük Ciro & Operasyonel Değerlendirme (Bugünkü tempo nasıl geçti?)
                2. 🏆 Ürün & Satış Verimliliği (En çok kazandıranlar ve sepet durumu)
                3. ⚠️ Stok ve Tedarik Uyarıları (Yarın mutfakta/dükkanda aksaklık olmaması için yapılması gerekenler)
                4. 💡 Yarın İçin 3 Somut Aksiyon Önerisi
            """.trimIndent()

            val aiResponse = GeminiClient.generateAiContent(
                prompt = prompt,
                systemInstruction = systemInstruction,
                model = "gemini-3.5-flash"
            )

            Result.success(aiResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
