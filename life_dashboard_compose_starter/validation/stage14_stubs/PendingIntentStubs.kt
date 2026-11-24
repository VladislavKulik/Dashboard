package android.app
import android.content.Context
import android.content.Intent
class PendingIntent {
    fun cancel() {}
    companion object {
        const val FLAG_NO_CREATE=1
        const val FLAG_IMMUTABLE=2
        const val FLAG_UPDATE_CURRENT=4
        fun getBroadcast(context: Context, requestCode: Int, intent: Intent, flags: Int): PendingIntent = PendingIntent()
        fun getActivity(context: Context, requestCode: Int, intent: Intent, flags: Int): PendingIntent? = PendingIntent()
    }
}
class AlarmManager {
    fun canScheduleExactAlarms() = true
    fun setExactAndAllowWhileIdle(type: Int, at: Long, pendingIntent: PendingIntent) {}
    fun setExact(type: Int, at: Long, pendingIntent: PendingIntent) {}
    fun setAndAllowWhileIdle(type: Int, at: Long, pendingIntent: PendingIntent) {}
    fun set(type: Int, at: Long, pendingIntent: PendingIntent) {}
    fun cancel(pendingIntent: PendingIntent) {}
    companion object { const val RTC_WAKEUP = 0 }
}
class Notification {
    class BigTextStyle { fun bigText(value: String) = this }
    class Builder {
        constructor(context: Context)
        constructor(context: Context, channelId: String)
        fun setSmallIcon(icon: Int) = this
        fun setContentTitle(title: String) = this
        fun setContentText(text: String) = this
        fun setStyle(style: BigTextStyle) = this
        fun setContentIntent(intent: PendingIntent?) = this
        fun setAutoCancel(value: Boolean) = this
        fun setCategory(value: String) = this
        fun build() = Notification()
    }
    companion object { const val CATEGORY_REMINDER = "reminder" }
}
class NotificationChannel(id: String, name: CharSequence, importance: Int) { var description: String = "" }
class NotificationManager {
    fun areNotificationsEnabled() = true
    fun createNotificationChannel(channel: NotificationChannel) {}
    fun notify(id: Int, notification: Notification) {}
    companion object { const val IMPORTANCE_DEFAULT = 3 }
}
