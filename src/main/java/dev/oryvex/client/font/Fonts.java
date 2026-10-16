package dev.oryvex.client.font;

import dev.oryvex.client.render.ColorUtil;

/**
 * Convenience facade over {@link BiDiFontRenderer} for Oryvex UI code.
 */
public final class Fonts {

    private Fonts() {
    }

    private static BiDiFontRenderer r() {
        return BiDiFontRenderer.get();
    }

    public static float draw(String text, float x, float y, int color, float size) {
        return r().drawString(text, x, y, color, size, false, false);
    }

    public static float draw(String text, float x, float y, int color, float size, boolean bold) {
        return r().drawString(text, x, y, color, size, bold, false);
    }

    public static float drawShadow(String text, float x, float y, int color, float size, boolean bold) {
        return r().drawString(text, x, y, color, size, bold, true);
    }

    public static float width(String text, float size) {
        return r().getWidth(text, size, false);
    }

    public static float width(String text, float size, boolean bold) {
        return r().getWidth(text, size, bold);
    }

    /** Horizontally centred on {@code cx}, top at {@code y}. */
    public static void drawCenterX(String text, float cx, float y, int color, float size, boolean bold) {
        draw(text, cx - width(text, size, bold) / 2.0F, y, color, size, bold);
    }

    /** Centred on both axes around ({@code cx}, {@code cy}) using cap-height centring. */
    public static void drawCenter(String text, float cx, float cy, int color, float size, boolean bold) {
        draw(text, cx - width(text, size, bold) / 2.0F, cy - size * 0.42F, color, size, bold);
    }

    /** Left aligned at {@code x}, vertically centred on {@code cy}. */
    public static float drawMiddle(String text, float x, float cy, int color, float size, boolean bold) {
        return draw(text, x, cy - size * 0.42F, color, size, bold);
    }

    /** Right aligned so the text ends at {@code right}, vertically centred on {@code cy}. */
    public static void drawRightMiddle(String text, float right, float cy, int color, float size, boolean bold) {
        draw(text, right - width(text, size, bold), cy - size * 0.42F, color, size, bold);
    }

    /** Per-character horizontal gradient (LTR text). */
    /** Compatibility API rendered as one flat color. */
    public static float drawGradient(String text, float x, float y, float size, boolean bold, int from, int to) {
        return draw(text, x, y, from, size, bold);
    }

    /** Trims text with an ellipsis so it fits {@code maxWidth}. */
    public static String trim(String text, float maxWidth, float size, boolean bold) {
        if (width(text, size, bold) <= maxWidth) {
            return text;
        }
        String ellipsis = "...";
        float ellipsisWidth = width(ellipsis, size, bold);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            sb.append(text.charAt(i));
            if (width(sb.toString(), size, bold) + ellipsisWidth > maxWidth) {
                sb.setLength(Math.max(0, sb.length() - 1));
                break;
            }
        }
        return sb + ellipsis;
    }

    /** Greedy word wrap. */
    public static java.util.List<String> wrap(String text, float maxWidth, float size) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (width(candidate, size) > maxWidth && line.length() > 0) {
                lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(candidate);
            }
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }
        return lines;
    }
}
