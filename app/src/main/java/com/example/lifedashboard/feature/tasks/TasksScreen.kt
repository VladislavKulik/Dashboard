package com.example.lifedashboard.feature.tasks

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.data.model.DashboardTask
import com.example.lifedashboard.data.model.TaskDayCategory
import com.example.lifedashboard.data.model.TaskPriority
import com.example.lifedashboard.data.model.TaskRecurrence
import com.example.lifedashboard.presentation.TaskTimerState
import com.example.lifedashboard.ui.components.AppFab
import com.example.lifedashboard.ui.components.EmptySectionState
import com.example.lifedashboard.ui.components.FilterPill
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.components.ScreenTopBar
import com.example.lifedashboard.ui.theme.AppColors
import java.time.LocalDate
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

private enum class TaskFilter(val title: String) {
    ALL("Все"),
    TODAY("Сегодня"),
    OVERDUE("Просрочено"),
    UPCOMING("Позже"),
    COMPLETED("Выполнено")
}

@Composable
fun TasksScreen(
    tasks: List<DashboardTask>,
    projects: List<DashboardProject>,
    initialProjectId: String? = null,
    onToggleTask: (String) -> Unit,
    onAddTask: (String, TaskPriority, String?, LocalDate?, TaskRecurrence, Set<Int>, Boolean, String) -> Unit,
    onOpenProject: (String) -> Unit,
    onUpdateTask: (DashboardTask, () -> Unit) -> Unit,
    onDeleteTask: (String, () -> Unit) -> Unit,
    operationError: String? = null,
    initialTaskId: String? = null,
    startWithCreate: Boolean = false,
    onOpenTaskDetails: (String) -> Unit = {},
    onBack: (() -> Unit)? = null,
    timerState: TaskTimerState = TaskTimerState(),
    onToggleTimerPause: () -> Unit = {},
    onStopTimer: () -> Unit = {},
    onOpenTimer: () -> Unit = {},
    onCreateTask: (() -> Unit)? = null,
    onOpenSearch: () -> Unit = {}
) {
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedFilter by remember(initialProjectId) { mutableStateOf(TaskFilter.ALL) }
    var showAddDialog by remember(startWithCreate) { mutableStateOf(startWithCreate && onCreateTask == null) }
    var deletingTask by remember { mutableStateOf<DashboardTask?>(null) }

    val today = LocalDate.now()
    val baseTasks = if (initialProjectId == null) tasks else tasks.filter { it.projectId == initialProjectId }
    val visibleTasks = baseTasks.filter { task ->
        when (selectedFilter) {
            TaskFilter.ALL -> true
            TaskFilter.TODAY -> (task.dueDate == today || (task.dueDate == null && task.dayCategory == TaskDayCategory.TODAY)) && !task.isCompleted
            TaskFilter.UPCOMING -> (task.dueDate?.isAfter(today) == true || (task.dueDate == null && task.dayCategory == TaskDayCategory.UPCOMING)) && !task.isCompleted
            TaskFilter.OVERDUE -> task.dueDate?.isBefore(today) == true && !task.isCompleted
            TaskFilter.COMPLETED -> task.isCompleted
        }
    }

    val todaySectionTasks = remember(visibleTasks, today) {
        visibleTasks.filter {
            it.dueDate == today ||
                it.dueDate?.isBefore(today) == true ||
                (it.dueDate == null && it.dayCategory == TaskDayCategory.TODAY)
        }
    }
    val laterSectionTasks = remember(visibleTasks, todaySectionTasks) {
        val todayIds = todaySectionTasks.map { it.id }.toSet()
        visibleTasks.filter { it.id !in todayIds }
    }

    val completedCount = baseTasks.count { it.isCompleted }
    val totalCount = baseTasks.size
    val selectedProject = projects.firstOrNull { it.id == initialProjectId }
    val overviewCompleted = selectedProject?.completedTasks ?: completedCount
    val overviewTotal = selectedProject?.totalTasks ?: totalCount
    val overviewPercent = if (overviewTotal == 0) 0 else (overviewCompleted * 100 / overviewTotal)

    fun handleToggleWithUndo(task: DashboardTask) {
        onToggleTask(task.id)
        if (!task.isCompleted) {
            scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                val res = snackbarHostState.showSnackbar(
                    message = "Задача выполнена",
                    actionLabel = "Отменить",
                    duration = SnackbarDuration.Short
                )
                if (res == SnackbarResult.ActionPerformed) {
                    onToggleTask(task.id)
                }
            }
        }
    }

    fun openAddFlow() {
        if (onCreateTask != null) onCreateTask() else showAddDialog = true
    }

    LaunchedEffect(initialTaskId, tasks) {
        val target = tasks.firstOrNull { it.id == initialTaskId }
        if (target != null) {
            selectedFilter = when {
                target.isCompleted -> TaskFilter.COMPLETED
                target.dueDate?.isBefore(today) == true -> TaskFilter.OVERDUE
                target.dueDate == today || (target.dueDate == null && target.dayCategory == TaskDayCategory.TODAY) -> TaskFilter.TODAY
                else -> TaskFilter.UPCOMING
            }
        }
    }

    val projectNames = remember(projects) { projects.associate { it.id to it.name } }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                ScreenTopBar(
                    title = if (selectedProject == null) "Задачи" else selectedProject.name,
                    actionIcon = Icons.Outlined.Search,
                    actionDescription = "Поиск задач",
                    onActionClick = onOpenSearch,
                    onBackClick = onBack
                )
            }

            if (!operationError.isNullOrBlank()) {
                item { Text(operationError, color = AppColors.Error, style = MaterialTheme.typography.bodySmall) }
            }

            if (baseTasks.isEmpty()) {
                item {
                    EmptySectionState(
                        icon = Icons.Outlined.CheckCircleOutline,
                        iconBackground = AppColors.Blue,
                        title = "Задач пока нет",
                        subtitle = "Добавьте первую задачу, чтобы спланировать день и отслеживать свой прогресс",
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
            } else {
                item {
                    PastelCard(
                        color = AppColors.Yellow,
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Выполнено $overviewCompleted из $overviewTotal",
                                color = AppColors.Ink,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "$overviewPercent%",
                                color = AppColors.Ink,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TaskFilter.values().forEach { filter ->
                            FilterPill(
                                label = filter.title,
                                selected = selectedFilter == filter,
                                onClick = { selectedFilter = filter }
                            )
                        }
                    }
                }

                if (visibleTasks.isEmpty()) {
                    item {
                        EmptySectionState(
                            icon = Icons.Outlined.CheckCircleOutline,
                            iconBackground = AppColors.Blue,
                            title = "В этом списке пусто",
                            subtitle = "По выбранному фильтру задач нет",
                            modifier = Modifier.padding(top = 24.dp)
                        )
                    }
                } else {
                    if (todaySectionTasks.isNotEmpty()) {
                        item {
                            Text("Сегодня", color = AppColors.Muted, style = MaterialTheme.typography.bodySmall)
                        }
                        items(todaySectionTasks, key = { it.id }) { task ->
                            SwipeableTaskCardRow(
                                task = task,
                                projectName = projectNames[task.projectId],
                                highlighted = task.id == initialTaskId,
                                onToggle = { handleToggleWithUndo(task) },
                                onOpenDetails = { onOpenTaskDetails(task.id) },
                                onDelete = { deletingTask = task }
                            )
                        }
                    }

                    if (laterSectionTasks.isNotEmpty()) {
                        item {
                            Text(
                                text = if (selectedFilter == TaskFilter.COMPLETED) "Завершённые" else "Позже",
                                color = AppColors.Muted,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        items(laterSectionTasks, key = { it.id }) { task ->
                            SwipeableTaskCardRow(
                                task = task,
                                projectName = projectNames[task.projectId],
                                highlighted = task.id == initialTaskId,
                                onToggle = { handleToggleWithUndo(task) },
                                onOpenDetails = { onOpenTaskDetails(task.id) },
                                onDelete = { deletingTask = task }
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (timerState.activeTaskId != null) {
                ActiveTimerMiniBar(
                    timerState = timerState,
                    onClick = onOpenTimer,
                    onTogglePause = onToggleTimerPause,
                    onStop = onStopTimer
                )
            }
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    shape = RoundedCornerShape(20.dp),
                    containerColor = AppColors.Ink,
                    contentColor = AppColors.White,
                    action = {
                        data.visuals.actionLabel?.let { label ->
                            TextButton(onClick = { data.performAction() }) {
                                Text(label, color = AppColors.Yellow, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                ) {
                    Text(data.visuals.message, fontWeight = FontWeight.Medium)
                }
            }
        }

        AppFab(
            icon = Icons.Outlined.Add,
            contentDescription = "Добавить задачу",
            onClick = ::openAddFlow,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = if (timerState.activeTaskId != null) 94.dp else 20.dp)
        )
    }

    if (showAddDialog) {
        TaskCreateScreen(
            projects = projects,
            initialProjectId = initialProjectId,
            onBack = { showAddDialog = false },
            onCreate = { title, priority, projectId, dueDate, recurrence, recurrenceDays, reminderEnabled, reminderTime, _, _, _ ->
                onAddTask(title, priority, projectId, dueDate, recurrence, recurrenceDays, reminderEnabled, reminderTime)
                selectedFilter = TaskFilter.ALL
                showAddDialog = false
            }
        )
    }

    deletingTask?.let { task ->
        AlertDialog(
            onDismissRequest = { deletingTask = null },
            title = { Text(if (task.recurrence != TaskRecurrence.NONE && !task.isCompleted) "Пропустить повторение?" else "Удалить задачу?", color = AppColors.Ink) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(task.title, color = AppColors.Muted)
                    if (task.recurrence != TaskRecurrence.NONE && !task.isCompleted) {
                        Text("Следующее повторение будет создано по расписанию. Остальная серия сохранится.", color = AppColors.Muted, fontSize = 12.sp)
                    }
                    if (!operationError.isNullOrBlank()) Text(operationError, color = AppColors.Error)
                }
            },
            confirmButton = {
                TextButton(onClick = { onDeleteTask(task.id) { deletingTask = null } }) {
                    Text(if (task.recurrence != TaskRecurrence.NONE && !task.isCompleted) "Пропустить" else "Удалить", color = AppColors.Error)
                }
            },
            dismissButton = { TextButton(onClick = { deletingTask = null }) { Text("Отмена", color = AppColors.Muted) } },
            containerColor = AppColors.White
        )
    }
}

@Composable
private fun SwipeableTaskCardRow(
    task: DashboardTask,
    projectName: String?,
    highlighted: Boolean,
    onToggle: () -> Unit,
    onOpenDetails: () -> Unit,
    onDelete: () -> Unit
) {
    val maxRevealPx = with(LocalDensity.current) { 108.dp.toPx() }
    var rawOffset by remember(task.id) { mutableFloatStateOf(0f) }
    val animatedOffset by animateFloatAsState(targetValue = rawOffset, label = "taskSwipe")

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AppColors.Mint)
                    .clickable {
                        rawOffset = 0f
                        onToggle()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Check, contentDescription = "Выполнить", tint = AppColors.Ink, modifier = Modifier.size(20.dp))
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AppColors.Pink)
                    .clickable {
                        rawOffset = 0f
                        onDelete()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = "Удалить", tint = AppColors.Ink, modifier = Modifier.size(20.dp))
            }
        }

        PastelCard(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .pointerInput(task.id) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            rawOffset = if (rawOffset < -maxRevealPx * 0.45f) -maxRevealPx else 0f
                        },
                        onDragCancel = { rawOffset = 0f },
                        onHorizontalDrag = { _, dragAmount ->
                            rawOffset = (rawOffset + dragAmount).coerceIn(-maxRevealPx, 0f)
                        }
                    )
                }
                .clickable {
                    if (rawOffset != 0f) rawOffset = 0f else onOpenDetails()
                },
            color = if (highlighted) AppColors.Lavender else AppColors.White,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .then(if (task.isCompleted) Modifier.background(AppColors.Ink) else Modifier)
                        .clickable(role = Role.Checkbox, onClick = onToggle),
                    contentAlignment = Alignment.Center
                ) {
                    if (task.isCompleted) {
                        Icon(Icons.Outlined.Check, contentDescription = "Выполнено", tint = AppColors.White, modifier = Modifier.size(14.dp))
                    } else {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(color = Color(0xFF151515), style = Stroke(width = 2.dp.toPx()))
                        }
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = task.title,
                        color = if (task.isCompleted) AppColors.Muted else AppColors.Ink,
                        style = MaterialTheme.typography.bodyLarge,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    val subtitle = buildList {
                        if (!projectName.isNullOrBlank()) add(projectName)
                        if (task.dueLabel.isNotBlank() && !task.dueLabel.equals("Сегодня", ignoreCase = true)) {
                            add(task.dueLabel)
                        } else if (projectName.isNullOrBlank()) {
                            add(task.trailingLabel ?: "Без проекта")
                        }
                        if (task.recurrence != TaskRecurrence.NONE) add("↻")
                    }.joinToString(" · ")
                    if (subtitle.isNotBlank()) {
                        Text(subtitle, color = AppColors.Muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                Spacer(Modifier.width(10.dp))

                PriorityDotLabel(task.priority)
            }
        }
    }
}

@Composable
private fun PriorityDotLabel(priority: TaskPriority) {
    val dotColor = when (priority) {
        TaskPriority.HIGH -> AppColors.Error
        TaskPriority.MEDIUM -> AppColors.ChartYellow
        TaskPriority.LOW -> AppColors.ChartGreen
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Text(
            text = stringResource(id = priority.labelResId),
            color = AppColors.Ink,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ActiveTimerMiniBar(
    timerState: TaskTimerState,
    onClick: () -> Unit,
    onTogglePause: () -> Unit,
    onStop: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = AppColors.Ink,
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = timerState.activeTaskTitle,
                    color = AppColors.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = TaskTimerState.formatHms(timerState.elapsedSeconds),
                    color = AppColors.MutedOnDark,
                    fontSize = 12.sp
                )
            }
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(AppColors.Yellow)
                    .clickable(onClick = onTogglePause),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (timerState.isRunning) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                    contentDescription = if (timerState.isRunning) "Пауза" else "Продолжить",
                    tint = AppColors.Ink,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(AppColors.White.copy(alpha = 0.14f))
                    .clickable(onClick = onStop),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Stop,
                    contentDescription = "Остановить",
                    tint = AppColors.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}