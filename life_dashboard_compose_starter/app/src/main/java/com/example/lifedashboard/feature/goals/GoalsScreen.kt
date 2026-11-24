package com.example.lifedashboard.feature.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.data.DateInput
import com.example.lifedashboard.data.model.DashboardGoal
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.data.model.GoalColor
import com.example.lifedashboard.ui.components.CircleIconButton
import com.example.lifedashboard.ui.components.FilterPill
import com.example.lifedashboard.ui.components.LocalDatePickerField
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.components.ProgressTrack
import com.example.lifedashboard.ui.components.ScreenTopBar
import com.example.lifedashboard.ui.theme.AppColors
import java.time.LocalDate
import java.util.UUID

private enum class GoalFilter(val title: String) {
    ALL("Все"), ACTIVE("В процессе"), COMPLETED("Достигнуты")
}

@Composable
fun GoalsScreen(
    goals: List<DashboardGoal>,
    projects: List<DashboardProject>,
    onBack: () -> Unit,
    onSaveGoal: (DashboardGoal, (() -> Unit)?) -> Unit,
    onDeleteGoal: (String, (() -> Unit)?) -> Unit,
    onChangeProgress: (String, Int) -> Unit,
    operationError: String? = null,
    initialGoalId: String? = null,
    startWithCreate: Boolean = false
) {
    val listState = rememberLazyListState()
    var filter by remember { mutableStateOf(GoalFilter.ALL) }
    var showEditor by remember(startWithCreate) { mutableStateOf(startWithCreate) }
    var editingGoal by remember { mutableStateOf<DashboardGoal?>(null) }
    var deletingGoal by remember { mutableStateOf<DashboardGoal?>(null) }

    val completed = goals.count { it.isCompleted }

    LaunchedEffect(initialGoalId, goals) {
        val target = goals.firstOrNull { it.id == initialGoalId }
        if (target != null) filter = if (target.isCompleted) GoalFilter.COMPLETED else GoalFilter.ACTIVE
    }

    val visibleGoals = goals.filter {
        when (filter) {
            GoalFilter.ALL -> true
            GoalFilter.ACTIVE -> !it.isCompleted
            GoalFilter.COMPLETED -> it.isCompleted
        }
    }

    LaunchedEffect(initialGoalId, visibleGoals, operationError) {
        val targetIndex = visibleGoals.indexOfFirst { it.id == initialGoalId }
        if (initialGoalId != null && targetIndex >= 0) {
            val fixedItemsBeforeGoals = 3 + if (operationError != null) 1 else 0
            listState.animateScrollToItem(fixedItemsBeforeGoals + targetIndex)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ScreenTopBar(
                title = "Цели",
                actionIcon = Icons.Outlined.Add,
                actionDescription = "Добавить цель",
                onActionClick = {
                    editingGoal = null
                    showEditor = true
                },
                onBackClick = onBack
            )
        }
        item {
            PastelCard(color = AppColors.Blue) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Личный прогресс", color = AppColors.Ink, fontSize = 14.sp)
                        Text("$completed из ${goals.size} целей", color = AppColors.Ink, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                    }
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.size(54.dp).clip(CircleShape).background(AppColors.White.copy(alpha = 0.75f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Flag, contentDescription = null, tint = AppColors.Ink, modifier = Modifier.size(26.dp))
                    }
                }
                ProgressTrack(
                    progress = if (goals.isEmpty()) 0f else completed.toFloat() / goals.size,
                    trackColor = AppColors.White.copy(alpha = 0.8f),
                    progressColor = AppColors.Ink
                )
                Text(
                    text = if (goals.isEmpty()) "Добавь первую цель и отслеживай её выполнение" else "${(completed * 100 / goals.size)}% целей достигнуто",
                    color = AppColors.Ink,
                    fontSize = 13.sp
                )
            }
        }
        if (operationError != null && !showEditor && deletingGoal == null) {
            item {
                PastelCard(color = AppColors.White) {
                    Text(operationError, color = AppColors.Error, fontSize = 13.sp)
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GoalFilter.values().forEach { option ->
                    FilterPill(
                        label = option.title,
                        selected = filter == option,
                        count = when (option) {
                            GoalFilter.ALL -> goals.size.toString()
                            GoalFilter.ACTIVE -> (goals.size - completed).toString()
                            GoalFilter.COMPLETED -> completed.toString()
                        },
                        onClick = { filter = option }
                    )
                }
            }
        }
        if (visibleGoals.isEmpty()) {
            item {
                PastelCard(color = AppColors.White) {
                    Text(
                        text = when {
                            goals.isEmpty() -> "Пока нет целей"
                            filter == GoalFilter.ACTIVE -> "Все цели достигнуты"
                            else -> "Пока нет достигнутых целей"
                        },
                        color = AppColors.Ink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = if (goals.isEmpty()) "Создай цель, укажи желаемый результат и постепенно отмечай прогресс." else "Выбери другой фильтр, чтобы посмотреть остальные цели.",
                        color = AppColors.Muted,
                        fontSize = 14.sp
                    )
                    if (goals.isEmpty()) {
                        TextButton(onClick = { editingGoal = null; showEditor = true }) { Text("Создать цель") }
                    }
                }
            }
        } else {
            items(visibleGoals, key = { it.id }) { goal ->
                GoalCard(
                    goal = goal,
                    projectName = projects.firstOrNull { it.id == goal.projectId }?.name,
                    onEdit = { editingGoal = goal; showEditor = true },
                    onDelete = { deletingGoal = goal },
                    onProgressChange = { delta -> onChangeProgress(goal.id, delta) },
                    highlighted = goal.id == initialGoalId
                )
            }
        }
    }

    if (showEditor) {
        GoalEditorDialog(
            initialGoal = editingGoal,
            projects = projects,
            operationError = operationError,
            onDismiss = { showEditor = false },
            onSave = { goal -> onSaveGoal(goal) { showEditor = false } }
        )
    }

    deletingGoal?.let { goal ->
        AlertDialog(
            onDismissRequest = { deletingGoal = null },
            title = { Text("Удалить цель?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("«${goal.title}» и её прогресс будут удалены без возможности восстановления.")
                    operationError?.let { Text(it, color = AppColors.Error, fontSize = 12.sp) }
                }
            },
            confirmButton = {
                TextButton(onClick = { onDeleteGoal(goal.id) { deletingGoal = null } }) { Text("Удалить") }
            },
            dismissButton = { TextButton(onClick = { deletingGoal = null }) { Text("Отмена") } }
        )
    }
}

