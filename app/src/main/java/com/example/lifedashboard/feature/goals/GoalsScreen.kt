package com.example.lifedashboard.feature.goals

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.lifedashboard.R
import com.example.lifedashboard.data.DateInput
import com.example.lifedashboard.data.model.DashboardGoal
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.ui.components.AppFab
import com.example.lifedashboard.ui.components.CircularProgressRing
import com.example.lifedashboard.ui.components.EmptySectionState
import com.example.lifedashboard.ui.components.FilterPill
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.components.ScreenTopBar
import com.example.lifedashboard.ui.theme.AppColors

private enum class GoalFilter(val titleResId: Int) {
    ALL(R.string.filter_all), ACTIVE(R.string.filter_active), COMPLETED(R.string.filter_completed)
}

@Composable
fun GoalsScreen(
    goals: List<DashboardGoal>,
    projects: List<DashboardProject>,
    onBack: () -> Unit,
    onSaveGoal: (DashboardGoal, (() -> Unit)?) -> Unit,
    onDeleteGoal: (String, (() -> Unit)?) -> Unit,
    onChangeProgress: (String, Int) -> Unit,
    operationError: String? = null,
    initialGoalId: String? = null,
    startWithCreate: Boolean = false,
    onOpenGoalDetails: (String) -> Unit = {},
    onCreateGoal: (() -> Unit)? = null,
    onOpenSearch: () -> Unit = {}
) {
    val listState = rememberLazyListState()
    var filter by remember { mutableStateOf(GoalFilter.ALL) }
    var showEditor by remember(startWithCreate) { mutableStateOf(startWithCreate && onCreateGoal == null) }

    val completed = goals.count { it.isCompleted }
    val projectNames = remember(projects) { projects.associate { it.id to it.name } }

    LaunchedEffect(initialGoalId, goals) {
        val target = goals.firstOrNull { it.id == initialGoalId }
        if (target != null) filter = if (target.isCompleted) GoalFilter.COMPLETED else GoalFilter.ACTIVE
    }

    val visibleGoals = goals.filter {
        when (filter) {
            GoalFilter.ALL -> true
            GoalFilter.ACTIVE -> !it.isCompleted
            GoalFilter.COMPLETED -> it.isCompleted
        }
    }

    LaunchedEffect(initialGoalId, visibleGoals, operationError) {
        val targetIndex = visibleGoals.indexOfFirst { it.id == initialGoalId }
        if (initialGoalId != null && targetIndex >= 0) {
            val fixedItemsBeforeGoals = 3 + if (operationError != null) 1 else 0
            listState.animateScrollToItem(fixedItemsBeforeGoals + targetIndex)
        }
    }

    fun triggerCreate() {
        if (onCreateGoal != null) onCreateGoal() else showEditor = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ScreenTopBar(
                    title = stringResource(R.string.goals_title),
                    actionIcon = Icons.Outlined.Search,
                    actionDescription = stringResource(R.string.goals_search_desc),
                    onActionClick = onOpenSearch,
                    onBackClick = onBack
                )
            }

            if (goals.isEmpty()) {
                if (operationError != null && !showEditor) {
                    item {
                        PastelCard(color = AppColors.White) {
                            Text(operationError, color = AppColors.Error, fontSize = 13.sp)
                        }
                    }
                }
                item {
                    EmptySectionState(
                        icon = Icons.Outlined.Flag,
                        iconBackground = AppColors.Pink,
                        title = stringResource(R.string.goals_empty_title),
                        subtitle = stringResource(R.string.goals_empty_subtitle)
                    )
                }
            } else {
                item {
                    PastelCard(
                        color = AppColors.Pink,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(18.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "$completed из ${goals.size}",
                            color = AppColors.Ink,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = stringResource(R.string.goals_achieved_count),
                            color = AppColors.Muted,
                            fontSize = 13.sp
                        )
                    }
                }

                if (operationError != null && !showEditor) {
                    item {
                        PastelCard(color = AppColors.White) {
                            Text(operationError, color = AppColors.Error, fontSize = 13.sp)
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GoalFilter.values().forEach { option ->
                            FilterPill(
                                label = stringResource(option.titleResId),
                                selected = filter == option,
                                count = when (option) {
                                    GoalFilter.ALL -> goals.size.toString()
                                    GoalFilter.ACTIVE -> (goals.size - completed).toString()
                                    GoalFilter.COMPLETED -> completed.toString()
                                },
                                onClick = { filter = option }
                            )
                        }
                    }
                }

                if (visibleGoals.isEmpty()) {
                    item {
                        EmptySectionState(
                            icon = Icons.Outlined.Flag,
                            iconBackground = AppColors.Pink,
                            title = if (filter == GoalFilter.ACTIVE) stringResource(R.string.all_goals_achieved) else stringResource(R.string.no_completed_goals),
                            subtitle = stringResource(R.string.empty_filter_goals_subtitle)
                        )
                    }
                } else {
                    items(visibleGoals, key = { it.id }) { goal ->
                        GoalSummaryCard(
                            goal = goal,
                            projectName = projectNames[goal.projectId],
                            highlighted = goal.id == initialGoalId,
                            onClick = { onOpenGoalDetails(goal.id) }
                        )
                    }
                }
            }
        }

        AppFab(
            icon = Icons.Outlined.Add,
            contentDescription = stringResource(R.string.add_goal),
            onClick = ::triggerCreate,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        )
    }

    if (showEditor) {
        GoalEditorScreen(
            initialGoal = null,
            projects = projects,
            operationError = operationError,
            onBack = { showEditor = false },
            onSave = { goal, _ -> onSaveGoal(goal) { showEditor = false } }
        )
    }
}

@Composable
private fun GoalSummaryCard(
    goal: DashboardGoal,
    projectName: String?,
    highlighted: Boolean,
    onClick: () -> Unit
) {
    val initial = goal.title.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "Ц"
    val background = if (highlighted) AppColors.Lavender else goalCardColor(goal.color)
    val statusAchievedStr = stringResource(R.string.status_achieved)
    val subtitle = buildString {
        append("${goal.currentValue} из ${goal.targetValue} ${goal.unit}")
        if (goal.isCompleted) {
            append(" · $statusAchievedStr")
        } else {
            goal.dueDate?.let { append(" · до ${DateInput.formatShort(it)}") }
        }
        if (!projectName.isNullOrBlank()) {
            append(" · $projectName")
        }
    }

    PastelCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = background,
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = AppColors.Ink,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(subtitle, color = AppColors.Muted, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.width(12.dp))
            CircularProgressRing(
                progress = goal.progress,
                size = 52.dp,
                strokeWidth = 5.dp,
                trackColor = AppColors.Ink.copy(alpha = 0.14f),
                progressColor = AppColors.Ink
            )
        }
    }
}