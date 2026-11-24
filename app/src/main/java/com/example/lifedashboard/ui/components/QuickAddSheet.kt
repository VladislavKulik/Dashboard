package com.example.lifedashboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddSheet(
    onDismiss: () -> Unit,
    onSelect: (QuickAddType) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppColors.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Что добавить?",
                style = MaterialTheme.typography.headlineMedium,
                color = AppColors.Ink,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            QuickAddRow(
                label = "Задача",
                subtitle = "Что сделать сегодня",
                icon = Icons.Outlined.CheckCircleOutline,
                iconBackground = AppColors.Blue
            ) { onSelect(QuickAddType.TASK) }

            QuickAddRow(
                label = "Заметка",
                subtitle = "Текст, список, фото",
                icon = Icons.Outlined.Description,
                iconBackground = AppColors.Yellow
            ) { onSelect(QuickAddType.NOTE) }

            QuickAddRow(
                label = "Проект",
                subtitle = "Этапы и дедлайн",
                icon = Icons.Outlined.FolderOpen,
                iconBackground = AppColors.Lavender
            ) { onSelect(QuickAddType.PROJECT) }

            QuickAddRow(
                label = "Привычка",
                subtitle = "Повторять регулярно",
                icon = Icons.Outlined.Repeat,
                iconBackground = AppColors.Mint
            ) { onSelect(QuickAddType.HABIT) }

            QuickAddRow(
                label = "Цель",
                subtitle = "Число и срок",
                icon = Icons.Outlined.Flag,
                iconBackground = AppColors.Pink
            ) { onSelect(QuickAddType.GOAL) }
        }
    }
}

@Composable
private fun QuickAddRow(
    label: String,
    subtitle: String,
    icon: ImageVector,
    iconBackground: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = AppColors.Ink, modifier = Modifier.size(21.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = AppColors.Ink)
            Text(subtitle, fontSize = 13.sp, color = AppColors.Muted)
        }
        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = AppColors.Muted)
    }
}

enum class QuickAddType { TASK, PROJECT, NOTE, HABIT, GOAL }