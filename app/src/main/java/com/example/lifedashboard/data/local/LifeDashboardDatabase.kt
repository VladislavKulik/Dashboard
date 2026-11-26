package com.example.lifedashboard.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@TypeConverters(DateConverters::class)
@Database(
    entities = [
        TaskEntity::class,
        TaskSubtaskEntity::class,
        TaskTimerSessionEntity::class,
        ProjectEntity::class,
        ProjectStageEntity::class,
        NoteEntity::class,
        NoteAttachmentEntity::class,
        HabitEntity::class,
        ActivityEventEntity::class,
        AppMetadataEntity::class,
        GoalEntity::class
    ],
    version = 12,
    exportSchema = true
)
abstract class LifeDashboardDatabase : RoomDatabase() {
    abstract fun dashboardDao(): DashboardDao
}