# Stage 10 — ProjectsRepository

## What changed

- Added `ProjectsRepository` with a Room-backed `Flow<List<DashboardProject>>`.
- `RoomDashboardPersistence` now implements the projects contract.
- Projects and their stages are observed together and mapped into the existing UI model.
- `saveProject` writes the project and replaces its stages in one Room transaction.
- `toggleStage` changes completion/current-stage flags and the stage label transactionally.
- `DashboardViewModel` collects project updates and exposes `saveProject` / `toggleProjectStage` commands with an operation error state.
- The transitional `DemoDashboardStore` only mirrors repository emissions; it does not persist project changes.
- Initial demo projects are seeded only on first launch. Later snapshot saves from other features no longer clear and rewrite projects.

## Compatibility notes

- Existing Projects and Project Details layouts are unchanged.
- The current screens are read-only for project metadata and stages. The ViewModel commands are ready for a future edit/stage interaction, but no new controls were added in this stage.
- Task completion still updates the linked project's task counters atomically through `TaskRepository.saveMutation`; project Flow then refreshes the shared UI state.
- Stage completion is intentionally independent of task progress. It updates `stageLabel`, not task completion counts or `isCompleted`.

## Next

Stage 11 should remove the remaining `DemoDashboardStore` responsibilities incrementally, beginning with activity events and dashboard read-state composition. Add explicit UI editing only when the corresponding screen design is specified.
