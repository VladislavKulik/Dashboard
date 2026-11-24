package com.example.lifedashboard.data.repository

import com.example.lifedashboard.data.model.DashboardSettings
import com.example.lifedashboard.data.model.DashboardThemeMode
import com.example.lifedashboard.data.model.HomeBlockConfig
import kotlinx.coroutines.flow.Flow

/** Small persistence contract for app preferences; feature data remains in Room. */
interface SettingsRepository {
    fun observeSettings(): Flow<DashboardSettings>
    suspend fun setThemeMode(mode: DashboardThemeMode)
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setShowCompletedTasksOnHome(enabled: Boolean)
    suspend fun setHomeBlocks(blocks: List<HomeBlockConfig>)
    suspend fun setProfile(name: String, email: String, dailyTaskGoal: Int)
    /** Atomically replaces all app preferences when a full backup is restored. */
    suspend fun replaceSettings(settings: DashboardSettings)
}