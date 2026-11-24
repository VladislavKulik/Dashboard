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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.data.DateKeys
import com.example.lifedashboard.data.HabitMetrics
import com.example.lifedashboard.data.model.DashboardHabit
import com.example.lifedashboard.ui.components.CircleIconButton
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.theme.AppColors
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HabitDetailsScreen(
    habit: DashboardHabit,
    onBack: () -> Unit,
    onToggleToday: () -> Unit,
    onSaveHabit: (DashboardHabit, (() -> Unit)?) -> Unit,
    onDeleteHabit: (String, (() -> Unit)?) -> Unit,
    operationError: String? = null,
    onToggleDate: (String, String) -> Unit = { _, _ -> },
    onEditHabit: (() -> Unit)? = null
) {
    val streak = remember(habit.completionDates) { HabitMetrics.currentStreak(habit.completionDates) }
    val best = remember(habit.completionDates) { HabitMetrics.bestStreak(habit.completionDates) }
    var monthOffset by rememberSaveable(habit.id) { mutableIntStateOf(0) }
    val displayedMonth = remember(monthOffset) { YearMonth.now().plusMonths(monthOffset.toLong()) }
    val monthPercent = remember(habit.completionDates, displayedMonth) {
        HabitMetrics.habitMonthlyPercent(habit, displayedMonth)
    }
    val todayCompleted = DateKeys.today() in habit.completionDates

    var showOverflowMenu by remember { mutableStateOf(false) }
    var showEditor by rememberSaveable(habit.id) { mutableStateOf(false) }
    var showDeleteConfirmation by rememberSaveable(habit.id) { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 92.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleIconButton(
                    icon = Icons.Outlined.ArrowBack,
                    contentDescription = "Назад",
                    onClick = onBack
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Привычка",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineLarge,
                    color = AppColors.Ink
                )
                Box {
                    CircleIconButton(
                        icon = Icons.Outlined.MoreHoriz,
                        contentDescription = "Действия",
                        onClick = { showOverflowMenu = true }
                    )
                    DropdownMenu(
                        expanded = showOverflowMenu,
                        onDismissRequest = { showOverflowMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Редактировать") },
                            onClick = {
                                showOverflowMenu = false
                                if (onEditHabit != null) onEditHabit() else showEditor = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Удалить привычку", color = AppColors.Error) },
                            onClick = {
                                showOverflowMenu = false
                                showDeleteConfirmation = true
                            }
                        )
                    }
                }
            }

            if (operationError != null) {
                Text(operationError, color = AppColors.Error, style = MaterialTheme.typography.bodySmall)
            }

            PastelCard(
                modifier = Modifier.fillMaxWidth(),
                color = habitColor(habit.color),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = habit.title, color = AppColors.Ink, style = MaterialTheme.typography.headlineMedium)
                    Text(
                        text = reminderSummary(habit),
                        color = AppColors.Muted,
                        fontSize = 13.sp
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    HabitStatItem(value = streak.toString(), label = "серия", modifier = Modifier.weight(1f))
                    HabitStatItem(value = best.toString(), label = "лучшая", modifier = Modifier.weight(1f))
                    HabitStatItem(value = "$monthPercent%", label = "за месяц", modifier = Modifier.weight(1f))
                }
            }

            SingleHabitCalendarCard(
                habit = habit,
                displayedMonth = displayedMonth,
                onPreviousMonth = { monthOffset -= 1 },
                onNextMonth = { monthOffset += 1 },
                onToggleDate = { dateKey -> onToggleDate(habit.id, dateKey) }
            )
        }

        Button(
            onClick = onToggleToday,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (todayCompleted) AppColors.White else AppColors.Ink,
                contentColor = if (todayCompleted) AppColors.Ink else AppColors.White
            ),
            contentPadding = PaddingValues(vertical = 15.dp)
        ) {
            Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (todayCompleted) "Отменить отметку за сегодня" else "Отметить сегодня",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }

    if (showEditor) {
        HabitEditorScreen(
            initialHabit = habit,
            operationError = operationError,
            onBack = { showEditor = false },
            onSave = { updated, _ -> onSaveHabit(updated) { showEditor = false } }
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
                        Text(operationError, color = AppColors.Error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteHabit(habit.id) { showDeleteConfirmation = false }
                }) {
                    Text("Удалить", color = AppColors.Error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) { Text("Отмена", color = AppColors.Muted) }
            },
            containerColor = AppColors.White
        )
    }
}

@Composable
private fun HabitStatItem(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(value, color = AppColors.Ink, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        Text(label, color = AppColors.Muted, fontSize = 12.sp)
    }
}

@Composable
private fun SingleHabitCalendarCard(
    habit: DashboardHabit,
    displayedMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onToggleDate: (String) -> Unit
) {
    val monthTitle = remember(displayedMonth) {
        displayedMonth.format(DateTimeFormatter.ofPattern("LLLL yyyy", Locale.forLanguageTag("ru")))
            .replaceFirstChar { it.uppercase(Locale.forLanguageTag("ru")) }
    }
    val today = remember { LocalDate.now() }
    val cells = remember(displayedMonth) {
        val offset = displayedMonth.atDay(1).dayOfWeek.value - 1
        buildList<LocalDate?> {
            repeat(offset) { add(null) }
            for (day in 1..displayedMonth.lengthOfMonth()) add(displayedMonth.atDay(day))
            while (size % 7 != 0) add(null)
        }
    }
    val weekdays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

    PastelCard(
        modifier = Modifier.fillMaxWidth(),
        color = AppColors.White,
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
            Text(
                text = monthTitle,
                color = AppColors.Ink,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
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

        Row(modifier = Modifier.fillMaxWidth()) {
            weekdays.forEach { day ->
                Text(day, modifier = Modifier.weight(1f), color = AppColors.Muted, fontSize = 11.sp, textAlign = TextAlign.Center)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            cells.chunked(7).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    week.forEach { date ->
                        Box(
                            modifier = Modifier.weight(1f).height(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (date != null) {
                                val key = date.toString()
                                val checked = key in habit.completionDates
                                val isToday = date == today
                                val isFuture = date.isAfter(today)
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .then(
                                            when {
                                                checked -> Modifier.background(AppColors.Ink)
                                                isToday -> Modifier.border(1.5.dp, AppColors.Ink, CircleShape)
                                                else -> Modifier
                                            }
                                        )
                                        .clickable(enabled = !isFuture) { onToggleDate(key) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = date.dayOfMonth.toString(),
                                        color = when {
                                            checked -> AppColors.White
                                            isFuture -> AppColors.Muted.copy(alpha = 0.65f)
                                            else -> AppColors.Ink
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = if (checked || isToday) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun reminderSummary(habit: DashboardHabit): String {
    if (!habit.reminderEnabled) return "Напоминание отключено"
    val dayNames = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    return if (habit.reminderDays.size == 7) {
        "Каждый день · напоминание ${habit.reminderTime}"
    } else {
        val days = habit.reminderDays.sorted().mapNotNull { dayNames.getOrNull(it - 1) }
        "${days.joinToString(" ")} · напоминание ${habit.reminderTime}"
    }
}