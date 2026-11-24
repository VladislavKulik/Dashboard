package com.example.lifedashboard.feature.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.data.DateInput
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.data.model.DashboardTask
import com.example.lifedashboard.data.model.TaskDayCategory
import com.example.lifedashboard.data.model.TaskPriority
import com.example.lifedashboard.data.model.TaskRecurrence
import com.example.lifedashboard.ui.components.CircleIconButton
import com.example.lifedashboard.ui.components.DateTimePickerSheet
import com.example.lifedashboard.ui.components.EditorBottomBar
import com.example.lifedashboard.ui.components.FilterPill
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.theme.AppColors
import java.time.LocalDate

private enum class RecurrenceLimitMode(val label: String) {
    UNLIMITED("Бессрочно"),
    UNTIL_DATE("До даты"),
    BY_COUNT("По числу")
}

@Composable
fun TaskCreateScreen(
    projects: List<DashboardProject>,
    onBack: () -> Unit,
    onCreate: (
        title: String,
        priority: TaskPriority,
        projectId: String?,
        dueDate: LocalDate?,
        recurrence: TaskRecurrence,
        recurrenceDays: Set<Int>,
        reminderEnabled: Boolean,
        reminderTime: String,
        recurrenceEndDate: LocalDate?,
        recurrenceRemainingCount: Int?,
        plannedMinutes: Int
    ) -> Unit,
    initialTask: DashboardTask? = null,
    initialProjectId: String? = null,
    initialDueDate: LocalDate? = null,
    onUpdate: (DashboardTask, () -> Unit) -> Unit = { _, cb -> cb() }
) {
    val today = remember { LocalDate.now() }
    val seedDate = initialTask?.dueDate
        ?: initialTask?.let { DateInput.parseLegacyLabel(it.dueLabel) }
        ?: initialDueDate
        ?: today

    var title by rememberSaveable(initialTask?.id) { mutableStateOf(initialTask?.title.orEmpty()) }
    var attemptedSubmit by rememberSaveable { mutableStateOf(false) }
    var dueDate by rememberSaveable(initialTask?.id, initialDueDate) {
        mutableStateOf(
            if (initialTask != null) {
                initialTask.dueDate ?: DateInput.parseLegacyLabel(initialTask.dueLabel)
            } else {
                initialDueDate ?: today
            }
        )
    }
    var dueDateTouched by rememberSaveable(initialTask?.id, initialDueDate) {
        mutableStateOf(initialTask == null || initialDueDate != null)
    }
    var priority by rememberSaveable(initialTask?.id) { mutableStateOf(initialTask?.priority ?: TaskPriority.MEDIUM) }
    var recurrence by rememberSaveable(initialTask?.id) { mutableStateOf(initialTask?.recurrence ?: TaskRecurrence.NONE) }
    var recurrenceDays by remember(initialTask?.id) {
        mutableStateOf(initialTask?.recurrenceDays?.ifEmpty { setOf(seedDate.dayOfWeek.value) } ?: setOf(seedDate.dayOfWeek.value))
    }
    var showMoreRecurrence by rememberSaveable(initialTask?.id) {
        mutableStateOf(initialTask?.recurrence == TaskRecurrence.WEEKLY || initialTask?.recurrence == TaskRecurrence.MONTHLY)
    }
    var limitMode by rememberSaveable(initialTask?.id) {
        mutableStateOf(
            when {
                initialTask?.recurrenceEndDate != null -> RecurrenceLimitMode.UNTIL_DATE
                initialTask?.recurrenceRemainingCount != null -> RecurrenceLimitMode.BY_COUNT
                else -> RecurrenceLimitMode.UNLIMITED
            }
        )
    }
    var recurrenceEndDate by rememberSaveable(initialTask?.id) {
        mutableStateOf(initialTask?.recurrenceEndDate ?: seedDate.plusMonths(1))
    }
    var recurrenceCount by rememberSaveable(initialTask?.id) {
        mutableIntStateOf((initialTask?.recurrenceRemainingCount ?: 5).coerceIn(1, 99))
    }
    var plannedMinutes by rememberSaveable(initialTask?.id) {
        mutableIntStateOf((initialTask?.plannedMinutes ?: 120).coerceIn(5, 24 * 60))
    }
    var projectId by rememberSaveable(initialTask?.id, initialProjectId) {
        mutableStateOf(initialTask?.projectId ?: initialProjectId)
    }
    var reminderEnabled by rememberSaveable(initialTask?.id) { mutableStateOf(initialTask?.reminderEnabled ?: false) }
    var reminderTime by rememberSaveable(initialTask?.id) { mutableStateOf(initialTask?.reminderTime ?: "09:00") }
    var showDateSheet by remember { mutableStateOf(false) }
    var showEndDateSheet by remember { mutableStateOf(false) }

    val timeValid = reminderTime.matches(Regex("(?:[01]\\d|2[0-3]):[0-5]\\d"))
    val titleError = attemptedSubmit && title.isBlank()
    val canSave = title.isNotBlank() &&
        (recurrence == TaskRecurrence.NONE || dueDate != null) &&
        (recurrence != TaskRecurrence.WEEKLY || recurrenceDays.isNotEmpty()) &&
        (!reminderEnabled || (dueDate != null && timeValid))

    fun submit() {
        attemptedSubmit = true
        if (!canSave) return
        val endDate = if (recurrence != TaskRecurrence.NONE && limitMode == RecurrenceLimitMode.UNTIL_DATE) recurrenceEndDate else null
        val remCount = if (recurrence != TaskRecurrence.NONE && limitMode == RecurrenceLimitMode.BY_COUNT) recurrenceCount else null

        if (initialTask == null) {
            onCreate(
                title.trim(),
                priority,
                projectId,
                dueDate,
                recurrence,
                if (recurrence == TaskRecurrence.WEEKLY) recurrenceDays else emptySet(),
                reminderEnabled,
                reminderTime,
                endDate,
                remCount,
                plannedMinutes
            )
            onBack()
        } else {
            val formattedDueLabel = if (!dueDateTouched) initialTask.dueLabel else DateInput.formatTaskLabel(dueDate)
            onUpdate(
                initialTask.copy(
                    title = title.trim(),
                    dueDate = dueDate,
                    dueLabel = formattedDueLabel,
                    priority = priority,
                    dayCategory = DateInput.categoryFor(dueDate) ?: initialTask.dayCategory,
                    projectId = projectId,
                    recurrence = recurrence,
                    recurrenceDays = if (recurrence == TaskRecurrence.WEEKLY) recurrenceDays else emptySet(),
                    recurrenceSeriesId = if (recurrence == TaskRecurrence.NONE) null else (initialTask.recurrenceSeriesId ?: initialTask.id),
                    recurrenceAnchorDay = if (recurrence == TaskRecurrence.MONTHLY) dueDate?.dayOfMonth ?: 0 else 0,
                    reminderEnabled = reminderEnabled && dueDate != null,
                    reminderTime = reminderTime,
                    recurrenceEndDate = endDate,
                    recurrenceRemainingCount = remCount,
                    plannedMinutes = plannedMinutes
                ),
                onBack
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    CircleIconButton(icon = Icons.Outlined.Close, contentDescription = "Закрыть", onClick = onBack)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = if (initialTask == null) "Новая задача" else "Редактировать задачу",
                        style = MaterialTheme.typography.headlineLarge,
                        color = AppColors.Ink
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Название") },
                    placeholder = { Text("Что нужно сделать?") },
                    isError = titleError,
                    supportingText = if (titleError) {
                        { Text("Введите название задачи", color = AppColors.Error) }
                    } else null,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AppColors.White,
                        unfocusedContainerColor = AppColors.White,
                        errorContainerColor = AppColors.White,
                        focusedBorderColor = AppColors.Ink,
                        unfocusedBorderColor = Color.Transparent,
                        errorBorderColor = AppColors.Error
                    )
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Срок", color = AppColors.Muted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterPill("Сегодня", dueDate == today, { dueDate = today; dueDateTouched = true })
                        FilterPill("Завтра", dueDate == today.plusDays(1), { dueDate = today.plusDays(1); dueDateTouched = true })
                        val customLabel = if (dueDate != null && dueDate != today && dueDate != today.plusDays(1)) {
                            DateInput.formatTaskLabel(dueDate!!)
                        } else if (dueDate == null) {
                            "Без срока"
                        } else {
                            "Выбрать дату"
                        }
                        FilterPill(
                            label = customLabel,
                            selected = dueDate != today && dueDate != today.plusDays(1),
                            onClick = { showDateSheet = true }
                        )
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Приоритет", color = AppColors.Muted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CircleShape)
                            .background(AppColors.White)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TaskPriority.values().forEach { option ->
                            val selected = priority == option
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .background(if (selected) AppColors.Ink else Color.Transparent)
                                    .clickable { priority = option }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = option.label,
                                    color = if (selected) AppColors.White else AppColors.Ink,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Плановое время", color = AppColors.Muted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(30 to "30 мин", 60 to "1 ч", 120 to "2 ч", 240 to "4 ч").forEach { (mins, label) ->
                            FilterPill(label, plannedMinutes == mins, { plannedMinutes = mins })
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Повторение", color = AppColors.Muted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterPill("Не повторять", recurrence == TaskRecurrence.NONE, {
                            recurrence = TaskRecurrence.NONE
                            showMoreRecurrence = false
                        })
                        FilterPill("Каждый день", recurrence == TaskRecurrence.DAILY, {
                            recurrence = TaskRecurrence.DAILY
                            showMoreRecurrence = false
                        })
                        FilterPill(
                            label = if (recurrence == TaskRecurrence.WEEKLY || recurrence == TaskRecurrence.MONTHLY) recurrence.label else "Ещё",
                            selected = showMoreRecurrence || recurrence == TaskRecurrence.WEEKLY || recurrence == TaskRecurrence.MONTHLY,
                            onClick = { showMoreRecurrence = !showMoreRecurrence }
                        )
                    }
                    if (showMoreRecurrence || recurrence == TaskRecurrence.WEEKLY || recurrence == TaskRecurrence.MONTHLY) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterPill("Каждую неделю", recurrence == TaskRecurrence.WEEKLY, { recurrence = TaskRecurrence.WEEKLY })
                            FilterPill("Каждый месяц", recurrence == TaskRecurrence.MONTHLY, { recurrence = TaskRecurrence.MONTHLY })
                        }
                    }
                    if (recurrence == TaskRecurrence.WEEKLY) {
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEachIndexed { index, label ->
                                val day = index + 1
                                FilterPill(label, day in recurrenceDays, {
                                    recurrenceDays = if (day in recurrenceDays) recurrenceDays - day else recurrenceDays + day
                                })
                            }
                        }
                    }

                    if (recurrence != TaskRecurrence.NONE) {
                        Text("Завершение серии", color = AppColors.Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RecurrenceLimitMode.values().forEach { mode ->
                                FilterPill(mode.label, limitMode == mode, { limitMode = mode })
                            }
                        }
                        when (limitMode) {
                            RecurrenceLimitMode.UNTIL_DATE -> {
                                PastelCard(
                                    color = AppColors.White,
                                    modifier = Modifier.fillMaxWidth().clickable { showEndDateSheet = true },
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Text("Повторять до", color = AppColors.Muted, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                        Text(DateInput.formatForInput(recurrenceEndDate), color = AppColors.Ink, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }
                            }
                            RecurrenceLimitMode.BY_COUNT -> {
                                PastelCard(color = AppColors.White, contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Text("Осталось повторений", color = AppColors.Ink, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Box(
                                                modifier = Modifier.size(32.dp).clip(CircleShape).background(AppColors.Background)
                                                    .clickable(enabled = recurrenceCount > 1) { recurrenceCount = (recurrenceCount - 1).coerceAtLeast(1) },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Outlined.Remove, contentDescription = "Меньше", tint = AppColors.Ink, modifier = Modifier.size(16.dp))
                                            }
                                            Text(recurrenceCount.toString(), color = AppColors.Ink, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                                            Box(
                                                modifier = Modifier.size(32.dp).clip(CircleShape).background(AppColors.Ink)
                                                    .clickable(enabled = recurrenceCount < 99) { recurrenceCount = (recurrenceCount + 1).coerceAtMost(99) },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Outlined.Add, contentDescription = "Больше", tint = AppColors.White, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                            RecurrenceLimitMode.UNLIMITED -> Unit
                        }
                    }
                }
            }

            if (projects.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Проект", color = AppColors.Muted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterPill("Без проекта", projectId == null, { projectId = null })
                            projects.forEach { project ->
                                FilterPill(project.name, projectId == project.id, { projectId = project.id })
                            }
                        }
                    }
                }
            }

            item {
                PastelCard(
                    color = AppColors.White,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Напомнить", color = AppColors.Ink, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = if (reminderEnabled) "В выбранный день · $reminderTime" else "В выбранный день",
                                color = AppColors.Muted,
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = { reminderEnabled = it },
                            enabled = dueDate != null
                        )
                    }
                    if (reminderEnabled) {
                        OutlinedTextField(
                            value = reminderTime,
                            onValueChange = { reminderTime = it.filter { ch -> ch.isDigit() || ch == ':' }.take(5) },
                            label = { Text("Время (ЧЧ:ММ)") },
                            singleLine = true,
                            isError = !timeValid,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        EditorBottomBar(
            confirmEnabled = title.isNotBlank(),
            onConfirm = ::submit,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            IconButton(onClick = { showDateSheet = true }) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = "Выбрать дату", tint = AppColors.Ink)
            }
            IconButton(onClick = { showMoreRecurrence = !showMoreRecurrence }) {
                Icon(Icons.Outlined.Repeat, contentDescription = "Повторение", tint = AppColors.Ink)
            }
            IconButton(onClick = { if (dueDate != null) reminderEnabled = !reminderEnabled }) {
                Icon(
                    Icons.Outlined.NotificationsNone,
                    contentDescription = "Напоминание",
                    tint = if (reminderEnabled) AppColors.Purple else AppColors.Ink
                )
            }
        }
    }

    if (showDateSheet) {
        DateTimePickerSheet(
            initialDate = dueDate,
            initialTime = reminderTime,
            showTimeField = true,
            allowClear = true,
            onDismiss = { showDateSheet = false },
            onConfirm = { pickedDate, pickedTime ->
                dueDate = pickedDate
                dueDateTouched = true
                reminderTime = pickedTime
                if (pickedDate == null) reminderEnabled = false
                showDateSheet = false
            }
        )
    }

    if (showEndDateSheet) {
        DateTimePickerSheet(
            initialDate = recurrenceEndDate,
            showTimeField = false,
            allowClear = false,
            onDismiss = { showEndDateSheet = false },
            onConfirm = { pickedDate, _ ->
                if (pickedDate != null) recurrenceEndDate = pickedDate
                showEndDateSheet = false
            }
        )
    }
}