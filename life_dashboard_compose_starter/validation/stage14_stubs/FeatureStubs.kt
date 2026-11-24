package com.example.lifedashboard.data.model
enum class HabitColor { PINK, BLUE, YELLOW, MINT, LAVENDER }
data class DashboardHabit(
 val id: String,
 val title: String,
 val reminderEnabled: Boolean,
 val reminderTime: String,
 val reminderDays: Set<Int>
)

 data class DashboardTask(val id: String)
