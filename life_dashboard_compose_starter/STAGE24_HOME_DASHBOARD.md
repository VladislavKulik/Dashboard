# Stage 24 — Home dashboard improvements

## What changed

- Replaced the hard-coded `14 hours` statistic with a completion percentage calculated from today's tasks.
- Added the current date above the Home greeting, formatted using the Russian locale.
- The task summary now counts tasks whose structured `dueDate` is today, with the existing `TODAY` category as a compatibility fallback for tasks without a structured date.
- Replaced the decorative fixed five-bar chart with a seven-day activity chart derived from persisted activity events.
- Replaced sample event text with the two latest real activity events. Empty history now has an explicit empty state.
- Made the activity and recent-events cards open the existing Calendar screen.
- Passed activity events from the shared dashboard state; no new persistence, Room migration, or backup schema was needed.
- Kept the existing pastel palette, rounded cards, responsive compact layout, and task interactions.

## Notes

- The activity chart counts recorded activity events, not time spent. It intentionally does not display invented hour estimates.
- The existing `showOnHome` flag remains the criterion for showing individual task rows on Home.
- The profile and notification icon actions remain unchanged; this stage does not invent profile or notification-center screens.

## Validation

Run `bash validation/run_stage24_checks.sh`.

The checks cover source wiring, removal of the fabricated hour value and sample event strings, seven-day aggregation, Calendar navigation, and Kotlin delimiter validation. They do not replace a full Android Gradle build or emulator UI test.
