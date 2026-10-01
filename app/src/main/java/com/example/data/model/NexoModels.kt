package com.example.data.model

import java.util.UUID

enum class NexoModule(
    val key: String,
    val titleTr: String,
    val titleEn: String,
    val description: String,
    val monthlyPriceTl: Int,
    val isCore: Boolean = false
) {
    MENU("menu", "Dijital Menü", "Digital Menu", "Kategori ve ürün yönetimi, varyantlar ve alerjenler", 0, true),
    ORDERS("orders", "Siparişler & Mutfak", "Orders & Kitchen", "Anlık masa siparişleri ve KDS mutfak ekranı", 0, true),
    CRM("crm", "Müşteri (CRM)", "Customer CRM", "Müşteri profilleri, harcama geçmişi ve segmentasyon", 99),
    STOCK("stock", "Stok & Envanter", "Stock & Inventory", "Kritik stok uyarıları, hareketler ve otomatik düşüş", 99),
    BOOKING("booking", "Rezervasyon", "Booking & Appointments", "Hizmet ve randevu takvimi, personel ataması", 149),
    LOYALTY("loyalty", "Sadakat & Puan", "Loyalty & Rewards", "Puan biriktirme ve hediye ödül sistemi", 99),
    SALES("sales", "Kasa & POS Satış", "POS & Quick Sales", "Hızlı kasa satışı, tahsilat ve gün sonu raporu", 99),
    STAFF("staff", "Personel & Roller", "Staff & Roles", "Garson, mutfak, kasa ve yetkilendirme matrisi", 99),
    AI("ai", "NEXO Yapay Zeka", "NEXO AI Assistant", "Gemini destekli menü, sosyal medya, pazarlama asistanı", 149),
    ANALYTICS("analytics", "Rapor & Analitik", "Analytics & Insights", "Ciro, sipariş trendi, en çok satanlar ve grafikler", 99),
    CAMPAIGNS("campaigns", "Kampanyalar", "Campaigns & Marketing", "İndirimler, VIP kurguları ve duyurular", 99),
    QUOTES("quotes", "Teklif Yönetimi", "B2B Quotations", "B2B profesyonel teklif hazırlama ve paylaşım", 149),
    SETTINGS("settings", "İşletme Ayarları", "Settings", "Şube, para birimi, marka ve genel konfigürasyon", 0, true);

    companion object {
        fun fromKey(key: String): NexoModule = entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: MENU
    }
}

enum class BusinessType(val titleTr: String, val titleEn: String) {
    CAFE("Kafe & Kahveci", "Cafe & Coffee Shop"),
    RESTAURANT("Restoran & Lokanta", "Restaurant & Dining"),
    BARBER("Berber & Kuaför", "Barber & Hair Salon"),
    REPAIR("Tamirci & Teknik Servis", "Repair & Auto Service"),
    GROCERY("Bakkal, Şarküteri & Market", "Grocery & Mini Market"),
    BAR("Bar & Pub", "Bar & Pub"),
    BAKERY("Pastane & Fırın", "Bakery & Patisserie"),
    HOTEL("Otel & Konaklama", "Hotel & Hospitality"),
    BEAUTY("Güzellik Merkezi & Spa", "Beauty & Spa"),
    RETAIL("Perakende & Mağaza", "Retail Store"),
    TEXTILE("Tekstil & Giyim", "Textile & Apparel"),
    MANUFACTURING("İmalat & Üretim", "Manufacturing"),
    SERVICE("Hizmet & Danışmanlık", "Service & Consultancy"),
    OTHER("Diğer İşletme", "Other Business")
}

enum class PlanType(
    val title: String,
    val priceTl: Int,
    val maxProducts: Int,
    val maxTables: Int,
    val maxEmployees: Int,
    val aiCredits: Int
) {
    STARTER("STARTER", 199, 100, 20, 1, 50),
    PRO("PRO", 399, Int.MAX_VALUE, Int.MAX_VALUE, 5, 250),
    BUSINESS("BUSINESS", 799, Int.MAX_VALUE, Int.MAX_VALUE, 15, 750)
}

