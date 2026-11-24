package com.example.lifedashboard.feature.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.data.DateKeys
import com.example.lifedashboard.data.HabitMetrics
import com.example.lifedashboard.data.model.DashboardHabit
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.components.ScreenTopBar
import com.example.lifedashboard.ui.theme.AppColors

@Composable
fun HabitsScreen(
    habits: List<DashboardHabit>,
    onBack: () -> Unit,
    onOpenHabit: (String) -> Unit,
    onSaveHabit: (DashboardHabit, (() -> Unit)?) -> Unit,
    onToggleToday: (String) -> Unit,
    operationError: String? = null,
    startWithCreate: Boolean = false
) {
    var showAddDialog by rememberSaveable(startWithCreate) { mutableStateOf(startWithCreate) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ScreenTopBar(
                title = "Привычки",
                actionIcon = Icons.Outlined.Add,
                actionDescription = "Добавить привычку",
                onActionClick = { showAddDialog = true },
                onBackClick = onBack
            )
        }
        item {
            ActivityCalendarCard(habits = habits)
        }
        if (operationError != null) {
            item {
                Text(
                    text = operationError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Мои привычки",
                    modifier = Modifier.weight(1f),
                    color = AppColors.Ink,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = habits.size.toString(),
                    color = AppColors.Muted,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        if (habits.isEmpty()) {
            item { EmptyHabitsState(onAddClick = { showAddDialog = true }) }
        } else {
            items(habits.size, key = { habits[it].id }) { index ->
                val habit = habits[index]
                HabitCard(
                    habit = habit,
                    onClick = { onOpenHabit(habit.id) },
                    onToggleToday = { onToggleToday(habit.id) }
                )
            }
        }
    }

    if (showAddDialog) {
        HabitEditorDialog(
            operationError = operationError,
            onDismiss = { showAddDialog = false },
            onSave = { habit ->
                onSaveHabit(habit) { showAddDialog = false }
            }
        )
    }
}

@Composable
fun ActivityCalendarCard(
    habits: List<DashboardHabit>,
    title: String = "Календарь активности"
) {
    val monthGrid = DateKeys.currentMonthGrid()
    val activityPercent = HabitMetrics.monthlyActivityPercent(habits)
    val weekdays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

    PastelCard(
        modifier = Modifier.fillMaxWidth(),
        color = AppColors.Yellow,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                color = AppColors.Ink,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(AppColors.White.copy(alpha = 0.82f))
                    .padding(horizontal = 15.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "$activityPercent%",
                    color = AppColors.Ink,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
        Text(
            text = DateKeys.currentMonthTitle(),
            color = AppColors.Ink.copy(alpha = 0.68f),
            style = MaterialTheme.typography.labelMedium
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            weekdays.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    color = AppColors.Ink.copy(alpha = 0.58f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            monthGrid.chunked(7).forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    week.forEach { key ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(25.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (key != null) {
                                val completedCount = HabitMetrics.completedHabitCount(habits, key)
                                val cellColor = when {
                                    completedCount == 0 -> AppColors.White.copy(alpha = 0.50f)
                                    completedCount == 1 -> AppColors.Ink.copy(alpha = 0.16f)
                                    else -> AppColors.Ink.copy(alpha = 0.42f)
                                }
                                val isToday = key == DateKeys.today()
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(cellColor)
                                        .then(
                                            if (isToday) Modifier.border(1.5.dp, AppColors.Ink, RoundedCornerShape(8.dp))
                                            else Modifier
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = DateKeys.dayOfMonthLabel(key),
                                        color = AppColors.Ink,
                                        fontSize = 9.sp,
                                        fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CalendarLegendItem(color = AppColors.White.copy(alpha = 0.62f), label = "Нет отметок")
            CalendarLegendItem(color = AppColors.Ink.copy(alpha = 0.16f), label = "1 привычка")
            CalendarLegendItem(color = AppColors.Ink.copy(alpha = 0.42f), label = "2+ привычки")
        }
    }
}

@Composable
private fun CalendarLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(Modifier.size(9.dp).clip(RoundedCornerShape(3.dp)).background(color))
        Text(text = label, color = AppColors.Ink.copy(alpha = 0.7f), fontSize = 9.sp, maxLines = 1)
    }
}

@Composable
private fun HabitCard(
    habit: DashboardHabit,
    onClick: () -> Unit,
    onToggleToday: () -> Unit
) {
    val color = habitColor(habit.color)
    val history = DateKeys.lastDays(7)
    val streak = HabitMetrics.currentStreak(habit.completionDates)
    PastelCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = color,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = habit.title,
                modifier = Modifier.weight(1f),
                color = AppColors.Ink,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(AppColors.Ink)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Серия $streak",
                    color = AppColors.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            history.forEachIndexed { index, key ->
                val checked = key in habit.completionDates
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = DateKeys.weekdayLabel(key).take(2),
                        color = AppColors.Ink.copy(alpha = 0.58f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .then(
                                if (checked) Modifier.background(AppColors.Ink)
                                else Modifier.border(1.8.dp, AppColors.Ink, CircleShape)
                            )
                            .then(if (index == history.lastIndex) Modifier.clickable(onClick = onToggleToday) else Modifier)
                            .semantics {
                                if (index == history.lastIndex) {
                                    role = Role.Button
                                    contentDescription = if (checked) "Снять отметку за сегодня" else "Отметить выполнение сегодня"
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (checked) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = if (index == history.lastIndex) "Отметить сегодня выполненным" else null,
                                tint = AppColors.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
        Text(
            text = if (DateKeys.today() in habit.completionDates) "Сегодня выполнено" else "Нажми на последний кружок, чтобы отметить сегодня",
            color = AppColors.Ink.copy(alpha = 0.65f),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EmptyHabitsState(onAddClick: () -> Unit) {
    PastelCard(
        modifier = Modifier.fillMaxWidth(),
        color = AppColors.White,
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Пока нет привычек", color = AppColors.Ink, style = MaterialTheme.typography.titleLarge)
        Text("Добавь первую привычку, чтобы начать отмечать регулярные действия.", color = AppColors.Muted, style = MaterialTheme.typography.bodyMedium)
        TextButton(onClick = onAddClick) { Text("Добавить привычку", color = AppColors.Ink) }
    }
}

@Composable
fun habitColor(color: HabitColor): Color = when (color) {
    HabitColor.PINK -> AppColors.Pink
    HabitColor.BLUE -> AppColors.Blue
    HabitColor.YELLOW -> AppColors.Yellow
    HabitColor.MINT -> AppColors.Mint
    HabitColor.LAVENDER -> AppColors.Lavender
}
