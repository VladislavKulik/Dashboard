#!/usr/bin/env python3
"""Static validation for the standalone Android Studio project setup."""
from pathlib import Path
import tomllib
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]

catalog = tomllib.loads((ROOT / "gradle/libs.versions.toml").read_text(encoding="utf-8"))
versions = catalog["versions"]
plugins = catalog["plugins"]
libraries = catalog["libraries"]
assert versions["agp"] == "9.3.1"
assert versions["kotlin"] == "2.4.21"
assert versions["ksp"] == "2.3.12"
assert versions["composeBom"] == "2026.09.00"
assert plugins["android-application"]["id"] == "com.android.application"
assert plugins["compose-compiler"]["id"] == "org.jetbrains.kotlin.plugin.compose"
assert plugins["ksp"]["id"] == "com.google.devtools.ksp"
for key in (
    "androidx-core-ktx",
    "androidx-activity-compose",
    "androidx-lifecycle-runtime-ktx",
    "androidx-lifecycle-viewmodel-ktx",
    "androidx-lifecycle-viewmodel-compose",
    "androidx-navigation-compose",
    "compose-runtime",
    "compose-foundation",
    "compose-ui",
    "compose-ui-tooling-preview",
    "compose-material3",
    "compose-material-icons-extended",
    "room-runtime",
    "room-ktx",
    "room-compiler",
    "datastore-preferences",
    "kotlinx-coroutines-android",
    "compose-ui-tooling",
):
    assert key in libraries, f"Missing dependency alias: {key}"

settings = (ROOT / "settings.gradle.kts").read_text(encoding="utf-8")
root_build = (ROOT / "build.gradle.kts").read_text(encoding="utf-8")
app_build = (ROOT / "app/build.gradle.kts").read_text(encoding="utf-8")
assert 'include(":app")' in settings
assert "FAIL_ON_PROJECT_REPOS" in settings
assert "alias(libs.plugins.android.application)" in root_build
assert "alias(libs.plugins.compose.compiler)" in root_build
assert "alias(libs.plugins.ksp)" in root_build
assert 'namespace = "com.example.lifedashboard"' in app_build
assert "compileSdk = 37" in app_build
assert "minSdk = 26" in app_build
assert "targetSdk = 36" in app_build
assert "compose = true" in app_build
assert "JvmTarget.JVM_17" in app_build
assert "ksp(libs.room.compiler)" in app_build
assert 'arg("room.schemaLocation"' in app_build
assert 'id("org.jetbrains.kotlin.android")' not in app_build

manifest = ET.parse(ROOT / "app/src/main/AndroidManifest.xml").getroot()
android_ns = "{http://schemas.android.com/apk/res/android}"
application = manifest.find("application")
assert application is not None
assert application.attrib[android_ns + "theme"] == "@style/Theme.LifeDashboard"
activities = application.findall("activity")
assert any(a.attrib.get(android_ns + "name") == ".MainActivity" for a in activities)
receivers = application.findall("receiver")
receiver_names = {r.attrib.get(android_ns + "name") for r in receivers}
assert ".reminders.HabitReminderReceiver" in receiver_names
assert ".reminders.HabitReminderRescheduleReceiver" in receiver_names
permissions = {p.attrib.get(android_ns + "name") for p in manifest.findall("uses-permission")}
assert "android.permission.POST_NOTIFICATIONS" in permissions
assert "android.permission.RECEIVE_BOOT_COMPLETED" in permissions
assert "android.permission.SCHEDULE_EXACT_ALARM" in permissions
ET.parse(ROOT / "app/src/main/res/values/styles.xml")
ET.parse(ROOT / "app/src/main/res/values/colors.xml")

preview_path = ROOT / "app/src/main/java/com/example/lifedashboard/ui/preview/LifeDashboardScreenPreviews.kt"
preview_source = preview_path.read_text(encoding="utf-8")
preview_count = preview_source.count("@Preview(")
assert preview_count == 18, f"Expected 18 previews, found {preview_count}"
assert "Home · Light" in preview_source and "Home · Dark" in preview_source

bootstrap_sh = (ROOT / "tools/bootstrap-gradle-wrapper.sh").read_text(encoding="utf-8")
bootstrap_ps = (ROOT / "tools/bootstrap-gradle-wrapper.ps1").read_text(encoding="utf-8")
assert "wrapper-project" in bootstrap_sh and "wrapper-project" in bootstrap_ps
assert "distributionSha256Sum" in bootstrap_sh and "distributionSha256Sum" in bootstrap_ps
assert "sha256sum" in bootstrap_sh and "shasum -a 256" in bootstrap_sh

for relative in (
    "tools/bootstrap-gradle-wrapper.sh",
    "tools/bootstrap-gradle-wrapper.ps1",
    "ANDROID_STUDIO_SETUP.md",
    "STAGE20_ANDROID_STUDIO.md",
    "VALIDATION_STAGE20.md",
    "app/schemas/README.md",
):
    assert (ROOT / relative).is_file(), f"Missing required file: {relative}"

print("Stage 20 Android Studio configuration validation passed.")
print(f"Validated {len(libraries)} dependency aliases and {preview_count} Compose previews.")
