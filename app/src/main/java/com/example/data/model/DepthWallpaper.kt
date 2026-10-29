package com.example.data.model

import com.example.R

enum class ClockPosition {
    UPPER_CENTER,
    UPPER_LEFT,
    UPPER_RIGHT
}

enum class ClockFontStyle(val displayName: String) {
    MODERN_BOLD("Modern Bold"),
    ELEGANT_THIN("Elegant Thin"),
    ROUNDED("Luxury Rounded"),
    MONO_TECH("Cyber Monospace"),
    SERIF_CINEMA("Cinema Serif")
}

data class DepthWallpaper(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val bgDrawableRes: Int? = null,
    val fgDrawableRes: Int? = null, // Transparent PNG with alpha channel cutout!
    val customBgUri: String? = null,
    val customFgUri: String? = null,
    val defaultClockPosition: ClockPosition = ClockPosition.UPPER_CENTER,
    val defaultClockOffsetY: Int = 30, // in DP
    val defaultClockScale: Float = 1.0f,
    val defaultDimming: Float = 0.20f,
    val isCustom: Boolean = false
) {
    val isGenuineDepth: Boolean
        get() = fgDrawableRes != null || customFgUri != null
}

data class ClockCustomization(
    val fontStyle: ClockFontStyle = ClockFontStyle.MODERN_BOLD,
    val colorHex: Long = 0xFFFFFFFF,
    val sizeSp: Int = 92,
    val verticalOffsetY: Int = 36,
    val showDate: Boolean = true,
    val is24Hour: Boolean = false,
    val dimmingFactor: Float = 0.20f
)
