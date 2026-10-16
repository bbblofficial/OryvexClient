package dev.oryvex.client.modules.visuals;

import dev.oryvex.client.module.Category;
import dev.oryvex.client.font.Fonts;
import dev.oryvex.client.hud.HudElement;
import dev.oryvex.client.hud.HudStyle;
import dev.oryvex.client.ui.theme.Theme;
import net.minecraft.client.renderer.GlStateManager;
import dev.oryvex.client.module.Module;
import dev.oryvex.client.module.ModuleManager;
import dev.oryvex.client.setting.BooleanSetting;
import dev.oryvex.client.setting.NumberSetting;

public final class TabMod extends Module {
    public final BooleanSetting background = this.add(new BooleanSetting("Background", true));
    public final NumberSetting opacity = this.add(new NumberSetting("Background Opacity", 0.82D, 0.15D, 1.0D, 0.05D));
    public final NumberSetting scale = this.add(new NumberSetting("Scale", 1.0D, 0.75D, 1.5D, 0.05D));
    public final BooleanSetting ping = this.add(new BooleanSetting("Show Ping", true));
    public final BooleanSetting health = this.add(new BooleanSetting("Show Health", false));
    private final HudElement hudElement;
    public TabMod() {
        super("Tab", "", "Customize the player list background, scale, ping and health display.", Category.VISUALS, true);
        this.hudElement = this.addHud(new HudElement(this, "tab", "Tab Scoreboard", 0.50F, 0.05F) {
            @Override public float getWidth(boolean preview) { return 320.0F; }
            @Override public float getHeight(boolean preview) { return 80.0F; }
            @Override protected void render(float partialTicks, boolean preview) {
                if (!preview) return;
                HudStyle.panel(0.0F, 0.0F, getWidth(true), getHeight(true), true);
                Fonts.drawCenterX("Tab Scoreboard", 160.0F, 8.0F, Theme.TEXT_PRIMARY, 9.0F, true);
                for (int i = 0; i < 4; i++) Fonts.draw("Player " + (i + 1), 12.0F + (i % 2) * 154.0F, 28.0F + (i / 2) * 18.0F, Theme.TEXT_SECONDARY, 8.0F, false);
            }
        });
    }
    public void beginRender(int width, int height) {
        float baseX = width * 0.5F - 160.0F;
        float baseY = 5.0F;
        float x = this.hudElement.getX(width, false);
        float y = this.hudElement.getY(height, false);
        float s = this.hudElement.getScale();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x - baseX * s, y - baseY * s, 0.0F);
        GlStateManager.scale(s, s, 1.0F);
    }
    public void endRender() { GlStateManager.popMatrix(); }
    public static TabMod active() { TabMod m = ModuleManager.module(TabMod.class); return m != null && m.isEnabled() ? m : null; }
}
