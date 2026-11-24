# Validation — Stage 20

## Automated checks

1. Python `tomllib` parsed `gradle/libs.versions.toml` and verified required plugin/library versions.
2. XML parser validated `AndroidManifest.xml`, `styles.xml` and `colors.xml`.
3. Static Gradle configuration checks verified plugin aliases, repositories, app namespace, SDK levels, Compose flag, Room KSP, and required runtime/tooling dependencies.
4. Preview catalog compilation passed using compile-only Compose/screen-contract stubs. This validates the preview Kotlin source shape and callback signatures but does not render Android Studio previews.
5. Existing Stage 19 validators and data migration checks were rerun.
6. Bash bootstrap script passed `bash -n`.
7. Wrapper bootstrap scripts include SHA-256 validation against the official Gradle checksum endpoint.
8. ZIP archives passed integrity verification.

## Not verified here

- Gradle sync and `:app:assembleDebug` (no Gradle distribution, Android SDK, or outbound dependency access in the preparation environment).
- Actual KSP Room code generation and Android resource merge.
- Compose Preview rendering inside Android Studio.
- Live Edit on a running emulator/device.
- Installation and runtime behavior on Android.

The first Android Studio sync/build is therefore a required verification step and must not be treated as already passed.
