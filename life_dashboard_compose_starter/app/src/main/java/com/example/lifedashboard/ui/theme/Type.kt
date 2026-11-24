package com.example.lifedashboard.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

/**
 * The HTML mockups use Manrope (weights 500–800).
 * FontFamily.SansSerif is a compile-ready fallback. See README.md for replacing it
 * with the actual Manrope TTF/OTF files in res/font.
 */
private val DashboardFont = FontFamily.SansSerif

private fun dashboardTextStyle(
    size: androidx.compose.ui.unit.TextUnit,
    weight: FontWeight,
    lineHeight: androidx.compose.ui.unit.TextUnit,
    letterSpacing: androidx.compose.ui.unit.TextUnit = 0.sp
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
    displayLarge = dashboardTextStyle(36.sp, FontWeight.ExtraBold, 38.sp, (-1).sp),
    headlineLarge = dashboardTextStyle(36.sp, FontWeight.ExtraBold, 38.sp, (-1).sp),
    headlineMedium = dashboardTextStyle(28.sp, FontWeight.ExtraBold, 31.sp, (-0.5).sp),
    titleLarge = dashboardTextStyle(20.sp, FontWeight.ExtraBold, 23.sp),
    titleMedium = dashboardTextStyle(16.sp, FontWeight.Bold, 20.sp),
    titleSmall = dashboardTextStyle(14.sp, FontWeight.Bold, 18.sp),
    bodyLarge = dashboardTextStyle(15.sp, FontWeight.SemiBold, 20.sp),
    bodyMedium = dashboardTextStyle(14.sp, FontWeight.Medium, 19.sp),
    bodySmall = dashboardTextStyle(13.sp, FontWeight.Medium, 17.sp),
    labelLarge = dashboardTextStyle(14.sp, FontWeight.Bold, 18.sp),
    labelMedium = dashboardTextStyle(12.sp, FontWeight.Bold, 15.sp),
    labelSmall = dashboardTextStyle(11.sp, FontWeight.SemiBold, 14.sp)
)
