package dev.oryvex.client.render;

import dev.oryvex.client.render.shader.ShaderProgram;
import dev.oryvex.client.render.shader.Shaders;
import dev.oryvex.client.ui.theme.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;

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
        ShaderProgram shader = Shaders.nebula();
        if (shader == null) {
            RenderUtil.gradient(0.0F, 0.0F, width, height, Theme.OBSIDIAN_SOLID, Theme.SLATE_SOLID, true);
            RenderUtil.gradient(0.0F, height * 0.55F, width, height * 0.45F, 0x00007BFF, 0x30007BFF, true);
            return;
        }
        float targetX = mc.displayWidth > 0 ? Mouse.getX() / (float) mc.displayWidth : 0.5F;
        float targetY = mc.displayHeight > 0 ? Mouse.getY() / (float) mc.displayHeight : 0.5F;
        mouseX += (targetX - mouseX) * 0.04F;
        mouseY += (targetY - mouseY) * 0.04F;

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
