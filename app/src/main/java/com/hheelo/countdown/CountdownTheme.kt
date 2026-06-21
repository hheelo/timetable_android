package com.hheelo.countdown

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class CountdownExtraColors(
    val textPrimary: Color,
    val textSecondary: Color,
    val backgroundStart: Color,
    val backgroundMiddle: Color,
    val backgroundEnd: Color,
    val cardSurface: Color,
    val iconBubble: Color,
    val success: Color,
    val swatchBorder: Color
)

val LocalCountdownColors = staticCompositionLocalOf {
    CountdownExtraColors(
        textPrimary = Color.Unspecified,
        textSecondary = Color.Unspecified,
        backgroundStart = Color.Unspecified,
        backgroundMiddle = Color.Unspecified,
        backgroundEnd = Color.Unspecified,
        cardSurface = Color.Unspecified,
        iconBubble = Color.Unspecified,
        success = Color.Unspecified,
        swatchBorder = Color.Unspecified
    )
}

private val LightExtraColors = CountdownExtraColors(
    textPrimary = countdownColor(CountdownColorHex.TextPrimary),
    textSecondary = countdownColor(CountdownColorHex.TextSecondary),
    backgroundStart = countdownColor(CountdownColorHex.BackgroundStart),
    backgroundMiddle = countdownColor(CountdownColorHex.BackgroundMiddle),
    backgroundEnd = countdownColor(CountdownColorHex.BackgroundEnd),
    cardSurface = Color.White.copy(alpha = 0.78f),
    iconBubble = Color.White.copy(alpha = 0.85f),
    success = countdownColor(CountdownColorHex.Success),
    swatchBorder = Color.White
)

private val DarkExtraColors = CountdownExtraColors(
    textPrimary = countdownColor(CountdownColorHex.DarkTextPrimary),
    textSecondary = countdownColor(CountdownColorHex.DarkTextSecondary),
    backgroundStart = countdownColor(CountdownColorHex.DarkBackgroundStart),
    backgroundMiddle = countdownColor(CountdownColorHex.DarkBackgroundMiddle),
    backgroundEnd = countdownColor(CountdownColorHex.DarkBackgroundEnd),
    cardSurface = Color.White.copy(alpha = 0.08f),
    iconBubble = Color.White.copy(alpha = 0.12f),
    success = countdownColor(CountdownColorHex.DarkSuccess),
    swatchBorder = Color.White.copy(alpha = 0.7f)
)

private val LightColorScheme = lightColorScheme(
    primary = countdownColor(CountdownColorHex.Brand),
    secondary = countdownColor(CountdownColorHex.AccentGreen),
    surface = Color.White,
    background = countdownColor(CountdownColorHex.BackgroundStart)
)

private val DarkColorScheme = darkColorScheme(
    primary = countdownColor(CountdownColorHex.Brand),
    secondary = countdownColor(CountdownColorHex.AccentGreen),
    surface = Color(0xFF1E1E1E),
    background = countdownColor(CountdownColorHex.DarkBackgroundStart)
)

@Composable
internal fun CountdownTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extraColors = if (darkTheme) DarkExtraColors else LightExtraColors

    CompositionLocalProvider(LocalCountdownColors provides extraColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
