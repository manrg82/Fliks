package com.fliks.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CinematicDarkColorScheme = darkColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primaryContainer,
    secondary = secondary,
    onSecondary = onSecondary,
    error = errorColor,
    onError = onError,
    background = background,
    onBackground = onBackground,
    surface = surface,
    onSurface = onSurface,
    surfaceVariant = surfaceContainer,
    onSurfaceVariant = onSurfaceVariant,
    outline = outline
)

@Composable
fun FliksTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CinematicDarkColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}