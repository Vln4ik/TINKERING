package com.tinkering.twinby.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = Purple,
    secondary = Purple2,
    background = Bg,
    surface = Surface,
)

private val LightColors = lightColorScheme()

@Composable
fun TwinbyTheme(content: @Composable () -> Unit) {
    // MVP: force dark theme to match the reference design.
    val colors = DarkColors
    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}


