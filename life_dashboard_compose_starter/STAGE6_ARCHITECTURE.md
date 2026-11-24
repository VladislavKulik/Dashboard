> Исторический документ Stage 6. В Stage 7 список задач уже переведён на `TaskRepository` и `Flow`; остальные домены пока продолжают использовать описанный здесь фасад.

# Stage 6 — Repository contract, ViewModel and immutable UI state

## What changed

- Added `DashboardRepository`, a storage contract independent of Room.
- `RoomDashboardPersistence` now implements that contract. The state facade no longer depends on the concrete Room class.
- Added `DashboardViewModel`, created through a factory from the Compose navigation root. It owns the dashboard state facade for the Activity's ViewModel lifecycle.
- Added immutable `DashboardUiState` and a `StateFlow` read model. The store publishes a new snapshot after mutations and after Room restoration.
- Existing screens still consume the Compose-observable lists through `viewModel.store`. This is deliberate: the stage introduces the lifecycle and data boundaries without a risky all-at-once rewrite of every screen.

## Current data flow

```text
Compose screens
    ↓ user actions
DashboardViewModel
    ↓
DemoDashboardStore ── StateFlow<DashboardUiState>
    ↓
DashboardRepository
    ↓
RoomDashboardPersistence
    ↓
Room DAO / SQLite
```

## Important scope note

This is an incremental architecture step, not the final clean-architecture split. Business rules still live in `DemoDashboardStore`, and screens still observe its Compose state lists. The new `StateFlow` is available as the immutable presentation read model but the screens have not yet been migrated to collect it. Domain-specific repositories/use-cases are intentionally not duplicated on top of the current snapshot persistence layer.

A sensible next refactor is to move mutations into repository methods and migrate one feature at a time (Tasks first), then remove `DemoDashboardStore` and its Compose runtime dependency. Avoid moving all features at once.

## Dependency to add

The app module needs the Lifecycle ViewModel Compose integration matching the project's AndroidX Lifecycle version:

```kotlin
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:<lifecycle-version>")
```

Do not add a second version if the project already manages Lifecycle through a version catalog or Compose BOM.

## Validation boundary

Source-level checks can confirm the expected files and references, but a Gradle build and emulator run are still required. The supplied source bundle does not contain the Gradle wrapper, build scripts, manifest, or version catalog, so dependency compatibility and compilation cannot be confirmed here.
