package dev.oryvex.client.render;

import dev.oryvex.client.ui.theme.Theme;

/** Solid menu background renderer retained behind the existing menu API. */
public final class NebulaBackground {

    private NebulaBackground() {
    }

    public static void render(float width, float height) {
        // Flat background only. Keep the old method signature so callers remain compatible.
        RenderUtil.rect(0.0F, 0.0F, width, height, Theme.OBSIDIAN_SOLID);
    }
}
