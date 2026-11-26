package com.example.lifedashboard.feature.home

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import com.example.lifedashboard.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.data.HabitMetrics
import com.example.lifedashboard.data.model.DashboardActivityEvent
import com.example.lifedashboard.data.model.DashboardHabit
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.data.model.DashboardTask
import com.example.lifedashboard.data.model.HomeBlockConfig
import com.example.lifedashboard.data.model.HomeBlockType
import com.example.lifedashboard.data.model.TaskDayCategory
import com.example.lifedashboard.feature.tasks.ActiveTimerMiniBar
import com.example.lifedashboard.presentation.TaskTimerState
import com.example.lifedashboard.ui.components.CircleIconButton
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.components.TaskRow
import com.example.lifedashboard.ui.theme.AppColors
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HomeScreen(
    tasks: List<DashboardTask>,
    activeProjectCount: Int,
    onToggleTask: (String) -> Unit,
    onOpenTasks: () -> Unit,
    onOpenProjects: () -> Unit,
    onOpenNotes: () -> Unit = {},
    onOpenHabits: () -> Unit = {},
    showCompletedTasks: Boolean = true,
    activityEvents: List<DashboardActivityEvent> = emptyList(),
    onOpenCalendar: () -> Unit = {},
    homeBlocks: List<HomeBlockConfig> = HomeBlockConfig.DEFAULT_ORDER,
    onOpenSearch: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onUpdateHomeBlocks: (List<HomeBlockConfig>) -> Unit = {},
    projects: List<DashboardProject> = emptyList(),
    habits: List<DashboardHabit> = emptyList(),
    onOpenNotifications: () -> Unit = onOpenSettings,
    onOpenProject: (String) -> Unit = { onOpenProjects() },
    timerState: TaskTimerState = TaskTimerState(),
    onToggleTimerPause: () -> Unit = {},
    onStopTimer: () -> Unit = {},
    onOpenTimer: () -> Unit = {}
) {
    var showLayoutEditor by remember { mutableStateOf(false) }
    var showHeaderMenu by remember { mutableStateOf(false) }

    val today = LocalDate.now()
    val todayTasks = remember(tasks, today) {
        tasks.filter { it.dueDate == today || (it.dueDate == null && it.dayCategory == TaskDayCategory.TODAY) }
    }
    val taskPreview = remember(todayTasks, showCompletedTasks) {
        todayTasks
            .filter { showCompletedTasks || !it.isCompleted }
            .sortedWith(compareBy<DashboardTask> { it.isCompleted }.thenBy { it.priority.ordinal })
            .take(4)
    }
    val completedToday = todayTasks.count { it.isCompleted }
    val dateLabel = today.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.forLanguageTag("ru")))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.forLanguageTag("ru")) else it.toString() }

    val weekDays = remember(today) { (6 downTo 0).map { today.minusDays(it.toLong()) } }
    val weekStart = weekDays.first()
    val previousWeekStart = weekStart.minusDays(7)

    val completedThisWeek = activityEvents.count { event ->
        event.type == "TASK_COMPLETION" && event.completed &&
            !Instant.ofEpochMilli(event.occurredAtEpochMillis).atZone(ZoneId.systemDefault()).toLocalDate().isBefore(weekStart)
    }
    val completedPreviousWeek = activityEvents.count { event ->
        val d = Instant.ofEpochMilli(event.occurredAtEpochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        event.type == "TASK_COMPLETION" && event.completed && !d.isBefore(previousWeekStart) && d.isBefore(weekStart)
    }
    val weekDeltaPercent = if (completedPreviousWeek == 0) null else {
        ((completedThisWeek - completedPreviousWeek) * 100f / completedPreviousWeek).toInt()
    }
    val habitPercent = remember(habits) { HabitMetrics.monthlyActivityPercent(habits) }
    val activeProjects = remember(projects) { projects.filter { !it.isCompleted }.take(3) }
    val visibleBlocks = remember(homeBlocks) { HomeBlockConfig.normalize(homeBlocks).filter { it.visible } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(dateLabel, color = AppColors.Muted, fontSize = 13.sp)
                    Text(
                        text = stringResource(R.string.home_header_title),
                        style = MaterialTheme.typography.headlineLarge,
                        color = AppColors.Ink
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircleIconButton(
                        icon = Icons.Outlined.NotificationsNone,
                        contentDescription = stringResource(R.string.notifications),
                        onClick = onOpenNotifications
                    )
                    Box {
                        CircleIconButton(
                            icon = Icons.Outlined.MoreHoriz,
                            contentDescription = stringResource(R.string.home_menu),
                            onClick = { showHeaderMenu = true }
                        )
                        DropdownMenu(
                            expanded = showHeaderMenu,
                            onDismissRequest = { showHeaderMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.configure_blocks)) },
                                onClick = {
                                    showHeaderMenu = false
                                    showLayoutEditor = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.calendar_title)) },
                                onClick = {
                                    showHeaderMenu = false
                                    onOpenCalendar()
                                }
                            )
                        }
                    }
                }
            }
        }

        if (visibleBlocks.isEmpty()) {
            item {
                PastelCard(color = AppColors.White, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.all_blocks_hidden), style = MaterialTheme.typography.titleLarge, color = AppColors.Ink)
                    Text(stringResource(R.string.all_blocks_hidden_sub), color = AppColors.Muted)
                    TextButton(onClick = { showLayoutEditor = true }) {
                        Text(stringResource(R.string.configure_home), color = AppColors.Ink, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            items(visibleBlocks, key = { it.type.name }) { block ->
                when (block.type) {
                    HomeBlockType.TODAY_TASKS -> {
                        PastelCard(
                            color = AppColors.Yellow,
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(stringResource(R.string.date_today), style = MaterialTheme.typography.titleLarge, color = AppColors.Ink)
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(AppColors.White.copy(alpha = 0.85f))
                                        .clickable(onClick = onOpenTasks)
                                        .padding(horizontal = 12.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = "$completedToday из ${todayTasks.size}",
                                        color = AppColors.Ink,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            if (taskPreview.isEmpty()) {
                                Text(stringResource(R.string.no_today_tasks), color = AppColors.Muted, fontSize = 14.sp)
                            } else {
                                taskPreview.forEach { task ->
                                    TaskRow(
                                        title = task.title,
                                        completed = task.isCompleted,
                                        trailing = task.trailingLabel,
                                        onToggle = { onToggleTask(task.id) }
                                    )
                                }
                            }
                            if (todayTasks.size > taskPreview.size) {
                                Text(
                                    text = stringResource(R.string.show_all_with_count, todayTasks.size),
                                    color = AppColors.Ink.copy(alpha = 0.7f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.clickable(onClick = onOpenTasks)
                                )
                            }
                        }
                    }

                    HomeBlockType.STATS -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HomeMetricCard(
                                value = tasks.size.toString(),
                                label = stringResource(R.string.label_tasks),
                                modifier = Modifier.weight(1f),
                                onClick = onOpenTasks
                            )
                            HomeMetricCard(
                                value = completedThisWeek.toString(),
                                label = stringResource(R.string.label_for_week),
                                modifier = Modifier.weight(1f),
                                onClick = onOpenCalendar
                            )
                            HomeMetricCard(
                                value = "$habitPercent%",
                                label = stringResource(R.string.label_habits),
                                modifier = Modifier.weight(1f),
                                onClick = onOpenHabits
                            )
                        }
                    }

                    HomeBlockType.ACTIVITY -> {
                        WeekStripedActivityCard(
                            days = weekDays,
                            events = activityEvents,
                            deltaPercent = weekDeltaPercent,
                            onClick = onOpenCalendar
                        )
                    }

                    HomeBlockType.QUICK_ACCESS -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = stringResource(R.string.active_projects),
                                style = MaterialTheme.typography.titleMedium,
                                color = AppColors.Ink
                            )

                            PastelCard(
                                color = AppColors.White,
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (activeProjects.isEmpty()) {
                                    Text(
                                        text = if (activeProjectCount == 0) stringResource(R.string.no_active_projects) else stringResource(R.string.active_projects_count, activeProjectCount),
                                        color = AppColors.Muted,
                                        fontSize = 14.sp,
                                        modifier = Modifier.clickable(onClick = onOpenProjects)
                                    )
                                } else {
                                    activeProjects.forEachIndexed { index, project ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onOpenProject(project.id) },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = project.name,
                                                color = AppColors.Ink,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = "${(project.progress * 100).toInt()}%",
                                                color = AppColors.Ink,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                        if (index < activeProjects.lastIndex) {
                                            HorizontalDivider(color = AppColors.Outline.copy(alpha = 0.45f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (timerState.activeTaskId != null) {
            item {
                ActiveTimerMiniBar(
                    timerState = timerState,
                    onClick = onOpenTimer,
                    onTogglePause = onToggleTimerPause,
                    onStop = onStopTimer
                )
            }
        }
    }

    if (showLayoutEditor) {
        HomeLayoutSheet(
            blocks = HomeBlockConfig.normalize(homeBlocks),
            onDismiss = { showLayoutEditor = false },
            onSave = { updated -> onUpdateHomeBlocks(updated) }
        )
    }
}

@Composable
private fun HomeMetricCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    PastelCard(
        modifier = modifier.clickable(onClick = onClick),
        color = AppColors.White,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(value, color = AppColors.Ink, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        Text(label, color = AppColors.Muted, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1)
    }
}

@Composable
private fun WeekStripedActivityCard(
    days: List<LocalDate>,
    events: List<DashboardActivityEvent>,
    deltaPercent: Int?,
    onClick: () -> Unit
) {
    val counts = days.map { day ->
        events.count { event ->
            Instant.ofEpochMilli(event.occurredAtEpochMillis).atZone(ZoneId.systemDefault()).toLocalDate() == day
        }
    }
    val maxCount = counts.maxOrNull()?.coerceAtLeast(1) ?: 1
    val barColor = AppColors.Purple

    PastelCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = AppColors.White,
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Неделя", style = MaterialTheme.typography.titleLarge, color = AppColors.Ink)
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(AppColors.Mint)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = deltaPercent?.let { "${if (it >= 0) "+" else ""}$it%" } ?: "${counts.sum()} соб.",
                    color = AppColors.Ink,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(98.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            counts.forEachIndexed { index, count ->
                val isToday = index == counts.lastIndex
                val barHeight = (14f + 58f * count / maxCount).dp
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(barHeight)
                            .clip(RoundedCornerShape(9.dp))
                    ) {
                        if (isToday) {
                            drawRoundRect(color = barColor, cornerRadius = CornerRadius(9.dp.toPx()))
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
                        text = days[index].dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("ru"))
                            .replaceFirstChar { it.uppercase(Locale.forLanguageTag("ru")) },
                        fontSize = 11.sp,
                        color = if (isToday) AppColors.Ink else AppColors.Muted,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeLayoutSheet(
    blocks: List<HomeBlockConfig>,
    onDismiss: () -> Unit,
    onSave: (List<HomeBlockConfig>) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppColors.Background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                text = stringResource(R.string.home_blocks_title),
                style = MaterialTheme.typography.titleLarge,
                color = AppColors.Ink,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = { onSave(HomeBlockConfig.DEFAULT_ORDER) }) {
                Text(stringResource(R.string.action_reset), color = AppColors.Muted)
            }
            }

            blocks.forEachIndexed { index, item ->
                PastelCard(
                    color = AppColors.White,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(id = item.type.titleResId), color = AppColors.Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(stringResource(id = item.type.descResId), color = AppColors.Muted, fontSize = 11.sp)
                        }
                        IconButton(
                            onClick = {
                                if (index > 0) {
                                    val mutable = blocks.toMutableList()
                                    mutable[index] = mutable[index - 1].also { mutable[index - 1] = item }
                                    onSave(mutable)
                                }
                            },
                            enabled = index > 0
                        ) {
                            Icon(
                                Icons.Outlined.KeyboardArrowUp,
                                contentDescription = stringResource(R.string.action_up),
                                tint = if (index > 0) AppColors.Ink else AppColors.Muted.copy(alpha = 0.35f)
                            )
                        }
                        IconButton(
                            onClick = {
                                if (index < blocks.lastIndex) {
                                    val mutable = blocks.toMutableList()
                                    mutable[index] = mutable[index + 1].also { mutable[index + 1] = item }
                                    onSave(mutable)
                                }
                            },
                            enabled = index < blocks.lastIndex
                        ) {
                            Icon(
                                Icons.Outlined.KeyboardArrowDown,
                                contentDescription = stringResource(R.string.action_down),
                                tint = if (index < blocks.lastIndex) AppColors.Ink else AppColors.Muted.copy(alpha = 0.35f)
                            )
                        }
                        Switch(
                            checked = item.visible,
                            onCheckedChange = { checked ->
                                val mutable = blocks.toMutableList()
                                mutable[index] = item.copy(visible = checked)
                                onSave(mutable)
                            }
                        )
                    }
                }
            }
        }
    }
}