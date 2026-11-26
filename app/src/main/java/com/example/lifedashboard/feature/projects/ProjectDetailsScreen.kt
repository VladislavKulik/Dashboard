package com.example.lifedashboard.feature.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.data.model.DashboardActivityEvent
import com.example.lifedashboard.data.model.DashboardGoal
import com.example.lifedashboard.data.model.DashboardNote
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.data.model.DashboardTask
import com.example.lifedashboard.data.model.TaskPriority
import com.example.lifedashboard.data.model.TaskRecurrence
import com.example.lifedashboard.feature.tasks.AddTaskDialog
import com.example.lifedashboard.ui.components.AppFab
import com.example.lifedashboard.ui.components.CircleIconButton
import com.example.lifedashboard.ui.components.CircularProgressRing
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.components.ProgressTrack
import com.example.lifedashboard.ui.components.TaskRow
import com.example.lifedashboard.ui.theme.AppColors
import java.time.LocalDate

private enum class AttachItemTab(val label: String) {
    TASKS("Задачи"),
    GOALS("Цели"),
    NOTES("Заметки")
}

@Composable
fun ProjectDetailsScreen(
    project: DashboardProject,
    tasks: List<DashboardTask>,
    onBack: () -> Unit,
    onToggleTask: (String) -> Unit,
    onOpenAllTasks: () -> Unit,
    onToggleStage: (String, String) -> Unit = { _, _ -> },
    onSaveProject: (DashboardProject, () -> Unit) -> Unit,
    onDeleteProject: (String, () -> Unit) -> Unit,
    operationError: String? = null,
    activityEvents: List<DashboardActivityEvent> = emptyList(),
    onAddTask: (String, TaskPriority, String?, LocalDate?, TaskRecurrence, Set<Int>, Boolean, String) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onOpenTaskDetails: (String) -> Unit = {},
    goals: List<DashboardGoal> = emptyList(),
    notes: List<DashboardNote> = emptyList(),
    onOpenGoal: (String) -> Unit = {},
    onOpenNote: (String) -> Unit = {},
    onAddGoal: () -> Unit = {},
    onAddNote: () -> Unit = {},
    onEditProject: (() -> Unit)? = null,
    onOpenAddTask: (() -> Unit)? = null,
    allTasks: List<DashboardTask> = emptyList(),
    onOpenHistory: () -> Unit = {},
    onAttachExistingItems: (Set<String>, Set<String>, Set<String>) -> Unit = { _, _, _ -> },
    onToggleArchive: () -> Unit = {}
) {
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showEditor by remember(project.id) { mutableStateOf(false) }
    var showDeleteConfirmation by remember(project.id) { mutableStateOf(false) }
    var showAddTaskDialog by remember(project.id) { mutableStateOf(false) }
    var showCreateSheet by remember { mutableStateOf(false) }
    var showAttachSheet by remember { mutableStateOf(false) }

    val projectGoals = remember(project.id, goals) { goals.filter { it.projectId == project.id } }
    val projectNotes = remember(project.id, notes) { notes.filter { it.projectId == project.id } }

    val unattachedTasks = remember(project.id, allTasks) {
        allTasks.filter { it.projectId != project.id }
    }
    val unattachedGoals = remember(project.id, goals) {
        goals.filter { it.projectId != project.id }
    }
    val unattachedNotes = remember(project.id, notes) {
        notes.filter { it.projectId != project.id }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 156.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircleIconButton(
                        icon = Icons.Outlined.ArrowBack,
                        contentDescription = "Назад",
                        onClick = onBack
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Проект",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.headlineLarge,
                        color = AppColors.Ink
                    )
                    Box {
                        CircleIconButton(
                            icon = Icons.Outlined.MoreHoriz,
                            contentDescription = "Действия",
                            onClick = { showOverflowMenu = true }
                        )
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Редактировать") },
                                onClick = {
                                    showOverflowMenu = false
                                    if (onEditProject != null) onEditProject() else showEditor = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("История") },
                                onClick = {
                                    showOverflowMenu = false
                                    onOpenHistory()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (project.isArchived) "Вернуть из архива" else "В архив") },
                                onClick = {
                                    showOverflowMenu = false
                                    onToggleArchive()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Все задачи проекта") },
                                onClick = {
                                    showOverflowMenu = false
                                    onOpenAllTasks()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Удалить проект", color = AppColors.Error) },
                                onClick = {
                                    showOverflowMenu = false
                                    showDeleteConfirmation = true
                                }
                            )
                        }
                    }
                }
            }

            if (!operationError.isNullOrBlank()) {
                item { Text(operationError, color = AppColors.Error, style = MaterialTheme.typography.bodySmall) }
            }

            item {
                PastelCard(
                    color = Color(project.colorArgb),
                    contentPadding = PaddingValues(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(AppColors.Ink)
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "Дедлайн ${project.deadlineLabel}",
                                color = AppColors.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (project.isArchived) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(AppColors.White.copy(alpha = 0.85f))
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "В архиве",
                                    color = AppColors.Ink,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Text(
                        text = project.name,
                        color = AppColors.Ink,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    if (project.description.isNotBlank()) {
                        Text(project.description, color = AppColors.Muted, fontSize = 13.sp)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProgressTrack(
                            progress = project.progress,
                            modifier = Modifier.weight(1f),
                            trackColor = AppColors.Ink.copy(alpha = 0.14f),
                            progressColor = AppColors.Ink
                        )
                        Text(
                            text = "${(project.progress * 100).toInt()}%",
                            color = AppColors.Ink,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    }
                    Text(
                        text = "${project.completedTasks} из ${project.totalTasks} задач · ${projectGoals.size} цел. · ${projectNotes.size} зам.",
                        color = AppColors.Muted,
                        fontSize = 12.sp
                    )
                }
            }

            item {
                Text(
                    text = "Задачи (${tasks.size})",
                    color = AppColors.Muted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            item {
                PastelCard(
                    color = AppColors.White,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (tasks.isEmpty()) {
                        Text("Задач в проекте пока нет.", color = AppColors.Muted, fontSize = 13.sp)
                    } else {
                        tasks.take(6).forEachIndexed { index, task ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenTaskDetails(task.id) }
                            ) {
                                TaskRow(
                                    title = task.title,
                                    completed = task.isCompleted,
                                    trailing = task.dueLabel,
                                    onToggle = { onToggleTask(task.id) }
                                )
                            }
                            if (index < minOf(tasks.lastIndex, 5)) {
                                HorizontalDivider(color = AppColors.Outline.copy(alpha = 0.45f))
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Цели (${projectGoals.size})",
                    color = AppColors.Muted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            item {
                PastelCard(
                    color = AppColors.White,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (projectGoals.isEmpty()) {
                        Text("К проекту не привязаны цели.", color = AppColors.Muted, fontSize = 13.sp)
                    } else {
                        projectGoals.forEachIndexed { index, goal ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenGoal(goal.id) }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(AppColors.Pink),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Outlined.Flag, contentDescription = null, tint = AppColors.Ink, modifier = Modifier.size(17.dp))
                                }
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(goal.title, color = AppColors.Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("${goal.currentValue} / ${goal.targetValue} ${goal.unit}", color = AppColors.Muted, fontSize = 12.sp)
                                }
                                CircularProgressRing(
                                    progress = goal.progress,
                                    size = 38.dp,
                                    strokeWidth = 4.dp,
                                    labelFontSize = 10.sp
                                )
                            }
                            if (index < projectGoals.lastIndex) {
                                HorizontalDivider(color = AppColors.Outline.copy(alpha = 0.45f))
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Заметки (${projectNotes.size})",
                    color = AppColors.Muted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            item {
                PastelCard(
                    color = AppColors.White,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (projectNotes.isEmpty()) {
                        Text("К проекту не привязаны заметки.", color = AppColors.Muted, fontSize = 13.sp)
                    } else {
                        projectNotes.forEachIndexed { index, note ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenNote(note.id) }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(AppColors.Yellow),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Outlined.Description, contentDescription = null, tint = AppColors.Ink, modifier = Modifier.size(17.dp))
                                }
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(note.title, color = AppColors.Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    if (note.body.isNotBlank()) {
                                        Text(note.body.replace('\n', ' '), color = AppColors.Muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                                Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = AppColors.Muted)
                            }
                            if (index < projectNotes.lastIndex) {
                                HorizontalDivider(color = AppColors.Outline.copy(alpha = 0.45f))
                            }
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppFab(
                icon = Icons.Outlined.AttachFile,
                contentDescription = "Прикрепить существующие",
                onClick = { showAttachSheet = true },
                containerColor = AppColors.White,
                contentColor = AppColors.Ink,
                size = 52.dp,
                iconSize = 22.dp
            )

            AppFab(
                icon = Icons.Outlined.Add,
                contentDescription = "Создать в проекте",
                onClick = { showCreateSheet = true }
            )
        }
    }

    if (showCreateSheet) {
        ProjectCreateNewSheet(
            projectName = project.name,
            onDismiss = { showCreateSheet = false },
            onCreateTask = {
                showCreateSheet = false
                if (onOpenAddTask != null) onOpenAddTask() else showAddTaskDialog = true
            },
            onCreateGoal = {
                showCreateSheet = false
                onAddGoal()
            },
            onCreateNote = {
                showCreateSheet = false
                onAddNote()
            }
        )
    }

    if (showAttachSheet) {
        ProjectAttachExistingSheet(
            unattachedTasks = unattachedTasks,
            unattachedGoals = unattachedGoals,
            unattachedNotes = unattachedNotes,
            onDismiss = { showAttachSheet = false },
            onConfirmAttach = { taskIds, goalIds, noteIds ->
                onAttachExistingItems(taskIds, goalIds, noteIds)
                showAttachSheet = false
            }
        )
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            projects = listOf(project),
            initialProjectId = project.id,
            onDismiss = { showAddTaskDialog = false },
            onAdd = { title, priority, projectId, dueDate, recurrence, recurrenceDays, reminderEnabled, reminderTime ->
                onAddTask(title, priority, projectId ?: project.id, dueDate, recurrence, recurrenceDays, reminderEnabled, reminderTime)
                showAddTaskDialog = false
            }
        )
    }

    if (showEditor) {
        ProjectEditorDialog(
            initialProject = project,
            onDismiss = { showEditor = false },
            onSave = { updated -> onSaveProject(updated) { showEditor = false } },
            operationError = operationError
        )
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Удалить проект?", color = AppColors.Ink) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Проект будет удалён. Связанные задачи, цели и заметки сохранятся без привязки к проекту.", color = AppColors.Muted)
                    if (!operationError.isNullOrBlank()) Text(operationError, color = AppColors.Error)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteProject(project.id) {
                        showDeleteConfirmation = false
                        onBack()
                    }
                }) { Text("Удалить", color = AppColors.Error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirmation = false }) { Text("Отмена", color = AppColors.Muted) } },
            containerColor = AppColors.White
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectCreateNewSheet(
    projectName: String,
    onDismiss: () -> Unit,
    onCreateTask: () -> Unit,
    onCreateGoal: () -> Unit,
    onCreateNote: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppColors.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Создать в проекте",
                style = MaterialTheme.typography.headlineMedium,
                color = AppColors.Ink
            )
            Text(
                text = projectName,
                color = AppColors.Muted,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            ProjectSheetActionRow(
                label = "Новая задача",
                subtitle = "Сразу привязана к проекту",
                icon = Icons.Outlined.CheckCircleOutline,
                iconBg = AppColors.Blue,
                onClick = onCreateTask
            )
            ProjectSheetActionRow(
                label = "Новая цель",
                subtitle = "Измеримый результат проекта",
                icon = Icons.Outlined.Flag,
                iconBg = AppColors.Pink,
                onClick = onCreateGoal
            )
            ProjectSheetActionRow(
                label = "Новая заметка",
                subtitle = "Конспект, идеи или файлы проекта",
                icon = Icons.Outlined.Description,
                iconBg = AppColors.Yellow,
                onClick = onCreateNote
            )
        }
    }
}

@Composable
private fun ProjectSheetActionRow(
    label: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(AppColors.Background)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = AppColors.Ink, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, color = AppColors.Ink, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(subtitle, color = AppColors.Muted, fontSize = 12.sp)
        }
        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = AppColors.Muted)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectAttachExistingSheet(
    unattachedTasks: List<DashboardTask>,
    unattachedGoals: List<DashboardGoal>,
    unattachedNotes: List<DashboardNote>,
    onDismiss: () -> Unit,
    onConfirmAttach: (Set<String>, Set<String>, Set<String>) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableStateOf(AttachItemTab.TASKS) }
    var selectedTaskIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selectedGoalIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selectedNoteIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    val totalSelected = selectedTaskIds.size + selectedGoalIds.size + selectedNoteIds.size

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
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Прикрепить к проекту",
                style = MaterialTheme.typography.headlineMedium,
                color = AppColors.Ink
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(AppColors.White)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AttachItemTab.values().forEach { tab ->
                    val count = when (tab) {
                        AttachItemTab.TASKS -> selectedTaskIds.size
                        AttachItemTab.GOALS -> selectedGoalIds.size
                        AttachItemTab.NOTES -> selectedNoteIds.size
                    }
                    val label = if (count > 0) "${tab.label} ($count)" else tab.label
                    val isSelected = selectedTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .background(if (isSelected) AppColors.Ink else Color.Transparent)
                            .clickable(role = Role.Tab) { selectedTab = tab }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) AppColors.White else AppColors.Ink,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 160.dp, max = 340.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (selectedTab) {
                    AttachItemTab.TASKS -> {
                        if (unattachedTasks.isEmpty()) {
                            item {
                                EmptyAttachTabMessage("Все существующие задачи уже прикреплены к этому проекту.")
                            }
                        } else {
                            items(unattachedTasks, key = { "attach-task-${it.id}" }) { task ->
                                val checked = task.id in selectedTaskIds
                                AttachSelectableRow(
                                    title = task.title,
                                    subtitle = task.dueLabel,
                                    checked = checked,
                                    onToggle = {
                                        selectedTaskIds = if (checked) selectedTaskIds - task.id else selectedTaskIds + task.id
                                    }
                                )
                            }
                        }
                    }

                    AttachItemTab.GOALS -> {
                        if (unattachedGoals.isEmpty()) {
                            item {
                                EmptyAttachTabMessage("Нет доступных целей для прикрепления.")
                            }
                        } else {
                            items(unattachedGoals, key = { "attach-goal-${it.id}" }) { goal ->
                                val checked = goal.id in selectedGoalIds
                                AttachSelectableRow(
                                    title = goal.title,
                                    subtitle = "${goal.currentValue} из ${goal.targetValue} ${goal.unit}",
                                    checked = checked,
                                    onToggle = {
                                        selectedGoalIds = if (checked) selectedGoalIds - goal.id else selectedGoalIds + goal.id
                                    }
                                )
                            }
                        }
                    }

                    AttachItemTab.NOTES -> {
                        if (unattachedNotes.isEmpty()) {
                            item {
                                EmptyAttachTabMessage("Нет доступных заметок для прикрепления.")
                            }
                        } else {
                            items(unattachedNotes, key = { "attach-note-${it.id}" }) { note ->
                                val checked = note.id in selectedNoteIds
                                AttachSelectableRow(
                                    title = note.title,
                                    subtitle = stringResource(id = note.category.labelResId),
                                    checked = checked,
                                    onToggle = {
                                        selectedNoteIds = if (checked) selectedNoteIds - note.id else selectedNoteIds + note.id
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = { onConfirmAttach(selectedTaskIds, selectedGoalIds, selectedNoteIds) },
                enabled = totalSelected > 0,
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Ink,
                    contentColor = AppColors.White
                ),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text(
                    text = if (totalSelected > 0) "Прикрепить ($totalSelected)" else "Выбери элементы",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun AttachSelectableRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.White)
            .clickable(onClick = onToggle)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = { onToggle() })
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                color = AppColors.Ink,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    color = AppColors.Muted,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun EmptyAttachTabMessage(message: String) {
    PastelCard(
        color = AppColors.White,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(18.dp)
    ) {
        Text(text = message, color = AppColors.Muted, fontSize = 13.sp)
    }
}