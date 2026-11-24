package com.example.lifedashboard.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.lifedashboard.data.model.DashboardThemeMode

@Composable fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, count: String? = null) {}
@Composable fun PastelCard(modifier: Modifier = Modifier, color: Color, contentPadding: PaddingValues = PaddingValues(20.dp), verticalArrangement: Any = Arrangement.spacedBy(0.dp), content: @Composable ColumnScope.() -> Unit) { object : ColumnScope {}.content() }
@Composable fun ScreenTopBar(title: String, onBackClick: (() -> Unit)? = null) {}
