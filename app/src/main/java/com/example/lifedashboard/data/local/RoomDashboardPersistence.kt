package com.example.lifedashboard.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import com.example.lifedashboard.data.DashboardSeedData
import com.example.lifedashboard.data.DateInput
import com.example.lifedashboard.data.TaskRecurrenceCalculator
import com.example.lifedashboard.data.model.DashboardActivityEvent
import com.example.lifedashboard.data.model.DashboardGoal
import com.example.lifedashboard.data.model.DashboardHabit
import com.example.lifedashboard.data.model.DashboardNote
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.data.model.DashboardTask
import com.example.lifedashboard.data.model.GoalColor
import com.example.lifedashboard.data.model.HabitColor
import com.example.lifedashboard.data.model.NoteAttachment
import com.example.lifedashboard.data.model.NoteCategory
import com.example.lifedashboard.data.model.NoteColor
import com.example.lifedashboard.data.model.ProjectStage
import com.example.lifedashboard.data.model.TaskDayCategory
import com.example.lifedashboard.data.model.TaskPriority
import com.example.lifedashboard.data.model.TaskRecurrence
import com.example.lifedashboard.data.model.TaskSubtask
import com.example.lifedashboard.data.model.TaskTimerSession
import com.example.lifedashboard.data.repository.ActivityRepository
import com.example.lifedashboard.data.repository.BackupRepository
import com.example.lifedashboard.data.repository.DashboardRepository
import com.example.lifedashboard.data.repository.GoalsRepository
import com.example.lifedashboard.data.repository.HabitsRepository
import com.example.lifedashboard.data.repository.NotesRepository
import com.example.lifedashboard.data.repository.ProjectsRepository
import com.example.lifedashboard.data.repository.TaskRepository
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Room implementation for all local feature repositories. */
class RoomDashboardPersistence private constructor(context: Context) :
    DashboardRepository,
    TaskRepository,
    NotesRepository,
    HabitsRepository,
    ProjectsRepository,
    ActivityRepository,
    GoalsRepository {

    private val appContext = context.applicationContext

    private val database = Room.databaseBuilder(
        appContext,
        LifeDashboardDatabase::class.java,
        "life_dashboard.db"
    )
        .fallbackToDestructiveMigration(true)
        .build()

    private val dao = database.dashboardDao()
    private val writeMutex = Mutex()

    override suspend fun initializeIfNeeded() = withContext(Dispatchers.IO) {
        writeMutex.withLock {
            database.withTransaction {
                if (dao.getMetadata(SEED_MARKER) != null) return@withTransaction

                val hasExistingData = dao.countTasks() > 0 ||
                    dao.countProjects() > 0 || dao.countNotes() > 0 || dao.countHabits() > 0

                if (!hasExistingData) {
                    val tasks = DashboardSeedData.tasks()
                    val projects = DashboardSeedData.projects()
                    val notes = DashboardSeedData.notes()
                    val habits = DashboardSeedData.habits()
                    dao.insertTasks(tasks.mapIndexed { index, task -> task.toEntity(sortOrder = tasks.size - index) })
                    dao.insertProjects(projects.map { it.toEntity() })
                    dao.insertStages(projects.flatMap { project ->
                        project.stages.mapIndexed { index, stage -> stage.toEntity(project.id, index) }
                    })
                    dao.insertNotes(notes.map { it.toEntity() })
                    dao.insertHabits(habits.map { it.toEntity() })
                }

                dao.insertMetadata(AppMetadataEntity(SEED_MARKER, "complete"))
            }
        }
    }

    override fun observeTasks(): Flow<List<DashboardTask>> =
        dao.observeTasks().map { entities -> entities.map { it.toModel() } }

    override fun observeSubtasks(): Flow<List<TaskSubtask>> =
        dao.observeSubtasks().map { items -> items.map { it.toModel() } }

    override fun observeTimerSessions(): Flow<List<TaskTimerSession>> =
        dao.observeTimerSessions().map { items -> items.map { it.toModel() } }

    suspend fun getAllTasks(): List<DashboardTask> = dao.getTasks().map { it.toModel() }

    suspend fun getTaskById(taskId: String): DashboardTask? = dao.getTask(taskId)?.toModel()

    override suspend fun toggleTask(taskId: String): Boolean = writeMutex.withLock {
        database.withTransaction {
            val current = dao.getTask(taskId) ?: return@withTransaction false
            val updatedTask = current.copy(isCompleted = !current.isCompleted)
            dao.updateTask(updatedTask)

            if (updatedTask.isCompleted && current.recurrence != "NONE" && current.dueDate != null) {
                val nextDate = TaskRecurrenceCalculator.nextDate(
                    completedDate = current.dueDate,
                    recurrence = TaskRecurrence.valueOf(current.recurrence),
                    selectedDays = current.recurrenceDays.split(",").mapNotNull(String::toIntOrNull).toSet(),
                    anchorDayOfMonth = current.recurrenceAnchorDay
                )
                val nextRemaining = TaskRecurrenceCalculator.nextRemainingCount(current.recurrenceRemainingCount)
                val seriesId = current.recurrenceSeriesId ?: current.id
                if (TaskRecurrenceCalculator.canCreateNext(nextDate, current.recurrenceEndDate, nextRemaining) &&
                    dao.getTaskForSeriesDate(seriesId, nextDate.toString()) == null
                ) {
                    if (current.recurrenceSeriesId == null) dao.updateTask(current.copy(recurrenceSeriesId = seriesId))
                    dao.insertTask(
                        current.copy(
                            id = UUID.randomUUID().toString(),
                            dueLabel = DateInput.formatTaskLabel(nextDate),
                            dueDate = nextDate,
                            dayCategory = DateInput.categoryFor(nextDate)?.name ?: TaskDayCategory.UPCOMING.name,
                            isCompleted = false,
                            showOnHome = false,
                            recurrenceSeriesId = seriesId,
                            recurrenceAnchorDay = current.recurrenceAnchorDay.takeIf { it in 1..31 } ?: current.dueDate.dayOfMonth,
                            recurrenceRemainingCount = nextRemaining,
                            sortOrder = dao.nextTaskSortOrder()
                        )
                    )
                    current.projectId?.let { changeProjectTaskCounters(it, 1, 0) }
                }
            }

            current.projectId?.let { projectId ->
                val project = dao.getProject(projectId)
                if (project != null) {
                    val completed = (project.completedTasks + if (updatedTask.isCompleted) 1 else -1)
                        .coerceIn(0, project.totalTasks)
                    dao.updateProject(
                        project.copy(
                            completedTasks = completed,
                            progress = if (project.totalTasks == 0) 0f else completed.toFloat() / project.totalTasks,
                            isCompleted = project.totalTasks > 0 && completed == project.totalTasks
                        )
                    )
                }
            }

            dao.insertEvent(
                ActivityEventEntity(
                    id = UUID.randomUUID().toString(),
                    type = "TASK_COMPLETION",
                    itemId = taskId,
                    occurredAtEpochMillis = System.currentTimeMillis(),
                    completed = updatedTask.isCompleted
                )
            )
            true
        }
    }

    override suspend fun addTask(
        title: String,
        priority: TaskPriority,
        projectId: String?,
        dueDate: LocalDate?,
        recurrence: TaskRecurrence,
        recurrenceDays: Set<Int>,
        reminderEnabled: Boolean,
        reminderTime: String,
        recurrenceEndDate: LocalDate?,
        recurrenceRemainingCount: Int?,
        plannedMinutes: Int
    ): Boolean {
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) return false
        if (recurrence != TaskRecurrence.NONE && dueDate == null) return false
        val validRecurrenceDays = recurrenceDays.filter { it in 1..7 }.toSet()
        if (recurrence == TaskRecurrence.WEEKLY && validRecurrenceDays.isEmpty()) return false
        val normalizedRemaining = recurrenceRemainingCount?.takeIf { it >= 1 }
        val normalizedPlanned = plannedMinutes.coerceIn(5, 24 * 60)

        return writeMutex.withLock {
            database.withTransaction {
                if (projectId != null && dao.getProject(projectId) == null) return@withTransaction false
                val taskId = UUID.randomUUID().toString()
                dao.insertTask(
                    TaskEntity(
                        id = taskId,
                        title = cleanTitle,
                        trailingLabel = null,
                        dueLabel = DateInput.formatTaskLabel(dueDate),
                        dueDate = dueDate,
                        projectId = projectId,
                        dayCategory = DateInput.categoryFor(dueDate)?.name ?: TaskDayCategory.UPCOMING.name,
                        priority = priority.name,
                        isCompleted = false,
                        showOnHome = false,
                        sortOrder = dao.nextTaskSortOrder(),
                        recurrence = recurrence.name,
                        recurrenceDays = if (recurrence == TaskRecurrence.WEEKLY) validRecurrenceDays.sorted().joinToString(",") else "",
                        recurrenceSeriesId = taskId.takeIf { recurrence != TaskRecurrence.NONE },
                        recurrenceAnchorDay = if (recurrence == TaskRecurrence.MONTHLY) dueDate!!.dayOfMonth else 0,
                        reminderEnabled = reminderEnabled && dueDate != null && validReminderTime(reminderTime),
                        reminderTime = reminderTime.takeIf(::validReminderTime) ?: "09:00",
                        recurrenceEndDate = recurrenceEndDate.takeIf { recurrence != TaskRecurrence.NONE },
                        recurrenceRemainingCount = normalizedRemaining.takeIf { recurrence != TaskRecurrence.NONE },
                        plannedMinutes = normalizedPlanned
                    )
                )
                projectId?.let { changeProjectTaskCounters(it, totalDelta = 1, completedDelta = 0) }
                true
            }
        }
    }

    override suspend fun updateTask(task: DashboardTask): Boolean {
        val cleanTitle = task.title.trim()
        if (cleanTitle.isBlank()) return false
        return writeMutex.withLock {
            database.withTransaction {
                val current = dao.getTask(task.id) ?: return@withTransaction false
                val newProjectId = task.projectId?.takeIf { dao.getProject(it) != null }
                    ?: if (task.projectId == null) null else return@withTransaction false
                val updated = current.copy(
                    title = cleanTitle,
                    trailingLabel = task.trailingLabel,
                    dueLabel = task.dueLabel.trim().ifBlank { "Без срока" },
                    dueDate = task.dueDate,
                    projectId = newProjectId,
                    dayCategory = task.dayCategory.name,
                    priority = task.priority.name,
                    showOnHome = task.showOnHome,
                    recurrence = task.recurrence.name,
                    recurrenceDays = task.recurrenceDays.filter { it in 1..7 }.sorted().joinToString(","),
                    recurrenceSeriesId = if (task.recurrence == TaskRecurrence.NONE) null else (task.recurrenceSeriesId ?: task.id),
                    recurrenceAnchorDay = if (task.recurrence == TaskRecurrence.MONTHLY) {
                        task.recurrenceAnchorDay.takeIf { it in 1..31 } ?: (task.dueDate?.dayOfMonth ?: 0)
                    } else 0,
                    reminderEnabled = task.reminderEnabled && task.dueDate != null && validReminderTime(task.reminderTime),
                    reminderTime = task.reminderTime.takeIf(::validReminderTime) ?: "09:00",
                    recurrenceEndDate = task.recurrenceEndDate.takeIf { task.recurrence != TaskRecurrence.NONE },
                    recurrenceRemainingCount = task.recurrenceRemainingCount?.takeIf { it >= 1 && task.recurrence != TaskRecurrence.NONE },
                    plannedMinutes = task.plannedMinutes.coerceIn(5, 24 * 60)
                )
                dao.updateTask(updated)
                if (current.projectId != newProjectId) {
                    current.projectId?.let { changeProjectTaskCounters(it, -1, if (current.isCompleted) -1 else 0) }
                    newProjectId?.let { changeProjectTaskCounters(it, 1, if (updated.isCompleted) 1 else 0) }
                }
                true
            }
        }
    }

    override suspend fun deleteTask(taskId: String): Boolean = writeMutex.withLock {
        database.withTransaction {
            val current = dao.getTask(taskId) ?: return@withTransaction false
            val skippedRecurringOccurrence = !current.isCompleted && current.recurrence != "NONE" && current.dueDate != null
            if (skippedRecurringOccurrence) {
                val nextDate = TaskRecurrenceCalculator.nextDate(
                    completedDate = current.dueDate!!,
                    recurrence = TaskRecurrence.valueOf(current.recurrence),
                    selectedDays = current.recurrenceDays.split(",").mapNotNull(String::toIntOrNull).toSet(),
                    anchorDayOfMonth = current.recurrenceAnchorDay
                )
                val nextRemaining = TaskRecurrenceCalculator.nextRemainingCount(current.recurrenceRemainingCount)
                val seriesId = current.recurrenceSeriesId ?: current.id
                if (TaskRecurrenceCalculator.canCreateNext(nextDate, current.recurrenceEndDate, nextRemaining) &&
                    dao.getTaskForSeriesDate(seriesId, nextDate.toString()) == null
                ) {
                    dao.insertTask(
                        current.copy(
                            id = UUID.randomUUID().toString(),
                            dueLabel = DateInput.formatTaskLabel(nextDate!!),
                            dueDate = nextDate,
                            dayCategory = DateInput.categoryFor(nextDate)?.name ?: TaskDayCategory.UPCOMING.name,
                            isCompleted = false,
                            showOnHome = false,
                            recurrenceSeriesId = seriesId,
                            recurrenceAnchorDay = current.recurrenceAnchorDay.takeIf { it in 1..31 } ?: current.dueDate.dayOfMonth,
                            recurrenceRemainingCount = nextRemaining,
                            sortOrder = dao.nextTaskSortOrder()
                        )
                    )
                    current.projectId?.let { changeProjectTaskCounters(it, 1, 0) }
                }
                dao.insertEvent(
                    ActivityEventEntity(
                        id = UUID.randomUUID().toString(),
                        type = "TASK_SKIPPED",
                        itemId = taskId,
                        occurredAtEpochMillis = System.currentTimeMillis(),
                        completed = false
                    )
                )
            }
            dao.deleteSubtasksForTask(taskId)
            dao.deleteTimerSessionsForTask(taskId)
            dao.deleteTask(taskId)
            current.projectId?.let { changeProjectTaskCounters(it, -1, if (current.isCompleted) -1 else 0) }
            true
        }
    }

    override suspend fun addSubtask(taskId: String, title: String): Boolean {
        val clean = title.trim()
        if (clean.isBlank()) return false
        return writeMutex.withLock {
            database.withTransaction {
                if (dao.getTask(taskId) == null) return@withTransaction false
                dao.insertSubtask(
                    TaskSubtaskEntity(
                        id = UUID.randomUUID().toString(),
                        taskId = taskId,
                        title = clean,
                        isCompleted = false,
                        sortOrder = dao.nextSubtaskSortOrder(taskId)
                    )
                )
                true
            }
        }
    }

    override suspend fun toggleSubtask(subtaskId: String): Boolean = writeMutex.withLock {
        database.withTransaction {
            val current = dao.getSubtask(subtaskId) ?: return@withTransaction false
            dao.insertSubtask(current.copy(isCompleted = !current.isCompleted))
            true
        }
    }

    override suspend fun deleteSubtask(subtaskId: String): Boolean = writeMutex.withLock {
        database.withTransaction {
            if (dao.getSubtask(subtaskId) == null) return@withTransaction false
            dao.deleteSubtask(subtaskId)
            true
        }
    }

    override suspend fun saveTimerSession(session: TaskTimerSession) {
        if (session.durationSeconds <= 0L) return
        writeMutex.withLock {
            database.withTransaction {
                dao.insertTimerSession(session.toEntity())
            }
        }
    }

    private suspend fun changeProjectTaskCounters(projectId: String, totalDelta: Int, completedDelta: Int) {
        val project = dao.getProject(projectId) ?: return
        val total = (project.totalTasks + totalDelta).coerceAtLeast(0)
        val completed = (project.completedTasks + completedDelta).coerceIn(0, total)
        dao.updateProject(
            project.copy(
                totalTasks = total,
                completedTasks = completed,
                progress = if (total == 0) 0f else completed.toFloat() / total,
                isCompleted = project.isCompleted || (total > 0 && completed == total)
            )
        )
    }

    override fun observeNotes(): Flow<List<DashboardNote>> =
        dao.observeNotes().map { entities -> entities.map { it.toModel() } }

    override fun observeAttachments(): Flow<List<NoteAttachment>> =
        dao.observeNoteAttachments().map { entities -> entities.map { it.toModel() } }

    override suspend fun saveNote(note: DashboardNote) {
        val normalized = note.copy(
            title = note.title.trim().ifBlank { "Без названия" },
            body = note.body.trim(),
            updatedLabel = "Только что"
        )
        writeMutex.withLock {
            database.withTransaction {
                val validProjectId = normalized.projectId?.takeIf { dao.getProject(it) != null }
                val realCount = dao.countAttachmentsForNote(normalized.id)
                val count = if (realCount > 0) realCount else normalized.attachmentCount
                dao.insertNote(normalized.copy(attachmentCount = count, projectId = validProjectId).toEntity())
            }
        }
    }

    override suspend fun deleteNote(noteId: String) {
        val paths = writeMutex.withLock {
            database.withTransaction {
                val attachments = dao.getAttachmentsForNote(noteId)
                dao.deleteAttachmentsForNote(noteId)
                dao.deleteNote(noteId)
                attachments.map { it.localPath }
            }
        }
        paths.forEach(NoteAttachmentStorage::deleteFile)
        NoteAttachmentStorage.deleteNoteDirectory(appContext, noteId)
    }

    override suspend fun togglePinned(noteId: String) {
        writeMutex.withLock {
            database.withTransaction {
                dao.getNote(noteId)?.let { current ->
                    dao.insertNote(current.copy(isPinned = !current.isPinned, updatedLabel = "Только что"))
                }
            }
        }
    }

    override suspend fun addAttachment(attachment: NoteAttachment): Boolean = writeMutex.withLock {
        database.withTransaction {
            val existingNote = dao.getNote(attachment.noteId)
            dao.insertNoteAttachment(attachment.toEntity())
            if (existingNote != null) {
                val total = dao.countAttachmentsForNote(attachment.noteId)
                dao.insertNote(existingNote.copy(attachmentCount = total, updatedLabel = "Только что"))
            }
            true
        }
    }

    override suspend fun deleteAttachment(attachmentId: String): Boolean {
        val removed = writeMutex.withLock {
            database.withTransaction {
                val current = dao.getAttachment(attachmentId) ?: return@withTransaction null
                dao.deleteNoteAttachment(attachmentId)
                dao.getNote(current.noteId)?.let { note ->
                    val total = dao.countAttachmentsForNote(current.noteId)
                    dao.insertNote(note.copy(attachmentCount = total, updatedLabel = "Только что"))
                }
                current
            }
        } ?: return false
        NoteAttachmentStorage.deleteFile(removed.localPath)
        return true
    }

    override fun observeHabits(): Flow<List<DashboardHabit>> =
        dao.observeHabits().map { entities -> entities.map { it.toModel() } }

    override suspend fun getHabit(habitId: String): DashboardHabit? =
        dao.getHabit(habitId)?.toModel()

    override suspend fun getEnabledHabits(): List<DashboardHabit> =
        dao.getEnabledReminderHabits().map { it.toModel() }

    fun close() {
        database.close()
    }

    override suspend fun createHabit(habit: DashboardHabit): Boolean {
        val normalized = habit.normalizeForStorage() ?: return false
        if (normalized.id.isBlank()) return false
        return writeMutex.withLock {
            database.withTransaction {
                if (dao.getHabit(normalized.id) != null) return@withTransaction false
                dao.insertHabit(normalized.copy(completionDates = emptySet()).toEntity())
                true
            }
        }
    }

    override suspend fun updateHabit(habit: DashboardHabit): Boolean {
        val normalized = habit.normalizeForStorage() ?: return false
        if (normalized.id.isBlank()) return false
        return writeMutex.withLock {
            database.withTransaction {
                val current = dao.getHabit(normalized.id) ?: return@withTransaction false
                dao.insertHabit(
                    current.copy(
                        title = normalized.title,
                        color = normalized.color.name,
                        reminderLabel = normalized.reminderLabel,
                        reminderEnabled = normalized.reminderEnabled,
                        reminderTime = normalized.reminderTime,
                        reminderDays = normalized.reminderDays.sorted().joinToString(",")
                    )
                )
                true
            }
        }
    }

    override suspend fun deleteHabit(habitId: String): Boolean = writeMutex.withLock {
        database.withTransaction {
            if (dao.getHabit(habitId) == null) return@withTransaction false
            dao.deleteHabit(habitId)
            true
        }
    }

    override suspend fun toggleCompletion(habitId: String, dateKey: String): Boolean =
        writeMutex.withLock {
            database.withTransaction {
                val current = dao.getHabit(habitId) ?: return@withTransaction false
                val dates = current.completionDates.split(',').filter(String::isNotBlank).toMutableSet()
                if (!dates.add(dateKey)) dates.remove(dateKey)
                dao.insertHabit(current.copy(completionDates = dates.sorted().joinToString(",")))
                true
            }
        }

    override fun observeProjects(): Flow<List<DashboardProject>> =
        combine(dao.observeProjects(), dao.observeAllStages()) { projects, stages ->
            val stagesByProject = stages.groupBy { it.projectId }
            projects.map { entity -> entity.toModel(stagesByProject[entity.id].orEmpty()) }
        }

    override suspend fun saveProject(project: DashboardProject) {
        writeMutex.withLock {
            database.withTransaction {
                val existing = dao.getProject(project.id)
                val totalTasks = existing?.totalTasks ?: project.totalTasks
                val completedTasks = existing?.completedTasks ?: project.completedTasks
                val progress = if (totalTasks == 0) project.progress else completedTasks.toFloat() / totalTasks
                dao.insertProject(
                    project.copy(
                        totalTasks = totalTasks,
                        completedTasks = completedTasks,
                        progress = progress
                    ).toEntity()
                )
                dao.deleteStagesForProject(project.id)
                if (project.stages.isNotEmpty()) {
                    dao.insertStages(project.stages.mapIndexed { index, stage -> stage.toEntity(project.id, index) })
                }
            }
        }
    }

    override suspend fun deleteProject(projectId: String): Boolean = writeMutex.withLock {
        database.withTransaction {
            if (dao.getProject(projectId) == null) return@withTransaction false
            dao.detachTasksFromProject(projectId)
            dao.detachGoalsFromProject(projectId)
            dao.detachNotesFromProject(projectId)
            dao.deleteStagesForProject(projectId)
            dao.deleteProject(projectId)
            true
        }
    }

    override suspend fun toggleStage(projectId: String, stageId: String): Boolean =
        writeMutex.withLock {
            database.withTransaction {
                val project = dao.getProject(projectId) ?: return@withTransaction false
                val stages = dao.getStagesForProject(projectId)
                val targetIndex = stages.indexOfFirst { it.id == stageId }
                if (targetIndex < 0) return@withTransaction false
                val target = stages[targetIndex]
                val targetWillBeCompleted = !target.isCompleted
                val candidateCurrentIndex = if (targetWillBeCompleted) {
                    (targetIndex + 1 until stages.size).firstOrNull { !stages[it].isCompleted }
                        ?: stages.indices.firstOrNull { !stages[it].isCompleted || it == targetIndex }
                } else {
                    targetIndex
                }
                val updated = stages.mapIndexed { index, stage ->
                    val isCompleted = if (stage.id == stageId) targetWillBeCompleted else stage.isCompleted
                    stage.copy(isCompleted = isCompleted, isCurrent = index == candidateCurrentIndex && !isCompleted)
                }
                dao.deleteStagesForProject(projectId)
                if (updated.isNotEmpty()) dao.insertStages(updated.mapIndexed { index, stage -> stage.copy(sortOrder = index) })
                val completedCount = updated.count { it.isCompleted }
                val currentIndex = updated.indexOfFirst { it.isCurrent }
                val label = when {
                    updated.isNotEmpty() && completedCount == updated.size -> "Все этапы завершены"
                    currentIndex >= 0 -> "Этап ${currentIndex + 1} из ${updated.size}"
                    else -> "Этап ${completedCount + 1} из ${updated.size}"
                }
                dao.updateProject(project.copy(stageLabel = label, isCompleted = (updated.isNotEmpty() && completedCount == updated.size) || (project.totalTasks > 0 && project.completedTasks == project.totalTasks)))
                true
            }
        }

    override fun observeEvents(): Flow<List<DashboardActivityEvent>> =
        dao.observeActivityEvents().map { events ->
            events.map { DashboardActivityEvent(it.id, it.type, it.itemId, it.occurredAtEpochMillis, it.completed) }
        }

    override fun observeGoals(): Flow<List<DashboardGoal>> =
        dao.observeGoals().map { goals -> goals.map { it.toModel() } }

    override suspend fun saveGoal(goal: DashboardGoal): Boolean {
        val normalized = goal.normalizeForStorage() ?: return false
        return writeMutex.withLock {
            database.withTransaction {
                if (normalized.projectId != null && dao.getProject(normalized.projectId) == null) {
                    return@withTransaction false
                }
                val current = dao.getGoal(normalized.id)
                val sortOrder = current?.sortOrder ?: dao.nextGoalSortOrder()
                dao.insertGoal(normalized.copy(sortOrder = sortOrder).toEntity())
                true
            }
        }
    }

    override suspend fun deleteGoal(goalId: String): Boolean = writeMutex.withLock {
        database.withTransaction {
            if (dao.getGoal(goalId) == null) return@withTransaction false
            dao.deleteGoal(goalId)
            true
        }
    }

    override suspend fun changeProgress(goalId: String, delta: Int): Boolean = writeMutex.withLock {
        database.withTransaction {
            val current = dao.getGoal(goalId) ?: return@withTransaction false
            val nextValue = (current.currentValue.toLong() + delta.toLong())
                .coerceIn(0L, current.targetValue.toLong()).toInt()
            dao.insertGoal(current.copy(currentValue = nextValue))
            true
        }
    }

    fun createBackupRepository(): BackupRepository =
        com.example.lifedashboard.data.backup.RoomDashboardBackupRepository(database)

    companion object {
        private const val SEED_MARKER = "starter_seed_v1"
        fun create(context: Context) = RoomDashboardPersistence(context)
    }
}

