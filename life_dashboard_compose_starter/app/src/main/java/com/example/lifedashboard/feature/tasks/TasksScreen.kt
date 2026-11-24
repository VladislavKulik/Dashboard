package com.example.lifedashboard.feature.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.data.DateInput
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.data.model.DashboardTask
import com.example.lifedashboard.data.model.TaskDayCategory
import com.example.lifedashboard.data.model.TaskPriority
import com.example.lifedashboard.data.model.TaskRecurrence
import com.example.lifedashboard.ui.components.FilterPill
import com.example.lifedashboard.ui.components.LocalDatePickerField
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.components.ProgressTrack
import com.example.lifedashboard.ui.components.ScreenTopBar
import com.example.lifedashboard.ui.components.TaskRow
import com.example.lifedashboard.ui.theme.AppColors
import java.time.LocalDate

private enum class TaskFilter(val title: String) {
    ALL("Все"),
    TODAY("Сегодня"),
    UPCOMING("Предстоящие"),
    OVERDUE("Просрочено"),
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
    startWithCreate: Boolean = false
) {
    val listState = rememberLazyListState()
    var selectedFilter by remember(initialProjectId) { mutableStateOf(TaskFilter.ALL) }
    var searchExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember(startWithCreate) { mutableStateOf(startWithCreate) }
    var editingTask by remember { mutableStateOf<DashboardTask?>(null) }
    var deletingTask by remember { mutableStateOf<DashboardTask?>(null) }

    val baseTasks = if (initialProjectId == null) tasks else tasks.filter { it.projectId == initialProjectId }
    val visibleTasks = baseTasks.filter { task ->
        val matchesFilter = when (selectedFilter) {
            TaskFilter.ALL -> true
            TaskFilter.TODAY -> (task.dueDate == LocalDate.now() || (task.dueDate == null && task.dayCategory == TaskDayCategory.TODAY)) && !task.isCompleted
            TaskFilter.UPCOMING -> (task.dueDate?.isAfter(LocalDate.now()) == true || (task.dueDate == null && task.dayCategory == TaskDayCategory.UPCOMING)) && !task.isCompleted
            TaskFilter.OVERDUE -> task.dueDate?.isBefore(LocalDate.now()) == true && !task.isCompleted
            TaskFilter.COMPLETED -> task.isCompleted
        }
        val matchesSearch = searchQuery.isBlank() || task.title.contains(searchQuery, ignoreCase = true)
        matchesFilter && matchesSearch
    }
    val completedCount = baseTasks.count { it.isCompleted }
    val totalCount = baseTasks.size
    val selectedProject = projects.firstOrNull { it.id == initialProjectId }

    LaunchedEffect(initialTaskId, tasks) {
        val target = tasks.firstOrNull { it.id == initialTaskId }
        if (target != null) {
            selectedFilter = when {
                target.isCompleted -> TaskFilter.COMPLETED
                target.dueDate?.isBefore(LocalDate.now()) == true -> TaskFilter.OVERDUE
                target.dueDate == LocalDate.now() || (target.dueDate == null && target.dayCategory == TaskDayCategory.TODAY) -> TaskFilter.TODAY
                else -> TaskFilter.UPCOMING
            }
            searchExpanded = false
            searchQuery = ""
        }
    }
    LaunchedEffect(initialTaskId, visibleTasks, operationError, searchExpanded) {
        val targetIndex = visibleTasks.indexOfFirst { it.id == initialTaskId }
        if (initialTaskId != null && targetIndex >= 0) {
            val fixedItemsBeforeTasks = 4 + (if (searchExpanded) 1 else 0) + (if (!operationError.isNullOrBlank()) 1 else 0)
            listState.animateScrollToItem(fixedItemsBeforeTasks + targetIndex)
        }
    }

    val overviewCompleted = selectedProject?.completedTasks ?: completedCount
    val overviewTotal = selectedProject?.totalTasks ?: totalCount

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ScreenTopBar(
                title = if (selectedProject == null) "Задачи" else "Задачи проекта",
                actionIcon = if (searchExpanded) Icons.Outlined.Close else Icons.Outlined.Search,
                actionDescription = if (searchExpanded) "Закрыть поиск" else "Поиск задач",
                onActionClick = {
                    searchExpanded = !searchExpanded
                    if (!searchExpanded) searchQuery = ""
                }
            )
        }

        if (searchExpanded) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Найти задачу") },
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        if (!operationError.isNullOrBlank()) {
            item { Text(operationError, color = AppColors.Error, style = MaterialTheme.typography.bodySmall) }
        }

        item {
            TaskOverviewCard(
                title = if (selectedProject == null) "Прогресс задач" else "Прогресс проекта",
                completedCount = overviewCompleted,
                totalCount = overviewTotal
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskFilter.values().forEach { filter ->
                    val count = when (filter) {
                        TaskFilter.ALL -> baseTasks.size
                        TaskFilter.TODAY -> baseTasks.count { (it.dueDate == LocalDate.now() || (it.dueDate == null && it.dayCategory == TaskDayCategory.TODAY)) && !it.isCompleted }
                        TaskFilter.UPCOMING -> baseTasks.count {
                            (it.dueDate?.isAfter(LocalDate.now()) == true || (it.dueDate == null && it.dayCategory == TaskDayCategory.UPCOMING)) && !it.isCompleted
                        }
                        TaskFilter.OVERDUE -> baseTasks.count { it.dueDate?.isBefore(LocalDate.now()) == true && !it.isCompleted }
                        TaskFilter.COMPLETED -> completedCount
                    }
                    FilterPill(
                        label = filter.title,
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        count = count.toString()
                    )
                }
            }
        }

        if (visibleTasks.isEmpty()) {
            item {
                EmptyTasksState(
                    title = if (searchQuery.isNotBlank()) "Ничего не найдено" else "Здесь пока пусто",
                    description = if (searchQuery.isNotBlank()) {
                        "Попробуй изменить поисковый запрос."
                    } else {
                        "Добавь задачу или выбери другой фильтр."
                    }
                )
            }
        } else {
            item {
                Text(
                    text = when (selectedFilter) {
                        TaskFilter.ALL -> "Все задачи"
                        TaskFilter.TODAY -> "На сегодня"
                        TaskFilter.UPCOMING -> "Впереди"
                        TaskFilter.OVERDUE -> "Просроченные"
                        TaskFilter.COMPLETED -> "Завершённые"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = AppColors.Ink
                )
            }
            items(visibleTasks, key = { it.id }) { task ->
                TaskItemCard(
                    task = task,
                    projectName = projects.firstOrNull { it.id == task.projectId }?.name,
                    onToggle = { onToggleTask(task.id) },
                    onProjectClick = task.projectId?.let { id -> { onOpenProject(id) } },
                    onEdit = { editingTask = task },
                    onDelete = { deletingTask = task },
                    highlighted = task.id == initialTaskId
                )
            }
        }

        item {
            AddTaskButton(onClick = { showAddDialog = true })
        }
    }

    if (showAddDialog) {
        AddTaskDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title, priority, dueDate, recurrence, recurrenceDays, reminderEnabled, reminderTime ->
                onAddTask(title, priority, initialProjectId, dueDate, recurrence, recurrenceDays, reminderEnabled, reminderTime)
                selectedFilter = TaskFilter.ALL
                searchQuery = ""
                searchExpanded = false
                showAddDialog = false
            }
        )
    }
    editingTask?.let { task ->
        EditTaskDialog(
            task = task,
            projects = projects,
            onDismiss = { editingTask = null },
            onSave = { updated -> onUpdateTask(updated) { editingTask = null } },
            operationError = operationError
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
private fun TaskOverviewCard(title: String, completedCount: Int, totalCount: Int) {
    val progress = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount
    PastelCard(
        color = AppColors.Yellow,
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, color = AppColors.Ink, style = MaterialTheme.typography.titleLarge)
                Text("Выполнено $completedCount из $totalCount", color = AppColors.Muted, fontSize = 13.sp)
            }
            Text(
                text = "${(progress * 100).toInt()}%",
                color = AppColors.Ink,
                style = MaterialTheme.typography.titleLarge
            )
        }
        ProgressTrack(progress = progress, trackColor = AppColors.White.copy(alpha = 0.75f))
    }
}

