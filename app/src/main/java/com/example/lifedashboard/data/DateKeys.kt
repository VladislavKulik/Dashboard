package com.example.lifedashboard.data

import java.text.SimpleDateFormat
import java.text.ParsePosition
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Local-date helpers used by the in-memory habit demo store and its UI.
 * Keys are generated in the device's local time zone (yyyy-MM-dd).
 */
object DateKeys {
    private const val KEY_PATTERN = "yyyy-MM-dd"
    private val keyLocale = Locale.US
    private val labelLocale = Locale.forLanguageTag("ru")

    fun today(): String = keyForDaysAgo(0)

    /** Strictly validates a yyyy-MM-dd key without relying on newer java.time APIs. */
    fun isValidDateKey(key: String): Boolean {
        if (!key.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) return false
        val formatter = SimpleDateFormat(KEY_PATTERN, keyLocale).apply { isLenient = false }
        val position = ParsePosition(0)
        val parsed = formatter.parse(key, position)
        return parsed != null && position.index == key.length
    }

    fun keyForDaysAgo(daysAgo: Int): String {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -daysAgo)
        }
        return SimpleDateFormat(KEY_PATTERN, keyLocale).format(calendar.time)
    }

    /** Oldest to newest, with today as the final item. */
    fun lastDays(count: Int): List<String> =
        (count - 1 downTo 0).map(::keyForDaysAgo)

    fun weekdayLabel(key: String): String {
        val date = parseKey(key) ?: return ""
        return SimpleDateFormat("EE", labelLocale).format(date)
            .replaceFirstChar { it.uppercase(labelLocale) }
    }

    fun dayOfMonthLabel(key: String): String {
        val date = parseKey(key) ?: return ""
        return SimpleDateFormat("d", keyLocale).format(date)
    }

    fun currentMonthTitle(): String =
        SimpleDateFormat("LLLL yyyy", labelLocale).format(Calendar.getInstance().time)
            .replaceFirstChar { it.uppercase(labelLocale) }

    fun currentMonthGrid(): List<String?> {
        val calendar = Calendar.getInstance()
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val mondayFirstOffset = (calendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7

        val cells = mutableListOf<String?>()
        repeat(mondayFirstOffset) { cells += null }
        for (day in 1..daysInMonth) {
            calendar.set(Calendar.DAY_OF_MONTH, day)
            cells += SimpleDateFormat(KEY_PATTERN, keyLocale).format(calendar.time)
        }
        while (cells.size % 7 != 0) cells += null
        return cells
    }

    fun currentMonthElapsedKeys(): List<String> {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_MONTH)
        return (1 until today).map { day ->
            calendar.set(Calendar.DAY_OF_MONTH, day)
            SimpleDateFormat(KEY_PATTERN, keyLocale).format(calendar.time)
        }
    }

    private fun parseKey(key: String): Date? =
        runCatching { SimpleDateFormat(KEY_PATTERN, keyLocale).parse(key) }.getOrNull()
}
