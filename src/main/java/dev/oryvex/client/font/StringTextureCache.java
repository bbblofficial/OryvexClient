package dev.oryvex.client.font;

import dev.oryvex.client.render.GlTextures;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.text.AttributedString;
import java.text.Bidi;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Renders complex (RTL / mixed) strings as whole-line textures: contextual shaping by
 * {@link ArabicShaper}, BiDi reordering and glyph layout by {@link TextLayout} with
 * per-run fonts (Inter / Vazirmatn).
 */
final class StringTextureCache {

    private static final int MAX_ENTRIES = 512;
    private static final long MAX_IDLE_MS = 15_000L;

    static final class LineTexture {
        final int texture;
        final int width;
        final int height;
        final float baseline;
        final float padX;
        long lastUsed;

        LineTexture(int texture, int width, int height, float baseline, float padX) {
            this.texture = texture;
            this.width = width;
            this.height = height;
            this.baseline = baseline;
            this.padX = padX;
            this.lastUsed = System.currentTimeMillis();
        }
    }

    private final LinkedHashMap<String, LineTexture> entries = new LinkedHashMap<String, LineTexture>(256, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, LineTexture> eldest) {
            if (this.size() > MAX_ENTRIES) {
                GlTextures.delete(eldest.getValue().texture);
                return true;
            }
            return false;
        }
    };

    LineTexture get(String key, StyledText text, float pxSize) {
        LineTexture entry = this.entries.get(key);
        if (entry == null) {
            entry = build(text, pxSize);
            this.entries.put(key, entry);
        }
        entry.lastUsed = System.currentTimeMillis();
        return entry;
    }

    void evictIdle() {
        long now = System.currentTimeMillis();
        Iterator<LineTexture> it = this.entries.values().iterator();
        while (it.hasNext()) {
            LineTexture entry = it.next();
            if (now - entry.lastUsed > MAX_IDLE_MS) {
                GlTextures.delete(entry.texture);
                it.remove();
            }
        }
    }

    void clear() {
        for (LineTexture entry : this.entries.values()) {
            GlTextures.delete(entry.texture);
        }
        this.entries.clear();
    }

    private static LineTexture build(StyledText text, float pxSize) {
        TextLayout layout = layout(text, pxSize, true);
        float ascent = layout.getAscent();
        float descent = layout.getDescent();
        int pad = GlyphAtlas.PAD;
        int width = Math.max(1, (int) Math.ceil(layout.getAdvance()) + pad * 2 + 2);
        int height = Math.max(1, (int) Math.ceil(ascent + descent) + pad * 2);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        layout.draw(g, pad, pad + ascent);
        g.dispose();

        int texture = GlTextures.create(image);
        return new LineTexture(texture, width, height, pad + ascent, pad);
    }

    /**
     * Builds the shaped, bidi-aware layout of a styled string.
     *
     * @param withColors whether to attach per-character foreground colors
     */
    static TextLayout layout(StyledText text, float pxSize, boolean withColors) {
        ArabicShaper.Result shaped = ArabicShaper.shape(text.chars, text.length,
            c -> FontRegistry.canDisplay(FontRegistry.VAZIR, (char) c));
        String plain = shaped.text();
        if (plain.isEmpty()) {
            plain = " ";
        }
        int n = plain.length();
        int[] families = assignFamilies(plain);
        AttributedString attributed = new AttributedString(plain);
        Bidi bidi = new Bidi(text.plain(), Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);
        attributed.addAttribute(TextAttribute.RUN_DIRECTION,
            bidi.baseIsLeftToRight() ? TextAttribute.RUN_DIRECTION_LTR : TextAttribute.RUN_DIRECTION_RTL);

        int runStart = 0;
        for (int i = 1; i <= n; i++) {
            if (i == n || !sameRun(text, shaped, families, runStart, i, withColors)) {
                int src = sourceIndex(shaped, runStart, text.length);
                int style = src >= 0 ? text.style[src] : 0;
                boolean bold = (style & StyledText.BOLD) != 0;
                boolean italic = (style & StyledText.ITALIC) != 0;
                attributed.addAttribute(TextAttribute.FONT, FontRegistry.derive(families[runStart], bold, italic, pxSize), runStart, i);
                if (withColors) {
                    int rgb = src >= 0 ? text.rgb[src] : 0xFFFFFF;
                    attributed.addAttribute(TextAttribute.FOREGROUND, new Color(rgb & 0xFFFFFF), runStart, i);
                }
                if ((style & StyledText.UNDERLINE) != 0) {
                    attributed.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, runStart, i);
                }
                if ((style & StyledText.STRIKE) != 0) {
                    attributed.addAttribute(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON, runStart, i);
                }
                runStart = i;
            }
        }
        return new TextLayout(attributed.getIterator(), FontRegistry.FRC);
    }

    private static int sourceIndex(ArabicShaper.Result shaped, int outIndex, int sourceLength) {
        if (outIndex < shaped.length) {
            return shaped.outToSrc[outIndex];
        }
        return sourceLength > 0 ? sourceLength - 1 : -1;
    }

    private static boolean sameRun(StyledText text, ArabicShaper.Result shaped, int[] families, int a, int b, boolean colors) {
        if (families[a] != families[b]) {
            return false;
        }
        int sa = sourceIndex(shaped, a, text.length);
        int sb = sourceIndex(shaped, b, text.length);
        if (sa < 0 || sb < 0) {
            return sa == sb;
        }
        return text.style[sa] == text.style[sb] && (!colors || text.rgb[sa] == text.rgb[sb]);
    }

    /** Per-character family; neutral characters inherit from their neighbours. */
    static int[] assignFamilies(String text) {
        int n = text.length();
        int[] families = new int[n];
        int last = -1;
        for (int i = 0; i < n; i++) {
            char c = text.charAt(i);
            if (Scripts.isNeutral(c)) {
                families[i] = -1;
            } else {
                families[i] = FontRegistry.familyFor(c);
                last = families[i];
            }
        }
        int fill = last >= 0 ? last : FontRegistry.INTER;
        for (int i = 0; i < n; i++) {
            if (families[i] >= 0) {
                fill = families[i];
                break;
            }
        }
        for (int i = 0; i < n; i++) {
            if (families[i] < 0) {
                families[i] = fill;
            } else {
                fill = families[i];
            }
        }
        return families;
    }
}
