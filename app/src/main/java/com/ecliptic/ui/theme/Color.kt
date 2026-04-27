package com.ecliptic.ui.theme

import androidx.compose.ui.graphics.Color

// Dark theme — AMOLED black
val Navy       = Color(0xFF0A0A0A)   // background (name kept for compat)
val NavyLight  = Color(0xFF2A2A2A)   // outline / border
val Teal       = Color(0xFF21AD9A)   // primary accent
val Blue       = Color(0xFF5D77E1)
val Red        = Color(0xFFD04A4E)
val White      = Color(0xFFEDEDED)   // slightly warm white — easier on AMOLED
val TextMuted  = Color(0xFF6B6B6B)
val Surface1   = Color(0xFF111111)   // cards
val Surface2   = Color(0xFF1C1C1C)   // elevated / selected

// Accent palette (both themes)
val Purple     = Color(0xFF8B5CF6)
val Orange     = Color(0xFFF97316)
val Amber      = Color(0xFFF59E0B)
val Cyan       = Color(0xFF06B6D4)
val Green      = Color(0xFF10B981)

// Light theme
val LightBg         = Color(0xFFF5F5F5)
val LightSurface    = Color(0xFFFFFFFF)
val LightSurface2   = Color(0xFFEEEEEE)
val LightOutline    = Color(0xFFDDDDDD)
val LightTextMuted  = Color(0xFF888888)

fun categoryAccentColor(category: String): Color? = when {
    category.contains("CMS", ignoreCase = true)                -> Teal
    category.contains("E-commerce", ignoreCase = true)         -> Green
    category.contains("CDN", ignoreCase = true)                -> Cyan
    category.contains("Analytics", ignoreCase = true)          -> Purple
    category.contains("Tag manager", ignoreCase = true)        -> Purple
    category.contains("JavaScript", ignoreCase = true)         -> Blue
    category.contains("Web framework", ignoreCase = true)      -> Blue
    category.contains("Mobile", ignoreCase = true)             -> Blue
    category.contains("Web server", ignoreCase = true)         -> Red
    category.contains("Operating system", ignoreCase = true)   -> Red
    category.contains("Security", ignoreCase = true)           -> Red
    category.contains("Programming", ignoreCase = true)        -> Orange
    category.contains("Database", ignoreCase = true)           -> Amber
    category.contains("Font", ignoreCase = true)               -> Amber
    category.contains("Payment", ignoreCase = true)            -> Green
    category.contains("Advertising", ignoreCase = true)        -> Orange
    category.contains("Hosting", ignoreCase = true)            -> Cyan
    category.contains("WordPress Plugin", ignoreCase = true)   -> Blue
    else                                                        -> null
}
