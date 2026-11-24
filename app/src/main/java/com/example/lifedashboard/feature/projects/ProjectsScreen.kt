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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.data.model.DashboardGoal
import com.example.lifedashboard.data.model.DashboardNote
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.data.model.DashboardTask
import com.example.lifedashboard.ui.components.CircularProgressRing
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.components.ScreenTopBar
import com.example.lifedashboard.ui.theme.AppColors

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
    onOpenSearch: () -> Unit = {}
) {
    var showActive by remember { mutableStateOf(true) }
    var showCreateDialog by remember(startWithCreate) { mutableStateOf(startWithCreate && onCreateProject == null) }

    val activeCount = projects.count { !it.isCompleted }
    val completedCount = projects.count { it.isCompleted }
    val visibleProjects = projects.filter { it.isCompleted != showActive }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ScreenTopBar(
                    title = "Проекты",
                    actionIcon = Icons.Outlined.Search,
                    actionDescription = "Поиск проектов",
                    onActionClick = onOpenSearch,
                    onBackClick = onBack
                )
            }

            if (!operationError.isNullOrBlank()) {
                item { Text(operationError, color = AppColors.Error, style = MaterialTheme.typography.bodySmall) }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(AppColors.White)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ProjectSegmentPill(
                        label = "Активные $activeCount",
                        selected = showActive,
                        onClick = { showActive = true },
                        modifier = Modifier.weight(1f)
                    )
                    ProjectSegmentPill(
                        label = "Завершены $completedCount",
                        selected = !showActive,
                        onClick = { showActive = false },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (visibleProjects.isEmpty()) {
                item {
                    PastelCard(color = AppColors.White, modifier = Modifier.fillMaxWidth()) {
                        Text("Проектов пока нет", style = MaterialTheme.typography.titleLarge, color = AppColors.Ink)
                        Text("Нажми «+», чтобы создать первый проект.", color = AppColors.Muted)
                    }
                }
            } else {
                items(visibleProjects, key = { it.id }) { project ->
                    val taskCount = tasks.count { it.projectId == project.id }
                    val goalCount = goals.count { it.projectId == project.id }
                    val noteCount = notes.count { it.projectId == project.id }
                    ProjectCard(
                        project = project,
                        taskCount = taskCount,
                        goalCount = goalCount,
                        noteCount = noteCount,
                        onClick = { onOpenProject(project.id) }
                    )
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .size(60.dp)
                .clip(CircleShape)
                .clickable(role = Role.Button) {
                    if (onCreateProject != null) onCreateProject() else showCreateDialog = true
                },
            color = AppColors.Yellow,
            shape = CircleShape,
            shadowElevation = 6.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Add, contentDescription = "Новый проект", tint = AppColors.Ink, modifier = Modifier.size(26.dp))
            }
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
}

@Composable
private fun ProjectSegmentPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(if (selected) AppColors.Ink else Color.Transparent)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) AppColors.White else AppColors.Ink,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ProjectCard(
    project: DashboardProject,
    taskCount: Int,
    goalCount: Int,
    noteCount: Int,
    onClick: () -> Unit
) {
    val initial = project.name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "П"
    val deadlineText = if (project.deadlineLabel.equals("Без срока", ignoreCase = true) ||
        project.deadlineLabel.equals("Готово", ignoreCase = true)
    ) {
        project.deadlineLabel.lowercase()
    } else {
        "до ${project.deadlineLabel}"
    }
    val links = buildList {
        add("$taskCount зад.")
        if (goalCount > 0) add("$goalCount цел.")
        if (noteCount > 0) add("$noteCount зам.")
    }.joinToString(" · ")

    PastelCard(
        color = Color(project.colorArgb),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick),
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