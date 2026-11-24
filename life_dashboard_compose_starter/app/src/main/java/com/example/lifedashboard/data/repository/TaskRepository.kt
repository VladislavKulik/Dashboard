package com.example.lifedashboard.data.repository

import com.example.lifedashboard.data.model.DashboardTask
import com.example.lifedashboard.data.model.TaskPriority
import com.example.lifedashboard.data.model.TaskRecurrence
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

/** Task operations. Project counters and activity events are committed with task changes. */
interface TaskRepository {
    fun observeTasks(): Flow<List<DashboardTask>>
    suspend fun toggleTask(taskId: String): Boolean
    suspend fun addTask(title: String, priority: TaskPriority, projectId: String? = null, dueDate: LocalDate? = LocalDate.now(), recurrence: TaskRecurrence = TaskRecurrence.NONE, recurrenceDays: Set<Int> = emptySet(), reminderEnabled: Boolean = false, reminderTime: String = "09:00"): Boolean
    suspend fun updateTask(task: DashboardTask): Boolean
    suspend fun deleteTask(taskId: String): Boolean
}
