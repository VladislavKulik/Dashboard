package com.example.lifedashboard.ui.preview

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.lifedashboard.data.model.DashboardActivityEvent
import com.example.lifedashboard.data.model.DashboardGoal
import com.example.lifedashboard.data.model.DashboardHabit
import com.example.lifedashboard.data.model.DashboardNote
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.data.model.DashboardTask
import com.example.lifedashboard.data.model.DashboardSettings
import com.example.lifedashboard.data.model.DashboardThemeMode
import com.example.lifedashboard.data.model.GoalColor
import com.example.lifedashboard.data.model.HabitColor
import com.example.lifedashboard.data.model.NoteCategory
import com.example.lifedashboard.data.model.NoteColor
import com.example.lifedashboard.data.model.ProjectStage
import com.example.lifedashboard.data.model.TaskDayCategory
import com.example.lifedashboard.data.model.TaskPriority
import com.example.lifedashboard.feature.analytics.AnalyticsScreen
import com.example.lifedashboard.feature.calendar.CalendarScreen
import com.example.lifedashboard.feature.goals.GoalsScreen
import com.example.lifedashboard.feature.habits.HabitsScreen
import com.example.lifedashboard.feature.habits.HabitDetailsScreen
import com.example.lifedashboard.feature.home.HomeScreen
import com.example.lifedashboard.feature.notes.NotesScreen
import com.example.lifedashboard.feature.notes.NoteDetailsScreen
import com.example.lifedashboard.feature.projects.ProjectsScreen
import com.example.lifedashboard.feature.projects.ProjectDetailsScreen
import com.example.lifedashboard.feature.tasks.TasksScreen
import com.example.lifedashboard.feature.settings.SettingsScreen
import com.example.lifedashboard.ui.theme.AppColors
import com.example.lifedashboard.ui.theme.LifeDashboardTheme
import java.time.LocalDate

private object PreviewData {
    val today = LocalDate.now()

    val tasks = listOf(
        DashboardTask(
            id = "preview-task-1",
            title = "Подготовить макет главного экрана",
            trailingLabel = "12:30",
            dueLabel = "Сегодня",
            dayCategory = TaskDayCategory.TODAY,
            priority = TaskPriority.HIGH,
            showOnHome = true,
            dueDate = today
        ),
        DashboardTask(
            id = "preview-task-2",
            title = "Проверить список идей",
            trailingLabel = "15:00",
            dueLabel = "Сегодня",
            dayCategory = TaskDayCategory.TODAY,
            priority = TaskPriority.MEDIUM,
            isCompleted = true,
            showOnHome = true,
            dueDate = today
        ),
        DashboardTask(
            id = "preview-task-3",
            title = "Продумать структуру аналитики",
            dueLabel = "Завтра",
            dayCategory = TaskDayCategory.UPCOMING,
            priority = TaskPriority.LOW,
            showOnHome = true,
            dueDate = today.plusDays(1)
        )
    )

    val projects = listOf(
        DashboardProject(
            id = "preview-project-1",
            name = "Life Dashboard",
            description = "Персональное пространство для задач, проектов и заметок.",
            deadlineLabel = "Через 12 дней",
            stageLabel = "Дизайн и архитектура",
            progress = 0.68f,
            completedTasks = 8,
            totalTasks = 12,
            colorArgb = 0xFFFFE680.toInt(),
            stages = listOf(
                ProjectStage("stage-1", "Структура приложения", true),
                ProjectStage("stage-2", "Основные экраны", true),
                ProjectStage("stage-3", "Полировка интерфейса", false, true)
            ),
            deadlineDate = today.plusDays(12)
        )
    )

    val goals = listOf(
        DashboardGoal(
            id = "preview-goal-1",
            title = "Прочитать 24 книги",
            description = "Регулярно читать и фиксировать заметки.",
            dueDate = today.plusDays(45),
            currentValue = 9,
            targetValue = 24,
            unit = "книг",
            color = GoalColor.LAVENDER
        ),
        DashboardGoal(
            id = "preview-goal-2",
            title = "Завершить личный проект",
            description = "Довести первую версию до рабочего состояния.",
            dueDate = today.plusDays(20),
            currentValue = 6,
            targetValue = 10,
            unit = "этапов",
            projectId = "preview-project-1",
            color = GoalColor.MINT
        )
    )

