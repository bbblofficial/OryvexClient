# OryvexClient

PvP & utility client for **Minecraft 1.8.9** (Forge 11.15.1.2318), built with
Java 8, ForgeGradle 2.1, SpongePowered Mixin 0.7.11 and LWJGL 2.

Deep-black / electric-blue glass UI, an automatic Persian (RTL) + English (LTR)
font engine, an offline alt manager, a Badlion-style mod dashboard with a magnetic
HUD editor, and the usual PvP modules.

## Build

The repo ships **zero binaries** (no wrapper jar, no fonts). Two ways to build:

**GitHub Actions** (recommended): push the repo; `.github/workflows/build.yml`
installs Temurin JDK 8, downloads Vazirmatn + Inter, generates the Gradle 4.10.3
wrapper, runs `./gradlew setupCiWorkspace build shadowJar --stacktrace` and uploads
`OryvexClient-1.8.9.jar` as the `OryvexClient-1.8.9` artifact.

**Locally** (JDK 8 required):

```bash
./scripts/bootstrap-wrapper.sh      # creates ./gradlew for Gradle 4.10.3
./scripts/fetch-fonts.sh            # optional: the fetchFonts task also does this
./gradlew setupDecompWorkspace      # first time only (IDE sources)
./gradlew build                     # -> build/libs/OryvexClient-1.8.9.jar
./gradlew runClient                 # dev client with Mixin tweaker
```

Install: drop `OryvexClient-1.8.9.jar` into the `mods` folder of a Forge
1.8.9 (11.15.1.2318) profile. The jar is a tweaker-mod: Forge loads the Mixin
tweaker from the manifest and then the `@Mod`.

## Features

| Area | What it does |
|------|--------------|
| Theme | Obsidian `#07080A`/90%, slate cards `#0E1117`/85%, 1px `#1C2433` borders, flat muted blue accent, no gradients or glow. SDF rounded-rect shader for every surface. |
| Glass | Dual-Kawase blur (down + up passes, `kawase_down.fsh` / `kawase_up.fsh`) composited into rounded frames by `blur_mask.fsh`. |
| Animation | Delta-time `AnimationClock`; `Animation` tweens with `CubicBezier`, `EXPO_OUT`, `BACK_OUT`; `SmoothValue` for scrolling. |
| Font engine | `BiDiFontRenderer`: Inter for Latin, Vazirmatn for Arabic script (U+0600–06FF, FB50–FDFF, FE70–FEFF) in the same string, no language toggle. `ArabicShaper` does Isolated/Initial/Medial/Final + Lam-Alef (پ چ ژ گ ک ی included, ZWNJ/ZWJ aware); `java.text.Bidi` + `TextLayout` reorder. Latin uses glyph atlases, RTL lines are cached textures. Hooked into the vanilla `FontRenderer`, so chat, HUD, buttons, tooltips and nametags all shape correctly. |
| Menus | `OryvexMainMenu` (flat background, solid nav, session widget with skin head), `OryvexIngameMenu` (solid live-world tint), themed `GuiButton`, sliders, list backgrounds for `GuiMultiplayer` / `GuiOptions` and every vanilla screen. |
| Alt manager | Offline login (3–16 chars), `UUID.nameUUIDFromBytes("OfflinePlayer:" + name)`, `new Session(name, uuid, "0", "legacy")` swapped at runtime, random PvP names, favourites, `oryvex/alts.json`. |
| Dashboard | `RSHIFT` or the pause menu. Search (EN/FA), All/HUD/PvP/Visuals/Misc tabs, profiles, card grid with pill toggles, settings drawer (sliders, modes, HSB color picker, keybinds). |
| HUD editor | Drag & drop with centre/edge/element snapping guides, wheel scaling, right-click reset. |
| Modules | Keystrokes, ToggleSprint, ArmorStatus, PotionStatus, OldAnimations, PerformanceHUD (FPS, ping, CPS, reach, combo, coords), Visuals (fullbright, crosshair, block overlay, hit color, motion blur), ChatTweaks, Interface. |

## Notes

* **Session mixin:** Mixin 0.7.11's `@Mutable` targets fields only (`@Target(FIELD)`),
  so `@Accessor @Mutable` on an interface method does not compile on that version.
  `MixinMinecraft` therefore shadows `session` as `@Shadow @Final @Mutable` and
  implements the `IMixinMinecraft` duck interface (`oryvex$setSession`).
* **Offline sessions** only authenticate on servers running `online-mode=false`
  (LAN worlds, offline/test servers). Premium servers will reject them.
* Reach and combo are display-only statistics; no module changes hit detection,
  reach, velocity or any other gameplay value.
* Config lives in `.minecraft/oryvex/` (`profiles/*.json`, `client.json`, `alts.json`).

## Layout

```
src/main/java/dev/oryvex/client
├── OryvexClient.java          @Mod entry point
├── alt/                       offline sessions, alt store, name generator
├── anim/                      AnimationClock, Animation, Easing, CubicBezier, SmoothValue
├── bridge/                    IMixinMinecraft duck interface
├── config/                    profiles + JSON IO
├── core/                      Forge events, screen router, themed backgrounds, render context
├── font/                      BiDiFontRenderer, ArabicShaper, glyph atlas, line cache
├── hud/                       HudElement, renderer, style
├── mixin/                     all Mixins (see mixins.oryvex.json)
├── module/ + modules/         module system and features
├── render/                    shaders, Kawase blur, nebula, motion blur, primitives
├── setting/                   typed settings
├── ui/                        screens, ClickGui, widgets, theme
└── util/                      CPS + combat trackers
```
