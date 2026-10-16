package dev.oryvex.client.ui.screen;

import dev.oryvex.client.OryvexClient;
import dev.oryvex.client.alt.SessionService;
import dev.oryvex.client.anim.Animation;
import dev.oryvex.client.anim.Easing;
import dev.oryvex.client.font.Fonts;
import dev.oryvex.client.render.ColorUtil;
import dev.oryvex.client.render.NebulaBackground;
import dev.oryvex.client.render.RoundedRenderer;
import dev.oryvex.client.render.SkinHeads;
import dev.oryvex.client.ui.clickgui.OryvexClickGui;
import dev.oryvex.client.ui.theme.Theme;
import dev.oryvex.client.ui.widget.OButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Session;

import java.io.IOException;

/**
 * Oryvex title screen: animated GLSL nebula, centred glass navigation and a session widget.
 */
public final class OryvexMainMenu extends OryvexScreen {

    private static final float PANEL_W = 220.0F;
    private static final float BUTTON_H = 24.0F;
    private static final float GAP = 6.0F;
    private static final float SESSION_W = 156.0F;
    private static final float SESSION_H = 36.0F;

    private final Animation intro = new Animation(Easing.EXPO_OUT, 1100.0F, 0.0F);
    private float panelX;
    private float panelY;
    private float panelH;
    private float titleY;
    private OButton switchButton;

    @Override
    protected void layout() {
        this.intro.animateTo(1.0F);
        this.panelH = 6 * BUTTON_H + 5 * GAP + 28.0F;
        float block = this.panelH + 64.0F;
        float top = Math.max(6.0F, (this.height - block) / 2.0F + 6.0F);
        this.titleY = top;
        this.panelX = (this.width - PANEL_W) / 2.0F;
        this.panelY = top + 64.0F;
        float bx = this.panelX + 14.0F;
        float bw = PANEL_W - 28.0F;
        float by = this.panelY + 14.0F;
        this.button("Singleplayer", OButton.Style.SECONDARY, () -> this.mc.displayGuiScreen(new GuiSelectWorld(this)))
            .bounds(bx, by, bw, BUTTON_H);
        this.button("Multiplayer", OButton.Style.PRIMARY, () -> this.mc.displayGuiScreen(new GuiMultiplayer(this)))
            .bounds(bx, by + (BUTTON_H + GAP), bw, BUTTON_H);
        this.button("Offline Alt Manager", OButton.Style.SECONDARY, () -> this.mc.displayGuiScreen(new OryvexAltManager(this)))
            .bounds(bx, by + (BUTTON_H + GAP) * 2, bw, BUTTON_H);
        this.button("Mod Dashboard", OButton.Style.SECONDARY, () -> this.mc.displayGuiScreen(new OryvexClickGui(this)))
            .bounds(bx, by + (BUTTON_H + GAP) * 3, bw, BUTTON_H);
        this.button("Options", OButton.Style.SECONDARY, () -> this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings)))
            .bounds(bx, by + (BUTTON_H + GAP) * 4, bw, BUTTON_H);
        this.button("Quit", OButton.Style.DANGER, () -> this.mc.shutdown())
            .bounds(bx, by + (BUTTON_H + GAP) * 5, bw, BUTTON_H);

        float sx = this.width - SESSION_W - 8.0F;
        this.switchButton = this.button("Switch", OButton.Style.GHOST, () -> this.mc.displayGuiScreen(new OryvexAltManager(this)));
        this.switchButton.textSize = 8.0F;
        this.switchButton.bounds(sx + SESSION_W - 46.0F, 8.0F + (SESSION_H - 18.0F) / 2.0F, 40.0F, 18.0F);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        float t = this.intro.get();
        float a = Math.min(1.0F, t * 1.4F);
        NebulaBackground.render(this.width, this.height);

        GlStateManager.pushMatrix();

        float titleSize = 34.0F;
        float titleWidth = Fonts.width("ORYVEX", titleSize, true);
        float tx = (this.width - titleWidth) / 2.0F;
        RoundedRenderer.glow(tx + titleWidth * 0.15F, this.titleY + 10.0F, titleWidth * 0.7F, 14.0F, 7.0F, 26.0F,
            ColorUtil.mulAlpha(Theme.ACCENT_START, 0.22F * a));
        Fonts.drawGradient("ORYVEX", tx, this.titleY, titleSize, true, ColorUtil.mulAlpha(Theme.ACCENT_START, a), ColorUtil.mulAlpha(Theme.GLOW, a));
        Fonts.drawCenterX("PVP & UTILITY CLIENT  \u00b7  1.8.9", this.width / 2.0F, this.titleY + 36.0F,
            ColorUtil.mulAlpha(Theme.TEXT_SECONDARY, a), 8.0F, true);

        this.glass(this.panelX, this.panelY, PANEL_W, this.panelH, Theme.RADIUS_PANEL, a);
        this.renderButtons(mouseX, mouseY, a);
        GlStateManager.popMatrix();

        this.renderSessionWidget(mouseX, mouseY, a);

        Fonts.draw("OryvexClient " + OryvexClient.VERSION, 6.0F, this.height - 12.0F, ColorUtil.mulAlpha(Theme.TEXT_MUTED, a), 8.0F);
        String right = "Minecraft 1.8.9  \u00b7  Forge";
        Fonts.draw(right, this.width - Fonts.width(right, 8.0F) - 6.0F, this.height - 12.0F, ColorUtil.mulAlpha(Theme.TEXT_MUTED, a), 8.0F);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void renderSessionWidget(int mouseX, int mouseY, float alpha) {
        Session session = this.mc.getSession();
        float x = this.width - SESSION_W - 8.0F;
        float y = 8.0F;
        this.glass(x, y, SESSION_W, SESSION_H, Theme.RADIUS_CARD, alpha);
        RoundedRenderer.rect(x + 6.0F, y + 6.0F, 24.0F, 24.0F, 4.0F, ColorUtil.mulAlpha(Theme.BORDER, alpha));
        SkinHeads.drawHead(session.getUsername(), x + 8.0F, y + 8.0F, 20.0F, alpha);
        String name = Fonts.trim(session.getUsername(), SESSION_W - 92.0F, 9.5F, true);
        Fonts.draw(name, x + 36.0F, y + 7.0F, ColorUtil.mulAlpha(Theme.TEXT_PRIMARY, alpha), 9.5F, true);
        boolean offline = SessionService.isOffline(session);
        RoundedRenderer.circle(x + 39.0F, y + 25.0F, 2.0F, ColorUtil.mulAlpha(offline ? Theme.FAVORITE : Theme.SUCCESS, alpha));
        Fonts.draw(offline ? "Offline session" : "Premium session", x + 44.0F, y + 21.0F,
            ColorUtil.mulAlpha(Theme.TEXT_SECONDARY, alpha), 7.5F);
        this.switchButton.render(mouseX, mouseY, alpha);
    }

    @Override
    protected void renderButtons(int mouseX, int mouseY, float alpha) {
        for (OButton button : this.buttons) {
            if (button != this.switchButton) {
                button.render(mouseX, mouseY, alpha);
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        // The title screen cannot be closed with ESC.
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
