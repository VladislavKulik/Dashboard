# Stage 4 — Analytics & Habits

## Analytics

- Replaced the Analytics placeholder with a scrollable screen that follows the supplied mockup: Week/Month segmented selector, lavender completed-tasks card, dark project distribution card and mint activity trend card.
- The completed-tasks chart has selectable bars. Selecting a bar updates the day/week label and count below the chart.
- Project distribution is calculated from the current in-memory `DashboardTask` list grouped by project. The donut chart and legend use each project's existing pastel color.
- Week/month completed-task totals and activity trend line are seeded demo history. They are explicitly labelled as demonstration values in the UI because the current store does not keep a dated task-completion event log. Do not present these historic totals as production analytics until an event table is added.

## Habits

- Replaced the Habits placeholder with the pastel activity calendar, legend, habit cards, seven-day completion indicators and current streak badges.
- Added a habit creation dialog with title and pastel color selection.
- Tapping a habit opens Habit Details. The detail view shows its streak, schedule, last seven dates and a button to toggle today's completion.
- Tapping the final circle on a habit card also toggles today's completion.
- `DashboardHabit.completionDates` stores local `yyyy-MM-dd` date keys. Streak and monthly active-day percentage are calculated by `HabitMetrics`.
- The initial habit data is seeded relative to the device's current local date to demonstrate 14-day and 5-day streaks and a monthly active-day percentage close to the supplied 86% sample.

## Shared state

`DemoDashboardStore` remains the source of truth during the current process. Habit completion updates propagate to the calendar, card history, streak and details screen because all these UI surfaces read the same state list.

## Known limitations

- Habit and task data are in memory only. Force-stopping the app resets them to demo values.
- Analytics has no historical persistence or dated task event log yet. The bar chart and activity trend use sample values; distribution reflects the current in-memory task/project relationship.
- Habit reminders/notifications, schedules other than daily, editing/deleting habits and export are not implemented.
- No full Android Gradle build was run because the supplied starter archive does not include the user's Gradle wrapper/build files or full Android Studio project. Compose API compatibility must be confirmed in the target project.
