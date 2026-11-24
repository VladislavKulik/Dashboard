# Stage 26 — Quick creation flows

## What changed

The global Quick Add sheet now opens the creation UI directly for every supported item type:

- Task: opens the existing Add Task dialog on the Tasks screen.
- Project: opens the existing Project Editor dialog on the Projects screen.
- Note: opens the existing new-note editor route.
- Habit: opens the existing Habit Editor dialog on the Habits screen.
- Goal: opens the existing Goal Editor dialog on the Goals screen.

The implementation reuses existing editors, validation, ViewModel callbacks, repositories, and Room persistence. No parallel forms, new entities, database migrations, or backup format changes were introduced.

## Navigation behavior

Creation is requested through a `create=true` navigation argument for tasks, projects, habits, and goals. The argument defaults to false, so existing navigation paths continue to open normal list screens. Note creation already had a dedicated `note_details/new` route and is unchanged.

## Validation

Run `python3 validation/validate_stage26.py` from the project root. This is a source-level wiring check; it does not replace Gradle compilation or emulator testing.
