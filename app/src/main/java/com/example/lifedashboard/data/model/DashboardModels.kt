package com.example.lifedashboard.data.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class TaskDayCategory {
    TODAY,
    UPCOMING
}

enum class TaskRecurrence(val label: String) {
    NONE("Не повторять"),
    DAILY("Каждый день"),
    WEEKLY("Каждую неделю"),
    MONTHLY("Каждый месяц")
}

enum class TaskPriority(val label: String) {
    HIGH("Высокий"),
    MEDIUM("Средний"),
    LOW("Низкий")
}

data class DashboardTask(
    val id: String,
    val title: String,
    val trailingLabel: String? = null,
    val dueLabel: String,
    val projectId: String? = null,
    val dayCategory: TaskDayCategory = TaskDayCategory.TODAY,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val isCompleted: Boolean = false,
    val showOnHome: Boolean = false,
    val dueDate: LocalDate? = null,
    val recurrence: TaskRecurrence = TaskRecurrence.NONE,
    val recurrenceDays: Set<Int> = emptySet(),
    val recurrenceSeriesId: String? = null,
    val recurrenceAnchorDay: Int = 0,
    val reminderEnabled: Boolean = false,
    val reminderTime: String = "09:00",
    val recurrenceEndDate: LocalDate? = null,
    val recurrenceRemainingCount: Int? = null,
    val plannedMinutes: Int = 120
)

data class TaskSubtask(
    val id: String,
    val taskId: String,
    val title: String,
    val isCompleted: Boolean = false,
    val sortOrder: Int = 0
)

data class TaskTimerSession(
    val id: String,
    val taskId: String,
    val projectId: String?,
    val startedAtMillis: Long,
    val endedAtMillis: Long,
    val durationSeconds: Long
) {
    fun formattedRange(): String {
        val zone = ZoneId.systemDefault()
        val timeFmt = DateTimeFormatter.ofPattern("HH:mm", Locale.forLanguageTag("ru"))
        val dayFmt = DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("ru"))
        val startZoned = Instant.ofEpochMilli(startedAtMillis).atZone(zone)
        val endZoned = Instant.ofEpochMilli(endedAtMillis).atZone(zone)
        val dayLabel = if (startZoned.toLocalDate() == LocalDate.now()) {
            "Сегодня"
        } else {
            startZoned.format(dayFmt)
        }
        return "$dayLabel ${startZoned.format(timeFmt)}–${endZoned.format(timeFmt)}"
    }
}

data class ProjectStage(
    val id: String,
    val title: String,
    val isCompleted: Boolean = false,
    val isCurrent: Boolean = false
)

data class DashboardProject(
    val id: String,
    val name: String,
    val description: String,
    val deadlineLabel: String,
    val stageLabel: String = "",
    val progress: Float,
    val completedTasks: Int,
    val totalTasks: Int,
    val colorArgb: Int,
    val isCompleted: Boolean = false,
    val stages: List<ProjectStage> = emptyList(),
    val deadlineDate: LocalDate? = null
)

enum class NoteCategory(val label: String) {
    APP("Приложение"),
    BLOG("Блог"),
    PERSONAL("Личное"),
    IDEAS("Идеи")
}

enum class NoteColor {
    YELLOW,
    PINK,
    MINT,
    LAVENDER,
    BLUE
}

data class DashboardNote(
    val id: String,
    val title: String,
    val body: String,
    val category: NoteCategory,
    val color: NoteColor = NoteColor.YELLOW,
    val updatedLabel: String = "Только что",
    val isPinned: Boolean = false,
    val attachmentCount: Int = 0,
    val projectId: String? = null
)

data class NoteAttachment(
    val id: String,
    val noteId: String,
    val displayName: String,
    val mimeType: String,
    val localPath: String,
    val sizeBytes: Long,
    val createdAtEpochMillis: Long
) {
    val isImage: Boolean get() = mimeType.startsWith("image/", ignoreCase = true)
}

enum class HabitColor {
    PINK,
    BLUE,
    YELLOW,
    MINT,
    LAVENDER
}

data class DashboardHabit(
    val id: String,
    val title: String,
    val completionDates: Set<String> = emptySet(),
    val color: HabitColor = HabitColor.PINK,
    val reminderLabel: String = "Не настроено",
    val reminderEnabled: Boolean = false,
    val reminderTime: String = "09:00",
    val reminderDays: Set<Int> = (1..7).toSet()
)

data class DashboardActivityEvent(
    val id: String,
    val type: String,
    val itemId: String,
    val occurredAtEpochMillis: Long,
    val completed: Boolean
)

enum class GoalColor {
    BLUE, MINT, YELLOW, PINK, LAVENDER
}

data class DashboardGoal(
    val id: String,
    val title: String,
    val description: String = "",
    val dueDate: LocalDate? = null,
    val currentValue: Int = 0,
    val targetValue: Int = 1,
    val unit: String = "раз",
    val projectId: String? = null,
    val color: GoalColor = GoalColor.BLUE,
    val sortOrder: Int = 0
) {
    val progress: Float get() = (currentValue.toFloat() / targetValue.coerceAtLeast(1)).coerceIn(0f, 1f)
    val isCompleted: Boolean get() = currentValue >= targetValue
}