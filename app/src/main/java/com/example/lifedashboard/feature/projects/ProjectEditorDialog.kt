package com.example.lifedashboard.feature.projects

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.data.DateInput
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.data.model.ProjectStage
import com.example.lifedashboard.ui.components.FilterPill
import com.example.lifedashboard.ui.components.LocalDatePickerField
import com.example.lifedashboard.ui.theme.AppColors
import java.time.LocalDate
import java.util.UUID

@Composable
fun ProjectEditorDialog(
    initialProject: DashboardProject?,
    onDismiss: () -> Unit,
    onSave: (DashboardProject) -> Unit,
    operationError: String? = null,
    initialDeadlineDate: LocalDate? = null
) {
    val defaultColorArgb = AppColors.Lavender.toArgb()
    var name by remember(initialProject?.id) { mutableStateOf(initialProject?.name.orEmpty()) }
    var description by remember(initialProject?.id) { mutableStateOf(initialProject?.description.orEmpty()) }
    var deadlineDate by remember(initialProject?.id, initialDeadlineDate) {
        mutableStateOf(
            initialProject?.deadlineDate
                ?: DateInput.parseLegacyLabel(initialProject?.deadlineLabel)
                ?: initialDeadlineDate
        )
    }
    var deadlineDateTouched by remember(initialProject?.id, initialDeadlineDate) {
        mutableStateOf(initialProject == null && initialDeadlineDate != null)
    }
    var stageLines by remember(initialProject?.id) {
        mutableStateOf(initialProject?.stages?.joinToString("\n") { it.title }.orEmpty())
    }
    var colorArgb by remember(initialProject?.id) {
        mutableStateOf(initialProject?.colorArgb ?: defaultColorArgb)
    }

    val colors = listOf(
        "Жёлтый" to AppColors.Yellow,
        "Лавандовый" to AppColors.Lavender,
        "Мятный" to AppColors.Mint,
        "Розовый" to AppColors.Pink,
        "Голубой" to AppColors.Blue
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialProject == null) "Новый проект" else "Редактировать проект", color = AppColors.Ink) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 460.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Название") }
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    label = { Text("Описание") }
                )
                LocalDatePickerField(
                    date = deadlineDate,
                    label = "Дедлайн проекта",
                    onDateChange = { deadlineDate = it; deadlineDateTouched = true }
                )
                OutlinedTextField(
                    value = stageLines,
                    onValueChange = { stageLines = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    label = { Text("Этапы проекта") },
                    supportingText = { Text("Каждый этап — с новой строки") }
                )
                if (!operationError.isNullOrBlank()) {
                    Text(operationError, color = AppColors.Error, fontSize = 12.sp)
                }
                Text("Цвет карточки", color = AppColors.Muted, fontSize = 12.sp)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    colors.forEach { (label, color) ->
                        FilterPill(
                            label = label,
                            selected = colorArgb == color.toArgb(),
                            onClick = { colorArgb = color.toArgb() }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val oldStages = initialProject?.stages.orEmpty()
                    val titles = stageLines.lines().map { it.trim() }.filter { it.isNotBlank() }.distinct()
                    val preserveByIndex = titles.size == oldStages.size
                    val stages = titles.mapIndexed { index, title ->
                        val old = oldStages.firstOrNull { it.title.equals(title, ignoreCase = true) }
                            ?: if (preserveByIndex) oldStages.getOrNull(index) else null
                        ProjectStage(
                            id = old?.id ?: UUID.randomUUID().toString(),
                            title = title,
                            isCompleted = old?.isCompleted ?: false,
                            isCurrent = false
                        )
                    }.let { values ->
                        val currentIndex = values.indexOfFirst { !it.isCompleted }
                        values.mapIndexed { index, stage -> stage.copy(isCurrent = index == currentIndex) }
                    }
                    val completed = stages.count { it.isCompleted }
                    val currentIndex = stages.indexOfFirst { it.isCurrent }
                    val stageLabel = when {
                        stages.isEmpty() -> "Без этапов"
                        completed == stages.size -> "Все этапы завершены"
                        else -> "Этап ${currentIndex + 1} из ${stages.size}"
                    }
                    val base = initialProject
                    val allStagesCompleted = stages.isNotEmpty() && completed == stages.size
                    val allTasksCompleted = base != null && base.totalTasks > 0 && base.completedTasks == base.totalTasks
                    onSave(
                        DashboardProject(
                            id = base?.id ?: UUID.randomUUID().toString(),
                            name = name.trim(),
                            description = description.trim(),
                            deadlineLabel = if (deadlineDateTouched) DateInput.formatTaskLabel(deadlineDate) else (initialProject?.deadlineLabel ?: DateInput.formatTaskLabel(deadlineDate)),
                            deadlineDate = deadlineDate ?: if (!deadlineDateTouched) DateInput.parseLegacyLabel(initialProject?.deadlineLabel) else null,
                            stageLabel = stageLabel,
                            progress = base?.progress ?: 0f,
                            completedTasks = base?.completedTasks ?: 0,
                            totalTasks = base?.totalTasks ?: 0,
                            colorArgb = colorArgb,
                            isCompleted = allStagesCompleted || allTasksCompleted,
                            stages = stages
                        )
                    )
                },
                enabled = name.isNotBlank()
            ) { Text("Сохранить", color = AppColors.Ink) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена", color = AppColors.Muted) } },
        containerColor = AppColors.White
    )
}