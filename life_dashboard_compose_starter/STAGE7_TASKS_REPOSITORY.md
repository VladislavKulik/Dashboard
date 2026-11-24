# Stage 7 — TaskRepository and StateFlow migration

## What changed

- Added `TaskRepository` as a feature-specific persistence contract.
- Added Room `Flow` observation for the tasks table.
- Task writes now commit the task, its linked project counters, and its activity event in one Room transaction.
- `DashboardViewModel` coordinates task commands and waits for initial Room restoration/seeding before running them.
- Home, Tasks, Projects, Project Details, and Analytics now receive tasks from the immutable `DashboardUiState` collected with `collectAsState()`.
- The old broad snapshot writer no longer overwrites task records after initialization; tasks are owned by `TaskRepository`.

## Data flow

```text
Compose screens
    ↓ callbacks
DashboardViewModel
    ↓ prepare mutation
DemoDashboardStore (creates immutable TaskMutation)
    ↓ commit
TaskRepository
    ↓ transaction: task + project + activity event
Room
    ↓ Flow<List<DashboardTask>>
DashboardViewModel / DemoDashboardStore
    ↓ StateFlow<DashboardUiState>
Compose screens
```

## Scope and remaining work

- Notes, habits, and project editing still use the snapshot-based `DashboardRepository` through `DemoDashboardStore`.
- `DemoDashboardStore` still contains demo seed data and non-task state; it is not yet removed.
- There is no task deletion UI yet. Empty-table Flow values are accepted after the initial store readiness gate.
- Ordering is currently the DAO's natural query order. A persistent explicit sort key should be introduced with a proper Room schema migration before implementing drag-and-drop ordering.
- The task repository shares a mutex with snapshot writes. Snapshot writes only seed tasks when the task table is empty, preventing later note/habit writes from restoring stale task lists.

## Validation boundary

Source-level structure and ZIP integrity can be checked here. A real Gradle build and Android runtime test still require the app's Gradle wrapper, build scripts, manifest, version catalog, and Android SDK setup; those files were not part of the supplied source archive.
