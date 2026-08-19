package com.example.debt.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColors(
    primary = AccentColor,
    primaryVariant = AccentColor2,
    secondary = BlueColor,
    background = BgColor,
    surface = SurfaceColor,
    error = RedColor,
    onPrimary = BgColor,
    onSecondary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onError = TextPrimary
)

@Composable
fun DebtTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = DarkColorPalette,
        content = content
    )
}
