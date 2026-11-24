# Stage 5 — Room persistence

## Added files

- `data/local/DashboardEntities.kt` — Room entities for tasks, projects, stages, notes, habits and activity events.
- `data/local/DashboardDao.kt` — DAO queries and writes.
- `data/local/LifeDashboardDatabase.kt` — Room database.
- `data/local/RoomDashboardPersistence.kt` — mapping, restore and transactional snapshot persistence.

## Behavior

`LifeDashboardApp` creates a Room persistence adapter and passes it to `DemoDashboardStore`. The store restores persisted rows on startup. If the database is empty, the current sample data is saved as the initial dataset. Existing UI state lists remain the Compose-observed source of truth during the session.

Every task toggle, task addition, note change, pin/unpin, habit completion and habit addition persists the current snapshot. Task toggles also append an activity event. Analytics now groups completed-task events by local calendar day for the current week and by seven-day buckets for the current month.

## Important limitations

- This is a migration bridge for the existing UI-first starter, not the final repository/ViewModel architecture.
- Snapshot persistence rewrites the small personal-data tables transactionally. For a larger dataset, replace it with targeted DAO upserts/deletes.
- The activity log begins when this version is installed. It cannot infer historical completion dates for sample tasks.
- Schema migrations are not yet defined. Version 1 is the initial schema; before changing entities in a distributed build, add explicit Room migrations. Do not use destructive migration for real user data.
- Project color is stored as ARGB; enum values are stored by name.
- The archive still does not contain the user's root/app Gradle files. Add Room and KSP (or KAPT) dependencies using the instructions in README.


## Stage 7 update — task writes are now repository-owned

The Stage 5 snapshot behavior above describes the original Room integration. In the current Stage 7 source, tasks are observed through `TaskRepository.observeTasks()` and changed through targeted transactional writes. General snapshots only seed the task table when it is empty and preserve task-driven project counters; they no longer clear or replace task rows. Notes, habits and the remaining project data still use snapshot persistence.
