package com.example.lifedashboard.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lifedashboard.data.model.DashboardSettings
import com.example.lifedashboard.data.model.DashboardThemeMode
import com.example.lifedashboard.ui.components.FilterPill
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.components.ScreenTopBar
import com.example.lifedashboard.ui.theme.AppColors

@Composable
fun SettingsScreen(
    settings: DashboardSettings,
    notificationsPermissionGranted: Boolean,
    exactAlarmAccessGranted: Boolean,
    operationError: String? = null,
    backupBusy: Boolean = false,
    backupMessage: String? = null,
    backupError: String? = null,
    onExportData: () -> Unit = {},
    onCreateBackup: () -> Unit = {},
    onImportBackup: () -> Unit = {},
    onBack: () -> Unit,
    onThemeModeChange: (DashboardThemeMode) -> Unit,
    onNotificationsEnabledChange: (Boolean) -> Unit,
    onShowCompletedTasksChange: (Boolean) -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenExactAlarmSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        ScreenTopBar(title = "Настройки", onBackClick = onBack)

        Text(
            text = "Внешний вид",
            style = MaterialTheme.typography.titleMedium,
            color = AppColors.Muted
        )
        PastelCard(
            modifier = Modifier.fillMaxWidth(),
            color = AppColors.White,
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Тема приложения", color = AppColors.Ink, style = MaterialTheme.typography.titleLarge)
                Text("Светлая, тёмная или по настройкам устройства", color = AppColors.Muted, style = MaterialTheme.typography.bodySmall)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeModeOption("Система", DashboardThemeMode.SYSTEM, settings.themeMode, Modifier.weight(1f), onThemeModeChange)
                ThemeModeOption("Светлая", DashboardThemeMode.LIGHT, settings.themeMode, Modifier.weight(1f), onThemeModeChange)
                ThemeModeOption("Тёмная", DashboardThemeMode.DARK, settings.themeMode, Modifier.weight(1f), onThemeModeChange)
            }
        }

        Text(
            text = "Уведомления",
            style = MaterialTheme.typography.titleMedium,
            color = AppColors.Muted
        )
        PastelCard(
            modifier = Modifier.fillMaxWidth(),
            color = AppColors.White,
            contentPadding = PaddingValues(18.dp)
        ) {
            SettingSwitchRow(
                title = "Напоминания о привычках",
                description = "Разрешить или временно отключить все напоминания приложения",
                checked = settings.notificationsEnabled,
                onCheckedChange = onNotificationsEnabledChange
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                PermissionStatusRow(
                    title = "Уведомления Android",
                    status = if (notificationsPermissionGranted) "Разрешено" else "Не разрешено",
                    actionLabel = if (notificationsPermissionGranted) null else "Настроить",
                    onAction = onOpenNotificationSettings
                )
                PermissionStatusRow(
                    title = "Точное время",
                    status = if (exactAlarmAccessGranted) "Доступ есть" else "Будут возможны задержки",
                    actionLabel = if (exactAlarmAccessGranted) null else "Настроить",
                    onAction = onOpenExactAlarmSettings
                )
            }
        }

        Text(
            text = "Главный экран",
            style = MaterialTheme.typography.titleMedium,
            color = AppColors.Muted
        )
        PastelCard(
            modifier = Modifier.fillMaxWidth(),
            color = AppColors.White,
            contentPadding = PaddingValues(18.dp)
        ) {
            SettingSwitchRow(
                title = "Показывать выполненные задачи",
                description = "Не скрывать выполненные задачи в блоке задач на главной",
                checked = settings.showCompletedTasksOnHome,
                onCheckedChange = onShowCompletedTasksChange
            )
        }

        Text(
            text = "Данные и резервные копии",
            style = MaterialTheme.typography.titleMedium,
            color = AppColors.Muted
        )
        PastelCard(
            modifier = Modifier.fillMaxWidth(),
            color = AppColors.White,
            contentPadding = PaddingValues(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Экспорт данных", color = AppColors.Ink, style = MaterialTheme.typography.titleMedium)
                Text(
                    "JSON-файл с задачами, проектами, заметками, привычками, целями и историей активности.",
                    color = AppColors.Muted,
                    style = MaterialTheme.typography.bodySmall
                )
                Button(onClick = onExportData, enabled = !backupBusy, modifier = Modifier.fillMaxWidth()) {
                    Text("Экспортировать данные")
                }
                Button(onClick = onCreateBackup, enabled = !backupBusy, modifier = Modifier.fillMaxWidth()) {
                    Text("Создать полную резервную копию")
                }
                TextButton(onClick = onImportBackup, enabled = !backupBusy) {
                    Text("Восстановить из JSON")
                }
                if (backupBusy) Text("Выполняется операция…", color = AppColors.Muted, style = MaterialTheme.typography.bodySmall)
                backupMessage?.let { Text(it, color = AppColors.Ink, style = MaterialTheme.typography.bodySmall) }
                backupError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                Text(
                    "Восстановление заменяет текущие данные приложения. Перед импортом будет показано подтверждение.",
                    color = AppColors.Muted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        if (operationError != null) {
            Text(operationError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Text(
            text = "Настройки сохраняются автоматически на этом устройстве.",
            color = AppColors.Muted,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ThemeModeOption(
    label: String,
    value: DashboardThemeMode,
    selected: DashboardThemeMode,
    modifier: Modifier = Modifier,
    onSelected: (DashboardThemeMode) -> Unit
) {
    FilterPill(
        label = label,
        selected = selected == value,
        onClick = { onSelected(value) },
        modifier = modifier
    )
}

@Composable
private fun SettingSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = AppColors.Ink, style = MaterialTheme.typography.titleMedium)
            Text(description, color = AppColors.Muted, style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun PermissionStatusRow(
    title: String,
    status: String,
    actionLabel: String?,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = AppColors.Ink, style = MaterialTheme.typography.bodyMedium)
            Text(status, color = AppColors.Muted, style = MaterialTheme.typography.bodySmall)
        }
        if (actionLabel != null) {
            TextButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}
