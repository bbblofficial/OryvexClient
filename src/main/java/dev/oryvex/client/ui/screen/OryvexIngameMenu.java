package dev.oryvex.client.ui.screen;

import dev.oryvex.client.font.Fonts;
import dev.oryvex.client.render.ColorUtil;
import dev.oryvex.client.render.RoundedRenderer;
import dev.oryvex.client.ui.clickgui.OryvexClickGui;
import dev.oryvex.client.ui.theme.Theme;
import dev.oryvex.client.ui.widget.OButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;

/**
 * Replacement pause menu: blurred live world behind a black-blue dashboard.
 */
public final class OryvexIngameMenu extends OryvexScreen {

    private static final float CARD_W = 244.0F;
    private static final float BUTTON_H = 24.0F;
    private static final float GAP = 6.0F;
    private static final float HEADER = 40.0F;

    private float cardX;
    private float cardY;
    private float cardH;

    @Override
    protected void layout() {
        this.cardH = HEADER + 6 * BUTTON_H + 5 * GAP + 16.0F;
        this.cardX = (this.width - CARD_W) / 2.0F;
        this.cardY = (this.height - this.cardH) / 2.0F;
        float bx = this.cardX + 14.0F;
        float bw = CARD_W - 28.0F;
        float by = this.cardY + HEADER;
        this.button("Resume Game", OButton.Style.SECONDARY, () -> this.mc.displayGuiScreen(null)).bounds(bx, by, bw, BUTTON_H);
        this.button("Oryvex Mods & HUD Editor", OButton.Style.PRIMARY, () -> this.mc.displayGuiScreen(new OryvexClickGui(this)))
            .bounds(bx, by + (BUTTON_H + GAP), bw, BUTTON_H);
        this.button("Offline Alt Switcher", OButton.Style.SECONDARY, () -> this.mc.displayGuiScreen(new OryvexAltManager(this)))
            .bounds(bx, by + (BUTTON_H + GAP) * 2, bw, BUTTON_H);
        this.button("Server List", OButton.Style.SECONDARY, () -> this.disconnect(new GuiMultiplayer(new OryvexMainMenu())))
            .bounds(bx, by + (BUTTON_H + GAP) * 3, bw, BUTTON_H);
        this.button("Options", OButton.Style.SECONDARY, () -> this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings)))
            .bounds(bx, by + (BUTTON_H + GAP) * 4, bw, BUTTON_H);
        this.button("Disconnect", OButton.Style.DANGER, () -> this.disconnect(null))
            .bounds(bx, by + (BUTTON_H + GAP) * 5, bw, BUTTON_H);
    }

    private void disconnect(GuiScreen next) {
        boolean integrated = this.mc.isIntegratedServerRunning();
        if (this.mc.theWorld != null) {
            this.mc.theWorld.sendQuittingDisconnectingPacket();
        }
        this.mc.loadWorld((WorldClient) null);
        if (next != null) {
            this.mc.displayGuiScreen(next);
        } else if (integrated) {
            this.mc.displayGuiScreen(new OryvexMainMenu());
        } else {
            this.mc.displayGuiScreen(new GuiMultiplayer(new OryvexMainMenu()));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        float t = this.openProgress();
        this.worldBackdrop(t);

        float scale = 0.94F + 0.06F * t;
        GlStateManager.pushMatrix();
        GlStateManager.translate(this.width / 2.0F, this.height / 2.0F, 0.0F);
        GlStateManager.scale(scale, scale, 1.0F);
        GlStateManager.translate(-this.width / 2.0F, -this.height / 2.0F, 0.0F);

        this.glass(this.cardX, this.cardY, CARD_W, this.cardH, Theme.RADIUS_PANEL, t);
        RoundedRenderer.gradient(this.cardX + 14.0F, this.cardY + 30.0F, CARD_W - 28.0F, 1.0F, 0.5F,
            ColorUtil.mulAlpha(Theme.ACCENT_START, t), ColorUtil.mulAlpha(0x0000B4FF, t), false);
        Fonts.draw("Game Paused", this.cardX + 14.0F, this.cardY + 11.0F, ColorUtil.mulAlpha(Theme.TEXT_PRIMARY, t), 12.0F, true);
        this.renderButtons(mouseX, mouseY, t);
        GlStateManager.popMatrix();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
