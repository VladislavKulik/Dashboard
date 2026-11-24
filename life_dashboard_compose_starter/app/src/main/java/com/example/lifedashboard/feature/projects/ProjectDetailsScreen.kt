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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.data.model.DashboardTask
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.components.ProgressTrack
import com.example.lifedashboard.ui.components.ScreenTopBar
import com.example.lifedashboard.ui.components.TaskRow
import com.example.lifedashboard.ui.theme.AppColors

@Composable
fun ProjectDetailsScreen(
    project: DashboardProject,
    tasks: List<DashboardTask>,
    onBack: () -> Unit,
    onToggleTask: (String) -> Unit,
    onOpenAllTasks: () -> Unit,
    onToggleStage: (String, String) -> Unit,
    onSaveProject: (DashboardProject, () -> Unit) -> Unit,
    onDeleteProject: (String, () -> Unit) -> Unit,
    operationError: String? = null
) {
    var showEditor by remember(project.id) { mutableStateOf(false) }
    var showDeleteConfirmation by remember(project.id) { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ScreenTopBar(title = "Проект", actionIcon = Icons.Outlined.Edit, actionDescription = "Редактировать проект", onActionClick = { showEditor = true }, onBackClick = onBack)
        }

        item {
            ProjectOverviewCard(project = project)
        }

        item {
            Text(
                text = "Этапы проекта",
                color = AppColors.Ink,
                style = MaterialTheme.typography.titleLarge
            )
        }

        item {
            PastelCard(
                color = AppColors.White,
                contentPadding = PaddingValues(18.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                project.stages.forEachIndexed { index, stage ->
                    ProjectStageRow(
                        title = stage.title,
                        isCompleted = stage.isCompleted,
                        isCurrent = stage.isCurrent,
                        isLast = index == project.stages.lastIndex,
                        onClick = { onToggleStage(project.id, stage.id) }
                    )
                }
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Ближайшие задачи",
                    color = AppColors.Ink,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${tasks.size} в списке",
                    color = AppColors.Muted,
                    fontSize = 12.sp
                )
            }
        }

        if (tasks.isEmpty()) {
            item {
                PastelCard(color = AppColors.Mint) {
                    Text("Задач пока нет", style = MaterialTheme.typography.titleMedium, color = AppColors.Ink)
                    Text("Добавь первую задачу, чтобы двигать проект вперёд.", color = AppColors.Muted)
                }
            }
        } else {
            items(tasks.take(4), key = { it.id }) { task ->
                PastelCard(
                    color = AppColors.White,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TaskRow(
                        title = task.title,
                        completed = task.isCompleted,
                        trailing = task.dueLabel,
                        onToggle = { onToggleTask(task.id) }
                    )
                }
            }
        }

        if (tasks.size > 4 || tasks.isNotEmpty()) {
            item {
                OpenTasksButton(
                    label = if (tasks.size > 4) "Все задачи проекта" else "Открыть список задач",
                    onClick = onOpenAllTasks
                )
            }
        }

        item {
            PastelCard(
                color = AppColors.Ink,
                contentPadding = PaddingValues(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Последняя активность", color = AppColors.White, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = if (project.completedTasks > 0) "Завершено задач: ${project.completedTasks} · Обновлено сегодня" else "Проект создан · Активность пока не отмечена",
                    color = AppColors.MutedOnDark,
                    fontSize = 13.sp
                )
            }
        }
        if (!operationError.isNullOrBlank()) {
            item { Text(operationError, color = AppColors.Error, style = MaterialTheme.typography.bodySmall) }
        }
        item {
            TextButton(onClick = { showDeleteConfirmation = true }) {
                Text("Удалить проект", color = AppColors.Error)
            }
        }
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
                    Text("Проект и его этапы будут удалены. Связанные задачи останутся без проекта.", color = AppColors.Muted)
                    if (!operationError.isNullOrBlank()) Text(operationError, color = AppColors.Error)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteProject(project.id) { showDeleteConfirmation = false; onBack() }
                }) { Text("Удалить", color = AppColors.Error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirmation = false }) { Text("Отмена", color = AppColors.Muted) } },
            containerColor = AppColors.White
        )
    }
}

@Composable
private fun ProjectOverviewCard(project: DashboardProject) {
    PastelCard(
        color = Color(project.colorArgb),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Text(
                text = project.name,
                color = AppColors.Ink,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(AppColors.Ink)
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Text(project.deadlineLabel, color = AppColors.White, fontSize = 11.sp)
            }
        }
        Text(project.description, color = AppColors.Muted, style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = null,
                tint = AppColors.Ink,
                modifier = Modifier.size(17.dp)
            )
            Spacer(Modifier.width(7.dp))
            Text(project.stageLabel, color = AppColors.Ink, fontSize = 13.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ProgressTrack(
                progress = project.progress,
                modifier = Modifier.weight(1f),
                trackColor = AppColors.White.copy(alpha = 0.72f)
            )
            Text(
                text = "${(project.progress * 100).toInt()}%",
                color = AppColors.Ink,
                style = MaterialTheme.typography.titleMedium
            )
        }
        Text(
            text = "${project.completedTasks} из ${project.totalTasks} задач выполнено",
            color = AppColors.Muted,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun ProjectStageRow(
    title: String,
    isCompleted: Boolean,
    isCurrent: Boolean,
    isLast: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(28.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(if (isCompleted) AppColors.Ink else if (isCurrent) AppColors.Yellow else AppColors.Background),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "Этап завершён",
                        tint = AppColors.White,
                        modifier = Modifier.size(16.dp)
                    )
                } else if (isCurrent) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(AppColors.Ink))
                }
            }
            if (!isLast) {
                Box(
                    Modifier
                        .width(2.dp)
                        .height(22.dp)
                        .background(AppColors.Outline)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                color = AppColors.Ink,
                style = MaterialTheme.typography.bodyLarge
            )
            if (isCurrent) {
                Text("Текущий этап", color = AppColors.Muted, fontSize = 11.sp)
            } else if (isCompleted) {
                Text("Завершён", color = AppColors.Muted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun OpenTasksButton(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(AppColors.Yellow)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = AppColors.Ink, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
        Icon(Icons.Outlined.ArrowForward, contentDescription = null, tint = AppColors.Ink)
    }
}
