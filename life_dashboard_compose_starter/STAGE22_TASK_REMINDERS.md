# Stage 22 — Task reminders

## What changed

- Added optional reminder settings to each task: `reminderEnabled` and local time `reminderTime` (`HH:mm`).
- Added reminder controls to the create-task and edit-task dialogs. A reminder requires a due date and a valid time.
- Added `TaskReminderScheduler` with an Android `AlarmManager` implementation. It uses exact alarms when Android permits them and falls back to an inexact idle-safe alarm otherwise.
- Added a broadcast receiver that re-reads the task and the global notification setting before displaying a notification. Deleted, completed, disabled, or globally muted tasks do not show a notification.
- Task reminders are synchronized from the Room-backed task stream, so edits, completion, recurring next occurrences, and date changes update the pending alarm.
- Added boot/time/time-zone/exact-alarm rescheduling for task reminders alongside habit reminders.
- Added cancellation during task deletion and backup replacement.
- Added Room migration 7 → 8. Existing tasks keep reminders disabled.
- Bumped backup JSON format to v5. Older backups restore tasks with reminders disabled; v5 preserves reminder settings.

## Android behavior and limitations

- Notifications require the app-wide notifications setting to be enabled and Android notification permission to be granted on Android 13+.
- Exact alarm access is not required for basic reminders. Without it, Android may deliver a reminder later than the selected minute.
- A reminder is one-shot for a task occurrence. A recurring task's next occurrence inherits the reminder time and is scheduled when the next task appears in Room.
- The reminder time uses the device's current time zone. Changing time zone causes pending reminders to be rebuilt.

## Validation status

- Static source review completed for task model/entity mapping, Room migration registration, backup parsing/serialization, UI callbacks, and receiver registration.
- Kotlin compiler was invoked as a parser/type-check probe, but a real project compile was not possible because this environment has no Android SDK, Gradle installation, or Gradle wrapper. Android API and Compose integration still need a full build and device test in Android Studio.