    val habits = listOf(
        DashboardHabit(
            id = "preview-habit-1",
            title = "Читать 20 минут",
            completionDates = setOf(today.toString(), today.minusDays(1).toString(), today.minusDays(2).toString()),
            color = HabitColor.PINK
        ),
        DashboardHabit(
            id = "preview-habit-2",
            title = "Утренняя прогулка",
            completionDates = setOf(today.toString(), today.minusDays(2).toString()),
            color = HabitColor.BLUE
        )
    )

    val notes = listOf(
        DashboardNote(
            id = "preview-note-1",
            title = "Идеи для интерфейса",
            body = "Упростить навигацию, оставить больше воздуха между карточками и проверить адаптивность на небольших экранах.",
            category = NoteCategory.IDEAS,
            color = NoteColor.YELLOW,
            isPinned = true
        ),
        DashboardNote(
            id = "preview-note-2",
            title = "Что проверить",
            body = "Сохранение данных, работу календаря и поведение фильтров.",
            category = NoteCategory.APP,
            color = NoteColor.LAVENDER
        ),
        DashboardNote(
            id = "preview-note-3",
            title = "На будущее",
            body = "Виджеты Android и быстрые действия с главного экрана.",
            category = NoteCategory.PERSONAL,
            color = NoteColor.MINT
        )
    )

    val activity = listOf(
        DashboardActivityEvent(
            id = "preview-event-1",
            type = "task",
            itemId = "preview-task-2",
            occurredAtEpochMillis = System.currentTimeMillis(),
            completed = true
        )
    )
}

@Composable
private fun PreviewSurface(content: @Composable () -> Unit) {
    LifeDashboardTheme {
        Surface(color = AppColors.Background) {
            content()
        }
    }
}

