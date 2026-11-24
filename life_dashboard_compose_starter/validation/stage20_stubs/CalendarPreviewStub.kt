package com.example.lifedashboard.feature.calendar
import androidx.compose.runtime.Composable
import com.example.lifedashboard.data.model.*
@Composable fun CalendarScreen(tasks: List<DashboardTask>, projects: List<DashboardProject>, goals: List<DashboardGoal>, habits: List<DashboardHabit>, activityEvents: List<DashboardActivityEvent>, onBack: () -> Unit, onEventClick: (CalendarEvent) -> Unit) {}
data class CalendarEvent(val id: String)
