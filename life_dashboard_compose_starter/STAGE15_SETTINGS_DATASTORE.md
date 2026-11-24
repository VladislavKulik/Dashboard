# Stage 15 — Settings and Preferences DataStore

## Scope

This stage adds persistent application preferences without placing settings in the Room database. It does not implement backup/export, account sync, or feature-specific ViewModels.

## Persisted settings

`DashboardSettings` contains three preferences:

- `themeMode`: `SYSTEM`, `LIGHT`, or `DARK`.
- `notificationsEnabled`: master switch for habit reminder alarms and delivery.
- `showCompletedTasksOnHome`: whether completed tasks remain in the home-screen preview.

The defaults are `SYSTEM`, `true`, and `true`, preserving the existing notification and home-screen behavior for an existing installation. DataStore is stored in `dashboard_settings.preferences` through the `dashboardSettingsDataStore` delegate.

## Architecture

`SettingsRepository` is the settings contract. `DataStoreSettingsRepository` maps Preferences DataStore values into `DashboardSettings`; `DashboardViewModel` exposes the state through `StateFlow` and owns update commands. Room remains the source of truth for tasks, notes, projects, habits, and activity events.

The Settings screen is connected to the main navigation. Changes are persisted immediately.

## Theme

The original light palette remains the default. The dark palette is provided with a CompositionLocal so existing components can resolve semantic colors without passing a palette through every screen. `SYSTEM` follows the Android system appearance. `LIGHT` and `DARK` override it.

Some highly saturated decorative chart/card treatments were not individually redesigned; review them on a real device before treating dark mode as visually final.

## Notification master switch

- Disabling the master switch cancels alarms for all habits whose individual reminder is enabled.
- Enabling it schedules the next occurrence for those habits.
- The alarm receiver reads the master preference before delivering or scheduling the next reminder.
- The boot/time/time-zone rescheduler obeys the same preference.
- Android notification permission and exact-alarm access status are shown in Settings. If notification permission is denied, the master switch remains off. Missing exact-alarm access uses the existing inexact fallback unless the user grants access through Android Settings.

## Dependencies

Add the following dependency if it is not already present; choose a DataStore version compatible with the project's AndroidX/Kotlin setup:

```kotlin
implementation("androidx.datastore:datastore-preferences:<datastore-version>")
```

No Room schema change was made in this stage.

## Manual verification

1. Launch and switch `SYSTEM` / `LIGHT` / `DARK`; restart and confirm the selection persists.
2. Toggle the home completed-task preference and confirm the Home preview updates; restart and confirm persistence.
3. Create a habit with a reminder; verify it is scheduled when the master switch is on.
4. Disable global reminders; confirm future alarms are cancelled and a manually delivered receiver does not post a notification.
5. Re-enable global reminders; verify the next alarm is scheduled again.
6. Deny notification permission and confirm the global switch is not enabled.
7. Test exact-alarm access granted and denied on Android 12+; denied access should retain an inexact fallback.
8. Test SYSTEM mode with Android dark mode on and off.
