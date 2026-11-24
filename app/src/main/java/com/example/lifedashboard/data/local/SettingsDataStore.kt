package com.example.lifedashboard.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.lifedashboard.data.model.DashboardSettings
import com.example.lifedashboard.data.model.DashboardThemeMode
import com.example.lifedashboard.data.model.HomeBlockConfig
import com.example.lifedashboard.data.repository.SettingsRepository
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/** One DataStore instance per application process. */
val Context.dashboardSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "dashboard_settings"
)

class DataStoreSettingsRepository(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    override fun observeSettings(): Flow<DashboardSettings> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences ->
            DashboardSettings(
                themeMode = preferences[THEME_MODE]
                    ?.let { runCatching { DashboardThemeMode.valueOf(it) }.getOrNull() }
                    ?: DashboardThemeMode.SYSTEM,
                notificationsEnabled = preferences[NOTIFICATIONS_ENABLED] ?: true,
                showCompletedTasksOnHome = preferences[SHOW_COMPLETED_TASKS] ?: true,
                homeBlocks = HomeBlockConfig.decode(preferences[HOME_BLOCKS]),
                profileName = preferences[PROFILE_NAME].orEmpty(),
                profileEmail = preferences[PROFILE_EMAIL].orEmpty(),
                dailyTaskGoal = (preferences[DAILY_TASK_GOAL] ?: 5).coerceIn(1, 50)
            )
        }

    override suspend fun setThemeMode(mode: DashboardThemeMode) {
        dataStore.edit { it[THEME_MODE] = mode.name }
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[NOTIFICATIONS_ENABLED] = enabled }
    }

    override suspend fun setShowCompletedTasksOnHome(enabled: Boolean) {
        dataStore.edit { it[SHOW_COMPLETED_TASKS] = enabled }
    }

    override suspend fun setHomeBlocks(blocks: List<HomeBlockConfig>) {
        dataStore.edit { it[HOME_BLOCKS] = HomeBlockConfig.encode(blocks) }
    }

    override suspend fun setProfile(name: String, email: String, dailyTaskGoal: Int) {
        dataStore.edit { preferences ->
            preferences[PROFILE_NAME] = name.trim()
            preferences[PROFILE_EMAIL] = email.trim()
            preferences[DAILY_TASK_GOAL] = dailyTaskGoal.coerceIn(1, 50)
        }
    }

    override suspend fun replaceSettings(settings: DashboardSettings) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE] = settings.themeMode.name
            preferences[NOTIFICATIONS_ENABLED] = settings.notificationsEnabled
            preferences[SHOW_COMPLETED_TASKS] = settings.showCompletedTasksOnHome
            preferences[HOME_BLOCKS] = HomeBlockConfig.encode(settings.homeBlocks)
            preferences[PROFILE_NAME] = settings.profileName.trim()
            preferences[PROFILE_EMAIL] = settings.profileEmail.trim()
            preferences[DAILY_TASK_GOAL] = settings.dailyTaskGoal.coerceIn(1, 50)
        }
    }

    private companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val SHOW_COMPLETED_TASKS = booleanPreferencesKey("show_completed_tasks_on_home")
        val HOME_BLOCKS = stringPreferencesKey("home_blocks")
        val PROFILE_NAME = stringPreferencesKey("profile_name")
        val PROFILE_EMAIL = stringPreferencesKey("profile_email")
        val DAILY_TASK_GOAL = intPreferencesKey("daily_task_goal")
    }
}