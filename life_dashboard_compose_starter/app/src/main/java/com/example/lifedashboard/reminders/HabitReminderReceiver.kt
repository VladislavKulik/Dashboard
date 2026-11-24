package com.example.lifedashboard.reminders

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.example.lifedashboard.MainActivity
import com.example.lifedashboard.data.local.RoomDashboardPersistence
import com.example.lifedashboard.data.local.DataStoreSettingsRepository
import com.example.lifedashboard.data.local.dashboardSettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

/** Delivers the current habit reminder and arms its next weekly occurrence. */
class HabitReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val habitId = intent?.getStringExtra(AndroidHabitReminderScheduler.EXTRA_HABIT_ID)
            ?: return
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            var persistence: RoomDashboardPersistence? = null
            try {
                persistence = RoomDashboardPersistence.create(context.applicationContext)
                val notificationsEnabled = DataStoreSettingsRepository(context.applicationContext.dashboardSettingsDataStore)
                    .observeSettings().first().notificationsEnabled
                val habit = persistence.getHabit(habitId)
                if (!notificationsEnabled) {
                    AndroidHabitReminderScheduler(context).cancel(habitId)
                } else if (habit != null && habit.reminderEnabled) {
                    // Schedule again before displaying: the next notification is not dependent
                    // on the user opening the app after this one.
                    AndroidHabitReminderScheduler(context).schedule(habit)
                    HabitReminderNotifications.show(context, habit.id, habit.title)
                }
            } catch (_: Exception) {
                // Receivers must not crash the process for a transient database/notification error.
            } finally {
                persistence?.close()
                pendingResult.finish()
            }
        }
    }
}

internal object HabitReminderNotifications {
    private const val CHANNEL_ID = "life_dashboard_reminders"
    private const val CHANNEL_NAME = "Напоминания Life Dashboard"

    fun show(context: Context, habitId: String, title: String, notificationTitle: String = "Время для привычки") {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !manager.areNotificationsEnabled()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Напоминания о задачах и привычках"
                }
            )
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(context)
        }
        val notification = builder
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(notificationTitle)
            .setContentText(title)
            .setStyle(Notification.BigTextStyle().bigText(title))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setCategory(Notification.CATEGORY_REMINDER)
            .build()

        manager.notify(habitId.hashCode() and 0x7fffffff, notification)
    }
}
