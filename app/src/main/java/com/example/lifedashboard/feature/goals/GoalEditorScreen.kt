package com.example.lifedashboard.feature.goals

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Palette
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
import androidx.compose.ui.res.stringResource
import com.example.lifedashboard.R
import com.example.lifedashboard.data.DateInput
import com.example.lifedashboard.data.model.DashboardGoal
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.data.model.GoalColor
import com.example.lifedashboard.ui.components.CircleIconButton
import com.example.lifedashboard.ui.components.ColorPaletteSheet
import com.example.lifedashboard.ui.components.DateTimePickerSheet
import com.example.lifedashboard.ui.components.EditorBottomBar
import com.example.lifedashboard.ui.components.EditorPropertyChip
import com.example.lifedashboard.ui.components.EditorToolButton
import com.example.lifedashboard.ui.components.FilterPill
import com.example.lifedashboard.ui.components.PaletteColorOption
import com.example.lifedashboard.ui.components.ProjectPickerSheet
import com.example.lifedashboard.ui.theme.AppColors
import java.time.LocalDate
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
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
    val context = LocalContext.current
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
    var showProjectSheet by remember { mutableStateOf(false) }
    var showPaletteSheet by remember { mutableStateOf(false) }
    var showMoreSheet by remember { mutableStateOf(false) }

    val linkedProject = remember(projectId, projects) {
        projects.firstOrNull { it.id == projectId }
    }

    val colorOptions = listOf(
        PaletteColorOption(GoalColor.PINK, stringResource(R.string.color_pink), AppColors.Pink),
        PaletteColorOption(GoalColor.BLUE, stringResource(R.string.color_blue), AppColors.Blue),
        PaletteColorOption(GoalColor.MINT, stringResource(R.string.color_mint), AppColors.Mint),
        PaletteColorOption(GoalColor.YELLOW, stringResource(R.string.color_yellow), AppColors.Yellow),
        PaletteColorOption(GoalColor.LAVENDER, stringResource(R.string.color_lavender), AppColors.Lavender)
    )

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
                unit = unit.trim().ifBlank { context.getString(R.string.default_unit) },
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    CircleIconButton(icon = Icons.Outlined.Close, contentDescription = stringResource(R.string.action_close), onClick = onBack)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = if (initialGoal == null) stringResource(R.string.goal_new) else stringResource(R.string.goal_edit_alt),
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
                    label = { Text(stringResource(R.string.goal_name_full)) },
                    placeholder = { Text(stringResource(R.string.goal_name_placeholder)) },
                    isError = attemptedSubmit && title.isBlank(),
                    supportingText = if (attemptedSubmit && title.isBlank()) {
                        { Text(stringResource(R.string.enter_goal_title_error), color = AppColors.Error) }
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
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = currentValue,
                            onValueChange = { currentValue = it.filter(Char::isDigit).take(7) },
                            label = { Text(stringResource(R.string.goal_current)) },
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
                            label = { Text(stringResource(R.string.goal_target)) },
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
                            label = { Text(stringResource(R.string.unit_short)) },
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
                        Text(stringResource(R.string.check_values_error), color = AppColors.Error, fontSize = 12.sp)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("раз", "книг", "км", "часов", "уроков", "шагов", "%").forEach { quickUnit ->
                            FilterPill(
                                label = quickUnit,
                                selected = unit == quickUnit,
                                onClick = { unit = quickUnit }
                            )
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 8,
                    label = { Text(stringResource(R.string.goal_description)) },
                    placeholder = { Text(stringResource(R.string.why_needed_placeholder)) },
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
                        label = colorOptions.firstOrNull { it.value == color }?.label ?: stringResource(R.string.color_label),
                        backgroundColor = goalCardColor(color),
                        onClick = { showPaletteSheet = true }
                    )
                    val noDeadlineLabel = stringResource(R.string.date_no_deadline)
                    EditorPropertyChip(
                        icon = Icons.Outlined.CalendarMonth,
                        label = dueDate?.let { stringResource(R.string.deadline_prefix, DateInput.formatTaskLabel(context, it)) } ?: noDeadlineLabel,
                        onClick = { showDateSheet = true },
                        onClear = if (dueDate != null) ({ dueDate = null }) else null
                    )
                    if (linkedProject != null) {
                        EditorPropertyChip(
                            icon = Icons.Outlined.FolderOpen,
                            label = linkedProject.name,
                            backgroundColor = Color(linkedProject.colorArgb).copy(alpha = 0.65f),
                            onClick = { showProjectSheet = true },
                            onClear = { projectId = null }
                        )
                    }
                }
            }
        }

        EditorBottomBar(
            confirmEnabled = canSave,
            onConfirm = ::submit,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            EditorToolButton(
                icon = Icons.Outlined.CalendarMonth,
                contentDescription = stringResource(R.string.goal_deadline),
                isActive = dueDate != null,
                onClick = { showDateSheet = true }
            )
            EditorToolButton(
                icon = Icons.Outlined.Palette,
                contentDescription = stringResource(R.string.card_color),
                onClick = { showPaletteSheet = true }
            )
            if (projects.isNotEmpty()) {
                EditorToolButton(
                    icon = Icons.Outlined.MoreVert,
                    contentDescription = stringResource(R.string.linked_project_more),
                    isActive = projectId != null,
                    onClick = { showMoreSheet = true }
                )
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
                            showProjectSheet = true
                        }
                        .padding(horizontal = 6.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.FolderOpen, contentDescription = null, tint = AppColors.Ink, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(16.dp))
                    Text(stringResource(R.string.label_project), color = AppColors.Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Text(linkedProject?.name ?: stringResource(R.string.no_linked_project), color = AppColors.Muted, fontSize = 13.sp)
                }
            }
        }
    }

    if (showProjectSheet) {
        ProjectPickerSheet(
            projects = projects,
            selectedProjectId = projectId,
            onSelectProject = { projectId = it },
            onDismiss = { showProjectSheet = false }
        )
    }

    if (showPaletteSheet) {
        ColorPaletteSheet(
            title = stringResource(R.string.goal_color_title),
            options = colorOptions,
            selected = color,
            onSelect = { color = it },
            onDismiss = { showPaletteSheet = false }
        )
    }
}