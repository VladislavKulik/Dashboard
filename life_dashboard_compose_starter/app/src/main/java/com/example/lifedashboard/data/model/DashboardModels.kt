package com.example.lifedashboard.data.model

import java.time.LocalDate

/**
 * UI-facing models for the starter build. They are deliberately small and
 * can later be mapped to Room entities without tying the UI to the database.
 */
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
    /** ISO weekday numbers: Monday=1 … Sunday=7. Used by WEEKLY recurrence. */
    val recurrenceDays: Set<Int> = emptySet(),
    val recurrenceSeriesId: String? = null,
    /** Original day-of-month, so Jan 31 → Feb 28 → Mar 31 does not drift. */
    val recurrenceAnchorDay: Int = 0,
    val reminderEnabled: Boolean = false,
    val reminderTime: String = "09:00"
)

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
    val stageLabel: String,
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
    val attachmentCount: Int = 0
)


enum class HabitColor {
    PINK,
    BLUE,
    YELLOW,
    MINT,
    LAVENDER
}

/** A habit's completion history is stored as local yyyy-MM-dd date keys. */
data class DashboardHabit(
    val id: String,
    val title: String,
    val completionDates: Set<String> = emptySet(),
    val color: HabitColor = HabitColor.PINK,
    val reminderLabel: String = "Не настроено",
    val reminderEnabled: Boolean = false,
    val reminderTime: String = "09:00",
    /** ISO weekday numbers: Monday=1 … Sunday=7. */
    val reminderDays: Set<Int> = (1..7).toSet()
)

/** A persisted user action used to build real activity analytics. */
data class DashboardActivityEvent(
    val id: String,
    val type: String,
    val itemId: String,
    val occurredAtEpochMillis: Long,
    val completed: Boolean
)



/** A measurable personal objective; dates are stored as ISO yyyy-MM-dd keys. */
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
