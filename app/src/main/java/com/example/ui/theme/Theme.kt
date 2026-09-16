package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FluxDarkColorScheme = darkColorScheme(
    primary = CyanPrimary,
    onPrimary = Color(0xFF041E26),
    primaryContainer = Color(0xFF004D5A),
    onPrimaryContainer = Color(0xFFB2F5EA),
    secondary = VioletAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF2C1959),
    onSecondaryContainer = Color(0xFFE9D8FD),
    tertiary = EmeraldSuccess,
    onTertiary = Color(0xFF003820),
    tertiaryContainer = Color(0xFF005232),
    onTertiaryContainer = Color(0xFFB9F6CA),
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorderLight,
    error = RedCritical,
    onError = Color.White
)

@Composable
fun FluxTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FluxDarkColorScheme,
        typography = Typography,
        content = content
    )
}
