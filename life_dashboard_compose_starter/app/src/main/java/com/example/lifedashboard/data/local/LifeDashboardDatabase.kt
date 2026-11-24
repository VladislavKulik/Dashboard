package com.example.lifedashboard.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.lifedashboard.data.DateInput
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@TypeConverters(DateConverters::class)
@Database(
    entities = [
        TaskEntity::class,
        ProjectEntity::class,
        ProjectStageEntity::class,
        NoteEntity::class,
        HabitEntity::class,
        ActivityEventEntity::class,
        AppMetadataEntity::class,
        GoalEntity::class
    ],
    version = 8,
    exportSchema = true
)
abstract class LifeDashboardDatabase : RoomDatabase() {
    abstract fun dashboardDao(): DashboardDao

    companion object {
        /** Adds initialization metadata while preserving existing version-1 user data. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `tasks` ADD COLUMN `sortOrder` INTEGER NOT NULL DEFAULT 0")
                database.execSQL("UPDATE `tasks` SET `sortOrder` = -rowid")
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS `app_metadata` (`key` TEXT NOT NULL, `value` TEXT NOT NULL, PRIMARY KEY(`key`))"""
                )
            }
        }

        /** Persists the explicit project-stage order without rebuilding user data. */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Stage 11 created tasks.sortOrder without an explicit entity default,
                // while the v1->v2 migration added it with DEFAULT 0. Rebuild this
                // small table to normalize both installation paths before validation.
                database.execSQL(
                    """CREATE TABLE `tasks_new` (
                        `id` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `trailingLabel` TEXT,
                        `dueLabel` TEXT NOT NULL,
                        `projectId` TEXT,
                        `dayCategory` TEXT NOT NULL,
                        `priority` TEXT NOT NULL,
                        `isCompleted` INTEGER NOT NULL,
                        `showOnHome` INTEGER NOT NULL,
                        `sortOrder` INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`id`)
                    )"""
                )
                database.execSQL(
                    """INSERT INTO `tasks_new` (`id`, `title`, `trailingLabel`, `dueLabel`, `projectId`, `dayCategory`, `priority`, `isCompleted`, `showOnHome`, `sortOrder`)
                        SELECT `id`, `title`, `trailingLabel`, `dueLabel`, `projectId`, `dayCategory`, `priority`, `isCompleted`, `showOnHome`, `sortOrder`
                        FROM `tasks` ORDER BY `sortOrder` DESC, rowid DESC"""
                )
                database.execSQL("DROP TABLE `tasks`")
                database.execSQL("ALTER TABLE `tasks_new` RENAME TO `tasks`")

                database.execSQL("ALTER TABLE `project_stages` ADD COLUMN `sortOrder` INTEGER NOT NULL DEFAULT 0")
                database.execSQL("UPDATE `project_stages` SET `sortOrder` = CAST(substr(`id`, instr(`id`, 'stage-') + 6) AS INTEGER) WHERE `id` LIKE '%stage-%'")
            }
        }

        /** Adds per-habit reminder preferences without enabling notifications on existing habits. */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `habits` ADD COLUMN `reminderEnabled` INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE `habits` ADD COLUMN `reminderTime` TEXT NOT NULL DEFAULT '09:00'")
                database.execSQL("ALTER TABLE `habits` ADD COLUMN `reminderDays` TEXT NOT NULL DEFAULT '1,2,3,4,5,6,7'")
            }
        }


        /** Adds the measurable-goals table without changing any existing user tables. */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS `goals` (
                        `id` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `dueDate` TEXT,
                        `currentValue` INTEGER NOT NULL,
                        `targetValue` INTEGER NOT NULL,
                        `unit` TEXT NOT NULL,
                        `projectId` TEXT,
                        `color` TEXT NOT NULL,
                        `sortOrder` INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`id`)
                    )"""
                )
            }
        }

        /** Adds canonical ISO date columns while retaining all legacy display labels. */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `tasks` ADD COLUMN `dueDate` TEXT")
                database.execSQL("ALTER TABLE `projects` ADD COLUMN `deadlineDate` TEXT")

                database.query("SELECT `id`, `dueLabel` FROM `tasks`").use { cursor ->
                    while (cursor.moveToNext()) {
                        val id = cursor.getString(0)
                        val label = cursor.getString(1)
                        val date = DateInput.parseLegacyLabel(label)
                        database.execSQL(
                            "UPDATE `tasks` SET `dueDate` = ? WHERE `id` = ?",
                            arrayOf(date?.toString(), id)
                        )
                    }
                }
                database.query("SELECT `id`, `deadlineLabel` FROM `projects`").use { cursor ->
                    while (cursor.moveToNext()) {
                        val id = cursor.getString(0)
                        val label = cursor.getString(1)
                        val date = DateInput.parseLegacyLabel(label)
                        database.execSQL(
                            "UPDATE `projects` SET `deadlineDate` = ? WHERE `id` = ?",
                            arrayOf(date?.toString(), id)
                        )
                    }
                }
                // The v5 goals.dueDate column already contains ISO keys; its physical
                // SQLite representation remains TEXT, so no table rebuild is needed.
            }
        }

        /** Adds recurrence metadata to tasks; existing tasks remain non-recurring. */
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `tasks` ADD COLUMN `recurrence` TEXT NOT NULL DEFAULT 'NONE'")
                database.execSQL("ALTER TABLE `tasks` ADD COLUMN `recurrenceDays` TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE `tasks` ADD COLUMN `recurrenceSeriesId` TEXT")
                database.execSQL("ALTER TABLE `tasks` ADD COLUMN `recurrenceAnchorDay` INTEGER NOT NULL DEFAULT 0")
            }
        }

        /** Adds optional one-shot task reminders; existing tasks remain disabled. */
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `tasks` ADD COLUMN `reminderEnabled` INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE `tasks` ADD COLUMN `reminderTime` TEXT NOT NULL DEFAULT '09:00'")
            }
        }

    }
}
