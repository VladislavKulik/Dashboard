package com.example.lifedashboard.data

import com.example.lifedashboard.data.model.TaskRecurrence
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TaskRecurrenceCalculatorTest {
    @Test fun dailyCreatesTomorrow() {
        assertEquals(LocalDate.of(2026, 11, 24), TaskRecurrenceCalculator.nextDate(LocalDate.of(2026, 11, 23), TaskRecurrence.DAILY))
    }

    @Test fun weeklyUsesNextSelectedWeekday() {
        val monday = LocalDate.of(2026, 11, 23)
        assertEquals(LocalDate.of(2026, 11, 25), TaskRecurrenceCalculator.nextDate(monday, TaskRecurrence.WEEKLY, setOf(1, 3, 5)))
        assertEquals(LocalDate.of(2026, 11, 27), TaskRecurrenceCalculator.nextDate(LocalDate.of(2026, 11, 25), TaskRecurrence.WEEKLY, setOf(1, 3, 5)))
        assertEquals(LocalDate.of(2026, 11, 30), TaskRecurrenceCalculator.nextDate(LocalDate.of(2026, 11, 27), TaskRecurrence.WEEKLY, setOf(1, 3, 5)))
    }

    @Test fun monthlyClampsShortMonthWithoutLosingOriginalAnchor() {
        assertEquals(LocalDate.of(2027, 2, 28), TaskRecurrenceCalculator.nextDate(LocalDate.of(2027, 1, 31), TaskRecurrence.MONTHLY, anchorDayOfMonth = 31))
        assertEquals(LocalDate.of(2027, 3, 31), TaskRecurrenceCalculator.nextDate(LocalDate.of(2027, 2, 28), TaskRecurrence.MONTHLY, anchorDayOfMonth = 31))
    }

    @Test fun noneDoesNotCreateNextDate() {
        assertNull(TaskRecurrenceCalculator.nextDate(LocalDate.of(2026, 11, 23), TaskRecurrence.NONE))
    }
}
