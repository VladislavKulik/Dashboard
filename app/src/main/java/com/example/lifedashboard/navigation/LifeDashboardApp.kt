package com.example.lifedashboard.navigation

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lifedashboard.data.DateInput
import com.example.lifedashboard.data.HabitMetrics
import com.example.lifedashboard.data.local.DataStoreSettingsRepository
import com.example.lifedashboard.data.local.RoomDashboardPersistence
import com.example.lifedashboard.data.local.dashboardSettingsDataStore
import com.example.lifedashboard.data.model.DashboardGoal
import com.example.lifedashboard.data.model.DashboardHabit
import com.example.lifedashboard.data.model.DashboardNote
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.data.model.DashboardTask
import com.example.lifedashboard.data.model.DashboardThemeMode
import com.example.lifedashboard.data.model.TaskDayCategory
import com.example.lifedashboard.feature.analytics.AnalyticsScreen
import com.example.lifedashboard.feature.calendar.CalendarEventType
import com.example.lifedashboard.feature.calendar.CalendarScreen
import com.example.lifedashboard.feature.common.PlaceholderScreen
import com.example.lifedashboard.feature.goals.GoalDetailsScreen
import com.example.lifedashboard.feature.goals.GoalEditorScreen
import com.example.lifedashboard.feature.goals.GoalsScreen
import com.example.lifedashboard.feature.habits.HabitDetailsScreen
import com.example.lifedashboard.feature.habits.HabitEditorScreen
import com.example.lifedashboard.feature.habits.HabitsScreen
import com.example.lifedashboard.feature.home.HomeScreen
import com.example.lifedashboard.feature.menu.MenuScreen
import com.example.lifedashboard.feature.notes.NoteDetailsScreen
import com.example.lifedashboard.feature.notes.NoteViewScreen
import com.example.lifedashboard.feature.notes.NotesScreen
import com.example.lifedashboard.feature.projects.ProjectDetailsScreen
import com.example.lifedashboard.feature.projects.ProjectEditorScreen
import com.example.lifedashboard.feature.projects.ProjectHistoryScreen
import com.example.lifedashboard.feature.projects.ProjectsScreen
import com.example.lifedashboard.feature.search.SearchScope
import com.example.lifedashboard.feature.search.SearchScreen
import com.example.lifedashboard.feature.settings.BackupsScreen
import com.example.lifedashboard.feature.settings.NotificationTargetType
import com.example.lifedashboard.feature.settings.NotificationsScreen
import com.example.lifedashboard.feature.settings.ProfileScreen
import com.example.lifedashboard.feature.settings.SettingsScreen
import com.example.lifedashboard.feature.tasks.TaskCreateScreen
import com.example.lifedashboard.feature.tasks.TaskDetailsScreen
import com.example.lifedashboard.feature.tasks.TasksScreen
import com.example.lifedashboard.feature.tasks.TimerScreen
import com.example.lifedashboard.presentation.DashboardViewModel
import com.example.lifedashboard.reminders.AndroidHabitReminderScheduler
import com.example.lifedashboard.reminders.AndroidTaskReminderScheduler
import com.example.lifedashboard.ui.components.AppBottomBar
import com.example.lifedashboard.ui.components.QuickAddSheet
import com.example.lifedashboard.ui.components.QuickAddType
import com.example.lifedashboard.ui.components.RootDestination
import com.example.lifedashboard.ui.theme.AppColors
import com.example.lifedashboard.ui.theme.LifeDashboardTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LifeDashboardApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val persistence = remember(context) { RoomDashboardPersistence.create(context.applicationContext) }
    val reminderScheduler = remember(context) { AndroidHabitReminderScheduler(context.applicationContext) }
    val taskReminderScheduler = remember(context) { AndroidTaskReminderScheduler(context.applicationContext) }
    val settingsRepository = remember(context) {
        DataStoreSettingsRepository(context.applicationContext.dashboardSettingsDataStore)
    }
    val backupRepository = remember(persistence) { persistence.createBackupRepository() }
    val dashboardViewModel: DashboardViewModel = viewModel(
        factory = remember(persistence, reminderScheduler, taskReminderScheduler, settingsRepository, backupRepository) {
            DashboardViewModel.Factory(
                persistence, persistence, persistence, persistence, persistence, persistence,
                reminderScheduler, taskReminderScheduler, settingsRepository, backupRepository, persistence
            )
        }
    )

    val appSettings by dashboardViewModel.appSettings.collectAsState()
    val settingsOperationError by dashboardViewModel.settingsOperationError.collectAsState()
    val dashboardState by dashboardViewModel.uiState.collectAsState()
    val timerState by dashboardViewModel.timerState.collectAsState()
    val recentSearches by dashboardViewModel.recentSearches.collectAsState()
    val noteOperationError by dashboardViewModel.noteOperationError.collectAsState()
    val habitOperationError by dashboardViewModel.habitOperationError.collectAsState()
    val taskOperationError by dashboardViewModel.taskOperationError.collectAsState()
    val projectOperationError by dashboardViewModel.projectOperationError.collectAsState()
    val goalOperationError by dashboardViewModel.goalOperationError.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    var backupBusy by remember { mutableStateOf(false) }
    var backupMessage by remember { mutableStateOf<String?>(null) }
    var backupError by remember { mutableStateOf<String?>(null) }
    var pendingFullBackup by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var showImportConfirmation by remember { mutableStateOf(false) }
    var showQuickAdd by remember { mutableStateOf(false) }

    val exportDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri ->
            if (uri != null) {
                coroutineScope.launch {
                    backupBusy = true
                    backupMessage = null
                    backupError = null
                    try {
                        val json = dashboardViewModel.exportBackup(includeSettings = pendingFullBackup)
                        withContext(Dispatchers.IO) {
                            val output = context.contentResolver.openOutputStream(uri)
                                ?: error("Не удалось открыть файл для записи.")
                            output.use { it.write(json.toByteArray(Charsets.UTF_8)) }
                        }
                        backupMessage = if (pendingFullBackup) {
                            "Полная резервная копия сохранена. Она содержит данные Room и настройки DataStore."
                        } else {
                            "Экспорт JSON сохранён. Настройки приложения в него не входят."
                        }
                    } catch (error: Exception) {
                        backupError = error.message ?: "Не удалось создать файл."
                    } finally {
                        backupBusy = false
                    }
                }
            }
        }
    )

    val importDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                pendingImportUri = uri
                showImportConfirmation = true
                backupMessage = null
                backupError = null
            }
        }
    )

    val pendingExactAlarmAccess = remember { mutableStateOf(false) }
    val pendingGlobalNotificationsEnable = remember { mutableStateOf(false) }

    fun hasNotificationAccess(): Boolean {
        val runtimePermissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        val appNotificationsEnabled = Build.VERSION.SDK_INT < Build.VERSION_CODES.N ||
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)?.areNotificationsEnabled() != false
        return runtimePermissionGranted && appNotificationsEnabled
    }

    var notificationsPermissionGranted by remember(context) {
        mutableStateOf(hasNotificationAccess())
    }
    var exactAlarmAccessGranted by remember(context) {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                (context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager)?.canScheduleExactAlarms() == true
        )
    }

    fun refreshPermissionState() {
        notificationsPermissionGranted = hasNotificationAccess()
        exactAlarmAccessGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            (context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager)?.canScheduleExactAlarms() == true
    }

    val exactAlarmSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { refreshPermissionState() }
    )
    val notificationSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { refreshPermissionState() }
    )

    fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val settingsIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        try {
            exactAlarmSettingsLauncher.launch(settingsIntent)
        } catch (_: Exception) {
        }
    }

    fun openNotificationSettings() {
        val settingsIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
        }
        try {
            notificationSettingsLauncher.launch(settingsIntent)
        } catch (_: Exception) {
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (pendingGlobalNotificationsEnable.value) {
                dashboardViewModel.setNotificationsEnabled(granted)
            }
            if (granted && pendingExactAlarmAccess.value) openExactAlarmSettings()
            pendingGlobalNotificationsEnable.value = false
            pendingExactAlarmAccess.value = false
            refreshPermissionState()
        }
    )

    val onGlobalNotificationsChange: (Boolean) -> Unit = { enabled ->
        if (!enabled) {
            dashboardViewModel.setNotificationsEnabled(false)
        } else {
            val needsNotificationPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            val needsExactAlarmAccess = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !exactAlarmAccessGranted
            when {
                needsNotificationPermission -> {
                    pendingGlobalNotificationsEnable.value = true
                    pendingExactAlarmAccess.value = needsExactAlarmAccess
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                !notificationsPermissionGranted -> openNotificationSettings()
                else -> {
                    dashboardViewModel.setNotificationsEnabled(true)
                    if (needsExactAlarmAccess) openExactAlarmSettings()
                }
            }
        }
    }

    val saveHabit: (DashboardHabit, (() -> Unit)?) -> Unit = { habit, onSaved ->
        dashboardViewModel.saveHabit(habit, onSaved)
        if (habit.reminderEnabled && appSettings.notificationsEnabled) {
            val needsNotificationPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            val needsExactAlarmAccess = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                alarmManager?.canScheduleExactAlarms() != true
            if (needsNotificationPermission) {
                pendingExactAlarmAccess.value = needsExactAlarmAccess
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else if (needsExactAlarmAccess) {
                openExactAlarmSettings()
            }
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isFromSectionSearch = backStackEntry?.arguments?.getBoolean("fromSection") == true

    val showBottomBar = when {
        currentRoute == Destinations.HOME -> true
        currentRoute == Destinations.MENU -> true
        currentRoute == Destinations.SETTINGS -> true
        currentRoute?.startsWith(Destinations.SEARCH) == true && !isFromSectionSearch -> true
        else -> false
    }

    val selectedRoot = when {
        currentRoute == Destinations.HOME -> RootDestination.HOME
        currentRoute == Destinations.MENU -> RootDestination.MENU
        currentRoute?.startsWith(Destinations.SEARCH) == true -> RootDestination.SEARCH
        currentRoute == Destinations.SETTINGS -> RootDestination.SETTINGS
        else -> RootDestination.MENU
    }

    val useDarkTheme = when (appSettings.themeMode) {
        DashboardThemeMode.LIGHT -> false
        DashboardThemeMode.DARK -> true
        DashboardThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    LifeDashboardTheme(darkTheme = useDarkTheme) {
        Scaffold(
            containerColor = AppColors.Background,
            bottomBar = {
                if (showBottomBar) {
                    AppBottomBar(
                        selected = selectedRoot,
                        onHomeClick = { navigateRoot(navController, Destinations.HOME) },
                        onMenuClick = { navigateRoot(navController, Destinations.MENU) },
                        onSearchClick = { navigateRoot(navController, Destinations.SEARCH) },
                        onSettingsClick = { navigateRoot(navController, Destinations.SETTINGS) },
                        onAddClick = { showQuickAdd = true }
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Destinations.HOME,
                modifier = Modifier.padding(innerPadding),
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None }
            ) {
                composable(Destinations.HOME) { entry ->
                    HomeScreen(
                        tasks = dashboardState.tasks,
                        activeProjectCount = dashboardState.projects.count { !it.isCompleted },
                        onToggleTask = dashboardViewModel::toggleTask,
                        onOpenTasks = { navController.safeNavigate(entry, Destinations.TASKS) },
                        onOpenProjects = { navController.safeNavigate(entry, Destinations.PROJECTS) },
                        onOpenNotes = { navController.safeNavigate(entry, Destinations.NOTES) },
                        onOpenHabits = { navController.safeNavigate(entry, Destinations.HABITS) },
                        showCompletedTasks = appSettings.showCompletedTasksOnHome,
                        activityEvents = dashboardState.activityEvents,
                        onOpenCalendar = { navController.safeNavigate(entry, Destinations.CALENDAR) },
                        homeBlocks = appSettings.homeBlocks,
                        onOpenSearch = { navigateRoot(navController, Destinations.SEARCH) },
                        onOpenSettings = { navigateRoot(navController, Destinations.SETTINGS) },
                        onUpdateHomeBlocks = dashboardViewModel::setHomeBlocks,
                        projects = dashboardState.projects,
                        habits = dashboardState.habits,
                        onOpenNotifications = { navController.safeNavigate(entry, Destinations.NOTIFICATIONS) },
                        onOpenProject = { projectId ->
                            navController.safeNavigate(entry, "${Destinations.PROJECT_DETAILS}/$projectId")
                        },
                        timerState = timerState,
                        onToggleTimerPause = dashboardViewModel::toggleTimerPause,
                        onStopTimer = dashboardViewModel::stopTimer,
                        onOpenTimer = { navController.safeNavigate(entry, Destinations.TIMER) }
                    )
                }

                composable(Destinations.MENU) { entry ->
                    val today = LocalDate.now()
                    val todayTaskCount = dashboardState.tasks.count {
                        !it.isCompleted && (it.dueDate == today || (it.dueDate == null && it.dayCategory == TaskDayCategory.TODAY))
                    }
                    val weekStart = today.minusDays(6)
                    val completedThisWeek = dashboardState.activityEvents.count { event ->
                        event.type == "TASK_COMPLETION" && event.completed &&
                            !Instant.ofEpochMilli(event.occurredAtEpochMillis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                                .isBefore(weekStart)
                    }
                    val bestStreak = dashboardState.habits.maxOfOrNull {
                        HabitMetrics.currentStreak(it.completionDates)
                    } ?: 0

                    MenuScreen(
                        onNavigate = { route ->
                            navController.safeNavigate(entry, route)
                        },
                        onSettingsClick = { navigateRoot(navController, Destinations.SETTINGS) },
                        noteCount = dashboardState.notes.size,
                        goalCount = dashboardState.goals.size,
                        completedGoalCount = dashboardState.goals.count { it.isCompleted },
                        activeProjectCount = dashboardState.projects.count { !it.isCompleted },
                        todayTaskCount = todayTaskCount,
                        completedTasksThisWeek = completedThisWeek,
                        bestHabitStreak = bestStreak,
                        habitCount = dashboardState.habits.size,
                        onSearchClick = { navigateRoot(navController, Destinations.SEARCH) }
                    )
                }

                composable(
                    route = "${Destinations.SEARCH}?scope={scope}&fromSection={fromSection}",
                    arguments = listOf(
                        navArgument("scope") {
                            type = NavType.StringType
                            defaultValue = SearchScope.ALL.name
                        },
                        navArgument("fromSection") {
                            type = NavType.BoolType
                            defaultValue = false
                        }
                    )
                ) { entry ->
                    val rawScope = entry.arguments?.getString("scope") ?: SearchScope.ALL.name
                    val initialScope = runCatching { SearchScope.valueOf(rawScope) }.getOrDefault(SearchScope.ALL)
                    val fromSection = entry.arguments?.getBoolean("fromSection") == true

                    SearchScreen(
                        state = dashboardState,
                        onBack = if (fromSection) ({ navController.safePopBackStack(entry) }) else null,
                        initialScope = initialScope,
                        recentQueries = recentSearches,
                        onSaveRecentQuery = dashboardViewModel::addRecentSearch,
                        onRemoveRecentQuery = dashboardViewModel::removeRecentSearch,
                        onResultClick = { result ->
                            when (result.scope) {
                                SearchScope.TASKS -> navController.safeNavigate(entry, "${Destinations.TASK_DETAILS}/${result.entityId}")
                                SearchScope.PROJECTS -> navController.safeNavigate(entry, "${Destinations.PROJECT_DETAILS}/${result.entityId}")
                                SearchScope.NOTES -> navController.safeNavigate(entry, "${Destinations.NOTE_VIEW}/${result.entityId}")
                                SearchScope.GOALS -> navController.safeNavigate(entry, "${Destinations.GOAL_DETAILS}/${result.entityId}")
                                SearchScope.HABITS -> navController.safeNavigate(entry, "${Destinations.HABIT_DETAILS}/${result.entityId}")
                                SearchScope.ALL -> Unit
                            }
                        }
                    )
                }

                composable(Destinations.PROJECTS) { entry ->
                    ProjectsScreen(
                        projects = dashboardState.projects,
                        tasks = dashboardState.tasks,
                        goals = dashboardState.goals,
                        notes = dashboardState.notes,
                        onOpenProject = { projectId ->
                            navController.safeNavigate(entry, "${Destinations.PROJECT_DETAILS}/$projectId")
                        },
                        onToggleTask = dashboardViewModel::toggleTask,
                        onSaveProject = dashboardViewModel::saveProject,
                        operationError = projectOperationError,
                        onBack = { navController.safePopBackStack(entry) },
                        onCreateProject = { navController.safeNavigate(entry, Destinations.PROJECT_EDITOR) },
                        onOpenSearch = {
                            navController.safeNavigate(entry, "${Destinations.SEARCH}?scope=${SearchScope.PROJECTS.name}&fromSection=true")
                        }
                    )
                }

                composable(
                    route = "${Destinations.PROJECT_EDITOR}?projectId={projectId}&dueDate={dueDate}",
                    arguments = listOf(
                        navArgument("projectId") { type = NavType.StringType; nullable = true; defaultValue = null },
                        navArgument("dueDate") { type = NavType.StringType; nullable = true; defaultValue = null }
                    )
                ) { entry ->
                    val projectId = entry.arguments?.getString("projectId")
                    val initialProject = projectId?.let { id -> dashboardState.projects.firstOrNull { it.id == id } }
                    val initialDueDate = DateInput.parseIso(entry.arguments?.getString("dueDate"))

                    ProjectEditorScreen(
                        initialProject = initialProject,
                        initialDeadlineDate = initialDueDate,
                        operationError = projectOperationError,
                        onBack = { navController.safePopBackStack(entry) },
                        onSave = { project, _ ->
                            dashboardViewModel.saveProject(project) { navController.safePopBackStack(entry) }
                        }
                    )
                }

                composable(
                    route = "${Destinations.PROJECT_DETAILS}/{projectId}",
                    arguments = listOf(navArgument("projectId") { type = NavType.StringType })
                ) { entry ->
                    val projectId = entry.arguments?.getString("projectId").orEmpty()
                    val liveProject = dashboardState.projects.firstOrNull { it.id == projectId }
                    var cachedProject by remember(projectId) { mutableStateOf<DashboardProject?>(liveProject) }
                    if (liveProject != null) cachedProject = liveProject
                    val project = liveProject ?: cachedProject

                    if (project == null) {
                        PlaceholderScreen("Проект не найден", "Возможно, он был удалён.") {
                            navController.safePopBackStack(entry)
                        }
                    } else {
                        ProjectDetailsScreen(
                            project = project,
                            tasks = dashboardState.tasks.filter { it.projectId == projectId },
                            allTasks = dashboardState.tasks,
                            goals = dashboardState.goals,
                            notes = dashboardState.notes,
                            activityEvents = dashboardState.activityEvents,
                            onBack = { navController.safePopBackStack(entry) },
                            onToggleTask = dashboardViewModel::toggleTask,
                            onOpenAllTasks = {
                                navController.safeNavigate(entry, "${Destinations.TASKS}?projectId=$projectId")
                            },
                            onToggleStage = dashboardViewModel::toggleProjectStage,
                            onSaveProject = dashboardViewModel::saveProject,
                            onDeleteProject = { id, onDone ->
                                dashboardViewModel.deleteProject(id) { onDone() }
                            },
                            operationError = projectOperationError,
                            onAddTask = { title, priority, projId, dueDate, recurrence, recurrenceDays, reminderEnabled, reminderTime ->
                                dashboardViewModel.addTask(
                                    title = title,
                                    priority = priority,
                                    projectId = projId ?: projectId,
                                    dueDate = dueDate,
                                    recurrence = recurrence,
                                    recurrenceDays = recurrenceDays,
                                    reminderEnabled = reminderEnabled,
                                    reminderTime = reminderTime
                                )
                            },
                            onOpenTaskDetails = { taskId ->
                                navController.safeNavigate(entry, "${Destinations.TASK_DETAILS}/$taskId")
                            },
                            onOpenGoal = { goalId ->
                                navController.safeNavigate(entry, "${Destinations.GOAL_DETAILS}/$goalId")
                            },
                            onOpenNote = { noteId ->
                                navController.safeNavigate(entry, "${Destinations.NOTE_VIEW}/$noteId")
                            },
                            onAddGoal = {
                                navController.safeNavigate(entry, "${Destinations.GOAL_EDITOR}?projectId=$projectId")
                            },
                            onAddNote = {
                                navController.safeNavigate(entry, "${Destinations.NOTE_DETAILS}/new?projectId=$projectId")
                            },
                            onEditProject = {
                                navController.safeNavigate(entry, "${Destinations.PROJECT_EDITOR}?projectId=$projectId")
                            },
                            onOpenAddTask = {
                                navController.safeNavigate(entry, "${Destinations.TASK_CREATE}?projectId=$projectId")
                            },
                            onOpenHistory = {
                                navController.safeNavigate(entry, "${Destinations.PROJECT_HISTORY}/$projectId")
                            },
                            onAttachExistingItems = { taskIds, goalIds, noteIds ->
                                dashboardViewModel.attachItemsToProject(projectId, taskIds, goalIds, noteIds)
                            }
                        )
                    }
                }

                composable(
                    route = "${Destinations.PROJECT_HISTORY}/{projectId}",
                    arguments = listOf(navArgument("projectId") { type = NavType.StringType })
                ) { entry ->
                    val projectId = entry.arguments?.getString("projectId").orEmpty()
                    val project = dashboardState.projects.firstOrNull { it.id == projectId }
                    if (project == null) {
                        PlaceholderScreen("Проект не найден", "Возможно, он был удалён.") {
                            navController.safePopBackStack(entry)
                        }
                    } else {
                        ProjectHistoryScreen(
                            project = project,
                            tasks = dashboardState.tasks.filter { it.projectId == projectId },
                            activityEvents = dashboardState.activityEvents,
                            timerSessions = dashboardState.timerSessions,
                            onBack = { navController.safePopBackStack(entry) },
                            onOpenTaskDetails = { taskId ->
                                navController.safeNavigate(entry, "${Destinations.TASK_DETAILS}/$taskId")
                            }
                        )
                    }
                }

                composable(
                    route = "${Destinations.TASKS}?projectId={projectId}&taskId={taskId}",
                    arguments = listOf(
                        navArgument("projectId") { type = NavType.StringType; nullable = true; defaultValue = null },
                        navArgument("taskId") { type = NavType.StringType; nullable = true; defaultValue = null }
                    )
                ) { entry ->
                    val projectId = entry.arguments?.getString("projectId")
                    val taskId = entry.arguments?.getString("taskId")
                    TasksScreen(
                        tasks = dashboardState.tasks,
                        projects = dashboardState.projects,
                        initialProjectId = projectId,
                        initialTaskId = taskId,
                        onToggleTask = dashboardViewModel::toggleTask,
                        onAddTask = { title, priority, projId, dueDate, recurrence, recurrenceDays, reminderEnabled, reminderTime ->
                            dashboardViewModel.addTask(
                                title = title,
                                priority = priority,
                                projectId = projId,
                                dueDate = dueDate,
                                recurrence = recurrence,
                                recurrenceDays = recurrenceDays,
                                reminderEnabled = reminderEnabled,
                                reminderTime = reminderTime
                            )
                        },
                        onOpenProject = { id ->
                            navController.safeNavigate(entry, "${Destinations.PROJECT_DETAILS}/$id")
                        },
                        onUpdateTask = dashboardViewModel::updateTask,
                        onDeleteTask = dashboardViewModel::deleteTask,
                        operationError = taskOperationError,
                        onOpenTaskDetails = { id ->
                            navController.safeNavigate(entry, "${Destinations.TASK_DETAILS}/$id")
                        },
                        onBack = { navController.safePopBackStack(entry) },
                        timerState = timerState,
                        onToggleTimerPause = dashboardViewModel::toggleTimerPause,
                        onStopTimer = dashboardViewModel::stopTimer,
                        onOpenTimer = { navController.safeNavigate(entry, Destinations.TIMER) },
                        onCreateTask = {
                            val route = if (projectId != null) {
                                "${Destinations.TASK_CREATE}?projectId=$projectId"
                            } else {
                                Destinations.TASK_CREATE
                            }
                            navController.safeNavigate(entry, route)
                        },
                        onOpenSearch = {
                            navController.safeNavigate(entry, "${Destinations.SEARCH}?scope=${SearchScope.TASKS.name}&fromSection=true")
                        }
                    )
                }

                composable(
                    route = "${Destinations.TASK_CREATE}?taskId={taskId}&projectId={projectId}&dueDate={dueDate}",
                    arguments = listOf(
                        navArgument("taskId") { type = NavType.StringType; nullable = true; defaultValue = null },
                        navArgument("projectId") { type = NavType.StringType; nullable = true; defaultValue = null },
                        navArgument("dueDate") { type = NavType.StringType; nullable = true; defaultValue = null }
                    )
                ) { entry ->
                    val taskId = entry.arguments?.getString("taskId")
                    val initialTask = taskId?.let { id -> dashboardState.tasks.firstOrNull { it.id == id } }
                    val initialProjectId = entry.arguments?.getString("projectId")
                    val initialDueDate = DateInput.parseIso(entry.arguments?.getString("dueDate"))

                    TaskCreateScreen(
                        projects = dashboardState.projects,
                        initialTask = initialTask,
                        initialProjectId = initialProjectId,
                        initialDueDate = initialDueDate,
                        onBack = { navController.safePopBackStack(entry) },
                        onCreate = { title, priority, projectId, dueDate, recurrence, recurrenceDays, reminderEnabled, reminderTime, endDate, remCount, plannedMinutes ->
                            dashboardViewModel.addTask(
                                title = title,
                                priority = priority,
                                projectId = projectId,
                                dueDate = dueDate,
                                recurrence = recurrence,
                                recurrenceDays = recurrenceDays,
                                reminderEnabled = reminderEnabled,
                                reminderTime = reminderTime,
                                recurrenceEndDate = endDate,
                                recurrenceRemainingCount = remCount,
                                plannedMinutes = plannedMinutes
                            )
                        },
                        onUpdate = { updated, _ ->
                            dashboardViewModel.updateTask(updated) { navController.safePopBackStack(entry) }
                        }
                    )
                }

                composable(
                    route = "${Destinations.TASK_DETAILS}/{taskId}",
                    arguments = listOf(navArgument("taskId") { type = NavType.StringType })
                ) { entry ->
                    val taskId = entry.arguments?.getString("taskId").orEmpty()
                    val liveTask = dashboardState.tasks.firstOrNull { it.id == taskId }
                    var cachedTask by remember(taskId) { mutableStateOf<DashboardTask?>(liveTask) }
                    if (liveTask != null) cachedTask = liveTask
                    val task = liveTask ?: cachedTask

                    if (task == null) {
                        PlaceholderScreen("Задача не найдена", "Возможно, она была удалена.") {
                            navController.safePopBackStack(entry)
                        }
                    } else {
                        TaskDetailsScreen(
                            task = task,
                            allTasks = dashboardState.tasks,
                            projects = dashboardState.projects,
                            goals = dashboardState.goals,
                            activityEvents = dashboardState.activityEvents,
                            onBack = { navController.safePopBackStack(entry) },
                            onToggleTask = dashboardViewModel::toggleTask,
                            onUpdateTask = dashboardViewModel::updateTask,
                            onDeleteTask = { id, onDone ->
                                dashboardViewModel.deleteTask(id) { onDone() }
                            },
                            onOpenProject = { projectId ->
                                navController.safeNavigate(entry, "${Destinations.PROJECT_DETAILS}/$projectId")
                            },
                            onOpenGoal = { goalId ->
                                navController.safeNavigate(entry, "${Destinations.GOAL_DETAILS}/$goalId")
                            },
                            onOpenTask = { otherTaskId ->
                                navController.safeNavigate(entry, "${Destinations.TASK_DETAILS}/$otherTaskId")
                            },
                            operationError = taskOperationError,
                            timerState = timerState,
                            onStartTimer = { targetTask, projectName ->
                                dashboardViewModel.startTimer(targetTask, projectName)
                                navController.safeNavigate(entry, Destinations.TIMER)
                            },
                            subtasks = dashboardState.subtasks,
                            onAddSubtask = dashboardViewModel::addSubtask,
                            onToggleSubtask = dashboardViewModel::toggleSubtask,
                            onDeleteSubtask = dashboardViewModel::deleteSubtask,
                            onEditTask = {
                                navController.safeNavigate(entry, "${Destinations.TASK_CREATE}?taskId=${task.id}")
                            }
                        )
                    }
                }

                composable(Destinations.TIMER) { entry ->
                    TimerScreen(
                        timerState = timerState,
                        onBack = { navController.safePopBackStack(entry) },
                        onTogglePause = dashboardViewModel::toggleTimerPause,
                        onStop = {
                            dashboardViewModel.stopTimer()
                            navController.safePopBackStack(entry)
                        }
                    )
                }

                composable(Destinations.NOTES) { entry ->
                    NotesScreen(
                        notes = dashboardState.notes,
                        attachments = dashboardState.noteAttachments,
                        projects = dashboardState.projects,
                        onBack = { navController.safePopBackStack(entry) },
                        onOpenNote = { noteId ->
                            navController.safeNavigate(entry, "${Destinations.NOTE_VIEW}/$noteId")
                        },
                        onCreateNote = {
                            navController.safeNavigate(entry, "${Destinations.NOTE_DETAILS}/new")
                        },
                        onTogglePinned = dashboardViewModel::toggleNotePinned,
                        onDeleteNote = { id -> dashboardViewModel.deleteNote(id) },
                        operationError = noteOperationError,
                        onOpenSearch = {
                            navController.safeNavigate(entry, "${Destinations.SEARCH}?scope=${SearchScope.NOTES.name}&fromSection=true")
                        }
                    )
                }

                composable(
                    route = "${Destinations.NOTE_VIEW}/{noteId}",
                    arguments = listOf(navArgument("noteId") { type = NavType.StringType })
                ) { entry ->
                    val noteId = entry.arguments?.getString("noteId").orEmpty()
                    val liveNote = dashboardState.notes.firstOrNull { it.id == noteId }
                    var cachedNote by remember(noteId) { mutableStateOf<DashboardNote?>(liveNote) }
                    if (liveNote != null) cachedNote = liveNote
                    val note = liveNote ?: cachedNote

                    if (note == null) {
                        PlaceholderScreen("Заметка не найдена", "Возможно, она была удалена.") {
                            navController.safePopBackStack(entry)
                        }
                    } else {
                        NoteViewScreen(
                            note = note,
                            projects = dashboardState.projects,
                            attachments = dashboardState.noteAttachments,
                            onBack = { navController.safePopBackStack(entry) },
                            onEdit = {
                                navController.safeNavigate(entry, "${Destinations.NOTE_DETAILS}/${note.id}")
                            },
                            onTogglePinned = { dashboardViewModel.toggleNotePinned(note.id) },
                            onDelete = {
                                navController.safePopBackStack(entry)
                                dashboardViewModel.deleteNote(note.id)
                            },
                            onOpenProject = { projectId ->
                                navController.safeNavigate(entry, "${Destinations.PROJECT_DETAILS}/$projectId")
                            },
                            onUpdateNoteBody = { updatedBody ->
                                dashboardViewModel.saveNote(note.copy(body = updatedBody))
                            }
                        )
                    }
                }

                composable(
                    route = "${Destinations.NOTE_DETAILS}/{noteId}?projectId={projectId}",
                    arguments = listOf(
                        navArgument("noteId") { type = NavType.StringType },
                        navArgument("projectId") { type = NavType.StringType; nullable = true; defaultValue = null }
                    )
                ) { entry ->
                    val noteId = entry.arguments?.getString("noteId").orEmpty()
                    val initialProjectId = entry.arguments?.getString("projectId")
                    val isNewNote = noteId == "new"
                    val liveNote = if (isNewNote) null else dashboardState.notes.firstOrNull { it.id == noteId }
                    var cachedNote by remember(noteId) { mutableStateOf<DashboardNote?>(liveNote) }
                    if (liveNote != null) cachedNote = liveNote
                    val note = if (isNewNote) null else (liveNote ?: cachedNote)

                    if (!isNewNote && note == null) {
                        PlaceholderScreen("Заметка не найдена", "Возможно, она была удалена.") {
                            navController.safePopBackStack(entry)
                        }
                    } else {
                        NoteDetailsScreen(
                            note = note,
                            projects = dashboardState.projects,
                            initialProjectId = initialProjectId,
                            attachments = dashboardState.noteAttachments,
                            onAddAttachment = { targetNoteId, uri ->
                                dashboardViewModel.addNoteAttachment(context, targetNoteId, uri)
                            },
                            onDeleteAttachment = dashboardViewModel::deleteNoteAttachment,
                            onBack = { navController.safePopBackStack(entry) },
                            onSave = { updatedNote ->
                                dashboardViewModel.saveNote(updatedNote) { navController.safePopBackStack(entry) }
                            },
                            onDelete = { deletedNoteId ->
                                val previousIsView = navController.previousBackStackEntry
                                    ?.destination?.route?.startsWith(Destinations.NOTE_VIEW) == true
                                navController.popBackStack()
                                if (previousIsView) {
                                    navController.popBackStack()
                                }
                                dashboardViewModel.deleteNote(deletedNoteId)
                            },
                            operationError = noteOperationError,
                            allAvailableLabels = dashboardState.notes.flatMap { it.labels }.distinct()
                        )
                    }
                }

                composable(
                    route = "${Destinations.GOALS}?goalId={goalId}",
                    arguments = listOf(
                        navArgument("goalId") { type = NavType.StringType; nullable = true; defaultValue = null }
                    )
                ) { entry ->
                    val goalId = entry.arguments?.getString("goalId")
                    GoalsScreen(
                        goals = dashboardState.goals,
                        projects = dashboardState.projects,
                        initialGoalId = goalId,
                        operationError = goalOperationError,
                        onBack = { navController.safePopBackStack(entry) },
                        onSaveGoal = dashboardViewModel::saveGoal,
                        onDeleteGoal = dashboardViewModel::deleteGoal,
                        onChangeProgress = dashboardViewModel::changeGoalProgress,
                        onOpenGoalDetails = { id ->
                            navController.safeNavigate(entry, "${Destinations.GOAL_DETAILS}/$id")
                        },
                        onCreateGoal = {
                            navController.safeNavigate(entry, Destinations.GOAL_EDITOR)
                        },
                        onOpenSearch = {
                            navController.safeNavigate(entry, "${Destinations.SEARCH}?scope=${SearchScope.GOALS.name}&fromSection=true")
                        }
                    )
                }

                composable(
                    route = "${Destinations.GOAL_DETAILS}/{goalId}",
                    arguments = listOf(navArgument("goalId") { type = NavType.StringType })
                ) { entry ->
                    val goalId = entry.arguments?.getString("goalId").orEmpty()
                    val liveGoal = dashboardState.goals.firstOrNull { it.id == goalId }
                    var cachedGoal by remember(goalId) { mutableStateOf<DashboardGoal?>(liveGoal) }
                    if (liveGoal != null) cachedGoal = liveGoal
                    val goal = liveGoal ?: cachedGoal

                    if (goal == null) {
                        PlaceholderScreen("Цель не найдена", "Возможно, она была удалена.") {
                            navController.safePopBackStack(entry)
                        }
                    } else {
                        GoalDetailsScreen(
                            goal = goal,
                            projects = dashboardState.projects,
                            operationError = goalOperationError,
                            onBack = { navController.safePopBackStack(entry) },
                            onSaveGoal = dashboardViewModel::saveGoal,
                            onDeleteGoal = { id, onDone ->
                                dashboardViewModel.deleteGoal(id) { onDone?.invoke() }
                            },
                            onChangeProgress = dashboardViewModel::changeGoalProgress,
                            onOpenProject = { projectId ->
                                navController.safeNavigate(entry, "${Destinations.PROJECT_DETAILS}/$projectId")
                            },
                            onEditGoal = {
                                navController.safeNavigate(entry, "${Destinations.GOAL_EDITOR}?goalId=${goal.id}")
                            }
                        )
                    }
                }

                composable(
                    route = "${Destinations.GOAL_EDITOR}?goalId={goalId}&projectId={projectId}&dueDate={dueDate}",
                    arguments = listOf(
                        navArgument("goalId") { type = NavType.StringType; nullable = true; defaultValue = null },
                        navArgument("projectId") { type = NavType.StringType; nullable = true; defaultValue = null },
                        navArgument("dueDate") { type = NavType.StringType; nullable = true; defaultValue = null }
                    )
                ) { entry ->
                    val goalId = entry.arguments?.getString("goalId")
                    val initialGoal = goalId?.let { id -> dashboardState.goals.firstOrNull { it.id == id } }
                    val initialProjectId = entry.arguments?.getString("projectId")
                    val initialDueDate = DateInput.parseIso(entry.arguments?.getString("dueDate"))

                    GoalEditorScreen(
                        initialGoal = initialGoal,
                        projects = dashboardState.projects,
                        initialProjectId = initialProjectId,
                        initialDueDate = initialDueDate,
                        operationError = goalOperationError,
                        onBack = { navController.safePopBackStack(entry) },
                        onSave = { goal, _ ->
                            dashboardViewModel.saveGoal(goal) { navController.safePopBackStack(entry) }
                        }
                    )
                }

                composable(Destinations.HABITS) { entry ->
                    HabitsScreen(
                        habits = dashboardState.habits,
                        operationError = habitOperationError,
                        onBack = { navController.safePopBackStack(entry) },
                        onOpenHabit = { habitId ->
                            navController.safeNavigate(entry, "${Destinations.HABIT_DETAILS}/$habitId")
                        },
                        onSaveHabit = saveHabit,
                        onToggleToday = dashboardViewModel::toggleHabitToday,
                        onCreateHabit = {
                            navController.safeNavigate(entry, Destinations.HABIT_EDITOR)
                        },
                        onOpenSearch = {
                            navController.safeNavigate(entry, "${Destinations.SEARCH}?scope=${SearchScope.HABITS.name}&fromSection=true")
                        }
                    )
                }

                composable(
                    route = "${Destinations.HABIT_DETAILS}/{habitId}",
                    arguments = listOf(navArgument("habitId") { type = NavType.StringType })
                ) { entry ->
                    val habitId = entry.arguments?.getString("habitId").orEmpty()
                    val liveHabit = dashboardState.habits.firstOrNull { it.id == habitId }
                    var cachedHabit by remember(habitId) { mutableStateOf<DashboardHabit?>(liveHabit) }
                    if (liveHabit != null) cachedHabit = liveHabit
                    val habit = liveHabit ?: cachedHabit

                    if (habit == null) {
                        PlaceholderScreen("Привычка не найдена", "Возможно, она была удалена.") {
                            navController.safePopBackStack(entry)
                        }
                    } else {
                        HabitDetailsScreen(
                            habit = habit,
                            onBack = { navController.safePopBackStack(entry) },
                            onToggleToday = { dashboardViewModel.toggleHabitToday(habitId) },
                            onToggleDate = dashboardViewModel::toggleHabitOnDate,
                            onSaveHabit = saveHabit,
                            onDeleteHabit = { id, callback ->
                                navController.safePopBackStack(entry)
                                dashboardViewModel.deleteHabit(id) {
                                    callback?.invoke()
                                }
                            },
                            operationError = habitOperationError,
                            onEditHabit = {
                                navController.safeNavigate(entry, "${Destinations.HABIT_EDITOR}?habitId=${habit.id}")
                            }
                        )
                    }
                }

                composable(
                    route = "${Destinations.HABIT_EDITOR}?habitId={habitId}",
                    arguments = listOf(
                        navArgument("habitId") { type = NavType.StringType; nullable = true; defaultValue = null }
                    )
                ) { entry ->
                    val habitId = entry.arguments?.getString("habitId")
                    val initialHabit = habitId?.let { id -> dashboardState.habits.firstOrNull { it.id == id } }

                    HabitEditorScreen(
                        initialHabit = initialHabit,
                        operationError = habitOperationError,
                        onBack = { navController.safePopBackStack(entry) },
                        onSave = { habit, _ ->
                            saveHabit(habit) { navController.safePopBackStack(entry) }
                        }
                    )
                }

                composable(Destinations.ANALYTICS) { entry ->
                    AnalyticsScreen(
                        projects = dashboardState.projects,
                        tasks = dashboardState.tasks,
                        activityEvents = dashboardState.activityEvents,
                        habits = dashboardState.habits,
                        goals = dashboardState.goals,
                        timerState = timerState,
                        onBack = { navController.safePopBackStack(entry) }
                    )
                }

                composable(Destinations.CALENDAR) { entry ->
                    CalendarScreen(
                        tasks = dashboardState.tasks,
                        projects = dashboardState.projects,
                        goals = dashboardState.goals,
                        habits = dashboardState.habits,
                        activityEvents = dashboardState.activityEvents,
                        operationError = taskOperationError,
                        onBack = { navController.safePopBackStack(entry) },
                        onToggleTask = dashboardViewModel::toggleTask,
                        onToggleHabitOnDate = dashboardViewModel::toggleHabitOnDate,
                        onOpenTaskDetails = { taskId ->
                            navController.safeNavigate(entry, "${Destinations.TASK_DETAILS}/$taskId")
                        },
                        onOpenCreateTask = { date ->
                            navController.safeNavigate(entry, "${Destinations.TASK_CREATE}?dueDate=$date")
                        },
                        onOpenEditTask = { taskId ->
                            navController.safeNavigate(entry, "${Destinations.TASK_CREATE}?taskId=$taskId")
                        },
                        onOpenCreateProject = { date ->
                            navController.safeNavigate(entry, "${Destinations.PROJECT_EDITOR}?dueDate=$date")
                        },
                        onOpenCreateGoal = { date ->
                            navController.safeNavigate(entry, "${Destinations.GOAL_EDITOR}?dueDate=$date")
                        },
                        onEventClick = { event ->
                            when (event.type) {
                                CalendarEventType.TASK -> navController.safeNavigate(entry, "${Destinations.TASK_DETAILS}/${event.relatedId}")
                                CalendarEventType.PROJECT -> navController.safeNavigate(entry, "${Destinations.PROJECT_DETAILS}/${event.relatedId}")
                                CalendarEventType.GOAL -> navController.safeNavigate(entry, "${Destinations.GOAL_DETAILS}/${event.relatedId}")
                                CalendarEventType.HABIT -> navController.safeNavigate(entry, "${Destinations.HABIT_DETAILS}/${event.relatedId}")
                                CalendarEventType.ACTIVITY -> navController.safeNavigate(entry, Destinations.ANALYTICS)
                            }
                        }
                    )
                }

                composable(Destinations.SETTINGS) { entry ->
                    SettingsScreen(
                        settings = appSettings,
                        notificationsPermissionGranted = notificationsPermissionGranted,
                        exactAlarmAccessGranted = exactAlarmAccessGranted,
                        operationError = settingsOperationError,
                        backupBusy = backupBusy,
                        backupMessage = backupMessage,
                        backupError = backupError,
                        onExportData = {
                            pendingFullBackup = false
                            exportDocumentLauncher.launch("LifeDashboard_export.json")
                        },
                        onCreateBackup = {
                            pendingFullBackup = true
                            exportDocumentLauncher.launch("LifeDashboard_backup.json")
                        },
                        onImportBackup = {
                            importDocumentLauncher.launch(arrayOf("application/json", "text/json", "*/*"))
                        },
                        onThemeModeChange = dashboardViewModel::setThemeMode,
                        onNotificationsEnabledChange = onGlobalNotificationsChange,
                        onShowCompletedTasksChange = dashboardViewModel::setShowCompletedTasksOnHome,
                        onOpenNotificationSettings = ::openNotificationSettings,
                        onOpenExactAlarmSettings = ::openExactAlarmSettings,
                        onOpenProfile = { navController.safeNavigate(entry, Destinations.PROFILE) },
                        onOpenBackups = { navController.safeNavigate(entry, Destinations.BACKUPS) }
                    )
                }

                composable(Destinations.PROFILE) { entry ->
                    val bestStreak = dashboardState.habits.maxOfOrNull {
                        HabitMetrics.currentStreak(it.completionDates)
                    } ?: 0
                    ProfileScreen(
                        settings = appSettings,
                        projectCount = dashboardState.projects.size,
                        noteCount = dashboardState.notes.size,
                        bestHabitStreak = bestStreak,
                        onBack = { navController.safePopBackStack(entry) },
                        onSaveProfile = dashboardViewModel::setProfile
                    )
                }

                composable(Destinations.BACKUPS) { entry ->
                    BackupsScreen(
                        backupBusy = backupBusy,
                        backupMessage = backupMessage,
                        backupError = backupError,
                        onBack = { navController.safePopBackStack(entry) },
                        onCreateBackup = {
                            pendingFullBackup = true
                            exportDocumentLauncher.launch("LifeDashboard_backup.json")
                        },
                        onExportData = {
                            pendingFullBackup = false
                            exportDocumentLauncher.launch("LifeDashboard_export.json")
                        },
                        onImportBackup = {
                            importDocumentLauncher.launch(arrayOf("application/json", "text/json", "*/*"))
                        }
                    )
                }

                composable(Destinations.NOTIFICATIONS) { entry ->
                    NotificationsScreen(
                        state = dashboardState,
                        onBack = { navController.safePopBackStack(entry) },
                        onOpenItem = { type, id ->
                            when (type) {
                                NotificationTargetType.TASK -> navController.safeNavigate(entry, "${Destinations.TASK_DETAILS}/$id")
                                NotificationTargetType.HABIT -> navController.safeNavigate(entry, "${Destinations.HABIT_DETAILS}/$id")
                                NotificationTargetType.PROJECT -> navController.safeNavigate(entry, "${Destinations.PROJECT_DETAILS}/$id")
                                NotificationTargetType.GOAL -> navController.safeNavigate(entry, "${Destinations.GOAL_DETAILS}/$id")
                            }
                        }
                    )
                }
            }
        }

        if (showQuickAdd) {
            QuickAddSheet(
                onDismiss = { showQuickAdd = false },
                onSelect = { type ->
                    showQuickAdd = false
                    val destination = when (type) {
                        QuickAddType.TASK -> Destinations.TASK_CREATE
                        QuickAddType.PROJECT -> Destinations.PROJECT_EDITOR
                        QuickAddType.NOTE -> "${Destinations.NOTE_DETAILS}/new"
                        QuickAddType.HABIT -> Destinations.HABIT_EDITOR
                        QuickAddType.GOAL -> Destinations.GOAL_EDITOR
                    }
                    navController.navigate(destination) {
                        launchSingleTop = true
                    }
                }
            )
        }

        if (showImportConfirmation) {
            AlertDialog(
                onDismissRequest = {
                    showImportConfirmation = false
                    pendingImportUri = null
                },
                title = { Text("Заменить текущие данные?") },
                text = {
                    Text(
                        "Импорт заменит все текущие задачи, проекты, заметки, привычки, цели и историю активности. " +
                            "Настройки DataStore будут заменены только при импорте полной резервной копии."
                    )
                },
                confirmButton = {
                    TextButton(
                        enabled = !backupBusy,
                        onClick = {
                            val uri = pendingImportUri
                            showImportConfirmation = false
                            pendingImportUri = null
                            if (uri != null) {
                                coroutineScope.launch {
                                    backupBusy = true
                                    backupMessage = null
                                    backupError = null
                                    try {
                                        val json = withContext(Dispatchers.IO) { readJsonDocument(context, uri) }
                                        val result = dashboardViewModel.restoreBackup(json)
                                        backupMessage = buildString {
                                            append("Восстановлено: ${result.taskCount} задач, ${result.projectCount} проектов, ")
                                            append("${result.noteCount} заметок, ${result.habitCount} привычек, ${result.goalCount} целей и ${result.eventCount} событий.")
                                            if (result.settingsRestored == true) append(" Настройки тоже восстановлены.")
                                            if (result.settingsRestored == false) append(" Данные восстановлены, но настройки не удалось применить.")
                                        }
                                    } catch (error: Exception) {
                                        backupError = error.message ?: "Не удалось восстановить данные."
                                    } finally {
                                        backupBusy = false
                                    }
                                }
                            }
                        }
                    ) { Text("Заменить данные") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showImportConfirmation = false
                        pendingImportUri = null
                    }) { Text("Отмена") }
                }
            )
        }
    }
}

private fun NavBackStackEntry.isResumed(): Boolean =
    lifecycle.currentState == Lifecycle.State.RESUMED

private fun NavHostController.safeNavigate(fromEntry: NavBackStackEntry, route: String) {
    if (!fromEntry.isResumed()) return
    navigate(route) {
        launchSingleTop = true
    }
}

private fun NavHostController.safePopBackStack(fromEntry: NavBackStackEntry) {
    if (!fromEntry.isResumed()) return
    popBackStack()
}

private fun navigateRoot(navController: NavHostController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            inclusive = (route == Destinations.HOME)
            saveState = false
        }
        launchSingleTop = true
        restoreState = false
    }
}

private fun readJsonDocument(context: Context, uri: Uri): String {
    val input = context.contentResolver.openInputStream(uri)
        ?: throw IllegalArgumentException("Не удалось открыть выбранный файл.")
    input.use { stream ->
        val output = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(8 * 1024)
        var total = 0
        while (true) {
            val count = stream.read(buffer)
            if (count < 0) break
            total += count
            require(total <= MAX_IMPORT_BYTES) { "Файл слишком большой. Максимальный размер — 20 МБ." }
            output.write(buffer, 0, count)
        }
        return output.toString(Charsets.UTF_8.name())
    }
}

private const val MAX_IMPORT_BYTES = 20 * 1024 * 1024