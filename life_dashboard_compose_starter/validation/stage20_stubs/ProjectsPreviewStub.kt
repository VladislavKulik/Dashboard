package com.example.lifedashboard.feature.projects
import androidx.compose.runtime.Composable
import com.example.lifedashboard.data.model.*
@Composable fun ProjectsScreen(projects: List<DashboardProject>, tasks: List<DashboardTask>, onOpenProject: (String) -> Unit, onToggleTask: (String) -> Unit, onSaveProject: (DashboardProject, () -> Unit) -> Unit, operationError: String? = null) {}
