package com.example.lifedashboard.data.local

import androidx.room.TypeConverter
import java.time.LocalDate

/** Stores LocalDate as an ISO-8601 TEXT value (yyyy-MM-dd). */
class DateConverters {
    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let {
        runCatching { LocalDate.parse(it) }.getOrNull()
    }
}
