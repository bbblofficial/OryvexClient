package dev.oryvex.client.ui.screen;

import dev.oryvex.client.OryvexClient;
import dev.oryvex.client.alt.AltAccount;
import dev.oryvex.client.alt.AltStore;
import dev.oryvex.client.alt.NameGenerator;
import dev.oryvex.client.alt.SessionService;
import dev.oryvex.client.anim.SmoothValue;
import dev.oryvex.client.font.Fonts;
import dev.oryvex.client.render.ColorUtil;
import dev.oryvex.client.render.NebulaBackground;
import dev.oryvex.client.render.RenderUtil;
import dev.oryvex.client.render.RoundedRenderer;
import dev.oryvex.client.render.Scissor;
import dev.oryvex.client.render.SkinHeads;
import dev.oryvex.client.ui.theme.Theme;
import dev.oryvex.client.ui.widget.OButton;
import dev.oryvex.client.ui.widget.TextBox;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.List;

/**
 * Offline account manager: instant login, random PvP names, persistent favourites list.
 */
public final class OryvexAltManager extends OryvexScreen {

    private static final float ROW_H = 32.0F;
    private static final float ROW_GAP = 4.0F;

    private final GuiScreen parent;
    private final TextBox nameBox = new TextBox("Username (3-16 chars)", 16);
    private final SmoothValue scroll = new SmoothValue(16.0F);
    private float px;
    private float py;
    private float pw;
    private float ph;
    private float listW;
    private String status = "";
    private int statusColor = Theme.TEXT_SECONDARY;
    private long statusTime;

    public OryvexAltManager(GuiScreen parent) {
        this.parent = parent;
    }

    private static AltStore store() {
        return OryvexClient.alts();
    }

    @Override
    protected void layout() {
        this.pw = Math.min(this.width - 24.0F, 470.0F);
        this.ph = Math.min(this.height - 24.0F, 290.0F);
        this.px = (this.width - this.pw) / 2.0F;
        this.py = (this.height - this.ph) / 2.0F;
        this.listW = this.pw * 0.56F;
        float fx = this.px + this.listW + 14.0F;
        float fw = this.pw - this.listW - 26.0F;
        float fy = this.py + 48.0F;
        this.nameBox.bounds(fx, fy, fw, 22.0F);
        this.button("Login", OButton.Style.PRIMARY, this::loginFromField).bounds(fx, fy + 30.0F, fw, 22.0F);
        this.button("Add to List", OButton.Style.SECONDARY, this::addFromField).bounds(fx, fy + 58.0F, fw, 22.0F);
        this.button("Random Name", OButton.Style.SECONDARY, () -> this.nameBox.setText(NameGenerator.generate()))
            .bounds(fx, fy + 86.0F, fw, 22.0F);
        this.button("Back", OButton.Style.GHOST, () -> this.mc.displayGuiScreen(this.parent))
            .bounds(fx, this.py + this.ph - 34.0F, fw, 22.0F);
    }

    private void setStatus(String message, int color) {
        this.status = message;
        this.statusColor = color;
        this.statusTime = System.currentTimeMillis();
    }

    private void loginFromField() {
        String name = this.nameBox.getText().trim();
        if (!SessionService.isValidName(name)) {
            this.setStatus("Invalid name: use 3-16 letters, digits or _", Theme.DANGER);
            return;
        }
        AltAccount account = store().add(name);
        this.login(account);
    }

    private void addFromField() {
        String name = this.nameBox.getText().trim();
        if (!SessionService.isValidName(name)) {
            this.setStatus("Invalid name: use 3-16 letters, digits or _", Theme.DANGER);
            return;
        }
        boolean existed = store().find(name) != null;
        store().add(name);
        this.setStatus(existed ? name + " is already saved" : "Saved " + name, existed ? Theme.TEXT_SECONDARY : Theme.SUCCESS);
    }

    private void login(AltAccount account) {
        try {
            SessionService.login(account.getUsername());
            store().markUsed(account);
            this.setStatus("Logged in as " + account.getUsername() + " (offline)", Theme.SUCCESS);
        } catch (RuntimeException e) {
            this.setStatus("Login failed: " + e.getMessage(), Theme.DANGER);
        }
    }

