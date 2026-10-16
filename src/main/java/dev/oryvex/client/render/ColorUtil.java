package dev.oryvex.client.render;

import java.awt.Color;

public final class ColorUtil {

    private ColorUtil() {
    }

    public static int alpha(int c) {
        return c >>> 24;
    }

    public static int red(int c) {
        return c >> 16 & 0xFF;
    }

    public static int green(int c) {
        return c >> 8 & 0xFF;
    }

    public static int blue(int c) {
        return c & 0xFF;
    }

    public static int argb(int a, int r, int g, int b) {
        return (clamp255(a) << 24) | (clamp255(r) << 16) | (clamp255(g) << 8) | clamp255(b);
    }

    private static int clamp255(int v) {
        return v < 0 ? 0 : (v > 255 ? 255 : v);
    }

    /** Multiplies the alpha channel by {@code factor}. */
    public static int mulAlpha(int color, float factor) {
        int a = Math.round(alpha(color) * Math.max(0.0F, Math.min(1.0F, factor)));
        return (a << 24) | (color & 0xFFFFFF);
    }

    public static int withAlpha(int color, int alpha) {
        return (clamp255(alpha) << 24) | (color & 0xFFFFFF);
    }

    public static int lerp(int from, int to, float t) {
        float k = Math.max(0.0F, Math.min(1.0F, t));
        int a = Math.round(alpha(from) + (alpha(to) - alpha(from)) * k);
        int r = Math.round(red(from) + (red(to) - red(from)) * k);
        int g = Math.round(green(from) + (green(to) - green(from)) * k);
        int b = Math.round(blue(from) + (blue(to) - blue(from)) * k);
        return argb(a, r, g, b);
    }

    public static int fromHsb(float hue, float saturation, float brightness, int alpha) {
        int rgb = Color.HSBtoRGB(hue, saturation, brightness) & 0xFFFFFF;
        return (clamp255(alpha) << 24) | rgb;
    }

    public static float[] toHsb(int color) {
        return Color.RGBtoHSB(red(color), green(color), blue(color), null);
    }

    public static String toHex(int color) {
        return String.format("#%06X", color & 0xFFFFFF);
    }
}