private fun DashboardHabit.normalizeForStorage(): DashboardHabit? {
    val cleanTitle = title.trim()
    if (cleanTitle.isBlank()) return null
    val validTime = reminderTime.takeIf { it.matches(Regex("(?:[01]\\d|2[0-3]):[0-5]\\d")) } ?: "09:00"
    val validDays = reminderDays.filter { it in 1..7 }.toSet().ifEmpty { (1..7).toSet() }
    return copy(title = cleanTitle, reminderTime = validTime, reminderDays = validDays)
}

private fun TaskEntity.toModel() = DashboardTask(
    id = id, title = title, trailingLabel = trailingLabel, dueLabel = dueLabel, projectId = projectId,
    dayCategory = TaskDayCategory.valueOf(dayCategory), priority = TaskPriority.valueOf(priority),
    isCompleted = isCompleted, showOnHome = showOnHome, dueDate = dueDate,
    recurrence = runCatching { TaskRecurrence.valueOf(recurrence) }.getOrDefault(TaskRecurrence.NONE),
    recurrenceDays = recurrenceDays.split(',').mapNotNull { it.toIntOrNull()?.takeIf { day -> day in 1..7 } }.toSet(),
    recurrenceSeriesId = recurrenceSeriesId, recurrenceAnchorDay = recurrenceAnchorDay,
    reminderEnabled = reminderEnabled, reminderTime = reminderTime,
    recurrenceEndDate = recurrenceEndDate, recurrenceRemainingCount = recurrenceRemainingCount,
    plannedMinutes = plannedMinutes
)

