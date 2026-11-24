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
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.lifedashboard.data.model.DashboardHabit
import com.example.lifedashboard.data.model.DashboardThemeMode
import com.example.lifedashboard.data.model.TaskDayCategory
import com.example.lifedashboard.feature.analytics.AnalyticsScreen
import com.example.lifedashboard.feature.calendar.CalendarEventType
import com.example.lifedashboard.feature.calendar.CalendarScreen
import com.example.lifedashboard.feature.common.PlaceholderScreen
import com.example.lifedashboard.feature.goals.GoalDetailsScreen
import com.example.lifedashboard.feature.goals.GoalEditorScreen
import com.example.lifedashboard.feature.goals.GoalsAndHabitsScreen
import com.example.lifedashboard.feature.goals.GoalsHabitsTab
import com.example.lifedashboard.feature.habits.HabitDetailsScreen
import com.example.lifedashboard.feature.habits.HabitEditorScreen
import com.example.lifedashboard.feature.home.HomeScreen
import com.example.lifedashboard.feature.menu.MenuScreen
import com.example.lifedashboard.feature.notes.NoteDetailsScreen
import com.example.lifedashboard.feature.notes.NotesScreen
import com.example.lifedashboard.feature.projects.ProjectDetailsScreen
import com.example.lifedashboard.feature.projects.ProjectEditorScreen
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
    val timerState by dashboardViewModel.timerState.collectAsState()
    val recentSearches by dashboardViewModel.recentSearches.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var backupBusy by remember { mutableStateOf(false) }
    var backupMessage by remember { mutableStateOf<String?>(null) }
    var backupError by remember { mutableStateOf<String?>(null) }
    var pendingFullBackup by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var showImportConfirmation by remember { mutableStateOf(false) }

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
    val dashboardState by dashboardViewModel.uiState.collectAsState()
    val noteOperationError by dashboardViewModel.noteOperationError.collectAsState()
    val habitOperationError by dashboardViewModel.habitOperationError.collectAsState()
    val taskOperationError by dashboardViewModel.taskOperationError.collectAsState()
    val projectOperationError by dashboardViewModel.projectOperationError.collectAsState()
    val goalOperationError by dashboardViewModel.goalOperationError.collectAsState()
    var showQuickAdd by remember { mutableStateOf(false) }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val searchScopeArg = backStackEntry?.arguments?.getString("scope")
    val isRootSearch = currentRoute?.startsWith(Destinations.SEARCH) == true && searchScopeArg == null

    val showBottomBar = currentRoute in setOf(Destinations.HOME, Destinations.MENU, Destinations.SETTINGS) || isRootSearch
    val selectedRoot = when {
        currentRoute == Destinations.HOME -> RootDestination.HOME
        currentRoute == Destinations.SETTINGS -> RootDestination.SETTINGS
        isRootSearch -> RootDestination.SEARCH
        else -> RootDestination.MENU
    }

    val useDarkTheme = when (appSettings.themeMode) {
        DashboardThemeMode.LIGHT -> false
        DashboardThemeMode.DARK -> true
        DashboardThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    fun openScopedSearch(scope: SearchScope) {
        navController.navigate("${Destinations.SEARCH}?scope=${scope.name}")
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
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Destinations.HOME) {
                    HomeScreen(
                        tasks = dashboardState.tasks,
                        activeProjectCount = dashboardState.projects.count { !it.isCompleted },
                        onToggleTask = dashboardViewModel::toggleTask,
                        onOpenTasks = { navController.navigate(Destinations.TASKS) },
                        onOpenProjects = { navController.navigate(Destinations.PROJECTS) },
                        onOpenNotes = { navController.navigate(Destinations.NOTES) },
                        onOpenHabits = { navController.navigate(Destinations.GOALS_AND_HABITS) },
                        showCompletedTasks = appSettings.showCompletedTasksOnHome,
                        activityEvents = dashboardState.activityEvents,
                        onOpenCalendar = { navController.navigate(Destinations.CALENDAR) },
                        homeBlocks = appSettings.homeBlocks,
                        onOpenSearch = { navigateRoot(navController, Destinations.SEARCH) },
                        onOpenSettings = { navigateRoot(navController, Destinations.SETTINGS) },
                        onUpdateHomeBlocks = dashboardViewModel::setHomeBlocks,
                        projects = dashboardState.projects,
                        habits = dashboardState.habits,
                        onOpenNotifications = { navController.navigate(Destinations.NOTIFICATIONS) },
                        onOpenProject = { projectId ->
                            navController.navigate("${Destinations.PROJECT_DETAILS}/$projectId")
                        },
                        timerState = timerState,
                        onToggleTimerPause = dashboardViewModel::toggleTimerPause,
                        onStopTimer = dashboardViewModel::stopTimer,
                        onOpenTimer = { navController.navigate(Destinations.TIMER) }
                    )
                }

                composable(Destinations.MENU) {
                    val today = LocalDate.now()
                    val weekCutoff = today.minusDays(6)
                    val todayTasksCount = dashboardState.tasks.count {
                        !it.isCompleted && (it.dueDate == today || (it.dueDate == null && it.dayCategory == TaskDayCategory.TODAY))
                    }
                    val completedWeek = dashboardState.activityEvents.count { event ->
                        event.type == "TASK_COMPLETION" &&
                            event.completed &&
                            !Instant.ofEpochMilli(event.occurredAtEpochMillis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                                .isBefore(weekCutoff)
                    }
                    val bestStreak = dashboardState.habits.maxOfOrNull {
                        HabitMetrics.currentStreak(it.completionDates)
                    } ?: 0
                    MenuScreen(
                        onNavigate = { route -> navController.navigate(route) },
                        onSettingsClick = { navigateRoot(navController, Destinations.SETTINGS) },
                        noteCount = dashboardState.notes.size,
                        goalCount = dashboardState.goals.size,
                        completedGoalCount = dashboardState.goals.count { it.isCompleted },
                        activeProjectCount = dashboardState.projects.count { !it.isCompleted },
                        todayTaskCount = todayTasksCount,
                        completedTasksThisWeek = completedWeek,
                        bestHabitStreak = bestStreak,
                        habitCount = dashboardState.habits.size,
                        onSearchClick = { navigateRoot(navController, Destinations.SEARCH) }
                    )
                }

                composable(Destinations.SETTINGS) {
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
                        onBack = null,
                        onThemeModeChange = dashboardViewModel::setThemeMode,
                        onNotificationsEnabledChange = onGlobalNotificationsChange,
                        onShowCompletedTasksChange = dashboardViewModel::setShowCompletedTasksOnHome,
                        onOpenNotificationSettings = ::openNotificationSettings,
                        onOpenExactAlarmSettings = ::openExactAlarmSettings,
                        onOpenProfile = { navController.navigate(Destinations.PROFILE) },
                        onOpenBackups = { navController.navigate(Destinations.BACKUPS) }
                    )
                }

                composable(Destinations.BACKUPS) {
                    BackupsScreen(
                        backupBusy = backupBusy,
                        backupMessage = backupMessage,
                        backupError = backupError,
                        onBack = { navController.popBackStack() },
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

                composable(Destinations.PROFILE) {
                    val bestStreak = dashboardState.habits.maxOfOrNull {
                        HabitMetrics.currentStreak(it.completionDates)
                    } ?: 0
                    ProfileScreen(
                        settings = appSettings,
                        projectCount = dashboardState.projects.count { !it.isCompleted },
                        noteCount = dashboardState.notes.size,
                        bestHabitStreak = bestStreak,
                        onBack = { navController.popBackStack() },
                        onSaveProfile = dashboardViewModel::setProfile
                    )
                }

                composable(Destinations.NOTIFICATIONS) {
                    NotificationsScreen(
                        state = dashboardState,
                        onBack = { navController.popBackStack() },
                        onOpenItem = { type, id ->
                            when (type) {
                                NotificationTargetType.TASK -> navController.navigate("${Destinations.TASK_DETAILS}/$id")
                                NotificationTargetType.HABIT -> navController.navigate("${Destinations.HABIT_DETAILS}/$id")
                                NotificationTargetType.PROJECT -> navController.navigate("${Destinations.PROJECT_DETAILS}/$id")
                                NotificationTargetType.GOAL -> navController.navigate("${Destinations.GOAL_DETAILS}/$id")
                            }
                        }
                    )
                }

                composable(
                    route = "${Destinations.SEARCH}?scope={scope}",
                    arguments = listOf(navArgument("scope") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    })
                ) { entry ->
                    val rawScope = entry.arguments?.getString("scope")
                    val initialScope = rawScope?.let {
                        runCatching { SearchScope.valueOf(it) }.getOrNull()
                    } ?: SearchScope.ALL
                    SearchScreen(
                        state = dashboardState,
                        onBack = if (rawScope != null) ({ navController.popBackStack() }) else null,
                        onResultClick = { item ->
                            when (item.scope) {
                                SearchScope.TASKS -> navController.navigate("${Destinations.TASK_DETAILS}/${item.entityId}")
                                SearchScope.PROJECTS -> navController.navigate("${Destinations.PROJECT_DETAILS}/${item.entityId}")
                                SearchScope.NOTES -> navController.navigate("${Destinations.NOTE_DETAILS}/${item.entityId}")
                                SearchScope.GOALS -> navController.navigate("${Destinations.GOAL_DETAILS}/${item.entityId}")
                                SearchScope.HABITS -> navController.navigate("${Destinations.HABIT_DETAILS}/${item.entityId}")
                                SearchScope.ALL -> Unit
                            }
                        },
                        recentQueries = recentSearches,
                        onSaveRecentQuery = dashboardViewModel::addRecentSearch,
                        onRemoveRecentQuery = dashboardViewModel::removeRecentSearch,
                        initialScope = initialScope
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
                    val projectId = entry.arguments?.getString("projectId")
                    val dueDate = DateInput.parseIso(entry.arguments?.getString("dueDate"))
                    val existing = taskId?.let { id -> dashboardState.tasks.firstOrNull { it.id == id } }
                    TaskCreateScreen(
                        projects = dashboardState.projects,
                        onBack = { navController.popBackStack() },
                        onCreate = dashboardViewModel::addTask,
                        initialTask = existing,
                        initialProjectId = projectId,
                        initialDueDate = dueDate,
                        onUpdate = dashboardViewModel::updateTask
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
                    val dueDate = DateInput.parseIso(entry.arguments?.getString("dueDate"))
                    val project = projectId?.let { id -> dashboardState.projects.firstOrNull { it.id == id } }
                    ProjectEditorScreen(
                        initialProject = project,
                        onBack = { navController.popBackStack() },
                        onSave = dashboardViewModel::saveProject,
                        operationError = projectOperationError,
                        initialDeadlineDate = dueDate
                    )
                }

                composable(
                    route = "${Destinations.HABIT_EDITOR}?habitId={habitId}",
                    arguments = listOf(navArgument("habitId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    })
                ) { entry ->
                    val habitId = entry.arguments?.getString("habitId")
                    val habit = habitId?.let { id -> dashboardState.habits.firstOrNull { it.id == id } }
                    HabitEditorScreen(
                        initialHabit = habit,
                        onBack = { navController.popBackStack() },
                        onSave = saveHabit,
                        operationError = habitOperationError
                    )
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
                    val projectId = entry.arguments?.getString("projectId")
                    val dueDate = DateInput.parseIso(entry.arguments?.getString("dueDate"))
                    val goal = goalId?.let { id -> dashboardState.goals.firstOrNull { it.id == id } }
                    GoalEditorScreen(
                        initialGoal = goal,
                        projects = dashboardState.projects,
                        onBack = { navController.popBackStack() },
                        onSave = dashboardViewModel::saveGoal,
                        operationError = goalOperationError,
                        initialProjectId = projectId,
                        initialDueDate = dueDate
                    )
                }

                composable(
                    route = "${Destinations.PROJECTS}?create={create}",
                    arguments = listOf(navArgument("create") { type = NavType.BoolType; defaultValue = false })
                ) { entry ->
                    ProjectsScreen(
                        projects = dashboardState.projects,
                        tasks = dashboardState.tasks,
                        onOpenProject = { projectId ->
                            navController.navigate("${Destinations.PROJECT_DETAILS}/$projectId")
                        },
                        onToggleTask = dashboardViewModel::toggleTask,
                        onSaveProject = dashboardViewModel::saveProject,
                        operationError = projectOperationError,
                        startWithCreate = entry.arguments?.getBoolean("create") == true,
                        onBack = { navController.popBackStack() },
                        goals = dashboardState.goals,
                        notes = dashboardState.notes,
                        onCreateProject = { navController.navigate(Destinations.PROJECT_EDITOR) },
                        onOpenSearch = { openScopedSearch(SearchScope.PROJECTS) }
                    )
                }

                composable(
                    route = "${Destinations.TASKS}?projectId={projectId}&taskId={taskId}&create={create}",
                    arguments = listOf(
                        navArgument("projectId") { type = NavType.StringType; nullable = true; defaultValue = null },
                        navArgument("taskId") { type = NavType.StringType; nullable = true; defaultValue = null },
                        navArgument("create") { type = NavType.BoolType; defaultValue = false }
                    )
                ) { entry ->
                    val projectId = entry.arguments?.getString("projectId")
                    val taskId = entry.arguments?.getString("taskId")
                    val createTask = entry.arguments?.getBoolean("create") == true
                    TasksScreen(
                        tasks = dashboardState.tasks,
                        projects = dashboardState.projects,
                        initialProjectId = projectId,
                        onToggleTask = dashboardViewModel::toggleTask,
                        onAddTask = { title, priority, pid, due, rec, days, remEn, remTime ->
                            dashboardViewModel.addTask(title, priority, pid, due, rec, days, remEn, remTime)
                        },
                        onOpenProject = { id ->
                            navController.navigate("${Destinations.PROJECT_DETAILS}/$id")
                        },
                        onUpdateTask = dashboardViewModel::updateTask,
                        onDeleteTask = dashboardViewModel::deleteTask,
                        operationError = taskOperationError,
                        initialTaskId = taskId,
                        startWithCreate = createTask,
                        onOpenTaskDetails = { id ->
                            navController.navigate("${Destinations.TASK_DETAILS}/$id")
                        },
                        onBack = { navController.popBackStack() },
                        timerState = timerState,
                        onToggleTimerPause = dashboardViewModel::toggleTimerPause,
                        onStopTimer = dashboardViewModel::stopTimer,
                        onOpenTimer = { navController.navigate(Destinations.TIMER) },
                        onCreateTask = {
                            val route = if (projectId != null) {
                                "${Destinations.TASK_CREATE}?projectId=$projectId"
                            } else {
                                Destinations.TASK_CREATE
                            }
                            navController.navigate(route)
                        },
                        onOpenSearch = { openScopedSearch(SearchScope.TASKS) }
                    )
                }

                composable(
                    route = "${Destinations.TASK_DETAILS}/{taskId}",
                    arguments = listOf(navArgument("taskId") { type = NavType.StringType })
                ) { entry ->
                    val taskId = entry.arguments?.getString("taskId").orEmpty()
                    val task = dashboardState.tasks.firstOrNull { it.id == taskId }
                    if (task == null) {
                        PlaceholderScreen("Задача не найдена", "Возможно, она была удалена или пропущена.") {
                            navController.popBackStack()
                        }
                    } else {
                        TaskDetailsScreen(
                            task = task,
                            allTasks = dashboardState.tasks,
                            projects = dashboardState.projects,
                            goals = dashboardState.goals,
                            activityEvents = dashboardState.activityEvents,
                            onBack = { navController.popBackStack() },
                            onToggleTask = dashboardViewModel::toggleTask,
                            onUpdateTask = dashboardViewModel::updateTask,
                            onDeleteTask = dashboardViewModel::deleteTask,
                            onOpenProject = { projectId ->
                                navController.navigate("${Destinations.PROJECT_DETAILS}/$projectId")
                            },
                            onOpenGoal = { goalId ->
                                navController.navigate("${Destinations.GOAL_DETAILS}/$goalId")
                            },
                            onOpenTask = { otherId ->
                                navController.navigate("${Destinations.TASK_DETAILS}/$otherId")
                            },
                            operationError = taskOperationError,
                            timerState = timerState,
                            onStartTimer = { targetTask, projectName ->
                                dashboardViewModel.startTimer(targetTask, projectName)
                                navController.navigate(Destinations.TIMER)
                            },
                            subtasks = dashboardState.subtasks,
                            onAddSubtask = dashboardViewModel::addSubtask,
                            onToggleSubtask = dashboardViewModel::toggleSubtask,
                            onDeleteSubtask = dashboardViewModel::deleteSubtask,
                            onEditTask = {
                                navController.navigate("${Destinations.TASK_CREATE}?taskId=${task.id}")
                            }
                        )
                    }
                }

                composable(Destinations.TIMER) {
                    TimerScreen(
                        timerState = timerState,
                        onBack = { navController.popBackStack() },
                        onTogglePause = dashboardViewModel::toggleTimerPause,
                        onStop = {
                            dashboardViewModel.stopTimer()
                            navController.popBackStack()
                        }
                    )
                }

                composable(Destinations.NOTES) {
                    NotesScreen(
                        notes = dashboardState.notes,
                        onBack = { navController.popBackStack() },
                        onOpenNote = { noteId ->
                            navController.navigate("${Destinations.NOTE_DETAILS}/$noteId")
                        },
                        onCreateNote = {
                            navController.navigate("${Destinations.NOTE_DETAILS}/new")
                        },
                        onTogglePinned = dashboardViewModel::toggleNotePinned,
                        operationError = noteOperationError,
                        onDeleteNote = { id -> dashboardViewModel.deleteNote(id) },
                        attachments = dashboardState.noteAttachments,
                        projects = dashboardState.projects,
                        onOpenSearch = { openScopedSearch(SearchScope.NOTES) }
                    )
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
                    val note = if (isNewNote) null else dashboardState.notes.firstOrNull { it.id == noteId }

                    if (!isNewNote && note == null) {
                        PlaceholderScreen("Заметка не найдена", "Возможно, она была удалена.") {
                            navController.popBackStack()
                        }
                    } else {
                        NoteDetailsScreen(
                            note = note,
                            onBack = { navController.popBackStack() },
                            onSave = { updatedNote ->
                                dashboardViewModel.saveNote(updatedNote) { navController.popBackStack() }
                            },
                            onDelete = { deletedNoteId ->
                                dashboardViewModel.deleteNote(deletedNoteId) { navController.popBackStack() }
                            },
                            operationError = noteOperationError,
                            projects = dashboardState.projects,
                            initialProjectId = initialProjectId,
                            attachments = dashboardState.noteAttachments,
                            onAddAttachment = { targetNoteId, uri ->
                                dashboardViewModel.addNoteAttachment(context, targetNoteId, uri)
                            },
                            onDeleteAttachment = dashboardViewModel::deleteNoteAttachment
                        )
                    }
                }

                composable(Destinations.ANALYTICS) {
                    AnalyticsScreen(
                        projects = dashboardState.projects,
                        tasks = dashboardState.tasks,
                        activityEvents = dashboardState.activityEvents,
                        onBack = { navController.popBackStack() },
                        habits = dashboardState.habits,
                        goals = dashboardState.goals,
                        timerState = timerState
                    )
                }

                composable(Destinations.CALENDAR) {
                    CalendarScreen(
                        tasks = dashboardState.tasks,
                        projects = dashboardState.projects,
                        goals = dashboardState.goals,
                        habits = dashboardState.habits,
                        activityEvents = dashboardState.activityEvents,
                        onBack = { navController.popBackStack() },
                        onEventClick = { event ->
                            when (event.type) {
                                CalendarEventType.TASK -> navController.navigate("${Destinations.TASK_DETAILS}/${event.relatedId}")
                                CalendarEventType.PROJECT -> navController.navigate("${Destinations.PROJECT_DETAILS}/${event.relatedId}")
                                CalendarEventType.GOAL -> navController.navigate("${Destinations.GOAL_DETAILS}/${event.relatedId}")
                                CalendarEventType.HABIT -> navController.navigate("${Destinations.HABIT_DETAILS}/${event.relatedId}")
                                CalendarEventType.ACTIVITY -> navController.navigate(Destinations.ANALYTICS)
                            }
                        },
                        onToggleTask = dashboardViewModel::toggleTask,
                        onAddTask = { title, priority, pid, due, rec, days, remEn, remTime ->
                            dashboardViewModel.addTask(title, priority, pid, due, rec, days, remEn, remTime)
                        },
                        onUpdateTask = dashboardViewModel::updateTask,
                        onOpenTaskDetails = { id ->
                            navController.navigate("${Destinations.TASK_DETAILS}/$id")
                        },
                        onSaveProject = dashboardViewModel::saveProject,
                        onSaveGoal = dashboardViewModel::saveGoal,
                        onToggleHabitOnDate = dashboardViewModel::toggleHabitOnDate,
                        operationError = taskOperationError ?: projectOperationError ?: goalOperationError ?: habitOperationError,
                        onOpenCreateTask = { date ->
                            navController.navigate("${Destinations.TASK_CREATE}?dueDate=$date")
                        },
                        onOpenEditTask = { taskId ->
                            navController.navigate("${Destinations.TASK_CREATE}?taskId=$taskId")
                        },
                        onOpenCreateProject = { date ->
                            navController.navigate("${Destinations.PROJECT_EDITOR}?dueDate=$date")
                        },
                        onOpenCreateGoal = { date ->
                            navController.navigate("${Destinations.GOAL_EDITOR}?dueDate=$date")
                        }
                    )
                }

                composable(
                    route = "${Destinations.GOALS_AND_HABITS}?tab={tab}",
                    arguments = listOf(navArgument("tab") { type = NavType.StringType; nullable = true; defaultValue = null })
                ) { entry ->
                    val tabArg = entry.arguments?.getString("tab")
                    val initialTab = tabArg?.let { runCatching { GoalsHabitsTab.valueOf(it) }.getOrNull() } ?: GoalsHabitsTab.ALL
                    GoalsAndHabitsScreen(
                        habits = dashboardState.habits,
                        goals = dashboardState.goals,
                        onBack = { navController.popBackStack() },
                        onOpenHabit = { id -> navController.navigate("${Destinations.HABIT_DETAILS}/$id") },
                        onToggleHabitToday = dashboardViewModel::toggleHabitToday,
                        onOpenGoal = { id -> navController.navigate("${Destinations.GOAL_DETAILS}/$id") },
                        onCreateHabit = { navController.navigate(Destinations.HABIT_EDITOR) },
                        onCreateGoal = { navController.navigate(Destinations.GOAL_EDITOR) },
                        onOpenSearch = { scope -> openScopedSearch(scope) },
                        initialTab = initialTab,
                        operationError = habitOperationError ?: goalOperationError
                    )
                }

                composable(
                    route = "${Destinations.HABITS}?create={create}",
                    arguments = listOf(navArgument("create") { type = NavType.BoolType; defaultValue = false })
                ) {
                    GoalsAndHabitsScreen(
                        habits = dashboardState.habits,
                        goals = dashboardState.goals,
                        onBack = { navController.popBackStack() },
                        onOpenHabit = { id -> navController.navigate("${Destinations.HABIT_DETAILS}/$id") },
                        onToggleHabitToday = dashboardViewModel::toggleHabitToday,
                        onOpenGoal = { id -> navController.navigate("${Destinations.GOAL_DETAILS}/$id") },
                        onCreateHabit = { navController.navigate(Destinations.HABIT_EDITOR) },
                        onCreateGoal = { navController.navigate(Destinations.GOAL_EDITOR) },
                        onOpenSearch = { scope -> openScopedSearch(scope) },
                        initialTab = GoalsHabitsTab.HABITS,
                        operationError = habitOperationError
                    )
                }

                composable(
                    route = "${Destinations.HABIT_DETAILS}/{habitId}",
                    arguments = listOf(navArgument("habitId") { type = NavType.StringType })
                ) { entry ->
                    val habitId = entry.arguments?.getString("habitId").orEmpty()
                    val habit = dashboardState.habits.firstOrNull { it.id == habitId }
                    if (habit == null) {
                        PlaceholderScreen("Привычка не найдена", "Возможно, она была удалена.") {
                            navController.popBackStack()
                        }
                    } else {
                        HabitDetailsScreen(
                            habit = habit,
                            onBack = { navController.popBackStack() },
                            onToggleToday = { dashboardViewModel.toggleHabitToday(habitId) },
                            onSaveHabit = saveHabit,
                            onDeleteHabit = { id, callback ->
                                dashboardViewModel.deleteHabit(id) {
                                    callback?.invoke()
                                    navController.popBackStack()
                                }
                            },
                            operationError = habitOperationError,
                            onToggleDate = dashboardViewModel::toggleHabitOnDate,
                            onEditHabit = { navController.navigate("${Destinations.HABIT_EDITOR}?habitId=$habitId") }
                        )
                    }
                }

                composable(
                    route = "${Destinations.GOALS}?goalId={goalId}&create={create}",
                    arguments = listOf(
                        navArgument("goalId") { type = NavType.StringType; nullable = true; defaultValue = null },
                        navArgument("create") { type = NavType.BoolType; defaultValue = false }
                    )
                ) {
                    GoalsAndHabitsScreen(
                        habits = dashboardState.habits,
                        goals = dashboardState.goals,
                        onBack = { navController.popBackStack() },
                        onOpenHabit = { id -> navController.navigate("${Destinations.HABIT_DETAILS}/$id") },
                        onToggleHabitToday = dashboardViewModel::toggleHabitToday,
                        onOpenGoal = { id -> navController.navigate("${Destinations.GOAL_DETAILS}/$id") },
                        onCreateHabit = { navController.navigate(Destinations.HABIT_EDITOR) },
                        onCreateGoal = { navController.navigate(Destinations.GOAL_EDITOR) },
                        onOpenSearch = { scope -> openScopedSearch(scope) },
                        initialTab = GoalsHabitsTab.GOALS,
                        operationError = goalOperationError
                    )
                }

                composable(
                    route = "${Destinations.GOAL_DETAILS}/{goalId}",
                    arguments = listOf(navArgument("goalId") { type = NavType.StringType })
                ) { entry ->
                    val goalId = entry.arguments?.getString("goalId").orEmpty()
                    val goal = dashboardState.goals.firstOrNull { it.id == goalId }
                    if (goal == null) {
                        PlaceholderScreen("Цель не найдена", "Возможно, она была удалена.") {
                            navController.popBackStack()
                        }
                    } else {
                        GoalDetailsScreen(
                            goal = goal,
                            projects = dashboardState.projects,
                            onBack = { navController.popBackStack() },
                            onSaveGoal = dashboardViewModel::saveGoal,
                            onDeleteGoal = dashboardViewModel::deleteGoal,
                            onChangeProgress = dashboardViewModel::changeGoalProgress,
                            onOpenProject = { projectId ->
                                navController.navigate("${Destinations.PROJECT_DETAILS}/$projectId")
                            },
                            operationError = goalOperationError,
                            onEditGoal = { navController.navigate("${Destinations.GOAL_EDITOR}?goalId=$goalId") }
                        )
                    }
                }

                composable(
                    route = "${Destinations.PROJECT_DETAILS}/{projectId}",
                    arguments = listOf(navArgument("projectId") { type = NavType.StringType })
                ) { entry ->
                    val projectId = entry.arguments?.getString("projectId").orEmpty()
                    val project = dashboardState.projects.firstOrNull { it.id == projectId }
                    if (project == null) {
                        PlaceholderScreen("Проект не найден", "Возможно, он был удалён.") {
                            navController.popBackStack()
                        }
                    } else {
                        ProjectDetailsScreen(
                            project = project,
                            tasks = dashboardState.tasks.filter { it.projectId == projectId },
                            onBack = { navController.popBackStack() },
                            onToggleTask = dashboardViewModel::toggleTask,
                            onOpenAllTasks = {
                                navController.navigate("${Destinations.TASKS}?projectId=$projectId")
                            },
                            onToggleStage = dashboardViewModel::toggleProjectStage,
                            onSaveProject = dashboardViewModel::saveProject,
                            onDeleteProject = dashboardViewModel::deleteProject,
                            operationError = projectOperationError,
                            activityEvents = dashboardState.activityEvents,
                            onAddTask = { title, priority, pid, due, rec, days, remEn, remTime ->
                                dashboardViewModel.addTask(title, priority, pid, due, rec, days, remEn, remTime)
                            },
                            onOpenTaskDetails = { taskId ->
                                navController.navigate("${Destinations.TASK_DETAILS}/$taskId")
                            },
                            goals = dashboardState.goals,
                            notes = dashboardState.notes,
                            onOpenGoal = { goalId ->
                                navController.navigate("${Destinations.GOAL_DETAILS}/$goalId")
                            },
                            onOpenNote = { noteId ->
                                navController.navigate("${Destinations.NOTE_DETAILS}/$noteId")
                            },
                            onAddGoal = {
                                navController.navigate("${Destinations.GOAL_EDITOR}?projectId=$projectId")
                            },
                            onAddNote = {
                                navController.navigate("${Destinations.NOTE_DETAILS}/new?projectId=$projectId")
                            },
                            onEditProject = {
                                navController.navigate("${Destinations.PROJECT_EDITOR}?projectId=$projectId")
                            },
                            onOpenAddTask = {
                                navController.navigate("${Destinations.TASK_CREATE}?projectId=$projectId")
                            }
                        )
                    }
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
                    navController.navigate(destination)
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
                            "Настройки DataStore будут заменены только при импорте полной резервной копии. " +
                            "Перед продолжением рекомендуется создать текущую резервную копию."
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

private fun navigateRoot(navController: NavHostController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
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