# Stage 8 — validation

## Completed checks

- `NotesRepository` contract is present and implemented by `RoomDashboardPersistence`.
- DAO exposes `Flow<List<NoteEntity>>`, note lookup, upsert, and delete operations.
- `DashboardViewModel.Factory` passes the same Room adapter for dashboard, task, and notes repository contracts.
- The notes Flow collection starts only after `DemoDashboardStore.awaitReady()`, preventing the initial empty query from replacing the seeded state before first-launch writes complete.
- Navigation sends note save/delete/pin commands through `DashboardViewModel`; note list, details, and Menu counter read from `DashboardUiState`.
- General snapshot writes no longer clear or replace notes after initialization. Seed insertion is restricted to the awaited first-launch path.
- Legacy Store note write methods have been removed so the UI cannot accidentally bypass `NotesRepository`.
- Core data, repository, Room-adapter, Store, and ViewModel Kotlin sources passed a `kotlinc` type-check using local AndroidX/Room API stubs and the installed coroutines library.
- `DemoDashboardStore.replaceNotesFromRepository()` passed a smoke test confirming that the shared `StateFlow<DashboardUiState>` receives the updated list.
- The SQLite observation query `SELECT * FROM notes ORDER BY rowid DESC` passed a small in-memory SQLite check.

## Not verified

- Android Gradle build, Room KSP/KAPT annotation processing, emulator/device execution, and Compose UI interaction tests were not run. The input source package does not include Gradle wrapper/build files, manifest, version catalog, or Android SDK project configuration.
- Local stub-based compilation verifies Kotlin type relationships in the data/presentation slice; it does not substitute for Room compiler validation or a real Android build.

## Manual checks after importing into the Android project

1. Build the app and confirm Room validates `observeNotes()` and the new DAO methods.
2. Fresh install: demo notes appear, and pinned notes remain above unpinned notes.
3. Existing Stage 7 database: old notes still load without a destructive migration.
4. Create a note, navigate back, reopen it, and verify title/body/category/color/pin survive app restart.
5. Edit a note and verify the Menu count does not change.
6. Delete a note, restart the app, and confirm it does not reappear.
7. Toggle pin rapidly and confirm the final state matches the number of taps.
8. Make a habit change after editing a note; verify the note changes are not rolled back by the snapshot writer.
9. Temporarily force a database exception and verify the editor remains open and displays an error.
