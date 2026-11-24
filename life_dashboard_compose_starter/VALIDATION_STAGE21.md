# Stage 21 validation

Automated checks performed in the available environment:

- Pure recurrence date calculation: daily, weekly selected weekdays, monthly end-of-month anchor, and no-repeat cases.
- SQLite migration `6 → 7`: existing task row retained and all new defaults verified.
- Kotlin compile-only check for Room entities/DAO/migrations, backup repository, persistence and recurrence calculator using the project's Android/Room/JSON stubs.
- Kotlin compile-only check for Tasks, Goals and Project Editor Compose screens using compile-only Compose/Material stubs.
- Kotlin compile-only check for all 18 Compose Preview functions.
- Kotlin delimiter scan across the source tree.
- Static Android Studio project configuration validation.

Not verified here:

- A full Android Gradle build and KSP Room code generation.
- Real Room migration on an Android device/emulator.
- Visual layout on a device and actual Compose Preview rendering.

Compile-only stubs and SQLite migration tests do not replace a real Gradle build. Open the project in Android Studio and run `:app:assembleDebug` before using it with personal data.
