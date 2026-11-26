package com.example.lifedashboard.feature.tasks

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
import com.example.lifedashboard.ui.theme.AppColors
import java.time.LocalDate

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAdd: (String, TaskPriority, String?, LocalDate?, TaskRecurrence, Set<Int>, Boolean, String) -> Unit,
    projects: List<DashboardProject> = emptyList(),
    initialProjectId: String? = null,
    initialDueDate: LocalDate? = LocalDate.now()
) {
    val seedDate = initialDueDate ?: LocalDate.now()
    var title by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var projectId by remember(initialProjectId) { mutableStateOf(initialProjectId) }
    var dueDate by remember(initialDueDate) { mutableStateOf(initialDueDate) }
    var recurrence by remember { mutableStateOf(TaskRecurrence.NONE) }
    var recurrenceDays by remember(seedDate) { mutableStateOf(setOf(seedDate.dayOfWeek.value)) }
    var reminderEnabled by remember { mutableStateOf(false) }
    var reminderTime by remember { mutableStateOf("09:00") }

    val timeValid = reminderTime.matches(Regex("(?:[01]\\d|2[0-3]):[0-5]\\d"))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая задача", color = AppColors.Ink) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Название") },
                    placeholder = { Text("Что нужно сделать?") }
                )
                LocalDatePickerField(
                    date = dueDate,
                    label = "Срок выполнения",
                    onDateChange = {
                        dueDate = it
                        if (it == null) reminderEnabled = false
                    }
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = reminderEnabled,
                        onCheckedChange = { reminderEnabled = it },
                        enabled = dueDate != null
                    )
                    Column {
                        Text("Напомнить о задаче", color = AppColors.Ink, fontSize = 13.sp)
                        Text("Уведомление в выбранный день", color = AppColors.Muted, fontSize = 11.sp)
                    }
                }
                if (reminderEnabled) {
                    OutlinedTextField(
                        value = reminderTime,
                        onValueChange = { reminderTime = it },
                        label = { Text("Время (ЧЧ:ММ)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (!timeValid) {
                        Text("Укажи время в формате 09:30", color = AppColors.Error, fontSize = 11.sp)
                    }
                }
                Text("Повторение", color = AppColors.Muted, fontSize = 12.sp)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TaskRecurrence.values().forEach { option ->
                        FilterPill(stringResource(id = option.labelResId), recurrence == option, { recurrence = option })
                    }
                }
                if (recurrence == TaskRecurrence.WEEKLY) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEachIndexed { index, label ->
                            val day = index + 1
                            FilterPill(
                                label = label,
                                selected = day in recurrenceDays,
                                onClick = {
                                    recurrenceDays = if (day in recurrenceDays) recurrenceDays - day else recurrenceDays + day
                                }
                            )
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
                            label = stringResource(id = option.labelResId),
                            selected = priority == option,
                            onClick = { priority = option }
                        )
                    }
                }
                if (projects.isNotEmpty()) {
                    Text("Проект", color = AppColors.Muted, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterPill("Без проекта", projectId == null, { projectId = null })
                        projects.forEach { project ->
                            FilterPill(project.name, projectId == project.id, { projectId = project.id })
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onAdd(
                        title,
                        priority,
                        projectId,
                        dueDate,
                        recurrence,
                        if (recurrence == TaskRecurrence.WEEKLY) recurrenceDays else emptySet(),
                        reminderEnabled,
                        reminderTime
                    )
                },
                enabled = title.isNotBlank() &&
                        (recurrence == TaskRecurrence.NONE || dueDate != null) &&
                        (recurrence != TaskRecurrence.WEEKLY || recurrenceDays.isNotEmpty()) &&
                        (!reminderEnabled || (dueDate != null && timeValid))
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
fun EditTaskDialog(
    task: DashboardTask,
    projects: List<DashboardProject>,
    onDismiss: () -> Unit,
    onSave: (DashboardTask) -> Unit,
    operationError: String? = null
) {
    val context = LocalContext.current
    var title by remember(task.id) { mutableStateOf(task.title) }
    var dueDate by remember(task.id) { mutableStateOf(task.dueDate ?: DateInput.parseLegacyLabel(task.dueLabel)) }
    var dueDateTouched by remember(task.id) { mutableStateOf(false) }
    var priority by remember(task.id) { mutableStateOf(task.priority) }
    var dayCategory by remember(task.id) { mutableStateOf(task.dayCategory) }
    var projectId by remember(task.id) { mutableStateOf(task.projectId) }
    var recurrence by remember(task.id) { mutableStateOf(task.recurrence) }
    var recurrenceDays by remember(task.id) {
        mutableStateOf(task.recurrenceDays.ifEmpty { setOf(LocalDate.now().dayOfWeek.value) })
    }
    var reminderEnabled by remember(task.id) { mutableStateOf(task.reminderEnabled) }
    var reminderTime by remember(task.id) { mutableStateOf(task.reminderTime) }

    val timeValid = reminderTime.matches(Regex("(?:[01]\\d|2[0-3]):[0-5]\\d"))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактировать задачу", color = AppColors.Ink) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
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
                    onDateChange = {
                        dueDate = it
                        dueDateTouched = true
                        if (it == null) reminderEnabled = false
                    }
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = reminderEnabled,
                        onCheckedChange = { reminderEnabled = it },
                        enabled = dueDate != null
                    )
                    Text("Напомнить о задаче", color = AppColors.Ink, fontSize = 13.sp)
                }
                if (reminderEnabled) {
                    OutlinedTextField(
                        value = reminderTime,
                        onValueChange = { reminderTime = it },
                        label = { Text("Время (ЧЧ:ММ)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (!timeValid) {
                        Text("Укажи время в формате 09:30", color = AppColors.Error, fontSize = 11.sp)
                    }
                }
                Text("Повторение", color = AppColors.Muted, fontSize = 12.sp)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TaskRecurrence.values().forEach { option ->
                        FilterPill(stringResource(id = option.labelResId), recurrence == option, { recurrence = option })
                    }
                }
                if (recurrence == TaskRecurrence.WEEKLY) {
                    Text("Повторять по дням", color = AppColors.Muted, fontSize = 12.sp)
                    val dayLabels = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        dayLabels.forEachIndexed { index, label ->
                            val day = index + 1
                            FilterPill(label, day in recurrenceDays, {
                                recurrenceDays = if (day in recurrenceDays) recurrenceDays - day else recurrenceDays + day
                            })
                        }
                    }
                    if (recurrenceDays.isEmpty()) {
                        Text("Выбери хотя бы один день недели.", color = AppColors.Error, fontSize = 11.sp)
                    }
                }
                if (recurrence != TaskRecurrence.NONE && dueDate == null) {
                    Text("Для повторения необходимо указать дату первого выполнения.", color = AppColors.Error, fontSize = 11.sp)
                }
                Text("Приоритет", color = AppColors.Muted, fontSize = 12.sp)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TaskPriority.values().forEach { option ->
                        FilterPill(stringResource(id = option.labelResId), priority == option, { priority = option })
                    }
                }
                Text("Категория", color = AppColors.Muted, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterPill("Сегодня", dayCategory == TaskDayCategory.TODAY, { dayCategory = TaskDayCategory.TODAY })
                    FilterPill("Предстоящие", dayCategory == TaskDayCategory.UPCOMING, { dayCategory = TaskDayCategory.UPCOMING })
                }
                if (!operationError.isNullOrBlank()) {
                    Text(operationError, color = AppColors.Error, fontSize = 12.sp)
                }
                Text("Проект", color = AppColors.Muted, fontSize = 12.sp)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
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
                    val formattedDueLabel = if (!dueDateTouched) {
                        task.dueLabel
                    } else if (resolvedDate == null) {
                        "Без срока"
                    } else {
                        listOf(DateInput.formatTaskLabel(context, resolvedDate), timePart)
                            .filter(String::isNotBlank)
                            .joinToString(" · ")
                    }
                    onSave(
                        task.copy(
                            title = title.trim(),
                            dueDate = resolvedDate,
                            dueLabel = formattedDueLabel,
                            priority = priority,
                            dayCategory = DateInput.categoryFor(resolvedDate) ?: dayCategory,
                            projectId = projectId,
                            recurrence = recurrence,
                            recurrenceDays = if (recurrence == TaskRecurrence.WEEKLY) recurrenceDays else emptySet(),
                            recurrenceSeriesId = if (recurrence == TaskRecurrence.NONE) null else (task.recurrenceSeriesId ?: task.id),
                            reminderEnabled = reminderEnabled && resolvedDate != null,
                            reminderTime = reminderTime,
                            recurrenceAnchorDay = if (recurrence == TaskRecurrence.MONTHLY) {
                                if (dueDateTouched) resolvedDate?.dayOfMonth ?: 0
                                else task.recurrenceAnchorDay.takeIf { it in 1..31 } ?: (resolvedDate?.dayOfMonth ?: 0)
                            } else 0
                        )
                    )
                },
                enabled = title.isNotBlank() &&
                        (recurrence == TaskRecurrence.NONE || dueDate != null) &&
                        (recurrence != TaskRecurrence.WEEKLY || recurrenceDays.isNotEmpty()) &&
                        (!reminderEnabled || (dueDate != null && timeValid))
            ) {
                Text("Сохранить", color = AppColors.Ink)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена", color = AppColors.Muted) }
        },
        containerColor = AppColors.White
    )
}