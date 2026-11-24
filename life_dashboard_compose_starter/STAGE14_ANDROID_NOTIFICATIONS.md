# Stage 14 — Android notifications for habit reminders

## What changed

- Added `HabitReminderScheduler` boundary and `AndroidHabitReminderScheduler` backed by `AlarmManager`.
- Each enabled habit has one pending alarm for its next selected weekday/time. When it fires, the receiver reads the current habit from Room, shows a notification and schedules the next occurrence.
- Saving an enabled reminder replaces its pending alarm. Disabling a reminder or deleting its habit cancels the alarm.
- `DashboardViewModel` restores enabled reminders when the app process starts.
- `HabitReminderRescheduleReceiver` rebuilds alarms after device reboot, system clock/time-zone changes and exact-alarm access being granted.
- Android 13+ runtime notification permission is requested when the user saves an enabled reminder.
- On Android 12+, the app opens the system "Alarms & reminders" access page when exact-alarm access is missing. If access is not granted, an inexact `AlarmManager` fallback remains scheduled.

## Android manifest

`app/src/main/AndroidManifest.xml` is a minimal manifest for this sample package. When integrating into an existing Android app, merge these declarations into the existing manifest instead of blindly replacing it. Keep any existing application attributes, activity theme, deep links, backup rules and other components.

Required permissions:

- `android.permission.POST_NOTIFICATIONS`
- `android.permission.RECEIVE_BOOT_COMPLETED`
- `android.permission.SCHEDULE_EXACT_ALARM`

Required receivers:

- `.reminders.HabitReminderReceiver`
- `.reminders.HabitReminderRescheduleReceiver`, with `BOOT_COMPLETED`, `TIME_SET`, `TIMEZONE_CHANGED` and `SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED` actions.

Set `compileSdk` to 33 or newer for the Android 13 notification permission APIs. Prefer `targetSdk` 33 or newer so the app controls when the notification permission prompt is shown. No additional third-party scheduler or notification dependency was added; this stage uses platform `AlarmManager`, `NotificationManager` and the Activity Result API already used by the Compose app.

## Exact vs inexact alarms

Android 12+ gates exact alarms behind special app access. The app declares `SCHEDULE_EXACT_ALARM` and opens `Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM` when the user enables a reminder and the access is missing. If the user declines, the app uses `setAndAllowWhileIdle` as a best-effort fallback. The system may defer inexact alarms, so the chosen clock time is not guaranteed without exact-alarm access.

The app does not use `USE_EXACT_ALARM`; that permission is restricted to qualifying alarm-clock/calendar use cases. Users can later change the access under Android Settings → Apps → Special app access → Alarms & reminders.

## Scheduling behavior and limits

- Weekday numbers follow ISO-8601: Monday=1 through Sunday=7.
- The next occurrence is calculated in the device's current local time zone. A time-zone or clock change triggers a reschedule.
- Only the next occurrence is armed; the receiver schedules the following occurrence after firing.
- Alarms are restored after reboot. Android force-stop behavior still applies: after a user force-stops the app, the app must be launched again before its receivers/alarms can reliably run.
- Notifications are not shown when the user denies `POST_NOTIFICATIONS`, disables notifications for the app, or disables the notification channel. Alarms remain registered, so reminders can resume after notification access is restored.
- A reminder does not automatically mark the habit complete; the notification opens the app and the user records completion manually.
- The scheduler is local-only; no network access or background server is used.

## Database

No Room schema change was required in Stage 14. Reminder preferences already live in Room since Stage 13. The scheduler reads the existing habit ID, enabled flag, time and weekday set; completion history is not touched.
