package com.example.reelstracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// ☀️ Light — minimal, calm
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF3A7AFE),          // Soft blue accent
    onPrimary = Color.White,

    background = Color(0xFFF8F9FA),
    onBackground = Color(0xFF111111),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111111),

    surfaceVariant = Color(0xFFF1F3F5),   // Cards
    onSurfaceVariant = Color(0xFF444444),

    secondaryContainer = Color(0xFFF1F3F5),
    onSecondaryContainer = Color(0xFF222222),

    primaryContainer = Color(0xFFF0F5FF),
    onPrimaryContainer = Color(0xFF1A3D7C),

    error = Color(0xFFDC3545),
    onError = Color.White
)

// 🌙 Dark — true minimalist dark
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8BB2FF),
    onPrimary = Color.Black,

    background = Color(0xFF0E0F12),
    onBackground = Color(0xFFEDEDED),

    surface = Color(0xFF16181D),
    onSurface = Color(0xFFEDEDED),

    surfaceVariant = Color(0xFF1E2026),
    onSurfaceVariant = Color(0xFFB0B0B0),

    secondaryContainer = Color(0xFF1E2026),
    onSecondaryContainer = Color(0xFFEDEDED),

    primaryContainer = Color(0xFF1A2336),
    onPrimaryContainer = Color(0xFFD6E3FF),

    error = Color(0xFFFF6B6B),
    onError = Color.Black
)

@Composable
fun ReelsTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // ❌ disable Material You (important)
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
