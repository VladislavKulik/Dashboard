package com.example.lifedashboard.data

import com.example.lifedashboard.data.model.DashboardHabit
import java.time.LocalDate
import java.time.YearMonth

object HabitMetrics {
    /** A streak includes today when checked; otherwise it starts from yesterday. */
    fun currentStreak(completionDates: Set<String>): Int {
        var daysAgo = if (DateKeys.today() in completionDates) 0 else 1
        var streak = 0
        while (DateKeys.keyForDaysAgo(daysAgo) in completionDates) {
            streak += 1
            daysAgo += 1
        }
        return streak
    }

    /** Longest consecutive run across all recorded completion dates. */
    fun bestStreak(completionDates: Set<String>): Int {
        if (completionDates.isEmpty()) return 0
        val sorted = completionDates
            .mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }
            .distinct()
            .sorted()
        if (sorted.isEmpty()) return 0
        var best = 1
        var current = 1
        for (i in 1 until sorted.size) {
            if (sorted[i] == sorted[i - 1].plusDays(1)) {
                current += 1
                if (current > best) best = current
            } else {
                current = 1
            }
        }
        return best
    }

    /** Percentage of elapsed days in this month with at least one habit completed. */
    fun monthlyActivityPercent(habits: List<DashboardHabit>): Int {
        val elapsedDays = DateKeys.currentMonthElapsedKeys()
        if (elapsedDays.isEmpty() || habits.isEmpty()) return 0
        val activeDays = elapsedDays.count { key -> habits.any { key in it.completionDates } }
        return (activeDays * 100 / elapsedDays.size).coerceIn(0, 100)
    }

    /** Monthly completion rate for a single habit in the given month. */
    fun habitMonthlyPercent(habit: DashboardHabit, month: YearMonth = YearMonth.now()): Int {
        val today = LocalDate.now()
        val elapsedDays = when {
            month.isAfter(YearMonth.from(today)) -> 0
            month == YearMonth.from(today) -> today.dayOfMonth
            else -> month.lengthOfMonth()
        }
        if (elapsedDays <= 0) return 0
        val completed = (1..elapsedDays).count { day ->
            month.atDay(day).toString() in habit.completionDates
        }
        return (completed * 100 / elapsedDays).coerceIn(0, 100)
    }

    fun completedHabitCount(habits: List<DashboardHabit>, dateKey: String): Int =
        habits.count { dateKey in it.completionDates }
}