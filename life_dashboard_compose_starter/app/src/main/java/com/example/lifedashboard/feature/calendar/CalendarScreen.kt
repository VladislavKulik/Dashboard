package com.example.lifedashboard.feature.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.data.model.DashboardActivityEvent
import com.example.lifedashboard.data.model.DashboardGoal
import com.example.lifedashboard.data.model.DashboardHabit
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.data.model.DashboardTask
import com.example.lifedashboard.ui.components.FilterPill
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.components.ScreenTopBar
import com.example.lifedashboard.ui.theme.AppColors
import java.time.LocalDate
import java.time.YearMonth

private enum class CalendarPeriod(val title: String) {
    ALL("Все даты"), NEXT_7_DAYS("7 дней"), THIS_MONTH("Месяц"), OVERDUE("Просрочено")
}

private enum class CalendarTypeFilter(val title: String, val type: CalendarEventType?) {
    ALL("Все", null), TASKS("Задачи", CalendarEventType.TASK),
    PROJECTS("Проекты", CalendarEventType.PROJECT), GOALS("Цели", CalendarEventType.GOAL),
    HABITS("Привычки", CalendarEventType.HABIT), ACTIVITY("Активность", CalendarEventType.ACTIVITY)
}

@Composable
fun CalendarScreen(
    tasks: List<DashboardTask>,
    projects: List<DashboardProject>,
    goals: List<DashboardGoal>,
    habits: List<DashboardHabit>,
    activityEvents: List<DashboardActivityEvent>,
    onBack: () -> Unit,
    onEventClick: (CalendarEvent) -> Unit
) {
    val events = remember(tasks, projects, goals, habits, activityEvents) {
        CalendarEventFactory.build(tasks, projects, goals, habits, activityEvents)
    }
    var monthOffset by remember { mutableStateOf(0) }
    var selectedDate by remember { mutableStateOf(CalendarEventFactory.todayKey()) }
    var selectedPeriod by remember { mutableStateOf(CalendarPeriod.ALL) }
    var selectedType by remember { mutableStateOf(CalendarTypeFilter.ALL) }

    val todayDate = LocalDate.now()
    val today = remember { todayDate.toString() }
    val displayedYearMonth = YearMonth.now().plusMonths(monthOffset.toLong())
    val year = displayedYearMonth.year
    val month = displayedYearMonth.monthValue - 1
    fun shiftMonth(delta: Int) {
        monthOffset += delta
        val targetMonth = YearMonth.now().plusMonths(monthOffset.toLong())
        val selectedDay = runCatching { LocalDate.parse(selectedDate).dayOfMonth }.getOrDefault(todayDate.dayOfMonth)
        selectedDate = targetMonth.atDay(selectedDay.coerceAtMost(targetMonth.lengthOfMonth())).toString()
    }
    val monthCells = remember(year, month) { CalendarEventFactory.monthGrid(year, month) }
    val periodEvents = remember(events, selectedPeriod, year, month) {
        events.filter { event ->
            val date = runCatching { LocalDate.parse(event.dateKey) }.getOrNull() ?: return@filter false
            when (selectedPeriod) {
                CalendarPeriod.ALL -> true
                CalendarPeriod.NEXT_7_DAYS -> !date.isBefore(todayDate) && !date.isAfter(todayDate.plusDays(6))
                CalendarPeriod.THIS_MONTH -> YearMonth.from(date) == displayedYearMonth
                CalendarPeriod.OVERDUE -> date.isBefore(todayDate) && event.type in setOf(CalendarEventType.TASK, CalendarEventType.PROJECT, CalendarEventType.GOAL) && !event.completed
            }
        }
    }
    val visibleEvents = remember(periodEvents, selectedType) {
        periodEvents.filter { event -> selectedType.type == null || event.type == selectedType.type }
    }
    val monthEvents = remember(visibleEvents, year, month) {
        visibleEvents.filter { event ->
            val parts = event.dateKey.split('-')
            parts.size == 3 && parts[0].toIntOrNull() == year && parts[1].toIntOrNull() == month + 1
        }
    }
    val selectedEvents = remember(visibleEvents, selectedDate) { visibleEvents.filter { it.dateKey == selectedDate } }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenTopBar(title = "Календарь", onBackClick = onBack)

        PastelCard(color = AppColors.White, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    CalendarEventFactory.formatMonthTitle(year, month),
                    modifier = Modifier.weight(1f), color = AppColors.Ink,
                    fontSize = 20.sp, fontWeight = FontWeight.Bold
                )
                IconButton(onClick = {
                    shiftMonth(-1)
                }) {
                    Icon(Icons.Outlined.ChevronLeft, contentDescription = "Предыдущий месяц", tint = AppColors.Ink)
                }
                IconButton(onClick = {
                    shiftMonth(1)
                }) {
                    Icon(Icons.Outlined.ChevronRight, contentDescription = "Следующий месяц", tint = AppColors.Ink)
                }
                TextButton(onClick = { monthOffset = 0; selectedDate = today }) {
                    Icon(Icons.Outlined.Today, contentDescription = null, tint = AppColors.Ink, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Сегодня", color = AppColors.Ink)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CalendarPeriod.values().forEach { period ->
                    FilterPill(
                        label = period.title,
                        selected = selectedPeriod == period,
                        onClick = { selectedPeriod = period }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CalendarTypeFilter.values().forEach { filter ->
                    FilterPill(
                        label = filter.title,
                        selected = selectedType == filter,
                        onClick = { selectedType = filter }
                    )
                }
            }

            val weekdays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
            Row(modifier = Modifier.fillMaxWidth()) {
                weekdays.forEach { day ->
                    Text(day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, color = AppColors.Muted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            monthCells.chunked(7).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    week.forEach { dateKey ->
                        if (dateKey == null) {
                            Box(modifier = Modifier.weight(1f).height(58.dp))
                        } else {
                            val dayEvents = monthEvents.filter { it.dateKey == dateKey }
                            val isSelected = dateKey == selectedDate
                            val isToday = dateKey == today
                            Box(
                                modifier = Modifier.weight(1f).height(58.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) AppColors.Ink else if (isToday) AppColors.Lavender.copy(alpha = 0.5f) else Color.Transparent)
                                    .clickable { selectedDate = dateKey },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(
                                        dateKey.substringAfterLast('-').toIntOrNull()?.toString().orEmpty(),
                                        color = if (isSelected) AppColors.White else AppColors.Ink,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.height(5.dp)) {
                                        dayEvents.take(3).forEach { event ->
                                            Box(Modifier.size(4.dp).clip(CircleShape).background(if (isSelected) AppColors.White else eventColor(event.type)))
                                        }
                                    }
                                }
                                if (dayEvents.size > 3) {
                                    Text(
                                        "+${dayEvents.size - 3}", fontSize = 8.sp,
                                        color = if (isSelected) AppColors.White else AppColors.Muted,
                                        modifier = Modifier.align(Alignment.TopEnd).padding(top = 3.dp, end = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
            ) {
                LegendDot(AppColors.Purple, "Задачи")
                LegendDot(AppColors.Blue, "Проекты")
                LegendDot(AppColors.Yellow, "Цели")
                LegendDot(AppColors.Pink, "Привычки")
                LegendDot(AppColors.Mint, "Активность")
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(CalendarEventFactory.formatDayTitle(selectedDate), color = AppColors.Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(
                        when (selectedEvents.size) {
                            0 -> "Нет событий"
                            1 -> "1 событие"
                            in 2..4 -> "${selectedEvents.size} события"
                            else -> "${selectedEvents.size} событий"
                        }, color = AppColors.Muted, fontSize = 13.sp
                    )
                }
            }

            if (selectedEvents.isEmpty()) {
                PastelCard(color = AppColors.White) {
                    Text("На этот день ничего не запланировано", color = AppColors.Ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text("События появятся здесь автоматически. Вы можете сменить фильтр или выбрать другой день.", color = AppColors.Muted, fontSize = 13.sp)
                }
            } else {
                selectedEvents.forEach { event ->
                    PastelCard(
                        modifier = Modifier.fillMaxWidth().clickable { onEventClick(event) },
                        color = eventColor(event.type),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(event.type.label, color = AppColors.Ink.copy(alpha = 0.66f), fontSize = 11.sp, modifier = Modifier.weight(1f))
                            if (event.completed) Text("Готово", color = AppColors.Ink, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Text(event.title, color = AppColors.Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text(event.subtitle, color = AppColors.Ink.copy(alpha = 0.72f), fontSize = 12.sp)
                        event.timeLabel?.let { time ->
                            Text("Напоминание · $time", color = AppColors.Ink, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun LegendDot(color: Color, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(color))
        Text(title, color = AppColors.Muted, fontSize = 9.sp, maxLines = 1)
    }
}

@Composable
private fun eventColor(type: CalendarEventType): Color = when (type) {
    CalendarEventType.TASK -> AppColors.PurpleLight
    CalendarEventType.PROJECT -> AppColors.Blue
    CalendarEventType.GOAL -> AppColors.Yellow
    CalendarEventType.HABIT -> AppColors.Pink
    CalendarEventType.ACTIVITY -> AppColors.Mint
}
