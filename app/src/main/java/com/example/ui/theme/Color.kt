package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Light, Warm, Premium Hospitality SaaS Palette
val NexoOffWhite = Color(0xFFFAFAF7)
val NexoWarmBeige = Color(0xFFF5F2EB)
val NexoCream = Color(0xFFFDFBF7)
val NexoLightGray = Color(0xFFF1F0EC)
val NexoBorderLight = Color(0xFFE5E2DA)
val NexoBorderSubtle = Color(0xFFECE9E2)

val NexoTextPrimary = Color(0xFF1C1917) // Warm stone 900
val NexoTextSecondary = Color(0xFF57534E) // Warm stone 600
val NexoTextMuted = Color(0xFF8C867E)

// Restaurant Accent & Hospitality Colors
val NexoBurgundy = Color(0xFF881337)
val NexoTerracotta = Color(0xFFC2410C)
val NexoGold = Color(0xFFD97706)
val NexoOliveGreen = Color(0xFF3F6212)
val NexoSoftGreen = Color(0xFF16A34A)
val NexoSoftBlue = Color(0xFF0284C7)
val NexoWarmAmber = Color(0xFFF59E0B)
val NexoRoseRed = Color(0xFFDC2626)
val NexoEspressoBrown = Color(0xFF451A03)

// Restaurant Theme Definitions
enum class RestaurantTheme(
    val id: String,
    val titleTr: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentColor: Color,
    val surfaceColor: Color,
    val backgroundColor: Color,
    val description: String
) {
    MODERN_CAFE(
        "theme_cafe",
        "Modern Cafe",
        Color(0xFF634832),
        Color(0xFF967259),
        Color(0xFFDDA15E),
        Color(0xFFFFFFFF),
        Color(0xFFFDFBF7),
        "Beyaz + krem + zengin espresso tonları"
    ),
    ITALIAN(
        "theme_italian",
        "Italian Restaurant",
        Color(0xFF831843),
        Color(0xFF365314),
        Color(0xFFB45309),
        Color(0xFFFFFFFF),
        Color(0xFFFAFAF5),
        "Krem + zeytin yeşili + sıcak kırmızı"
    ),
    STEAKHOUSE(
        "theme_steakhouse",
        "Premium Steakhouse",
        Color(0xFF7F1D1D),
        Color(0xFF450A0A),
        Color(0xFFD97706),
        Color(0xFFFFFFFF),
        Color(0xFFF9F7F5),
        "Koyu bordo + krem + gold accent"
    ),
    MEDITERRANEAN(
        "theme_mediterranean",
        "Mediterranean (Casa Verde)",
        Color(0xFF0284C7),
        Color(0xFFC2410C),
        Color(0xFF0D9488),
        Color(0xFFFFFFFF),
        Color(0xFFF8FAFC),
        "Beyaz + açık sahil mavisi + terracotta"
    ),
    JAPANESE(
        "theme_japanese",
        "Japanese Dining",
        Color(0xFFB91C1C),
        Color(0xFF78350F),
        Color(0xFF1C1917),
        Color(0xFFFFFFFF),
        Color(0xFFFAFAF9),
        "Beyaz + soft kırmızı + doğal ahşap tonları"
    ),
    BURGER(
        "theme_burger",
        "Burger House",
        Color(0xFFDC2626),
        Color(0xFFD97706),
        Color(0xFF166534),
        Color(0xFFFFFFFF),
        Color(0xFFFFFBEB),
        "Krem + domates kırmızısı + hardal sarısı"
    ),
    BAKERY(
        "theme_bakery",
        "Bakery & Patisserie",
        Color(0xFFB45309),
        Color(0xFFFB923C),
        Color(0xFF854D0E),
        Color(0xFFFFFFFF),
        Color(0xFFFFF7ED),
        "Pastel şeftali + tatlı krem + tarçın"
    ),
    FINE_DINING(
        "theme_fine_dining",
        "Luxury Fine Dining",
        Color(0xFF4C0519),
        Color(0xFF831843),
        Color(0xFFB45309),
        Color(0xFFFFFFFF),
        Color(0xFFFAFAF7),
        "Fildişi + asil bordo + dore gold"
    ),
    COFFEE_SHOP(
        "theme_coffee_shop",
        "Artisan Coffee Shop",
        Color(0xFF451A03),
        Color(0xFF78350F),
        Color(0xFFD97706),
        Color(0xFFFFFFFF),
        Color(0xFFFDFBF7),
        "Krem + espresso kahve + karamel"
    ),
    CUSTOM_AI(
        "theme_custom_ai",
        "Custom AI Theme",
        Color(0xFF4F46E5),
        Color(0xFF059669),
        Color(0xFFD97706),
        Color(0xFFFFFFFF),
        Color(0xFFFAF9F6),
        "AI ile otomatik oluşturulmuş restoran marka kimliği"
    )
}

// Backwards-compatibility mappings for premium hospitality SaaS palette:
val NexoIndigoPrimary = Color(0xFF634832)
val NexoIndigoDark = Color(0xFF451A03)
val NexoIndigoLight = Color(0xFF967259)

val NexoEmerald = Color(0xFF16A34A)
val NexoEmeraldDark = Color(0xFF15803D)
val NexoEmeraldLight = Color(0xFF22C55E)

val NexoRose = Color(0xFFDC2626)
val NexoAmber = Color(0xFFD97706)
val NexoCyan = Color(0xFF0284C7)
val NexoPurple = Color(0xFF7C3AED)

val NexoDarkTextPrimary = NexoTextPrimary
val NexoDarkTextSecondary = NexoTextSecondary
val NexoDarkTextMuted = NexoTextMuted
val NexoDarkBorder = NexoBorderLight
val NexoDarkSurface = Color(0xFF1F2937)