enum class UserRole(val titleTr: String) {
    OWNER("İşletme Sahibi"),
    MANAGER("Müdür / Yönetici"),
    EMPLOYEE("Çalışan / Garson"),
    WAITER("Garson"),
    CASHIER("Kasiyer"),
    KITCHEN("Mutfak Şefi"),
    RECEPTION("Resepsiyonist")
}

enum class NexoPermission(val label: String) {
    VIEW_ORDERS("Siparişleri Görüntüle"),
    MANAGE_ORDERS("Sipariş Durumu Değiştir"),
    MANAGE_MENU("Menü & Ürün Düzenle"),
    MANAGE_STOCK("Stok & Envanter Yönet"),
    MANAGE_CUSTOMERS("Müşteri Kayıtlarını Yönet"),
    VIEW_ANALYTICS("Finans & Raporları Gör"),
    MANAGE_STAFF("Personel Ekle & Yetkilendir"),
    MANAGE_SETTINGS("İşletme Ayarlarını Değiştir")
}

enum class SubscriptionStatus(val labelTr: String) {
    TRIAL_ACTIVE("15 Günlük Deneme Sürümü"),
    PAID_ACTIVE("Aktif Lisans"),
    EXPIRED("Deneme Süresi Doldu")
}

data class Business(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val slug: String,
    val businessType: BusinessType = BusinessType.CAFE,
    val logoUrl: String? = null,
    val coverUrl: String? = null,
    val phone: String = "+90 555 000 0000",
    val address: String = "Bağdat Caddesi No:42, Kadıköy, İstanbul",
    val currency: String = "₺",
    val language: String = "tr",
    val plan: PlanType = PlanType.PRO,
    val enabledModules: Set<NexoModule> = NexoModule.entries.toSet(),
    val aiCreditsUsed: Int = 18,
    val branches: List<Branch> = listOf(Branch(name = "Merkez Şube", address = "Kadıköy, İstanbul", isMain = true)),
    val isSuspended: Boolean = false,
    val websiteDomain: String = "$slug.nexo.business",
    val brandColorHex: String = "#D97706",
    val tagline: String = "Özel Kahveler & Taze Lezzetler",
    val createdAt: Long = System.currentTimeMillis(),
    val subscriptionStatus: SubscriptionStatus = SubscriptionStatus.TRIAL_ACTIVE,
    val trialStartDate: Long = System.currentTimeMillis(),
    val trialEndDate: Long = System.currentTimeMillis() + 15L * 24 * 60 * 60 * 1000L,
    val isPaid: Boolean = false
) {
    val trialDaysRemaining: Int
        get() {
            if (isPaid || subscriptionStatus == SubscriptionStatus.PAID_ACTIVE) return 365
            val now = System.currentTimeMillis()
            if (now >= trialEndDate) return 0
            val millisLeft = trialEndDate - now
            return ((millisLeft / (1000L * 60 * 60 * 24)) + 1).toInt().coerceIn(0, 15)
        }

    val isTrialExpired: Boolean
        get() = !isPaid && subscriptionStatus != SubscriptionStatus.PAID_ACTIVE && trialDaysRemaining <= 0
}

data class NexoPushNotification(
    val id: String = UUID.randomUUID().toString(),
    val businessId: String,
    val orderId: String,
    val orderNumber: String,
    val tableInfo: String,
    val itemsSummary: String,
    val totalAmountFormatted: String,
    val total: Double,
    val timestamp: Long = System.currentTimeMillis()
)

data class Branch(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val address: String,
    val isMain: Boolean = false
)

data class Employee(
    val id: String = UUID.randomUUID().toString(),
    val businessId: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: UserRole,
    val permissions: Set<NexoPermission> = NexoPermission.entries.toSet(),
    val isActive: Boolean = true
)

data class Category(
    val id: String = UUID.randomUUID().toString(),
    val businessId: String,
    val name: String,
    val iconName: String = "Coffee",
    val sortOrder: Int = 0,
    val isActive: Boolean = true
)

data class ProductVariant(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val priceDiff: Double = 0.0
)

data class ProductOption(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val priceDiff: Double = 0.0
)

data class RecipeIngredient(
    val name: String,
    val amount: String,
    val cost: Double
)

