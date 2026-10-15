package io.github.reyfxck.wpsxandroid.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val WpsxDarkColors = darkColorScheme(
    primary = Color(0xFFCDB8FF),
    onPrimary = Color(0xFF2D1462),
    primaryContainer = Color(0xFF4B2589),
    onPrimaryContainer = Color(0xFFE9DDFF),
    secondary = Color(0xFFD5C2FF),
    secondaryContainer = Color(0xFF3C3154),
    onSecondaryContainer = Color(0xFFEDE4FF),
    tertiary = Color(0xFFB8C4FF),
    surface = Color(0xFF121016),
    surfaceContainer = Color(0xFF211E27),
    surfaceContainerHigh = Color(0xFF2C2833),
    surfaceContainerHighest = Color(0xFF37323E),
)

private val WpsxShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(30.dp),
    extraLarge = RoundedCornerShape(38.dp),
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WpsxTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) WpsxDarkColors else expressiveLightColorScheme()
    MaterialExpressiveTheme(
        colorScheme = colors,
        shapes = WpsxShapes,
        content = content,
    )
}
