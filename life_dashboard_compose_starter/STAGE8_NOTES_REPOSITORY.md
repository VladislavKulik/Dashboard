# Stage 8 — NotesRepository and Room Flow

## What changed

- Added `NotesRepository` as the feature-specific persistence contract for notes.
- `RoomDashboardPersistence` now implements `DashboardRepository`, `TaskRepository`, and `NotesRepository`.
- Room DAO exposes `observeNotes()` as a `Flow<List<NoteEntity>>`, plus focused read/write/delete methods.
- `DashboardViewModel` waits for first-launch seeding or Room restoration, then collects the notes Flow and updates the shared immutable `DashboardUiState`.
- Notes list, Menu counter, and Note Details resolve notes from `DashboardUiState`; note write actions no longer call `DemoDashboardStore` directly.
- Create/update, delete, and pin-toggle operations execute through the ViewModel and repository. UI navigation returns only after a save/delete transaction completes successfully.
- Repository failures are exposed as `noteOperationError` and shown on the Notes or Note Details screen.

## Data flow

```text
NotesScreen / NoteDetailsScreen
    ↓ callbacks
DashboardViewModel
    ↓ NotesRepository commands
RoomDashboardPersistence
    ↓ Room transaction
notes table
    ↓ Flow<List<NoteEntity>>
DashboardViewModel
    ↓ DemoDashboardStore.replaceNotesFromRepository()
StateFlow<DashboardUiState>
    ↓ collectAsState()
NotesScreen / Menu / Note Details
```

## Stale snapshot protection

After initial seeding, notes are owned by `NotesRepository`. General snapshot writes no longer clear or reinsert the notes table. This prevents a delayed habit/project snapshot from restoring an old note list after the user has edited, pinned, or deleted a note. Demo notes are inserted only by the awaited first-launch seed path.

The Room `notes` entity shape did not change, so this stage does not require a schema version bump or migration. The DAO observation orders rows by SQLite `rowid` descending, putting newly inserted/replaced notes first; the UI continues to sort pinned notes above unpinned notes.

## What remains transitional

- `DemoDashboardStore` still owns the shared mutable read model and initial demo data. It no longer exposes note write commands; notes are updated only from repository observation.
- Habits and parts of project editing still use the broad snapshot path and will be migrated separately.
- The single `DashboardViewModel` still coordinates more than one feature. A separate ViewModel per feature can be introduced after all feature repositories are extracted, without changing screen design.
- There is no explicit `updatedAt` timestamp for notes yet; `updatedLabel` remains presentation text.

## UI preserved

No visual redesign was made. Search, category filters, pinned-first ordering, two-column pastel cards, note colors, long body text, and delete confirmation are retained. Error text appears only if a database operation fails.
