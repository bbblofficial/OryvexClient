package dev.oryvex.client.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

/**
 * Per-frame render information shared by the UI, font engine and shaders.
 */
public final class RenderContext {

    private static int scaleFactor = 2;
    private static int scaledWidth = 427;
    private static int scaledHeight = 240;

    private RenderContext() {
    }

    public static void update() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.displayWidth <= 0 || mc.displayHeight <= 0) {
            return;
        }
        ScaledResolution sr = new ScaledResolution(mc);
        scaleFactor = Math.max(1, sr.getScaleFactor());
        scaledWidth = sr.getScaledWidth();
        scaledHeight = sr.getScaledHeight();
    }

    /** Physical pixels per GUI unit. */
    public static int scaleFactor() {
        return scaleFactor;
    }

    /** Raster scale used for text textures (never below 2 so glyphs stay crisp when scaled). */
    public static int textScale() {
        return Math.max(2, scaleFactor);
    }

    /** One physical pixel expressed in GUI units. */
    public static float pixel() {
        return 1.0F / scaleFactor;
    }

    public static int scaledWidth() {
        return scaledWidth;
    }

    public static int scaledHeight() {
        return scaledHeight;
    }
}
