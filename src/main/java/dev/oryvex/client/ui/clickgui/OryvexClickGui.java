package dev.oryvex.client.ui.clickgui;

import dev.oryvex.client.OryvexClient;
import dev.oryvex.client.anim.Animation;
import dev.oryvex.client.anim.Easing;
import dev.oryvex.client.anim.SmoothValue;
import dev.oryvex.client.config.ConfigManager;
import dev.oryvex.client.font.Fonts;
import dev.oryvex.client.module.Category;
import dev.oryvex.client.module.Module;
import dev.oryvex.client.module.ModuleManager;
import dev.oryvex.client.render.ColorUtil;
import dev.oryvex.client.render.NebulaBackground;
import dev.oryvex.client.render.RenderUtil;
import dev.oryvex.client.render.RoundedRenderer;
import dev.oryvex.client.render.Scissor;
import dev.oryvex.client.setting.BooleanSetting;
import dev.oryvex.client.setting.ColorSetting;
import dev.oryvex.client.setting.KeybindSetting;
import dev.oryvex.client.setting.ModeSetting;
import dev.oryvex.client.setting.NumberSetting;
import dev.oryvex.client.setting.Setting;
import dev.oryvex.client.ui.screen.HudEditorScreen;
import dev.oryvex.client.ui.screen.OryvexScreen;
import dev.oryvex.client.ui.theme.Theme;
import dev.oryvex.client.ui.widget.OButton;
import dev.oryvex.client.ui.widget.TextBox;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Badlion-style mod dashboard: search (EN/FA), category tabs, profile selector, card grid
 * with pill toggles and a sliding settings drawer.
 */
public final class OryvexClickGui extends OryvexScreen {

    private static final String[] TABS = {"All", "HUD", "PvP", "Visuals", "Misc"};
    private static final Category[] TAB_FILTER = {null, Category.HUD, Category.PVP, Category.VISUALS, Category.MISC};
    private static final float SIDEBAR = 120.0F;
    private static final float GAP = 8.0F;
    private static final float DRAWER_W = 196.0F;
    private static int selectedTab;

    private final GuiScreen parent;
    private final TextBox search = new TextBox("Search mods...", 32);
    private final Map<Module, ModCard> cards = new IdentityHashMap<>();
    private final SmoothValue gridScroll = new SmoothValue(16.0F);
    private final SmoothValue drawerScroll = new SmoothValue(16.0F);
    private final Animation tabIndicator = new Animation(Easing.EXPO_OUT, 360.0F, 0.0F);
    private final Animation drawerAnim = new Animation(Easing.EXPO_OUT, 380.0F, 0.0F);
    private final List<SettingComponent<?>> components = new ArrayList<>();
    private Module drawerModule;
    private SettingComponent<?> pressed;

    private float px;
    private float py;
    private float pw;
    private float ph;
    private float tabTop;
    private float tabStep;

    public OryvexClickGui(GuiScreen parent) {
        this.parent = parent;
    }

    private static ModuleManager modules() {
        return ModuleManager.getInstance();
    }

    @Override
    protected void layout() {
        this.pw = Math.min(this.width - 24.0F, 580.0F);
        this.ph = Math.min(this.height - 24.0F, 350.0F);
        this.px = (this.width - this.pw) / 2.0F;
        this.py = (this.height - this.ph) / 2.0F;
        this.tabTop = this.py + 48.0F;
        this.tabStep = Math.max(16.0F, Math.min(24.0F, (this.ph - 48.0F - 74.0F) / TABS.length));
        this.tabIndicator.set(this.tabTop + selectedTab * this.tabStep);

        float contentX = this.px + SIDEBAR + 10.0F;
        float contentW = this.pw - SIDEBAR - 20.0F;
        this.search.bounds(contentX, this.py + 12.0F, Math.min(220.0F, contentW * 0.62F), 22.0F);

        float sideX = this.px + 10.0F;
        float sideW = SIDEBAR - 20.0F;
        this.button("Edit HUD Layout", OButton.Style.PRIMARY, () -> this.mc.displayGuiScreen(new HudEditorScreen(this)))
            .bounds(sideX, this.py + this.ph - 60.0F, sideW, 20.0F).textSize = 8.5F;
        OButton prev = this.button("<", OButton.Style.SECONDARY, () -> this.config().cycleProfile(-1));
        prev.bounds(sideX, this.py + this.ph - 32.0F, 18.0F, 20.0F);
        OButton next = this.button(">", OButton.Style.SECONDARY, () -> this.config().cycleProfile(1));
        next.bounds(sideX + sideW - 40.0F, this.py + this.ph - 32.0F, 18.0F, 20.0F);
        OButton add = this.button("+", OButton.Style.SECONDARY, () -> this.config().createProfile());
        add.bounds(sideX + sideW - 18.0F, this.py + this.ph - 32.0F, 18.0F, 20.0F);
        this.rebuildDrawer();
    }

