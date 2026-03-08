package com.shadow.hellotv.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HotstarDarkScheme = darkColorScheme(
    primary = HotstarBlue,
    onPrimary = Color.White,
    primaryContainer = HotstarBlueDark,
    onPrimaryContainer = HotstarBlueLight,
    secondary = HotstarPink,
    onSecondary = Color.White,
    secondaryContainer = HotstarPink.copy(alpha = 0.3f),
    onSecondaryContainer = HotstarPinkLight,
    tertiary = HotstarPinkLight,
    onTertiary = Color.Black,
    background = SurfaceDark,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = TextMuted,
    outlineVariant = TextDisabled,
    error = StatusLive,
    onError = Color.White
)

private val HotstarLightScheme = lightColorScheme(
    primary = HotstarBlue,
    onPrimary = Color.White,
    secondary = HotstarPink,
    onSecondary = Color.White,
    tertiary = HotstarPinkLight,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF1E293B),
    surface = Color.White,
    onSurface = Color(0xFF1E293B)
)

@Composable
fun HelloTVTheme(
    darkTheme: Boolean = true, // Always dark for TV/streaming app
    dynamicColor: Boolean = false, // Disable dynamic colors to keep Hotstar theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) HotstarDarkScheme else HotstarLightScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
