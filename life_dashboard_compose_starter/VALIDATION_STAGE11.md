# Stage 11 validation

## Completed checks

- Compiled the changed data, repository, Room adapter and ViewModel Kotlin sources with `kotlinc` against lightweight Android/Room/Lifecycle API stubs and the bundled Coroutines library. Compilation succeeded.
- Confirmed that the `data` package contains no `androidx.compose` imports, `mutableStateListOf`, `DemoDashboardStore`, `Snapshot` or `TaskMutation` references.
- Confirmed that `DashboardViewModel` has no Store dependency and combines the five repository streams into `DashboardUiState`.
- Confirmed all `DashboardProject` constructors and UI color conversions use `colorArgb`.
- Confirmed the database version is 2 and the explicit migration 1→2 adds the task order column and metadata table without dropping user tables; existing task row order is copied from `rowid`.
- Verified archive integrity after packaging.

## Not verified

- A complete Android Gradle build, Room KSP/KAPT code generation, installation and emulator/device run could not be performed because the source bundle does not include the user's Gradle Wrapper, build scripts, version catalog, Android manifest or SDK project configuration.
- The stub compilation checks Kotlin syntax and core contracts, but it is not a substitute for the real Android build or Room schema validation.

## Integration note

The app must retain compatible Room runtime/compiler and lifecycle ViewModel Compose dependencies. The database migration is registered in `RoomDashboardPersistence`; do not replace it with `fallbackToDestructiveMigration()` because that could erase user data.
