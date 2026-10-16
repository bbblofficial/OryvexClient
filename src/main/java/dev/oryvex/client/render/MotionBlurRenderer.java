package dev.oryvex.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;

/**
 * Accumulation-buffer motion blur. Called right after the world is rendered (before the
 * HUD) so the HUD itself stays sharp: blends the previous accumulated frame over the
 * current one, then stores the result as the new history.
 */
public final class MotionBlurRenderer {

    private static Framebuffer history;
    private static boolean hasHistory;

    private MotionBlurRenderer() {
    }

    public static void apply(float amount, ScaledResolution sr) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!OpenGlHelper.isFramebufferEnabled() || amount <= 0.0F) {
            reset();
            return;
        }
        Framebuffer main = mc.getFramebuffer();
        if (history == null || history.framebufferWidth != main.framebufferWidth || history.framebufferHeight != main.framebufferHeight) {
            if (history != null) {
                history.deleteFramebuffer();
            }
            history = new Framebuffer(main.framebufferWidth, main.framebufferHeight, false);
            history.setFramebufferFilter(GL11.GL_NEAREST);
            hasHistory = false;
        }
        float w = sr.getScaledWidth();
        float h = sr.getScaledHeight();

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);

        if (hasHistory) {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            drawFramebuffer(history, w, h, Math.min(0.95F, amount));
        }

        history.bindFramebuffer(true);
        GL11.glDisable(GL11.GL_BLEND);
        drawFramebuffer(main, w, h, 1.0F);
        main.bindFramebuffer(true);

        GL11.glDepthMask(true);
        GL11.glPopAttrib();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        hasHistory = true;
    }

    private static void drawFramebuffer(Framebuffer fb, float w, float h, float alpha) {
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GlStateManager.bindTexture(fb.framebufferTexture);
        float u = fb.framebufferWidth / (float) fb.framebufferTextureWidth;
        float v = fb.framebufferHeight / (float) fb.framebufferTextureHeight;
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
        net.minecraft.client.renderer.Tessellator tessellator = net.minecraft.client.renderer.Tessellator.getInstance();
        net.minecraft.client.renderer.WorldRenderer wr = tessellator.getWorldRenderer();
        wr.begin(GL11.GL_QUADS, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_TEX);
        wr.pos(0.0D, h, 0.0D).tex(0.0D, 0.0D).endVertex();
        wr.pos(w, h, 0.0D).tex(u, 0.0D).endVertex();
        wr.pos(w, 0.0D, 0.0D).tex(u, v).endVertex();
        wr.pos(0.0D, 0.0D, 0.0D).tex(0.0D, v).endVertex();
        tessellator.draw();
    }

    public static void reset() {
        hasHistory = false;
    }
}
