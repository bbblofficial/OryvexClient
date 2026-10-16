package dev.oryvex.client.render.blur;

import dev.oryvex.client.anim.AnimationClock;
import dev.oryvex.client.core.RenderContext;
import dev.oryvex.client.render.RoundedRenderer;
import dev.oryvex.client.render.shader.ShaderProgram;
import dev.oryvex.client.render.shader.Shaders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;

/**
 * Dual-Kawase blur of the live framebuffer (downsample chain followed by an upsample
 * chain). The result is computed at most once per frame and composited into rounded
 * "glass" regions by {@link #drawBlurred}.
 */
public final class KawaseBlur {

    private static final int MAX_ITERATIONS = 5;

    private static Framebuffer[] chain = new Framebuffer[0];
    private static int chainWidth = -1;
    private static int chainHeight = -1;
    private static long preparedFrame = -1L;
    private static boolean ready;

    private KawaseBlur() {
    }

    public static boolean available() {
        return OpenGlHelper.isFramebufferEnabled() && Shaders.available();
    }

    /**
     * Blurs the current contents of the main framebuffer.
     *
     * @param iterations 1..5 (each one halves the resolution: stronger blur)
     * @param offset     sample spread, ~1.0 - 4.0
     */
    public static void prepare(int iterations, float offset) {
        if (!available()) {
            ready = false;
            return;
        }
        long frame = AnimationClock.frame();
        if (frame == preparedFrame && ready) {
            return;
        }
        preparedFrame = frame;
        Minecraft mc = Minecraft.getMinecraft();
        Framebuffer main = mc.getFramebuffer();
        int passes = Math.max(1, Math.min(MAX_ITERATIONS, iterations));
        ensureChain(main.framebufferWidth, main.framebufferHeight, passes);

        ShaderProgram down = Shaders.blurDown();
        ShaderProgram up = Shaders.blurUp();
        if (down == null || up == null) {
            ready = false;
            return;
        }

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_VIEWPORT_BIT);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        down.use();
        down.set1i("u_texture", 0).set1f("u_offset", offset);
        for (int i = 0; i < passes; i++) {
            Framebuffer target = chain[i];
            int source = i == 0 ? main.framebufferTexture : chain[i - 1].framebufferTexture;
            target.bindFramebuffer(true);
            GlStateManager.bindTexture(source);
            down.set2f("u_halfpixel", 0.5F / target.framebufferWidth, 0.5F / target.framebufferHeight);
            ndcQuad();
        }
        down.stop();

        up.use();
        up.set1i("u_texture", 0).set1f("u_offset", offset);
        for (int i = passes - 1; i > 0; i--) {
            Framebuffer target = chain[i - 1];
            target.bindFramebuffer(true);
            GlStateManager.bindTexture(chain[i].framebufferTexture);
            up.set2f("u_halfpixel", 0.5F / target.framebufferWidth, 0.5F / target.framebufferHeight);
            ndcQuad();
        }
        up.stop();

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        main.bindFramebuffer(true);
        GL11.glPopAttrib();
        ready = true;
    }

    /**
     * Composites the prepared blur inside a rounded rectangle (GUI coordinates) and tints it.
     */
    public static void drawBlurred(float x, float y, float w, float h, float radius, int tint, float alpha) {
        ShaderProgram mask = Shaders.blurMask();
        if (!ready || mask == null || chain.length == 0 || preparedFrame != AnimationClock.frame()) {
            RoundedRenderer.rect(x, y, w, h, radius, tint);
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        float r = Math.max(0.0F, Math.min(radius, Math.min(w, h) * 0.5F));
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.bindTexture(chain[0].framebufferTexture);
        mask.use();
        mask.set1i("u_texture", 0)
            .set2f("u_resolution", mc.displayWidth, mc.displayHeight)
            .set2f("u_size", w, h)
            .set1f("u_radius", r)
            .set1f("u_softness", RenderContext.pixel() * 1.25F)
            .setColor("u_tint", tint)
            .set1f("u_alpha", Math.max(0.0F, Math.min(1.0F, alpha)));
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer wr = tessellator.getWorldRenderer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        wr.pos(x, y + h, 0.0D).tex(0.0D, 1.0D).endVertex();
        wr.pos(x + w, y + h, 0.0D).tex(1.0D, 1.0D).endVertex();
        wr.pos(x + w, y, 0.0D).tex(1.0D, 0.0D).endVertex();
        wr.pos(x, y, 0.0D).tex(0.0D, 0.0D).endVertex();
        tessellator.draw();
        mask.stop();
        GlStateManager.enableAlpha();
    }

    /** Blurred, tinted, 1px-bordered glass frame. */
    public static void glass(float x, float y, float w, float h, float radius, int tint, int border, float alpha, int strength) {
        if (available()) {
            prepare(strength, 1.0F + strength * 0.45F);
            drawBlurred(x, y, w, h, radius, tint, alpha);
            RoundedRenderer.bordered(x, y, w, h, radius, 0x00000000, 1.0F, dev.oryvex.client.render.ColorUtil.mulAlpha(border, alpha));
        } else {
            RoundedRenderer.bordered(x, y, w, h, radius, dev.oryvex.client.render.ColorUtil.mulAlpha(tint | 0xFF000000, alpha * 0.92F), 1.0F,
                dev.oryvex.client.render.ColorUtil.mulAlpha(border, alpha));
        }
    }

    private static void ensureChain(int width, int height, int passes) {
        if (width == chainWidth && height == chainHeight && chain.length >= passes) {
            return;
        }
        for (Framebuffer fb : chain) {
            if (fb != null) {
                fb.deleteFramebuffer();
            }
        }
        chain = new Framebuffer[MAX_ITERATIONS];
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            int w = Math.max(1, width >> (i + 1));
            int h = Math.max(1, height >> (i + 1));
            Framebuffer fb = new Framebuffer(w, h, false);
            fb.setFramebufferFilter(GL11.GL_LINEAR);
            chain[i] = fb;
        }
        chainWidth = width;
        chainHeight = height;
    }

    private static void ndcQuad() {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer wr = tessellator.getWorldRenderer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        wr.pos(-1.0D, -1.0D, 0.0D).tex(0.0D, 0.0D).endVertex();
        wr.pos(1.0D, -1.0D, 0.0D).tex(1.0D, 0.0D).endVertex();
        wr.pos(1.0D, 1.0D, 0.0D).tex(1.0D, 1.0D).endVertex();
        wr.pos(-1.0D, 1.0D, 0.0D).tex(0.0D, 1.0D).endVertex();
        tessellator.draw();
    }
}
