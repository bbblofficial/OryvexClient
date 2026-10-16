#!/usr/bin/env bash
# Downloads the Inter + Vazirmatn TTF files used by the BiDi font engine.
set -euo pipefail
cd "$(dirname "$0")/.."
FONT_DIR=src/main/resources/assets/oryvex/fonts
mkdir -p "$FONT_DIR"
for weight in Regular Bold; do
  curl -fsSL --retry 3 -o "$FONT_DIR/Vazirmatn-$weight.ttf" \
    "https://raw.githubusercontent.com/rastikerdar/vazirmatn/master/fonts/ttf/Vazirmatn-$weight.ttf" \
  || curl -fsSL --retry 3 -o "$FONT_DIR/Vazirmatn-$weight.ttf" \
    "https://cdn.jsdelivr.net/gh/rastikerdar/vazirmatn@v33.003/fonts/ttf/Vazirmatn-$weight.ttf"
done
TMP="$(mktemp -d)"
curl -fsSL --retry 3 -o "$TMP/inter.zip" "https://github.com/rsms/inter/releases/download/v4.1/Inter-4.1.zip"
unzip -o -j "$TMP/inter.zip" "extras/ttf/Inter-Regular.ttf" "extras/ttf/Inter-Bold.ttf" -d "$FONT_DIR"
rm -rf "$TMP"
ls -la "$FONT_DIR"
