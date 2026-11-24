package com.example.lifedashboard.feature.habits
import androidx.compose.runtime.Composable
import com.example.lifedashboard.data.model.DashboardHabit
@Composable fun HabitDetailsScreen(habit: DashboardHabit, onBack: () -> Unit, onToggleToday: () -> Unit, onSaveHabit: (DashboardHabit, (() -> Unit)?) -> Unit, onDeleteHabit: (String, (() -> Unit)?) -> Unit, operationError: String? = null) {}
