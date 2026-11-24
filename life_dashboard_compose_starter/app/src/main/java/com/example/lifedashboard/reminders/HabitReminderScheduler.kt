package com.example.lifedashboard.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.example.lifedashboard.data.model.DashboardHabit

/** Small platform boundary so DashboardViewModel can keep Android APIs out of feature logic. */
interface HabitReminderScheduler {
    fun schedule(habit: DashboardHabit): Boolean
    fun cancel(habitId: String)
}

/** Schedules one next occurrence per habit; the receiver schedules the following week. */
class AndroidHabitReminderScheduler(context: Context) : HabitReminderScheduler {
    private val appContext = context.applicationContext
    private val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    override fun schedule(habit: DashboardHabit): Boolean {
        cancel(habit.id)
        if (!habit.reminderEnabled || habit.id.isBlank()) return true

        val triggerAt = ReminderTimeCalculator.nextTrigger(
            nowMillis = System.currentTimeMillis(),
            time = habit.reminderTime,
            isoDays = habit.reminderDays
        ) ?: return false

        val manager = alarmManager ?: return false
        val pendingIntent = reminderPendingIntent(habit)

        return try {
            val canUseExactAlarm = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || manager.canScheduleExactAlarms()
            if (canUseExactAlarm) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                } else {
                    manager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                }
            } else {
                // Android 12+ may deny exact-alarm access. Keep a best-effort reminder
                // instead of silently dropping the user's schedule.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                } else {
                    manager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                }
            }
            true
        } catch (_: SecurityException) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                } else {
                    manager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                }
                true
            } catch (_: Exception) {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    override fun cancel(habitId: String) {
        if (habitId.isBlank()) return
        val manager = alarmManager ?: return
        val intent = Intent(appContext, HabitReminderReceiver::class.java).apply {
            action = ACTION_HABIT_REMINDER
            data = reminderUri(habitId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            0,
            intent,
            PendingIntent.FLAG_NO_CREATE or immutableFlag()
        ) ?: return
        manager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun reminderPendingIntent(habit: DashboardHabit): PendingIntent {
        val intent = Intent(appContext, HabitReminderReceiver::class.java).apply {
            action = ACTION_HABIT_REMINDER
            data = reminderUri(habit.id)
            putExtra(EXTRA_HABIT_ID, habit.id)
        }
        return PendingIntent.getBroadcast(
            appContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or immutableFlag()
        )
    }

    private fun reminderUri(habitId: String) =
        Uri.parse("lifedashboard://habit-reminder/${Uri.encode(habitId)}")

    private fun immutableFlag(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0

    companion object {
        const val ACTION_HABIT_REMINDER = "com.example.lifedashboard.action.HABIT_REMINDER"
        const val EXTRA_HABIT_ID = "extra_habit_id"
    }
}
