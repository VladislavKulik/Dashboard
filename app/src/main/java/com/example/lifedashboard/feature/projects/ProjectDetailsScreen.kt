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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.data.model.DashboardActivityEvent
import com.example.lifedashboard.data.model.DashboardGoal
import com.example.lifedashboard.data.model.DashboardNote
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.data.model.DashboardTask
import com.example.lifedashboard.data.model.TaskPriority
import com.example.lifedashboard.data.model.TaskRecurrence
import com.example.lifedashboard.feature.tasks.AddTaskDialog
import com.example.lifedashboard.ui.components.CircleIconButton
import com.example.lifedashboard.ui.components.CircularProgressRing
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.components.ProgressTrack
import com.example.lifedashboard.ui.components.TaskRow
import com.example.lifedashboard.ui.theme.AppColors
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ProjectDetailsScreen(
    project: DashboardProject,
    tasks: List<DashboardTask>,
    onBack: () -> Unit,
    onToggleTask: (String) -> Unit,
    onOpenAllTasks: () -> Unit,
    onToggleStage: (String, String) -> Unit = { _, _ -> },
    onSaveProject: (DashboardProject, () -> Unit) -> Unit,
    onDeleteProject: (String, () -> Unit) -> Unit,
    operationError: String? = null,
    activityEvents: List<DashboardActivityEvent> = emptyList(),
    onAddTask: (String, TaskPriority, String?, LocalDate?, TaskRecurrence, Set<Int>, Boolean, String) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onOpenTaskDetails: (String) -> Unit = {},
    goals: List<DashboardGoal> = emptyList(),
    notes: List<DashboardNote> = emptyList(),
    onOpenGoal: (String) -> Unit = {},
    onOpenNote: (String) -> Unit = {},
    onAddGoal: () -> Unit = {},
    onAddNote: () -> Unit = {},
    onEditProject: (() -> Unit)? = null,
    onOpenAddTask: (() -> Unit)? = null
) {
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showEditor by remember(project.id) { mutableStateOf(false) }
    var showDeleteConfirmation by remember(project.id) { mutableStateOf(false) }
    var showAddTaskDialog by remember(project.id) { mutableStateOf(false) }

    val projectGoals = remember(project.id, goals) { goals.filter { it.projectId == project.id } }
    val projectNotes = remember(project.id, notes) { notes.filter { it.projectId == project.id } }
    val projectHistory = remember(project.id, tasks, activityEvents) {
        val taskMap = tasks.associateBy { it.id }
        activityEvents
            .filter { it.itemId in taskMap }
            .sortedByDescending { it.occurredAtEpochMillis }
            .take(5)
            .map { event -> event to taskMap[event.itemId]?.title.orEmpty() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 92.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircleIconButton(
                        icon = Icons.Outlined.ArrowBack,
                        contentDescription = "Назад",
                        onClick = onBack
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Проект",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.headlineLarge,
                        color = AppColors.Ink
                    )
                    Box {
                        CircleIconButton(
                            icon = Icons.Outlined.MoreHoriz,
                            contentDescription = "Действия",
                            onClick = { showOverflowMenu = true }
                        )
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Редактировать") },
                                onClick = {
                                    showOverflowMenu = false
                                    if (onEditProject != null) onEditProject() else showEditor = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Все задачи проекта") },
                                onClick = {
                                    showOverflowMenu = false
                                    onOpenAllTasks()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Удалить проект", color = AppColors.Error) },
                                onClick = {
                                    showOverflowMenu = false
                                    showDeleteConfirmation = true
                                }
                            )
                        }
                    }
                }
            }

            if (!operationError.isNullOrBlank()) {
                item { Text(operationError, color = AppColors.Error, style = MaterialTheme.typography.bodySmall) }
            }

            item {
                PastelCard(
                    color = Color(project.colorArgb),
                    contentPadding = PaddingValues(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(AppColors.Ink)
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Дедлайн ${project.deadlineLabel}",
                            color = AppColors.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = project.name,
                        color = AppColors.Ink,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    if (project.description.isNotBlank()) {
                        Text(project.description, color = AppColors.Muted, fontSize = 13.sp)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProgressTrack(
                            progress = project.progress,
                            modifier = Modifier.weight(1f),
                            trackColor = AppColors.Ink.copy(alpha = 0.14f),
                            progressColor = AppColors.Ink
                        )
                        Text(
                            text = "${(project.progress * 100).toInt()}%",
                            color = AppColors.Ink,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    }
                    Text(
                        text = "${project.completedTasks} из ${project.totalTasks} задач · ${projectGoals.size} цел. · ${projectNotes.size} зам.",
                        color = AppColors.Muted,
                        fontSize = 12.sp
                    )
                }
            }

            // Задачи проекта
            item {
                SectionActionHeader(
                    title = "Задачи (${tasks.size})",
                    actionLabel = if (tasks.size > 5) "Все" else null,
                    onAction = onOpenAllTasks
                )
            }
            item {
                PastelCard(
                    color = AppColors.White,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (tasks.isEmpty()) {
                        Text("Задач в проекте пока нет.", color = AppColors.Muted, fontSize = 13.sp)
                    } else {
                        tasks.take(6).forEachIndexed { index, task ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenTaskDetails(task.id) }
                            ) {
                                TaskRow(
                                    title = task.title,
                                    completed = task.isCompleted,
                                    trailing = task.dueLabel,
                                    onToggle = { onToggleTask(task.id) }
                                )
                            }
                            if (index < minOf(tasks.lastIndex, 5)) {
                                HorizontalDivider(color = AppColors.Outline.copy(alpha = 0.45f))
                            }
                        }
                    }
                }
            }

            // Цели проекта
            item {
                SectionActionHeader(
                    title = "Цели (${projectGoals.size})",
                    actionLabel = "+ Цель",
                    onAction = onAddGoal
                )
            }
            item {
                PastelCard(
                    color = AppColors.White,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (projectGoals.isEmpty()) {
                        Text("К проекту не привязаны цели.", color = AppColors.Muted, fontSize = 13.sp)
                    } else {
                        projectGoals.forEachIndexed { index, goal ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenGoal(goal.id) }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(AppColors.Pink),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Outlined.Flag, contentDescription = null, tint = AppColors.Ink, modifier = Modifier.size(17.dp))
                                }
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(goal.title, color = AppColors.Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("${goal.currentValue} / ${goal.targetValue} ${goal.unit}", color = AppColors.Muted, fontSize = 12.sp)
                                }
                                CircularProgressRing(
                                    progress = goal.progress,
                                    size = 38.dp,
                                    strokeWidth = 4.dp,
                                    labelFontSize = 10.sp
                                )
                            }
                            if (index < projectGoals.lastIndex) {
                                HorizontalDivider(color = AppColors.Outline.copy(alpha = 0.45f))
                            }
                        }
                    }
                }
            }

            // Заметки проекта
            item {
                SectionActionHeader(
                    title = "Заметки (${projectNotes.size})",
                    actionLabel = "+ Заметка",
                    onAction = onAddNote
                )
            }
            item {
                PastelCard(
                    color = AppColors.White,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (projectNotes.isEmpty()) {
                        Text("К проекту не привязаны заметки.", color = AppColors.Muted, fontSize = 13.sp)
                    } else {
                        projectNotes.forEachIndexed { index, note ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenNote(note.id) }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(AppColors.Yellow),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Outlined.Description, contentDescription = null, tint = AppColors.Ink, modifier = Modifier.size(17.dp))
                                }
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(note.title, color = AppColors.Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    if (note.body.isNotBlank()) {
                                        Text(note.body.replace('\n', ' '), color = AppColors.Muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                                Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = AppColors.Muted)
                            }
                            if (index < projectNotes.lastIndex) {
                                HorizontalDivider(color = AppColors.Outline.copy(alpha = 0.45f))
                            }
                        }
                    }
                }
            }

            item {
                Text("История", color = AppColors.Muted, style = MaterialTheme.typography.bodySmall)
            }
            item {
                PastelCard(
                    color = AppColors.White,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (projectHistory.isEmpty()) {
                        Text(
                            text = if (project.completedTasks > 0) "Завершено задач: ${project.completedTasks}" else "Активность по проекту пока не зафиксирована.",
                            color = AppColors.Muted,
                            fontSize = 13.sp
                        )
                    } else {
                        val zone = ZoneId.systemDefault()
                        val fmt = DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("ru"))
                        val today = LocalDate.now()
                        projectHistory.forEach { (event, title) ->
                            val date = Instant.ofEpochMilli(event.occurredAtEpochMillis).atZone(zone).toLocalDate()
                            val dayLabel = when (date) {
                                today -> "Сегодня"
                                today.minusDays(1) -> "Вчера"
                                else -> date.format(fmt)
                            }
                            val actionLabel = when {
                                event.type == "TASK_SKIPPED" -> "пропущена"
                                event.completed -> "выполнена"
                                else -> "возвращена"
                            }
                            Text(text = "$dayLabel · $actionLabel «$title»", color = AppColors.Ink, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        Button(
            onClick = { if (onOpenAddTask != null) onOpenAddTask() else showAddTaskDialog = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Ink, contentColor = AppColors.White),
            contentPadding = PaddingValues(vertical = 15.dp)
        ) {
            Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Добавить задачу", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            projects = listOf(project),
            initialProjectId = project.id,
            onDismiss = { showAddTaskDialog = false },
            onAdd = { title, priority, projectId, dueDate, recurrence, recurrenceDays, reminderEnabled, reminderTime ->
                onAddTask(title, priority, projectId ?: project.id, dueDate, recurrence, recurrenceDays, reminderEnabled, reminderTime)
                showAddTaskDialog = false
            }
        )
    }

    if (showEditor) {
        ProjectEditorDialog(
            initialProject = project,
            onDismiss = { showEditor = false },
            onSave = { updated -> onSaveProject(updated) { showEditor = false } },
            operationError = operationError
        )
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Удалить проект?", color = AppColors.Ink) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Проект будет удалён. Связанные задачи, цели и заметки сохранятся без привязки к проекту.", color = AppColors.Muted)
                    if (!operationError.isNullOrBlank()) Text(operationError, color = AppColors.Error)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteProject(project.id) {
                        showDeleteConfirmation = false
                        onBack()
                    }
                }) { Text("Удалить", color = AppColors.Error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirmation = false }) { Text("Отмена", color = AppColors.Muted) } },
            containerColor = AppColors.White
        )
    }
}

@Composable
private fun SectionActionHeader(
    title: String,
    actionLabel: String?,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = AppColors.Muted,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        if (actionLabel != null) {
            Text(
                text = actionLabel,
                color = AppColors.Ink,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onAction)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}