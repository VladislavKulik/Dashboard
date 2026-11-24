# Stage 14 validation

## Automated checks performed

- Compiled and ran `ReminderTimeCalculatorTest.kt` with the local Kotlin compiler. It covers same-day scheduling, the strict-future boundary, week wrap-around, nearest selected weekday, local time-zone behavior and invalid inputs.
- Compiled the AlarmManager scheduler and both BroadcastReceivers against minimal Android/Room API stubs.
- Compiled `DashboardViewModel` and repository contracts against minimal AndroidX/Android stubs to verify the scheduler injection and save/delete wiring.
- Reproducible command: `validation/run_stage14_checks.sh`.
- Parsed `app/src/main/AndroidManifest.xml` as XML and checked both receiver declarations and required permission declarations.
- Ran source-level checks for ViewModel scheduling/cancellation, Room enabled-habit queries, notification permission request, boot/time/time-zone/exact-access rescheduling actions and migration list consistency.
- Checked Kotlin delimiter balance across the packaged source tree.
- Checked ZIP integrity for the full project and patch archives.

## Not verified

A full Android Gradle build, manifest merger, device/emulator execution, actual permission dialogs, AlarmManager delivery under Doze, notification rendering and reboot behavior were not run. The supplied project does not include Gradle Wrapper, Gradle build files, a version catalog or an Android SDK project configuration. The manifest in this archive is a merge template and must be merged with the user's existing manifest.

## Manual device test

1. Merge the manifest declarations and set `compileSdk` to 33+.
2. Install and open the app. Open Habits and edit/create a habit.
3. Enable the reminder, select a weekday and a time 2–3 minutes ahead, and save.
4. Grant notification permission. On Android 12+, grant "Alarms & reminders" access for precise timing.
5. Verify the notification appears. Tap it and verify the app opens.
6. Edit the reminder to a different time and verify the old time no longer fires.
7. Disable the reminder or delete the habit and verify its alarm no longer fires.
8. Reboot the device and verify the next reminder is restored.
9. Change the device time zone and verify the schedule follows the new local time.
