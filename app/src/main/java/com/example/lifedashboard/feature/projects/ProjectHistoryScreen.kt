package com.example.lifedashboard.feature.projects

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.data.model.DashboardActivityEvent
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.data.model.DashboardTask
import com.example.lifedashboard.data.model.TaskTimerSession
import com.example.lifedashboard.presentation.TaskTimerState
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.components.ScreenTopBar
import com.example.lifedashboard.ui.theme.AppColors
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class ProjectHistoryItem(
    val id: String,
    val taskId: String,
    val taskTitle: String,
    val actionLabel: String,
    val dateTimeLabel: String,
    val occurredAtMillis: Long,
    val isSkipped: Boolean,
    val isCompleted: Boolean
)

@Composable
fun ProjectHistoryScreen(
    project: DashboardProject,
    tasks: List<DashboardTask>,
    activityEvents: List<DashboardActivityEvent>,
    timerSessions: List<TaskTimerSession> = emptyList(),
    onBack: () -> Unit,
    onOpenTaskDetails: (String) -> Unit = {}
) {
    val taskMap = remember(tasks) { tasks.associateBy { it.id } }
    val zone = remember { ZoneId.systemDefault() }
    val today = remember { LocalDate.now() }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm", Locale.forLanguageTag("ru")) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("ru")) }

    val historyItems = remember(project.id, taskMap, activityEvents) {
        activityEvents
            .filter { it.itemId in taskMap }
            .sortedByDescending { it.occurredAtEpochMillis }
            .map { event ->
                val zoned = Instant.ofEpochMilli(event.occurredAtEpochMillis).atZone(zone)
                val datePart = when (zoned.toLocalDate()) {
                    today -> "Сегодня"
                    today.minusDays(1) -> "Вчера"
                    else -> zoned.format(dateFormatter)
                }
                val actionLabel = when {
                    event.type == "TASK_SKIPPED" -> "Повторение пропущено"
                    event.completed -> "Задача выполнена"
                    else -> "Возвращена в работу"
                }
                ProjectHistoryItem(
                    id = event.id,
                    taskId = event.itemId,
                    taskTitle = taskMap[event.itemId]?.title ?: "Задача",
                    actionLabel = actionLabel,
                    dateTimeLabel = "$datePart, ${zoned.format(timeFormatter)}",
                    occurredAtMillis = event.occurredAtEpochMillis,
                    isSkipped = event.type == "TASK_SKIPPED",
                    isCompleted = event.completed
                )
            }
    }

    val projectSessions = remember(project.id, taskMap, timerSessions) {
        timerSessions
            .filter { it.projectId == project.id || it.taskId in taskMap }
            .sortedByDescending { it.endedAtMillis }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ScreenTopBar(
                title = "История изменений",
                onBackClick = onBack
            )
        }

        item {
            PastelCard(
                color = Color(project.colorArgb),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(18.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = AppColors.Ink
                )
                Text(
                    text = "Выполнено ${project.completedTasks} из ${project.totalTasks} задач · событий: ${historyItems.size}",
                    color = AppColors.Muted,
                    fontSize = 13.sp
                )
            }
        }

        if (historyItems.isEmpty() && projectSessions.isEmpty()) {
            item {
                PastelCard(
                    color = AppColors.White,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(22.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = null,
                        tint = AppColors.Muted,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "История пока пуста",
                        style = MaterialTheme.typography.titleLarge,
                        color = AppColors.Ink
                    )
                    Text(
                        text = "Здесь будут отображаться все изменения статусов задач и фокус-сессии таймера по этому проекту.",
                        color = AppColors.Muted,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            if (historyItems.isNotEmpty()) {
                item {
                    Text(
                        text = "Изменения задач (${historyItems.size})",
                        color = AppColors.Muted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                items(historyItems, key = { it.id }) { item ->
                    PastelCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenTaskDetails(item.taskId) },
                        color = AppColors.White,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            item.isSkipped -> AppColors.Pink
                                            item.isCompleted -> AppColors.Mint
                                            else -> AppColors.Yellow
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (item.isSkipped) Icons.Outlined.Repeat else Icons.Outlined.CheckCircleOutline,
                                    contentDescription = null,
                                    tint = AppColors.Ink,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = item.taskTitle,
                                    color = AppColors.Ink,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = item.actionLabel,
                                    color = AppColors.Muted,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = item.dateTimeLabel,
                                color = AppColors.Muted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            if (projectSessions.isNotEmpty()) {
                item {
                    Text(
                        text = "Сессии таймера (${projectSessions.size})",
                        color = AppColors.Muted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                item {
                    PastelCard(
                        color = AppColors.White,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        projectSessions.forEachIndexed { index, session ->
                            val taskTitle = taskMap[session.taskId]?.title ?: "Задача проекта"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenTaskDetails(session.taskId) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AccessTime,
                                    contentDescription = null,
                                    tint = AppColors.Muted,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = taskTitle,
                                        color = AppColors.Ink,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = session.formattedRange(),
                                        color = AppColors.Muted,
                                        fontSize = 12.sp
                                    )
                                }
                                Text(
                                    text = TaskTimerState.formatShort(session.durationSeconds),
                                    color = AppColors.Ink,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            if (index < projectSessions.lastIndex) {
                                HorizontalDivider(color = AppColors.Outline.copy(alpha = 0.45f))
                            }
                        }
                    }
                }
            }
        }
    }
}