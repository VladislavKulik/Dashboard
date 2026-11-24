package com.example.lifedashboard.feature.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.data.DateInput
import com.example.lifedashboard.data.DateKeys
import com.example.lifedashboard.data.HabitMetrics
import com.example.lifedashboard.data.model.DashboardGoal
import com.example.lifedashboard.data.model.DashboardHabit
import com.example.lifedashboard.feature.habits.ActivityCalendarCard
import com.example.lifedashboard.feature.habits.habitColor
import com.example.lifedashboard.feature.search.SearchScope
import com.example.lifedashboard.ui.components.CircularProgressRing
import com.example.lifedashboard.ui.components.FilterPill
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.components.ScreenTopBar
import com.example.lifedashboard.ui.theme.AppColors

enum class GoalsHabitsTab(val title: String) {
    ALL("Все"),
    HABITS("Привычки"),
    GOALS("Цели")
}

private enum class UnifiedGoalFilter(val title: String) {
    ALL("Все"),
    ACTIVE("В процессе"),
    COMPLETED("Достигнуты")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsAndHabitsScreen(
    habits: List<DashboardHabit>,
    goals: List<DashboardGoal>,
    onBack: () -> Unit,
    onOpenHabit: (String) -> Unit,
    onToggleHabitToday: (String) -> Unit,
    onOpenGoal: (String) -> Unit,
    onCreateHabit: () -> Unit,
    onCreateGoal: () -> Unit,
    onOpenSearch: (SearchScope) -> Unit,
    initialTab: GoalsHabitsTab = GoalsHabitsTab.ALL,
    operationError: String? = null
) {
    var selectedTab by rememberSaveable(initialTab) { mutableStateOf(initialTab) }
    var goalFilter by rememberSaveable { mutableStateOf(UnifiedGoalFilter.ALL) }
    var monthOffset by rememberSaveable { mutableIntStateOf(0) }
    var showCreatePicker by remember { mutableStateOf(false) }

    val activityPercent = remember(habits) { HabitMetrics.monthlyActivityPercent(habits) }
    val completedGoals = remember(goals) { goals.count { it.isCompleted } }
    val todayKey = remember { DateKeys.today() }

    val visibleGoals = remember(goals, goalFilter) {
        goals.filter {
            when (goalFilter) {
                UnifiedGoalFilter.ALL -> true
                UnifiedGoalFilter.ACTIVE -> !it.isCompleted
                UnifiedGoalFilter.COMPLETED -> it.isCompleted
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                ScreenTopBar(
                    title = "Цели и привычки",
                    actionIcon = Icons.Outlined.Search,
                    actionDescription = "Поиск целей и привычек",
                    onActionClick = {
                        val scope = when (selectedTab) {
                            GoalsHabitsTab.HABITS -> SearchScope.HABITS
                            GoalsHabitsTab.GOALS -> SearchScope.GOALS
                            GoalsHabitsTab.ALL -> SearchScope.ALL
                        }
                        onOpenSearch(scope)
                    },
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
                        color = AppColors.Mint,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text("$activityPercent%", color = AppColors.Ink, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                        Text("привычки за месяц", color = AppColors.Muted, fontSize = 12.sp)
                    }
                    PastelCard(
                        modifier = Modifier.weight(1f),
                        color = AppColors.Pink,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text("$completedGoals из ${goals.size}", color = AppColors.Ink, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                        Text("цели достигнуто", color = AppColors.Muted, fontSize = 12.sp)
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(AppColors.White)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    GoalsHabitsTab.values().forEach { tab ->
                        val selected = selectedTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(CircleShape)
                                .background(if (selected) AppColors.Ink else Color.Transparent)
                                .clickable(role = Role.Tab) { selectedTab = tab }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab.title,
                                color = if (selected) AppColors.White else AppColors.Ink,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (!operationError.isNullOrBlank()) {
                item { Text(operationError, color = AppColors.Error, style = MaterialTheme.typography.bodySmall) }
            }

            if (selectedTab == GoalsHabitsTab.ALL || selectedTab == GoalsHabitsTab.HABITS) {
                item {
                    ActivityCalendarCard(
                        habits = habits,
                        monthOffset = monthOffset,
                        onPreviousMonth = { monthOffset -= 1 },
                        onNextMonth = { monthOffset += 1 }
                    )
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Привычки", color = AppColors.Muted, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        Text(habits.size.toString(), color = AppColors.Muted, fontSize = 12.sp)
                    }
                }

                if (habits.isEmpty()) {
                    item {
                        PastelCard(color = AppColors.White, modifier = Modifier.fillMaxWidth()) {
                            Text("Привычек пока нет", color = AppColors.Ink, style = MaterialTheme.typography.titleMedium)
                            Text("Нажми «+», чтобы добавить регулярную привычку.", color = AppColors.Muted, fontSize = 13.sp)
                        }
                    }
                } else {
                    item {
                        PastelCard(
                            modifier = Modifier.fillMaxWidth(),
                            color = AppColors.White,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            habits.forEachIndexed { index, habit ->
                                val checked = todayKey in habit.completionDates
                                val streak = HabitMetrics.currentStreak(habit.completionDates)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onOpenHabit(habit.id) }
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
                                        Text(habit.title, color = AppColors.Ink, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("Серия $streak дн.", color = AppColors.Muted, fontSize = 12.sp)
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .then(
                                                if (checked) Modifier.background(AppColors.Ink)
                                                else Modifier.border(1.8.dp, AppColors.Ink, CircleShape)
                                            )
                                            .clickable(role = Role.Checkbox) { onToggleHabitToday(habit.id) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (checked) {
                                            Icon(Icons.Outlined.Check, contentDescription = "Выполнено сегодня", tint = AppColors.White, modifier = Modifier.size(15.dp))
                                        }
                                    }
                                }
                                if (index < habits.lastIndex) {
                                    HorizontalDivider(color = AppColors.Outline.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }
                }
            }

            if (selectedTab == GoalsHabitsTab.ALL || selectedTab == GoalsHabitsTab.GOALS) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Цели", color = AppColors.Muted, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        Text(goals.size.toString(), color = AppColors.Muted, fontSize = 12.sp)
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UnifiedGoalFilter.values().forEach { filter ->
                            FilterPill(filter.title, goalFilter == filter, { goalFilter = filter })
                        }
                    }
                }

                if (visibleGoals.isEmpty()) {
                    item {
                        PastelCard(color = AppColors.White, modifier = Modifier.fillMaxWidth()) {
                            Text("Целей пока нет", color = AppColors.Ink, style = MaterialTheme.typography.titleMedium)
                            Text("Нажми «+», чтобы поставить измеримую цель.", color = AppColors.Muted, fontSize = 13.sp)
                        }
                    }
                } else {
                    items(visibleGoals, key = { "goal-${it.id}" }) { goal ->
                        val initial = goal.title.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "Ц"
                        val subtitle = buildString {
                            append("${goal.currentValue} из ${goal.targetValue} ${goal.unit}")
                            if (goal.isCompleted) append(" · достигнуто")
                            else goal.dueDate?.let { append(" · до ${DateInput.formatShort(it)}") }
                        }
                        PastelCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenGoal(goal.id) },
                            color = goalCardColor(goal.color),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(AppColors.White.copy(alpha = 0.85f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(initial, color = AppColors.Ink, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(goal.title, style = MaterialTheme.typography.titleMedium, color = AppColors.Ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    Text(subtitle, color = AppColors.Muted, fontSize = 13.sp)
                                }
                                Spacer(Modifier.width(12.dp))
                                CircularProgressRing(progress = goal.progress, size = 50.dp, strokeWidth = 5.dp)
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
                .clickable(role = Role.Button) {
                    when (selectedTab) {
                        GoalsHabitsTab.HABITS -> onCreateHabit()
                        GoalsHabitsTab.GOALS -> onCreateGoal()
                        GoalsHabitsTab.ALL -> showCreatePicker = true
                    }
                },
            color = AppColors.Yellow,
            shape = CircleShape,
            shadowElevation = 6.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Add, contentDescription = "Добавить", tint = AppColors.Ink, modifier = Modifier.size(26.dp))
            }
        }
    }

    if (showCreatePicker) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showCreatePicker = false },
            sheetState = sheetState,
            containerColor = AppColors.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Добавить", style = MaterialTheme.typography.headlineMedium, color = AppColors.Ink)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(AppColors.Mint.copy(alpha = 0.55f))
                        .clickable {
                            showCreatePicker = false
                            onCreateHabit()
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Repeat, contentDescription = null, tint = AppColors.Ink)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Привычка", color = AppColors.Ink, fontWeight = FontWeight.Bold)
                        Text("Регулярное действие по дням", color = AppColors.Muted, fontSize = 12.sp)
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(AppColors.Pink.copy(alpha = 0.55f))
                        .clickable {
                            showCreatePicker = false
                            onCreateGoal()
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Flag, contentDescription = null, tint = AppColors.Ink)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Цель", color = AppColors.Ink, fontWeight = FontWeight.Bold)
                        Text("Измеримый результат к сроку", color = AppColors.Muted, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}