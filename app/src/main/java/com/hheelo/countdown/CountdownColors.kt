package com.hheelo.countdown

import androidx.compose.ui.graphics.Color

object CountdownColorHex {
    const val Brand = "#FF6B4A"
    const val AccentGreen = "#10B981"
    const val TextPrimary = "#111827"
    const val TextSecondary = "#6B7280"
    const val Danger = "#DC2626"
    const val Success = "#047857"
    const val BackgroundStart = "#FFF7ED"
    const val BackgroundMiddle = "#FFEDD5"
    const val BackgroundEnd = "#DCFCE7"
    const val Weekend = "#FDBA74"
    const val Holiday = "#34D399"
    const val WidgetBackground = "#111827"
    const val WidgetSmallSubtitle = "#B8FFFFFF"
    const val WidgetCardBackground = "#14FFFFFF"
    const val WidgetTitle = "#E6FFFFFF"
    const val WidgetSubtitle = "#9EFFFFFF"

    val eventSwatches = listOf(Brand, "#F59E0B", AccentGreen, "#3B82F6", "#8B5CF6")
}

fun countdownColor(hex: String): Color {
    return Color(android.graphics.Color.parseColor(hex))
}
