# Stage 23 — Calendar improvements

## What changed

- Added a calendar event-type filter: all, tasks, projects, goals, habits, and activity. The selected filter applies consistently to month markers and the selected-day agenda.
- Task events now expose their configured reminder time. Enabled reminders appear in the day agenda as `Напоминание · HH:mm`; disabled reminders remain hidden.
- Calendar events are ordered by date, event category, reminder time, and title.
- Month navigation now preserves the selected day where possible and clamps it to the target month's length (for example, the 31st becomes the 28th/29th in February).
- Increased day-cell height slightly to make date selection and event markers easier to scan.
- Kept the calendar as a read-only projection of repository data. No new Room table, migration, or backup-format change was introduced.

## Existing behavior retained

- Period filters: all dates, next seven days, selected month, and overdue items.
- Month grid, today shortcut, event markers, selected-day agenda, and navigation to the source item.
- Existing supported legacy date labels and structured `LocalDate` dates.

## Validation

Run `bash validation/run_stage23_checks.sh`. This covers focused calendar projection tests, reminder-time propagation, filter/navigation source checks, and Kotlin delimiter validation. It does not replace a full Android Gradle build or emulator UI test.
