package com.example.lifedashboard.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.lifedashboard.ui.theme.AppColors

@Composable
fun QuickAddSheet(
    onDismiss: () -> Unit,
    onSelect: (QuickAddType) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppColors.Background,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Быстрое добавление", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
            QuickAddRow("Задача", Icons.Outlined.CheckCircle) { onSelect(QuickAddType.TASK) }
            QuickAddRow("Проект", Icons.Outlined.FolderOpen) { onSelect(QuickAddType.PROJECT) }
            QuickAddRow("Заметка", Icons.Outlined.Description) { onSelect(QuickAddType.NOTE) }
            QuickAddRow("Привычка", Icons.Outlined.Repeat) { onSelect(QuickAddType.HABIT) }
            QuickAddRow("Цель", Icons.Outlined.Flag) { onSelect(QuickAddType.GOAL) }
        }
    }
}

@Composable
private fun QuickAddRow(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = AppColors.Ink, modifier = Modifier.size(24.dp))
        Spacer(Modifier.size(16.dp))
        Text(label, style = androidx.compose.material3.MaterialTheme.typography.bodyLarge)
    }
}

enum class QuickAddType { TASK, PROJECT, NOTE, HABIT, GOAL }
