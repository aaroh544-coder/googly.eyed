package com.example.bluey.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.bluey.model.Palette

private val DarkColorScheme = darkColorScheme(
    primary = Palette.Berry2,
    onPrimary = Color.White,
    primaryContainer = Palette.Berry3,
    onPrimaryContainer = Color.White,
    secondary = Palette.Berry1,
    onSecondary = Palette.Ink,
    background = Color.Black,
    onBackground = Color.White,
    surface = Palette.Panel,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF282536),
    onSurfaceVariant = Palette.InkSoft
)

@Composable
fun BlueyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
