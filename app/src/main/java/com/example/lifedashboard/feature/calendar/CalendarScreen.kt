package com.example.lifedashboard.feature.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.data.model.DashboardActivityEvent
import com.example.lifedashboard.data.model.DashboardGoal
import com.example.lifedashboard.data.model.DashboardHabit
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.data.model.DashboardTask
import com.example.lifedashboard.data.model.TaskPriority
import com.example.lifedashboard.data.model.TaskRecurrence
import com.example.lifedashboard.ui.components.AppFab
import com.example.lifedashboard.ui.components.FilterPill
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.components.ScreenTopBar
import com.example.lifedashboard.ui.theme.AppColors
import java.time.LocalDate
import java.time.YearMonth

private enum class CalendarPeriod(val title: String) {
    ALL("Все даты"), NEXT_7_DAYS("7 дней"), THIS_MONTH("Месяц"), OVERDUE("Просрочено")
}

private enum class CalendarTypeFilter(val title: String, val type: CalendarEventType?) {
    ALL("Все", null), TASKS("Задачи", CalendarEventType.TASK),
    PROJECTS("Проекты", CalendarEventType.PROJECT), GOALS("Цели", CalendarEventType.GOAL),
    HABITS("Привычки", CalendarEventType.HABIT), ACTIVITY("Активность", CalendarEventType.ACTIVITY)
}

private enum class CalendarCreateAction {
    TASK, PROJECT, GOAL, HABITS
}

