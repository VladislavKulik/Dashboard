package com.example.lifedashboard.feature.analytics

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.data.HabitMetrics
import com.example.lifedashboard.data.model.DashboardActivityEvent
import com.example.lifedashboard.data.model.DashboardGoal
import com.example.lifedashboard.data.model.DashboardHabit
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.data.model.DashboardTask
import com.example.lifedashboard.presentation.TaskTimerState
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.components.ProgressTrack
import com.example.lifedashboard.ui.components.ScreenTopBar
import com.example.lifedashboard.ui.theme.AppColors
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale
import kotlin.math.max

private enum class AnalyticsPeriod(val label: String) {
    WEEK("Неделя"),
    MONTH("Месяц")
}

private data class PeriodSeries(
    val completed: Int,
    val previous: Int,
    val values: List<Int>,
    val labels: List<String>,
    val selectedIndex: Int
)

private data class ProjectShare(
    val id: String,
    val name: String,
    val count: Int,
    val valueLabel: String,
    val color: Color
)

private fun buildWeekSeries(events: List<DashboardActivityEvent>): PeriodSeries {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        val mondayOffset = (get(Calendar.DAY_OF_WEEK) + 5) % 7
        add(Calendar.DAY_OF_MONTH, -mondayOffset)
    }
    val values = (0..6).map { day ->
        val dayCalendar = (calendar.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, day) }
        val nextDay = (dayCalendar.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
        val from = dayCalendar.timeInMillis
        val untilMillis = nextDay.timeInMillis
        events.count { it.type == "TASK_COMPLETION" && it.completed && it.occurredAtEpochMillis >= from && it.occurredAtEpochMillis < untilMillis }
    }
    val todayIndex = ((Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7).coerceIn(0, 6)
    val previousStart = (calendar.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, -7) }.timeInMillis
    val previousEnd = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -7) }.timeInMillis
    val previous = events.count { it.type == "TASK_COMPLETION" && it.completed && it.occurredAtEpochMillis >= previousStart && it.occurredAtEpochMillis < previousEnd }
    return PeriodSeries(values.sum(), previous, values, listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"), todayIndex)
}

