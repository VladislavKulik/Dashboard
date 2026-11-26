package com.example.lifedashboard.feature.projects

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Undo
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.data.DateInput
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.ui.components.CircleIconButton
import com.example.lifedashboard.ui.components.ColorPaletteSheet
import com.example.lifedashboard.ui.components.DateTimePickerSheet
import com.example.lifedashboard.ui.components.EditorBottomBar
import com.example.lifedashboard.ui.components.EditorPropertyChip
import com.example.lifedashboard.ui.components.EditorToolButton
import com.example.lifedashboard.ui.components.PaletteColorOption
import com.example.lifedashboard.ui.theme.AppColors
import java.time.LocalDate
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProjectEditorScreen(
    initialProject: DashboardProject?,
    onBack: () -> Unit,
    onSave: (DashboardProject, () -> Unit) -> Unit,
    operationError: String? = null,
    initialDeadlineDate: LocalDate? = null
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
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

    val descHistory = remember(initialProject?.id) { mutableStateListOf(initialProject?.description.orEmpty()) }
    var descHistoryIndex by remember(initialProject?.id) { mutableIntStateOf(0) }

    fun updateDescription(newText: String) {
        if (newText == description) return
        description = newText
        while (descHistory.lastIndex > descHistoryIndex) {
            descHistory.removeAt(descHistory.lastIndex)
        }
        descHistory.add(newText)
        descHistoryIndex = descHistory.lastIndex
    }

    var showDateSheet by remember { mutableStateOf(false) }
    var showPaletteSheet by remember { mutableStateOf(false) }
    var showMoreSheet by remember { mutableStateOf(false) }

    val paletteOptions = listOf(
        PaletteColorOption(AppColors.Lavender.toArgb(), "Лавандовый", AppColors.Lavender),
        PaletteColorOption(AppColors.Yellow.toArgb(), "Жёлтый", AppColors.Yellow),
        PaletteColorOption(AppColors.Mint.toArgb(), "Мятный", AppColors.Mint),
        PaletteColorOption(AppColors.Pink.toArgb(), "Розовый", AppColors.Pink),
        PaletteColorOption(AppColors.Blue.toArgb(), "Голубой", AppColors.Blue)
    )

    fun saveProject() {
        attemptedSubmit = true
        if (name.isBlank()) return
        val resolvedDeadline = deadlineDate ?: if (!deadlineTouched) DateInput.parseLegacyLabel(initialProject?.deadlineLabel) else null
        val resolvedLabel = if (deadlineTouched) {
            DateInput.formatTaskLabel(context, resolvedDeadline)
        } else {
            initialProject?.deadlineLabel ?: DateInput.formatTaskLabel(context, resolvedDeadline)
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
                stages = emptyList(),
                isArchived = initialProject?.isArchived ?: false
            ),
            onBack
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                    onValueChange = ::updateDescription,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 6,
                    maxLines = 16,
                    label = { Text("Описание") },
                    placeholder = { Text("Кратко о цели и деталях проекта") },
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
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EditorPropertyChip(
                        icon = Icons.Outlined.Palette,
                        label = paletteOptions.firstOrNull { it.value == colorArgb }?.label ?: "Цвет",
                        backgroundColor = Color(colorArgb),
                        onClick = { showPaletteSheet = true }
                    )
                    EditorPropertyChip(
                        icon = Icons.Outlined.CalendarMonth,
                        label = deadlineDate?.let { "Дедлайн: ${DateInput.formatTaskLabel(context, it)}" } ?: "Без дедлайна",
                        onClick = { showDateSheet = true },
                        onClear = if (deadlineDate != null) ({
                            deadlineDate = null
                            deadlineTouched = true
                        }) else null
                    )
                }
            }
        }

        EditorBottomBar(
            confirmEnabled = name.isNotBlank(),
            onConfirm = ::saveProject,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            EditorToolButton(
                icon = Icons.Outlined.CalendarMonth,
                contentDescription = "Дедлайн проекта",
                isActive = deadlineDate != null,
                onClick = { showDateSheet = true }
            )
            EditorToolButton(
                icon = Icons.Outlined.Palette,
                contentDescription = "Цвет карточки",
                onClick = { showPaletteSheet = true }
            )
            EditorToolButton(
                icon = Icons.Outlined.Undo,
                contentDescription = "Отменить ввод описания",
                enabled = descHistoryIndex > 0,
                onClick = {
                    if (descHistoryIndex > 0) {
                        descHistoryIndex -= 1
                        description = descHistory[descHistoryIndex]
                    }
                }
            )
            EditorToolButton(
                icon = Icons.Outlined.MoreVert,
                contentDescription = "Ещё",
                onClick = { showMoreSheet = true }
            )
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

    if (showPaletteSheet) {
        ColorPaletteSheet(
            title = "Цвет проекта",
            options = paletteOptions,
            selected = colorArgb,
            onSelect = { colorArgb = it },
            onDismiss = { showPaletteSheet = false }
        )
    }

    if (showMoreSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showMoreSheet = false },
            sheetState = sheetState,
            containerColor = AppColors.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            showMoreSheet = false
                            val text = listOf(name.trim(), description.trim()).filter { it.isNotBlank() }.joinToString("\n\n")
                            if (text.isNotBlank()) clipboard.setText(AnnotatedString(text))
                        }
                        .padding(horizontal = 6.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.ContentCopy, contentDescription = null, tint = AppColors.Ink, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(16.dp))
                    Text("Скопировать описание", color = AppColors.Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}