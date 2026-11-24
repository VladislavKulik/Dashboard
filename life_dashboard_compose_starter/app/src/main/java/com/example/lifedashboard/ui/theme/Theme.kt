package com.example.lifedashboard.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun LifeDashboardTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val palette = if (darkTheme) DarkDashboardPalette else LightDashboardPalette
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = palette.Ink,
            onPrimary = palette.White,
            secondary = palette.Lavender,
            onSecondary = palette.Ink,
            tertiary = palette.Yellow,
            onTertiary = palette.Ink,
            background = palette.Background,
            onBackground = palette.Ink,
            surface = palette.White,
            onSurface = palette.Ink,
            surfaceVariant = palette.Lavender.copy(alpha = 0.32f),
            onSurfaceVariant = palette.Muted,
            outline = palette.Outline,
            error = palette.Error
        )
    } else {
        lightColorScheme(
            primary = palette.Ink,
            onPrimary = palette.White,
            secondary = palette.Lavender,
            onSecondary = palette.Ink,
            tertiary = palette.Yellow,
            onTertiary = palette.Ink,
            background = palette.Background,
            onBackground = palette.Ink,
            surface = palette.White,
            onSurface = palette.Ink,
            surfaceVariant = palette.Lavender.copy(alpha = 0.32f),
            onSurfaceVariant = palette.Muted,
            outline = palette.Outline,
            error = palette.Error
        )
    }

    CompositionLocalProvider(LocalDashboardPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = DashboardTypography,
            shapes = DashboardShapes,
            content = content
        )
    }
}

