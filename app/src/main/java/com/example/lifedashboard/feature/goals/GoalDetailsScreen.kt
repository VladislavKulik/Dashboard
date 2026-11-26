package com.example.lifedashboard.feature.goals

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.lifedashboard.R
import com.example.lifedashboard.data.DateInput
import com.example.lifedashboard.data.model.DashboardGoal
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.ui.components.CircleIconButton
import com.example.lifedashboard.ui.components.CircularProgressRing
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.theme.AppColors
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun GoalDetailsScreen(
    goal: DashboardGoal,
    projects: List<DashboardProject>,
    onBack: () -> Unit,
    onSaveGoal: (DashboardGoal, (() -> Unit)?) -> Unit,
    onDeleteGoal: (String, (() -> Unit)?) -> Unit,
    onChangeProgress: (String, Int) -> Unit,
    onOpenProject: (String) -> Unit,
    operationError: String? = null,
    onEditGoal: (() -> Unit)? = null
) {
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showEditor by remember(goal.id) { mutableStateOf(false) }
    var showDeleteConfirmation by remember(goal.id) { mutableStateOf(false) }

    val linkedProject = remember(goal.projectId, projects) {
        projects.firstOrNull { it.id == goal.projectId }
    }
    val goalAchieved = stringResource(R.string.goal_achieved)
    val noDeadline = stringResource(R.string.date_no_deadline)
    val context = androidx.compose.ui.platform.LocalContext.current
    val deadlineSubtitle = remember(goal.dueDate, goal.isCompleted, context) {
        when {
            goal.isCompleted -> goalAchieved
            goal.dueDate == null -> noDeadline
            else -> {
                val daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), goal.dueDate)
                val datePart = context.getString(R.string.goal_due_to, DateInput.formatShort(goal.dueDate))
                when {
                    daysLeft < 0 -> context.getString(R.string.goal_overdue, datePart, -daysLeft)
                    daysLeft == 0L -> context.getString(R.string.goal_today, datePart)
                    else -> context.getString(R.string.goal_days_left, datePart, daysLeft)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleIconButton(
                icon = Icons.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.action_back),
                onClick = onBack
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.type_goal),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineLarge,
                color = AppColors.Ink
            )
            Box {
                CircleIconButton(
                    icon = Icons.Outlined.MoreHoriz,
                    contentDescription = stringResource(R.string.action_actions),
                    onClick = { showOverflowMenu = true }
                )
                DropdownMenu(
                    expanded = showOverflowMenu,
                    onDismissRequest = { showOverflowMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_edit)) },
                        onClick = {
                            showOverflowMenu = false
                            if (onEditGoal != null) onEditGoal() else showEditor = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_delete_goal), color = AppColors.Error) },
                        onClick = {
                            showOverflowMenu = false
                            showDeleteConfirmation = true
                        }
                    )
                }
            }
        }

        if (!operationError.isNullOrBlank()) {
            Text(operationError, color = AppColors.Error, style = MaterialTheme.typography.bodySmall)
        }

        PastelCard(
            color = goalCardColor(goal.color),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(22.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = AppColors.Ink,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = deadlineSubtitle,
                    color = AppColors.Muted,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(10.dp))
                CircularProgressRing(
                    progress = goal.progress,
                    size = 112.dp,
                    strokeWidth = 10.dp,
                    trackColor = AppColors.Ink.copy(alpha = 0.14f),
                    progressColor = AppColors.Ink,
                    labelFontSize = 22.sp
                )
            }
        }

        PastelCard(
            color = AppColors.White,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "${goal.currentValue} из ${goal.targetValue}",
                        color = AppColors.Ink,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(goal.unit, color = AppColors.Muted, fontSize = 13.sp)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(AppColors.Background)
                            .clickable(enabled = goal.currentValue > 0) { onChangeProgress(goal.id, -1) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Remove,
                            contentDescription = stringResource(R.string.action_decrease),
                            tint = if (goal.currentValue > 0) AppColors.Ink else AppColors.Muted.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = goal.currentValue.toString(),
                        color = AppColors.Ink,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (goal.isCompleted) AppColors.Background else AppColors.Ink)
                            .clickable(enabled = !goal.isCompleted) { onChangeProgress(goal.id, 1) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Add,
                            contentDescription = stringResource(R.string.action_increase),
                            tint = if (goal.isCompleted) AppColors.Muted.copy(alpha = 0.4f) else AppColors.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        if (linkedProject != null) {
            Text(stringResource(R.string.label_project), color = AppColors.Muted, style = MaterialTheme.typography.bodySmall)
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(AppColors.Ink)
                    .clickable { onOpenProject(linkedProject.id) }
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Text(linkedProject.name, color = AppColors.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        if (goal.description.isNotBlank()) {
            Text(stringResource(R.string.goal_description), color = AppColors.Muted, style = MaterialTheme.typography.bodySmall)
            PastelCard(color = AppColors.White, modifier = Modifier.fillMaxWidth()) {
                Text(goal.description, color = AppColors.Ink, fontSize = 14.sp)
            }
        }
    }

    if (showEditor) {
        GoalEditorScreen(
            initialGoal = goal,
            projects = projects,
            operationError = operationError,
            onBack = { showEditor = false },
            onSave = { updated, _ -> onSaveGoal(updated) { showEditor = false } }
        )
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.delete_goal_title), color = AppColors.Ink) },
            text = { Text(stringResource(R.string.delete_goal_confirm, goal.title), color = AppColors.Muted) },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteGoal(goal.id) {
                        showDeleteConfirmation = false
                        onBack()
                    }
                }) { Text(stringResource(R.string.action_delete), color = AppColors.Error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirmation = false }) { Text(stringResource(R.string.action_cancel), color = AppColors.Muted) } },
            containerColor = AppColors.White
        )
    }
}