data class Product(
    val id: String = UUID.randomUUID().toString(),
    val businessId: String,
    val categoryId: String,
    val name: String,
    val description: String,
    val price: Double,
    val ingredientCost: Double = 0.0,
    val imageUrl: String? = null,
    val isAvailable: Boolean = true,
    val isFeatured: Boolean = false,
    val sortOrder: Int = 0,
    val allergens: List<String> = emptyList(),
    val variants: List<ProductVariant> = emptyList(),
    val options: List<ProductOption> = emptyList(),
    val recipe: List<RecipeIngredient> = emptyList()
) {
    val foodCostPercent: Double get() = if (price > 0) (ingredientCost / price) * 100.0 else 0.0
    val grossMargin: Double get() = price - ingredientCost
}

enum class TableStatus(val titleTr: String, val colorHex: Long) {
    EMPTY("Boş", 0xFF10B981),
    OCCUPIED("Dolu", 0xFF0284C7),
    ORDER_PENDING("Sipariş Bekliyor", 0xFFF59E0B),
    PREPARING("Hazırlanıyor", 0xFF8B5CF6),
    BILL_REQUESTED("Hesap İstendi", 0xFFDC2626),
    RESERVED("Rezerve", 0xFFD97706),
    CLEANING("Temizleniyor", 0xFF64748B)
}

data class TableQr(
    val id: String = UUID.randomUUID().toString(),
    val businessId: String,
    val tableNumber: Int,
    val label: String,
    val secureToken: String = UUID.randomUUID().toString().take(8),
    val isEnabled: Boolean = true,
    val status: TableStatus = TableStatus.EMPTY,
    val capacity: Int = 4,
    val qrDesignColor: String = "#0284C7",
    val callToActionText: String = "Menüyü Gör & Sipariş Ver"
)

enum class OrderStatus(val titleTr: String, val colorHex: Long) {
    PENDING("Yeni Sipariş", 0xFFF59E0B),
    ACCEPTED("Onaylandı", 0xFF3B82F6),
    PREPARING("Hazırlanıyor", 0xFF8B5CF6),
    READY("Servise Hazır", 0xFF10B981),
    COMPLETED("Tamamlandı", 0xFF64748B),
    CANCELLED("İptal Edildi", 0xFFEF4444)
}

enum class PaymentMethod(val titleTr: String) {
    CASH("Nakit"),
    CARD("Kredi / Banka Kartı"),
    TRANSFER("Havale / FAST"),
    UNPAID("Ödenmedi")
}

data class OrderItem(
    val id: String = UUID.randomUUID().toString(),
    val productId: String,
    val productName: String,
    val variantName: String? = null,
    val selectedOptions: List<String> = emptyList(),
    val unitPrice: Double,
    val quantity: Int = 1,
    val total: Double = unitPrice * quantity
)

data class Order(
    val id: String = UUID.randomUUID().toString(),
    val businessId: String,
    val orderNumber: String,
    val tableNumber: Int?,
    val customerName: String? = null,
    val customerPhone: String? = null,
    val items: List<OrderItem>,
    val subtotal: Double,
    val discount: Double = 0.0,
    val total: Double,
    val status: OrderStatus = OrderStatus.PENDING,
    val paymentMethod: PaymentMethod = PaymentMethod.UNPAID,
    val notes: String? = null,
    val createdAtTimestamp: Long = System.currentTimeMillis()
)

data class Customer(
    val id: String = UUID.randomUUID().toString(),
    val businessId: String,
    val name: String,
    val phone: String,
    val email: String? = null,
    val notes: String? = null,
    val tags: List<String> = emptyList(),
    val totalSpent: Double = 0.0,
    val orderCount: Int = 0,
    val lastOrderAt: Long? = null,
    val loyaltyPoints: Int = 0
)

data class StaffInvitation(
    val id: String = UUID.randomUUID().toString(),
    val businessId: String,
    val email: String,
    val name: String,
    val role: UserRole = UserRole.WAITER,
    val note: String = "",
    val token: String = UUID.randomUUID().toString().take(8).uppercase(),
    val status: String = "PENDING", // PENDING, ACCEPTED, CANCELLED
    val createdAtTimestamp: Long = System.currentTimeMillis()
)

