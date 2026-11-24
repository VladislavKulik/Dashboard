package com.example.lifedashboard.feature.analytics

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.data.model.DashboardTask
import com.example.lifedashboard.data.model.DashboardActivityEvent
import java.util.Calendar
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.components.ScreenTopBar
import com.example.lifedashboard.ui.theme.AppColors
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


private fun buildWeekSeries(events: List<DashboardActivityEvent>): PeriodSeries {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        val mondayOffset = (get(Calendar.DAY_OF_WEEK) + 5) % 7
        add(Calendar.DAY_OF_MONTH, -mondayOffset)
    }
    val start = calendar.timeInMillis
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
    val monthStart = (now.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
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
    val labels = values.indices.map { "${it + 1} нед." }
    return PeriodSeries(values.sum(), previous, values, labels, ((now.get(Calendar.DAY_OF_MONTH) - 1) / 7).coerceIn(0, values.lastIndex))
}

private data class ProjectShare(
    val id: String,
    val name: String,
    val count: Int,
    val color: Color
)

@Composable
fun AnalyticsScreen(
    projects: List<DashboardProject>,
    tasks: List<DashboardTask>,
    activityEvents: List<DashboardActivityEvent>,
    onBack: () -> Unit
) {
    var period by rememberSaveable { mutableStateOf(AnalyticsPeriod.WEEK) }
    val eventSnapshot = activityEvents.toList()
    val weekSeries = remember(eventSnapshot) { buildWeekSeries(eventSnapshot) }
    val monthSeries = remember(eventSnapshot) { buildMonthSeries(eventSnapshot) }
    var selectedBar by rememberSaveable { mutableIntStateOf(weekSeries.selectedIndex) }
    val series = if (period == AnalyticsPeriod.WEEK) weekSeries else monthSeries
    val projectShares = projects.map { project ->
        ProjectShare(
            id = project.id,
            name = project.name,
            count = tasks.count { it.projectId == project.id },
            color = Color(project.colorArgb)
        )
    }.filter { it.count > 0 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
            series = series,
            selectedBar = selectedBar.coerceIn(series.values.indices),
            onSelectBar = { selectedBar = it }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProjectDistributionCard(
                shares = projectShares,
                modifier = Modifier.weight(1f)
            )
            ActivityTrendCard(
                period = period,
                values = series.values.map { it.toFloat() },
                modifier = Modifier.weight(1f)
            )
        }

        ProjectLegendCard(projectShares = projectShares)

        Text(
            text = if (activityEvents.none { it.type == "TASK_COMPLETION" && it.completed })
                "История начнёт заполняться после отметки выполненных задач. Ранее выполненные задачи не имеют даты завершения и не учитываются в графике."
            else "Графики построены по сохранённым событиям выполнения задач.",
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
    series: PeriodSeries,
    selectedBar: Int,
    onSelectBar: (Int) -> Unit
) {
    val change = if (series.previous == 0) null else {
        ((series.completed - series.previous) * 100f / series.previous).toInt()
    }
    PastelCard(
        modifier = Modifier.fillMaxWidth(),
        color = AppColors.Lavender,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Выполнено задач",
                modifier = Modifier.weight(1f),
                color = AppColors.Ink,
                style = MaterialTheme.typography.titleLarge
            )
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(AppColors.White.copy(alpha = 0.8f))
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    text = change?.let { "${if (it >= 0) "+" else ""}${it}%" } ?: "—",
                    color = AppColors.Ink,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        Text(
            text = series.completed.toString(),
            color = AppColors.Ink,
            fontSize = 38.sp,
            lineHeight = 42.sp,
            fontWeight = FontWeight.ExtraBold
        )

        BarChart(
            values = series.values,
            labels = series.labels,
            selectedIndex = selectedBar,
            onSelect = onSelectBar
        )

        Text(
            text = "${series.labels[selectedBar]} · ${series.values[selectedBar]} задач",
            color = AppColors.Ink.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun BarChart(
    values: List<Int>,
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    val maxValue = max(1, values.maxOrNull() ?: 1)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(146.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        values.forEachIndexed { index, value ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(116.dp)
                        .clickable { onSelect(index) },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    val barHeight = (value.toFloat() / maxValue * 92f).coerceAtLeast(12f).dp
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height(barHeight)
                            .clip(RoundedCornerShape(14.dp))
                            .then(
                                if (index == selectedIndex) Modifier.background(AppColors.Ink)
                                else Modifier.border(
                                    width = 1.5.dp,
                                    color = AppColors.Purple,
                                    shape = RoundedCornerShape(14.dp)
                                )
                            )
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = labels[index],
                    color = AppColors.Ink,
                    fontSize = if (labels[index].length > 3) 9.sp else 11.sp,
                    fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false
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
        modifier = modifier.height(212.dp),
        color = AppColors.Ink,
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "По проектам",
            color = AppColors.White,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            if (shares.isEmpty()) {
                Text("Нет задач", color = AppColors.MutedOnDark, fontSize = 12.sp)
            } else {
                DonutChart(shares = shares, modifier = Modifier.size(96.dp))
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
        val strokeWidth = size.minDimension * 0.22f
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
    period: AnalyticsPeriod,
    values: List<Float>,
    modifier: Modifier = Modifier
) {
    val maxValue = values.maxOrNull()?.coerceAtLeast(1f) ?: 1f
    val normalizedValues = values.map { it / maxValue }
    PastelCard(
        modifier = modifier.height(212.dp),
        color = AppColors.Mint,
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Динамика",
            color = AppColors.Ink,
            style = MaterialTheme.typography.titleLarge
        )
        LineTrendChart(values = normalizedValues, modifier = Modifier.fillMaxWidth().height(86.dp))
        Text(
            text = if (period == AnalyticsPeriod.WEEK) "За неделю" else "За месяц",
            color = AppColors.Ink.copy(alpha = 0.68f),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun LineTrendChart(
    values: List<Float>,
    modifier: Modifier = Modifier
) {
    val lineColor = AppColors.Ink
    Canvas(modifier = modifier) {
        if (values.size < 2) return@Canvas
        val minValue = values.minOrNull() ?: 0f
        val maxValue = values.maxOrNull() ?: 1f
        val range = (maxValue - minValue).coerceAtLeast(0.01f)
        val path = Path()
        values.forEachIndexed { index, value ->
            val x = size.width * index / (values.size - 1).toFloat()
            val y = size.height - ((value - minValue) / range) * (size.height * 0.72f) - size.height * 0.12f
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun ProjectLegendCard(projectShares: List<ProjectShare>) {
    PastelCard(
        modifier = Modifier.fillMaxWidth(),
        color = AppColors.White,
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Распределение задач",
            color = AppColors.Ink,
            style = MaterialTheme.typography.titleMedium
        )
        if (projectShares.isEmpty()) {
            Text("Пока нет задач, связанных с проектами.", color = AppColors.Muted, style = MaterialTheme.typography.bodyMedium)
        } else {
            projectShares.forEach { share ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(share.color)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = share.name,
                        modifier = Modifier.weight(1f),
                        color = AppColors.Ink,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${share.count}",
                        color = AppColors.Muted,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