private fun buildMonthSeries(events: List<DashboardActivityEvent>): PeriodSeries {
    val now = Calendar.getInstance()
    val monthStart = (now.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    val daysInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH)
    val bucketCount = (daysInMonth + 6) / 7
    val values = (0 until bucketCount).map { bucket ->
        val firstDay = bucket * 7 + 1
        val lastDay = minOf(firstDay + 6, daysInMonth)
        val from = (monthStart.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, firstDay) }.timeInMillis
        val until = (monthStart.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, lastDay); add(Calendar.DAY_OF_MONTH, 1) }.timeInMillis
        events.count { it.type == "TASK_COMPLETION" && it.completed && it.occurredAtEpochMillis >= from && it.occurredAtEpochMillis < until }
    }
    val previousMonthStart = (monthStart.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
    val previousMonthCutoff = (now.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
    val previous = events.count { it.type == "TASK_COMPLETION" && it.completed && it.occurredAtEpochMillis >= previousMonthStart.timeInMillis && it.occurredAtEpochMillis < previousMonthCutoff.timeInMillis }
    val labels = values.indices.map { "Нед ${it + 1}" }
    return PeriodSeries(values.sum(), previous, values, labels, ((now.get(Calendar.DAY_OF_MONTH) - 1) / 7).coerceIn(0, values.lastIndex))
}

@Composable
fun AnalyticsScreen(
    projects: List<DashboardProject>,
    tasks: List<DashboardTask>,
    activityEvents: List<DashboardActivityEvent>,
    onBack: () -> Unit,
    habits: List<DashboardHabit> = emptyList(),
    goals: List<DashboardGoal> = emptyList(),
    timerState: TaskTimerState = TaskTimerState()
) {
    var period by rememberSaveable { mutableStateOf(AnalyticsPeriod.WEEK) }
    val eventSnapshot = activityEvents.toList()
    val weekSeries = remember(eventSnapshot) { buildWeekSeries(eventSnapshot) }
    val monthSeries = remember(eventSnapshot) { buildMonthSeries(eventSnapshot) }
    var selectedBar by rememberSaveable { mutableIntStateOf(weekSeries.selectedIndex) }
    val series = if (period == AnalyticsPeriod.WEEK) weekSeries else monthSeries

    val chartColors = listOf(AppColors.Purple, AppColors.ChartYellow, AppColors.ChartGreen, AppColors.Blue)
    val projectShares = projects.mapIndexed { index, project ->
        val count = tasks.count { it.projectId == project.id }
        val seconds = timerState.sessions.filter { it.projectId == project.id }.sumOf { it.durationSeconds }
        val valueLabel = if (seconds > 0L) TaskTimerState.formatShort(seconds) else "$count зад."
        ProjectShare(
            id = project.id,
            name = project.name,
            count = count,
            valueLabel = valueLabel,
            color = chartColors[index % chartColors.size]
        )
    }.filter { it.count > 0 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ScreenTopBar(title = "Аналитика", onBackClick = onBack)

        PeriodSelector(
            selected = period,
            onSelect = {
                period = it
                selectedBar = if (it == AnalyticsPeriod.WEEK) weekSeries.selectedIndex else monthSeries.selectedIndex
            }
        )

        CompletedTasksCard(
            period = period,
            series = series,
            selectedBar = selectedBar.coerceIn(series.values.indices),
            onSelectBar = { selectedBar = it }
        )

        if (period == AnalyticsPeriod.WEEK) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProjectDistributionCard(
                    shares = projectShares,
                    modifier = Modifier.weight(1f)
                )
                ActivityTrendCard(
                    values = series.values.map { it.toFloat() },
                    modifier = Modifier.weight(1f)
                )
            }

            ProjectsBreakdownCard(projectShares = projectShares)
        } else {
            MonthComparisonCard(
                currentTasks = monthSeries.completed,
                previousTasks = monthSeries.previous,
                habits = habits,
                goals = goals,
                events = activityEvents
            )
        }

        Text(
            text = if (activityEvents.none { it.type == "TASK_COMPLETION" && it.completed }) {
                "История начнёт заполняться после отметки выполненных задач."
            } else {
                "Статистика строится по сохранённым событиям задач, привычек и целей."
            },
            color = AppColors.Muted,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
    }
}