@Composable
private fun GoalCard(
    goal: DashboardGoal,
    projectName: String?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onProgressChange: (Int) -> Unit,
    highlighted: Boolean = false
) {
    val background = if (highlighted) AppColors.Lavender else goalColor(goal.color)
    PastelCard(color = background) {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(goal.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.Ink)
                if (goal.description.isNotBlank()) Text(goal.description, fontSize = 13.sp, color = AppColors.Ink.copy(alpha = 0.76f))
                if (projectName != null) Text("Проект: $projectName", fontSize = 12.sp, color = AppColors.Ink.copy(alpha = 0.68f))
                goal.dueDate?.let { Text("Срок: ${DateInput.formatForInput(it)}", fontSize = 12.sp, color = AppColors.Ink.copy(alpha = 0.68f)) }
            }
            IconButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, contentDescription = "Изменить цель", tint = AppColors.Ink) }
            IconButton(onClick = onDelete) { Icon(Icons.Outlined.Delete, contentDescription = "Удалить цель", tint = AppColors.Ink) }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${goal.currentValue} / ${goal.targetValue} ${goal.unit}", modifier = Modifier.weight(1f), fontSize = 14.sp, color = AppColors.Ink, fontWeight = FontWeight.SemiBold)
            if (goal.isCompleted) {
                Icon(Icons.Outlined.CheckCircle, contentDescription = "Цель достигнута", tint = AppColors.Ink)
                Spacer(Modifier.width(6.dp))
                Text("Готово", color = AppColors.Ink, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
        ProgressTrack(progress = goal.progress, trackColor = AppColors.White.copy(alpha = 0.75f), progressColor = AppColors.Ink)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
            IconButton(onClick = { onProgressChange(-1) }, enabled = goal.currentValue > 0) {
                Icon(Icons.Outlined.Remove, contentDescription = "Уменьшить прогресс", tint = AppColors.Ink)
            }
            Text("Изменить прогресс", fontSize = 12.sp, color = AppColors.Ink.copy(alpha = 0.72f))
            IconButton(onClick = { onProgressChange(1) }, enabled = !goal.isCompleted) {
                Icon(Icons.Outlined.Add, contentDescription = "Увеличить прогресс", tint = AppColors.Ink)
            }
        }
    }
}

@Composable
private fun GoalEditorDialog(
    initialGoal: DashboardGoal?,
    projects: List<DashboardProject>,
    operationError: String?,
    onDismiss: () -> Unit,
    onSave: (DashboardGoal) -> Unit
) {
    var title by remember(initialGoal?.id) { mutableStateOf(initialGoal?.title.orEmpty()) }
    var description by remember(initialGoal?.id) { mutableStateOf(initialGoal?.description.orEmpty()) }
    var currentValue by remember(initialGoal?.id) { mutableStateOf((initialGoal?.currentValue ?: 0).toString()) }
    var targetValue by remember(initialGoal?.id) { mutableStateOf((initialGoal?.targetValue ?: 10).toString()) }
    var unit by remember(initialGoal?.id) { mutableStateOf(initialGoal?.unit ?: "раз") }
    var dueDate by remember(initialGoal?.id) { mutableStateOf(initialGoal?.dueDate) }
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
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Название") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Описание") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
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
                OutlinedTextField(value = unit, onValueChange = { unit = it.take(24) }, label = { Text("Единица измерения") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                LocalDatePickerField(
                    date = dueDate,
                    label = "Срок достижения",
                    onDateChange = { dueDate = it }
                )
                if (projects.isNotEmpty()) {
                    Text("Связанный проект", color = AppColors.Muted, fontSize = 13.sp)
                    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterPill(label = "Без проекта", selected = projectId == null, onClick = { projectId = null })
                        projects.forEach { project ->
                            FilterPill(label = project.name, selected = projectId == project.id, onClick = { projectId = project.id })
                        }
                    }
                }
                Text("Цвет карточки", color = AppColors.Muted, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    GoalColor.values().forEach { option ->
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .size(if (color == option) 38.dp else 32.dp)
                                .clip(CircleShape)
                                .background(goalColor(option))
                                .clickable { color = option },
                            contentAlignment = Alignment.Center
                        ) {
                            if (color == option) {
                                androidx.compose.foundation.layout.Box(
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
private fun goalColor(color: GoalColor): Color = when (color) {
    GoalColor.BLUE -> AppColors.Blue
    GoalColor.MINT -> AppColors.Mint
    GoalColor.YELLOW -> AppColors.Yellow
    GoalColor.PINK -> AppColors.Pink
    GoalColor.LAVENDER -> AppColors.Lavender
}

