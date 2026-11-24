package com.example.lifedashboard.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val trailingLabel: String?,
    val dueLabel: String,
    val dueDate: LocalDate?,
    val projectId: String?,
    val dayCategory: String,
    val priority: String,
    val isCompleted: Boolean,
    val showOnHome: Boolean,
    @ColumnInfo(defaultValue = "0") val sortOrder: Int = 0,
    @ColumnInfo(defaultValue = "'NONE'") val recurrence: String = "NONE",
    @ColumnInfo(defaultValue = "''") val recurrenceDays: String = "",
    val recurrenceSeriesId: String? = null,
    @ColumnInfo(defaultValue = "0") val recurrenceAnchorDay: Int = 0,
    @ColumnInfo(defaultValue = "0") val reminderEnabled: Boolean = false,
    @ColumnInfo(defaultValue = "'09:00'") val reminderTime: String = "09:00"
)

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val deadlineLabel: String,
    val deadlineDate: LocalDate?,
    val stageLabel: String,
    val progress: Float,
    val completedTasks: Int,
    val totalTasks: Int,
    val colorArgb: Int,
    val isCompleted: Boolean
)

@Entity(tableName = "project_stages")
data class ProjectStageEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val title: String,
    val isCompleted: Boolean,
    val isCurrent: Boolean,
    @ColumnInfo(defaultValue = "0") val sortOrder: Int = 0
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val body: String,
    val category: String,
    val color: String,
    val updatedLabel: String,
    val isPinned: Boolean,
    val attachmentCount: Int
)

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
    val title: String,
    /** Local yyyy-MM-dd keys separated by commas. */
    val completionDates: String,
    val color: String,
    val reminderLabel: String,
    @ColumnInfo(defaultValue = "0") val reminderEnabled: Boolean = false,
    @ColumnInfo(defaultValue = "'09:00'") val reminderTime: String = "09:00",
    @ColumnInfo(defaultValue = "'1,2,3,4,5,6,7'") val reminderDays: String = "1,2,3,4,5,6,7"
)

@Entity(tableName = "activity_events")
data class ActivityEventEntity(
    @PrimaryKey val id: String,
    val type: String,
    val itemId: String,
    val occurredAtEpochMillis: Long,
    val completed: Boolean
)

@Entity(tableName = "app_metadata")
data class AppMetadataEntity(
    @PrimaryKey val key: String,
    val value: String
)


@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val dueDate: LocalDate?,
    val currentValue: Int,
    val targetValue: Int,
    val unit: String,
    val projectId: String?,
    val color: String,
    @ColumnInfo(defaultValue = "0") val sortOrder: Int = 0
)
