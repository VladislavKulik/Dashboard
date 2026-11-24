#!/usr/bin/env sh
set -eu

GRADLE_VERSION="9.5.0"
PROJECT_ROOT="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
BOOTSTRAP_ROOT="${XDG_CACHE_HOME:-$HOME/.cache}/lifedashboard/gradle-bootstrap"
GRADLE_HOME="$BOOTSTRAP_ROOT/gradle-$GRADLE_VERSION"
ZIP_PATH="$BOOTSTRAP_ROOT/gradle-$GRADLE_VERSION-bin.zip"
CHECKSUM_PATH="$ZIP_PATH.sha256"
WRAPPER_PROJECT="$BOOTSTRAP_ROOT/wrapper-project"

mkdir -p "$BOOTSTRAP_ROOT"
command -v curl >/dev/null 2>&1 || { echo 'curl is required.' >&2; exit 1; }
command -v unzip >/dev/null 2>&1 || { echo 'unzip is required.' >&2; exit 1; }
if command -v sha256sum >/dev/null 2>&1; then
    hash_file() { sha256sum "$1" | awk '{print tolower($1)}'; }
elif command -v shasum >/dev/null 2>&1; then
    hash_file() { shasum -a 256 "$1" | awk '{print tolower($1)}'; }
else
    echo 'sha256sum or shasum is required.' >&2
    exit 1
fi

if [ ! -f "$CHECKSUM_PATH" ]; then
    curl -fL "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip.sha256" -o "$CHECKSUM_PATH"
fi
EXPECTED_HASH="$(awk '{print tolower($1)}' "$CHECKSUM_PATH")"

if [ ! -x "$GRADLE_HOME/bin/gradle" ]; then
    echo "Downloading Gradle $GRADLE_VERSION..."
    curl -fL "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" -o "$ZIP_PATH"
    ACTUAL_HASH="$(hash_file "$ZIP_PATH")"
    if [ "$EXPECTED_HASH" != "$ACTUAL_HASH" ]; then
        rm -f "$ZIP_PATH"
        echo 'Gradle distribution SHA-256 checksum mismatch. Download was rejected.' >&2
        exit 1
    fi
    unzip -q -o "$ZIP_PATH" -d "$BOOTSTRAP_ROOT"
fi

# Generate the wrapper in an empty temporary Gradle project. This avoids
# resolving the Android plugin or requiring an installed Android SDK first.
rm -rf "$WRAPPER_PROJECT"
mkdir -p "$WRAPPER_PROJECT"
printf "rootProject.name = 'lifedashboard-wrapper-bootstrap'\n" > "$WRAPPER_PROJECT/settings.gradle"
cd "$WRAPPER_PROJECT"
"$GRADLE_HOME/bin/gradle" wrapper --gradle-version "$GRADLE_VERSION" --distribution-type bin

WRAPPER_DIR="$PROJECT_ROOT/gradle/wrapper"
mkdir -p "$WRAPPER_DIR"
cp "$WRAPPER_PROJECT/gradlew" "$PROJECT_ROOT/gradlew"
cp "$WRAPPER_PROJECT/gradlew.bat" "$PROJECT_ROOT/gradlew.bat"
cp "$WRAPPER_PROJECT/gradle/wrapper/gradle-wrapper.jar" "$WRAPPER_DIR/gradle-wrapper.jar"
cp "$WRAPPER_PROJECT/gradle/wrapper/gradle-wrapper.properties" "$WRAPPER_DIR/gradle-wrapper.properties"
printf '\ndistributionSha256Sum=%s\n' "$EXPECTED_HASH" >> "$WRAPPER_DIR/gradle-wrapper.properties"
chmod +x "$PROJECT_ROOT/gradlew"

printf '\nGradle Wrapper generated and distribution checksum configured.\n'
printf 'Next: open this folder in Android Studio and sync the project.\n'