@Composable
private fun TaskItemCard(
    task: DashboardTask,
    projectName: String?,
    onToggle: () -> Unit,
    onProjectClick: (() -> Unit)?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    highlighted: Boolean = false
) {
    PastelCard(
        color = if (highlighted) AppColors.Lavender else AppColors.White,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 13.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        TaskRow(
            title = task.title,
            completed = task.isCompleted,
            onToggle = onToggle
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PriorityLabel(task.priority)
            if (task.recurrence != TaskRecurrence.NONE) {
                Text("↻", color = AppColors.Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                text = task.dueLabel,
                color = AppColors.Muted,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f)
            )
            if (!projectName.isNullOrBlank()) {
                Text(
                    text = projectName,
                    color = AppColors.Ink,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(AppColors.Background)
                        .clickable(enabled = onProjectClick != null, onClick = { onProjectClick?.invoke() })
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onEdit) { Text("Изменить", color = AppColors.Muted, fontSize = 12.sp) }
            TextButton(onClick = onDelete) { Text("Удалить", color = AppColors.Error, fontSize = 12.sp) }
        }
    }
}

@Composable
private fun PriorityLabel(priority: TaskPriority) {
    val background = when (priority) {
        TaskPriority.HIGH -> AppColors.Pink
        TaskPriority.MEDIUM -> AppColors.Yellow
        TaskPriority.LOW -> AppColors.Mint
    }
    Text(
        text = priority.label,
        color = AppColors.Ink,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(CircleShape)
            .background(background)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    )
}

@Composable
private fun EmptyTasksState(title: String, description: String) {
    PastelCard(color = AppColors.Lavender) {
        Text(title, color = AppColors.Ink, style = MaterialTheme.typography.titleLarge)
        Text(description, color = AppColors.Muted, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun AddTaskButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(AppColors.Ink)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        androidx.compose.material3.Icon(
            imageVector = Icons.Outlined.Add,
            contentDescription = null,
            tint = AppColors.White,
            modifier = Modifier.size(19.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text("Добавить задачу", color = AppColors.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAdd: (String, TaskPriority, LocalDate?, TaskRecurrence, Set<Int>, Boolean, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var dueDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var recurrence by remember { mutableStateOf(TaskRecurrence.NONE) }
    var recurrenceDays by remember { mutableStateOf(setOf(LocalDate.now().dayOfWeek.value)) }
    var reminderEnabled by remember { mutableStateOf(false) }
    var reminderTime by remember { mutableStateOf("09:00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая задача", color = AppColors.Ink) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Название") },
                    placeholder = { Text("Что нужно сделать?") }
                )
                LocalDatePickerField(date = dueDate, label = "Срок выполнения", onDateChange = { dueDate = it })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it }, enabled = dueDate != null)
                    Column {
                        Text("Напомнить о задаче", color = AppColors.Ink, fontSize = 13.sp)
                        Text("Уведомление в выбранный день", color = AppColors.Muted, fontSize = 11.sp)
                    }
                }
                if (reminderEnabled) {
                    OutlinedTextField(value = reminderTime, onValueChange = { reminderTime = it }, label = { Text("Время (ЧЧ:ММ)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    if (!reminderTime.matches(Regex("(?:[01]\\d|2[0-3]):[0-5]\\d"))) Text("Укажи время в формате 09:30", color = AppColors.Error, fontSize = 11.sp)
                }
                Text("Повторение", color = AppColors.Muted, fontSize = 12.sp)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TaskRecurrence.values().forEach { option -> FilterPill(option.label, recurrence == option, { recurrence = option }) }
                }
                if (recurrence == TaskRecurrence.WEEKLY) {
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEachIndexed { index, label ->
                            val day = index + 1
                            FilterPill(label, day in recurrenceDays, { recurrenceDays = if (day in recurrenceDays) recurrenceDays - day else recurrenceDays + day })
                        }
                    }
                }
                Text("Приоритет", color = AppColors.Muted, fontSize = 13.sp)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TaskPriority.values().forEach { option ->
                        FilterPill(
                            label = option.label,
                            selected = priority == option,
                            onClick = { priority = option }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(title, priority, dueDate, recurrence, if (recurrence == TaskRecurrence.WEEKLY) recurrenceDays else emptySet(), reminderEnabled, reminderTime) },
                enabled = title.isNotBlank() && (recurrence == TaskRecurrence.NONE || dueDate != null) && (recurrence != TaskRecurrence.WEEKLY || recurrenceDays.isNotEmpty()) && (!reminderEnabled || (dueDate != null && reminderTime.matches(Regex("(?:[01]\\d|2[0-3]):[0-5]\\d"))))
            ) {
                Text("Добавить", color = AppColors.Ink)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = AppColors.Muted)
            }
        },
        containerColor = AppColors.White
    )
}


@Composable
private fun EditTaskDialog(
    task: DashboardTask,
    projects: List<DashboardProject>,
    onDismiss: () -> Unit,
    onSave: (DashboardTask) -> Unit,
    operationError: String? = null
) {
    var title by remember(task.id) { mutableStateOf(task.title) }
    var dueDate by remember(task.id) { mutableStateOf(task.dueDate ?: DateInput.parseLegacyLabel(task.dueLabel)) }
    var dueDateTouched by remember(task.id) { mutableStateOf(false) }
    var priority by remember(task.id) { mutableStateOf(task.priority) }
    var dayCategory by remember(task.id) { mutableStateOf(task.dayCategory) }
    var projectId by remember(task.id) { mutableStateOf(task.projectId) }
    var recurrence by remember(task.id) { mutableStateOf(task.recurrence) }
    var recurrenceDays by remember(task.id) { mutableStateOf(task.recurrenceDays.ifEmpty { setOf(LocalDate.now().dayOfWeek.value) }) }
    var reminderEnabled by remember(task.id) { mutableStateOf(task.reminderEnabled) }
    var reminderTime by remember(task.id) { mutableStateOf(task.reminderTime) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактировать задачу", color = AppColors.Ink) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Название") }
                )
                LocalDatePickerField(
                    date = dueDate,
                    label = "Срок выполнения",
                    onDateChange = { dueDate = it; dueDateTouched = true }
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it }, enabled = dueDate != null)
                    Text("Напомнить о задаче", color = AppColors.Ink, fontSize = 13.sp)
                }
                if (reminderEnabled) {
                    OutlinedTextField(value = reminderTime, onValueChange = { reminderTime = it }, label = { Text("Время (ЧЧ:ММ)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    if (!reminderTime.matches(Regex("(?:[01]\\d|2[0-3]):[0-5]\\d"))) Text("Укажи время в формате 09:30", color = AppColors.Error, fontSize = 11.sp)
                }
                Text("Повторение", color = AppColors.Muted, fontSize = 12.sp)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TaskRecurrence.values().forEach { option ->
                        FilterPill(option.label, recurrence == option, { recurrence = option })
                    }
                }
                if (recurrence == TaskRecurrence.WEEKLY) {
                    Text("Повторять по дням", color = AppColors.Muted, fontSize = 12.sp)
                    val dayLabels = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        dayLabels.forEachIndexed { index, label ->
                            val day = index + 1
                            FilterPill(label, day in recurrenceDays, {
                                recurrenceDays = if (day in recurrenceDays) recurrenceDays - day else recurrenceDays + day
                            })
                        }
                    }
                    if (recurrenceDays.isEmpty()) Text("Выбери хотя бы один день недели.", color = AppColors.Error, fontSize = 11.sp)
                }
                if (recurrence != TaskRecurrence.NONE && dueDate == null) {
                    Text("Для повторения необходимо указать дату первого выполнения.", color = AppColors.Error, fontSize = 11.sp)
                }
                Text("Приоритет", color = AppColors.Muted, fontSize = 12.sp)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TaskPriority.values().forEach { option ->
                        FilterPill(option.label, priority == option, { priority = option })
                    }
                }
                Text("Категория", color = AppColors.Muted, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterPill("Сегодня", dayCategory == TaskDayCategory.TODAY, { dayCategory = TaskDayCategory.TODAY })
                    FilterPill("Предстоящие", dayCategory == TaskDayCategory.UPCOMING, { dayCategory = TaskDayCategory.UPCOMING })
                }
                if (!operationError.isNullOrBlank()) Text(operationError, color = AppColors.Error, fontSize = 12.sp)
                Text("Проект", color = AppColors.Muted, fontSize = 12.sp)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterPill("Без проекта", projectId == null, { projectId = null })
                    projects.forEach { project ->
                        FilterPill(project.name, projectId == project.id, { projectId = project.id })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val timePart = task.dueLabel.substringAfter('·', "").trim()
                    val resolvedDate = dueDate ?: if (!dueDateTouched) DateInput.parseLegacyLabel(task.dueLabel) else null
                    val formattedDueLabel = if (!dueDateTouched) task.dueLabel else if (resolvedDate == null) "Без срока" else {
                        listOf(DateInput.formatTaskLabel(resolvedDate), timePart).filter(String::isNotBlank).joinToString(" · ")
                    }
                    onSave(task.copy(
                        title = title.trim(), dueDate = resolvedDate, dueLabel = formattedDueLabel,
                        priority = priority, dayCategory = DateInput.categoryFor(dueDate) ?: dayCategory,
                        projectId = projectId, recurrence = recurrence,
                        recurrenceDays = if (recurrence == TaskRecurrence.WEEKLY) recurrenceDays else emptySet(),
                        recurrenceSeriesId = if (recurrence == TaskRecurrence.NONE) null else (task.recurrenceSeriesId ?: task.id),
                        reminderEnabled = reminderEnabled && resolvedDate != null, reminderTime = reminderTime,
                        recurrenceAnchorDay = if (recurrence == TaskRecurrence.MONTHLY) {
                            if (dueDateTouched) resolvedDate?.dayOfMonth ?: 0
                            else task.recurrenceAnchorDay.takeIf { it in 1..31 } ?: (resolvedDate?.dayOfMonth ?: 0)
                        } else 0
                    ))
                },
                enabled = title.isNotBlank() && (recurrence == TaskRecurrence.NONE || dueDate != null) &&
                    (recurrence != TaskRecurrence.WEEKLY || recurrenceDays.isNotEmpty()) &&
                    (!reminderEnabled || (dueDate != null && reminderTime.matches(Regex("(?:[01]\\d|2[0-3]):[0-5]\\d"))))
            ) { Text("Сохранить", color = AppColors.Ink) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена", color = AppColors.Muted) } },
        containerColor = AppColors.White
    )
}
