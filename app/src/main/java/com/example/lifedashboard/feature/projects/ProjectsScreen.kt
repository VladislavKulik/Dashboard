package com.example.lifedashboard.feature.projects

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.data.model.DashboardGoal
import com.example.lifedashboard.data.model.DashboardNote
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.data.model.DashboardTask
import com.example.lifedashboard.ui.components.AppFab
import com.example.lifedashboard.ui.components.CircleIconButton
import com.example.lifedashboard.ui.components.CircularProgressRing
import com.example.lifedashboard.ui.components.EmptySectionState
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.theme.AppColors
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@Composable
fun ProjectsScreen(
    projects: List<DashboardProject>,
    tasks: List<DashboardTask>,
    onOpenProject: (String) -> Unit,
    onToggleTask: (String) -> Unit,
    onSaveProject: (DashboardProject, () -> Unit) -> Unit,
    operationError: String? = null,
    startWithCreate: Boolean = false,
    onBack: (() -> Unit)? = null,
    goals: List<DashboardGoal> = emptyList(),
    notes: List<DashboardNote> = emptyList(),
    onCreateProject: (() -> Unit)? = null,
    onOpenSearch: () -> Unit = {},
    onToggleArchive: (String) -> Unit = {},
    onDeleteProject: (String, () -> Unit) -> Unit = { _, onDone -> onDone() }
) {
    var showArchived by rememberSaveable { mutableStateOf(false) }
    var showCreateDialog by remember(startWithCreate) { mutableStateOf(startWithCreate && onCreateProject == null) }
    var deletingProject by remember { mutableStateOf<DashboardProject?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val archivedCount = remember(projects) { projects.count { it.isArchived } }
    val visibleProjects = remember(projects, showArchived) {
        projects.filter { it.isArchived == showArchived }
    }

    fun handleArchiveToggle(project: DashboardProject) {
        val wasArchived = project.isArchived
        onToggleArchive(project.id)
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val res = snackbarHostState.showSnackbar(
                message = if (wasArchived) "Проект возвращён из архива" else "Проект перемещён в архив",
                actionLabel = "Отменить",
                duration = SnackbarDuration.Short
            )
            if (res == SnackbarResult.ActionPerformed) {
                onToggleArchive(project.id)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onBack != null) {
                        CircleIconButton(
                            icon = Icons.Outlined.ArrowBack,
                            contentDescription = "Назад",
                            onClick = {
                                if (showArchived) showArchived = false else onBack()
                            }
                        )
                        Spacer(Modifier.width(12.dp))
                    }
                    Text(
                        text = if (showArchived) "Архив" else "Проекты",
                        style = MaterialTheme.typography.headlineLarge,
                        color = AppColors.Ink,
                        modifier = Modifier.weight(1f)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircleIconButton(
                            icon = Icons.Outlined.Inventory2,
                            contentDescription = if (showArchived) "К списку проектов" else "Архив проектов ($archivedCount)",
                            background = if (showArchived) AppColors.Lavender else AppColors.White,
                            tint = AppColors.Ink,
                            onClick = { showArchived = !showArchived }
                        )
                        CircleIconButton(
                            icon = Icons.Outlined.Search,
                            contentDescription = "Поиск проектов",
                            onClick = onOpenSearch
                        )
                    }
                }
            }

            if (!operationError.isNullOrBlank()) {
                item { Text(operationError, color = AppColors.Error, style = MaterialTheme.typography.bodySmall) }
            }

            if (visibleProjects.isEmpty()) {
                item {
                    EmptySectionState(
                        icon = if (showArchived) Icons.Outlined.Inventory2 else Icons.Outlined.FolderOpen,
                        iconBackground = AppColors.Lavender,
                        title = if (showArchived) "Архив пуст" else "Проектов пока нет",
                        subtitle = if (showArchived) {
                            "Сюда можно убрать проекты, над которыми работа завершена или временно приостановлена"
                        } else {
                            "Создайте проект, чтобы объединить связанные задачи, цели и заметки в одном месте"
                        }
                    )
                }
            } else {
                items(visibleProjects, key = { it.id }) { project ->
                    val taskCount = tasks.count { it.projectId == project.id }
                    val goalCount = goals.count { it.projectId == project.id }
                    val noteCount = notes.count { it.projectId == project.id }
                    SwipeableProjectCard(
                        project = project,
                        taskCount = taskCount,
                        goalCount = goalCount,
                        noteCount = noteCount,
                        onClick = { onOpenProject(project.id) },
                        onToggleArchive = { handleArchiveToggle(project) },
                        onDelete = { deletingProject = project }
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    shape = RoundedCornerShape(20.dp),
                    containerColor = AppColors.Ink,
                    contentColor = AppColors.White,
                    action = {
                        data.visuals.actionLabel?.let { label ->
                            TextButton(onClick = { data.performAction() }) {
                                Text(label, color = AppColors.Yellow, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                ) {
                    Text(data.visuals.message, fontWeight = FontWeight.Medium)
                }
            }
        }

        if (!showArchived) {
            AppFab(
                icon = Icons.Outlined.Add,
                contentDescription = "Новый проект",
                onClick = {
                    if (onCreateProject != null) onCreateProject() else showCreateDialog = true
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
            )
        }
    }

    if (showCreateDialog) {
        ProjectEditorDialog(
            initialProject = null,
            onDismiss = { showCreateDialog = false },
            onSave = { project -> onSaveProject(project) { showCreateDialog = false } },
            operationError = operationError
        )
    }

    deletingProject?.let { project ->
        AlertDialog(
            onDismissRequest = { deletingProject = null },
            title = { Text("Удалить проект?", color = AppColors.Ink) },
            text = {
                Text(
                    "«${project.name}» будет удалён. Связанные задачи, цели и заметки сохранятся без привязки к проекту.",
                    color = AppColors.Muted
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteProject(project.id) { deletingProject = null }
                }) {
                    Text("Удалить", color = AppColors.Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingProject = null }) {
                    Text("Отмена", color = AppColors.Muted)
                }
            },
            containerColor = AppColors.White
        )
    }
}

@Composable
private fun SwipeableProjectCard(
    project: DashboardProject,
    taskCount: Int,
    goalCount: Int,
    noteCount: Int,
    onClick: () -> Unit,
    onToggleArchive: () -> Unit,
    onDelete: () -> Unit
) {
    val maxRevealPx = with(LocalDensity.current) { 108.dp.toPx() }
    var rawOffset by remember(project.id) { mutableFloatStateOf(0f) }
    val animatedOffset by animateFloatAsState(targetValue = rawOffset, label = "projectSwipe")

    val initial = project.name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "П"
    val deadlineText = when {
        project.isCompleted -> "завершён"
        project.deadlineLabel.equals("Без срока", ignoreCase = true) ||
            project.deadlineLabel.equals("Готово", ignoreCase = true) -> project.deadlineLabel.lowercase()
        else -> "до ${project.deadlineLabel}"
    }
    val links = buildList {
        add("$taskCount зад.")
        if (goalCount > 0) add("$goalCount цел.")
        if (noteCount > 0) add("$noteCount зам.")
    }.joinToString(" · ")

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AppColors.White)
                    .clickable {
                        rawOffset = 0f
                        onToggleArchive()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (project.isArchived) Icons.Outlined.Unarchive else Icons.Outlined.Inventory2,
                    contentDescription = if (project.isArchived) "Вернуть из архива" else "В архив",
                    tint = AppColors.Ink,
                    modifier = Modifier.size(20.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AppColors.Pink)
                    .clickable {
                        rawOffset = 0f
                        onDelete()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "Удалить",
                    tint = AppColors.Ink,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        PastelCard(
            color = Color(project.colorArgb),
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .pointerInput(project.id) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            rawOffset = if (rawOffset < -maxRevealPx * 0.45f) -maxRevealPx else 0f
                        },
                        onDragCancel = { rawOffset = 0f },
                        onHorizontalDrag = { _, dragAmount ->
                            rawOffset = (rawOffset + dragAmount).coerceIn(-maxRevealPx, 0f)
                        }
                    )
                }
                .clickable(role = Role.Button) {
                    if (rawOffset != 0f) rawOffset = 0f else onClick()
                },
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(AppColors.White.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initial, color = AppColors.Ink, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = AppColors.Ink,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$deadlineText · $links",
                        color = AppColors.Muted,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(12.dp))
                CircularProgressRing(
                    progress = project.progress,
                    size = 52.dp,
                    strokeWidth = 5.dp,
                    trackColor = AppColors.Ink.copy(alpha = 0.14f),
                    progressColor = AppColors.Ink
                )
            }
        }
    }
}