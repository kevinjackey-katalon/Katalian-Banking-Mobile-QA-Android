package com.katalon.katalianbanking.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.Typography

// Palette lifted from the web app's Tailwind classes (slate-950/900, emerald-500, red-500/600, cyan-500).
val Slate950 = Color(0xFF020617)
val Slate900 = Color(0xFF0F172A)
val SlateCard = Color(0xFF111C34)
val Slate500 = Color(0xFF64748B)
val Slate400 = Color(0xFF94A3B8)
val Emerald500 = Color(0xFF10B981)
val Emerald400 = Color(0xFF34D399)
val Red500 = Color(0xFFEF4444)
val Red600 = Color(0xFFDC2626)
val Cyan500 = Color(0xFF06B6D4)
val WhiteFaint = Color(0x14FFFFFF)

private val KatalianColorScheme = darkColorScheme(
    primary = Emerald500,
    onPrimary = Slate950,
    secondary = Cyan500,
    background = Slate950,
    onBackground = Color.White,
    surface = SlateCard,
    onSurface = Color.White,
    error = Red500,
    onError = Color.White
)

private val KatalianTypography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.Black),
    titleLarge = TextStyle(fontWeight = FontWeight.Black),
    titleMedium = TextStyle(fontWeight = FontWeight.Bold),
    labelSmall = TextStyle(fontWeight = FontWeight.Black)
)

@Composable
fun KatalianBankingTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KatalianColorScheme,
        typography = KatalianTypography,
        content = content
    )
}