@Composable
fun CalendarScreen(
    tasks: List<DashboardTask>,
    projects: List<DashboardProject>,
    goals: List<DashboardGoal>,
    habits: List<DashboardHabit>,
    activityEvents: List<DashboardActivityEvent>,
    onBack: () -> Unit,
    onEventClick: (CalendarEvent) -> Unit,
    onToggleTask: (String) -> Unit = {},
    onAddTask: (String, TaskPriority, String?, LocalDate?, TaskRecurrence, Set<Int>, Boolean, String) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onUpdateTask: (DashboardTask, () -> Unit) -> Unit = { _, onSaved -> onSaved() },
    onOpenTaskDetails: (String) -> Unit = {},
    onSaveProject: (DashboardProject, () -> Unit) -> Unit = { _, onSaved -> onSaved() },
    onSaveGoal: (DashboardGoal, (() -> Unit)?) -> Unit = { _, onSaved -> onSaved?.invoke() },
    onToggleHabitOnDate: (String, String) -> Unit = { _, _ -> },
    operationError: String? = null,
    onOpenCreateTask: ((LocalDate) -> Unit)? = null,
    onOpenEditTask: ((String) -> Unit)? = null,
    onOpenCreateProject: ((LocalDate) -> Unit)? = null,
    onOpenCreateGoal: ((LocalDate) -> Unit)? = null
) {
    val events = remember(tasks, projects, goals, habits, activityEvents) {
        CalendarEventFactory.build(tasks, projects, goals, habits, activityEvents)
    }
    var monthOffset by remember { mutableStateOf(0) }
    var selectedDate by remember { mutableStateOf(CalendarEventFactory.todayKey()) }
    var selectedPeriod by remember { mutableStateOf(CalendarPeriod.ALL) }
    var selectedType by remember { mutableStateOf(CalendarTypeFilter.ALL) }

    var showFilterSheet by remember { mutableStateOf(false) }
    var showCreatePicker by remember { mutableStateOf(false) }
    var activeCreateDialog by remember { mutableStateOf<CalendarCreateAction?>(null) }
    var selectedTaskForSheet by remember { mutableStateOf<DashboardTask?>(null) }

    val hasActiveFilter = selectedPeriod != CalendarPeriod.ALL || selectedType != CalendarTypeFilter.ALL

    val todayDate = LocalDate.now()
    val today = remember { todayDate.toString() }
    val selectedLocalDate = remember(selectedDate) {
        runCatching { LocalDate.parse(selectedDate) }.getOrNull() ?: todayDate
    }
    val displayedYearMonth = YearMonth.now().plusMonths(monthOffset.toLong())
    val year = displayedYearMonth.year
    val month = displayedYearMonth.monthValue - 1

    fun shiftMonth(delta: Int) {
        monthOffset += delta
        val targetMonth = YearMonth.now().plusMonths(monthOffset.toLong())
        val selectedDay = runCatching { LocalDate.parse(selectedDate).dayOfMonth }.getOrDefault(todayDate.dayOfMonth)
        selectedDate = targetMonth.atDay(selectedDay.coerceAtMost(targetMonth.lengthOfMonth())).toString()
    }

    val monthCells = remember(year, month) { CalendarEventFactory.monthGrid(year, month) }
    val periodEvents = remember(events, selectedPeriod, year, month) {
        events.filter { event ->
            val date = runCatching { LocalDate.parse(event.dateKey) }.getOrNull() ?: return@filter false
            when (selectedPeriod) {
                CalendarPeriod.ALL -> true
                CalendarPeriod.NEXT_7_DAYS -> !date.isBefore(todayDate) && !date.isAfter(todayDate.plusDays(6))
                CalendarPeriod.THIS_MONTH -> YearMonth.from(date) == displayedYearMonth
                CalendarPeriod.OVERDUE -> date.isBefore(todayDate) && event.type in setOf(CalendarEventType.TASK, CalendarEventType.PROJECT, CalendarEventType.GOAL) && !event.completed
            }
        }
    }
    val visibleEvents = remember(periodEvents, selectedType) {
        periodEvents.filter { event -> selectedType.type == null || event.type == selectedType.type }
    }
    val monthEvents = remember(visibleEvents, year, month) {
        visibleEvents.filter { event ->
            val parts = event.dateKey.split('-')
            parts.size == 3 && parts[0].toIntOrNull() == year && parts[1].toIntOrNull() == month + 1
        }
    }
    val selectedEvents = remember(visibleEvents, selectedDate) { visibleEvents.filter { it.dateKey == selectedDate } }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ScreenTopBar(
                title = "Календарь",
                actionIcon = Icons.Outlined.Tune,
                actionDescription = "Фильтры календаря",
                onActionClick = { showFilterSheet = true },
                onBackClick = onBack
            )

            if (hasActiveFilter) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(AppColors.Lavender)
                        .clickable { showFilterSheet = true }
                        .padding(start = 14.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val filterSummary = buildList {
                        if (selectedPeriod != CalendarPeriod.ALL) add(selectedPeriod.title)
                        if (selectedType != CalendarTypeFilter.ALL) add(selectedType.title)
                    }.joinToString(" · ")
                    Text(
                        text = "Фильтр: $filterSummary",
                        color = AppColors.Ink,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(AppColors.White.copy(alpha = 0.8f))
                            .clickable {
                                selectedPeriod = CalendarPeriod.ALL
                                selectedType = CalendarTypeFilter.ALL
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Сбросить фильтры",
                            tint = AppColors.Ink,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            if (!operationError.isNullOrBlank()) {
                Text(operationError, color = AppColors.Error, style = MaterialTheme.typography.bodySmall)
            }

            PastelCard(color = AppColors.White, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        CalendarEventFactory.formatMonthTitle(year, month),
                        modifier = Modifier.weight(1f), color = AppColors.Ink,
                        fontSize = 20.sp, fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { shiftMonth(-1) }) {
                        Icon(Icons.Outlined.ChevronLeft, contentDescription = "Предыдущий месяц", tint = AppColors.Ink)
                    }
                    IconButton(onClick = { shiftMonth(1) }) {
                        Icon(Icons.Outlined.ChevronRight, contentDescription = "Следующий месяц", tint = AppColors.Ink)
                    }
                    TextButton(onClick = { monthOffset = 0; selectedDate = today }) {
                        Icon(Icons.Outlined.Today, contentDescription = null, tint = AppColors.Ink, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Сегодня", color = AppColors.Ink)
                    }
                }

                val weekdays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
                Row(modifier = Modifier.fillMaxWidth()) {
                    weekdays.forEach { day ->
                        Text(day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, color = AppColors.Muted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }

                monthCells.chunked(7).forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        week.forEach { dateKey ->
                            if (dateKey == null) {
                                Box(modifier = Modifier.weight(1f).height(58.dp))
                            } else {
                                val dayEvents = monthEvents.filter { it.dateKey == dateKey }
                                val isSelected = dateKey == selectedDate
                                val isToday = dateKey == today
                                Box(
                                    modifier = Modifier.weight(1f).height(58.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) AppColors.Ink else if (isToday) AppColors.Lavender.copy(alpha = 0.5f) else Color.Transparent)
                                        .clickable { selectedDate = dateKey },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text(
                                            dateKey.substringAfterLast('-').toIntOrNull()?.toString().orEmpty(),
                                            color = if (isSelected) AppColors.White else AppColors.Ink,
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.height(5.dp)) {
                                            dayEvents.take(3).forEach { event ->
                                                Box(Modifier.size(4.dp).clip(CircleShape).background(if (isSelected) AppColors.White else eventColor(event.type)))
                                            }
                                        }
                                    }
                                    if (dayEvents.size > 3) {
                                        Text(
                                            "+${dayEvents.size - 3}", fontSize = 8.sp,
                                            color = if (isSelected) AppColors.White else AppColors.Muted,
                                            modifier = Modifier.align(Alignment.TopEnd).padding(top = 3.dp, end = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                ) {
                    LegendDot(AppColors.Purple, "Задачи")
                    LegendDot(AppColors.Blue, "Проекты")
                    LegendDot(AppColors.Yellow, "Цели")
                    LegendDot(AppColors.Pink, "Привычки")
                    LegendDot(AppColors.Mint, "Активность")
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(CalendarEventFactory.formatDayTitle(selectedDate), color = AppColors.Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(
                        when (selectedEvents.size) {
                            0 -> "Нет событий"
                            1 -> "1 событие"
                            in 2..4 -> "${selectedEvents.size} события"
                            else -> "${selectedEvents.size} событий"
                        },
                        color = AppColors.Muted,
                        fontSize = 13.sp
                    )
                }

                if (selectedEvents.isEmpty()) {
                    PastelCard(color = AppColors.White) {
                        Text("На этот день ничего не запланировано", color = AppColors.Ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text(
                            "Нажми кнопку «+» внизу экрана, чтобы создать задачу, цель, проект или отметить привычку за этот день.",
                            color = AppColors.Muted,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    selectedEvents.forEach { event ->
                        PastelCard(
                            modifier = Modifier.fillMaxWidth().clickable {
                                if (event.type == CalendarEventType.TASK) {
                                    val task = tasks.firstOrNull { it.id == event.relatedId }
                                    if (task != null) selectedTaskForSheet = task else onEventClick(event)
                                } else {
                                    onEventClick(event)
                                }
                            },
                            color = eventColor(event.type),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(event.type.label, color = AppColors.Ink.copy(alpha = 0.66f), fontSize = 11.sp, modifier = Modifier.weight(1f))
                                if (event.completed) Text("Готово", color = AppColors.Ink, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Text(event.title, color = AppColors.Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text(event.subtitle, color = AppColors.Ink.copy(alpha = 0.72f), fontSize = 12.sp)
                            event.timeLabel?.let { time ->
                                Text("Напоминание · $time", color = AppColors.Ink, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        AppFab(
            icon = Icons.Outlined.Add,
            contentDescription = "Добавить событие на выбранную дату",
            onClick = { showCreatePicker = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        )
    }

    if (showFilterSheet) {
        CalendarFilterSheet(
            selectedPeriod = selectedPeriod,
            selectedType = selectedType,
            onSelectPeriod = { selectedPeriod = it },
            onSelectType = { selectedType = it },
            onReset = {
                selectedPeriod = CalendarPeriod.ALL
                selectedType = CalendarTypeFilter.ALL
            },
            onDismiss = { showFilterSheet = false }
        )
    }

    if (showCreatePicker) {
        CalendarCreatePickerSheet(
            dayTitle = CalendarEventFactory.formatDayTitle(selectedDate),
            canToggleHabits = !selectedLocalDate.isAfter(todayDate) && habits.isNotEmpty(),
            onDismiss = { showCreatePicker = false },
            onSelect = { action ->
                showCreatePicker = false
                when (action) {
                    CalendarCreateAction.TASK -> if (onOpenCreateTask != null) onOpenCreateTask(selectedLocalDate) else activeCreateDialog = action
                    CalendarCreateAction.PROJECT -> if (onOpenCreateProject != null) onOpenCreateProject(selectedLocalDate) else activeCreateDialog = action
                    CalendarCreateAction.GOAL -> if (onOpenCreateGoal != null) onOpenCreateGoal(selectedLocalDate) else activeCreateDialog = action
                    CalendarCreateAction.HABITS -> activeCreateDialog = action
                }
            }
        )
    }

    when (activeCreateDialog) {
        CalendarCreateAction.HABITS -> {
            CalendarHabitsDialog(
                dateKey = selectedDate,
                dayTitle = CalendarEventFactory.formatDayTitle(selectedDate),
                habits = habits,
                onDismiss = { activeCreateDialog = null },
                onToggleHabit = { habitId -> onToggleHabitOnDate(habitId, selectedDate) }
            )
        }
        else -> Unit
    }

    selectedTaskForSheet?.let { task ->
        val freshTask = tasks.firstOrNull { it.id == task.id } ?: task
        CalendarTaskActionsSheet(
            task = freshTask,
            onDismiss = { selectedTaskForSheet = null },
            onToggle = {
                onToggleTask(freshTask.id)
                selectedTaskForSheet = null
            },
            onEdit = {
                selectedTaskForSheet = null
                if (onOpenEditTask != null) onOpenEditTask(freshTask.id)
            },
            onOpenDetails = {
                selectedTaskForSheet = null
                onOpenTaskDetails(freshTask.id)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CalendarFilterSheet(
    selectedPeriod: CalendarPeriod,
    selectedType: CalendarTypeFilter,
    onSelectPeriod: (CalendarPeriod) -> Unit,
    onSelectType: (CalendarTypeFilter) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppColors.Background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Фильтры календаря",
                    style = MaterialTheme.typography.headlineMedium,
                    color = AppColors.Ink,
                    modifier = Modifier.weight(1f)
                )
                if (selectedPeriod != CalendarPeriod.ALL || selectedType != CalendarTypeFilter.ALL) {
                    TextButton(onClick = onReset) {
                        Text("Сбросить", color = AppColors.Muted, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text("Период", color = AppColors.Muted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalendarPeriod.values().forEach { period ->
                    FilterPill(
                        label = period.title,
                        selected = selectedPeriod == period,
                        onClick = { onSelectPeriod(period) }
                    )
                }
            }

            Text("Тип событий", color = AppColors.Muted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalendarTypeFilter.values().forEach { filter ->
                    FilterPill(
                        label = filter.title,
                        selected = selectedType == filter,
                        onClick = { onSelectType(filter) }
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Ink, contentColor = AppColors.White),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text("Готово", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarCreatePickerSheet(
    dayTitle: String,
    canToggleHabits: Boolean,
    onDismiss: () -> Unit,
    onSelect: (CalendarCreateAction) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppColors.Background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Событие · $dayTitle",
                style = MaterialTheme.typography.titleLarge,
                color = AppColors.Ink
            )
            CalendarActionRow("Новая задача", Icons.Outlined.CheckCircle) { onSelect(CalendarCreateAction.TASK) }
            CalendarActionRow("Новый проект", Icons.Outlined.FolderOpen) { onSelect(CalendarCreateAction.PROJECT) }
            CalendarActionRow("Новая цель", Icons.Outlined.Flag) { onSelect(CalendarCreateAction.GOAL) }
            if (canToggleHabits) {
                CalendarActionRow("Отметить привычку", Icons.Outlined.Repeat) { onSelect(CalendarCreateAction.HABITS) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarTaskActionsSheet(
    task: DashboardTask,
    onDismiss: () -> Unit,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onOpenDetails: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppColors.Background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(task.title, style = MaterialTheme.typography.titleLarge, color = AppColors.Ink)
            Text(task.dueLabel, color = AppColors.Muted, fontSize = 13.sp)
            Spacer(Modifier.height(4.dp))
            CalendarActionRow(
                label = if (task.isCompleted) "Вернуть в работу" else "Отметить выполненной",
                icon = Icons.Outlined.CheckCircle,
                onClick = onToggle
            )
            CalendarActionRow("Редактировать или перенести", Icons.Outlined.Edit, onEdit)
            CalendarActionRow("Открыть страницу задачи", Icons.Outlined.OpenInNew, onOpenDetails)
        }
    }
}

@Composable
private fun CalendarActionRow(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = AppColors.Ink, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = AppColors.Ink)
    }
}

@Composable
private fun CalendarHabitsDialog(
    dateKey: String,
    dayTitle: String,
    habits: List<DashboardHabit>,
    onDismiss: () -> Unit,
    onToggleHabit: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Привычки · $dayTitle", color = AppColors.Ink) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                habits.forEach { habit ->
                    val checked = dateKey in habit.completionDates
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AppColors.Background)
                            .clickable { onToggleHabit(habit.id) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = checked, onCheckedChange = { onToggleHabit(habit.id) })
                        Spacer(Modifier.width(6.dp))
                        Text(habit.title, color = AppColors.Ink, fontWeight = FontWeight.Medium)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Готово", color = AppColors.Ink) }
        },
        containerColor = AppColors.White
    )
}

@Composable
private fun LegendDot(color: Color, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(color))
        Text(title, color = AppColors.Muted, fontSize = 9.sp, maxLines = 1)
    }
}

@Composable
private fun eventColor(type: CalendarEventType): Color = when (type) {
    CalendarEventType.TASK -> AppColors.PurpleLight
    CalendarEventType.PROJECT -> AppColors.Blue
    CalendarEventType.GOAL -> AppColors.Yellow
    CalendarEventType.HABIT -> AppColors.Pink
    CalendarEventType.ACTIVITY -> AppColors.Mint
}