@Preview(name = "Home · Light", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
fun HomeLightPreview() = PreviewSurface {
    HomeScreen(
        tasks = PreviewData.tasks,
        activeProjectCount = PreviewData.projects.count { !it.isCompleted },
        onToggleTask = {},
        onOpenTasks = {},
        onOpenProjects = {}
    )
}

@Preview(name = "Home · Compact", showBackground = true, widthDp = 320, heightDp = 720)
@Composable
fun HomeCompactPreview() = PreviewSurface {
    HomeScreen(
        tasks = PreviewData.tasks,
        activeProjectCount = PreviewData.projects.count { !it.isCompleted },
        onToggleTask = {},
        onOpenTasks = {},
        onOpenProjects = {}
    )
}

@Preview(name = "Home · Tablet", showBackground = true, widthDp = 840, heightDp = 1180)
@Composable
fun HomeTabletPreview() = PreviewSurface {
    HomeScreen(
        tasks = PreviewData.tasks,
        activeProjectCount = PreviewData.projects.count { !it.isCompleted },
        onToggleTask = {},
        onOpenTasks = {},
        onOpenProjects = {}
    )
}

@Preview(name = "Home · Dark", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
fun HomeDarkPreview() {
    LifeDashboardTheme(darkTheme = true) {
        Surface(color = AppColors.Background) {
            HomeScreen(
                tasks = PreviewData.tasks,
                activeProjectCount = 1,
                onToggleTask = {},
                onOpenTasks = {},
                onOpenProjects = {}
            )
        }
    }
}

@Preview(name = "Tasks", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
fun TasksScreenPreview() = PreviewSurface {
    TasksScreen(
        tasks = PreviewData.tasks,
        projects = PreviewData.projects,
        onToggleTask = {},
        onAddTask = { _, _, _, _, _, _, _, _ -> },
        onOpenProject = {},
        onUpdateTask = { _, onSaved -> onSaved() },
        onDeleteTask = { _, onDeleted -> onDeleted() }
    )
}

@Preview(name = "Tasks · Compact", showBackground = true, widthDp = 320, heightDp = 720)
@Composable
fun TasksCompactPreview() = PreviewSurface {
    TasksScreen(
        tasks = PreviewData.tasks,
        projects = PreviewData.projects,
        onToggleTask = {},
        onAddTask = { _, _, _, _, _, _, _, _ -> },
        onOpenProject = {},
        onUpdateTask = { _, onSaved -> onSaved() },
        onDeleteTask = { _, onDeleted -> onDeleted() }
    )
}

@Preview(name = "Tasks · Tablet", showBackground = true, widthDp = 840, heightDp = 1180)
@Composable
fun TasksTabletPreview() = PreviewSurface {
    TasksScreen(
        tasks = PreviewData.tasks,
        projects = PreviewData.projects,
        onToggleTask = {},
        onAddTask = { _, _, _, _, _, _, _, _ -> },
        onOpenProject = {},
        onUpdateTask = { _, onSaved -> onSaved() },
        onDeleteTask = { _, onDeleted -> onDeleted() }
    )
}

@Preview(name = "Projects", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
fun ProjectsScreenPreview() = PreviewSurface {
    ProjectsScreen(
        projects = PreviewData.projects,
        tasks = PreviewData.tasks,
        onOpenProject = {},
        onToggleTask = {},
        onSaveProject = { _, onSaved -> onSaved() }
    )
}

@Preview(name = "Projects · Tablet", showBackground = true, widthDp = 840, heightDp = 1180)
@Composable
fun ProjectsTabletPreview() = PreviewSurface {
    ProjectsScreen(
        projects = PreviewData.projects,
        tasks = PreviewData.tasks,
        onOpenProject = {},
        onToggleTask = {},
        onSaveProject = { _, onSaved -> onSaved() }
    )
}

@Preview(name = "Goals", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
fun GoalsScreenPreview() = PreviewSurface {
    GoalsScreen(
        goals = PreviewData.goals,
        projects = PreviewData.projects,
        onBack = {},
        onSaveGoal = { _, onSaved -> onSaved?.invoke() },
        onDeleteGoal = { _, onDeleted -> onDeleted?.invoke() },
        onChangeProgress = { _, _ -> }
    )
}

@Preview(name = "Calendar", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
fun CalendarScreenPreview() = PreviewSurface {
    CalendarScreen(
        tasks = PreviewData.tasks,
        projects = PreviewData.projects,
        goals = PreviewData.goals,
        habits = PreviewData.habits,
        activityEvents = PreviewData.activity,
        onBack = {},
        onEventClick = {}
    )
}

@Preview(name = "Habits", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
fun HabitsScreenPreview() = PreviewSurface {
    HabitsScreen(
        habits = PreviewData.habits,
        onBack = {},
        onOpenHabit = {},
        onSaveHabit = { _, onSaved -> onSaved?.invoke() },
        onToggleToday = {}
    )
}

@Preview(name = "Notes", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
fun NotesScreenPreview() = PreviewSurface {
    NotesScreen(
        notes = PreviewData.notes,
        onBack = {},
        onOpenNote = {},
        onCreateNote = {},
        onTogglePinned = {}
    )
}

@Preview(name = "Analytics", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
fun AnalyticsScreenPreview() = PreviewSurface {
    AnalyticsScreen(
        projects = PreviewData.projects,
        tasks = PreviewData.tasks,
        activityEvents = PreviewData.activity,
        onBack = {}
    )
}


@Preview(name = "Project details", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
fun ProjectDetailsScreenPreview() = PreviewSurface {
    ProjectDetailsScreen(
        project = PreviewData.projects.first(),
        tasks = PreviewData.tasks,
        onBack = {},
        onToggleTask = {},
        onOpenAllTasks = {},
        onToggleStage = { _, _ -> },
        onSaveProject = { _, onSaved -> onSaved() },
        onDeleteProject = { _, onDeleted -> onDeleted() }
    )
}

@Preview(name = "Note editor", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
fun NoteDetailsScreenPreview() = PreviewSurface {
    NoteDetailsScreen(
        note = PreviewData.notes.first(),
        onBack = {},
        onSave = {},
        onDelete = {}
    )
}

@Preview(name = "Habit details", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
fun HabitDetailsScreenPreview() = PreviewSurface {
    HabitDetailsScreen(
        habit = PreviewData.habits.first(),
        onBack = {},
        onToggleToday = {},
        onSaveHabit = { _, onSaved -> onSaved?.invoke() },
        onDeleteHabit = { _, onDeleted -> onDeleted?.invoke() }
    )
}

@Preview(name = "Settings", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
fun SettingsScreenPreview() = PreviewSurface {
    SettingsScreen(
        settings = DashboardSettings(themeMode = DashboardThemeMode.LIGHT),
        notificationsPermissionGranted = true,
        exactAlarmAccessGranted = true,
        onBack = {},
        onThemeModeChange = {},
        onNotificationsEnabledChange = {},
        onShowCompletedTasksChange = {},
        onOpenNotificationSettings = {},
        onOpenExactAlarmSettings = {}
    )
}