    private ConfigManager config() {
        return OryvexClient.config();
    }

    private List<Module> filtered() {
        List<Module> list = new ArrayList<>();
        if (modules() == null) {
            return list;
        }
        Category category = TAB_FILTER[selectedTab];
        String query = this.search.getText();
        for (Module module : modules().getModules()) {
            if ((category == null || module.getCategory() == category) && module.matches(query)) {
                list.add(module);
            }
        }
        return list;
    }

    private ModCard card(Module module) {
        return this.cards.computeIfAbsent(module, ModCard::new);
    }

    private void openDrawer(Module module) {
        if (this.drawerModule == module) {
            this.closeDrawer();
            return;
        }
        this.drawerModule = module;
        this.drawerScroll.snap(0.0F);
        this.rebuildDrawer();
        this.drawerAnim.animateTo(1.0F);
    }

    private void closeDrawer() {
        this.drawerAnim.animateTo(0.0F);
    }

    private void rebuildDrawer() {
        this.components.clear();
        if (this.drawerModule == null) {
            return;
        }
        for (Setting<?> setting : this.drawerModule.getSettings()) {
            if (setting instanceof BooleanSetting) {
                this.components.add(new BooleanComponent((BooleanSetting) setting));
            } else if (setting instanceof NumberSetting) {
                this.components.add(new NumberComponent((NumberSetting) setting));
            } else if (setting instanceof ModeSetting) {
                this.components.add(new ModeComponent((ModeSetting) setting));
            } else if (setting instanceof ColorSetting) {
                this.components.add(new ColorComponent((ColorSetting) setting));
            } else if (setting instanceof KeybindSetting) {
                this.components.add(new KeybindComponent((KeybindSetting) setting));
            }
        }
    }

    @Override
    public void updateScreen() {
        this.search.update();
    }

    // ------------------------------------------------------------------ rendering

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        float a = this.openProgress();
        if (this.mc.theWorld == null) {
            NebulaBackground.render(this.width, this.height);
        } else {
            this.worldBackdrop(a);
        }

        float scale = 0.95F + 0.05F * a;
        GlStateManager.pushMatrix();
        GlStateManager.translate(this.width / 2.0F, this.height / 2.0F, 0.0F);
        GlStateManager.scale(scale, scale, 1.0F);
        GlStateManager.translate(-this.width / 2.0F, -this.height / 2.0F, 0.0F);

        this.glass(this.px, this.py, this.pw, this.ph, Theme.RADIUS_PANEL, a);
        RoundedRenderer.rect(this.px + SIDEBAR, this.py + 10.0F, 1.0F, this.ph - 20.0F, 0.0F, ColorUtil.mulAlpha(Theme.BORDER, a));
        this.renderSidebar(mouseX, mouseY, a);
        this.renderTopBar(mouseX, mouseY, a);
        this.renderGrid(mouseX, mouseY, a);
        this.renderDrawer(mouseX, mouseY, a);

