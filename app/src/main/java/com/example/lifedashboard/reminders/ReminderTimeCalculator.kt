package com.example.lifedashboard.reminders

import java.util.Calendar
import java.util.TimeZone

/** Pure calendar calculation, separated from Android AlarmManager for deterministic tests. */
object ReminderTimeCalculator {
    private val timePattern = Regex("(?:[01]\\d|2[0-3]):[0-5]\\d")

    /** Returns the next local-time occurrence strictly after now, or null for invalid input. */
    fun nextTrigger(
        nowMillis: Long,
        time: String,
        isoDays: Set<Int>,
        timeZone: TimeZone = TimeZone.getDefault()
    ): Long? {
        if (!timePattern.matches(time)) return null
        val validDays = isoDays.filter { it in 1..7 }.toSet()
        if (validDays.isEmpty()) return null

        val parts = time.split(':')
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()
        val now = Calendar.getInstance(timeZone).apply { timeInMillis = nowMillis }

        for (offset in 0..7) {
            val candidate = (now.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, offset)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val isoWeekday = when (candidate.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY -> 4
                Calendar.FRIDAY -> 5
                Calendar.SATURDAY -> 6
                else -> 7
            }
            if (isoWeekday in validDays && candidate.timeInMillis > nowMillis) {
                return candidate.timeInMillis
            }
        }
        return null
    }
}
