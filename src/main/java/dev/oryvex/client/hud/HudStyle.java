package dev.oryvex.client.hud;

import dev.oryvex.client.render.RoundedRenderer;
import dev.oryvex.client.ui.theme.Theme;

/**
 * Shared look for HUD widgets.
 */
public final class HudStyle {

    public static final float PADDING = 5.0F;
    public static final float RADIUS = 4.0F;

    private HudStyle() {
    }

    public static void panel(float x, float y, float w, float h, boolean background) {
        if (!background) {
            return;
        }
        RoundedRenderer.bordered(x, y, w, h, RADIUS, Theme.SLATE, 1.0F, Theme.BORDER);
    }

    public static void accentBar(float x, float y, float h) {
        RoundedRenderer.gradient(x, y, 2.0F, h, 1.0F, Theme.ACCENT_START, Theme.ACCENT_END, true);
    }
}
