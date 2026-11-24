package com.example.lifedashboard.feature.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.data.DateKeys
import com.example.lifedashboard.data.HabitMetrics
import com.example.lifedashboard.data.model.DashboardHabit
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.components.ScreenTopBar
import com.example.lifedashboard.ui.theme.AppColors

@Composable
fun HabitDetailsScreen(
    habit: DashboardHabit,
    onBack: () -> Unit,
    onToggleToday: () -> Unit,
    onSaveHabit: (DashboardHabit, (() -> Unit)?) -> Unit,
    onDeleteHabit: (String, (() -> Unit)?) -> Unit,
    operationError: String? = null
) {
    val streak = HabitMetrics.currentStreak(habit.completionDates)
    val todayCompleted = DateKeys.today() in habit.completionDates
    val week = DateKeys.lastDays(7)
    var showEditor by rememberSaveable(habit.id) { mutableStateOf(false) }
    var showDeleteConfirmation by rememberSaveable(habit.id) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        ScreenTopBar(
            title = "Привычка",
            actionIcon = Icons.Outlined.Edit,
            actionDescription = "Редактировать привычку",
            onActionClick = { showEditor = true },
            onBackClick = onBack
        )

        if (operationError != null) {
            Text(operationError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        PastelCard(
            modifier = Modifier.fillMaxWidth(),
            color = habitColor(habit.color),
            contentPadding = PaddingValues(22.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Text(text = habit.title, color = AppColors.Ink, style = MaterialTheme.typography.headlineMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Серия",
                    color = AppColors.Ink.copy(alpha = 0.68f),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text("$streak дней", color = AppColors.Ink, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Напоминание",
                    color = AppColors.Ink.copy(alpha = 0.68f),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = reminderSummary(habit),
                    color = AppColors.Ink,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }

        PastelCard(
            modifier = Modifier.fillMaxWidth(),
            color = AppColors.White,
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Последние 7 дней", color = AppColors.Ink, style = MaterialTheme.typography.titleLarge)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                week.forEach { key ->
                    val checked = key in habit.completionDates
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = DateKeys.weekdayLabel(key),
                            color = AppColors.Muted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .then(
                                    if (checked) Modifier.background(AppColors.Ink)
                                    else Modifier.border(1.5.dp, AppColors.Ink.copy(alpha = 0.65f), CircleShape)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (checked) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = "Выполнено",
                                    tint = AppColors.White,
                                    modifier = Modifier.size(17.dp)
                                )
                            } else {
                                Text(DateKeys.dayOfMonthLabel(key), color = AppColors.Muted, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
            Text(
                text = "Отмеченные дни показывают историю выполнения привычки. Последний кружок соответствует сегодняшнему дню.",
                color = AppColors.Muted,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Button(
            onClick = onToggleToday,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (todayCompleted) AppColors.White else AppColors.Ink,
                contentColor = if (todayCompleted) AppColors.Ink else AppColors.White
            ),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            Text(
                text = if (todayCompleted) "Отменить отметку за сегодня" else "Отметить выполнение сегодня",
                fontWeight = FontWeight.Bold
            )
        }

        TextButton(onClick = { showDeleteConfirmation = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Удалить привычку", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
        }
    }

    if (showEditor) {
        HabitEditorDialog(
            initialHabit = habit,
            operationError = operationError,
            onDismiss = { showEditor = false },
            onSave = { updated -> onSaveHabit(updated) { showEditor = false } }
        )
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Удалить привычку?", color = AppColors.Ink, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "«${habit.title}» и история её отметок будут удалены. Это действие нельзя отменить.",
                        color = AppColors.Muted
                    )
                    if (operationError != null) {
                        Text(operationError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteHabit(habit.id) { showDeleteConfirmation = false }
                }) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) { Text("Отмена", color = AppColors.Muted) }
            },
            containerColor = AppColors.White
        )
    }
}

private fun reminderSummary(habit: DashboardHabit): String {
    if (!habit.reminderEnabled) return "Выключено"
    val dayNames = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    val days = habit.reminderDays.sorted().mapNotNull { dayNames.getOrNull(it - 1) }
    return "${days.joinToString(" ")} · ${habit.reminderTime}"
}
