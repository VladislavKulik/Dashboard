package com.example.lifedashboard.feature.tasks
import androidx.compose.runtime.Composable
import com.example.lifedashboard.data.model.*
@Composable fun TasksScreen(tasks: List<DashboardTask>, projects: List<DashboardProject>, initialProjectId: String? = null, onToggleTask: (String) -> Unit, onAddTask: (String, TaskPriority, String?, java.time.LocalDate?, com.example.lifedashboard.data.model.TaskRecurrence, Set<Int>, Boolean, String) -> Unit, onOpenProject: (String) -> Unit, onUpdateTask: (DashboardTask, () -> Unit) -> Unit, onDeleteTask: (String, () -> Unit) -> Unit, operationError: String? = null, initialTaskId: String? = null, startWithCreate: Boolean = false) {}
