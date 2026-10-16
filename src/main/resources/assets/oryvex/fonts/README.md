# Fonts

This folder is filled automatically: the GitHub Actions workflow and the `fetchFonts`
Gradle task download these files before `processResources` runs:

| File | Source | License |
|------|--------|---------|
| `Vazirmatn-Regular.ttf`, `Vazirmatn-Bold.ttf` | github.com/rastikerdar/vazirmatn | SIL OFL 1.1 |
| `Inter-Regular.ttf`, `Inter-Bold.ttf` | github.com/rsms/inter (v4.1 release, `extras/ttf`) | SIL OFL 1.1 |

They are git-ignored on purpose so the repository contains zero binary files.
If a file is missing at runtime the BiDi font engine falls back to the JVM `SansSerif` font.
