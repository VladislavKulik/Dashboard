# Stage 18 — Calendar

## Scope

Stage 18 introduces a calendar as a read-only projection of existing Life Dashboard data. It does not add a Room table, a repository, or a new persisted event format. Repository-backed `DashboardUiState` remains the single source of truth.

## Calendar entries

- Tasks: `dueLabel` is parsed when it contains a supported date label. The current task state is shown as completed or open.
- Projects: `deadlineLabel` is parsed for active projects; completed projects are not given a deadline marker.
- Goals: `dueDate` uses the stored ISO `yyyy-MM-dd` key.
- Habits: every valid local `completionDates` key becomes a completion entry.
- Activity: event timestamps are converted to local calendar dates using the device time zone.

Supported task/project labels include `Сегодня`, `Завтра`, `Послезавтра`, `Вчера`, `через N дней`, ISO `yyyy-MM-dd`, numeric `d.M` / `d.M.yyyy`, and Russian month names/abbreviations such as `27 ноя`. Date labels without a year use the nearest calendar-year occurrence within a six-month heuristic window. Unknown/free-form labels are ignored rather than guessed.

## Screen behavior

- Month navigation is local to the screen; selected date defaults to today.
- Each date cell shows up to three event dots and an overflow count.
- Selecting a day displays all entries for that date, sorted by date, type, and title.
- Event taps navigate to the relevant section. Task and goal destinations scroll to and highlight the selected card; project and habit entries open their existing details screens; activity entries open Analytics.
- Calendar is available through Menu and intentionally does not alter the bottom navigation.

## Data and compatibility

No Room migration, DataStore key, notification behavior, backup schema, or JSON format version change is required. The calendar reads the current flows already combined by `DashboardViewModel`.

## Known limits

- Task and project deadlines are legacy display strings, not structured date fields. Only documented/common formats can be placed reliably on the calendar. Free-form text that cannot be parsed is omitted. A future refinement could replace these legacy labels with nullable ISO date fields through a separate data migration and editor update.
- Task tap opens the task list at the matching card; this project does not yet have a standalone Task Details destination.
- The screen has not been visually verified on physical Android devices because the original Gradle project configuration is not part of the supplied source package.
