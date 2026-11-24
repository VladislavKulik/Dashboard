# Stage 19 validation

## Passed checks

1. `DateInputTest.kt` compiled and passed: ISO and dotted date parsing, invalid leap-date rejection, relative labels, Russian month labels, numeric day/month labels, legacy time suffixes and no-deadline labels.
2. `CalendarEventsTest.kt` compiled and passed with the new `LocalDate` goal model; event aggregation and month-grid behavior remain intact.
3. `DateMigrationTest.py` passed SQLite structural/data-preservation checks for the v5→v6 columns. The old task/project rows and existing goal date values remain present.
4. The Stage 19 Room/data compile script compiled the current entities, DAO, type converters, database/migrations, `RoomDashboardPersistence`, repositories and backup repository against compile-only Android/Room/SQLite/JSON stubs.
5. The Calendar screen and date picker component compiled against compile-only Compose/Material 3 stubs.
6. Tasks, Goals and Project Editor screens compiled against compile-only Compose/Material 3 stubs.
7. Source integration validators for Stages 14–19 passed.
8. Existing goal migration v4→v5 preservation test passed.
9. Kotlin delimiter and source syntax checks passed.

## Not verified

- A real Android Gradle build, because the source archive does not contain `settings.gradle(.kts)`, root/app `build.gradle(.kts)`, Gradle Wrapper, version catalog or the user's Android SDK configuration.
- Real Compose/Material 3 binary compatibility; compile-only stubs validate Kotlin call shapes, not the actual dependency versions.
- Running Room KSP/KAPT schema validation against the user's compiler/plugin versions.
- Running the migration on a physical Android SQLite/Room installation. SQLite structural behavior was exercised with Python's SQLite engine, and migration Kotlin code was compiled against stubs.
- Actual Date Picker interaction on an Android device.

## Required integration notes

- Register `LifeDashboardDatabase.MIGRATION_5_6`; `RoomDashboardPersistence` already includes it in the migration builder.
- Keep `DateConverters` registered on `LifeDashboardDatabase`.
- If `minSdk < 26`, enable Core Library Desugaring and add a compatible `desugar_jdk_libs` dependency; otherwise `LocalDate` requires API 26+.
- Room and Material 3 dependencies must match the versions already used by the consuming project.