private fun DashboardTask.toEntity(sortOrder: Int = 0) = TaskEntity(
    id = id, title = title, trailingLabel = trailingLabel, dueLabel = dueLabel,
    dueDate = dueDate ?: DateInput.parseLegacyLabel(dueLabel), projectId = projectId,
    dayCategory = dayCategory.name, priority = priority.name, isCompleted = isCompleted,
    showOnHome = showOnHome, sortOrder = sortOrder, recurrence = recurrence.name,
    recurrenceDays = recurrenceDays.sorted().joinToString(","), recurrenceSeriesId = recurrenceSeriesId,
    recurrenceAnchorDay = recurrenceAnchorDay, reminderEnabled = reminderEnabled, reminderTime = reminderTime,
    recurrenceEndDate = recurrenceEndDate, recurrenceRemainingCount = recurrenceRemainingCount,
    plannedMinutes = plannedMinutes
)

private fun TaskSubtaskEntity.toModel() = TaskSubtask(id, taskId, title, isCompleted, sortOrder)
private fun TaskTimerSessionEntity.toModel() = TaskTimerSession(id, taskId, projectId, startedAtMillis, endedAtMillis, durationSeconds)
private fun TaskTimerSession.toEntity() = TaskTimerSessionEntity(id, taskId, projectId, startedAtMillis, endedAtMillis, durationSeconds)

