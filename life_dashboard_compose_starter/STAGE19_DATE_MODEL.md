# Stage 19 — LocalDate and date filters

## What changed

- `DashboardTask.dueDate`, `DashboardProject.deadlineDate`, and `DashboardGoal.dueDate` are nullable `java.time.LocalDate` values.
- Room maps these values to ISO-8601 `TEXT` through `DateConverters`.
- `TaskEntity` and `ProjectEntity` gained canonical date columns. Existing `dueLabel` and `deadlineLabel` remain as display/compatibility fields; they are no longer the primary source of truth when a canonical date exists.
- Goals already had a `TEXT` date column in Room v5. Its Kotlin type is now `LocalDate?`; its SQLite representation remains the same.

## Migration 5 → 6

`MIGRATION_5_6` adds `tasks.dueDate` and `projects.deadlineDate`, then parses legacy labels with `DateInput.parseLegacyLabel`.

Recognized labels include:

- `Сегодня`, `Завтра`, `Послезавтра`, `Вчера`;
- `через N день/дня/дней`;
- ISO `yyyy-MM-dd` and dotted `dd.MM.yyyy`;
- numeric day/month labels such as `05.11`;
- Russian month labels such as `27 ноя`, `27 ноя.` and `15 декабря`;
- task labels with a time suffix such as `Сегодня · 16:00` (the date is migrated; the legacy display string remains untouched).

For month/day labels without a year, the closest year within a six-month window is used. Labels that cannot be interpreted safely remain unchanged in their legacy display columns, and the new canonical date stays `NULL`.

## UI behavior

- Task, project and goal editors share `LocalDatePickerField`, backed by Material 3 `DatePicker`.
- Task due date selection updates the canonical date and its display label; a time suffix already present in the old task label is retained.
- Tasks adds an `OVERDUE` filter for incomplete tasks with a date before today.
- Calendar adds `ALL`, `NEXT_7_DAYS`, `THIS_MONTH` and `OVERDUE` filters. The overdue period includes incomplete tasks, projects and goals, not historical habit completions.

## Backup compatibility

JSON format version is now `3`.

- New exports include `tasks[].dueDate` and `projects[].deadlineDate` as ISO strings or `null`.
- Goal dates remain ISO strings, now backed by `LocalDate` in the app model.
- Imports of versions 1 and 2 remain supported. Older task/project labels are converted using `DateInput` during import.
- Version 1 still has no goals array; version 2 and later do.

## Android compatibility note

`java.time.LocalDate` is available natively from Android API 26. For a lower `minSdk`, enable Core Library Desugaring in the app module and add the compatible `desugar_jdk_libs` dependency. The source package did not include a Gradle build file, so no Gradle dependency or `minSdk` value was changed here.

## Data safety

- Migration adds nullable columns and does not drop, rebuild, or rewrite legacy label columns.
- Existing goal date text values remain in the same SQLite column and are parsed by the Room converter.
- Export/import stays a logical Room backup in a transaction; no database file copying was introduced.
