package com.example.lifedashboard.data

import com.example.lifedashboard.data.model.TaskRecurrence
import java.time.LocalDate
import java.time.YearMonth

/** Pure recurrence date & limit calculation, independent from Android and Room. */
object TaskRecurrenceCalculator {
    fun nextDate(
        completedDate: LocalDate,
        recurrence: TaskRecurrence,
        selectedDays: Set<Int> = emptySet(),
        anchorDayOfMonth: Int = 0
    ): LocalDate? = when (recurrence) {
        TaskRecurrence.NONE -> null
        TaskRecurrence.DAILY -> completedDate.plusDays(1)
        TaskRecurrence.WEEKLY -> {
            val days = selectedDays.filter { it in 1..7 }.toSet()
                .ifEmpty { setOf(completedDate.dayOfWeek.value) }
            (1L..14L).asSequence()
                .map(completedDate::plusDays)
                .firstOrNull { it.dayOfWeek.value in days }
        }
        TaskRecurrence.MONTHLY -> {
            val targetMonth = YearMonth.from(completedDate).plusMonths(1)
            val day = (anchorDayOfMonth.takeIf { it in 1..31 } ?: completedDate.dayOfMonth)
                .coerceAtMost(targetMonth.lengthOfMonth())
            targetMonth.atDay(day)
        }
    }

    fun nextRemainingCount(currentRemaining: Int?): Int? =
        currentRemaining?.let { (it - 1).coerceAtLeast(0) }

    fun canCreateNext(
        nextDate: LocalDate?,
        endDate: LocalDate?,
        nextRemainingCount: Int?
    ): Boolean {
        if (nextDate == null) return false
        if (endDate != null && nextDate.isAfter(endDate)) return false
        if (nextRemainingCount != null && nextRemainingCount < 1) return false
        return true
    }
}