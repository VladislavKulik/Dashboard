# Stage 21 — Recurring Tasks

## Implemented

- Task recurrence modes: none, daily, weekly (selected ISO weekdays), monthly.
- Recurrence can be configured when creating a task or later in the task editor.
- A completed recurring occurrence creates only the next occurrence, inside the same Room transaction as the completion update.
- Skipping an open recurring occurrence removes that occurrence and creates the next scheduled one. The confirmation dialog explains this behavior.
- A one-off occurrence can be moved by editing its due date. For monthly schedules the original day-of-month is retained, so Jan 31 → Feb 28 → Mar 31 does not drift.
- Weekly recurrence supports Monday–Sunday and wraps to the next selected day.
- The next occurrence is visible to Calendar because Calendar already observes task due dates; no calendar-specific table was added.
- Activity history records skipped occurrences as `TASK_SKIPPED`.
- Project links are carried forward to the next occurrence; project counters are updated transactionally.

## Persistence and compatibility

- Room database version: 7.
- Migration `6 → 7` adds `recurrence`, `recurrenceDays`, `recurrenceSeriesId`, and `recurrenceAnchorDay` to `tasks`.
- Existing tasks migrate as non-recurring.
- JSON backup format: version 4. Imports from versions 1–3 remain supported; older files naturally restore tasks as non-recurring.
- Export includes recurrence rule, weekly days, series id, and monthly anchor day.

## Behavior notes

- Recurrence is occurrence-driven: only the next occurrence is generated, not an unlimited list of future tasks.
- Completing or skipping an occurrence advances the series. Editing the due date moves that occurrence; the next date is calculated from the moved occurrence for daily/weekly schedules. Monthly schedules retain their original day-of-month anchor.
- To stop a series, edit the next open occurrence and set recurrence to “Не повторять”; then it will no longer generate another occurrence.
- Deleting a completed occurrence deletes only that historical task. Skipping is available for open recurring occurrences.
- There is no end date / maximum occurrence count yet.

## Files

- `data/TaskRecurrenceCalculator.kt` — pure date calculation.
- `data/model/DashboardModels.kt` — recurrence model fields.
- `data/local/DashboardEntities.kt`, `DashboardDao.kt`, `LifeDashboardDatabase.kt` — Room schema and migration.
- `data/local/RoomDashboardPersistence.kt` — transactional generation and skip handling.
- `data/backup/RoomDashboardBackupRepository.kt` — versioned backup compatibility.
- `feature/tasks/TasksScreen.kt` — create/edit UI and skip confirmation.
