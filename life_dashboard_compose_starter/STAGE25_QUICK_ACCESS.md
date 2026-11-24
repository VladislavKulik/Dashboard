# Stage 25 — Home quick access

## What changed

- Added a compact `Быстрый доступ` row to the Home screen with shortcuts to Tasks, Notes, and Habits.
- Shortcuts reuse the existing navigation destinations and repository-backed screens; they do not introduce duplicate create flows or new persistence.
- Kept the existing pastel palette, rounded geometry, and bottom navigation quick-add sheet unchanged.
- Added optional callbacks to `HomeScreen` so previews and other call sites remain source-compatible.

## Behavior

- `Задачи` opens the existing Tasks screen.
- `Заметки` opens the existing Notes screen.
- `Привычки` opens the existing Habits screen.
- Actual item creation continues to use the existing add controls and Quick Add sheet. These shortcuts are navigation shortcuts, not one-tap object creation.

## Validation

Run `bash validation/run_stage25_checks.sh`.
The checks verify shortcut wiring, route reuse, and Kotlin delimiter balance. They do not replace a full Android Gradle build or emulator UI test.
