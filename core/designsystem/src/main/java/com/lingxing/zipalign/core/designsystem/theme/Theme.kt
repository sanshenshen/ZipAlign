package com.lingxing.zipalign.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors: ColorScheme = lightColorScheme(
    primary = SlateInk,
    onPrimary = CanvasIvory,
    primaryContainer = MistBlue,
    onPrimaryContainer = SlateInk,
    secondary = SignalTeal,
    onSecondary = CanvasIvory,
    secondaryContainer = SignalTeal.copy(alpha = 0.14f),
    onSecondaryContainer = SlateInk,
    tertiary = BrassAccent,
    onTertiary = SlateInk,
    background = CanvasIvory,
    onBackground = SlateInk,
    surface = Color.White.copy(alpha = 0.76f),
    onSurface = SlateInk,
    surfaceVariant = MistBlue,
    onSurfaceVariant = SlateInk.copy(alpha = 0.78f),
    outline = PanelLine,
    error = ErrorRust,
    onError = CanvasIvory,
)

private val DarkColors: ColorScheme = darkColorScheme(
    primary = NightAccent,
    onPrimary = NightBackground,
    primaryContainer = NightSurface,
    onPrimaryContainer = CanvasIvory,
    secondary = BrassAccent,
    onSecondary = NightBackground,
    secondaryContainer = SlateSurface,
    onSecondaryContainer = CanvasIvory,
    tertiary = WarningAmber,
    onTertiary = NightBackground,
    background = NightBackground,
    onBackground = CanvasIvory,
    surface = NightSurface.copy(alpha = 0.94f),
    onSurface = CanvasIvory,
    surfaceVariant = SlateSurface,
    onSurfaceVariant = CanvasIvory.copy(alpha = 0.76f),
    outline = Color.White.copy(alpha = 0.14f),
    error = Color(0xFFFF7D63),
    onError = NightBackground,
)

private val ZipAlignShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(36.dp),
)

@Composable
fun ZipAlignTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = ZipAlignTypography,
        shapes = ZipAlignShapes,
        content = content,
    )
}
