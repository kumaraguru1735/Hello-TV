package com.shadow.hellotv.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GoldDarkScheme = darkColorScheme(
    primary = AccentGold,
    onPrimary = Color.Black,
    primaryContainer = AccentGoldDark,
    onPrimaryContainer = AccentGoldLight,
    secondary = AccentGoldLight,
    onSecondary = Color.Black,
    secondaryContainer = AccentGoldSoft,
    onSecondaryContainer = AccentGoldLight,
    tertiary = GradientGoldEnd,
    onTertiary = Color.Black,
    background = SurfaceDark,
    onBackground = TextPrimary,
    surface = SurfacePrimary,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = TextMuted,
    outlineVariant = TextDisabled,
    error = StatusLive,
    onError = Color.White
)

@Composable
fun HelloTVTheme(
    darkTheme: Boolean = true, // Always dark for TV/streaming app
    dynamicColor: Boolean = false, // Disable dynamic colors to keep gold theme
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GoldDarkScheme,
        typography = Typography,
        content = content
    )
}
