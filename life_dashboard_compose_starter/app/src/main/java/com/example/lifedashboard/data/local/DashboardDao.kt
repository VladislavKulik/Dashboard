package com.example.lifedashboard.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DashboardDao {
    @Query("SELECT * FROM tasks ORDER BY sortOrder DESC, rowid DESC") suspend fun getTasks(): List<TaskEntity>
    @Query("SELECT * FROM tasks ORDER BY sortOrder DESC, rowid DESC") fun observeTasks(): Flow<List<TaskEntity>>
    @Query("SELECT * FROM tasks WHERE id = :taskId") suspend fun getTask(taskId: String): TaskEntity?
    @Query("SELECT * FROM tasks WHERE recurrenceSeriesId = :seriesId AND dueDate = :date LIMIT 1") suspend fun getTaskForSeriesDate(seriesId: String, date: String): TaskEntity?
    @Query("SELECT COUNT(*) FROM tasks WHERE projectId = :projectId") suspend fun countTasksForProject(projectId: String): Int
    @Query("SELECT COUNT(*) FROM tasks WHERE projectId = :projectId AND isCompleted = 1") suspend fun countCompletedTasksForProject(projectId: String): Int
    @Query("SELECT COUNT(*) FROM tasks") suspend fun countTasks(): Int
    @Query("SELECT COALESCE(MAX(sortOrder), 0) + 1 FROM tasks") suspend fun nextTaskSortOrder(): Int
    @Query("SELECT * FROM projects WHERE id = :projectId") suspend fun getProject(projectId: String): ProjectEntity?
    @Query("SELECT * FROM projects ORDER BY rowid DESC") suspend fun getProjects(): List<ProjectEntity>
    @Query("SELECT COUNT(*) FROM projects") suspend fun countProjects(): Int
    @Query("SELECT * FROM projects ORDER BY rowid DESC") fun observeProjects(): Flow<List<ProjectEntity>>
    @Query("SELECT * FROM project_stages") suspend fun getStages(): List<ProjectStageEntity>
    @Query("SELECT * FROM project_stages ORDER BY sortOrder ASC, id ASC") fun observeAllStages(): Flow<List<ProjectStageEntity>>
    @Query("SELECT * FROM project_stages WHERE projectId = :projectId ORDER BY sortOrder ASC, id ASC") suspend fun getStagesForProject(projectId: String): List<ProjectStageEntity>
    @Query("SELECT * FROM notes ORDER BY rowid DESC") suspend fun getNotes(): List<NoteEntity>
    @Query("SELECT COUNT(*) FROM notes") suspend fun countNotes(): Int
    @Query("SELECT * FROM notes ORDER BY rowid DESC") fun observeNotes(): Flow<List<NoteEntity>>
    @Query("SELECT * FROM notes WHERE id = :noteId") suspend fun getNote(noteId: String): NoteEntity?
    @Query("SELECT * FROM habits ORDER BY rowid DESC") suspend fun getHabits(): List<HabitEntity>
    @Query("SELECT COUNT(*) FROM habits") suspend fun countHabits(): Int
    @Query("SELECT * FROM habits ORDER BY rowid DESC") fun observeHabits(): Flow<List<HabitEntity>>
    @Query("SELECT * FROM habits WHERE id = :habitId") suspend fun getHabit(habitId: String): HabitEntity?
    @Query("SELECT * FROM habits WHERE reminderEnabled = 1") suspend fun getEnabledReminderHabits(): List<HabitEntity>
    @Query("SELECT * FROM goals ORDER BY sortOrder ASC, rowid DESC") suspend fun getGoals(): List<GoalEntity>
    @Query("SELECT * FROM goals ORDER BY sortOrder ASC, rowid DESC") fun observeGoals(): Flow<List<GoalEntity>>
    @Query("SELECT * FROM goals WHERE id = :goalId") suspend fun getGoal(goalId: String): GoalEntity?
    @Query("SELECT COALESCE(MAX(sortOrder), -1) + 1 FROM goals") suspend fun nextGoalSortOrder(): Int
    @Query("SELECT * FROM activity_events ORDER BY occurredAtEpochMillis") suspend fun getActivityEvents(): List<ActivityEventEntity>
    @Query("SELECT * FROM activity_events ORDER BY occurredAtEpochMillis ASC") fun observeActivityEvents(): Flow<List<ActivityEventEntity>>
    @Query("SELECT * FROM app_metadata WHERE `key` = :key LIMIT 1") suspend fun getMetadata(key: String): AppMetadataEntity?
    @Query("SELECT * FROM app_metadata") suspend fun getAllMetadata(): List<AppMetadataEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertTasks(items: List<TaskEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertTask(item: TaskEntity)
    @Update suspend fun updateTask(item: TaskEntity)
    @Update suspend fun updateProject(item: ProjectEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertProject(item: ProjectEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertProjects(items: List<ProjectEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertStages(items: List<ProjectStageEntity>)
    @Query("DELETE FROM project_stages WHERE projectId = :projectId") suspend fun deleteStagesForProject(projectId: String)
    @Query("DELETE FROM projects WHERE id = :projectId") suspend fun deleteProject(projectId: String)
    @Query("UPDATE tasks SET projectId = NULL WHERE projectId = :projectId") suspend fun detachTasksFromProject(projectId: String)
    @Query("DELETE FROM tasks WHERE id = :taskId") suspend fun deleteTask(taskId: String)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertNotes(items: List<NoteEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertNote(item: NoteEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertHabits(items: List<HabitEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertHabit(item: HabitEntity)
    @Query("DELETE FROM habits WHERE id = :habitId") suspend fun deleteHabit(habitId: String)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertGoal(goal: GoalEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertGoals(goals: List<GoalEntity>)
    @Query("DELETE FROM goals WHERE id = :goalId") suspend fun deleteGoal(goalId: String)
    @Query("UPDATE goals SET projectId = NULL WHERE projectId = :projectId") suspend fun detachGoalsFromProject(projectId: String)
    @Query("DELETE FROM goals") suspend fun clearGoals()
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertEvent(event: ActivityEventEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertEvents(items: List<ActivityEventEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertMetadata(item: AppMetadataEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertMetadataItems(items: List<AppMetadataEntity>)

    @Query("DELETE FROM tasks") suspend fun clearTasks()
    @Query("DELETE FROM projects") suspend fun clearProjects()
    @Query("DELETE FROM project_stages") suspend fun clearStages()
    @Query("DELETE FROM notes") suspend fun clearNotes()
    @Query("DELETE FROM habits") suspend fun clearHabits()
    @Query("DELETE FROM activity_events") suspend fun clearActivityEvents()
    @Query("DELETE FROM app_metadata") suspend fun clearMetadata()
    @Query("DELETE FROM notes WHERE id = :noteId") suspend fun deleteNote(noteId: String)
}
