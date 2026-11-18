package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val OutpostDarkScheme = darkColorScheme(
    primary = CyanFloor,
    onPrimary = Color(0xFF03151C),
    primaryContainer = Color(0xFF0F2D3A),
    onPrimaryContainer = BoneWhite,
    secondary = AmberSignal,
    onSecondary = Color(0xFF1A0F00),
    secondaryContainer = BunkerSurfaceVariant,
    onSecondaryContainer = AmberBright,
    tertiary = EmeraldHud,
    onTertiary = Color(0xFF031F10),
    background = BunkerObsidian,
    onBackground = BoneWhite,
    surface = BunkerPanel,
    onSurface = BoneWhite,
    surfaceVariant = BunkerSurface,
    onSurfaceVariant = SlateDim,
    outline = OliveBorder,
    error = CrimsonAlert,
    onError = Color.White
)

// Crisp tactical instrumentation corners (Option-3-inspired sleek frames)
val TacticalShapes = Shapes(
    extraSmall = RoundedCornerShape(3.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(6.dp),
    large = RoundedCornerShape(6.dp),
    extraLarge = RoundedCornerShape(8.dp)
)

@Composable
fun OutpostZeroTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = OutpostDarkScheme,
        typography = OutpostTypography,
        shapes = TacticalShapes,
        content = content
    )
}
