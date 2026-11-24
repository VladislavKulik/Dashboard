package com.example.lifedashboard.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import com.example.lifedashboard.data.model.DashboardTask
import com.example.lifedashboard.data.model.DashboardActivityEvent
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.example.lifedashboard.data.model.TaskDayCategory
import com.example.lifedashboard.ui.components.CircleIconButton
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.components.TaskRow
import com.example.lifedashboard.ui.theme.AppColors

@Composable
fun HomeScreen(
    tasks: List<DashboardTask>,
    activeProjectCount: Int,
    onToggleTask: (String) -> Unit,
    onOpenTasks: () -> Unit,
    onOpenProjects: () -> Unit,
    onOpenNotes: () -> Unit = {},
    onOpenHabits: () -> Unit = {},
    showCompletedTasks: Boolean = true,
    activityEvents: List<DashboardActivityEvent> = emptyList(),
    onOpenCalendar: () -> Unit = {}
) {
    val today = LocalDate.now()
    val todayTasks = tasks.filter { it.dueDate == today || (it.dueDate == null && it.dayCategory == TaskDayCategory.TODAY) }
    val taskPreview = todayTasks.filter { it.showOnHome && (showCompletedTasks || !it.isCompleted) }.take(3)
    val completedToday = todayTasks.count { it.isCompleted }
    val completionPercent = if (todayTasks.isEmpty()) 0 else (completedToday * 100 / todayTasks.size)
    val dateLabel = today.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.forLanguageTag("ru")))
    val weekEvents = activityEvents.filter { event ->
        val date = Instant.ofEpochMilli(event.occurredAtEpochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        !date.isBefore(today.minusDays(6)) && !date.isAfter(today)
    }
    val recentEvents = activityEvents.sortedByDescending { it.occurredAtEpochMillis }.take(2)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleIconButton(
                    icon = Icons.Outlined.PersonOutline,
                    contentDescription = "Профиль",
                    onClick = { }
                )
                CircleIconButton(
                    icon = Icons.Outlined.NotificationsNone,
                    contentDescription = "Уведомления",
                    onClick = { }
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = dateLabel.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.forLanguageTag("ru")) else it.toString() },
                    color = AppColors.Muted,
                    fontSize = 13.sp
                )
                Text(
                    text = "Твой день\nв одном месте",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
                    color = AppColors.Ink
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatPill("Задач", todayTasks.size.toString(), AppColors.Pink, onClick = onOpenTasks)
                StatPill("Проектов", activeProjectCount.toString(), AppColors.Blue, onClick = onOpenProjects)
                StatPill("Готово", "$completionPercent%", AppColors.Mint, onClick = onOpenTasks)
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Быстрый доступ", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, color = AppColors.Ink)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeShortcut(
                        title = "Задачи",
                        icon = Icons.Outlined.CheckCircleOutline,
                        color = AppColors.Pink,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenTasks
                    )
                    HomeShortcut(
                        title = "Заметки",
                        icon = Icons.Outlined.Description,
                        color = AppColors.Lavender,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenNotes
                    )
                    HomeShortcut(
                        title = "Привычки",
                        icon = Icons.Outlined.Repeat,
                        color = AppColors.Mint,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenHabits
                    )
                }
            }
        }

        item {
            PastelCard(color = AppColors.Yellow) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Задачи на сегодня", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
                    SmallBadge("$completedToday из ${todayTasks.size}")
                }
                if (taskPreview.isEmpty()) {
                    Text("На сегодня задач нет", color = AppColors.Muted)
                } else {
                    taskPreview.forEach { task ->
                        TaskRow(
                            title = task.title,
                            completed = task.isCompleted,
                            trailing = task.trailingLabel,
                            onToggle = { onToggleTask(task.id) }
                        )
                    }
                }
                if (todayTasks.size > taskPreview.size) {
                    Text(
                        text = "Ещё ${todayTasks.size - taskPreview.size} задачи",
                        color = AppColors.Muted,
                        fontSize = 12.sp,
                        modifier = Modifier.clickable(onClick = onOpenTasks)
                    )
                }
            }
        }

        item {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                if (maxWidth < 340.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        WeekActivityCard(weekEvents, Modifier.fillMaxWidth(), onOpenCalendar)
                        EventsCard(recentEvents, Modifier.fillMaxWidth(), onOpenCalendar)
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        WeekActivityCard(weekEvents, Modifier.weight(1f), onOpenCalendar)
                        EventsCard(recentEvents, Modifier.weight(1f), onOpenCalendar)
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeShortcut(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
            .background(color)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
        horizontalAlignment = Alignment.Start
    ) {
        androidx.compose.material3.Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppColors.Ink,
            modifier = Modifier.size(22.dp)
        )
        Text(title, color = AppColors.Ink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
private fun StatPill(label: String, count: String, color: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick)
            .padding(start = 20.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.Ink)
        Spacer(Modifier.width(8.dp))
        SmallBadge(count)
    }
}

@Composable
private fun SmallBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(AppColors.White.copy(alpha = 0.78f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = AppColors.Ink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun WeekActivityCard(
    events: List<DashboardActivityEvent>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val today = LocalDate.now()
    val days = (6 downTo 0).map { offset -> today.minusDays(offset.toLong()) }
    val counts = days.map { day ->
        events.count { event ->
            Instant.ofEpochMilli(event.occurredAtEpochMillis).atZone(ZoneId.systemDefault()).toLocalDate() == day
        }
    }
    val maxCount = counts.maxOrNull()?.coerceAtLeast(1) ?: 1

    PastelCard(
        modifier = modifier.height(190.dp).clickable(onClick = onClick),
        color = AppColors.Lavender,
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Активность", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
            Text("7 дней", color = AppColors.Muted, fontSize = 11.sp)
        }
        Row(
            modifier = Modifier.fillMaxWidth().height(100.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            counts.forEachIndexed { index, count ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.62f)
                            .height((12 + 62f * count / maxCount).dp)
                            .clip(CircleShape)
                            .background(if (index == 6) AppColors.Ink else AppColors.Purple.copy(alpha = 0.25f))
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(days[index].dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.forLanguageTag("ru")), fontSize = 9.sp, color = AppColors.Muted)
                }
            }
        }
    }
}

@Composable
private fun EventsCard(
    events: List<DashboardActivityEvent>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    PastelCard(
        modifier = modifier.height(190.dp).clickable(onClick = onClick),
        color = AppColors.Ink,
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Последнее", style = androidx.compose.material3.MaterialTheme.typography.titleLarge, color = AppColors.White)
            Text("Все", color = AppColors.MutedOnDark, fontSize = 11.sp)
        }
        if (events.isEmpty()) {
            Text("Здесь появится твоя активность", color = AppColors.MutedOnDark, fontSize = 12.sp)
        } else {
            events.forEach { event ->
                val time = Instant.ofEpochMilli(event.occurredAtEpochMillis)
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("HH:mm"))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        if (event.completed) "Задача выполнена" else "Задача возвращена в работу",
                        color = AppColors.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(time, color = AppColors.MutedOnDark, fontSize = 11.sp)
                }
            }
        }
    }
}
