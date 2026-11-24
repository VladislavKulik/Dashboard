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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.components.ScreenTopBar
import com.example.lifedashboard.ui.theme.AppColors

@Composable
fun BackupsScreen(
    backupBusy: Boolean,
    backupMessage: String?,
    backupError: String?,
    onBack: () -> Unit,
    onCreateBackup: () -> Unit,
    onExportData: () -> Unit,
    onImportBackup: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenTopBar(title = "Резервные копии", onBackClick = onBack)

        PastelCard(
            color = AppColors.Mint,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(AppColors.White.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.CloudQueue, contentDescription = null, tint = AppColors.Ink)
                }
                Spacer(Modifier.width(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Полная резервная копия", color = AppColors.Ink, style = MaterialTheme.typography.titleMedium)
                    Text("Все таблицы Room и настройки DataStore", color = AppColors.Muted, fontSize = 12.sp)
                }
            }
            Button(
                onClick = onCreateBackup,
                enabled = !backupBusy,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Ink,
                    contentColor = AppColors.White
                ),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text("Создать копию сейчас", fontWeight = FontWeight.Bold)
            }
        }

        if (backupBusy) {
            Text("Выполняется операция…", color = AppColors.Muted, fontSize = 13.sp)
        }
        backupMessage?.let {
            PastelCard(color = AppColors.White, contentPadding = PaddingValues(14.dp)) {
                Text(it, color = AppColors.Ink, fontSize = 13.sp)
            }
        }
        backupError?.let {
            PastelCard(color = AppColors.Pink, contentPadding = PaddingValues(14.dp)) {
                Text(it, color = AppColors.Error, fontSize = 13.sp)
            }
        }

        PastelCard(
            color = AppColors.White,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !backupBusy, onClick = onImportBackup)
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Восстановить из JSON", color = AppColors.Ink, style = MaterialTheme.typography.titleMedium)
                    Text("Заменяет текущие данные приложения", color = AppColors.Muted, fontSize = 12.sp)
                }
                Text("Восстановить", color = AppColors.Ink, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            HorizontalDivider(color = AppColors.Outline.copy(alpha = 0.5f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !backupBusy, onClick = onExportData)
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Экспорт данных в JSON", color = AppColors.Ink, style = MaterialTheme.typography.titleMedium)
                    Text("Только рабочие данные без настроек", color = AppColors.Muted, fontSize = 12.sp)
                }
                Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = AppColors.Muted)
            }
        }

        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Назад в настройки", color = AppColors.Muted)
        }
    }
}