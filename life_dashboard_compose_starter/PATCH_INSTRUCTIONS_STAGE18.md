# Applying Stage 18 patch

1. Make a copy or commit of your current Stage 17 project.
2. Unzip the patch into the project root and allow the listed files to replace their Stage 17 versions.
3. Keep your existing Gradle files and AndroidManifest; this stage adds no Android permissions or dependencies.
4. Build and run in Android Studio. Open Menu → Calendar, inspect a date with events, and tap task/goal/project/habit/activity entries to verify navigation.

Stage 18 does not change Room or backup schemas. It adds a computed calendar projection and new route only.
