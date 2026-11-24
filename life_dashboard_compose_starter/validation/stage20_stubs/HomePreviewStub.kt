package com.example.lifedashboard.feature.home
import androidx.compose.runtime.Composable
import com.example.lifedashboard.data.model.DashboardTask
@Composable fun HomeScreen(tasks: List<DashboardTask>, activeProjectCount: Int, onToggleTask: (String) -> Unit, onOpenTasks: () -> Unit, onOpenProjects: () -> Unit, showCompletedTasks: Boolean = true) {}
