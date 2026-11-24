package com.example.lifedashboard.feature.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.data.DateInput
import com.example.lifedashboard.data.model.DashboardGoal
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.data.model.GoalColor
import com.example.lifedashboard.ui.components.CircleIconButton
import com.example.lifedashboard.ui.components.DateTimePickerSheet
import com.example.lifedashboard.ui.components.EditorBottomBar
import com.example.lifedashboard.ui.components.FilterPill
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.theme.AppColors
import java.time.LocalDate
import java.util.UUID

@Composable
fun GoalEditorScreen(
    initialGoal: DashboardGoal?,
    projects: List<DashboardProject>,
    onBack: () -> Unit,
    onSave: (DashboardGoal, (() -> Unit)?) -> Unit,
    operationError: String? = null,
    initialProjectId: String? = null,
    initialDueDate: LocalDate? = null
) {
    var title by rememberSaveable(initialGoal?.id) { mutableStateOf(initialGoal?.title.orEmpty()) }
    var description by rememberSaveable(initialGoal?.id) { mutableStateOf(initialGoal?.description.orEmpty()) }
    var currentValue by rememberSaveable(initialGoal?.id) { mutableStateOf((initialGoal?.currentValue ?: 0).toString()) }
    var targetValue by rememberSaveable(initialGoal?.id) { mutableStateOf((initialGoal?.targetValue ?: 10).toString()) }
    var unit by rememberSaveable(initialGoal?.id) { mutableStateOf(initialGoal?.unit ?: "раз") }
    var dueDate by rememberSaveable(initialGoal?.id, initialDueDate) { mutableStateOf(initialGoal?.dueDate ?: initialDueDate) }
    var projectId by rememberSaveable(initialGoal?.id, initialProjectId) { mutableStateOf(initialGoal?.projectId ?: initialProjectId) }
    var color by rememberSaveable(initialGoal?.id) { mutableStateOf(initialGoal?.color ?: GoalColor.PINK) }
    var attemptedSubmit by rememberSaveable { mutableStateOf(false) }
    var showDateSheet by remember { mutableStateOf(false) }

    val parsedCurrent = currentValue.toIntOrNull()
    val parsedTarget = targetValue.toIntOrNull()
    val progressValid = parsedCurrent != null && parsedTarget != null &&
        parsedTarget in 1..1_000_000 && parsedCurrent in 0..parsedTarget
    val canSave = title.isNotBlank() && progressValid

    fun submit() {
        attemptedSubmit = true
        if (!canSave || parsedCurrent == null || parsedTarget == null) return
        onSave(
            DashboardGoal(
                id = initialGoal?.id ?: UUID.randomUUID().toString(),
                title = title.trim(),
                description = description.trim(),
                dueDate = dueDate,
                currentValue = parsedCurrent,
                targetValue = parsedTarget,
                unit = unit.trim().ifBlank { "раз" },
                projectId = projectId,
                color = color,
                sortOrder = initialGoal?.sortOrder ?: 0
            ),
            onBack
        )
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
                        text = if (initialGoal == null) "Новая цель" else "Редактировать цель",
                        style = MaterialTheme.typography.headlineLarge,
                        color = AppColors.Ink
                    )
                }
            }

            if (!operationError.isNullOrBlank()) {
                item { Text(operationError, color = AppColors.Error, fontSize = 12.sp) }
            }

            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Название") },
                    placeholder = { Text("Название цели") },
                    isError = attemptedSubmit && title.isBlank(),
                    supportingText = if (attemptedSubmit && title.isBlank()) {
                        { Text("Введите название цели", color = AppColors.Error) }
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
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    label = { Text("Описание") },
                    placeholder = { Text("Зачем это нужно") },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AppColors.White,
                        unfocusedContainerColor = AppColors.White,
                        focusedBorderColor = AppColors.Ink,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = currentValue,
                        onValueChange = { currentValue = it.filter(Char::isDigit).take(7) },
                        label = { Text("Сейчас") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = AppColors.White,
                            unfocusedContainerColor = AppColors.White,
                            focusedBorderColor = AppColors.Ink,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = targetValue,
                        onValueChange = { targetValue = it.filter(Char::isDigit).take(7) },
                        label = { Text("Цель") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = AppColors.White,
                            unfocusedContainerColor = AppColors.White,
                            focusedBorderColor = AppColors.Ink,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it.take(24) },
                        label = { Text("Единица") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = AppColors.White,
                            unfocusedContainerColor = AppColors.White,
                            focusedBorderColor = AppColors.Ink,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
                if (attemptedSubmit && !progressValid) {
                    Text("Проверь значения: 0 ≤ Сейчас ≤ Цель", color = AppColors.Error, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }

            item {
                PastelCard(
                    color = AppColors.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDateSheet = true },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Срок", color = AppColors.Muted, fontSize = 11.sp)
                            Text(
                                text = dueDate?.let { DateInput.formatForInput(it) } ?: "Выбрать дату",
                                color = if (dueDate == null) AppColors.Muted else AppColors.Ink,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Icon(Icons.Outlined.CalendarMonth, contentDescription = "Выбрать дату", tint = AppColors.Ink)
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
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Цвет карточки", color = AppColors.Muted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GoalColor.values().forEach { option ->
                            val selected = color == option
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(goalCardColor(option))
                                    .then(if (selected) Modifier.border(2.dp, AppColors.Ink, CircleShape) else Modifier)
                                    .clickable { color = option },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selected) {
                                    Icon(Icons.Outlined.Check, contentDescription = "Выбрано", tint = AppColors.Ink, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        EditorBottomBar(
            confirmEnabled = canSave,
            onConfirm = ::submit,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            IconButton(onClick = { showDateSheet = true }) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = "Срок", tint = AppColors.Ink)
            }
            IconButton(onClick = {
                val values = GoalColor.values()
                color = values[(color.ordinal + 1) % values.size]
            }) {
                Icon(Icons.Outlined.Flag, contentDescription = "Сменить цвет", tint = AppColors.Ink)
            }
        }
    }

    if (showDateSheet) {
        DateTimePickerSheet(
            initialDate = dueDate,
            showTimeField = false,
            allowClear = true,
            onDismiss = { showDateSheet = false },
            onConfirm = { pickedDate, _ ->
                dueDate = pickedDate
                showDateSheet = false
            }
        )
    }
}