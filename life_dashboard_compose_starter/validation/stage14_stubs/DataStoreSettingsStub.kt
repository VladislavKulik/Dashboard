package com.example.lifedashboard.data.local
import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
data class StubDashboardSettings(val notificationsEnabled: Boolean = true)
val Context.dashboardSettingsDataStore: Any get() = Any()
class DataStoreSettingsRepository(store: Any) {
    fun observeSettings(): Flow<StubDashboardSettings> = flowOf(StubDashboardSettings())
}
