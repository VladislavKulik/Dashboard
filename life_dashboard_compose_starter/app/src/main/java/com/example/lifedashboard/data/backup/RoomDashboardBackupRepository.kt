package com.example.lifedashboard.data.backup

import androidx.room.withTransaction
import com.example.lifedashboard.data.DateKeys
import com.example.lifedashboard.data.DateInput
import com.example.lifedashboard.data.local.*
import com.example.lifedashboard.data.model.DashboardSettings
import com.example.lifedashboard.data.model.DashboardThemeMode
import com.example.lifedashboard.data.repository.BackupRepository
import com.example.lifedashboard.data.repository.BackupRestoreResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Versioned logical backup of every persisted Room table. It intentionally avoids copying a
 * live SQLite/WAL file; Room snapshots and restores are taken transactionally instead.
 */
class RoomDashboardBackupRepository(
    private val database: LifeDashboardDatabase
) : BackupRepository {
    private val dao = database.dashboardDao()
    private val mutex = Mutex()

    override suspend fun exportJson(includeSettings: Boolean, settings: DashboardSettings?): String =
        withContext(Dispatchers.IO) {
            mutex.withLock {
                val data = database.withTransaction {
                    JSONObject()
                        .put("tasks", JSONArray().apply { dao.getTasks().forEach { put(it.toJson()) } })
                        .put("projects", JSONArray().apply { dao.getProjects().forEach { put(it.toJson()) } })
                        .put("projectStages", JSONArray().apply { dao.getStages().forEach { put(it.toJson()) } })
                        .put("notes", JSONArray().apply { dao.getNotes().forEach { put(it.toJson()) } })
                        .put("habits", JSONArray().apply { dao.getHabits().forEach { put(it.toJson()) } })
                        .put("goals", JSONArray().apply { dao.getGoals().forEach { put(it.toJson()) } })
                        .put("activityEvents", JSONArray().apply { dao.getActivityEvents().forEach { put(it.toJson()) } })
                        .put("appMetadata", JSONArray().apply { dao.getAllMetadata().forEach { put(it.toJson()) } })
                }
                val root = JSONObject()
                    .put("appId", APP_ID)
                    .put("formatVersion", FORMAT_VERSION)
                    .put("type", if (includeSettings) TYPE_FULL_BACKUP else TYPE_DATA_EXPORT)
                    .put("createdAtEpochMillis", System.currentTimeMillis())
                    .put("data", data)
                if (includeSettings) {
                    requireNotNull(settings) { "Настройки приложения недоступны для резервной копии." }
                    root.put("settings", settings.toJson())
                }
                root.toString(2).also { json ->
                    require(json.toByteArray(Charsets.UTF_8).size <= MAX_BACKUP_BYTES) {
                        "Резервная копия превышает лимит 20 МБ. Уменьши объём заметок или истории и повтори экспорт."
                    }
                }
            }
        }

    override suspend fun restoreJson(json: String): BackupRestoreResult = withContext(Dispatchers.IO) {
        // Parse and validate the entire document before opening the write transaction.
        val snapshot = parseAndValidate(json)
        mutex.withLock {
            database.withTransaction {
                dao.clearTasks()
                dao.clearStages()
                dao.clearProjects()
                dao.clearNotes()
                dao.clearHabits()
                dao.clearGoals()
                dao.clearActivityEvents()
                dao.clearMetadata()

                // Insert project parents before dependent task/stage rows; each list is
                // serialized in the same order used by its Room observer.
                if (snapshot.projects.isNotEmpty()) dao.insertProjects(snapshot.projects)
                if (snapshot.stages.isNotEmpty()) dao.insertStages(snapshot.stages)
                if (snapshot.tasks.isNotEmpty()) dao.insertTasks(snapshot.tasks)
                if (snapshot.notes.isNotEmpty()) dao.insertNotes(snapshot.notes)
                if (snapshot.habits.isNotEmpty()) dao.insertHabits(snapshot.habits)
                if (snapshot.goals.isNotEmpty()) dao.insertGoals(snapshot.goals)
                if (snapshot.events.isNotEmpty()) dao.insertEvents(snapshot.events)
                if (snapshot.metadata.isNotEmpty()) dao.insertMetadataItems(snapshot.metadata)
                // A valid import must never trigger starter-data seeding on next launch,
                // even when the source was a manually curated or older JSON export.
                if (dao.getMetadata(SEED_MARKER) == null) {
                    dao.insertMetadata(AppMetadataEntity(SEED_MARKER, "complete"))
                }
            }
        }
        BackupRestoreResult(
            taskCount = snapshot.tasks.size,
            projectCount = snapshot.projects.size,
            noteCount = snapshot.notes.size,
            habitCount = snapshot.habits.size,
            eventCount = snapshot.events.size,
            goalCount = snapshot.goals.size,
            settings = snapshot.settings
        )
    }

    private data class Snapshot(
        val tasks: List<TaskEntity>,
        val projects: List<ProjectEntity>,
        val stages: List<ProjectStageEntity>,
        val notes: List<NoteEntity>,
        val habits: List<HabitEntity>,
        val goals: List<GoalEntity>,
        val events: List<ActivityEventEntity>,
        val metadata: List<AppMetadataEntity>,
        val settings: DashboardSettings?
    )

    private fun parseAndValidate(json: String): Snapshot {
        require(json.isNotBlank()) { "Файл пустой." }
        val root = try { JSONObject(json) } catch (_: Exception) {
            throw IllegalArgumentException("Файл не является корректным JSON.")
        }
        require(root.requiredString("appId") == APP_ID) { "Это не файл Life Dashboard." }
        val formatVersion = root.requiredInt("formatVersion")
        require(formatVersion in MIN_SUPPORTED_FORMAT_VERSION..FORMAT_VERSION) { "Версия файла не поддерживается." }
        val type = root.requiredString("type")
        require(type == TYPE_FULL_BACKUP || type == TYPE_DATA_EXPORT) { "Неизвестный тип файла." }
        root.requiredLong("createdAtEpochMillis").also { require(it > 0L) }
        val data = root.requiredObject("data")

        val tasks = data.requiredArray("tasks").objects { item ->
            TaskEntity(
                id = item.requiredString("id"),
                title = item.requiredString("title"),
                trailingLabel = item.optionalString("trailingLabel"),
                dueLabel = item.requiredString("dueLabel"),
                dueDate = if (formatVersion >= 3) {
                    val raw = item.optionalString("dueDate")
                    require(raw == null || DateInput.parseIso(raw) != null) { "Некорректная дата задачи." }
                    DateInput.parseIso(raw)
                } else DateInput.parseLegacyLabel(item.requiredString("dueLabel")),
                projectId = item.optionalString("projectId"),
                dayCategory = item.requiredString("dayCategory"),
                priority = item.requiredString("priority"),
                isCompleted = item.requiredBoolean("isCompleted"),
                showOnHome = item.requiredBoolean("showOnHome"),
                sortOrder = item.requiredInt("sortOrder"),
                recurrence = if (formatVersion >= 4) item.requiredString("recurrence") else "NONE",
                recurrenceDays = if (formatVersion >= 4) item.requiredString("recurrenceDays") else "",
                recurrenceSeriesId = if (formatVersion >= 4) item.optionalString("recurrenceSeriesId") else null,
                recurrenceAnchorDay = if (formatVersion >= 4) item.optInt("recurrenceAnchorDay", 0) else 0,
                reminderEnabled = if (formatVersion >= 5) item.optBoolean("reminderEnabled", false) else false,
                reminderTime = if (formatVersion >= 5) item.optString("reminderTime", "09:00") else "09:00"
            ).also {
                require(it.id.isNotBlank() && it.title.isNotBlank())
                com.example.lifedashboard.data.model.TaskDayCategory.valueOf(it.dayCategory)
                com.example.lifedashboard.data.model.TaskPriority.valueOf(it.priority)
                com.example.lifedashboard.data.model.TaskRecurrence.valueOf(it.recurrence)
                require(it.recurrenceDays.split(',').filter(String::isNotBlank).all { day -> day.toIntOrNull()?.let { value -> value in 1..7 } == true })
                require(it.recurrence == "WEEKLY" || it.recurrenceDays.isBlank())
                require(it.recurrence == "NONE" || it.dueDate != null) { "Для повторяющейся задачи нужна дата." }
                require(it.recurrence == "NONE" || !it.recurrenceSeriesId.isNullOrBlank()) { "У повторяющейся задачи отсутствует идентификатор серии." }
                require(it.recurrence != "WEEKLY" || it.recurrenceDays.split(',').any { day -> day.toIntOrNull()?.let { value -> value in 1..7 } == true }) { "Для еженедельного повтора выбери дни." }
                require(it.recurrenceAnchorDay in 0..31)
                require(it.reminderTime.matches(Regex("(?:[01]\\d|2[0-3]):[0-5]\\d")))
                require(!it.reminderEnabled || it.dueDate != null) { "Для напоминания задачи нужна дата." }
                require(it.recurrence != "MONTHLY" || it.recurrenceAnchorDay in 1..31 || it.dueDate != null)
            }
        }.uniqueBy("id") { it.id }

        val projects = data.requiredArray("projects").objects { item ->
            ProjectEntity(
                id = item.requiredString("id"),
                name = item.requiredString("name"),
                description = item.requiredString("description"),
                deadlineLabel = item.requiredString("deadlineLabel"),
                deadlineDate = if (formatVersion >= 3) {
                    val raw = item.optionalString("deadlineDate")
                    require(raw == null || DateInput.parseIso(raw) != null) { "Некорректная дата проекта." }
                    DateInput.parseIso(raw)
                } else DateInput.parseLegacyLabel(item.requiredString("deadlineLabel")),
                stageLabel = item.requiredString("stageLabel"),
                progress = item.requiredNumber("progress").toFloat(),
                completedTasks = item.requiredInt("completedTasks"),
                totalTasks = item.requiredInt("totalTasks"),
                colorArgb = item.requiredInt("colorArgb"),
                isCompleted = item.requiredBoolean("isCompleted")
            ).also {
                require(it.id.isNotBlank() && it.name.isNotBlank())
                require(it.progress.isFinite() && it.progress in 0f..1f)
                require(it.completedTasks >= 0 && it.totalTasks >= it.completedTasks)
            }
        }.uniqueBy("id") { it.id }
        val projectIds = projects.map { it.id }.toSet()

        val stages = data.requiredArray("projectStages").objects { item ->
            ProjectStageEntity(
                id = item.requiredString("id"),
                projectId = item.requiredString("projectId"),
                title = item.requiredString("title"),
                isCompleted = item.requiredBoolean("isCompleted"),
                isCurrent = item.requiredBoolean("isCurrent"),
                sortOrder = item.requiredInt("sortOrder")
            ).also { require(it.id.isNotBlank() && it.title.isNotBlank() && it.projectId in projectIds) }
        }.uniqueBy("id") { it.id }

        val notes = data.requiredArray("notes").objects { item ->
            NoteEntity(
                id = item.requiredString("id"),
                title = item.requiredString("title"),
                body = item.requiredString("body"),
                category = item.requiredString("category"),
                color = item.requiredString("color"),
                updatedLabel = item.requiredString("updatedLabel"),
                isPinned = item.requiredBoolean("isPinned"),
                attachmentCount = item.requiredInt("attachmentCount")
            ).also {
                require(it.id.isNotBlank() && it.attachmentCount >= 0)
                com.example.lifedashboard.data.model.NoteCategory.valueOf(it.category)
                com.example.lifedashboard.data.model.NoteColor.valueOf(it.color)
            }
        }.uniqueBy("id") { it.id }

        val habits = data.requiredArray("habits").objects { item ->
            val completionDates = item.requiredString("completionDates")
            require(completionDates.split(',').filter(String::isNotBlank).all { it.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) })
            val reminderTime = item.requiredString("reminderTime")
            require(reminderTime.matches(Regex("(?:[01]\\d|2[0-3]):[0-5]\\d")))
            val reminderDays = item.requiredString("reminderDays")
            require(reminderDays.split(',').all { value -> value.toIntOrNull()?.let { it in 1..7 } == true })
            HabitEntity(
                id = item.requiredString("id"),
                title = item.requiredString("title"),
                completionDates = completionDates,
                color = item.requiredString("color"),
                reminderLabel = item.requiredString("reminderLabel"),
                reminderEnabled = item.requiredBoolean("reminderEnabled"),
                reminderTime = reminderTime,
                reminderDays = reminderDays
            ).also {
                require(it.id.isNotBlank() && it.title.isNotBlank())
                com.example.lifedashboard.data.model.HabitColor.valueOf(it.color)
            }
        }.uniqueBy("id") { it.id }

        val goals = if (formatVersion == 1) {
            emptyList()
        } else {
            data.requiredArray("goals").objects { item ->
                GoalEntity(
                    id = item.requiredString("id"),
                    title = item.requiredString("title"),
                    description = item.requiredString("description"),
                    dueDate = item.optionalString("dueDate")?.let { raw ->
                        DateInput.parseIso(raw) ?: throw IllegalArgumentException("Некорректная дата цели.")
                    },
                    currentValue = item.requiredInt("currentValue"),
                    targetValue = item.requiredInt("targetValue"),
                    unit = item.requiredString("unit"),
                    projectId = item.optionalString("projectId"),
                    color = item.requiredString("color"),
                    sortOrder = item.requiredInt("sortOrder")
                ).also { goal ->
                    require(goal.id.isNotBlank() && goal.title.isNotBlank())
                    require(goal.targetValue in 1..1_000_000 && goal.currentValue in 0..goal.targetValue)
                    require(goal.unit.isNotBlank() && goal.unit.length <= 24)
                                        require(goal.projectId == null || goal.projectId in projectIds)
                    com.example.lifedashboard.data.model.GoalColor.valueOf(goal.color)
                }
            }.uniqueBy("id") { it.id }
        }

        val events = data.requiredArray("activityEvents").objects { item ->
            ActivityEventEntity(
                id = item.requiredString("id"),
                type = item.requiredString("type"),
                itemId = item.requiredString("itemId"),
                occurredAtEpochMillis = item.requiredLong("occurredAtEpochMillis"),
                completed = item.requiredBoolean("completed")
            ).also { require(it.id.isNotBlank() && it.occurredAtEpochMillis > 0L) }
        }.uniqueBy("id") { it.id }

        val metadata = data.requiredArray("appMetadata").objects { item ->
            AppMetadataEntity(item.requiredString("key"), item.requiredString("value"))
                .also { require(it.key.isNotBlank()) }
        }.uniqueBy("key") { it.key }

        tasks.forEach { task -> require(task.projectId == null || task.projectId in projectIds) }
        val settings = if (type == TYPE_FULL_BACKUP) root.requiredObject("settings").toSettings() else null
        return Snapshot(tasks, projects, stages, notes, habits, goals, events, metadata, settings)
    }

    private fun TaskEntity.toJson() = JSONObject()
        .put("id", id).put("title", title).put("trailingLabel", trailingLabel ?: JSONObject.NULL)
        .put("dueLabel", dueLabel).put("dueDate", dueDate?.toString() ?: JSONObject.NULL)
        .put("projectId", projectId ?: JSONObject.NULL)
        .put("dayCategory", dayCategory).put("priority", priority).put("isCompleted", isCompleted)
        .put("showOnHome", showOnHome).put("sortOrder", sortOrder)
        .put("recurrence", recurrence).put("recurrenceDays", recurrenceDays)
        .put("recurrenceSeriesId", recurrenceSeriesId ?: JSONObject.NULL)
        .put("recurrenceAnchorDay", recurrenceAnchorDay)
        .put("reminderEnabled", reminderEnabled).put("reminderTime", reminderTime)

    private fun ProjectEntity.toJson() = JSONObject()
        .put("id", id).put("name", name).put("description", description)
        .put("deadlineLabel", deadlineLabel).put("deadlineDate", deadlineDate?.toString() ?: JSONObject.NULL)
        .put("stageLabel", stageLabel).put("progress", progress.toDouble())
        .put("completedTasks", completedTasks).put("totalTasks", totalTasks).put("colorArgb", colorArgb)
        .put("isCompleted", isCompleted)

    private fun ProjectStageEntity.toJson() = JSONObject()
        .put("id", id).put("projectId", projectId).put("title", title)
        .put("isCompleted", isCompleted).put("isCurrent", isCurrent).put("sortOrder", sortOrder)

    private fun NoteEntity.toJson() = JSONObject()
        .put("id", id).put("title", title).put("body", body).put("category", category)
        .put("color", color).put("updatedLabel", updatedLabel).put("isPinned", isPinned)
        .put("attachmentCount", attachmentCount)

    private fun HabitEntity.toJson() = JSONObject()
        .put("id", id).put("title", title).put("completionDates", completionDates)
        .put("color", color).put("reminderLabel", reminderLabel).put("reminderEnabled", reminderEnabled)
        .put("reminderTime", reminderTime).put("reminderDays", reminderDays)

    private fun GoalEntity.toJson() = JSONObject()
        .put("id", id).put("title", title).put("description", description)
        .put("dueDate", dueDate?.toString() ?: JSONObject.NULL).put("currentValue", currentValue)
        .put("targetValue", targetValue).put("unit", unit)
        .put("projectId", projectId ?: JSONObject.NULL).put("color", color).put("sortOrder", sortOrder)

    private fun ActivityEventEntity.toJson() = JSONObject()
        .put("id", id).put("type", type).put("itemId", itemId)
        .put("occurredAtEpochMillis", occurredAtEpochMillis).put("completed", completed)

    private fun AppMetadataEntity.toJson() = JSONObject().put("key", key).put("value", value)

    private fun DashboardSettings.toJson() = JSONObject()
        .put("themeMode", themeMode.name)
        .put("notificationsEnabled", notificationsEnabled)
        .put("showCompletedTasksOnHome", showCompletedTasksOnHome)

    private fun JSONObject.toSettings() = DashboardSettings(
        themeMode = DashboardThemeMode.valueOf(requiredString("themeMode")),
        notificationsEnabled = requiredBoolean("notificationsEnabled"),
        showCompletedTasksOnHome = requiredBoolean("showCompletedTasksOnHome")
    )

    private fun <T> JSONArray.objects(parse: (JSONObject) -> T): List<T> =
        (0 until length()).map { index ->
            val value = optJSONObject(index) ?: throw IllegalArgumentException("В массиве обнаружена некорректная запись.")
            parse(value)
        }

    private fun <T> List<T>.uniqueBy(label: String, key: (T) -> String): List<T> {
        require(map(key).toSet().size == size) { "В файле обнаружены повторяющиеся идентификаторы ($label)." }
        return this
    }

    private fun JSONObject.requiredString(key: String): String {
        val value = opt(key)
        require(value is String) { "Поле '$key' отсутствует или имеет неверный тип." }
        return value
    }

    private fun JSONObject.optionalString(key: String): String? {
        val value = opt(key)
        if (value == null || value === JSONObject.NULL) return null
        require(value is String) { "Поле '$key' имеет неверный тип." }
        return value
    }

    private fun JSONObject.requiredBoolean(key: String): Boolean {
        val value = opt(key)
        require(value is Boolean) { "Поле '$key' отсутствует или имеет неверный тип." }
        return value
    }

    private fun JSONObject.requiredNumber(key: String): Double {
        val value = opt(key)
        require(value is Number) { "Поле '$key' отсутствует или имеет неверный тип." }
        return value.toDouble().also { require(it.isFinite()) }
    }

    private fun JSONObject.requiredInt(key: String): Int {
        val value = requiredNumber(key)
        require(value % 1.0 == 0.0 && value >= Int.MIN_VALUE && value <= Int.MAX_VALUE) {
            "Поле '$key' имеет неверное числовое значение."
        }
        return value.toInt()
    }

    private fun JSONObject.requiredLong(key: String): Long {
        val value = requiredNumber(key)
        require(value % 1.0 == 0.0 && value >= Long.MIN_VALUE.toDouble() && value <= Long.MAX_VALUE.toDouble())
        return value.toLong()
    }

    private fun JSONObject.requiredArray(key: String): JSONArray =
        optJSONArray(key) ?: throw IllegalArgumentException("В файле отсутствует массив '$key'.")

    private fun JSONObject.requiredObject(key: String): JSONObject =
        optJSONObject(key) ?: throw IllegalArgumentException("В файле отсутствует объект '$key'.")

    private companion object {
        const val APP_ID = "com.example.lifedashboard"
        const val FORMAT_VERSION = 5
        const val MIN_SUPPORTED_FORMAT_VERSION = 1
        const val TYPE_FULL_BACKUP = "LIFE_DASHBOARD_BACKUP"
        const val TYPE_DATA_EXPORT = "LIFE_DASHBOARD_EXPORT"
        const val SEED_MARKER = "starter_seed_v1"
        const val MAX_BACKUP_BYTES = 20 * 1024 * 1024
    }
}
