# Stage 11 — Remove DemoDashboardStore

## Goal

Make Room-backed repositories the only source of persisted feature data and remove the transitional Compose-backed store without changing the existing screen design.

## State flow

`Room -> Feature Repository Flow -> DashboardViewModel.combine(...) -> DashboardUiState -> LifeDashboardApp -> screens`

The ViewModel combines task, project, note, habit and activity streams. The screens continue to receive plain immutable lists and callbacks; their layout and component styling are unchanged.

## Repository responsibilities

- `TaskRepository`: observes tasks, adds tasks, toggles completion. Updating a linked project's counters and writing a `TASK_COMPLETION` event are part of the same Room transaction.
- `ProjectsRepository`: observes project rows together with stages and updates stage progress.
- `NotesRepository`: observes and mutates notes.
- `HabitsRepository`: observes habits and mutates dated completion history.
- `ActivityRepository`: observes the event history consumed by Analytics. Event creation is committed inside the task mutation transaction to avoid orphan activity records.
- `DashboardRepository`: initializes the database once; it is no longer a snapshot persistence facade.

`DashboardUiState` is a presentation-layer model. `DemoDashboardStore`, `Snapshot`, and `TaskMutation` are retired.

## Initial data and migration

The starter content lives in `DashboardSeedData`. Room database version is now 2. Migration `1 -> 2` adds `tasks.sortOrder` and `app_metadata` without deleting or recreating any existing feature table. Existing task order is derived from `rowid`, so the migration preserves the database's current task sequence.

`initializeIfNeeded()` runs in a transaction:

1. If `starter_seed_v1` exists, do nothing.
2. If any user data already exists in feature tables, preserve it and only write the marker.
3. Otherwise, insert the starter rows, project stages and marker together.

This avoids both destructive migration and re-seeding after the user has deleted their own content. The metadata marker is the durable indication that initialization has been handled.

## Compose isolation

`DashboardProject` stores a plain ARGB integer. UI files convert it to Compose `Color`. The data and presentation model packages no longer import Compose types or Compose observable collections.

## Ordering

Task rows use an explicit `sortOrder`. Existing values are derived from `rowid` during migration; seed tasks receive descending positions and newly added tasks receive the next highest position. A status toggle uses Room `@Update`, so it does not remove/reinsert the row or change the task's position.

## Notes

- `ActivityRepository` is intentionally read-only. Event creation belongs to the task transaction where the originating action is known.
- The sample dataset remains for a genuinely new installation. A migrated database with any existing feature records is not seeded again.
- Full Gradle configuration, Room compiler setup and Android runtime behavior still need verification in the user's Android Studio project.
