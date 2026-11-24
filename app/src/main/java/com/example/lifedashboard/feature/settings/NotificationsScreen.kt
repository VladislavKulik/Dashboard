package com.example.lifedashboard.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.data.DateInput
import com.example.lifedashboard.data.DateKeys
import com.example.lifedashboard.presentation.DashboardUiState
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.components.ScreenTopBar
import com.example.lifedashboard.ui.theme.AppColors
import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class NotificationTargetType { TASK, HABIT, PROJECT, GOAL }

data class NotificationFeedItem(
    val id: String,
    val type: NotificationTargetType,
    val targetId: String,
    val title: String,
    val subtitle: String,
    val isToday: Boolean
)

@Composable
fun NotificationsScreen(
    state: DashboardUiState,
    onBack: () -> Unit,
    onOpenItem: (NotificationTargetType, String) -> Unit
) {
    var readIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    val feed = remember(state) { buildNotificationFeed(state) }
    val todayItems = remember(feed) { feed.filter { it.isToday } }
    val upcomingItems = remember(feed) { feed.filterNot { it.isToday } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ScreenTopBar(
                title = "Уведомления",
                actionIcon = Icons.Outlined.Check,
                actionDescription = "Отметить прочитанными",
                onActionClick = { readIds = feed.map { it.id }.toSet() },
                onBackClick = onBack
            )
        }

        if (feed.isEmpty()) {
            item {
                PastelCard(color = AppColors.White, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier.size(44.dp).clip(CircleShape).background(AppColors.Mint),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.NotificationsNone, contentDescription = null, tint = AppColors.Ink)
                        }
                        Column {
                            Text("Всё выполнено", style = MaterialTheme.typography.titleMedium, color = AppColors.Ink)
                            Text("Срочных уведомлений и дедлайнов нет.", color = AppColors.Muted, fontSize = 13.sp)
                        }
                    }
                }
            }
        } else {
            if (todayItems.isNotEmpty()) {
                item { Text("Сегодня", color = AppColors.Muted, fontSize = 13.sp, fontWeight = FontWeight.Medium) }
                item {
                    NotificationGroupCard(
                        items = todayItems,
                        readIds = readIds,
                        onClick = { item ->
                            readIds = readIds + item.id
                            onOpenItem(item.type, item.targetId)
                        }
                    )
                }
            }

            if (upcomingItems.isNotEmpty()) {
                item { Text("Скоро и просрочено", color = AppColors.Muted, fontSize = 13.sp, fontWeight = FontWeight.Medium) }
                item {
                    NotificationGroupCard(
                        items = upcomingItems,
                        readIds = readIds,
                        onClick = { item ->
                            readIds = readIds + item.id
                            onOpenItem(item.type, item.targetId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationGroupCard(
    items: List<NotificationFeedItem>,
    readIds: Set<String>,
    onClick: (NotificationFeedItem) -> Unit
) {
    PastelCard(
        color = AppColors.White,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.forEachIndexed { index, item ->
            val unread = item.id !in readIds
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick(item) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(notificationColor(item.type)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(notificationIcon(item.type), contentDescription = null, tint = AppColors.Ink, modifier = Modifier.size(19.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = item.title,
                        color = AppColors.Ink,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(item.subtitle, color = AppColors.Muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (unread) {
                    Spacer(Modifier.width(8.dp))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AppColors.Purple))
                }
            }
            if (index < items.lastIndex) {
                HorizontalDivider(color = AppColors.Outline.copy(alpha = 0.45f))
            }
        }
    }
}

private fun buildNotificationFeed(state: DashboardUiState): List<NotificationFeedItem> {
    val today = LocalDate.now()
    val todayKey = DateKeys.today()
    val items = mutableListOf<NotificationFeedItem>()

    state.tasks.filter { !it.isCompleted && (it.dueDate == today || it.reminderEnabled) }.forEach { task ->
        if (task.dueDate == today || task.dueDate?.isBefore(today) == true) {
            val subtitle = buildString {
                if (task.dueDate?.isBefore(today) == true) append("Просрочено")
                else append("На сегодня")
                if (task.reminderEnabled) append(" · ${task.reminderTime}")
            }
            items += NotificationFeedItem(
                id = "task-${task.id}",
                type = NotificationTargetType.TASK,
                targetId = task.id,
                title = task.title,
                subtitle = subtitle,
                isToday = task.dueDate == today
            )
        }
    }

    state.habits.filter { todayKey !in it.completionDates }.forEach { habit ->
        val subtitle = if (habit.reminderEnabled) {
            "Привычка ещё не отмечена · ${habit.reminderTime}"
        } else {
            "Привычка ещё не отмечена сегодня"
        }
        items += NotificationFeedItem(
            id = "habit-${habit.id}",
            type = NotificationTargetType.HABIT,
            targetId = habit.id,
            title = habit.title,
            subtitle = subtitle,
            isToday = true
        )
    }

    state.projects.filter { !it.isCompleted && it.deadlineDate != null }.forEach { project ->
        val days = ChronoUnit.DAYS.between(today, project.deadlineDate)
        if (days <= 7) {
            val subtitle = when {
                days < 0 -> "Дедлайн просрочен на ${-days} дн."
                days == 0L -> "Дедлайн сегодня"
                else -> "До дедлайна $days дн."
            }
            items += NotificationFeedItem(
                id = "project-${project.id}",
                type = NotificationTargetType.PROJECT,
                targetId = project.id,
                title = project.name,
                subtitle = subtitle,
                isToday = days == 0L
            )
        }
    }

    state.goals.filter { !it.isCompleted && it.dueDate != null }.forEach { goal ->
        val days = ChronoUnit.DAYS.between(today, goal.dueDate)
        if (days <= 14) {
            val left = (goal.targetValue - goal.currentValue).coerceAtLeast(0)
            val dateText = DateInput.formatShort(goal.dueDate)
            items += NotificationFeedItem(
                id = "goal-${goal.id}",
                type = NotificationTargetType.GOAL,
                targetId = goal.id,
                title = goal.title,
                subtitle = "Осталось $left ${goal.unit}, срок $dateText",
                isToday = days == 0L
            )
        }
    }

    return items
}

@Composable
private fun notificationColor(type: NotificationTargetType): Color = when (type) {
    NotificationTargetType.TASK -> AppColors.Blue
    NotificationTargetType.HABIT -> AppColors.Mint
    NotificationTargetType.PROJECT -> AppColors.Lavender
    NotificationTargetType.GOAL -> AppColors.Pink
}

private fun notificationIcon(type: NotificationTargetType): ImageVector = when (type) {
    NotificationTargetType.TASK -> Icons.Outlined.CheckCircleOutline
    NotificationTargetType.HABIT -> Icons.Outlined.Repeat
    NotificationTargetType.PROJECT -> Icons.Outlined.FolderOpen
    NotificationTargetType.GOAL -> Icons.Outlined.Flag
}