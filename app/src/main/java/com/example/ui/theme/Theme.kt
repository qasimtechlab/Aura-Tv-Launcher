package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AuraTvColorScheme = darkColorScheme(
    primary = AppleBlue,
    onPrimary = TextPrimary,
    secondary = AppleCyan,
    onSecondary = TextPrimary,
    tertiary = AppleGold,
    background = TvBackground,
    onBackground = TextPrimary,
    surface = TvSurface,
    onSurface = TextPrimary,
    surfaceVariant = TvSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
)

@Composable
fun AuraTvTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AuraTvColorScheme,
        typography = Typography,
        content = content
    )
}