data class StockItem(
    val id: String = UUID.randomUUID().toString(),
    val businessId: String,
    val sku: String,
    val name: String,
    val category: String,
    val purchasePrice: Double,
    val sellingPrice: Double,
    val stockQuantity: Double,
    val minimumStock: Double,
    val unit: String = "Adet",
    val supplier: String = "Genel Tedarikçi"
) {
    val isLowStock: Boolean get() = stockQuantity <= minimumStock
    val isOutOfStock: Boolean get() = stockQuantity <= 0
}

enum class StockMovementType(val titleTr: String) {
    INCREASE("Giriş / Alım"),
    DECREASE("Çıkış / Fire"),
    ADJUSTMENT("Sayım Düzeltme"),
    SALE_ORDER("Siparişten Düşüm")
}

data class StockMovement(
    val id: String = UUID.randomUUID().toString(),
    val stockItemId: String,
    val productName: String,
    val type: StockMovementType,
    val quantity: Double,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ServiceItem(
    val id: String = UUID.randomUUID().toString(),
    val businessId: String,
    val name: String,
    val durationMinutes: Int = 30,
    val price: Double,
    val description: String = ""
)

enum class AppointmentStatus(val titleTr: String) {
    PENDING("Bekliyor"),
    CONFIRMED("Onaylandı"),
    COMPLETED("Tamamlandı"),
    CANCELLED("İptal Edildi"),
    NO_SHOW("Gelmedi")
}

data class Appointment(
    val id: String = UUID.randomUUID().toString(),
    val businessId: String,
    val customerName: String,
    val customerPhone: String,
    val serviceId: String,
    val serviceName: String,
    val staffName: String,
    val dateString: String,
    val timeString: String,
    val status: AppointmentStatus = AppointmentStatus.CONFIRMED,
    val notes: String = ""
)

data class LoyaltyReward(
    val id: String = UUID.randomUUID().toString(),
    val businessId: String,
    val title: String,
    val requiredPoints: Int,
    val description: String
)

data class SaleRecord(
    val id: String = UUID.randomUUID().toString(),
    val businessId: String,
    val receiptNo: String,
    val itemsSummary: String,
    val totalAmount: Double,
    val paymentMethod: PaymentMethod,
    val customerName: String?,
    val timestamp: Long = System.currentTimeMillis()
)

enum class QuoteStatus(val titleTr: String) {
    DRAFT("Taslak"),
    SENT("Gönderildi"),
    ACCEPTED("Kabul Edildi"),
    REJECTED("Reddedildi"),
    EXPIRED("Süresi Doldu")
}

data class QuoteItem(
    val name: String,
    val description: String = "",
    val quantity: Double,
    val unitPrice: Double,
    val total: Double = quantity * unitPrice
)

data class Quote(
    val id: String = UUID.randomUUID().toString(),
    val businessId: String,
    val quoteNumber: String,
    val customerName: String,
    val customerEmail: String,
    val customerPhone: String,
    val items: List<QuoteItem>,
    val subtotal: Double,
    val discountPercent: Double = 0.0,
    val taxPercent: Double = 20.0,
    val total: Double,
    val notes: String = "Fiyatlarımıza KDV dahildir. Geçerlilik süresi 15 gündür.",
    val validityDate: String,
    val status: QuoteStatus = QuoteStatus.SENT
)

enum class CampaignType(val titleTr: String) {
    PERCENTAGE("Yüzde İndirimi"),
    FIXED_DISCOUNT("Sabit Tutar İndirimi"),
    FREE_PRODUCT("Hediye Ürün"),
    LOYALTY_BONUS("Ekstra Sadakat Puanı"),
    ANNOUNCEMENT("Duyuru")
}

data class Campaign(
    val id: String = UUID.randomUUID().toString(),
    val businessId: String,
    val name: String,
    val description: String,
    val type: CampaignType = CampaignType.PERCENTAGE,
    val discountValue: String = "%15",
    val targetCustomerSegment: String = "Tüm Müşteriler",
    val startDate: String,
    val endDate: String,
    val isActive: Boolean = true
)

data class AiGenerationLog(
    val id: String = UUID.randomUUID().toString(),
    val businessId: String,
    val toolType: String,
    val prompt: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
)
