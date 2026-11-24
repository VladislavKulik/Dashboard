package com.example.lifedashboard.data.model

/** Persisted, app-wide preferences. Defaults preserve the current light-dashboard experience. */
enum class DashboardThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class DashboardSettings(
    val themeMode: DashboardThemeMode = DashboardThemeMode.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val showCompletedTasksOnHome: Boolean = true
)
