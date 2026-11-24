package com.example.lifedashboard.feature.calendar

import com.example.lifedashboard.data.model.DashboardActivityEvent
import com.example.lifedashboard.data.model.DashboardGoal
import com.example.lifedashboard.data.model.DashboardHabit
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.data.model.DashboardTask
import com.example.lifedashboard.data.model.TaskDayCategory
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Event types are derived from existing records; the calendar does not duplicate persisted data. */
enum class CalendarEventType(val label: String) {
    TASK("Задача"),
    PROJECT("Проект"),
    GOAL("Цель"),
    HABIT("Привычка"),
    ACTIVITY("Активность")
}

data class CalendarEvent(
    val id: String,
    val dateKey: String,
    val type: CalendarEventType,
    val title: String,
    val subtitle: String,
    val relatedId: String,
    val projectId: String? = null,
    val completed: Boolean = false,
    val timeLabel: String? = null
)

/** Builds a read-only calendar projection from current repository state. */
object CalendarEventFactory {
    fun build(
        tasks: List<DashboardTask>,
        projects: List<DashboardProject>,
        goals: List<DashboardGoal>,
        habits: List<DashboardHabit>,
        activityEvents: List<DashboardActivityEvent>,
        nowMillis: Long = System.currentTimeMillis()
    ): List<CalendarEvent> {
        val result = mutableListOf<CalendarEvent>()

        tasks.forEach { task ->
            val parsedDate = task.dueDate?.toString() ?: parseDateLabel(task.dueLabel, nowMillis)
            if (parsedDate != null) {
                result += CalendarEvent(
                    id = "task:${task.id}", dateKey = parsedDate, type = CalendarEventType.TASK,
                    title = task.title,
                    subtitle = if (task.isCompleted) "Выполнена" else "Срок выполнения",
                    relatedId = task.id, projectId = task.projectId, completed = task.isCompleted,
                    timeLabel = task.reminderTime.takeIf { task.reminderEnabled }
                )
            } else if (task.dueLabel.isBlank() && task.dayCategory == TaskDayCategory.TODAY) {
                result += CalendarEvent("task:${task.id}", todayKey(nowMillis), CalendarEventType.TASK,
                    task.title, if (task.isCompleted) "Выполнена" else "На сегодня", task.id,
                    task.projectId, task.isCompleted, task.reminderTime.takeIf { task.reminderEnabled })
            }
        }

        projects.filterNot { it.isCompleted }.forEach { project ->
            (project.deadlineDate?.toString() ?: parseDateLabel(project.deadlineLabel, nowMillis))?.let { date ->
                result += CalendarEvent(
                    id = "project:${project.id}", dateKey = date, type = CalendarEventType.PROJECT,
                    title = project.name, subtitle = "Срок проекта", relatedId = project.id
                )
            }
        }

        goals.forEach { goal ->
            goal.dueDate?.toString()?.takeIf(::isValidKey)?.let { date ->
                result += CalendarEvent(
                    id = "goal:${goal.id}", dateKey = date, type = CalendarEventType.GOAL,
                    title = goal.title,
                    subtitle = if (goal.isCompleted) "Цель достигнута" else "Срок цели",
                    relatedId = goal.id, projectId = goal.projectId, completed = goal.isCompleted
                )
            }
        }

        habits.forEach { habit ->
            habit.completionDates.forEach { date ->
                if (isValidKey(date)) {
                    result += CalendarEvent(
                        id = "habit:${habit.id}:$date", dateKey = date, type = CalendarEventType.HABIT,
                        title = habit.title, subtitle = "Привычка выполнена", relatedId = habit.id,
                        completed = true
                    )
                }
            }
        }

        val taskNames = tasks.associate { it.id to it.title }
        activityEvents.forEach { event ->
            val date = dateKeyForMillis(event.occurredAtEpochMillis) ?: return@forEach
            val action = if (event.completed) "Выполнено" else "Возвращено в работу"
            val name = taskNames[event.itemId]
            result += CalendarEvent(
                id = "activity:${event.id}", dateKey = date, type = CalendarEventType.ACTIVITY,
                title = name ?: "Изменение задачи",
                subtitle = action, relatedId = event.itemId, completed = event.completed
            )
        }

        return result.sortedWith(
            compareBy<CalendarEvent> { it.dateKey }
                .thenBy { eventOrder(it.type) }
                .thenBy { it.timeLabel ?: "99:99" }
                .thenBy { it.title.lowercase(Locale.getDefault()) }
        )
    }

    /** Handles ISO dates, Russian relative labels, numeric dates, and labels like "27 ноя". */
    fun parseDateLabel(label: String, nowMillis: Long = System.currentTimeMillis()): String? {
        val value = label.trim().lowercase(Locale.forLanguageTag("ru"))
        if (value.isBlank() || value in setOf("без срока", "без даты", "готово", "—", "-")) return null
        if (isValidKey(value)) return value

        val today = Calendar.getInstance().apply { timeInMillis = nowMillis }
        when (value) {
            "сегодня" -> return todayKey(nowMillis)
            "завтра" -> return keyWithOffset(nowMillis, 1)
            "послезавтра" -> return keyWithOffset(nowMillis, 2)
            "вчера" -> return keyWithOffset(nowMillis, -1)
        }
        Regex("через\\s+(\\d+)\\s+д(?:ень|ня|ней)").matchEntire(value)?.let { match ->
            val days = match.groupValues[1].toIntOrNull()?.coerceAtMost(3650) ?: return null
            return keyWithOffset(nowMillis, days)
        }

        parseExact(value, listOf("dd.MM.yyyy", "d.M.yyyy", "dd/MM/yyyy", "d/M/yyyy"))?.let { return it }
        parseDayMonthNumeric(value)?.let { return it }
        parseRussianMonth(value, today)?.let { return it }
        return null
    }

