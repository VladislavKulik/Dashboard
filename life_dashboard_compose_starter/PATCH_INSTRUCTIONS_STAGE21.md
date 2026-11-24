# Stage 21 patch instructions

This patch is intended for the exact Stage 20 source tree.

1. Make a backup of your current project and `life_dashboard.db`.
2. Copy the patch contents over the Stage 20 project, preserving the directory structure.
3. Sync Gradle in Android Studio.
4. Build and run `:app:assembleDebug` / the app on a test device.
5. Confirm Room migration 6 → 7 before using the app with your only copy of personal data.

The patch updates the task model, Room schema/migration, task repository/ViewModel, task creation/editing UI, backup format, validation scripts, and documentation. It does not include a Gradle wrapper JAR; use the Stage 20 bootstrap script first if it has not already been generated.

Do not replace an existing user's database with a test database. Migration 6 → 7 adds recurrence columns and leaves existing tasks as non-recurring.
