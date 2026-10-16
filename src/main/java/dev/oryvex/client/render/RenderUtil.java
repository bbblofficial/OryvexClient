package dev.oryvex.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * Float-precision 2D primitives in GUI coordinates.
 */
public final class RenderUtil {

    private RenderUtil() {
    }

    public static boolean hovered(float mouseX, float mouseY, float x, float y, float w, float h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    public static void color(int argb) {
        GlStateManager.color(ColorUtil.red(argb) / 255.0F, ColorUtil.green(argb) / 255.0F,
            ColorUtil.blue(argb) / 255.0F, ColorUtil.alpha(argb) / 255.0F);
    }

    public static void rect(float x, float y, float w, float h, int color) {
        if (w <= 0.0F || h <= 0.0F || ColorUtil.alpha(color) == 0) {
            return;
        }
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer wr = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        color(color);
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        wr.pos(x, y + h, 0.0D).endVertex();
        wr.pos(x + w, y + h, 0.0D).endVertex();
        wr.pos(x + w, y, 0.0D).endVertex();
        wr.pos(x, y, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void gradient(float x, float y, float w, float h, int from, int to, boolean vertical) {
        if (w <= 0.0F || h <= 0.0F) {
            return;
        }
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer wr = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        int bl = vertical ? to : from;
        int br = to;
        int tr = vertical ? from : to;
        int tl = from;
        vertex(wr, x, y + h, bl);
        vertex(wr, x + w, y + h, br);
        vertex(wr, x + w, y, tr);
        vertex(wr, x, y, tl);
        tessellator.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    private static void vertex(WorldRenderer wr, float x, float y, int c) {
        wr.pos(x, y, 0.0D).color(ColorUtil.red(c), ColorUtil.green(c), ColorUtil.blue(c), ColorUtil.alpha(c)).endVertex();
    }

    public static void outline(float x, float y, float w, float h, float t, int color) {
        rect(x, y, w, t, color);
        rect(x, y + h - t, w, t, color);
        rect(x, y + t, t, h - t * 2.0F, color);
        rect(x + w - t, y + t, t, h - t * 2.0F, color);
    }

    /** Draws a texture region at float coordinates. */
    public static void texture(ResourceLocation location, float x, float y, float w, float h,
                               float u, float v, float uw, float vh, float texW, float texH, int tint) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(location);
        texturedQuad(x, y, w, h, u / texW, v / texH, (u + uw) / texW, (v + vh) / texH, tint);
    }

    public static void texturedQuad(float x, float y, float w, float h, float u0, float v0, float u1, float v1, int tint) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer wr = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        color(tint);
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        wr.pos(x, y + h, 0.0D).tex(u0, v1).endVertex();
        wr.pos(x + w, y + h, 0.0D).tex(u1, v1).endVertex();
        wr.pos(x + w, y, 0.0D).tex(u1, v0).endVertex();
        wr.pos(x, y, 0.0D).tex(u0, v0).endVertex();
        tessellator.draw();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /** Draws a raw GL texture id. */
    public static void glTexture(int textureId, float x, float y, float w, float h, float u0, float v0, float u1, float v1, int tint) {
        GlStateManager.bindTexture(textureId);
        texturedQuad(x, y, w, h, u0, v0, u1, v1, tint);
    }

    /** Integer rect matching vanilla Gui.drawRect semantics. */
    public static void vanillaRect(int left, int top, int right, int bottom, int color) {
        Gui.drawRect(left, top, right, bottom, color);
    }
}
