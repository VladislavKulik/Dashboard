package com.example.lifedashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lifedashboard.data.DateKeys
import com.example.lifedashboard.data.model.DashboardGoal
import com.example.lifedashboard.data.model.DashboardHabit
import com.example.lifedashboard.data.model.DashboardNote
import com.example.lifedashboard.data.model.DashboardSettings
import com.example.lifedashboard.data.model.DashboardThemeMode
import com.example.lifedashboard.data.model.HabitColor
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.data.model.DashboardTask
import com.example.lifedashboard.data.model.TaskPriority
import com.example.lifedashboard.data.model.TaskRecurrence
import java.time.LocalDate
import com.example.lifedashboard.data.repository.ActivityRepository
import com.example.lifedashboard.data.repository.BackupRepository
import com.example.lifedashboard.data.repository.BackupRestoreResult
import com.example.lifedashboard.data.repository.DashboardRepository
import com.example.lifedashboard.data.repository.GoalsRepository
import com.example.lifedashboard.data.repository.HabitsRepository
import com.example.lifedashboard.data.repository.NotesRepository
import com.example.lifedashboard.data.repository.ProjectsRepository
import com.example.lifedashboard.data.repository.SettingsRepository
import com.example.lifedashboard.data.repository.TaskRepository
import com.example.lifedashboard.reminders.HabitReminderScheduler
import com.example.lifedashboard.reminders.TaskReminderScheduler
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Coordinates feature commands and assembles the dashboard read model from Room-backed streams. */
class DashboardViewModel(
    private val dashboardRepository: DashboardRepository,
    private val taskRepository: TaskRepository,
    private val notesRepository: NotesRepository,
    private val habitsRepository: HabitsRepository,
    private val projectsRepository: ProjectsRepository,
    private val activityRepository: ActivityRepository,
    private val habitReminderScheduler: HabitReminderScheduler? = null,
    private val taskReminderScheduler: TaskReminderScheduler? = null,
    private val settingsRepository: SettingsRepository? = null,
    private val backupRepository: BackupRepository? = null,
    private val goalsRepository: GoalsRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _initializationError = MutableStateFlow<String?>(null)
    val initializationError: StateFlow<String?> = _initializationError.asStateFlow()

    private val _taskOperationError = MutableStateFlow<String?>(null)
    val taskOperationError: StateFlow<String?> = _taskOperationError.asStateFlow()
    private val _noteOperationError = MutableStateFlow<String?>(null)
    val noteOperationError: StateFlow<String?> = _noteOperationError.asStateFlow()
    private val _habitOperationError = MutableStateFlow<String?>(null)
    val habitOperationError: StateFlow<String?> = _habitOperationError.asStateFlow()
    private val _projectOperationError = MutableStateFlow<String?>(null)
    val projectOperationError: StateFlow<String?> = _projectOperationError.asStateFlow()
    private val _goalOperationError = MutableStateFlow<String?>(null)
    val goalOperationError: StateFlow<String?> = _goalOperationError.asStateFlow()

    private val _appSettings = MutableStateFlow(DashboardSettings())
    val appSettings: StateFlow<DashboardSettings> = _appSettings.asStateFlow()
    private val _settingsOperationError = MutableStateFlow<String?>(null)
    val settingsOperationError: StateFlow<String?> = _settingsOperationError.asStateFlow()

    private val taskMutationMutex = Mutex()
    private val habitMutationMutex = Mutex()
    private val projectMutationMutex = Mutex()
    private val goalMutationMutex = Mutex()

    init {
        viewModelScope.launch {
            try {
                dashboardRepository.initializeIfNeeded()
                val coreState = combine(
                    taskRepository.observeTasks(),
                    projectsRepository.observeProjects(),
                    notesRepository.observeNotes(),
                    habitsRepository.observeHabits(),
                    activityRepository.observeEvents()
                ) { tasks, projects, notes, habits, events ->
                    DashboardUiState(
                        tasks = tasks,
                        projects = projects,
                        notes = notes,
                        habits = habits,
                        activityEvents = events
                    )
                }
                combine(coreState, goalsRepository?.observeGoals() ?: flowOf<List<DashboardGoal>>(emptyList())) { state, goals ->
                    state.copy(goals = goals)
                }.collect { state ->
                    _uiState.value = state
                    _isReady.value = true
                    syncTaskReminders(state.tasks, _appSettings.value.notificationsEnabled)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _initializationError.value = "Не удалось загрузить данные. Проверьте локальное хранилище."
            }
        }

        viewModelScope.launch {
            try {
                if (settingsRepository == null) {
                    // Compatibility for isolated tests/factories that do not provide DataStore.
                    habitsRepository.getEnabledHabits().forEach { habit ->
                        if (_appSettings.value.notificationsEnabled) habitReminderScheduler?.schedule(habit)
                    }
                    _isReady.first { it }
                    syncTaskReminders(_uiState.value.tasks, _appSettings.value.notificationsEnabled)
                } else {
                    var isFirstEmission = true
                    var previousNotificationsEnabled = _appSettings.value.notificationsEnabled
                    settingsRepository.observeSettings().collect { settings ->
                        _appSettings.value = settings
                        if (isFirstEmission || settings.notificationsEnabled != previousNotificationsEnabled) {
                            syncHabitReminders(settings.notificationsEnabled)
                            syncTaskReminders(_uiState.value.tasks, settings.notificationsEnabled)
                        }
                        isFirstEmission = false
                        previousNotificationsEnabled = settings.notificationsEnabled
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _settingsOperationError.value = "Не удалось загрузить настройки приложения."
            }
        }
    }

    private fun syncTaskReminders(tasks: List<DashboardTask>, enabled: Boolean) {
        val scheduler = taskReminderScheduler ?: return
        if (enabled) tasks.forEach(scheduler::schedule) else tasks.forEach { scheduler.cancel(it.id) }
    }

    private suspend fun syncHabitReminders(enabled: Boolean) {
        val scheduler = habitReminderScheduler ?: return
        // Wait until Room seeding/initial Flow collection has completed before reading habits.
        _isReady.first { it }
        val enabledHabits = habitsRepository.getEnabledHabits()
        if (enabled) enabledHabits.forEach(scheduler::schedule)
        else enabledHabits.forEach { scheduler.cancel(it.id) }
    }


    /** Generates either a portable data export or a full backup including DataStore settings. */
    suspend fun exportBackup(includeSettings: Boolean): String {
        val repository = backupRepository ?: error("Резервное копирование не подключено.")
        if (!_isReady.value) error("Данные приложения ещё загружаются.")
        val settings = if (includeSettings) {
            settingsRepository?.observeSettings()?.first()
                ?: error("Хранилище настроек не подключено.")
        } else null
        return repository.exportJson(includeSettings, settings)
    }

    /** Replaces all local Room data; a full backup also restores DataStore settings. */
    suspend fun restoreBackup(json: String): BackupRestoreResult {
        val repository = backupRepository ?: error("Восстановление не подключено.")
        if (!_isReady.value) error("Данные приложения ещё загружаются или не удалось их загрузить.")
        val oldHabitIds = _uiState.value.habits.map { it.id }.toSet()
        val oldTaskIds = _uiState.value.tasks.map { it.id }.toSet()
        val result = repository.restoreJson(json)

        var settingsRestored: Boolean? = null
        if (result.settings != null) {
            settingsRestored = try {
                settingsRepository?.replaceSettings(result.settings)
                    ?: error("Хранилище настроек не подключено.")
                _appSettings.value = result.settings
                true
            } catch (_: Exception) {
                false
            }
        }

        // Replace alarms too: ids that existed only in the previous database must be cancelled.
        oldHabitIds.forEach { habitReminderScheduler?.cancel(it) }
        oldTaskIds.forEach { taskReminderScheduler?.cancel(it) }
        val notificationSetting = if (settingsRestored == true) {
            result.settings?.notificationsEnabled ?: _appSettings.value.notificationsEnabled
        } else {
            _appSettings.value.notificationsEnabled
        }
        val restoredHabits = habitsRepository.getEnabledHabits()
        if (notificationSetting) restoredHabits.forEach { habitReminderScheduler?.schedule(it) }
        else restoredHabits.forEach { habitReminderScheduler?.cancel(it.id) }
        syncTaskReminders(_uiState.value.tasks, notificationSetting)

        return result.copy(settingsRestored = settingsRestored)
    }

    fun setThemeMode(mode: DashboardThemeMode) {
        updateAppSettings { setThemeMode(mode) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        updateAppSettings { setNotificationsEnabled(enabled) }
    }

    fun setShowCompletedTasksOnHome(enabled: Boolean) {
        updateAppSettings { setShowCompletedTasksOnHome(enabled) }
    }

    private fun updateAppSettings(update: suspend SettingsRepository.() -> Unit) {
        val repository = settingsRepository ?: run {
            _settingsOperationError.value = "Хранилище настроек не подключено."
            return
        }
        viewModelScope.launch {
            _settingsOperationError.value = null
            try {
                repository.update()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _settingsOperationError.value = "Не удалось сохранить настройку. Попробуйте ещё раз."
            }
        }
    }

    fun toggleTask(taskId: String) {
        viewModelScope.launch {
            taskMutationMutex.withLock {
                if (!_isReady.value) return@withLock
                _taskOperationError.value = null
                try {
                    if (!taskRepository.toggleTask(taskId)) {
                        _taskOperationError.value = "Задача не найдена."
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    _taskOperationError.value = "Не удалось обновить задачу. Изменения не применены."
                }
            }
        }
    }

    fun addTask(
        title: String, priority: TaskPriority, projectId: String? = null, dueDate: LocalDate? = LocalDate.now(),
        recurrence: TaskRecurrence = TaskRecurrence.NONE, recurrenceDays: Set<Int> = emptySet(),
        reminderEnabled: Boolean = false, reminderTime: String = "09:00"
    ) {
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) return
        viewModelScope.launch {
            taskMutationMutex.withLock {
                if (!_isReady.value) return@withLock
                _taskOperationError.value = null
                try {
                    if (!taskRepository.addTask(cleanTitle, priority, projectId, dueDate, recurrence, recurrenceDays, reminderEnabled, reminderTime)) {
                        _taskOperationError.value = "Не удалось создать задачу."
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    _taskOperationError.value = "Не удалось сохранить задачу."
                }
            }
        }
    }

    fun updateTask(task: DashboardTask, onSaved: (() -> Unit)? = null) {
        val cleanTitle = task.title.trim()
        if (cleanTitle.isBlank()) return
        viewModelScope.launch {
            taskMutationMutex.withLock {
                _taskOperationError.value = null
                try {
                    if (taskRepository.updateTask(task.copy(title = cleanTitle))) onSaved?.invoke()
                    else _taskOperationError.value = "Не удалось найти задачу или проект."
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    _taskOperationError.value = "Не удалось сохранить изменения задачи."
                }
            }
        }
    }

    fun deleteTask(taskId: String, onDeleted: (() -> Unit)? = null) {
        viewModelScope.launch {
            taskMutationMutex.withLock {
                _taskOperationError.value = null
                try {
                    if (taskRepository.deleteTask(taskId)) {
                        taskReminderScheduler?.cancel(taskId)
                        onDeleted?.invoke()
                    }
                    else _taskOperationError.value = "Задача уже удалена."
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    _taskOperationError.value = "Не удалось удалить задачу."
                }
            }
        }
    }

    fun saveNote(note: DashboardNote, onSaved: (() -> Unit)? = null) {
        val cleanTitle = note.title.trim()
        val cleanBody = note.body.trim()
        if (cleanTitle.isBlank() && cleanBody.isBlank()) return
        viewModelScope.launch {
            _noteOperationError.value = null
            try {
                notesRepository.saveNote(
                    note.copy(
                        title = cleanTitle.ifBlank { "Без названия" },
                        body = cleanBody,
                        updatedLabel = "Только что"
                    )
                )
                onSaved?.invoke()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _noteOperationError.value = "Не удалось сохранить заметку. Изменения не применены."
            }
        }
    }

    fun deleteNote(noteId: String, onDeleted: (() -> Unit)? = null) {
        viewModelScope.launch {
            _noteOperationError.value = null
            try {
                notesRepository.deleteNote(noteId)
                onDeleted?.invoke()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _noteOperationError.value = "Не удалось удалить заметку."
            }
        }
    }

    fun toggleNotePinned(noteId: String) {
        viewModelScope.launch {
            _noteOperationError.value = null
            try {
                notesRepository.togglePinned(noteId)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _noteOperationError.value = "Не удалось изменить закрепление заметки."
            }
        }
    }

    fun addHabit(title: String, color: HabitColor) {
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) return
        saveHabit(
            DashboardHabit(
                id = "",
                title = cleanTitle,
                color = color
            )
        )
    }

    fun saveHabit(habit: DashboardHabit, onSaved: (() -> Unit)? = null) {
        val cleanTitle = habit.title.trim()
        if (cleanTitle.isBlank()) {
            _habitOperationError.value = "Укажи название привычки."
            return
        }
        viewModelScope.launch {
            habitMutationMutex.withLock {
                _habitOperationError.value = null
                try {
                    val isNew = habit.id.isBlank()
                    val normalized = habit.copy(
                        id = if (isNew) UUID.randomUUID().toString() else habit.id,
                        title = cleanTitle,
                        reminderTime = habit.reminderTime.takeIf {
                            it.matches(Regex("(?:[01]\\d|2[0-3]):[0-5]\\d"))
                        } ?: "09:00",
                        reminderDays = habit.reminderDays.filter { it in 1..7 }.toSet()
                            .ifEmpty { (1..7).toSet() }
                    )
                    val saved = if (isNew) habitsRepository.createHabit(normalized)
                    else habitsRepository.updateHabit(normalized)
                    if (saved) {
                        val reminderScheduled = if (normalized.reminderEnabled && _appSettings.value.notificationsEnabled) {
                            habitReminderScheduler?.schedule(normalized) ?: true
                        } else {
                            habitReminderScheduler?.cancel(normalized.id)
                            true
                        }
                        if (!reminderScheduled) {
                            _habitOperationError.value = "Привычка сохранена, но напоминание не удалось запланировать. Проверь настройки будильников Android."
                        }
                        onSaved?.invoke()
                    } else {
                        _habitOperationError.value = if (isNew) "Не удалось создать привычку." else "Привычка уже удалена."
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    _habitOperationError.value = "Не удалось сохранить привычку. Изменения не применены."
                }
            }
        }
    }

    fun deleteHabit(habitId: String, onDeleted: (() -> Unit)? = null) {
        viewModelScope.launch {
            habitMutationMutex.withLock {
                _habitOperationError.value = null
                try {
                    if (habitsRepository.deleteHabit(habitId)) {
                        habitReminderScheduler?.cancel(habitId)
                        onDeleted?.invoke()
                    } else _habitOperationError.value = "Привычка уже удалена."
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    _habitOperationError.value = "Не удалось удалить привычку. Данные не изменены."
                }
            }
        }
    }

    fun toggleHabitToday(habitId: String) {
        viewModelScope.launch {
            habitMutationMutex.withLock {
                _habitOperationError.value = null
                try {
                    if (!habitsRepository.toggleCompletion(habitId, DateKeys.today())) {
                        _habitOperationError.value = "Привычка не найдена."
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    _habitOperationError.value = "Не удалось обновить отметку привычки."
                }
            }
        }
    }

    fun saveProject(project: DashboardProject, onSaved: (() -> Unit)? = null) {
        val cleanName = project.name.trim()
        if (cleanName.isBlank()) return
        viewModelScope.launch {
            projectMutationMutex.withLock {
                _projectOperationError.value = null
                try {
                    projectsRepository.saveProject(project.copy(name = cleanName, description = project.description.trim()))
                    onSaved?.invoke()
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    _projectOperationError.value = "Не удалось сохранить проект. Изменения не применены."
                }
            }
        }
    }

    fun deleteProject(projectId: String, onDeleted: (() -> Unit)? = null) {
        viewModelScope.launch {
            projectMutationMutex.withLock {
                _projectOperationError.value = null
                try {
                    if (projectsRepository.deleteProject(projectId)) onDeleted?.invoke()
                    else _projectOperationError.value = "Проект уже удалён."
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    _projectOperationError.value = "Не удалось удалить проект."
                }
            }
        }
    }

    fun toggleProjectStage(projectId: String, stageId: String) {
        viewModelScope.launch {
            projectMutationMutex.withLock {
                _projectOperationError.value = null
                try {
                    if (!projectsRepository.toggleStage(projectId, stageId)) {
                        _projectOperationError.value = "Проект или этап не найден."
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    _projectOperationError.value = "Не удалось обновить этап проекта."
                }
            }
        }
    }

    fun saveGoal(goal: DashboardGoal, onSaved: (() -> Unit)? = null) {
        val repository = goalsRepository ?: run {
            _goalOperationError.value = "Хранилище целей не подключено."
            return
        }
        viewModelScope.launch {
            goalMutationMutex.withLock {
                _goalOperationError.value = null
                try {
                    if (repository.saveGoal(goal)) onSaved?.invoke()
                    else _goalOperationError.value = "Проверь название, прогресс и связанный проект."
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    _goalOperationError.value = "Не удалось сохранить цель. Изменения не применены."
                }
            }
        }
    }

    fun deleteGoal(goalId: String, onDeleted: (() -> Unit)? = null) {
        val repository = goalsRepository ?: run {
            _goalOperationError.value = "Хранилище целей не подключено."
            return
        }
        viewModelScope.launch {
            goalMutationMutex.withLock {
                _goalOperationError.value = null
                try {
                    if (repository.deleteGoal(goalId)) onDeleted?.invoke()
                    else _goalOperationError.value = "Цель уже удалена."
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    _goalOperationError.value = "Не удалось удалить цель."
                }
            }
        }
    }

    fun changeGoalProgress(goalId: String, delta: Int) {
        val repository = goalsRepository ?: return
        viewModelScope.launch {
            goalMutationMutex.withLock {
                _goalOperationError.value = null
                try {
                    if (!repository.changeProgress(goalId, delta)) {
                        _goalOperationError.value = "Цель не найдена."
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    _goalOperationError.value = "Не удалось изменить прогресс цели."
                }
            }
        }
    }

    class Factory(
        private val dashboardRepository: DashboardRepository,
        private val taskRepository: TaskRepository,
        private val notesRepository: NotesRepository,
        private val habitsRepository: HabitsRepository,
        private val projectsRepository: ProjectsRepository,
        private val activityRepository: ActivityRepository,
        private val habitReminderScheduler: HabitReminderScheduler? = null,
        private val taskReminderScheduler: TaskReminderScheduler? = null,
        private val settingsRepository: SettingsRepository? = null,
        private val backupRepository: BackupRepository? = null,
        private val goalsRepository: GoalsRepository? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                return DashboardViewModel(
                    dashboardRepository,
                    taskRepository,
                    notesRepository,
                    habitsRepository,
                    projectsRepository,
                    activityRepository,
                    habitReminderScheduler,
                    taskReminderScheduler,
                    settingsRepository,
                    backupRepository,
                    goalsRepository
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
