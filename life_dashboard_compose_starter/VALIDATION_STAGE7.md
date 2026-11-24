# Stage 7 — validation

## Structural checks

- `TaskRepository` exists and is implemented by `RoomDashboardPersistence`.
- Room exposes `observeTasks()` as `Flow<List<TaskEntity>>`.
- Task mutations write the task, linked project counters, and optional activity event in one transaction.
- `DashboardViewModel` waits for initial persistence restore/seed before observing tasks or handling task commands.
- All visible task toggles/addition callbacks in the navigation root point to `DashboardViewModel`.
- Home, Tasks, Projects, Project Details and Analytics receive task data from the `StateFlow` snapshot.
- General snapshot writes seed the task table only when it is empty; they do not clear or replace it afterwards.
- Project task counters are preserved from Room during unrelated snapshot writes.

## Checks not performed

The Android Gradle build, Room annotation processing, emulator run, and UI interaction tests were not performed because the supplied source archive does not contain the Gradle wrapper, Gradle build files, manifest, version catalog, or Android SDK project configuration. A compiler parse attempt reports no Kotlin syntax diagnostics, but cannot type-check AndroidX/Room/Compose code without those dependencies.

## Manual checks after importing into the Android project

1. Build the app and confirm Room compiler accepts the new DAO Flow query and insert methods.
2. Fresh install: demo tasks appear after first-launch seeding.
3. Existing database: tasks restore without being replaced by demo values.
4. Toggle one task from Home and confirm its state is identical on Tasks and Project Details.
5. Add a project-linked task and confirm the project total/progress updates and survives app restart.
6. Toggle a task, open Analytics, and confirm the activity event is reflected.
7. Edit a note or habit after task changes and confirm the task state and project task counters are not reverted.
