import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.lifedashboard.data.local.DataStoreSettingsRepository
import com.example.lifedashboard.data.model.DashboardThemeMode
import com.example.lifedashboard.data.model.DashboardSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private class MemorySettingsDataStore : DataStore<Preferences> {
    private val state = MutableStateFlow<Preferences>(Preferences())
    override val data = state
    override suspend fun update(transform: (Preferences) -> Preferences) {
        state.value = transform(state.value)
    }
}

fun main() = runBlocking {
    val repo = DataStoreSettingsRepository(MemorySettingsDataStore())
    val defaults = repo.observeSettings().first()
    check(defaults.themeMode == DashboardThemeMode.SYSTEM)
    check(defaults.notificationsEnabled)
    check(defaults.showCompletedTasksOnHome)

    repo.setThemeMode(DashboardThemeMode.DARK)
    repo.setNotificationsEnabled(false)
    repo.setShowCompletedTasksOnHome(false)
    val saved = repo.observeSettings().first()
    check(saved.themeMode == DashboardThemeMode.DARK)
    check(!saved.notificationsEnabled)
    check(!saved.showCompletedTasksOnHome)

    repo.setThemeMode(DashboardThemeMode.LIGHT)
    check(repo.observeSettings().first().themeMode == DashboardThemeMode.LIGHT)

    repo.replaceSettings(
        DashboardSettings(
            themeMode = DashboardThemeMode.DARK,
            notificationsEnabled = false,
            showCompletedTasksOnHome = true
        )
    )
    val restored = repo.observeSettings().first()
    check(restored.themeMode == DashboardThemeMode.DARK)
    check(!restored.notificationsEnabled)
    check(restored.showCompletedTasksOnHome)
    println("Settings DataStore persistence + backup restore checks passed")
}
