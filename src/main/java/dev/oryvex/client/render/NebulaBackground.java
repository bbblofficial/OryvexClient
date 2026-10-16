package dev.oryvex.client.render;

import dev.oryvex.client.render.shader.ShaderProgram;
import dev.oryvex.client.render.shader.Shaders;
import dev.oryvex.client.ui.theme.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

/**
 * Animated procedural nebula + perspective grid used behind every menu.
 */
public final class NebulaBackground {

    private static final long START = System.currentTimeMillis();
    private static float mouseX = 0.5F;
    private static float mouseY = 0.5F;

    private NebulaBackground() {
    }

    public static void render(float width, float height) {
        Minecraft mc = Minecraft.getMinecraft();
        // Paint before any shader work so the first frame can never expose the default white buffer.
        RenderUtil.rect(0.0F, 0.0F, width, height, Theme.OBSIDIAN_SOLID);
        ShaderProgram shader = Shaders.nebula();
        if (shader == null) {
            return;
        }
        // Keep the background fixed: menu motion is limited to fade and scale transitions.
        mouseX = 0.5F;
        mouseY = 0.5F;

        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.depthMask(false);
        shader.use();
        shader.set1f("u_time", (System.currentTimeMillis() - START) / 1000.0F)
            .set2f("u_resolution", mc.displayWidth, mc.displayHeight)
            .set2f("u_mouse", mouseX, mouseY);
        RoundedRenderer.quad(0.0F, 0.0F, width, height);
        shader.stop();
        GlStateManager.depthMask(true);
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
    }
}
