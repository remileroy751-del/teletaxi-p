package com.telotaxi.planner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = TaxiBlue,
    onPrimary = Color.White,
    secondary = TaxiYellow,
    onSecondary = Color.Black,
    background = BackgroundLight,
    surface = SurfaceLight,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = DangerRed
)

private val DarkColors = darkColorScheme(
    primary = TaxiYellow,
    onPrimary = Color.Black,
    secondary = TaxiBlue,
    onSecondary = Color.White,
    background = Color(0xFF121417),
    surface = Color(0xFF1C1F23),
    onBackground = Color(0xFFE3E3E3),
    onSurface = Color(0xFFE3E3E3),
    error = Color(0xFFEF9A9A)
)

@Composable
fun TeleTaxiPlannerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
