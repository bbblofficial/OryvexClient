package dev.oryvex.client.hud;

import dev.oryvex.client.module.ModuleManager;
import net.minecraft.client.renderer.GlStateManager;

/**
 * Draws every visible HUD element.
 */
public final class HudRenderer {

    private HudRenderer() {
    }

    public static void renderAll(float screenWidth, float screenHeight, float partialTicks, boolean preview) {
        ModuleManager manager = ModuleManager.getInstance();
        if (manager == null) {
            return;
        }
        GlStateManager.pushMatrix();
        for (HudElement element : manager.getHudElements()) {
            if (element.isVisible()) {
                element.draw(screenWidth, screenHeight, partialTicks, preview);
            }
        }
        GlStateManager.popMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
    }
}