        GlStateManager.popMatrix();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void renderSidebar(int mouseX, int mouseY, float a) {
        float x = this.px + 14.0F;
        Fonts.drawGradient("ORYVEX", x, this.py + 12.0F, 17.0F, true, ColorUtil.mulAlpha(Theme.ACCENT_START, a), ColorUtil.mulAlpha(Theme.GLOW, a));
        Fonts.draw("MOD DASHBOARD", x, this.py + 31.0F, ColorUtil.mulAlpha(Theme.TEXT_MUTED, a), 7.0F, true);

        float tabX = this.px + 8.0F;
        float tabW = SIDEBAR - 16.0F;
        float indicatorY = this.tabIndicator.animateTo(this.tabTop + selectedTab * this.tabStep).get();
        RoundedRenderer.glow(tabX, indicatorY + 1.0F, tabW, this.tabStep - 2.0F, 5.0F, 5.0F, ColorUtil.mulAlpha(Theme.ACCENT_START, 0.25F * a));
        RoundedRenderer.gradient(tabX, indicatorY + 1.0F, tabW, this.tabStep - 2.0F, 5.0F,
            ColorUtil.mulAlpha(0xD9007BFF, a), ColorUtil.mulAlpha(0x5500B4FF, a), false);
        for (int i = 0; i < TABS.length; i++) {
            float ty = this.tabTop + i * this.tabStep;
            boolean hovered = RenderUtil.hovered(mouseX, mouseY, tabX, ty, tabW, this.tabStep);
            int color = i == selectedTab ? Theme.TEXT_PRIMARY : hovered ? 0xFFCBD5E1 : Theme.TEXT_SECONDARY;
            Fonts.drawMiddle(TABS[i], tabX + 10.0F, ty + this.tabStep / 2.0F, ColorUtil.mulAlpha(color, a), 9.0F, i == selectedTab);
            int count = this.countFor(i);
            Fonts.drawRightMiddle(String.valueOf(count), tabX + tabW - 8.0F, ty + this.tabStep / 2.0F,
                ColorUtil.mulAlpha(i == selectedTab ? 0xCCFFFFFF : Theme.TEXT_MUTED, a), 7.5F, false);
        }

        float sideX = this.px + 10.0F;
        float sideW = SIDEBAR - 20.0F;
        Fonts.draw("PROFILE", sideX, this.py + this.ph - 42.0F, ColorUtil.mulAlpha(Theme.TEXT_MUTED, a), 6.5F, true);
        String profile = this.config() != null ? this.config().getActiveProfile() : ConfigManager.DEFAULT_PROFILE;
        float nameX = sideX + 21.0F;
        float nameW = sideW - 63.0F;
        RoundedRenderer.bordered(nameX, this.py + this.ph - 32.0F, nameW, 20.0F, 4.0F, ColorUtil.mulAlpha(Theme.OBSIDIAN, a), 1.0F,
            ColorUtil.mulAlpha(Theme.BORDER, a));
        Fonts.drawCenter(Fonts.trim(profile, nameW - 6.0F, 7.5F, true), nameX + nameW / 2.0F, this.py + this.ph - 22.0F,
            ColorUtil.mulAlpha(Theme.TEXT_PRIMARY, a), 7.5F, true);
        this.renderButtons(mouseX, mouseY, a);
    }

    private int countFor(int tab) {
        if (modules() == null) {
            return 0;
        }
        int count = 0;
        for (Module module : modules().getModules()) {
            if (TAB_FILTER[tab] == null || module.getCategory() == TAB_FILTER[tab]) {
                count++;
            }
        }
        return count;
    }

    private void renderTopBar(int mouseX, int mouseY, float a) {
        this.search.render(mouseX, mouseY, a);
        List<Module> list = this.filtered();
        int enabled = 0;
        for (Module module : list) {
            if (module.isEnabled()) {
                enabled++;
            }
        }
        String summary = enabled + " / " + list.size() + " enabled";
        Fonts.drawRightMiddle(summary, this.px + this.pw - 14.0F, this.py + 23.0F, ColorUtil.mulAlpha(Theme.TEXT_SECONDARY, a), 8.0F, false);
    }

    private float gridX() {
        return this.px + SIDEBAR + 10.0F;
    }

    private float gridY() {
        return this.py + 44.0F;
    }

    private float gridW() {
        return this.pw - SIDEBAR - 20.0F;
    }

    private float gridH() {
        return this.ph - 54.0F;
    }

    private int columns() {
        return this.gridW() >= 400.0F ? 3 : this.gridW() >= 250.0F ? 2 : 1;
    }

    private float cardW() {
        int cols = this.columns();
        return (this.gridW() - GAP * (cols - 1)) / cols;
    }

