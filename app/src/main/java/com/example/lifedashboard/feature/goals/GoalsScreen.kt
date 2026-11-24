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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.lifedashboard.data.DateInput
import com.example.lifedashboard.data.model.DashboardGoal
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.ui.components.CircularProgressRing
import com.example.lifedashboard.ui.components.FilterPill
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.components.ScreenTopBar
import com.example.lifedashboard.ui.theme.AppColors

private enum class GoalFilter(val title: String) {
    ALL("Все"), ACTIVE("В процессе"), COMPLETED("Достигнуты")
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
    onOpenGoalDetails: (String) -> Unit = {}
) {
    val listState = rememberLazyListState()
    var filter by remember { mutableStateOf(GoalFilter.ALL) }
    var showEditor by remember(startWithCreate) { mutableStateOf(startWithCreate) }

    val completed = goals.count { it.isCompleted }

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

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ScreenTopBar(
                title = "Цели",
                actionIcon = Icons.Outlined.Add,
                actionDescription = "Добавить цель",
                onActionClick = { showEditor = true },
                onBackClick = onBack
            )
        }

        item {
            PastelCard(
                color = AppColors.Blue,
                contentPadding = PaddingValues(18.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Личный прогресс", color = AppColors.Ink, style = MaterialTheme.typography.titleLarge)
                Text("Достигнуто $completed из ${goals.size}", color = AppColors.Muted, fontSize = 14.sp)
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
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GoalFilter.values().forEach { option ->
                    FilterPill(
                        label = option.title,
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
                PastelCard(color = AppColors.White) {
                    Text(
                        text = when {
                            goals.isEmpty() -> "Пока нет целей"
                            filter == GoalFilter.ACTIVE -> "Все цели достигнуты"
                            else -> "Пока нет достигнутых целей"
                        },
                        color = AppColors.Ink,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = if (goals.isEmpty()) "Создай цель, укажи желаемый результат и постепенно отмечай прогресс." else "Выбери другой фильтр, чтобы посмотреть остальные цели.",
                        color = AppColors.Muted,
                        fontSize = 14.sp
                    )
                    if (goals.isEmpty()) {
                        TextButton(onClick = { showEditor = true }) { Text("Создать цель", color = AppColors.Ink, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        } else {
            items(visibleGoals, key = { it.id }) { goal ->
                GoalSummaryCard(
                    goal = goal,
                    highlighted = goal.id == initialGoalId,
                    onClick = { onOpenGoalDetails(goal.id) }
                )
            }
        }
    }

    if (showEditor) {
        GoalEditorDialog(
            initialGoal = null,
            projects = projects,
            operationError = operationError,
            onDismiss = { showEditor = false },
            onSave = { goal -> onSaveGoal(goal) { showEditor = false } }
        )
    }
}

@Composable
private fun GoalSummaryCard(
    goal: DashboardGoal,
    highlighted: Boolean,
    onClick: () -> Unit
) {
    val initial = goal.title.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "Ц"
    val background = if (highlighted) AppColors.Lavender else goalCardColor(goal.color)
    val subtitle = buildString {
        append("${goal.currentValue} из ${goal.targetValue}")
        if (goal.isCompleted) {
            append(" · достигнуто")
        } else {
            goal.dueDate?.let { append(" · до ${DateInput.formatShort(it)}") }
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
                Text(subtitle, color = AppColors.Muted, fontSize = 13.sp)
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