private fun ProjectEntity.toModel(stages: List<ProjectStageEntity>) = DashboardProject(
    id = id,
    name = name,
    description = description,
    deadlineLabel = deadlineLabel,
    deadlineDate = deadlineDate,
    stageLabel = stageLabel,
    progress = progress,
    completedTasks = completedTasks,
    totalTasks = totalTasks,
    colorArgb = colorArgb,
    isCompleted = isCompleted,
    stages = stages.sortedWith(compareBy<ProjectStageEntity> { it.sortOrder }.thenBy { it.id }).map { ProjectStage(it.id, it.title, it.isCompleted, it.isCurrent) }
)

private fun DashboardProject.toEntity() = ProjectEntity(
    id = id, name = name, description = description, deadlineLabel = deadlineLabel,
    deadlineDate = deadlineDate ?: DateInput.parseLegacyLabel(deadlineLabel), stageLabel = stageLabel,
    progress = progress, completedTasks = completedTasks, totalTasks = totalTasks,
    colorArgb = colorArgb, isCompleted = isCompleted
)

private fun ProjectStage.toEntity(projectId: String, sortOrder: Int = 0) =
    ProjectStageEntity(id, projectId, title, isCompleted, isCurrent, sortOrder)

private fun NoteEntity.toModel() = DashboardNote(
    id = id,
    title = title,
    body = body,
    category = NoteCategory.valueOf(category),
    color = NoteColor.valueOf(color),
    updatedLabel = updatedLabel,
    isPinned = isPinned,
    attachmentCount = attachmentCount,
    projectId = projectId
)

