package com.example.lifedashboard.feature.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.data.DateInput
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.ui.components.CircleIconButton
import com.example.lifedashboard.ui.components.DateTimePickerSheet
import com.example.lifedashboard.ui.components.EditorBottomBar
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.theme.AppColors
import java.time.LocalDate
import java.util.UUID

@Composable
fun ProjectEditorScreen(
    initialProject: DashboardProject?,
    onBack: () -> Unit,
    onSave: (DashboardProject, () -> Unit) -> Unit,
    operationError: String? = null,
    initialDeadlineDate: LocalDate? = null
) {
    val defaultArgb = AppColors.Lavender.toArgb()
    var name by rememberSaveable(initialProject?.id) { mutableStateOf(initialProject?.name.orEmpty()) }
    var description by rememberSaveable(initialProject?.id) { mutableStateOf(initialProject?.description.orEmpty()) }
    var attemptedSubmit by rememberSaveable { mutableStateOf(false) }
    var deadlineDate by rememberSaveable(initialProject?.id, initialDeadlineDate) {
        mutableStateOf(
            initialProject?.deadlineDate
                ?: DateInput.parseLegacyLabel(initialProject?.deadlineLabel)
                ?: initialDeadlineDate
        )
    }
    var deadlineTouched by rememberSaveable(initialProject?.id, initialDeadlineDate) {
        mutableStateOf(initialProject == null && initialDeadlineDate != null)
    }
    var colorArgb by rememberSaveable(initialProject?.id) { mutableIntStateOf(initialProject?.colorArgb ?: defaultArgb) }
    var showDateSheet by remember { mutableStateOf(false) }

    val paletteOptions = listOf(
        AppColors.Yellow,
        AppColors.Pink,
        AppColors.Mint,
        AppColors.Lavender,
        AppColors.Blue
    )

    fun saveProject() {
        attemptedSubmit = true
        if (name.isBlank()) return
        val resolvedDeadline = deadlineDate ?: if (!deadlineTouched) DateInput.parseLegacyLabel(initialProject?.deadlineLabel) else null
        val resolvedLabel = if (deadlineTouched) {
            DateInput.formatTaskLabel(resolvedDeadline)
        } else {
            initialProject?.deadlineLabel ?: DateInput.formatTaskLabel(resolvedDeadline)
        }
        val allTasksCompleted = initialProject != null &&
            initialProject.totalTasks > 0 &&
            initialProject.completedTasks == initialProject.totalTasks

        onSave(
            DashboardProject(
                id = initialProject?.id ?: UUID.randomUUID().toString(),
                name = name.trim(),
                description = description.trim(),
                deadlineLabel = resolvedLabel,
                deadlineDate = resolvedDeadline,
                stageLabel = "",
                progress = initialProject?.progress ?: 0f,
                completedTasks = initialProject?.completedTasks ?: 0,
                totalTasks = initialProject?.totalTasks ?: 0,
                colorArgb = colorArgb,
                isCompleted = initialProject?.isCompleted ?: allTasksCompleted,
                stages = emptyList()
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
                        text = if (initialProject == null) "Новый проект" else "Редактировать проект",
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
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Название") },
                    placeholder = { Text("Название проекта") },
                    isError = attemptedSubmit && name.isBlank(),
                    supportingText = if (attemptedSubmit && name.isBlank()) {
                        { Text("Введите название проекта", color = AppColors.Error) }
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
                    minLines = 3,
                    label = { Text("Описание") },
                    placeholder = { Text("Кратко о цели проекта") },
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
                PastelCard(
                    color = AppColors.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDateSheet = true },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Дедлайн", color = AppColors.Muted, fontSize = 11.sp)
                            Text(
                                text = deadlineDate?.let { DateInput.formatForInput(it) } ?: "Выбрать дату",
                                color = if (deadlineDate == null) AppColors.Muted else AppColors.Ink,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Icon(Icons.Outlined.CalendarMonth, contentDescription = "Выбрать дату", tint = AppColors.Ink)
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Цвет карточки", color = AppColors.Muted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        paletteOptions.forEach { option ->
                            val argb = option.toArgb()
                            val selected = colorArgb == argb
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(option)
                                    .then(if (selected) Modifier.border(2.dp, AppColors.Ink, CircleShape) else Modifier)
                                    .clickable { colorArgb = argb },
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
            confirmEnabled = name.isNotBlank(),
            onConfirm = ::saveProject,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            IconButton(onClick = { showDateSheet = true }) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = "Дедлайн", tint = AppColors.Ink)
            }
            IconButton(onClick = {
                val idx = paletteOptions.indexOfFirst { it.toArgb() == colorArgb }
                colorArgb = paletteOptions[(idx + 1) % paletteOptions.size].toArgb()
            }) {
                Icon(Icons.Outlined.FolderOpen, contentDescription = "Сменить цвет", tint = AppColors.Ink)
            }
        }
    }

    if (showDateSheet) {
        DateTimePickerSheet(
            initialDate = deadlineDate,
            showTimeField = false,
            allowClear = true,
            onDismiss = { showDateSheet = false },
            onConfirm = { pickedDate, _ ->
                deadlineDate = pickedDate
                deadlineTouched = true
                showDateSheet = false
            }
        )
    }
}