    private void renderGrid(int mouseX, int mouseY, float a) {
        List<Module> list = this.filtered();
        int cols = this.columns();
        float cw = this.cardW();
        int rows = (list.size() + cols - 1) / cols;
        float content = rows * (ModCard.HEIGHT + GAP) - GAP;
        this.gridScroll.clamp(0.0F, Math.max(0.0F, content - this.gridH()));
        float offset = this.gridScroll.get();
        boolean drawerOpen = this.drawerAnim.get() > 0.5F;
        float drawerLeft = this.px + this.pw - DRAWER_W;

        Scissor.push(this.gridX() - 6.0F, this.gridY() - 2.0F, this.gridW() + 12.0F, this.gridH() + 4.0F);
        if (list.isEmpty()) {
            Fonts.drawCenter("No mods match your search", this.gridX() + this.gridW() / 2.0F, this.gridY() + 40.0F,
                ColorUtil.mulAlpha(Theme.TEXT_SECONDARY, a), 9.0F, false);
        }
        for (int i = 0; i < list.size(); i++) {
            int col = i % cols;
            int row = i / cols;
            float x = this.gridX() + col * (cw + GAP);
            float y = this.gridY() + row * (ModCard.HEIGHT + GAP) - offset;
            if (y + ModCard.HEIGHT < this.gridY() - 4.0F || y > this.gridY() + this.gridH() + 4.0F) {
                continue;
            }
            boolean inGrid = mouseY >= this.gridY() && mouseY < this.gridY() + this.gridH() && (!drawerOpen || mouseX < drawerLeft);
            Module module = list.get(i);
            this.card(module).render(x, y, cw, inGrid ? mouseX : -1, inGrid ? mouseY : -1, a, module == this.drawerModule && drawerOpen);
        }
        Scissor.pop();

        if (content > this.gridH()) {
            float trackX = this.px + this.pw - 6.0F;
            float barH = Math.max(18.0F, this.gridH() * this.gridH() / content);
            float barY = this.gridY() + (this.gridH() - barH) * (offset / Math.max(1.0F, content - this.gridH()));
            RoundedRenderer.rect(trackX, barY, 2.0F, barH, 1.0F, ColorUtil.mulAlpha(Theme.ACCENT_END, 0.6F * a));
        }
    }

    private float drawerX() {
        return this.px + this.pw - DRAWER_W;
    }

    private void renderDrawer(int mouseX, int mouseY, float a) {
        float d = this.drawerAnim.get();
        if (d < 0.01F || this.drawerModule == null) {
            if (d < 0.01F && this.drawerAnim.getTarget() == 0.0F) {
                this.drawerModule = null;
                this.components.clear();
            }
            return;
        }
        float x = this.drawerX();
        float y = this.py + 1.0F;
        float h = this.ph - 2.0F;
        Scissor.push(this.px, this.py, this.pw, this.ph);
        RoundedRenderer.glow(x, y, DRAWER_W, h, Theme.RADIUS_PANEL, 14.0F, ColorUtil.mulAlpha(0xFF000000, 0.5F * d * a));
        RoundedRenderer.bordered(x, y, DRAWER_W, h, Theme.RADIUS_PANEL, ColorUtil.mulAlpha(0xF70B0E14, a * d), 1.0F, ColorUtil.mulAlpha(Theme.BORDER, a * d));
        float inner = x + 12.0F;
        float innerW = DRAWER_W - 24.0F;
        Fonts.draw(Fonts.trim(this.drawerModule.getName(), innerW - 20.0F, 11.0F, true), inner, y + 11.0F, ColorUtil.mulAlpha(Theme.TEXT_PRIMARY, a * d), 11.0F, true);
        Fonts.draw(this.drawerModule.getDescription(), inner, y + 25.0F, ColorUtil.mulAlpha(Theme.TEXT_SECONDARY, a * d), 8.0F, false);
        boolean closeHover = RenderUtil.hovered(mouseX, mouseY, x + DRAWER_W - 24.0F, y + 8.0F, 16.0F, 16.0F);
        Fonts.drawCenter("\u00d7", x + DRAWER_W - 16.0F, y + 16.0F, ColorUtil.mulAlpha(closeHover ? Theme.DANGER : Theme.TEXT_SECONDARY, a * d), 12.0F, true);
        RoundedRenderer.gradient(inner, y + 38.0F, innerW, 1.0F, 0.5F, ColorUtil.mulAlpha(Theme.ACCENT_START, a * d), ColorUtil.mulAlpha(0x0000B4FF, a), false);

        float top = y + 44.0F;
        float height = h - 50.0F;
        float content = 0.0F;
        for (SettingComponent<?> component : this.components) {
            if (component.getSetting().isVisible()) {
                content += component.getHeight() + 4.0F;
            }
        }
        this.drawerScroll.clamp(0.0F, Math.max(0.0F, content - height));
        float cy = top - this.drawerScroll.get();
        Scissor.push(x + 1.0F, top, DRAWER_W - 2.0F, height);
        for (SettingComponent<?> component : this.components) {
            if (!component.getSetting().isVisible()) {
                continue;
            }
            if (this.pressed == component) {
                component.mouseDragged(inner, cy, innerW, mouseX, mouseY);
            }
            if (cy + component.getHeight() > top - 2.0F && cy < top + height + 2.0F) {
                component.render(inner, cy, innerW, mouseX, mouseY, a * d);
            }
            cy += component.getHeight() + 4.0F;
        }
        Scissor.pop();
        Scissor.pop();
    }

