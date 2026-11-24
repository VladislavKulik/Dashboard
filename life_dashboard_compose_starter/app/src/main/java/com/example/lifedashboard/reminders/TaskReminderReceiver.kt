package com.example.lifedashboard.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.lifedashboard.data.local.RoomDashboardPersistence
import com.example.lifedashboard.data.local.DataStoreSettingsRepository
import com.example.lifedashboard.data.local.dashboardSettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TaskReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val taskId = intent?.getStringExtra(AndroidTaskReminderScheduler.EXTRA_TASK_ID) ?: return
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            var persistence: RoomDashboardPersistence? = null
            try {
                persistence = RoomDashboardPersistence.create(context.applicationContext)
                val enabled = DataStoreSettingsRepository(context.applicationContext.dashboardSettingsDataStore)
                    .observeSettings().first().notificationsEnabled
                val task = persistence.getTaskById(taskId)
                if (task == null || task.isCompleted || !task.reminderEnabled || !enabled) {
                    AndroidTaskReminderScheduler(context).cancel(taskId)
                } else {
                    HabitReminderNotifications.show(context, "task-$taskId", task.title, "Напоминание о задаче")
                    AndroidTaskReminderScheduler(context).cancel(taskId)
                }
            } catch (_: Exception) {
            } finally {
                persistence?.close()
                pending.finish()
            }
        }
    }
}