private fun DashboardNote.toEntity() = NoteEntity(
    id = id,
    title = title,
    body = body,
    category = category.name,
    color = color.name,
    updatedLabel = updatedLabel,
    isPinned = isPinned,
    attachmentCount = attachmentCount,
    projectId = projectId
)

private fun NoteAttachmentEntity.toModel() =
    NoteAttachment(id, noteId, displayName, mimeType, localPath, sizeBytes, createdAtEpochMillis)

private fun NoteAttachment.toEntity() =
    NoteAttachmentEntity(id, noteId, displayName, mimeType, localPath, sizeBytes, createdAtEpochMillis)

private fun HabitEntity.toModel() = DashboardHabit(
    id = id,
    title = title,
    completionDates = completionDates.split(',').filter(String::isNotBlank).toSet(),
    color = HabitColor.valueOf(color),
    reminderLabel = reminderLabel,
    reminderEnabled = reminderEnabled,
    reminderTime = reminderTime,
    reminderDays = reminderDays.split(',').mapNotNull(String::toIntOrNull).filter { it in 1..7 }.toSet()
        .ifEmpty { (1..7).toSet() }
)

private fun DashboardHabit.toEntity() = HabitEntity(
    id = id,
    title = title,
    completionDates = completionDates.sorted().joinToString(","),
    color = color.name,
    reminderLabel = reminderLabel,
    reminderEnabled = reminderEnabled,
    reminderTime = reminderTime,
    reminderDays = reminderDays.filter { it in 1..7 }.sorted().joinToString(",")
)

private fun DashboardGoal.normalizeForStorage(): DashboardGoal? {
    val cleanTitle = title.trim()
    val cleanUnit = unit.trim().ifBlank { "раз" }
    if (id.isBlank() || cleanTitle.isBlank()) return null
    if (targetValue !in 1..1_000_000 || currentValue !in 0..targetValue) return null
    if (cleanUnit.length > 24) return null
    return copy(title = cleanTitle, description = description.trim(), unit = cleanUnit)
}

private fun GoalEntity.toModel() = DashboardGoal(
    id = id,
    title = title,
    description = description,
    dueDate = dueDate,
    currentValue = currentValue,
    targetValue = targetValue,
    unit = unit,
    projectId = projectId,
    color = GoalColor.valueOf(color),
    sortOrder = sortOrder
)

private fun DashboardGoal.toEntity() = GoalEntity(
    id = id,
    title = title,
    description = description,
    dueDate = dueDate,
    currentValue = currentValue,
    targetValue = targetValue,
    unit = unit,
    projectId = projectId,
    color = color.name,
    sortOrder = sortOrder
)

private fun validReminderTime(value: String): Boolean = value.matches(Regex("(?:[01]\\d|2[0-3]):[0-5]\\d"))