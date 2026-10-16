# Oryvex UI/HUD review

- UI and HUD gradient entry points now render a single solid color; font gradients are flat.
- Glow/bloom calls are retained only as no-op compatibility methods, so existing modules cannot re-enable halos.
- Menu nebula and glass blur paths were removed from active rendering; surfaces use solid fills and borders.
- HUD scale tweens use a monotonic cubic curve, capped frame delta, and center anchoring to avoid clipping/jumps.
- The editor now exposes the scoreboard and tab scoreboard as first-class draggable/scalable HUD elements. Their positions and scales persist through the existing config serializer.
- Static checks: Java brace balance passed. A Gradle compile could not be run in this sandbox because the project has no Gradle wrapper and dependencies are not available locally.
- Chat input now has a short, frame-smooth settle animation for typed/edited text and caret movement, while keystrokes remain immediate.
- Emoji/unicode supplementary characters now render as full code points instead of two independent UTF-16 surrogate glyphs; font selection, measurement, atlas rasterization, and cursor advance stay aligned.