@Composable
private fun PeriodSelector(
    selected: AnalyticsPeriod,
    onSelect: (AnalyticsPeriod) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(AppColors.White)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        AnalyticsPeriod.values().forEach { period ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(if (selected == period) AppColors.Ink else Color.Transparent)
                    .clickable { onSelect(period) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = period.label,
                    color = if (selected == period) AppColors.White else AppColors.Ink,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun CompletedTasksCard(
    period: AnalyticsPeriod,
    series: PeriodSeries,
    selectedBar: Int,
    onSelectBar: (Int) -> Unit
) {
    val change = if (series.previous == 0) null else {
        ((series.completed - series.previous) * 100f / series.previous).toInt()
    }
    val previousLabel = if (period == AnalyticsPeriod.WEEK) {
        "на прошлой неделе ${series.previous}"
    } else {
        "в прошлом месяце ${series.previous}"
    }

    PastelCard(
        modifier = Modifier.fillMaxWidth(),
        color = AppColors.White,
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Выполнено задач",
                modifier = Modifier.weight(1f),
                color = AppColors.Ink,
                style = MaterialTheme.typography.titleMedium
            )
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(AppColors.Mint)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = change?.let { "${if (it >= 0) "+" else ""}$it%" } ?: "—",
                    color = AppColors.Ink,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = series.completed.toString(),
                color = AppColors.Ink,
                fontSize = 34.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = previousLabel,
                color = AppColors.Muted,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        StripedBarChart(
            values = series.values,
            labels = series.labels,
            selectedIndex = selectedBar,
            onSelect = onSelectBar
        )

        Text(
            text = "${series.labels[selectedBar]} · ${series.values[selectedBar]} задач",
            color = AppColors.Muted,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun StripedBarChart(
    values: List<Int>,
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    val maxValue = max(1, values.maxOrNull() ?: 1)
    val barColor = AppColors.Purple
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(118.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        values.forEachIndexed { index, value ->
            val isSelected = index == selectedIndex
            val barHeight = (14f + 72f * value / maxValue).dp
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(index) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(barHeight)
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    if (isSelected) {
                        drawRoundRect(color = barColor, cornerRadius = CornerRadius(10.dp.toPx()))
                    } else {
                        val step = 6.dp.toPx()
                        val stroke = 2.dp.toPx()
                        var x = -size.height
                        while (x < size.width + size.height) {
                            drawLine(
                                color = barColor.copy(alpha = 0.72f),
                                start = Offset(x, size.height),
                                end = Offset(x + size.height, 0f),
                                strokeWidth = stroke
                            )
                            x += step
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = labels[index],
                    color = if (isSelected) AppColors.Ink else AppColors.Muted,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun ProjectDistributionCard(
    shares: List<ProjectShare>,
    modifier: Modifier = Modifier
) {
    PastelCard(
        modifier = modifier.height(178.dp),
        color = AppColors.White,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Распределение",
            color = AppColors.Ink,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            if (shares.isEmpty()) {
                Text("Нет задач", color = AppColors.Muted, fontSize = 12.sp)
            } else {
                DonutChart(shares = shares, modifier = Modifier.size(92.dp))
            }
        }
    }
}

@Composable
private fun DonutChart(
    shares: List<ProjectShare>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val total = shares.sumOf { it.count }.coerceAtLeast(1)
        val strokeWidth = size.minDimension * 0.24f
        var startAngle = -90f
        shares.forEach { share ->
            val sweep = share.count.toFloat() / total * 360f
            drawArc(
                color = share.color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )
            startAngle += sweep
        }
    }
}

@Composable
private fun ActivityTrendCard(
    values: List<Float>,
    modifier: Modifier = Modifier
) {
    val lineColor = AppColors.Purple
    PastelCard(
        modifier = modifier.height(178.dp),
        color = AppColors.White,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Динамика",
            color = AppColors.Ink,
            style = MaterialTheme.typography.titleMedium
        )
        Canvas(modifier = Modifier.fillMaxWidth().height(88.dp)) {
            if (values.size < 2) return@Canvas
            val minValue = values.minOrNull() ?: 0f
            val maxValue = (values.maxOrNull() ?: 1f).coerceAtLeast(minValue + 1f)
            val range = maxValue - minValue
            val path = Path()
            values.forEachIndexed { index, value ->
                val x = size.width * index / (values.size - 1).toFloat()
                val y = size.height - ((value - minValue) / range) * (size.height * 0.72f) - size.height * 0.14f
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Spacer(Modifier.height(2.dp))
    }
}

@Composable
private fun ProjectsBreakdownCard(projectShares: List<ProjectShare>) {
    val maxCount = max(1, projectShares.maxOfOrNull { it.count } ?: 1)
    PastelCard(
        modifier = Modifier.fillMaxWidth(),
        color = AppColors.White,
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("По проектам", color = AppColors.Ink, style = MaterialTheme.typography.titleMedium)
        if (projectShares.isEmpty()) {
            Text("Пока нет задач, связанных с проектами.", color = AppColors.Muted, fontSize = 13.sp)
        } else {
            projectShares.forEach { share ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = share.name,
                            color = AppColors.Ink,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(share.valueLabel, color = AppColors.Muted, fontSize = 13.sp)
                    }
                    ProgressTrack(
                        progress = share.count.toFloat() / maxCount,
                        trackColor = AppColors.Background,
                        progressColor = share.color
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthComparisonCard(
    currentTasks: Int,
    previousTasks: Int,
    habits: List<DashboardHabit>,
    goals: List<DashboardGoal>,
    events: List<DashboardActivityEvent>
) {
    val currentMonth = YearMonth.now()
    val previousMonth = currentMonth.minusMonths(1)
    val ru = Locale.forLanguageTag("ru")
    val currentName = currentMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, ru)
        .replaceFirstChar { it.uppercase(ru) }
    val previousName = previousMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, ru)
        .lowercase(ru)

    val currentHabitPct = HabitMetrics.monthlyActivityPercent(habits)
    val previousHabitPct = remember(habits, previousMonth) {
        if (habits.isEmpty()) 0 else {
            val days = previousMonth.lengthOfMonth()
            val active = (1..days).count { d ->
                val key = previousMonth.atDay(d).toString()
                habits.any { key in it.completionDates }
            }
            (active * 100 / days).coerceIn(0, 100)
        }
    }

    val completedGoals = goals.count { it.isCompleted }
    val totalGoals = goals.size
    val taskMax = max(1, max(currentTasks, previousTasks))
    val zone = ZoneId.systemDefault()
    val currentActiveDays = remember(events, currentMonth) {
        events.map { Instant.ofEpochMilli(it.occurredAtEpochMillis).atZone(zone).toLocalDate() }
            .filter { YearMonth.from(it) == currentMonth }
            .distinct()
            .size
    }
    val previousActiveDays = remember(events, previousMonth) {
        events.map { Instant.ofEpochMilli(it.occurredAtEpochMillis).atZone(zone).toLocalDate() }
            .filter { YearMonth.from(it) == previousMonth }
            .distinct()
            .size
    }
    val activeDayMax = max(1, max(currentActiveDays, previousActiveDays))

    PastelCard(
        modifier = Modifier.fillMaxWidth(),
        color = AppColors.White,
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("$currentName и $previousName", color = AppColors.Ink, style = MaterialTheme.typography.titleMedium)

        ComparisonMetricRow(
            title = "Задачи",
            valueLabel = "$currentTasks · было $previousTasks",
            currentProgress = currentTasks.toFloat() / taskMax,
            previousProgress = previousTasks.toFloat() / taskMax
        )
        ComparisonMetricRow(
            title = "Активные дни",
            valueLabel = "$currentActiveDays · было $previousActiveDays",
            currentProgress = currentActiveDays.toFloat() / activeDayMax,
            previousProgress = previousActiveDays.toFloat() / activeDayMax
        )
        ComparisonMetricRow(
            title = "Привычки",
            valueLabel = "$currentHabitPct% · было $previousHabitPct%",
            currentProgress = currentHabitPct / 100f,
            previousProgress = previousHabitPct / 100f
        )
        if (totalGoals > 0) {
            ComparisonMetricRow(
                title = "Цели",
                valueLabel = "$completedGoals из $totalGoals достигнуто",
                currentProgress = completedGoals.toFloat() / totalGoals,
                previousProgress = 0f
            )
        }

        Text(
            text = "Тёмная полоса — текущий месяц, светлая — прошлый",
            color = AppColors.Muted,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun ComparisonMetricRow(
    title: String,
    valueLabel: String,
    currentProgress: Float,
    previousProgress: Float
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = AppColors.Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text(valueLabel, color = AppColors.Muted, fontSize = 12.sp)
        }
        ProgressTrack(
            progress = currentProgress.coerceIn(0f, 1f),
            trackColor = AppColors.Background,
            progressColor = AppColors.Purple
        )
        ProgressTrack(
            progress = previousProgress.coerceIn(0f, 1f),
            trackColor = AppColors.Background,
            progressColor = AppColors.Lavender
        )
    }
}