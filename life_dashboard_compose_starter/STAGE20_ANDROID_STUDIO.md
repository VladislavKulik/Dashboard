# Stage 20 — Android Studio project setup and live UI previews

## Added

- Standalone Gradle Android project files (`settings.gradle.kts`, root/app `build.gradle.kts`, `gradle/libs.versions.toml`, `gradle.properties`).
- Version-compatible toolchain: AGP 9.3.1, Kotlin 2.4.21, Compose compiler plugin 2.4.21, KSP 2.3.12, Gradle Wrapper target 9.5.0.
- Dependencies for Compose UI/Material 3, Activity Compose, Navigation Compose, Lifecycle ViewModel, Room, DataStore and coroutines.
- Android app namespace/applicationId `com.example.lifedashboard`, Manifest theme and values resources.
- Room schema export configuration (`app/schemas`).
- An 18-entry Compose Preview catalog for the main screens and detail screens, including compact-phone and tablet variants for Home/Tasks/Projects, using local sample data only.
- PowerShell and Bash bootstrap scripts which download Gradle from the official distribution service, validate its SHA-256 checksum, generate the wrapper in a temporary empty Gradle project and copy the scripts/JAR into the app project with distribution checksum pinning.
- Android Studio setup instructions, Preview and Live Edit workflow.

## Important toolchain choice

Kotlin 2.4.21's documented AGP compatibility ceiling is 9.3.1. The project therefore uses AGP 9.3.1 rather than AGP 9.5.x, even though newer AGP patches exist. AGP 9 has built-in Kotlin support; `org.jetbrains.kotlin.android` is intentionally not applied. Kotlin Gradle Plugin 2.4.21 is explicitly pinned on the root buildscript classpath to align the compiler version with Compose Compiler 2.4.21. Compose Compiler and KSP are applied as their own plugins.

## Validation status

- Version catalog parsed with Python TOML parser.
- Manifest and resource XML parsed.
- Preview catalog compiled with compile-only API stubs; this validates Kotlin syntax and callback signatures against the screen contracts, not actual Compose rendering.
- Existing Stage 19 validators and Room/date migration checks rerun.
- Gradle wrapper bootstrap scripts passed shell syntax validation and SHA-256 checking logic was inspected.
- ZIP integrity verified.

A real Gradle sync/build was not possible in the archive-preparation environment because it has no Gradle distribution or Android SDK and outbound network access to Gradle/Maven repositories is unavailable. The Wrapper bootstrap scripts create the official Wrapper on the user's machine, after which Android Studio can resolve dependencies and perform the actual build.
