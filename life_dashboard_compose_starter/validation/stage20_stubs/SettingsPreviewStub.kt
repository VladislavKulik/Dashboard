package com.example.lifedashboard.feature.settings
import androidx.compose.runtime.Composable
import com.example.lifedashboard.data.model.DashboardSettings
import com.example.lifedashboard.data.model.DashboardThemeMode
@Composable fun SettingsScreen(settings: DashboardSettings, notificationsPermissionGranted: Boolean, exactAlarmAccessGranted: Boolean, operationError: String? = null, backupBusy: Boolean = false, backupMessage: String? = null, backupError: String? = null, onExportData: () -> Unit = {}, onCreateBackup: () -> Unit = {}, onImportBackup: () -> Unit = {}, onBack: () -> Unit, onThemeModeChange: (DashboardThemeMode) -> Unit, onNotificationsEnabledChange: (Boolean) -> Unit, onShowCompletedTasksChange: (Boolean) -> Unit, onOpenNotificationSettings: () -> Unit, onOpenExactAlarmSettings: () -> Unit) {}
