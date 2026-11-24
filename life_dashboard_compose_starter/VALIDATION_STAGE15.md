# Stage 15 validation

## Automated checks

- `SettingsRepository` contract and DataStore adapter compile against small API stubs; an in-memory preferences test confirms defaults and persistence.
- Defaults and persistence of theme mode, global notification switch, and home completed-task preference are tested with an in-memory DataStore.
- `DashboardViewModel` compiles with AndroidX/Android test stubs and now observes settings independently from Room-backed feature flows.
- `SettingsScreen` and the dynamic light/dark theme palette compile in a separate Kotlin run using Compose API stubs (`validation/compile_settings_ui.sh`).
- Habit reminder receiver and boot/time rescheduler compile with Android, Room, and DataStore stubs.
- Existing Stage 14 reminder-time calculation, habit metrics smoke test, Room migration check, manifest checks, and Kotlin delimiter checks are retained.
- Static checks verify that Settings replaces the placeholder, app theme follows `DashboardSettings`, Home uses `showCompletedTasksOnHome`, and receivers honor `notificationsEnabled`.

## Not verified

The original Gradle wrapper, app/build.gradle(.kts), root build files, version catalog, and a configured Android SDK project were not supplied. Therefore this archive has not been through a complete Android Gradle build, Compose compiler pass, install, or device/emulator test. The DataStore dependency must be added to the user's existing app module if not already present.

Dark mode is implemented at the semantic palette level, but every screen's visual balance should still be reviewed on a real device, especially chart/card contrast. No Room migration is required.
