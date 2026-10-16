package dev.oryvex.client.render;

import dev.oryvex.client.core.RenderContext;
import dev.oryvex.client.render.shader.ShaderProgram;
import dev.oryvex.client.render.shader.Shaders;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

/**
 * Signed-distance-field rounded rectangles: fills, gradients, borders and glows.
 */
public final class RoundedRenderer {

    private RoundedRenderer() {
    }

    public static void rect(float x, float y, float w, float h, float radius, int color) {
        draw(x, y, w, h, radius, color, color, false, 0.0F, 0, 0.0F);
    }

    public static void gradient(float x, float y, float w, float h, float radius, int from, int to, boolean vertical) {
        draw(x, y, w, h, radius, from, to, vertical, 0.0F, 0, 0.0F);
    }

    public static void bordered(float x, float y, float w, float h, float radius, int fill, float border, int borderColor) {
        draw(x, y, w, h, radius, fill, fill, false, border, borderColor, 0.0F);
    }

    public static void gradientBordered(float x, float y, float w, float h, float radius, int from, int to,
                                        float border, int borderColor) {
        draw(x, y, w, h, radius, from, to, false, border, borderColor, 0.0F);
    }

    /** Soft bloom around a rectangle; draw it before the rectangle itself. */
    public static void glow(float x, float y, float w, float h, float radius, float size, int color) {
        if (size <= 0.0F || ColorUtil.alpha(color) == 0) {
            return;
        }
        draw(x, y, w, h, radius, color, color, false, 0.0F, 0, size);
    }

    public static void circle(float cx, float cy, float r, int color) {
        draw(cx - r, cy - r, r * 2.0F, r * 2.0F, r, color, color, false, 0.0F, 0, 0.0F);
    }

    private static void draw(float x, float y, float w, float h, float radius, int c1, int c2, boolean vertical,
                             float border, int borderColor, float glow) {
        if (w <= 0.0F || h <= 0.0F) {
            return;
        }
        ShaderProgram shader = Shaders.rect();
        if (shader == null) {
            fallback(x, y, w, h, c1, c2, vertical, border, borderColor, glow);
            return;
        }
        float r = Math.max(0.0F, Math.min(radius, Math.min(w, h) * 0.5F));
        float pad = glow > 0.0F ? glow : 1.0F;
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableAlpha();
        shader.use();
        shader.set2f("u_size", w, h)
            .set1f("u_pad", pad)
            .set1f("u_radius", r)
            .set1f("u_softness", RenderContext.pixel() * 1.25F)
            .setColor("u_color1", c1)
            .setColor("u_color2", c2)
            .set1f("u_vertical", vertical ? 1.0F : 0.0F)
            .set1f("u_border", border)
            .setColor("u_borderColor", borderColor)
            .set1f("u_glow", glow);
        quad(x - pad, y - pad, w + pad * 2.0F, h + pad * 2.0F);
        shader.stop();
        GlStateManager.enableAlpha();
    }

    static void quad(float x, float y, float w, float h) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer wr = tessellator.getWorldRenderer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        wr.pos(x, y + h, 0.0D).tex(0.0D, 1.0D).endVertex();
        wr.pos(x + w, y + h, 0.0D).tex(1.0D, 1.0D).endVertex();
        wr.pos(x + w, y, 0.0D).tex(1.0D, 0.0D).endVertex();
        wr.pos(x, y, 0.0D).tex(0.0D, 0.0D).endVertex();
        tessellator.draw();
    }

    private static void fallback(float x, float y, float w, float h, int c1, int c2, boolean vertical,
                                 float border, int borderColor, float glow) {
        if (glow > 0.0F) {
            return;
        }
        if (c1 == c2) {
            RenderUtil.rect(x, y, w, h, c1);
        } else {
            RenderUtil.gradient(x, y, w, h, c1, c2, vertical);
        }
        if (border > 0.0F) {
            RenderUtil.outline(x, y, w, h, border, borderColor);
        }
    }
}
