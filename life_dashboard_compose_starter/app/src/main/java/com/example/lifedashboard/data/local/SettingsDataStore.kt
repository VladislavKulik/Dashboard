package com.example.lifedashboard.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.lifedashboard.data.model.DashboardSettings
import com.example.lifedashboard.data.model.DashboardThemeMode
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
                showCompletedTasksOnHome = preferences[SHOW_COMPLETED_TASKS] ?: true
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

    override suspend fun replaceSettings(settings: DashboardSettings) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE] = settings.themeMode.name
            preferences[NOTIFICATIONS_ENABLED] = settings.notificationsEnabled
            preferences[SHOW_COMPLETED_TASKS] = settings.showCompletedTasksOnHome
        }
    }

    private companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val SHOW_COMPLETED_TASKS = booleanPreferencesKey("show_completed_tasks_on_home")
    }
}
