package com.example.lifedashboard.feature.goals
import androidx.compose.runtime.Composable
import com.example.lifedashboard.data.model.*
@Composable fun GoalsScreen(goals: List<DashboardGoal>, projects: List<DashboardProject>, onBack: () -> Unit, onSaveGoal: (DashboardGoal, (() -> Unit)?) -> Unit, onDeleteGoal: (String, (() -> Unit)?) -> Unit, onChangeProgress: (String, Int) -> Unit, operationError: String? = null, initialGoalId: String? = null) {}
