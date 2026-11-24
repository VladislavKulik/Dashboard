package com.example.lifedashboard.feature.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.data.DateKeys
import com.example.lifedashboard.data.HabitMetrics
import com.example.lifedashboard.data.model.DashboardHabit
import com.example.lifedashboard.data.model.HabitColor
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.components.ScreenTopBar
import com.example.lifedashboard.ui.theme.AppColors
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

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
    var monthOffset by rememberSaveable { mutableIntStateOf(0) }

    val activityPercent = remember(habits) { HabitMetrics.monthlyActivityPercent(habits) }
    val bestStreak = remember(habits) {
        habits.maxOfOrNull { HabitMetrics.currentStreak(it.completionDates) } ?: 0
    }
    val todayKey = remember { DateKeys.today() }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PastelCard(
                        modifier = Modifier.weight(1f),
                        color = AppColors.White,
                        contentPadding = PaddingValues(18.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text("$activityPercent%", color = AppColors.Ink, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                        Text("за месяц", color = AppColors.Muted, fontSize = 13.sp)
                    }
                    PastelCard(
                        modifier = Modifier.weight(1f),
                        color = AppColors.White,
                        contentPadding = PaddingValues(18.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text("$bestStreak дн.", color = AppColors.Ink, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                        Text("лучшая серия", color = AppColors.Muted, fontSize = 13.sp)
                    }
                }
            }

            item {
                ActivityCalendarCard(
                    habits = habits,
                    monthOffset = monthOffset,
                    onPreviousMonth = { monthOffset -= 1 },
                    onNextMonth = { monthOffset += 1 }
                )
            }

            if (operationError != null) {
                item {
                    Text(
                        text = operationError,
                        color = AppColors.Error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (habits.isEmpty()) {
                item { EmptyHabitsState(onAddClick = { showAddDialog = true }) }
            } else {
                item {
                    PastelCard(
                        modifier = Modifier.fillMaxWidth(),
                        color = AppColors.White,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        habits.forEachIndexed { index, habit ->
                            HabitRow(
                                habit = habit,
                                checkedToday = todayKey in habit.completionDates,
                                onClick = { onOpenHabit(habit.id) },
                                onToggleToday = { onToggleToday(habit.id) }
                            )
                            if (index < habits.lastIndex) {
                                HorizontalDivider(color = AppColors.Outline.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .size(60.dp)
                .clip(CircleShape)
                .clickable(role = Role.Button) { showAddDialog = true },
            color = AppColors.Yellow,
            shape = CircleShape,
            shadowElevation = 6.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Add, contentDescription = "Добавить привычку", tint = AppColors.Ink, modifier = Modifier.size(26.dp))
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
    title: String = "",
    monthOffset: Int = 0,
    onPreviousMonth: (() -> Unit)? = null,
    onNextMonth: (() -> Unit)? = null
) {
    val displayedMonth = remember(monthOffset) { YearMonth.now().plusMonths(monthOffset.toLong()) }
    val monthTitle = remember(displayedMonth) {
        displayedMonth.format(DateTimeFormatter.ofPattern("LLLL yyyy", Locale.forLanguageTag("ru")))
            .replaceFirstChar { it.uppercase(Locale.forLanguageTag("ru")) }
    }
    val monthGrid = remember(displayedMonth) { buildMonthCells(displayedMonth) }
    val today = remember { LocalDate.now() }
    val weekdays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

    PastelCard(
        modifier = Modifier.fillMaxWidth(),
        color = AppColors.White,
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (onPreviousMonth != null) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(AppColors.Background)
                        .clickable(onClick = onPreviousMonth),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.ChevronLeft, contentDescription = "Предыдущий месяц", tint = AppColors.Ink, modifier = Modifier.size(18.dp))
                }
            }
            Text(
                text = if (title.isNotBlank()) "$title · $monthTitle" else monthTitle,
                color = AppColors.Ink,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            if (onNextMonth != null) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(AppColors.Background)
                        .clickable(onClick = onNextMonth),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.ChevronRight, contentDescription = "Следующий месяц", tint = AppColors.Ink, modifier = Modifier.size(18.dp))
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            weekdays.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    color = AppColors.Muted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            monthGrid.chunked(7).forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    week.forEach { date ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (date != null) {
                                val key = date.toString()
                                val completedCount = HabitMetrics.completedHabitCount(habits, key)
                                val allCompleted = habits.isNotEmpty() && completedCount >= habits.size
                                val partialCompleted = !allCompleted && completedCount > 0
                                val isToday = date == today
                                val isFuture = date.isAfter(today)

                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .then(
                                            when {
                                                allCompleted -> Modifier.background(AppColors.Ink)
                                                partialCompleted -> Modifier.background(AppColors.Lavender)
                                                isToday -> Modifier.border(1.5.dp, AppColors.Ink, CircleShape)
                                                else -> Modifier
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = date.dayOfMonth.toString(),
                                        color = when {
                                            allCompleted -> AppColors.White
                                            isFuture -> AppColors.Muted.copy(alpha = 0.7f)
                                            else -> AppColors.Ink
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = if (allCompleted || partialCompleted || isToday) FontWeight.Bold else FontWeight.Medium
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
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CalendarLegendItem(color = AppColors.Ink, label = "все")
            CalendarLegendItem(color = AppColors.Lavender, label = "часть")
        }
    }
}

private fun buildMonthCells(month: YearMonth): List<LocalDate?> {
    val firstDay = month.atDay(1)
    val offset = firstDay.dayOfWeek.value - 1
    val cells = mutableListOf<LocalDate?>()
    repeat(offset) { cells += null }
    for (day in 1..month.lengthOfMonth()) {
        cells += month.atDay(day)
    }
    while (cells.size % 7 != 0) cells += null
    return cells
}

@Composable
private fun CalendarLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(text = label, color = AppColors.Muted, fontSize = 12.sp)
    }
}

@Composable
private fun HabitRow(
    habit: DashboardHabit,
    checkedToday: Boolean,
    onClick: () -> Unit,
    onToggleToday: () -> Unit
) {
    val streak = HabitMetrics.currentStreak(habit.completionDates)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(habitColor(habit.color)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Repeat, contentDescription = null, tint = AppColors.Ink, modifier = Modifier.size(19.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = habit.title,
                color = AppColors.Ink,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text("Серия $streak дн.", color = AppColors.Muted, fontSize = 12.sp)
        }
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .then(
                    if (checkedToday) Modifier.background(AppColors.Ink)
                    else Modifier.border(1.8.dp, AppColors.Ink, CircleShape)
                )
                .clickable(role = Role.Checkbox, onClick = onToggleToday),
            contentAlignment = Alignment.Center
        ) {
            if (checkedToday) {
                Icon(Icons.Outlined.Check, contentDescription = "Выполнено сегодня", tint = AppColors.White, modifier = Modifier.size(15.dp))
            }
        }
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
        TextButton(onClick = onAddClick) { Text("Добавить привычку", color = AppColors.Ink, fontWeight = FontWeight.Bold) }
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