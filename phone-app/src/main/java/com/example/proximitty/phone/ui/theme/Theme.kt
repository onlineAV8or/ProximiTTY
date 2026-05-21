package com.example.proximitty.phone.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable

private val proximityLightScheme = lightColorScheme(
    primary = Color(0xFF1A56DB),           // deep blue — buttons, icons, accents
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD6E4FF),  // soft blue tint for icon badge bg
    onPrimaryContainer = Color(0xFF00276A),
    secondary = Color(0xFF2563EB),
    tertiary = Color(0xFF0369A1),
    background = Color(0xFFF5F7FA),        // near-white page background
    surface = Color(0xFFFFFFFF),           // card surface
    surfaceVariant = Color(0xFFEEF2F8),    // slightly tinted card bg
    onBackground = Color(0xFF111827),      // near-black — primary text
    onSurface = Color(0xFF111827),
    onSurfaceVariant = Color(0xFF4B5563),  // medium grey — secondary text
    outline = Color(0xFFD1D5DB),
    error = Color(0xFFDC2626),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),
)

@Composable
fun ProximityTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = proximityLightScheme,
        content = content,
    )
}
