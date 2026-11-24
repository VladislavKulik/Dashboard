package com.example.lifedashboard.data.model
enum class DashboardThemeMode { SYSTEM, LIGHT, DARK }
data class DashboardSettings(val themeMode:DashboardThemeMode=DashboardThemeMode.SYSTEM,val notificationsEnabled:Boolean=true,val showCompletedTasksOnHome:Boolean=true)
enum class TaskDayCategory { TODAY, UPCOMING }
enum class TaskPriority { HIGH, MEDIUM, LOW }
enum class NoteCategory { APP, BLOG, PERSONAL, IDEAS }
enum class NoteColor { YELLOW, PINK, MINT, LAVENDER, BLUE }
enum class HabitColor { PINK, BLUE, YELLOW, MINT, LAVENDER }
enum class GoalColor { BLUE, MINT, YELLOW, PINK, LAVENDER }
