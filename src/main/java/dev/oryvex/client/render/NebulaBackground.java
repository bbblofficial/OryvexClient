package dev.oryvex.client.render;

import dev.oryvex.client.ui.theme.Theme;

/**
 * Animated procedural nebula + perspective grid used behind every menu.
 */
public final class NebulaBackground {

    private static final long START = System.currentTimeMillis();
    private static float mouseX = 0.5F;
    private static float mouseY = 0.5F;

    private NebulaBackground() {
    }

    public static void render(float width, float height) {
        // Flat background only. Keep the old method signature so callers remain compatible.
        RenderUtil.rect(0.0F, 0.0F, width, height, Theme.OBSIDIAN_SOLID);
    }
}
