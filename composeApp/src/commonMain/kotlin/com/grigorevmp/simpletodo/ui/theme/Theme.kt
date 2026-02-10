package com.grigorevmp.simpletodo.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.grigorevmp.simpletodo.model.ThemeMode

private val Light = lightColorScheme(
    primary = Color(0xFF4F46E5),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF06B6D4),
    onSecondary = Color(0xFF001018),
    tertiary = Color(0xFFF97316),
    onTertiary = Color(0xFF1B0A00),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0B1220),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0B1220),
    surfaceVariant = Color(0xFFEFF2F7),
    onSurfaceVariant = Color(0xFF1B2535),
    error = Color(0xFFDC2626),
    onError = Color(0xFFFFFFFF),
)

private val Dark = darkColorScheme(
    primary = Color(0xFF818CF8),
    onPrimary = Color(0xFF0B1020),
    secondary = Color(0xFF22D3EE),
    onSecondary = Color(0xFF001018),
    tertiary = Color(0xFFFB923C),
    onTertiary = Color(0xFF1B0A00),
    background = Color(0xFF070A12),
    onBackground = Color(0xFFEAF0FF),
    surface = Color(0xFF0B1220),
    onSurface = Color(0xFFEAF0FF),
    surfaceVariant = Color(0xFF121B2B),
    onSurfaceVariant = Color(0xFFB7C4E0),
    error = Color(0xFFEF4444),
    onError = Color(0xFF1B0000),
)

private val Dim = darkColorScheme(
    primary = Color(0xFFA1A1AA),
    onPrimary = Color(0xFF0A0A0B),
    secondary = Color(0xFF71717A),
    onSecondary = Color(0xFF0A0A0B),
    tertiary = Color(0xFF94A3B8),
    onTertiary = Color(0xFF0A0A0B),
    background = Color(0xFF0A0A0B),
    onBackground = Color(0xFFE4E4E7),
    surface = Color(0xFF111113),
    onSurface = Color(0xFFE4E4E7),
    surfaceVariant = Color(0xFF1A1A1E),
    onSurfaceVariant = Color(0xFFB4B4B8),
    error = Color(0xFFF87171),
    onError = Color(0xFF1B0000),
)

@Composable
fun DinoTheme(
    dark: Boolean,
    mode: ThemeMode,
    authorAccentIndex: Int,
    content: @Composable () -> Unit
) {
    val dynamic = platformDynamicColorScheme(dark)
    val scheme: ColorScheme = when (mode) {
        ThemeMode.DYNAMIC -> dynamic ?: if (dark) Dark else Light
        ThemeMode.DIM -> Dim
        ThemeMode.AUTHOR -> authorScheme(dark, authorAccentIndex)
        ThemeMode.SYSTEM -> dynamic ?: if (dark) Dark else Light
    }

    MaterialTheme(
        colorScheme = scheme,
        typography = DinoTypography,
        shapes = DinoShapes,
        content = content
    )
}

private fun authorScheme(dark: Boolean, accentIndex: Int): ColorScheme {
    val base = if (dark) Dark else Light
    val accent = authorAccentColors()[accentIndex.coerceIn(0, authorAccentColors().lastIndex)]
    val onAccent = if (accent.luminance() > 0.5f) Color(0xFF0B1020) else Color(0xFFFFFFFF)
    return base.copy(
        primary = accent,
        onPrimary = onAccent,
        secondary = accent,
        onSecondary = onAccent
    )
}

fun authorAccentColors(): List<Color> = listOf(
    Color(0xFF4F46E5), // Indigo
    Color(0xFF06B6D4), // Cyan
    Color(0xFFF97316), // Orange
    Color(0xFFEC4899), // Pink
    Color(0xFF10B981)  // Emerald
)
