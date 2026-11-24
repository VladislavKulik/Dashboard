package com.example.lifedashboard.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.example.lifedashboard.data.model.DashboardTask
import java.time.LocalTime
import java.time.ZoneId

interface TaskReminderScheduler {
    fun schedule(task: DashboardTask): Boolean
    fun cancel(taskId: String)
}

/** One-shot reminders are tied to a task occurrence, including each recurring occurrence. */
class AndroidTaskReminderScheduler(context: Context) : TaskReminderScheduler {
    private val appContext = context.applicationContext
    private val manager = appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    override fun schedule(task: DashboardTask): Boolean {
        cancel(task.id)
        val date = task.dueDate ?: return !task.reminderEnabled
        if (!task.reminderEnabled || task.isCompleted) return true
        val time = runCatching { LocalTime.parse(task.reminderTime) }.getOrNull() ?: return false
        val triggerAt = date.atTime(time).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (triggerAt <= System.currentTimeMillis()) return true
        val alarmManager = manager ?: return false
        val pending = pendingIntent(task.id, PendingIntent.FLAG_UPDATE_CURRENT)
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            } else alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            true
        } catch (_: Exception) { false }
    }

    override fun cancel(taskId: String) {
        if (taskId.isBlank()) return
        val existing = pendingIntent(taskId, PendingIntent.FLAG_NO_CREATE) ?: return
        manager?.cancel(existing)
        existing.cancel()
    }

    private fun pendingIntent(taskId: String, flag: Int): PendingIntent? {
        val intent = Intent(appContext, TaskReminderReceiver::class.java).apply {
            action = ACTION_TASK_REMINDER
            data = Uri.parse("lifedashboard://task-reminder/${Uri.encode(taskId)}")
            putExtra(EXTRA_TASK_ID, taskId)
        }
        return PendingIntent.getBroadcast(appContext, 0, intent, flag or immutableFlag())
    }

    private fun immutableFlag() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0

    companion object {
        const val ACTION_TASK_REMINDER = "com.example.lifedashboard.action.TASK_REMINDER"
        const val EXTRA_TASK_ID = "extra_task_id"
    }
}
