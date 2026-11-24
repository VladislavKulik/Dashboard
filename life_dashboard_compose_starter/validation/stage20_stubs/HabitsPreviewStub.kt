package com.example.lifedashboard.feature.habits
import androidx.compose.runtime.Composable
import com.example.lifedashboard.data.model.DashboardHabit
@Composable fun HabitsScreen(habits: List<DashboardHabit>, onBack: () -> Unit, onOpenHabit: (String) -> Unit, onSaveHabit: (DashboardHabit, (() -> Unit)?) -> Unit, onToggleToday: (String) -> Unit, operationError: String? = null) {}
