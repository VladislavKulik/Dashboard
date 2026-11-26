package com.example.lifedashboard.feature.menu

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.lifedashboard.R
import com.example.lifedashboard.navigation.Destinations
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.theme.AppColors

private data class MenuItemUi(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val route: String,
    val isDark: Boolean = false
)

@Composable
fun MenuScreen(
    onNavigate: (String) -> Unit,
    onSettingsClick: () -> Unit = {},
    noteCount: Int = 0,
    goalCount: Int = 0,
    completedGoalCount: Int = 0,
    activeProjectCount: Int = 0,
    todayTaskCount: Int = 0,
    completedTasksThisWeek: Int = 0,
    bestHabitStreak: Int = 0,
    habitCount: Int = 0,
    onSearchClick: () -> Unit = {}
) {
    val noGoalsStr = stringResource(R.string.menu_no_goals)
    val goalsProgressStr = stringResource(R.string.menu_goals_progress, completedGoalCount, goalCount)
    val goalsSubtitle = if (goalCount == 0) {
        noGoalsStr
    } else {
        goalsProgressStr
    }

    val noHabitsStr = stringResource(R.string.menu_no_habits)
    val habitStreakStr = stringResource(R.string.menu_habit_streak, habitCount, bestHabitStreak)
    val habitsSubtitle = when {
        habitCount == 0 -> noHabitsStr
        bestHabitStreak > 0 -> habitStreakStr
        else -> "$habitCount ${russianPlural(habitCount, stringResource(R.string.habit_plural_1), stringResource(R.string.habit_plural_2_4), stringResource(R.string.habit_plural_5_0))}"
    }

    val projectsTitle = stringResource(R.string.menu_projects)
    val tasksTitle = stringResource(R.string.menu_tasks)
    val tasksTodaySubtitle = stringResource(R.string.menu_tasks_today, todayTaskCount)
    val notesTitle = stringResource(R.string.notes_title)
    val goalsTitle = stringResource(R.string.goals_title)
    val habitsTitle = stringResource(R.string.habits_title)
    val calendarTitle = stringResource(R.string.calendar_title)
    val calendarSubtitle = stringResource(R.string.calendar_subtitle)
    val analyticsTitle = stringResource(R.string.analytics_title)
    val sectionsTitle = stringResource(R.string.sections_title)
    val defaultAnalyticsDesc = stringResource(R.string.analytics_default_desc)

    val gridItems = listOf(
        MenuItemUi(projectsTitle, activeProjectCountLabel(activeProjectCount), Icons.Outlined.FolderOpen, AppColors.Lavender, Destinations.PROJECTS),
        MenuItemUi(tasksTitle, tasksTodaySubtitle, Icons.Outlined.CheckCircleOutline, AppColors.Blue, Destinations.TASKS),
        MenuItemUi(notesTitle, noteCountLabel(noteCount), Icons.Outlined.Description, AppColors.Yellow, Destinations.NOTES),
        MenuItemUi(goalsTitle, goalsSubtitle, Icons.Outlined.Flag, AppColors.Pink, Destinations.GOALS),
        MenuItemUi(habitsTitle, habitsSubtitle, Icons.Outlined.Repeat, AppColors.Mint, Destinations.HABITS),
        MenuItemUi(calendarTitle, calendarSubtitle, Icons.Outlined.CalendarMonth, AppColors.Lavender.copy(alpha = 0.75f), Destinations.CALENDAR)
    )

    val analyticsItem = MenuItemUi(
        title = analyticsTitle,
        subtitle = weeklyCompletedLabel(completedTasksThisWeek, defaultAnalyticsDesc),
        icon = Icons.Outlined.BarChart,
        color = AppColors.Ink,
        route = Destinations.ANALYTICS,
        isDark = true
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(2) }) {
            Text(
                text = sectionsTitle,
                style = MaterialTheme.typography.headlineLarge,
                color = AppColors.Ink
            )
        }

        items(gridItems, key = { it.route }) { item ->
            MenuTile(
                item = item,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(152.dp),
                onClick = { onNavigate(item.route) }
            )
        }

        item(span = { GridItemSpan(2) }) {
            WideMenuTile(
                item = analyticsItem,
                modifier = Modifier.fillMaxWidth(),
                onClick = { onNavigate(analyticsItem.route) }
            )
        }
    }
}

@Composable
private fun MenuTile(item: MenuItemUi, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val titleColor = if (item.isDark) Color.White else AppColors.Ink
    val subtitleColor = if (item.isDark) Color(0xFFA7A4B0) else AppColors.Muted
    val badgeColor = if (item.isDark) Color(0xFFD9D8DE) else AppColors.White.copy(alpha = 0.78f)
    val iconTint = Color(0xFF151515)

    PastelCard(
        modifier = modifier.clickable(role = Role.Button, onClick = onClick),
        color = item.color,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(badgeColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(item.icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleLarge, color = titleColor)
            Text(item.subtitle, fontSize = 13.sp, color = subtitleColor)
        }
    }
}

@Composable
private fun WideMenuTile(item: MenuItemUi, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val titleColor = if (item.isDark) Color.White else AppColors.Ink
    val subtitleColor = if (item.isDark) Color(0xFFA7A4B0) else AppColors.Muted
    val badgeColor = if (item.isDark) Color(0xFFD9D8DE) else AppColors.White.copy(alpha = 0.78f)
    val iconTint = Color(0xFF151515)

    PastelCard(
        modifier = modifier.clickable(role = Role.Button, onClick = onClick),
        color = item.color,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(badgeColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(item.title, style = MaterialTheme.typography.titleLarge, color = titleColor)
                Text(item.subtitle, fontSize = 13.sp, color = subtitleColor)
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = subtitleColor
            )
        }
    }
}

private fun noteCountLabel(count: Int): String =
    "$count ${russianPlural(count, "запись", "записи", "записей")}"

private fun activeProjectCountLabel(count: Int): String =
    "$count ${russianPlural(count, "активный", "активных", "активных")}"

private fun weeklyCompletedLabel(count: Int, defaultDesc: String): String =
    if (count == 0) defaultDesc
    else "$count ${russianPlural(count, "задача", "задачи", "задач")} выполнено за 7 дн."

private fun russianPlural(count: Int, one: String, few: String, many: String): String {
    val lastTwo = count % 100
    val last = count % 10
    return when {
        lastTwo in 11..14 -> many
        last == 1 -> one
        last in 2..4 -> few
        else -> many
    }
}