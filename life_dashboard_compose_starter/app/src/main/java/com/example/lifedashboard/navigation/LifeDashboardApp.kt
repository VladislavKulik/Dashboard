package com.example.lifedashboard.navigation

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.navArgument
import com.example.lifedashboard.presentation.DashboardViewModel
import com.example.lifedashboard.data.model.DashboardHabit
import com.example.lifedashboard.data.model.DashboardThemeMode
import com.example.lifedashboard.reminders.AndroidHabitReminderScheduler
import com.example.lifedashboard.reminders.AndroidTaskReminderScheduler
import com.example.lifedashboard.data.local.RoomDashboardPersistence
import com.example.lifedashboard.data.local.DataStoreSettingsRepository
import com.example.lifedashboard.data.local.dashboardSettingsDataStore
import com.example.lifedashboard.feature.common.PlaceholderScreen
import com.example.lifedashboard.feature.analytics.AnalyticsScreen
import com.example.lifedashboard.feature.calendar.CalendarScreen
import com.example.lifedashboard.feature.calendar.CalendarEventType
import com.example.lifedashboard.feature.goals.GoalsScreen
import com.example.lifedashboard.feature.habits.HabitsScreen
import com.example.lifedashboard.feature.habits.HabitDetailsScreen
import com.example.lifedashboard.feature.home.HomeScreen
import com.example.lifedashboard.feature.menu.MenuScreen
import com.example.lifedashboard.feature.notes.NotesScreen
import com.example.lifedashboard.feature.notes.NoteDetailsScreen
import com.example.lifedashboard.feature.projects.ProjectDetailsScreen
import com.example.lifedashboard.feature.projects.ProjectsScreen
import com.example.lifedashboard.feature.tasks.TasksScreen
import com.example.lifedashboard.feature.settings.SettingsScreen
import com.example.lifedashboard.ui.components.AppBottomBar
import com.example.lifedashboard.ui.components.QuickAddSheet
import com.example.lifedashboard.ui.components.QuickAddType
import com.example.lifedashboard.ui.components.RootDestination
import com.example.lifedashboard.ui.theme.AppColors
import com.example.lifedashboard.ui.theme.LifeDashboardTheme

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
            // The inexact fallback remains scheduled if this Settings screen is unavailable.
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
            // Permission can still be changed later in Android app settings.
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

    val selectedRoot = when {
        currentRoute == Destinations.HOME -> RootDestination.HOME
        currentRoute == Destinations.PROJECTS || currentRoute?.startsWith(Destinations.PROJECT_DETAILS) == true -> RootDestination.PROJECTS
        currentRoute?.startsWith(Destinations.TASKS) == true -> RootDestination.HOME
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
            AppBottomBar(
                selected = selectedRoot,
                onHomeClick = { navigateRoot(navController, Destinations.HOME) },
                onProjectsClick = { navigateRoot(navController, Destinations.PROJECTS) },
                onAddClick = { showQuickAdd = true },
                onMenuClick = { navigateRoot(navController, Destinations.MENU) }
            )
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
                    onOpenProjects = { navigateRoot(navController, Destinations.PROJECTS) },
                    onOpenNotes = { navController.navigate(Destinations.NOTES) },
                    onOpenHabits = { navController.navigate(Destinations.HABITS) },
                    showCompletedTasks = appSettings.showCompletedTasksOnHome,
                    activityEvents = dashboardState.activityEvents,
                    onOpenCalendar = { navController.navigate(Destinations.CALENDAR) }
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
                    startWithCreate = entry.arguments?.getBoolean("create") == true
                )
            }

            composable(Destinations.MENU) {
                MenuScreen(
                    onNavigate = { route -> navController.navigate(route) },
                    onSettingsClick = { navController.navigate(Destinations.SETTINGS) },
                    noteCount = dashboardState.notes.size,
                    goalCount = dashboardState.goals.size,
                    completedGoalCount = dashboardState.goals.count { it.isCompleted }
                )
            }

            composable(
                route = "${Destinations.TASKS}?projectId={projectId}&taskId={taskId}&create={create}",
                arguments = listOf(
                    navArgument("projectId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("taskId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
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
                    onAddTask = dashboardViewModel::addTask,
                    onOpenProject = { id ->
                        navController.navigate("${Destinations.PROJECT_DETAILS}/$id")
                    },
                    onUpdateTask = dashboardViewModel::updateTask,
                    onDeleteTask = dashboardViewModel::deleteTask,
                    operationError = taskOperationError,
                    initialTaskId = taskId,
                    startWithCreate = createTask
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
                    operationError = noteOperationError
                )
            }

            composable(
                route = "${Destinations.NOTE_DETAILS}/{noteId}",
                arguments = listOf(navArgument("noteId") { type = NavType.StringType })
            ) { entry ->
                val noteId = entry.arguments?.getString("noteId").orEmpty()
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
                        operationError = noteOperationError
                    )
                }
            }
            composable(Destinations.ANALYTICS) {
                AnalyticsScreen(
                    projects = dashboardState.projects,
                    tasks = dashboardState.tasks,
                    activityEvents = dashboardState.activityEvents,
                    onBack = { navController.popBackStack() }
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
                            CalendarEventType.TASK -> {
                                val route = if (event.projectId != null) {
                                    "${Destinations.TASKS}?projectId=${event.projectId}&taskId=${event.relatedId}"
                                } else {
                                    "${Destinations.TASKS}?taskId=${event.relatedId}"
                                }
                                navController.navigate(route)
                            }
                            CalendarEventType.PROJECT -> navController.navigate("${Destinations.PROJECT_DETAILS}/${event.relatedId}")
                            CalendarEventType.GOAL -> navController.navigate("${Destinations.GOALS}?goalId=${event.relatedId}")
                            CalendarEventType.HABIT -> navController.navigate("${Destinations.HABIT_DETAILS}/${event.relatedId}")
                            CalendarEventType.ACTIVITY -> navController.navigate(Destinations.ANALYTICS)
                        }
                    }
                )
            }
            composable(
                route = "${Destinations.HABITS}?create={create}",
                arguments = listOf(navArgument("create") { type = NavType.BoolType; defaultValue = false })
            ) { entry ->
                HabitsScreen(
                    habits = dashboardState.habits,
                    onBack = { navController.popBackStack() },
                    onOpenHabit = { habitId ->
                        navController.navigate("${Destinations.HABIT_DETAILS}/$habitId")
                    },
                    onSaveHabit = saveHabit,
                    onToggleToday = dashboardViewModel::toggleHabitToday,
                    operationError = habitOperationError,
                    startWithCreate = entry.arguments?.getBoolean("create") == true
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
                        onDeleteHabit = { id, callback -> dashboardViewModel.deleteHabit(id) { callback?.invoke(); navController.popBackStack() } },
                        operationError = habitOperationError
                    )
                }
            }
            composable(
                route = "${Destinations.GOALS}?goalId={goalId}&create={create}",
                arguments = listOf(navArgument("goalId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }, navArgument("create") { type = NavType.BoolType; defaultValue = false })
            ) { entry ->
                val goalId = entry.arguments?.getString("goalId")
                GoalsScreen(
                    goals = dashboardState.goals,
                    projects = dashboardState.projects,
                    onBack = { navController.popBackStack() },
                    onSaveGoal = dashboardViewModel::saveGoal,
                    onDeleteGoal = dashboardViewModel::deleteGoal,
                    onChangeProgress = dashboardViewModel::changeGoalProgress,
                    operationError = goalOperationError,
                    initialGoalId = goalId,
                    startWithCreate = entry.arguments?.getBoolean("create") == true
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
                    onBack = { navController.popBackStack() },
                    onThemeModeChange = dashboardViewModel::setThemeMode,
                    onNotificationsEnabledChange = onGlobalNotificationsChange,
                    onShowCompletedTasksChange = dashboardViewModel::setShowCompletedTasksOnHome,
                    onOpenNotificationSettings = ::openNotificationSettings,
                    onOpenExactAlarmSettings = ::openExactAlarmSettings
                )
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
                        operationError = projectOperationError
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
                    QuickAddType.TASK -> "${Destinations.TASKS}?create=true"
                    QuickAddType.PROJECT -> "${Destinations.PROJECTS}?create=true"
                    QuickAddType.NOTE -> "${Destinations.NOTE_DETAILS}/new"
                    QuickAddType.HABIT -> "${Destinations.HABITS}?create=true"
                    QuickAddType.GOAL -> "${Destinations.GOALS}?create=true"
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
                    "Импорт заменит все текущие задачи, проекты, этапы, заметки, привычки, цели и историю активности. " +
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
