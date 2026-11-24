package com.example.lifedashboard.data

import com.example.lifedashboard.data.model.DashboardHabit

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

    /** Percentage of elapsed days in this month with at least one habit completed. */
    fun monthlyActivityPercent(habits: List<DashboardHabit>): Int {
        val elapsedDays = DateKeys.currentMonthElapsedKeys()
        if (elapsedDays.isEmpty() || habits.isEmpty()) return 0
        val activeDays = elapsedDays.count { key -> habits.any { key in it.completionDates } }
        return (activeDays * 100 / elapsedDays.size).coerceIn(0, 100)
    }

    fun completedHabitCount(habits: List<DashboardHabit>, dateKey: String): Int =
        habits.count { dateKey in it.completionDates }
}
