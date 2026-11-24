package validation

import com.example.lifedashboard.reminders.ReminderTimeCalculator
import java.util.Calendar
import java.util.TimeZone

private fun utcMillis(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
    Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(year, month - 1, day, hour, minute, 0)
    }.timeInMillis

fun main() {
    val utc = TimeZone.getTimeZone("UTC")

    // Monday, 23 Nov 2026, 10:15 -> same-day 10:30.
    check(
        ReminderTimeCalculator.nextTrigger(
            utcMillis(2026, 11, 23, 10, 15), "10:30", setOf(1), utc
        ) == utcMillis(2026, 11, 23, 10, 30)
    )

    // Exactly at the configured time is not considered a future occurrence.
    check(
        ReminderTimeCalculator.nextTrigger(
            utcMillis(2026, 11, 23, 10, 30), "10:30", setOf(1), utc
        ) == utcMillis(2026, 11, 30, 10, 30)
    )

    // Sunday late evening -> next Monday morning.
    check(
        ReminderTimeCalculator.nextTrigger(
            utcMillis(2026, 11, 29, 23, 50), "00:15", setOf(1), utc
        ) == utcMillis(2026, 11, 30, 0, 15)
    )

    // Multiple weekdays choose the nearest valid day.
    check(
        ReminderTimeCalculator.nextTrigger(
            utcMillis(2026, 11, 23, 12, 0), "08:00", setOf(2, 4, 6), utc
        ) == utcMillis(2026, 11, 24, 8, 0)
    )

    check(ReminderTimeCalculator.nextTrigger(0L, "25:00", setOf(1), utc) == null)
    check(ReminderTimeCalculator.nextTrigger(0L, "09:00", emptySet(), utc) == null)

    // The wall-clock time follows the current device time zone (Kyiv is UTC+2 in November).
    val kyiv = TimeZone.getTimeZone("Europe/Kyiv")
    check(
        ReminderTimeCalculator.nextTrigger(
            utcMillis(2026, 11, 23, 8, 15), "10:30", setOf(1), kyiv
        ) == utcMillis(2026, 11, 23, 8, 30)
    )

    println("ReminderTimeCalculator: 7 checks passed")
}
