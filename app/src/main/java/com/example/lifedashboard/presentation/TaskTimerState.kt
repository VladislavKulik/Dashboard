package com.example.lifedashboard.presentation

import com.example.lifedashboard.data.model.TaskTimerSession
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

data class TaskTimerState(
    val activeTaskId: String? = null,
    val activeTaskTitle: String = "",
    val activeProjectId: String? = null,
    val activeProjectName: String? = null,
    val elapsedSeconds: Long = 0L,
    val plannedSeconds: Long = DEFAULT_PLANNED_SECONDS,
    val isRunning: Boolean = false,
    val startedAtMillis: Long = 0L,
    val sessions: List<TaskTimerSession> = emptyList()
) {
    fun totalSecondsForTask(taskId: String): Long {
        val logged = sessions.filter { it.taskId == taskId }.sumOf { it.durationSeconds }
        val active = if (activeTaskId == taskId) elapsedSeconds else 0L
        return logged + active
    }

    fun totalSecondsForProjectToday(projectId: String?): Long {
        if (projectId == null) return 0L
        val today = LocalDate.now()
        val zone = ZoneId.systemDefault()
        val loggedToday = sessions
            .filter {
                it.projectId == projectId &&
                    Instant.ofEpochMilli(it.endedAtMillis).atZone(zone).toLocalDate() == today
            }
            .sumOf { it.durationSeconds }
        val active = if (activeProjectId == projectId) elapsedSeconds else 0L
        return loggedToday + active
    }

    companion object {
        const val DEFAULT_PLANNED_SECONDS = 2L * 3600L

        fun formatHms(totalSeconds: Long): String {
            val safe = totalSeconds.coerceAtLeast(0L)
            val hours = safe / 3600
            val minutes = (safe % 3600) / 60
            val seconds = safe % 60
            return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        }

        fun formatShort(totalSeconds: Long): String {
            val safe = totalSeconds.coerceAtLeast(0L)
            if (safe in 1L..59L) return "$safe сек"
            val mins = (safe / 60).toInt()
            val h = mins / 60
            val m = mins % 60
            return when {
                h > 0 && m > 0 -> "$h ч $m мин"
                h > 0 -> "$h ч"
                else -> "$m мин"
            }
        }
    }
}