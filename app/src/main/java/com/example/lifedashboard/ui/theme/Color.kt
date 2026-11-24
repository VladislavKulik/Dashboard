package com.example.lifedashboard.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Semantic palette matching the Stage 34 Life Dashboard design specification. */
data class DashboardPalette(
    val Background: Color,
    val Ink: Color,
    val White: Color,
    val Muted: Color,
    val MutedOnDark: Color,
    val Outline: Color,
    val Yellow: Color,
    val Lavender: Color,
    val Mint: Color,
    val Pink: Color,
    val Blue: Color,
    val Purple: Color,
    val PurpleLight: Color,
    val ChartYellow: Color,
    val ChartGreen: Color,
    val Error: Color
)

internal val LightDashboardPalette = DashboardPalette(
    Background = Color(0xFFF6F5FA),
    Ink = Color(0xFF151515),
    White = Color(0xFFFFFFFF),
    Muted = Color(0xFF5C5B66),
    MutedOnDark = Color(0xFFCFCEDA),
    Outline = Color(0xFFE1DFE8),
    Yellow = Color(0xFFFFE680),
    Lavender = Color(0xFFDCCBFF),
    Mint = Color(0xFFBFEBDD),
    Pink = Color(0xFFFFC8DC),
    Blue = Color(0xFFBFE2FF),
    Purple = Color(0xFF7A55D6),
    PurpleLight = Color(0xFFB79BFF),
    ChartYellow = Color(0xFFFFB800),
    ChartGreen = Color(0xFF1FA37A),
    Error = Color(0xFFC62F4B)
)

internal val DarkDashboardPalette = DashboardPalette(
    Background = Color(0xFF15131B),
    Ink = Color(0xFFF2EEF7),
    White = Color(0xFF24212C),
    Muted = Color(0xFFB8B0C2),
    MutedOnDark = Color(0xFF8C8699),
    Outline = Color(0xFF3E394B),
    Yellow = Color(0xFF514321),
    Lavender = Color(0xFF423550),
    Mint = Color(0xFF24483C),
    Pink = Color(0xFF4D3040),
    Blue = Color(0xFF2C4359),
    Purple = Color(0xFFB69AEF),
    PurpleLight = Color(0xFFD0BBFF),
    ChartYellow = Color(0xFFFFCA45),
    ChartGreen = Color(0xFF4FD1A5),
    Error = Color(0xFFFF8A9B)
)

internal val LocalDashboardPalette = staticCompositionLocalOf { LightDashboardPalette }

/** Keeps existing AppColors call sites concise while resolving values from the active theme. */
object AppColors {
    val Background: Color @Composable get() = LocalDashboardPalette.current.Background
    val Ink: Color @Composable get() = LocalDashboardPalette.current.Ink
    val White: Color @Composable get() = LocalDashboardPalette.current.White
    val Muted: Color @Composable get() = LocalDashboardPalette.current.Muted
    val MutedOnDark: Color @Composable get() = LocalDashboardPalette.current.MutedOnDark
    val Outline: Color @Composable get() = LocalDashboardPalette.current.Outline
    val Yellow: Color @Composable get() = LocalDashboardPalette.current.Yellow
    val Lavender: Color @Composable get() = LocalDashboardPalette.current.Lavender
    val Mint: Color @Composable get() = LocalDashboardPalette.current.Mint
    val Pink: Color @Composable get() = LocalDashboardPalette.current.Pink
    val Blue: Color @Composable get() = LocalDashboardPalette.current.Blue
    val Purple: Color @Composable get() = LocalDashboardPalette.current.Purple
    val PurpleLight: Color @Composable get() = LocalDashboardPalette.current.PurpleLight
    val ChartYellow: Color @Composable get() = LocalDashboardPalette.current.ChartYellow
    val ChartGreen: Color @Composable get() = LocalDashboardPalette.current.ChartGreen
    val Error: Color @Composable get() = LocalDashboardPalette.current.Error
}