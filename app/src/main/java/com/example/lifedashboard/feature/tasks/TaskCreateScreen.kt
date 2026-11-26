package com.example.lifedashboard.feature.tasks

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.data.DateInput
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.data.model.DashboardTask
import com.example.lifedashboard.data.model.TaskPriority
import com.example.lifedashboard.data.model.TaskRecurrence
import com.example.lifedashboard.presentation.TaskTimerState
import com.example.lifedashboard.ui.components.CircleIconButton
import com.example.lifedashboard.ui.components.EditorBottomBar
import com.example.lifedashboard.ui.components.EditorPropertyChip
import com.example.lifedashboard.ui.components.EditorToolButton
import com.example.lifedashboard.ui.components.FilterPill
import com.example.lifedashboard.ui.components.ProjectPickerSheet
import com.example.lifedashboard.ui.components.DateTimePickerSheet
import com.example.lifedashboard.ui.theme.AppColors
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
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
    val context = LocalContext.current
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
    var recurrenceEndDate by rememberSaveable(initialTask?.id) { mutableStateOf(initialTask?.recurrenceEndDate) }
    var recurrenceRemainingCount by rememberSaveable(initialTask?.id) { mutableStateOf(initialTask?.recurrenceRemainingCount) }
    var plannedMinutes by rememberSaveable(initialTask?.id) {
        mutableIntStateOf((initialTask?.plannedMinutes ?: 120).coerceIn(5, 24 * 60))
    }
    var projectId by rememberSaveable(initialTask?.id, initialProjectId) {
        mutableStateOf(initialTask?.projectId ?: initialProjectId)
    }
    var reminderEnabled by rememberSaveable(initialTask?.id) { mutableStateOf(initialTask?.reminderEnabled ?: false) }
    var reminderTime by rememberSaveable(initialTask?.id) { mutableStateOf(initialTask?.reminderTime ?: "09:00") }

    var showScheduleSheet by remember { mutableStateOf(false) }
    var showProjectSheet by remember { mutableStateOf(false) }
    var showMoreSheet by remember { mutableStateOf(false) }

    val linkedProject = remember(projectId, projects) {
        projects.firstOrNull { it.id == projectId }
    }

    val timeValid = reminderTime.matches(Regex("(?:[01]\\d|2[0-3]):[0-5]\\d"))
    val titleError = attemptedSubmit && title.isBlank()
    val canSave = title.isNotBlank() &&
        (recurrence == TaskRecurrence.NONE || dueDate != null) &&
        (recurrence != TaskRecurrence.WEEKLY || recurrenceDays.isNotEmpty()) &&
        (!reminderEnabled || (dueDate != null && timeValid))

    fun submit() {
        attemptedSubmit = true
        if (!canSave) return

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
                recurrenceEndDate.takeIf { recurrence != TaskRecurrence.NONE },
                recurrenceRemainingCount.takeIf { recurrence != TaskRecurrence.NONE },
                plannedMinutes
            )
            onBack()
        } else {
            val formattedDueLabel = if (!dueDateTouched) initialTask.dueLabel else DateInput.formatTaskLabel(context, dueDate)
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
                    recurrenceEndDate = recurrenceEndDate.takeIf { recurrence != TaskRecurrence.NONE },
                    recurrenceRemainingCount = recurrenceRemainingCount.takeIf { recurrence != TaskRecurrence.NONE },
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
                                    text = stringResource(id = option.labelResId),
                                    color = if (selected) AppColors.White else AppColors.Ink,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Active properties summary chips
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EditorPropertyChip(
                        icon = Icons.Outlined.CalendarMonth,
                        label = dueDate?.let { DateInput.formatTaskLabel(context, it) } ?: "Без срока",
                        backgroundColor = if (dueDate != null) AppColors.Blue.copy(alpha = 0.65f) else AppColors.White,
                        onClick = { showScheduleSheet = true },
                        onClear = if (dueDate != null && recurrence == TaskRecurrence.NONE) ({
                            dueDate = null
                            dueDateTouched = true
                            reminderEnabled = false
                        }) else null
                    )
                    if (reminderEnabled) {
                        EditorPropertyChip(
                            icon = Icons.Outlined.NotificationsNone,
                            label = reminderTime,
                            backgroundColor = AppColors.Yellow,
                            onClick = { showScheduleSheet = true },
                            onClear = { reminderEnabled = false }
                        )
                    }
                    if (recurrence != TaskRecurrence.NONE) {
                        EditorPropertyChip(
                            icon = Icons.Outlined.Repeat,
                            label = stringResource(id = recurrence.labelResId),
                            backgroundColor = AppColors.Lavender,
                            onClick = { showScheduleSheet = true },
                            onClear = {
                                recurrence = TaskRecurrence.NONE
                                recurrenceEndDate = null
                                recurrenceRemainingCount = null
                            }
                        )
                    }
                    if (linkedProject != null) {
                        EditorPropertyChip(
                            icon = Icons.Outlined.FolderOpen,
                            label = linkedProject.name,
                            backgroundColor = Color(linkedProject.colorArgb).copy(alpha = 0.65f),
                            onClick = { showProjectSheet = true },
                            onClear = { projectId = null }
                        )
                    }
                    if (plannedMinutes != 120) {
                        EditorPropertyChip(
                            icon = Icons.Outlined.AccessTime,
                            label = "План: ${TaskTimerState.formatShort(plannedMinutes * 60L)}",
                            onClick = { showMoreSheet = true }
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
                    FilterPill("Сегодня", dueDate == today, { dueDate = today; dueDateTouched = true })
                    FilterPill("Завтра", dueDate == today.plusDays(1), { dueDate = today.plusDays(1); dueDateTouched = true })
                    FilterPill("Через неделю", dueDate == today.plusWeeks(1), { dueDate = today.plusWeeks(1); dueDateTouched = true })
                }
            }
        }

        EditorBottomBar(
            confirmEnabled = canSave,
            onConfirm = ::submit,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            // Single unified Schedule button (Date + Reminder Time + Recurrence)
            EditorToolButton(
                icon = Icons.Outlined.CalendarMonth,
                contentDescription = "Срок, время и повтор",
                isActive = dueDate != null || reminderEnabled || recurrence != TaskRecurrence.NONE,
                onClick = { showScheduleSheet = true }
            )
            if (projects.isNotEmpty()) {
                EditorToolButton(
                    icon = Icons.Outlined.FolderOpen,
                    contentDescription = "Привязать к проекту",
                    isActive = projectId != null,
                    onClick = { showProjectSheet = true }
                )
            }
            EditorToolButton(
                icon = Icons.Outlined.MoreVert,
                contentDescription = "Дополнительно (плановое время)",
                isActive = plannedMinutes != 120,
                onClick = { showMoreSheet = true }
            )
        }
    }

    if (showScheduleSheet) {
        DateTimePickerSheet(
            initialDate = dueDate,
            initialTime = reminderTime,
            showTimeField = true,
            allowClear = true,
            onDismiss = { showScheduleSheet = false },
            onConfirm = { pickedDate, pickedTime ->
                dueDate = pickedDate
                dueDateTouched = true
                reminderTime = pickedTime
                reminderEnabled = pickedDate != null
                showScheduleSheet = false
            }
        )
    }

    if (showProjectSheet) {
        ProjectPickerSheet(
            projects = projects,
            selectedProjectId = projectId,
            onSelectProject = { projectId = it },
            onDismiss = { showProjectSheet = false }
        )
    }

    if (showMoreSheet) {
        TaskMoreOptionsSheet(
            plannedMinutes = plannedMinutes,
            onSelectPlannedMinutes = {
                plannedMinutes = it
                showMoreSheet = false
            },
            onDismiss = { showMoreSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun TaskMoreOptionsSheet(
    plannedMinutes: Int,
    onSelectPlannedMinutes: (Int) -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Плановое время фокуса", style = MaterialTheme.typography.headlineMedium, color = AppColors.Ink)
            Text("Отображается в таймере задачи", color = AppColors.Muted, fontSize = 13.sp)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    15 to "15 мин",
                    30 to "30 мин",
                    45 to "45 мин",
                    60 to "1 час",
                    90 to "1.5 часа",
                    120 to "2 часа",
                    180 to "3 часа",
                    240 to "4 часа"
                ).forEach { (mins, label) ->
                    FilterPill(
                        label = label,
                        selected = plannedMinutes == mins,
                        onClick = { onSelectPlannedMinutes(mins) }
                    )
                }
            }
        }
    }
}