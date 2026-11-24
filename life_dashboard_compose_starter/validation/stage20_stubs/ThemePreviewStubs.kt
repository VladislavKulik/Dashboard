package com.example.lifedashboard.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AppColors { val Background: Color = Color(0xFFF7F6FB) }

@Composable
fun LifeDashboardTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) = content()
