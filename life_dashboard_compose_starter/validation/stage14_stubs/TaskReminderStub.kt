package com.example.lifedashboard.reminders
import android.content.Context
import com.example.lifedashboard.data.model.DashboardTask
interface TaskReminderScheduler {
    fun schedule(task: DashboardTask): Boolean
    fun cancel(taskId: String)
}
class AndroidTaskReminderScheduler(context: Context) : TaskReminderScheduler {
    override fun schedule(task: DashboardTask): Boolean = true
    override fun cancel(taskId: String) {}
}
