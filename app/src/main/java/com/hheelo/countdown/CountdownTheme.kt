package com.hheelo.countdown

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
internal fun CountdownTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = countdownColor(CountdownColorHex.Brand),
            secondary = countdownColor(CountdownColorHex.AccentGreen),
            surface = Color.White,
            background = countdownColor(CountdownColorHex.BackgroundStart)
        ),
        content = content
    )
}
