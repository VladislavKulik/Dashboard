# Applying Stage 19 patch

1. Make a backup or commit of the Stage 18 project.
2. Extract this patch into the Stage 18 project root and allow the listed files to be replaced/added.
3. Keep the consuming project's existing Gradle files. Confirm `minSdk 26+`, or enable Core Library Desugaring for `java.time.LocalDate` when supporting older Android versions.
4. Keep the Room KSP/KAPT setup already used by the app. `DateConverters` is registered on `LifeDashboardDatabase`; `MIGRATION_5_6` is already passed by `RoomDashboardPersistence`.
5. Build and test the v5→v6 migration on a copy of the database before updating a device with important data.
6. Verify date selection in Tasks, Project Editor and Goals; then test Calendar period chips and import an older v1/v2 JSON backup.

JSON export is version 3, and v1/v2 import remains supported. Legacy labels that cannot be parsed safely remain visible but do not receive a guessed canonical date.
