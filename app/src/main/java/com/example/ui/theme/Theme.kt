package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val GunboundDarkScheme = darkColorScheme(
    primary = GbCyanPrimary,
    onPrimary = GbNavyDeep,
    primaryContainer = GbSlatePanel,
    onPrimaryContainer = GbTextPrimary,
    secondary = GbGoldAccent,
    onSecondary = GbNavyDeep,
    secondaryContainer = GbNavySurface,
    onSecondaryContainer = GbGoldAccent,
    tertiary = GbEmeraldHp,
    onTertiary = GbNavyDeep,
    background = GbNavyDeep,
    onBackground = GbTextPrimary,
    surface = GbNavySurface,
    onSurface = GbTextPrimary,
    surfaceVariant = GbSlatePanel,
    onSurfaceVariant = GbTextSecondary,
    outline = GbSteelBorder,
    error = GbCrimsonDanger,
    onError = GbTextPrimary
)

val GunboundShapes = Shapes(
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(10.dp),
    large = RoundedCornerShape(14.dp),
    extraLarge = RoundedCornerShape(20.dp)
)

@Composable
fun GunboundTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GunboundDarkScheme,
        typography = GunboundTypography,
        shapes = GunboundShapes,
        content = content
    )
}
