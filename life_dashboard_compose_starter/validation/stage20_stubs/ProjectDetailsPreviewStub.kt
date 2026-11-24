package com.example.lifedashboard.feature.projects
import androidx.compose.runtime.Composable
import com.example.lifedashboard.data.model.*
@Composable fun ProjectDetailsScreen(project: DashboardProject, tasks: List<DashboardTask>, onBack: () -> Unit, onToggleTask: (String) -> Unit, onOpenAllTasks: () -> Unit, onToggleStage: (String, String) -> Unit, onSaveProject: (DashboardProject, () -> Unit) -> Unit, onDeleteProject: (String, () -> Unit) -> Unit, operationError: String? = null) {}
