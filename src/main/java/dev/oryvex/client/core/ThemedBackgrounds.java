package dev.oryvex.client.core;

import dev.oryvex.client.modules.misc.InterfaceMod;
import dev.oryvex.client.render.ColorUtil;
import dev.oryvex.client.render.NebulaBackground;
import dev.oryvex.client.render.RenderUtil;
import dev.oryvex.client.ui.theme.Theme;

/**
 * Dark-blue backgrounds injected into vanilla screens (options, multiplayer, containers).
 */
public final class ThemedBackgrounds {

    private ThemedBackgrounds() {
    }

    /** Replaces the dirt background used when no world is loaded. */
    public static void menu(int width, int height) {
        NebulaBackground.render(width, height);
        RenderUtil.rect(0.0F, 0.0F, width, height, ColorUtil.mulAlpha(Theme.OBSIDIAN, 0.35F));
    }

    /** Replaces the translucent overlay drawn over the live world. */
    public static void world(int width, int height) {
        RenderUtil.rect(0.0F, 0.0F, width, height, ColorUtil.mulAlpha(Theme.OBSIDIAN, 0.65F));
    }

    /** Lists (server list, resource packs...): translucent slate instead of dirt. */
    public static void list(int left, int top, int right, int bottom) {
        RenderUtil.rect(left, top, right - left, bottom - top, ColorUtil.mulAlpha(Theme.OBSIDIAN, 0.55F));
    }

    /** Header/footer strips above and below lists. */
    public static void listOverlay(int left, int width, int top, int bottom) {
        RenderUtil.rect(left, top, width, bottom - top, Theme.OBSIDIAN);
        if (top == 0) {
            RenderUtil.rect(left, bottom - 1, width, 1.0F, Theme.BORDER);
        } else {
            RenderUtil.rect(left, top, width, 1.0F, Theme.BORDER);
        }
    }
}