    fun monthGrid(year: Int, monthZeroBased: Int): List<String?> {
        val calendar = Calendar.getInstance().apply {
            clear()
            set(year, monthZeroBased, 1)
        }
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val offset = (calendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7
        val cells = mutableListOf<String?>()
        repeat(offset) { cells += null }
        for (day in 1..daysInMonth) {
            calendar.set(Calendar.DAY_OF_MONTH, day)
            cells += formatKey(calendar.time)
        }
        while (cells.size % 7 != 0) cells += null
        return cells
    }

    fun todayKey(nowMillis: Long = System.currentTimeMillis()): String =
        formatKey(Date(nowMillis))

    fun formatDayTitle(dateKey: String): String = runCatching {
        val date = parseKey(dateKey) ?: return ""
        SimpleDateFormat("d MMMM", Locale.forLanguageTag("ru")).format(date)
            .replaceFirstChar { it.uppercase(Locale.forLanguageTag("ru")) }
    }.getOrDefault("")

    fun formatMonthTitle(year: Int, monthZeroBased: Int): String {
        val calendar = Calendar.getInstance().apply { clear(); set(year, monthZeroBased, 1) }
        return SimpleDateFormat("LLLL yyyy", Locale.forLanguageTag("ru")).format(calendar.time)
            .replaceFirstChar { it.uppercase(Locale.forLanguageTag("ru")) }
    }

    private fun parseExact(value: String, patterns: List<String>): String? {
        patterns.forEach { pattern ->
            val formatter = SimpleDateFormat(pattern, Locale.US).apply { isLenient = false }
            val position = ParsePosition(0)
            val date = formatter.parse(value, position)
            if (date != null && position.index == value.length) return formatKey(date)
        }
        return null
    }

    private fun parseDayMonthNumeric(value: String): String? {
        val match = Regex("^(\\d{1,2})[.\\-/](\\d{1,2})$").matchEntire(value) ?: return null
        val day = match.groupValues[1].toIntOrNull() ?: return null
        val month = match.groupValues[2].toIntOrNull()?.minus(1) ?: return null
        return nearestYearKey(day, month)
    }

    private fun parseRussianMonth(value: String, reference: Calendar): String? {
        val match = Regex("^(\\d{1,2})\\s+([а-яё]+\\.?)(?:\\s+(\\d{4}))?$").matchEntire(value) ?: return null
        val day = match.groupValues[1].toIntOrNull() ?: return null
        val monthToken = match.groupValues[2]
        val explicitYear = match.groupValues[3].toIntOrNull()
        val month = monthIndex(monthToken) ?: return null
        if (explicitYear != null) return makeKey(explicitYear, month, day)
        return nearestYearKey(day, month, reference)
    }

    private fun monthIndex(token: String): Int? {
        val normalized = token.trim().removeSuffix(".")
        val aliases = mapOf(
            0 to setOf("январь", "января", "янв", "январе"),
            1 to setOf("февраль", "февраля", "фев", "февр"),
            2 to setOf("март", "марта", "мар", "марте"),
            3 to setOf("апрель", "апреля", "апр", "апреле"),
            4 to setOf("май", "мая", "мае"),
            5 to setOf("июнь", "июня", "июн", "июне"),
            6 to setOf("июль", "июля", "июл", "июле"),
            7 to setOf("август", "августа", "авг", "августе"),
            8 to setOf("сентябрь", "сентября", "сен", "сент", "сентябре"),
            9 to setOf("октябрь", "октября", "окт", "октябре"),
            10 to setOf("ноябрь", "ноября", "ноя", "нояб", "ноябре"),
            11 to setOf("декабрь", "декабря", "дек", "декабре")
        )
        return aliases.entries.firstOrNull { normalized in it.value }?.key
    }

    private fun nearestYearKey(day: Int, month: Int, reference: Calendar = Calendar.getInstance()): String? {
        val year = reference.get(Calendar.YEAR)
        val candidate = makeDate(year, month, day) ?: return null
        val referenceDate = (reference.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val difference = ((candidate.time - referenceDate) / MILLIS_PER_DAY).toInt()
        val resolvedYear = when {
            difference < -180 -> year + 1
            difference > 180 -> year - 1
            else -> year
        }
        return makeKey(resolvedYear, month, day)
    }

    private fun makeKey(year: Int, month: Int, day: Int): String? =
        makeDate(year, month, day)?.let(::formatKey)

    private fun makeDate(year: Int, month: Int, day: Int): Date? {
        val calendar = Calendar.getInstance().apply {
            isLenient = false
            clear()
            set(year, month, day, 12, 0, 0)
        }
        return runCatching { calendar.time }.getOrNull()
    }

    private fun keyWithOffset(nowMillis: Long, days: Int): String = Calendar.getInstance().apply {
        timeInMillis = nowMillis
        add(Calendar.DAY_OF_YEAR, days)
    }.let { formatKey(it.time) }

    private fun dateKeyForMillis(millis: Long): String? =
        if (millis <= 0L) null else formatKey(Date(millis))

    private fun isValidKey(key: String): Boolean = parseKey(key) != null

    private fun parseKey(key: String): Date? {
        if (!key.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) return null
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
        val position = ParsePosition(0)
        return formatter.parse(key, position)?.takeIf { position.index == key.length }
    }

    private fun formatKey(date: Date): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)

    private fun eventOrder(type: CalendarEventType): Int = when (type) {
        CalendarEventType.TASK -> 0
        CalendarEventType.PROJECT -> 1
        CalendarEventType.GOAL -> 2
        CalendarEventType.HABIT -> 3
        CalendarEventType.ACTIVITY -> 4
    }

    private const val MILLIS_PER_DAY = 86_400_000L
}
