package com.example.lifedashboard.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onBack: (() -> Unit)? = null,
    onThemeModeChange: (DashboardThemeMode) -> Unit,
    onNotificationsEnabledChange: (Boolean) -> Unit,
    onShowCompletedTasksChange: (Boolean) -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenExactAlarmSettings: () -> Unit,
    onOpenProfile: () -> Unit = {},
    onOpenBackups: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (onBack != null) {
            ScreenTopBar(title = "Настройки", onBackClick = onBack)
        } else {
            Text(
                text = "Настройки",
                style = MaterialTheme.typography.headlineLarge,
                color = AppColors.Ink
            )
        }

        PastelCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenProfile),
            color = AppColors.White,
            contentPadding = PaddingValues(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(AppColors.Lavender),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.PersonOutline, contentDescription = null, tint = AppColors.Ink)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = settings.profileName.ifBlank { "Профиль" },
                        color = AppColors.Ink,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = settings.profileEmail.ifBlank { "Имя и аккаунт" },
                        color = AppColors.Muted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = AppColors.Muted)
            }
        }

        Text("Внешний вид", style = MaterialTheme.typography.bodySmall, color = AppColors.Muted)
        PastelCard(
            modifier = Modifier.fillMaxWidth(),
            color = AppColors.White,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Тема приложения", color = AppColors.Ink, style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeModeOption("Система", DashboardThemeMode.SYSTEM, settings.themeMode, Modifier.weight(1f), onThemeModeChange)
                ThemeModeOption("Светлая", DashboardThemeMode.LIGHT, settings.themeMode, Modifier.weight(1f), onThemeModeChange)
                ThemeModeOption("Тёмная", DashboardThemeMode.DARK, settings.themeMode, Modifier.weight(1f), onThemeModeChange)
            }
        }

        Text("Уведомления", style = MaterialTheme.typography.bodySmall, color = AppColors.Muted)
        PastelCard(
            modifier = Modifier.fillMaxWidth(),
            color = AppColors.White,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SettingSwitchRow(
                title = "Напоминания о привычках",
                description = "Можно временно отключить",
                checked = settings.notificationsEnabled,
                onCheckedChange = onNotificationsEnabledChange
            )
            HorizontalDivider(color = AppColors.Outline.copy(alpha = 0.5f))
            PermissionStatusRow(
                title = "Уведомления Android",
                status = if (notificationsPermissionGranted) "Разрешено" else "Не разрешено",
                actionLabel = if (notificationsPermissionGranted) null else "Настроить",
                onAction = onOpenNotificationSettings
            )
            if (!exactAlarmAccessGranted) {
                HorizontalDivider(color = AppColors.Outline.copy(alpha = 0.5f))
                PermissionStatusRow(
                    title = "Точное время",
                    status = "Будут возможны задержки",
                    actionLabel = "Настроить",
                    onAction = onOpenExactAlarmSettings
                )
            }
        }

        Text("Главный экран", style = MaterialTheme.typography.bodySmall, color = AppColors.Muted)
        PastelCard(
            modifier = Modifier.fillMaxWidth(),
            color = AppColors.White,
            contentPadding = PaddingValues(16.dp)
        ) {
            SettingSwitchRow(
                title = "Показывать выполненные задачи",
                description = "В карточке «Сегодня» на главной",
                checked = settings.showCompletedTasksOnHome,
                onCheckedChange = onShowCompletedTasksChange
            )
        }

        Text("Данные", style = MaterialTheme.typography.bodySmall, color = AppColors.Muted)
        PastelCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenBackups),
            color = AppColors.White,
            contentPadding = PaddingValues(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Резервные копии",
                    color = AppColors.Ink,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = AppColors.Muted)
            }
        }

        if (operationError != null) {
            Text(operationError, color = AppColors.Error, style = MaterialTheme.typography.bodySmall)
        }
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
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = AppColors.Ink, style = MaterialTheme.typography.titleMedium)
            Text(description, color = AppColors.Muted, fontSize = 12.sp)
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
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = AppColors.Ink, style = MaterialTheme.typography.bodyLarge)
            Text(status, color = AppColors.Muted, fontSize = 12.sp)
        }
        if (actionLabel != null) {
            TextButton(onClick = onAction) {
                Text(actionLabel, color = AppColors.Ink, fontWeight = FontWeight.Bold)
            }
        }
    }
}