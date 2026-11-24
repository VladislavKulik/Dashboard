package com.example.lifedashboard.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.lifedashboard.ui.theme.AppColors

@Composable
fun ScreenTopBar(
    title: String,
    actionIcon: ImageVector? = null,
    actionDescription: String = "Действие",
    onActionClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBackClick != null) {
            CircleIconButton(
                icon = androidx.compose.material.icons.Icons.Outlined.ArrowBack,
                contentDescription = "Назад",
                onClick = onBackClick
            )
            Spacer(Modifier.width(12.dp))
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = AppColors.Ink,
            style = androidx.compose.material3.MaterialTheme.typography.headlineLarge
        )
        if (actionIcon != null && onActionClick != null) {
            Spacer(Modifier.size(12.dp))
            CircleIconButton(
                icon = actionIcon,
                contentDescription = actionDescription,
                onClick = onActionClick
            )
        }
    }
}
