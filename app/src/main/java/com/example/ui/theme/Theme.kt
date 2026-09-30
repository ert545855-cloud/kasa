package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalRestaurantTheme = staticCompositionLocalOf { RestaurantTheme.MEDITERRANEAN }

@Composable
fun NexoRestaurantTheme(
    restaurantTheme: RestaurantTheme = RestaurantTheme.MEDITERRANEAN,
    content: @Composable () -> Unit
) {
    val lightColors = lightColorScheme(
        primary = restaurantTheme.primaryColor,
        onPrimary = Color.White,
        primaryContainer = restaurantTheme.primaryColor.copy(alpha = 0.12f),
        onPrimaryContainer = restaurantTheme.primaryColor,
        secondary = restaurantTheme.secondaryColor,
        onSecondary = Color.White,
        secondaryContainer = restaurantTheme.secondaryColor.copy(alpha = 0.12f),
        onSecondaryContainer = restaurantTheme.secondaryColor,
        tertiary = restaurantTheme.accentColor,
        background = restaurantTheme.backgroundColor,
        onBackground = NexoTextPrimary,
        surface = restaurantTheme.surfaceColor,
        onSurface = NexoTextPrimary,
        surfaceVariant = NexoWarmBeige,
        onSurfaceVariant = NexoTextSecondary,
        outline = NexoBorderLight,
        outlineVariant = NexoBorderSubtle,
        error = NexoRoseRed,
        onError = Color.White
    )

    CompositionLocalProvider(LocalRestaurantTheme provides restaurantTheme) {
        MaterialTheme(
            colorScheme = lightColors,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun NexoTheme(
    content: @Composable () -> Unit
) {
    val activeTheme by com.example.data.repository.NexoRepository.activeTheme.collectAsState()
    NexoRestaurantTheme(
        restaurantTheme = activeTheme,
        content = content
    )
}
