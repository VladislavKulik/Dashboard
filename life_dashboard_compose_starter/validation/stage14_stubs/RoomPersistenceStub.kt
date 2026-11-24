package com.example.lifedashboard.data.local
import android.content.Context
import com.example.lifedashboard.data.model.DashboardHabit
import com.example.lifedashboard.data.model.DashboardTask
class RoomDashboardPersistence {
 suspend fun getHabit(habitId: String): DashboardHabit? = null
 suspend fun getEnabledHabits(): List<DashboardHabit> = emptyList()
 suspend fun getAllTasks(): List<DashboardTask> = emptyList()
 fun close() {}
 companion object { fun create(context: Context) = RoomDashboardPersistence() }
}
