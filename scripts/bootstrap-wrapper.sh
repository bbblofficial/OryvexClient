#!/usr/bin/env bash
# Generates the Gradle 4.10.3 wrapper locally (the repo intentionally ships no wrapper jar).
set -euo pipefail
cd "$(dirname "$0")/.."
GRADLE_VERSION=4.10.3
TMP="$(mktemp -d)"
curl -fsSL -o "$TMP/gradle.zip" "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
unzip -q "$TMP/gradle.zip" -d "$TMP"
mkdir -p "$TMP/gen" && touch "$TMP/gen/settings.gradle"
( cd "$TMP/gen" && "$TMP/gradle-${GRADLE_VERSION}/bin/gradle" wrapper --gradle-version "$GRADLE_VERSION" --distribution-type bin --no-daemon -q )
cp "$TMP/gen/gradlew" "$TMP/gen/gradlew.bat" .
mkdir -p gradle/wrapper && cp "$TMP/gen/gradle/wrapper/"* gradle/wrapper/
chmod +x gradlew
rm -rf "$TMP"
echo "Wrapper ready: ./gradlew setupDecompWorkspace build"
