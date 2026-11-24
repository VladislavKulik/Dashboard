package com.example.lifedashboard.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Typography scale from Design Spec (page 34):
 * - Screen title: 800 · 28
 * - Card title:   800 · 20
 * - Section:      800 · 17
 * - Body text:    600 · 15
 * - Caption:      500 · 13
 *
 * To enable Manrope font files, place manrope_medium.ttf, manrope_semibold.ttf,
 * manrope_bold.ttf, manrope_extrabold.ttf in res/font and replace FontFamily.SansSerif.
 */
private val DashboardFont = FontFamily.SansSerif

private fun dashboardTextStyle(
    size: TextUnit,
    weight: FontWeight,
    lineHeight: TextUnit,
    letterSpacing: TextUnit = 0.sp
) = TextStyle(
    fontFamily = DashboardFont,
    fontWeight = weight,
    fontSize = size,
    lineHeight = lineHeight,
    letterSpacing = letterSpacing,
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Proportional,
        trim = LineHeightStyle.Trim.Both
    )
)

val DashboardTypography = Typography(
    displayLarge = dashboardTextStyle(32.sp, FontWeight.ExtraBold, 36.sp, (-0.8).sp),
    headlineLarge = dashboardTextStyle(28.sp, FontWeight.ExtraBold, 32.sp, (-0.5).sp),
    headlineMedium = dashboardTextStyle(24.sp, FontWeight.ExtraBold, 28.sp, (-0.4).sp),
    titleLarge = dashboardTextStyle(20.sp, FontWeight.ExtraBold, 24.sp),
    titleMedium = dashboardTextStyle(17.sp, FontWeight.ExtraBold, 21.sp),
    titleSmall = dashboardTextStyle(15.sp, FontWeight.Bold, 19.sp),
    bodyLarge = dashboardTextStyle(15.sp, FontWeight.SemiBold, 20.sp),
    bodyMedium = dashboardTextStyle(14.sp, FontWeight.Medium, 19.sp),
    bodySmall = dashboardTextStyle(13.sp, FontWeight.Medium, 17.sp),
    labelLarge = dashboardTextStyle(14.sp, FontWeight.Bold, 18.sp),
    labelMedium = dashboardTextStyle(12.sp, FontWeight.Bold, 15.sp),
    labelSmall = dashboardTextStyle(11.sp, FontWeight.SemiBold, 14.sp)
)