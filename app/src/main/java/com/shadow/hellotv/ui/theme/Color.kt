package com.shadow.hellotv.ui.theme

import androidx.compose.ui.graphics.Color

// ── Primary Gold Accent ──
val AccentGold = Color(0xFFFFB800)
val AccentGoldDark = Color(0xFFE5A500)
val AccentGoldLight = Color(0xFFFFD54F)
val AccentGoldSoft = Color(0x33FFB800) // 20% alpha

// ── Surfaces (Dark Theme) ──
val SurfaceDark = Color(0xFF0A0A0A)        // Deepest background
val SurfacePrimary = Color(0xFF121212)      // Main background
val SurfaceCard = Color(0xFF1A1A2E)         // Card backgrounds
val SurfaceElevated = Color(0xFF1E1E32)     // Elevated cards/panels
val SurfaceOverlay = Color(0xCC1A1A2E)      // Semi-transparent overlay
val SurfaceInput = Color(0xFF252540)        // Input field backgrounds
val SurfaceSeparator = Color(0xFF2A2A3E)    // Dividers/borders

// ── Text Colors ──
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFB0B0C0)
val TextMuted = Color(0xFF707088)
val TextDisabled = Color(0xFF505068)

// ── Status Colors ──
val StatusLive = Color(0xFFEF4444)
val StatusSuccess = Color(0xFF10B981)
val StatusWarning = Color(0xFFFBBF24)
val StatusInfo = Color(0xFF3B82F6)

// ── Gradients ──
val GradientGoldStart = Color(0xFFFFB800)
val GradientGoldEnd = Color(0xFFFF8C00)

// ── Player ──
val PlayerBackground = Color(0xFF000000)
val PlayerOverlay = Color(0x80000000)        // 50% black
val PlayerControlBg = Color(0xCC303040)      // Floating panel bg
val PlayerSeekBar = Color(0xFFFFB800)
val PlayerSeekBarBg = Color(0x40FFFFFF)

// ── TV-specific ──
val TvSurface = Color(0xFF0D0D0D)
val TvSurfaceCard = Color(0xFF1A1A1A)
val TvSeparator = Color(0xFF2A2A2A)

// ── Legacy aliases (backward compat for components not yet migrated) ──
val HotstarBlue = AccentGold
val HotstarBlueDark = AccentGoldDark
val HotstarBlueLight = AccentGoldLight
val HotstarNavy = SurfaceDark
val HotstarPink = GradientGoldEnd
val HotstarPinkLight = AccentGoldLight
val HotstarPinkSoft = AccentGoldLight
val HotstarRose = AccentGoldLight
val GradientBlueStart = GradientGoldStart
val GradientBlueEnd = GradientGoldEnd
val GradientPinkStart = GradientGoldStart
val GradientPinkEnd = GradientGoldEnd