    @Override
    public void updateScreen() {
        this.nameBox.update();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        float a = this.openProgress();
        if (this.mc.theWorld == null) {
            NebulaBackground.render(this.width, this.height);
        } else {
            this.worldBackdrop(a);
        }
        this.glass(this.px, this.py, this.pw, this.ph, Theme.RADIUS_PANEL, a);

        Fonts.draw("Offline Alt Manager", this.px + 14.0F, this.py + 12.0F, ColorUtil.mulAlpha(Theme.TEXT_PRIMARY, a), 12.0F, true);
        String current = "Current: " + this.mc.getSession().getUsername();
        Fonts.draw(current, this.px + 14.0F, this.py + 28.0F, ColorUtil.mulAlpha(Theme.TEXT_SECONDARY, a), 8.0F);

        this.renderList(mouseX, mouseY, a);

        float fx = this.px + this.listW + 14.0F;
        float fw = this.pw - this.listW - 26.0F;
        RoundedRenderer.rect(this.px + this.listW + 4.0F, this.py + 44.0F, 1.0F, this.ph - 56.0F, 0.0F, ColorUtil.mulAlpha(Theme.BORDER, a));
        Fonts.draw("USERNAME", fx, this.py + 38.0F, ColorUtil.mulAlpha(Theme.TEXT_MUTED, a), 7.0F, true);
        this.nameBox.render(mouseX, mouseY, a);
        this.renderButtons(mouseX, mouseY, a);

        float infoY = this.py + 48.0F + 118.0F;
        if (!this.status.isEmpty() && System.currentTimeMillis() - this.statusTime < 6000L) {
            for (String line : Fonts.wrap(this.status, fw, 8.0F)) {
                Fonts.draw(line, fx, infoY, ColorUtil.mulAlpha(this.statusColor, a), 8.0F);
                infoY += 10.0F;
            }
            infoY += 4.0F;
        }
        for (String line : Fonts.wrap("Offline sessions only join servers running online-mode=false (LAN, offline or test servers).", fw, 7.5F)) {
            if (infoY > this.py + this.ph - 44.0F) {
                break;
            }
            Fonts.draw(line, fx, infoY, ColorUtil.mulAlpha(Theme.TEXT_MUTED, a), 7.5F);
            infoY += 9.5F;
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private float listTop() {
        return this.py + 44.0F;
    }

    private float listHeight() {
        return this.ph - 56.0F;
    }

    private void renderList(int mouseX, int mouseY, float a) {
        List<AltAccount> accounts = store().sorted();
        float x = this.px + 12.0F;
        float w = this.listW - 16.0F;
        float top = this.listTop();
        float height = this.listHeight();
        float content = accounts.size() * (ROW_H + ROW_GAP);
        this.scroll.clamp(0.0F, Math.max(0.0F, content - height));
        float offset = this.scroll.get();

        if (accounts.isEmpty()) {
            Fonts.drawCenter("No saved accounts yet", x + w / 2.0F, top + height / 2.0F - 6.0F, ColorUtil.mulAlpha(Theme.TEXT_SECONDARY, a), 9.0F, false);
            return;
        }
        String active = this.mc.getSession().getUsername();
        Scissor.push(x - 2.0F, top, w + 4.0F, height);
        for (int i = 0; i < accounts.size(); i++) {
            AltAccount account = accounts.get(i);
            float y = top + i * (ROW_H + ROW_GAP) - offset;
            if (y + ROW_H < top || y > top + height) {
                continue;
            }
            boolean hovered = RenderUtil.hovered(mouseX, mouseY, x, y, w, ROW_H) && mouseY >= top && mouseY < top + height;
            boolean current = account.getUsername().equalsIgnoreCase(active);
            int border = current ? Theme.ACCENT_END : hovered ? 0xFF2A3548 : Theme.BORDER;
            RoundedRenderer.bordered(x, y, w, ROW_H, Theme.RADIUS_CARD, ColorUtil.mulAlpha(hovered ? Theme.SLATE_RAISED : Theme.OBSIDIAN, a), 1.0F,
                ColorUtil.mulAlpha(border, a));
            if (current) {
                RoundedRenderer.gradient(x, y + 6.0F, 2.0F, ROW_H - 12.0F, 1.0F, Theme.ACCENT_START, Theme.GLOW, true);
            }
            SkinHeads.drawHead(account.getUsername(), x + 7.0F, y + 6.0F, 20.0F, a);
            Fonts.draw(account.getUsername(), x + 33.0F, y + 7.0F, ColorUtil.mulAlpha(Theme.TEXT_PRIMARY, a), 9.0F, true);
            Fonts.draw(current ? "Active session" : lastUsed(account), x + 33.0F, y + 18.5F,
                ColorUtil.mulAlpha(current ? Theme.ACCENT_END : Theme.TEXT_MUTED, a), 7.5F);

            float starX = x + w - 34.0F;
            float delX = x + w - 17.0F;
            boolean starHover = RenderUtil.hovered(mouseX, mouseY, starX, y + 8.0F, 14.0F, 16.0F);
            boolean delHover = RenderUtil.hovered(mouseX, mouseY, delX, y + 8.0F, 14.0F, 16.0F);
            int starColor = account.isFavorite() ? Theme.FAVORITE : starHover ? Theme.TEXT_PRIMARY : Theme.TEXT_MUTED;
            Fonts.drawCenter(account.isFavorite() ? "\u2605" : "\u2606", starX + 7.0F, y + ROW_H / 2.0F, ColorUtil.mulAlpha(starColor, a), 10.0F, false);
            Fonts.drawCenter("\u00d7", delX + 7.0F, y + ROW_H / 2.0F, ColorUtil.mulAlpha(delHover ? Theme.DANGER : Theme.TEXT_MUTED, a), 11.0F, true);
        }
        Scissor.pop();
    }

    private static String lastUsed(AltAccount account) {
        if (account.getLastUsed() <= 0L) {
            return "Never used  \u00b7  offline";
        }
        long seconds = (System.currentTimeMillis() - account.getLastUsed()) / 1000L;
        String ago;
        if (seconds < 60L) {
            ago = "just now";
        } else if (seconds < 3600L) {
            ago = seconds / 60L + "m ago";
        } else if (seconds < 86400L) {
            ago = seconds / 3600L + "h ago";
        } else {
            ago = seconds / 86400L + "d ago";
        }
        return "Used " + ago + "  \u00b7  offline";
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (this.nameBox.mouseClicked(mouseX, mouseY, mouseButton)) {
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
        float x = this.px + 12.0F;
        float w = this.listW - 16.0F;
        float top = this.listTop();
        float height = this.listHeight();
        if (mouseButton != 0 || mouseY < top || mouseY >= top + height || mouseX < x || mouseX >= x + w) {
            return;
        }
        List<AltAccount> accounts = store().sorted();
        float offset = this.scroll.get();
        for (int i = 0; i < accounts.size(); i++) {
            AltAccount account = accounts.get(i);
            float y = top + i * (ROW_H + ROW_GAP) - offset;
            if (!RenderUtil.hovered(mouseX, mouseY, x, y, w, ROW_H)) {
                continue;
            }
            OButton.playClick();
            if (RenderUtil.hovered(mouseX, mouseY, x + w - 34.0F, y + 8.0F, 14.0F, 16.0F)) {
                store().toggleFavorite(account);
            } else if (RenderUtil.hovered(mouseX, mouseY, x + w - 17.0F, y + 8.0F, 14.0F, 16.0F)) {
                store().remove(account);
                this.setStatus("Removed " + account.getUsername(), Theme.TEXT_SECONDARY);
            } else {
                this.login(account);
            }
            return;
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            this.scroll.addTarget(wheel > 0 ? -(ROW_H + ROW_GAP) : ROW_H + ROW_GAP);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.mc.displayGuiScreen(this.parent);
            return;
        }
        if (keyCode == Keyboard.KEY_RETURN && this.nameBox.isFocused()) {
            this.loginFromField();
            return;
        }
        this.nameBox.keyTyped(typedChar, keyCode);
    }
}