    // ------------------------------------------------------------------ input

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (this.drawerAnim.get() > 0.5F && this.drawerModule != null && mouseX >= this.drawerX() && mouseX < this.px + this.pw
            && mouseY >= this.py && mouseY < this.py + this.ph) {
            this.clickDrawer(mouseX, mouseY, mouseButton);
            return;
        }
        if (this.search.mouseClicked(mouseX, mouseY, mouseButton)) {
            return;
        }
        for (OButton button : new ArrayList<>(this.buttons)) {
            if (button.mouseClicked(mouseX, mouseY, mouseButton)) {
                return;
            }
        }
        float tabX = this.px + 8.0F;
        for (int i = 0; i < TABS.length; i++) {
            if (RenderUtil.hovered(mouseX, mouseY, tabX, this.tabTop + i * this.tabStep, SIDEBAR - 16.0F, this.tabStep)) {
                if (selectedTab != i) {
                    selectedTab = i;
                    this.gridScroll.snap(0.0F);
                    OButton.playClick();
                }
                return;
            }
        }
        if (mouseY < this.gridY() || mouseY >= this.gridY() + this.gridH()) {
            return;
        }
        List<Module> list = this.filtered();
        int cols = this.columns();
        float cw = this.cardW();
        float offset = this.gridScroll.get();
        for (int i = 0; i < list.size(); i++) {
            float x = this.gridX() + (i % cols) * (cw + GAP);
            float y = this.gridY() + (i / cols) * (ModCard.HEIGHT + GAP) - offset;
            if (!RenderUtil.hovered(mouseX, mouseY, x, y, cw, ModCard.HEIGHT)) {
                continue;
            }
            Module module = list.get(i);
            OButton.playClick();
            if (mouseButton == 1 || this.card(module).isOverSettings(x, y, cw, mouseX, mouseY)) {
                this.openDrawer(module);
            } else if (mouseButton == 0) {
                module.toggle();
            }
            return;
        }
    }

    private void clickDrawer(int mouseX, int mouseY, int button) {
        float x = this.drawerX();
        float y = this.py + 1.0F;
        if (RenderUtil.hovered(mouseX, mouseY, x + DRAWER_W - 24.0F, y + 8.0F, 16.0F, 16.0F)) {
            OButton.playClick();
            this.closeDrawer();
            return;
        }
        float inner = x + 12.0F;
        float innerW = DRAWER_W - 24.0F;
        float top = y + 44.0F;
        float height = this.ph - 2.0F - 50.0F;
        if (mouseY < top || mouseY >= top + height) {
            return;
        }
        float cy = top - this.drawerScroll.get();
        for (SettingComponent<?> component : this.components) {
            if (!component.getSetting().isVisible()) {
                continue;
            }
            if (component.mouseClicked(inner, cy, innerW, mouseX, mouseY, button)) {
                this.pressed = component;
            }
            cy += component.getHeight() + 4.0F;
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (this.pressed != null) {
            this.pressed.mouseReleased();
            this.pressed = null;
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel == 0) {
            return;
        }
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        float step = wheel > 0 ? -40.0F : 40.0F;
        if (this.drawerAnim.get() > 0.5F && this.drawerModule != null && mouseX >= this.drawerX()) {
            this.drawerScroll.addTarget(step);
        } else {
            this.gridScroll.addTarget(step);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        for (SettingComponent<?> component : this.components) {
            if (component.isCapturingKeys() && component.keyTyped(typedChar, keyCode)) {
                return;
            }
        }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            if (this.search.isFocused()) {
                this.search.setFocused(false);
                return;
            }
            if (this.drawerModule != null && this.drawerAnim.getTarget() > 0.0F) {
                this.closeDrawer();
                return;
            }
            this.close();
            return;
        }
        if (OryvexClient.dashboardKey() != null && keyCode == OryvexClient.dashboardKey().getKeyCode() && !this.search.isFocused()) {
            this.close();
            return;
        }
        if (this.search.keyTyped(typedChar, keyCode)) {
            this.gridScroll.snap(0.0F);
            return;
        }
        if (!this.search.isFocused() && typedChar >= ' ' && typedChar != '\u007f') {
            this.search.setFocused(true);
            this.search.keyTyped(typedChar, keyCode);
            this.gridScroll.snap(0.0F);
        }
    }

    private void close() {
        this.mc.displayGuiScreen(this.parent);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        if (this.config() != null) {
            this.config().save();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
