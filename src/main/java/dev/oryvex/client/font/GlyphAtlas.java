package dev.oryvex.client.font;

import dev.oryvex.client.render.GlTextures;
import org.lwjgl.BufferUtils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Dynamic glyph atlas for one font face at one raster size. Glyphs are rasterised in white
 * on demand and tinted with the vertex color, so formatting colors cost nothing.
 */
final class GlyphAtlas {

    static final int PAD = 2;
    private static final int PAGE_SIZE = 1024;

    static final class Glyph {
        final int texture;
        final float u0;
        final float v0;
        final float u1;
        final float v1;
        final int width;
        final int height;
        /** Distance (px) from the pen position to the left edge of the bitmap. */
        final float originX;
        /** Distance (px) from the top of the bitmap to the baseline. */
        final float originY;

        Glyph(int texture, float u0, float v0, float u1, float v1, int width, int height, float originX, float originY) {
            this.texture = texture;
            this.u0 = u0;
            this.v0 = v0;
            this.u1 = u1;
            this.v1 = v1;
            this.width = width;
            this.height = height;
            this.originX = originX;
            this.originY = originY;
        }
    }

    private static final Glyph EMPTY = new Glyph(-1, 0, 0, 0, 0, 0, 0, 0, 0);

    private final Font font;
    private final float ascent;
    private final int cellHeight;
    private final Glyph[] glyphs = new Glyph[65536];
    private final List<Integer> pages = new ArrayList<>();
    private int page = -1;
    private int penX;
    private int penY;
    private int rowHeight;

    GlyphAtlas(Font font) {
        this.font = font;
        LineMetrics metrics = font.getLineMetrics("Hgy\u0647\u06AF", FontRegistry.FRC);
        this.ascent = metrics.getAscent();
        this.cellHeight = (int) Math.ceil(metrics.getAscent() + metrics.getDescent()) + PAD * 2;
    }

    Glyph get(char c) {
        Glyph glyph = this.glyphs[c];
        if (glyph == null) {
            glyph = this.rasterise(c);
            this.glyphs[c] = glyph;
        }
        return glyph;
    }

    private Glyph rasterise(char c) {
        GlyphVector gv = this.font.createGlyphVector(FontRegistry.FRC, new char[]{c});
        Rectangle2D bounds = gv.getVisualBounds();
        if (Character.isWhitespace(c) || bounds.isEmpty()) {
            return EMPTY;
        }
        float advance = gv.getGlyphMetrics(0).getAdvance();
        int left = (int) Math.floor(Math.min(0.0D, bounds.getX()));
        int right = (int) Math.ceil(Math.max(advance, bounds.getMaxX()));
        int width = right - left + PAD * 2;
        int height = this.cellHeight;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setColor(Color.WHITE);
        float originX = PAD - left;
        float originY = PAD + this.ascent;
        g.drawGlyphVector(gv, originX, originY);
        g.dispose();

        if (this.page < 0 || this.penX + width > PAGE_SIZE) {
            this.penX = 0;
            this.penY += this.rowHeight;
            this.rowHeight = 0;
        }
        if (this.page < 0 || this.penY + height > PAGE_SIZE) {
            this.page = GlTextures.create(PAGE_SIZE, PAGE_SIZE, BufferUtils.createByteBuffer(PAGE_SIZE * PAGE_SIZE * 4));
            this.pages.add(this.page);
            this.penX = 0;
            this.penY = 0;
            this.rowHeight = 0;
        }
        GlTextures.upload(this.page, this.penX, this.penY, image);
        Glyph glyph = new Glyph(this.page,
            this.penX / (float) PAGE_SIZE, this.penY / (float) PAGE_SIZE,
            (this.penX + width) / (float) PAGE_SIZE, (this.penY + height) / (float) PAGE_SIZE,
            width, height, originX, originY);
        this.penX += width + 1;
        this.rowHeight = Math.max(this.rowHeight, height + 1);
        return glyph;
    }

    void delete() {
        for (int texture : this.pages) {
            GlTextures.delete(texture);
        }
        this.pages.clear();
        this.page = -1;
    }
}
