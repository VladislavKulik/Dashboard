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
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

/** Rebuilds pending reminders after reboot, clock/time-zone changes or exact-alarm access grant. */
class HabitReminderRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            var persistence: RoomDashboardPersistence? = null
            try {
                persistence = RoomDashboardPersistence.create(context.applicationContext)
                val scheduler = AndroidHabitReminderScheduler(context)
                val notificationsEnabled = DataStoreSettingsRepository(context.applicationContext.dashboardSettingsDataStore)
                    .observeSettings().first().notificationsEnabled
                val habits = persistence.getEnabledHabits()
                if (notificationsEnabled) habits.forEach(scheduler::schedule)
                else habits.forEach { scheduler.cancel(it.id) }
                val taskScheduler = AndroidTaskReminderScheduler(context)
                val tasks = persistence.getAllTasks()
                if (notificationsEnabled) tasks.forEach(taskScheduler::schedule)
                else tasks.forEach { taskScheduler.cancel(it.id) }
            } catch (_: Exception) {
                // The app will retry scheduling on its next foreground launch.
            } finally {
                persistence?.close()
                pendingResult.finish()
            }
        }
    }
}
