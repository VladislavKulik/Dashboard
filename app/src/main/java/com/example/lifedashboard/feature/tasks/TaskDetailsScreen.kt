package com.example.lifedashboard.feature.tasks

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.data.DateInput
import com.example.lifedashboard.data.model.DashboardActivityEvent
import com.example.lifedashboard.data.model.DashboardGoal
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.data.model.DashboardTask
import com.example.lifedashboard.data.model.TaskRecurrence
import com.example.lifedashboard.data.model.TaskSubtask
import com.example.lifedashboard.presentation.TaskTimerState
import com.example.lifedashboard.ui.components.CircleIconButton
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.theme.AppColors
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TaskDetailsScreen(
    task: DashboardTask,
    allTasks: List<DashboardTask>,
    projects: List<DashboardProject>,
    goals: List<DashboardGoal>,
    activityEvents: List<DashboardActivityEvent>,
    onBack: () -> Unit,
    onToggleTask: (String) -> Unit,
    onUpdateTask: (DashboardTask, () -> Unit) -> Unit,
    onDeleteTask: (String, () -> Unit) -> Unit,
    onOpenProject: (String) -> Unit,
    onOpenGoal: (String) -> Unit,
    onOpenTask: (String) -> Unit,
    operationError: String? = null,
    timerState: TaskTimerState = TaskTimerState(),
    onStartTimer: (DashboardTask, String?) -> Unit = { _, _ -> },
    subtasks: List<TaskSubtask> = emptyList(),
    onAddSubtask: (String, String) -> Unit = { _, _ -> },
    onToggleSubtask: (String) -> Unit = {},
    onDeleteSubtask: (String) -> Unit = {},
    onEditTask: (() -> Unit)? = null
) {
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showEditDialog by rememberSaveable(task.id) { mutableStateOf(false) }
    var showDeleteConfirmation by rememberSaveable(task.id) { mutableStateOf(false) }
    var newSubtaskTitle by rememberSaveable(task.id) { mutableStateOf("") }

    val linkedProject = remember(task.projectId, projects) {
        projects.firstOrNull { it.id == task.projectId }
    }
    val relatedGoals = remember(task.projectId, goals) {
        if (task.projectId == null) emptyList() else goals.filter { it.projectId == task.projectId }
    }
    val taskSubtasks = remember(task.id, subtasks) {
        subtasks.filter { it.taskId == task.id }
    }
    val seriesOccurrences = remember(task.id, task.recurrenceSeriesId, allTasks) {
        val seriesId = task.recurrenceSeriesId ?: return@remember emptyList()
        allTasks.filter { it.id != task.id && it.recurrenceSeriesId == seriesId }
            .sortedByDescending { it.dueDate ?: LocalDate.MIN }
            .take(5)
    }
    val taskHistory = remember(task.id, activityEvents) {
        activityEvents.filter { it.itemId == task.id }
            .sortedByDescending { it.occurredAtEpochMillis }
    }
    val taskSessions = remember(task.id, timerState.sessions) {
        timerState.sessions.filter { it.taskId == task.id }
    }
    val totalTaskSeconds = timerState.totalSecondsForTask(task.id)
    val plannedSeconds = task.plannedMinutes.coerceIn(5, 24 * 60) * 60L
    val isTimerActiveHere = timerState.activeTaskId == task.id

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
                        text = "Задача",
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
                                    if (onEditTask != null) onEditTask() else showEditDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (task.isCompleted) "Вернуть в работу" else "Отметить выполненной") },
                                onClick = {
                                    showOverflowMenu = false
                                    onToggleTask(task.id)
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (task.recurrence != TaskRecurrence.NONE && !task.isCompleted) "Пропустить повторение" else "Удалить задачу",
                                        color = AppColors.Error
                                    )
                                },
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
                    color = if (task.isCompleted) AppColors.Mint else AppColors.Yellow,
                    contentPadding = PaddingValues(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(AppColors.White.copy(alpha = 0.85f))
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(task.priority.label, color = AppColors.Ink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(AppColors.Ink)
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = task.dueDate?.let { DateInput.formatTaskLabel(it) } ?: task.dueLabel,
                                color = AppColors.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = task.title,
                        color = AppColors.Ink,
                        style = MaterialTheme.typography.headlineMedium,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                    if (linkedProject != null) {
                        Text(
                            text = "Проект: ${linkedProject.name}",
                            color = AppColors.Muted,
                            fontSize = 13.sp,
                            modifier = Modifier.clickable { onOpenProject(linkedProject.id) }
                        )
                    }
                }
            }

            item {
                PastelCard(
                    color = AppColors.White,
                    contentPadding = PaddingValues(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Время", color = AppColors.Ink, style = MaterialTheme.typography.titleMedium)
                        Text("план ${TaskTimerState.formatShort(plannedSeconds)}", color = AppColors.Muted, fontSize = 12.sp)
                    }
                    Text(
                        text = if (totalTaskSeconds == 0L) "0 мин" else TaskTimerState.formatShort(totalTaskSeconds),
                        color = AppColors.Ink,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (taskSessions.isNotEmpty()) {
                        HorizontalDivider(color = AppColors.Outline.copy(alpha = 0.5f))
                        taskSessions.take(4).forEach { session ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(session.formattedRange(), color = AppColors.Muted, fontSize = 13.sp)
                                Text(TaskTimerState.formatShort(session.durationSeconds), color = AppColors.Ink, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Подзадачи (стр. 19)
            item {
                Text("Подзадачи", color = AppColors.Muted, style = MaterialTheme.typography.bodySmall)
            }
            item {
                PastelCard(
                    color = AppColors.White,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    taskSubtasks.forEach { sub ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(role = Role.Checkbox) { onToggleSubtask(sub.id) }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .then(if (sub.isCompleted) Modifier.background(AppColors.Ink) else Modifier),
                                contentAlignment = Alignment.Center
                            ) {
                                if (sub.isCompleted) {
                                    Icon(Icons.Outlined.Check, contentDescription = "Выполнено", tint = AppColors.White, modifier = Modifier.size(13.dp))
                                } else {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        drawCircle(color = Color(0xFF151515), style = Stroke(width = 1.8.dp.toPx()))
                                    }
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = sub.title,
                                color = if (sub.isCompleted) AppColors.Muted else AppColors.Ink,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                textDecoration = if (sub.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { onDeleteSubtask(sub.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Outlined.Close, contentDescription = "Удалить подзадачу", tint = AppColors.Muted, modifier = Modifier.size(15.dp))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AppColors.Background)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = newSubtaskTitle,
                            onValueChange = { newSubtaskTitle = it },
                            singleLine = true,
                            textStyle = TextStyle(color = AppColors.Ink, fontSize = 14.sp, fontWeight = FontWeight.Medium),
                            cursorBrush = SolidColor(AppColors.Ink),
                            modifier = Modifier.weight(1f),
                            decorationBox = { inner ->
                                if (newSubtaskTitle.isEmpty()) Text("Добавить подзадачу…", color = AppColors.Muted, fontSize = 14.sp)
                                inner()
                            }
                        )
                        IconButton(
                            onClick = {
                                if (newSubtaskTitle.isNotBlank()) {
                                    onAddSubtask(task.id, newSubtaskTitle)
                                    newSubtaskTitle = ""
                                }
                            },
                            enabled = newSubtaskTitle.isNotBlank(),
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Add,
                                contentDescription = "Добавить",
                                tint = if (newSubtaskTitle.isNotBlank()) AppColors.Ink else AppColors.Muted.copy(alpha = 0.4f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            item {
                PastelCard(
                    color = AppColors.White,
                    contentPadding = PaddingValues(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Детали", color = AppColors.Ink, style = MaterialTheme.typography.titleMedium)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Outlined.AccessTime, contentDescription = null, tint = AppColors.Muted, modifier = Modifier.size(18.dp))
                        Text("Напоминание", color = AppColors.Muted, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Text(
                            text = if (task.reminderEnabled) "Вкл · ${task.reminderTime}" else "Отключено",
                            color = AppColors.Ink,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Outlined.Repeat, contentDescription = null, tint = AppColors.Muted, modifier = Modifier.size(18.dp))
                        Text("Повторение", color = AppColors.Muted, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Text(formatRecurrence(task), color = AppColors.Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (linkedProject != null || relatedGoals.isNotEmpty() || seriesOccurrences.isNotEmpty()) {
                item {
                    PastelCard(
                        color = AppColors.White,
                        contentPadding = PaddingValues(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Связи", color = AppColors.Ink, style = MaterialTheme.typography.titleMedium)
                        linkedProject?.let { project ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(project.colorArgb).copy(alpha = 0.55f))
                                    .clickable { onOpenProject(project.id) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.FolderOpen, contentDescription = null, tint = AppColors.Ink, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(project.name, color = AppColors.Ink, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text("${(project.progress * 100).toInt()}%", color = AppColors.Ink, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = AppColors.Ink)
                            }
                        }
                        relatedGoals.forEach { goal ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(AppColors.Background)
                                    .clickable { onOpenGoal(goal.id) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.Flag, contentDescription = null, tint = AppColors.Ink, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(goal.title, color = AppColors.Ink, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                Text("${goal.currentValue}/${goal.targetValue}", color = AppColors.Muted, fontSize = 12.sp)
                            }
                        }
                        seriesOccurrences.forEach { occ ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(AppColors.Background)
                                    .clickable { onOpenTask(occ.id) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.CheckCircleOutline, contentDescription = null, tint = AppColors.Muted, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(occ.dueLabel, color = AppColors.Ink, modifier = Modifier.weight(1f), fontSize = 13.sp)
                                Text(if (occ.isCompleted) "Готово" else "Открыта", color = AppColors.Muted, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            if (taskHistory.isNotEmpty()) {
                item {
                    PastelCard(
                        color = AppColors.White,
                        contentPadding = PaddingValues(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Outlined.History, contentDescription = null, tint = AppColors.Ink, modifier = Modifier.size(18.dp))
                            Text("История изменений", color = AppColors.Ink, style = MaterialTheme.typography.titleMedium)
                        }
                        taskHistory.forEach { event ->
                            val dateTime = Instant.ofEpochMilli(event.occurredAtEpochMillis)
                                .atZone(ZoneId.systemDefault())
                                .format(DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale.forLanguageTag("ru")))
                            val label = when {
                                event.type == "TASK_SKIPPED" -> "Повторение пропущено"
                                event.completed -> "Задача выполнена"
                                else -> "Возвращена в работу"
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(label, color = AppColors.Ink, fontSize = 13.sp)
                                Text(dateTime, color = AppColors.Muted, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = { onStartTimer(task, linkedProject?.name) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Ink, contentColor = AppColors.White),
            contentPadding = PaddingValues(vertical = 15.dp)
        ) {
            Icon(Icons.Outlined.PlayArrow, contentDescription = null, modifier = Modifier.size(19.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (isTimerActiveHere) "Открыть таймер" else "Начать таймер",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }

    if (showEditDialog) {
        TaskCreateScreen(
            projects = projects,
            initialTask = task,
            initialProjectId = task.projectId,
            onBack = { showEditDialog = false },
            onCreate = { _, _, _, _, _, _, _, _, _, _, _ -> },
            onUpdate = onUpdateTask
        )
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(if (task.recurrence != TaskRecurrence.NONE && !task.isCompleted) "Пропустить повторение?" else "Удалить задачу?", color = AppColors.Ink) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(task.title, color = AppColors.Muted)
                    if (task.recurrence != TaskRecurrence.NONE && !task.isCompleted) {
                        Text("Следующее повторение будет создано по расписанию.", color = AppColors.Muted, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteTask(task.id) {
                        showDeleteConfirmation = false
                        onBack()
                    }
                }) {
                    Text(if (task.recurrence != TaskRecurrence.NONE && !task.isCompleted) "Пропустить" else "Удалить", color = AppColors.Error)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirmation = false }) { Text("Отмена", color = AppColors.Muted) } },
            containerColor = AppColors.White
        )
    }
}

private fun formatRecurrence(task: DashboardTask): String {
    val base = when (task.recurrence) {
        TaskRecurrence.NONE -> return "Не повторяется"
        TaskRecurrence.DAILY -> "Каждый день"
        TaskRecurrence.WEEKLY -> {
            val names = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
            val days = task.recurrenceDays.sorted().mapNotNull { names.getOrNull(it - 1) }
            if (days.isEmpty()) "Каждую неделю" else "Еженедельно · ${days.joinToString(" ")}"
        }
        TaskRecurrence.MONTHLY -> {
            val day = task.recurrenceAnchorDay.takeIf { it in 1..31 } ?: task.dueDate?.dayOfMonth
            if (day != null) "Каждый месяц ($day числа)" else "Каждый месяц"
        }
    }
    val limit = when {
        task.recurrenceEndDate != null -> " · до ${DateInput.formatShort(task.recurrenceEndDate)}"
        task.recurrenceRemainingCount != null -> " · осталось ${task.recurrenceRemainingCount}"
        else -> ""
    }
    return base + limit
}