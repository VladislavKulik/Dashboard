package com.example.lifedashboard.data

import android.content.Context
import com.example.lifedashboard.R
import com.example.lifedashboard.data.model.TaskDayCategory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.util.Locale

/** Strict parsing/formatting for user-entered dates and labels from older app versions. */
object DateInput {
    private val displayFormatter = DateTimeFormatter.ofPattern("dd.MM.uuuu", Locale.ROOT).withResolverStyle(ResolverStyle.STRICT)
    private val shortFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("ru"))

    fun parseIso(value: String?): LocalDate? {
        if (value.isNullOrBlank()) return null
        return runCatching { LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull()
    }

    fun parseUserInput(value: String): LocalDate? {
        val clean = value.trim()
        if (clean.isEmpty()) return null
        parseIso(clean)?.let { return it }
        return try { LocalDate.parse(clean, displayFormatter) } catch (_: DateTimeParseException) { null }
    }

    /** Parses historical labels, preserving the original text separately for display. */
    fun parseLegacyLabel(value: String?, today: LocalDate = LocalDate.now()): LocalDate? {
        val clean = value?.trim()?.lowercase(Locale.forLanguageTag("ru")) ?: return null
        if (clean.isBlank() || clean in setOf("без срока", "готово", "не указано", "—")) return null
        parseUserInput(clean)?.let { return it }
        when (clean) {
            "сегодня" -> return today
            "завтра" -> return today.plusDays(1)
            "послезавтра" -> return today.plusDays(2)
            "вчера" -> return today.minusDays(1)
        }
        Regex("через\\s+(\\d+)\\s+д(?:ень|ня|ней)").matchEntire(clean)?.let {
            return today.plusDays(it.groupValues[1].toLong())
        }
        val withoutTime = clean.substringBefore('·').trim()
        if (withoutTime != clean) parseLegacyLabel(withoutTime, today)?.let { return it }
        val numeric = Regex("(\\d{1,2})[.\\-/](\\d{1,2})").matchEntire(clean)
        if (numeric != null) {
            val day = numeric.groupValues[1].toIntOrNull() ?: return null
            val month = numeric.groupValues[2].toIntOrNull() ?: return null
            return nearestYearDate(today, month, day)
        }
        val match = Regex("(\\d{1,2})\\s+([а-яё]+\\.?)(?:\\s+(\\d{4}))?").matchEntire(clean) ?: return null
        val day = match.groupValues[1].toIntOrNull() ?: return null
        val month = russianMonth(match.groupValues[2]) ?: return null
        val explicitYear = match.groupValues[3].toIntOrNull()
        return if (explicitYear != null) runCatching { LocalDate.of(explicitYear, month, day) }.getOrNull()
        else nearestYearDate(today, month, day)
    }

    fun formatForInput(date: LocalDate?): String = date?.format(displayFormatter).orEmpty()

    fun formatShort(date: LocalDate?): String =
        date?.format(shortFormatter)?.replaceFirstChar { it.uppercase(Locale.forLanguageTag("ru")) }.orEmpty()

    fun formatTaskLabel(context: Context, date: LocalDate?): String {
        if (date == null) return context.getString(R.string.date_no_deadline)
        val today = LocalDate.now()
        return when (date) {
            today -> context.getString(R.string.date_today)
            today.plusDays(1) -> context.getString(R.string.date_tomorrow)
            today.plusDays(2) -> context.getString(R.string.date_day_after_tomorrow)
            else -> date.format(shortFormatter)
        }
    }

    fun categoryFor(date: LocalDate?): TaskDayCategory? = when {
        date == null -> null
        !date.isAfter(LocalDate.now()) -> TaskDayCategory.TODAY
        else -> TaskDayCategory.UPCOMING
    }

    private fun nearestYearDate(today: LocalDate, month: Int, day: Int): LocalDate? = runCatching {
        val candidate = LocalDate.of(today.year, month, day)
        when {
            candidate.isBefore(today.minusDays(180)) -> candidate.plusYears(1)
            candidate.isAfter(today.plusDays(180)) -> candidate.minusYears(1)
            else -> candidate
        }
    }.getOrNull()

    private fun russianMonth(value: String): Int? = when (value.trimEnd('.')) {
        "янв", "январь", "января" -> 1
        "фев", "февр", "февраль", "февраля" -> 2
        "мар", "март", "марта" -> 3
        "апр", "апрель", "апреля" -> 4
        "май", "мая" -> 5
        "июн", "июнь", "июня" -> 6
        "июл", "июль", "июля" -> 7
        "авг", "август", "августа" -> 8
        "сен", "сент", "сентябрь", "сентября" -> 9
        "окт", "октябрь", "октября" -> 10
        "ноя", "нояб", "ноябрь", "ноября" -> 11
        "дек", "декабрь", "декабря" -> 12
        else -> null
    }
}