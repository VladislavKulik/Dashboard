package com.example.lifedashboard.feature.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.data.model.DashboardGoal
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.data.model.GoalColor
import com.example.lifedashboard.ui.components.FilterPill
import com.example.lifedashboard.ui.components.LocalDatePickerField
import com.example.lifedashboard.ui.theme.AppColors
import java.time.LocalDate
import java.util.UUID

@Composable
fun GoalEditorDialog(
    initialGoal: DashboardGoal?,
    projects: List<DashboardProject>,
    operationError: String?,
    onDismiss: () -> Unit,
    onSave: (DashboardGoal) -> Unit,
    initialDueDate: LocalDate? = null
) {
    var title by remember(initialGoal?.id) { mutableStateOf(initialGoal?.title.orEmpty()) }
    var description by remember(initialGoal?.id) { mutableStateOf(initialGoal?.description.orEmpty()) }
    var currentValue by remember(initialGoal?.id) { mutableStateOf((initialGoal?.currentValue ?: 0).toString()) }
    var targetValue by remember(initialGoal?.id) { mutableStateOf((initialGoal?.targetValue ?: 10).toString()) }
    var unit by remember(initialGoal?.id) { mutableStateOf(initialGoal?.unit ?: "раз") }
    var dueDate by remember(initialGoal?.id, initialDueDate) { mutableStateOf(initialGoal?.dueDate ?: initialDueDate) }
    var projectId by remember(initialGoal?.id) { mutableStateOf(initialGoal?.projectId) }
    var color by remember(initialGoal?.id) { mutableStateOf(initialGoal?.color ?: GoalColor.BLUE) }
    var localError by remember(initialGoal?.id) { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialGoal == null) "Новая цель" else "Изменить цель") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 430.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = currentValue,
                        onValueChange = { currentValue = it.filter(Char::isDigit).take(7) },
                        label = { Text("Сейчас") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = targetValue,
                        onValueChange = { targetValue = it.filter(Char::isDigit).take(7) },
                        label = { Text("Цель") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it.take(24) },
                    label = { Text("Единица измерения") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                LocalDatePickerField(
                    date = dueDate,
                    label = "Срок достижения",
                    onDateChange = { dueDate = it }
                )
                if (projects.isNotEmpty()) {
                    Text("Связанный проект", color = AppColors.Muted, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterPill(label = "Без проекта", selected = projectId == null, onClick = { projectId = null })
                        projects.forEach { project ->
                            FilterPill(label = project.name, selected = projectId == project.id, onClick = { projectId = project.id })
                        }
                    }
                }
                Text("Цвет карточки", color = AppColors.Muted, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    GoalColor.values().forEach { option ->
                        Box(
                            modifier = Modifier
                                .size(if (color == option) 38.dp else 32.dp)
                                .clip(CircleShape)
                                .background(goalCardColor(option))
                                .clickable { color = option },
                            contentAlignment = Alignment.Center
                        ) {
                            if (color == option) {
                                Box(
                                    modifier = Modifier.size(10.dp).clip(CircleShape).background(AppColors.Ink)
                                )
                            }
                        }
                    }
                }
                (localError ?: operationError)?.let { Text(it, color = AppColors.Error, fontSize = 12.sp) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val current = currentValue.toIntOrNull()
                val target = targetValue.toIntOrNull()
                localError = when {
                    title.isBlank() -> "Укажи название цели."
                    current == null || target == null || target !in 1..1_000_000 || current !in 0..target -> "Проверь значения прогресса."
                    else -> null
                }
                if (localError == null && current != null && target != null) {
                    onSave(
                        DashboardGoal(
                            id = initialGoal?.id ?: UUID.randomUUID().toString(),
                            title = title,
                            description = description,
                            dueDate = dueDate,
                            currentValue = current,
                            targetValue = target,
                            unit = unit,
                            projectId = projectId,
                            color = color,
                            sortOrder = initialGoal?.sortOrder ?: 0
                        )
                    )
                }
            }) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

@Composable
internal fun goalCardColor(color: GoalColor): Color = when (color) {
    GoalColor.BLUE -> AppColors.Blue
    GoalColor.MINT -> AppColors.Mint
    GoalColor.YELLOW -> AppColors.Yellow
    GoalColor.PINK -> AppColors.Pink
    GoalColor.LAVENDER -> AppColors.Lavender
}