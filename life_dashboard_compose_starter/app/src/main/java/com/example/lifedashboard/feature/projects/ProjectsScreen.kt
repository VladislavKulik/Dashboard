package com.example.lifedashboard.feature.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.data.model.DashboardTask
import com.example.lifedashboard.ui.components.CircleIconButton
import com.example.lifedashboard.ui.components.FilterPill
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.components.ProgressTrack
import com.example.lifedashboard.ui.components.TaskRow
import com.example.lifedashboard.ui.theme.AppColors

@Composable
fun ProjectsScreen(
    projects: List<DashboardProject>,
    tasks: List<DashboardTask>,
    onOpenProject: (String) -> Unit,
    onToggleTask: (String) -> Unit,
    onSaveProject: (DashboardProject, () -> Unit) -> Unit,
    operationError: String? = null,
    startWithCreate: Boolean = false
) {
    var showActive by remember { mutableStateOf(true) }
    var showCreateDialog by remember(startWithCreate) { mutableStateOf(startWithCreate) }
    val visibleProjects = projects.filter { it.isCompleted != showActive }
    val activeCount = projects.count { !it.isCompleted }
    val completedCount = projects.count { it.isCompleted }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Проекты",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineLarge,
                    color = AppColors.Ink
                )
                CircleIconButton(
                    icon = Icons.Outlined.Add,
                    contentDescription = "Добавить проект",
                    onClick = { showCreateDialog = true }
                )
            }
        }
        if (!operationError.isNullOrBlank()) {
            item { Text(operationError, color = AppColors.Error, style = MaterialTheme.typography.bodySmall) }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterPill("Активные", showActive, { showActive = true }, count = activeCount.toString())
                FilterPill("Завершены", !showActive, { showActive = false }, count = completedCount.toString())
            }
        }
        if (visibleProjects.isEmpty()) {
            item {
                PastelCard(color = AppColors.Lavender) {
                    Text("Проектов пока нет", style = MaterialTheme.typography.titleLarge, color = AppColors.Ink)
                    Text("Здесь появятся проекты после их создания или завершения.", color = AppColors.Muted)
                }
            }
        } else {
            items(visibleProjects, key = { it.id }) { project ->
                ProjectCard(
                    project = project,
                    previewTasks = tasks.filter { it.projectId == project.id }.take(2),
                    onClick = { onOpenProject(project.id) },
                    onToggleTask = onToggleTask
                )
            }
        }
    }

    CreateProjectDialogHost(
        visible = showCreateDialog,
        onDismiss = { showCreateDialog = false },
        onSaveProject = onSaveProject,
        operationError = operationError
    )
}

@Composable
private fun ProjectCard(
    project: DashboardProject,
    previewTasks: List<DashboardTask>,
    onClick: () -> Unit,
    onToggleTask: (String) -> Unit
) {
    PastelCard(
        color = Color(project.colorArgb),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = project.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                color = AppColors.Ink
            )
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .background(AppColors.Ink, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(project.deadlineLabel, color = AppColors.White, fontSize = 12.sp)
            }
        }
        Text(
            text = "${project.stageLabel} · ${project.completedTasks} из ${project.totalTasks} задач",
            color = AppColors.Muted,
            fontSize = 13.sp
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ProgressTrack(project.progress, modifier = Modifier.weight(1f))
            Text("${(project.progress * 100).toInt()}%", color = AppColors.Ink, style = MaterialTheme.typography.labelLarge)
        }
        if (previewTasks.isNotEmpty()) {
            PastelCard(
                color = AppColors.White,
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                previewTasks.forEach { task ->
                    TaskRow(
                        title = task.title,
                        completed = task.isCompleted,
                        onToggle = { onToggleTask(task.id) }
                    )
                }
                Text("Открыть проект →", color = AppColors.Muted, fontSize = 12.sp)
            }
        }
    }
}


@Composable
private fun CreateProjectDialogHost(
    visible: Boolean,
    onDismiss: () -> Unit,
    onSaveProject: (DashboardProject, () -> Unit) -> Unit,
    operationError: String?
) {
    if (visible) {
        ProjectEditorDialog(
            initialProject = null,
            onDismiss = onDismiss,
            onSave = { project -> onSaveProject(project, onDismiss) },
            operationError = operationError
        )
    }
}
