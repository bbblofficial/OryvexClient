package dev.oryvex.client.font;

import dev.oryvex.client.core.RenderContext;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.awt.Shape;
import java.awt.font.TextLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Automatic bidirectional font renderer.
 *
 * <ul>
 *   <li>Latin / LTR text is drawn per glyph from TTF atlases (Inter), tinted per formatting code.</li>
 *   <li>Strings containing Persian/Arabic (or any RTL) characters are shaped
 *       (Isolated/Initial/Medial/Final + Lam-Alef), reordered with java.text.Bidi via
 *       TextLayout and drawn as cached line textures, mixing Vazirmatn and Inter per run.</li>
 * </ul>
 * There is no language toggle: the path is chosen per string from its code points.
 */
public final class BiDiFontRenderer {

    /** Baseline position as a fraction of the font size below the top of the line box. */
    public static final float BASELINE = 0.78F;
    private static final String OBFUSCATION_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final float WIDTH_PRECISION = 4.0F;

    private static final BiDiFontRenderer INSTANCE = new BiDiFontRenderer();

    private final Map<Long, GlyphAtlas> atlases = new HashMap<>();
    private final StringTextureCache lineCache = new StringTextureCache();
    private final Map<String, Float> complexWidths = new LinkedHashMap<String, Float>(512, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Float> eldest) {
            return this.size() > 4096;
        }
    };
    private final Random random = new Random();
    private final List<float[]> decorations = new ArrayList<>();
    private long lastGc;

    private BiDiFontRenderer() {
    }

    public static BiDiFontRenderer get() {
        return INSTANCE;
    }

    public boolean isReady() {
        return FontRegistry.isLoaded();
    }

    public static boolean containsComplex(String text) {
        return Scripts.containsComplex(text);
    }

    /** True when the paragraph direction of {@code text} is right-to-left. */
    public static boolean isRtlParagraph(String text) {
        if (!Scripts.containsComplex(text)) {
            return false;
        }
        return !new java.text.Bidi(StyledText.strip(text), java.text.Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT).baseIsLeftToRight();
    }

    // ------------------------------------------------------------------ Oryvex UI API

    /**
     * Draws text with the top of its line box at {@code y}.
     *
     * @return advance width in GUI units
     */
    public float drawString(String text, float x, float y, int argb, float size, boolean bold, boolean shadow) {
        if (text == null || text.isEmpty()) {
            return 0.0F;
        }
        float alpha = (argb >>> 24) / 255.0F;
        if (alpha <= 0.01F) {
            return this.getWidth(text, size, bold);
        }
        int rgb = argb & 0xFFFFFF;
        if (shadow) {
            int shadowRgb = (rgb & 0xFCFCFC) >> 2;
            float offset = Math.max(0.5F, size / 9.0F);
            this.render(text, x + offset, y + offset, shadowRgb, alpha, size, bold, true);
        }
        return this.render(text, x, y, rgb, alpha, size, bold, false);
    }

    public float getWidth(String text, float size, boolean bold) {
        if (text == null || text.isEmpty()) {
            return 0.0F;
        }
        if (Scripts.containsComplex(text)) {
            return this.complexWidth(text, size, bold);
        }
        return this.simpleWidth(text, size, bold);
    }

    /** X offset of the caret placed before logical index {@code index} of a plain string. */
    public float getCaretX(String text, int index, float size) {
        if (text == null || text.isEmpty() || index <= 0) {
            return 0.0F;
        }
        int clamped = Math.min(index, text.length());
        if (!Scripts.containsComplex(text)) {
            return this.simpleWidth(text.substring(0, clamped), size, false);
        }
        StyledText styled = StyledText.parse(text, 0xFFFFFF, false, false);
        ArabicShaper.Result shaped = ArabicShaper.shape(styled.chars, styled.length,
            c -> FontRegistry.canDisplay(FontRegistry.VAZIR, (char) c));
        int mapped = shaped.srcToOut[Math.min(clamped, styled.length)];
        TextLayout layout = StringTextureCache.layout(styled, size * WIDTH_PRECISION, false);
        int safe = Math.max(0, Math.min(mapped, layout.getCharacterCount()));
        Shape[] carets = layout.getCaretShapes(safe);
        return (float) carets[0].getBounds2D().getX() / WIDTH_PRECISION;
    }

    // ------------------------------------------------------------------ vanilla bridge

    /** Mirrors FontRenderer#renderString semantics (color promotion, shadow color). */
    public int renderVanilla(String text, float x, float y, int color, boolean dropShadow, float size) {
        if ((color & 0xFC000000) == 0) {
            color |= 0xFF000000;
        }
        if (dropShadow) {
            color = (color & 0xFCFCFC) >> 2 | (color & 0xFF000000);
        }
        float alpha = (color >>> 24) / 255.0F;
        float width = this.render(text, x, y, color & 0xFFFFFF, alpha, size, false, dropShadow);
        return (int) (x + width);
    }

    public int vanillaWidth(String text, float size) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return Math.round(this.getWidth(text, size, false));
    }

    public int vanillaCharWidth(char c, float size) {
        if (c == '\u00a7') {
            return -1;
        }
        return Math.round(FontRegistry.advanceEm(c, FontRegistry.familyFor(c), false) * size);
    }

    /** Called once per frame: frees line textures that have not been drawn recently. */
    public void collectGarbage() {
        long now = System.currentTimeMillis();
        if (now - this.lastGc > 1000L) {
            this.lastGc = now;
            this.lineCache.evictIdle();
        }
    }

    public void invalidate() {
        this.lineCache.clear();
        for (GlyphAtlas atlas : this.atlases.values()) {
            atlas.delete();
        }
        this.atlases.clear();
        synchronized (this.complexWidths) {
            this.complexWidths.clear();
        }
    }

    // ------------------------------------------------------------------ internals

    private float simpleWidth(String text, float size, boolean baseBold) {
        float width = 0.0F;
        boolean bold = baseBold;
        int n = text.length();
        for (int i = 0; i < n; i++) {
            char c = text.charAt(i);
            if (c == '\u00a7' && i + 1 < n) {
                char code = Character.toLowerCase(text.charAt(++i));
                if (code == 'l') {
                    bold = true;
                } else if (code == 'r' || "0123456789abcdef".indexOf(code) >= 0 || "klmno".indexOf(code) < 0) {
                    bold = baseBold;
                }
                continue;
            }
            int codePoint = text.codePointAt(i);
            int count = Character.charCount(codePoint);
            width += FontRegistry.advanceEm(codePoint, FontRegistry.familyForCodePoint(codePoint), bold) * size;
            i += count - 1;
        }
        return width;
    }

    private float complexWidth(String text, float size, boolean bold) {
        String key = size + (bold ? "b:" : "r:") + text;
        synchronized (this.complexWidths) {
            Float cached = this.complexWidths.get(key);
            if (cached != null) {
                return cached;
            }
        }
        StyledText styled = StyledText.parse(text, 0xFFFFFF, false, bold);
        float width = 0.0F;
        if (styled.length > 0) {
            width = StringTextureCache.layout(styled, size * WIDTH_PRECISION, false).getAdvance() / WIDTH_PRECISION;
        }
        synchronized (this.complexWidths) {
            this.complexWidths.put(key, width);
        }
        return width;
    }

    private float render(String text, float x, float y, int rgb, float alpha, float size, boolean bold, boolean shadow) {
        if (Scripts.containsComplex(text)) {
            return this.renderComplex(text, x, y, rgb, alpha, size, bold, shadow);
        }
        StyledText styled = StyledText.parse(text, rgb, shadow, bold);
        return this.renderGlyphs(styled, x, y, alpha, size);
    }

    private GlyphAtlas atlas(int family, boolean bold, int pxSize) {
        long key = ((long) (family * 2 + (bold ? 1 : 0)) << 32) | pxSize;
        GlyphAtlas atlas = this.atlases.get(key);
        if (atlas == null) {
            atlas = new GlyphAtlas(FontRegistry.derive(family, bold, false, pxSize));
            this.atlases.put(key, atlas);
        }
        return atlas;
    }

    private float renderGlyphs(StyledText text, float x, float y, float alpha, float size) {
        if (text.length == 0) {
            return 0.0F;
        }
        int scale = RenderContext.textScale();
        int pxSize = Math.max(4, Math.round(size * scale));
        float inv = size / pxSize;
        float baseline = y + size * BASELINE;
        float cursor = x;
        boolean blendWasOn = GL11.glIsEnabled(GL11.GL_BLEND);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.enableTexture2D();

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer wr = tessellator.getWorldRenderer();
        boolean drawing = false;
        int boundTexture = -1;
        int currentRgb = -1;
        this.decorations.clear();

        for (int i = 0; i < text.length; i++) {
            char c = text.chars[i];
            int codePoint = Character.codePointAt(text.chars, i, text.length);
            int codePointCount = Character.charCount(codePoint);
            int style = text.style[i];
            boolean bold = (style & StyledText.BOLD) != 0;
            int family = FontRegistry.familyForCodePoint(codePoint);
            float advance = FontRegistry.advanceEm(codePoint, family, bold) * size;
            int drawn = codePoint;
            int drawFamily = family;
            if ((style & StyledText.OBFUSCATED) != 0 && !Character.isWhitespace(c)) {
                drawn = OBFUSCATION_POOL.charAt(this.random.nextInt(OBFUSCATION_POOL.length()));
                drawFamily = FontRegistry.INTER;
            }
            GlyphAtlas.Glyph glyph = this.atlas(drawFamily, bold, pxSize).get(drawn);
            int rgb = text.rgb[i];
            if (glyph.texture >= 0) {
                if (glyph.texture != boundTexture || rgb != currentRgb) {
                    if (drawing) {
                        tessellator.draw();
                        drawing = false;
                    }
                    if (glyph.texture != boundTexture) {
                        GlStateManager.bindTexture(glyph.texture);
                        boundTexture = glyph.texture;
                    }
                    if (rgb != currentRgb) {
                        GlStateManager.color((rgb >> 16 & 0xFF) / 255.0F, (rgb >> 8 & 0xFF) / 255.0F, (rgb & 0xFF) / 255.0F, alpha);
                        currentRgb = rgb;
                    }
                }
                if (!drawing) {
                    wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                    drawing = true;
                }
                float left = cursor - glyph.originX * inv;
                float top = baseline - glyph.originY * inv;
                float w = glyph.width * inv;
                float h = glyph.height * inv;
                float skew = (style & StyledText.ITALIC) != 0 ? size * 0.09F : 0.0F;
                wr.pos(left - skew, top + h, 0.0D).tex(glyph.u0, glyph.v1).endVertex();
                wr.pos(left + w - skew, top + h, 0.0D).tex(glyph.u1, glyph.v1).endVertex();
                wr.pos(left + w + skew, top, 0.0D).tex(glyph.u1, glyph.v0).endVertex();
                wr.pos(left + skew, top, 0.0D).tex(glyph.u0, glyph.v0).endVertex();
            }
            float thickness = Math.max(size / 12.0F, 1.0F / scale);
            if ((style & StyledText.UNDERLINE) != 0) {
                this.decorations.add(new float[]{cursor, baseline + size * 0.12F, advance, thickness, rgb});
            }
            if ((style & StyledText.STRIKE) != 0) {
                this.decorations.add(new float[]{cursor, baseline - size * 0.30F, advance, thickness, rgb});
            }
            cursor += advance;
            i += codePointCount - 1;
        }
        if (drawing) {
            tessellator.draw();
        }
        if (!this.decorations.isEmpty()) {
            GlStateManager.disableTexture2D();
            for (float[] d : this.decorations) {
                int rgb = (int) d[4];
                GlStateManager.color((rgb >> 16 & 0xFF) / 255.0F, (rgb >> 8 & 0xFF) / 255.0F, (rgb & 0xFF) / 255.0F, alpha);
                wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
                wr.pos(d[0], d[1] + d[3], 0.0D).endVertex();
                wr.pos(d[0] + d[2], d[1] + d[3], 0.0D).endVertex();
                wr.pos(d[0] + d[2], d[1], 0.0D).endVertex();
                wr.pos(d[0], d[1], 0.0D).endVertex();
                tessellator.draw();
            }
            GlStateManager.enableTexture2D();
        }
        if (!blendWasOn) {
            GlStateManager.disableBlend();
        }
        return cursor - x;
    }

    private float renderComplex(String text, float x, float y, int rgb, float alpha, float size, boolean bold, boolean shadow) {
        int scale = RenderContext.textScale();
        int pxSize = Math.max(4, Math.round(size * scale));
        String key = rgb + ":" + pxSize + (shadow ? "s" : "n") + (bold ? "b:" : "r:") + text;
        StringTextureCache.LineTexture entry = this.lineCache.get(key, StyledText.parse(text, rgb, shadow, bold), pxSize);
        float inv = size / pxSize;
        float left = x - entry.padX * inv;
        float top = y + size * BASELINE - entry.baseline * inv;
        float w = entry.width * inv;
        float h = entry.height * inv;

        boolean blendWasOn = GL11.glIsEnabled(GL11.GL_BLEND);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.enableTexture2D();
        GlStateManager.bindTexture(entry.texture);
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer wr = tessellator.getWorldRenderer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        wr.pos(left, top + h, 0.0D).tex(0.0D, 1.0D).endVertex();
        wr.pos(left + w, top + h, 0.0D).tex(1.0D, 1.0D).endVertex();
        wr.pos(left + w, top, 0.0D).tex(1.0D, 0.0D).endVertex();
        wr.pos(left, top, 0.0D).tex(0.0D, 0.0D).endVertex();
        tessellator.draw();
        if (!blendWasOn) {
            GlStateManager.disableBlend();
        }
        return this.complexWidth(text, size, bold);
    }
}
