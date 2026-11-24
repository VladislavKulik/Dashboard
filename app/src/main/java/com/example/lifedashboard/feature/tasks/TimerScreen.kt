package com.example.lifedashboard.feature.tasks

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.presentation.TaskTimerState
import com.example.lifedashboard.ui.components.CircularProgressRing
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.components.ScreenTopBar
import com.example.lifedashboard.ui.theme.AppColors

@Composable
fun TimerScreen(
    timerState: TaskTimerState,
    onBack: () -> Unit,
    onTogglePause: () -> Unit,
    onStop: () -> Unit
) {
    val taskId = timerState.activeTaskId
    val totalTaskSeconds = if (taskId != null) timerState.totalSecondsForTask(taskId) else 0L
    val totalProjectTodaySeconds = timerState.totalSecondsForProjectToday(timerState.activeProjectId)
    val ringProgress = if (timerState.plannedSeconds <= 0L) 0f else {
        (timerState.elapsedSeconds.toFloat() / timerState.plannedSeconds.toFloat()).coerceIn(0f, 1f)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ScreenTopBar(title = "Таймер", onBackClick = onBack)

            PastelCard(
                color = AppColors.Lavender,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 26.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = timerState.activeTaskTitle.ifBlank { "Фокус-сессия" },
                        style = MaterialTheme.typography.titleLarge,
                        color = AppColors.Ink,
                        textAlign = TextAlign.Center
                    )
                    timerState.activeProjectName?.let { projectName ->
                        Text(projectName, color = AppColors.Muted, fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(14.dp))
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressRing(
                            progress = ringProgress,
                            size = 216.dp,
                            strokeWidth = 14.dp,
                            trackColor = AppColors.Ink.copy(alpha = 0.12f),
                            progressColor = AppColors.Ink,
                            label = null
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = TaskTimerState.formatHms(timerState.elapsedSeconds),
                                color = AppColors.Ink,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "план ${TaskTimerState.formatShort(timerState.plannedSeconds)}",
                                color = AppColors.Muted,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PastelCard(
                    modifier = Modifier.weight(1f),
                    color = AppColors.White,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (totalTaskSeconds == 0L) "0 мин" else TaskTimerState.formatShort(totalTaskSeconds),
                        color = AppColors.Ink,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text("по задаче", color = AppColors.Muted, fontSize = 12.sp)
                }

                PastelCard(
                    modifier = Modifier.weight(1f),
                    color = AppColors.White,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (totalProjectTodaySeconds == 0L) "0 мин" else TaskTimerState.formatShort(totalProjectTodaySeconds),
                        color = AppColors.Ink,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text("по проекту сегодня", color = AppColors.Muted, fontSize = 12.sp)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(AppColors.Ink)
                    .clickable(onClick = onTogglePause),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (timerState.isRunning) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                    contentDescription = if (timerState.isRunning) "Пауза" else "Продолжить",
                    tint = AppColors.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Surface(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onStop),
                color = AppColors.White,
                shape = CircleShape,
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Stop,
                        contentDescription = "Завершить",
                        tint = AppColors.Ink,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}