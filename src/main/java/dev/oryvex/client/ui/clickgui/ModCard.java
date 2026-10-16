package dev.oryvex.client.ui.clickgui;

import dev.oryvex.client.anim.Animation;
import dev.oryvex.client.anim.Easing;
import dev.oryvex.client.font.Fonts;
import dev.oryvex.client.module.Module;
import dev.oryvex.client.render.ColorUtil;
import dev.oryvex.client.render.RenderUtil;
import dev.oryvex.client.render.RoundedRenderer;
import dev.oryvex.client.ui.theme.Theme;
import dev.oryvex.client.ui.widget.PillToggle;

/**
 * Grid card for one module: names (EN + FA), description, settings link and pill toggle.
 */
public final class ModCard {

    public static final float HEIGHT = 74.0F;

    private final Module module;
    private final PillToggle toggle = new PillToggle();
    private final Animation hover = new Animation(Easing.EXPO_OUT, 220.0F, 0.0F);
    private final Animation enabledGlow = new Animation(Easing.EXPO_OUT, 300.0F, 0.0F);

    public ModCard(Module module) {
        this.module = module;
        this.enabledGlow.set(module.isEnabled() ? 1.0F : 0.0F);
    }

    public Module getModule() {
        return this.module;
    }

    public void render(float x, float y, float w, int mouseX, int mouseY, float alpha, boolean selected) {
        boolean hovered = RenderUtil.hovered(mouseX, mouseY, x, y, w, HEIGHT);
        float t = this.hover.animateTo(hovered ? 1.0F : 0.0F).get();
        float on = this.enabledGlow.animateTo(this.module.isEnabled() ? 1.0F : 0.0F).get();

        if (on > 0.02F) {
            RoundedRenderer.glow(x, y, w, HEIGHT, Theme.RADIUS_CARD, 6.0F, ColorUtil.mulAlpha(Theme.ACCENT_START, 0.16F * on * alpha));
        }
        int border = selected ? Theme.GLOW : ColorUtil.lerp(Theme.BORDER, Theme.ACCENT_END, Math.max(t * 0.7F, on * 0.35F));
        RoundedRenderer.bordered(x, y, w, HEIGHT, Theme.RADIUS_CARD,
            ColorUtil.mulAlpha(ColorUtil.lerp(Theme.SLATE, Theme.SLATE_RAISED, t), alpha), 1.0F, ColorUtil.mulAlpha(border, alpha));
        if (on > 0.02F) {
            RoundedRenderer.gradient(x + 10.0F, y, w - 20.0F, 1.5F, 0.75F,
                ColorUtil.mulAlpha(Theme.ACCENT_START, on * alpha), ColorUtil.mulAlpha(Theme.GLOW, on * alpha), false);
        }

        float iconSize = 20.0F;
        if (on > 0.02F) {
            RoundedRenderer.gradient(x + 10.0F, y + 10.0F, iconSize, iconSize, 5.0F,
                ColorUtil.mulAlpha(Theme.ACCENT_START, alpha), ColorUtil.mulAlpha(ColorUtil.lerp(Theme.BORDER, Theme.ACCENT_END, on), alpha), false);
        } else {
            RoundedRenderer.rect(x + 10.0F, y + 10.0F, iconSize, iconSize, 5.0F, ColorUtil.mulAlpha(Theme.BORDER, alpha));
        }
        Fonts.drawCenter(this.module.getName().substring(0, 1), x + 10.0F + iconSize / 2.0F, y + 10.0F + iconSize / 2.0F,
            ColorUtil.mulAlpha(Theme.TEXT_PRIMARY, alpha), 10.0F, true);

        float textX = x + 36.0F;
        float textW = w - 46.0F;
        Fonts.draw(Fonts.trim(this.module.getName(), textW, 9.5F, true), textX, y + 9.0F, ColorUtil.mulAlpha(Theme.TEXT_PRIMARY, alpha), 9.5F, true);
        Fonts.draw(Fonts.trim(this.module.getPersianName(), textW, 8.0F, false), textX, y + 20.5F,
            ColorUtil.mulAlpha(Theme.TEXT_SECONDARY, alpha), 8.0F, false);
        java.util.List<String> lines = Fonts.wrap(this.module.getDescription(), w - 20.0F, 7.5F);
        for (int i = 0; i < Math.min(2, lines.size()); i++) {
            String line = lines.get(i);
            if (i == 1 && lines.size() > 2) {
                line = Fonts.trim(line + " ...", w - 20.0F, 7.5F, false);
            }
            Fonts.draw(line, x + 10.0F, y + 35.0F + i * 9.0F, ColorUtil.mulAlpha(Theme.TEXT_MUTED, alpha), 7.5F, false);
        }

        boolean settingsHover = this.isOverSettings(x, y, w, mouseX, mouseY);
        Fonts.drawMiddle("Settings \u203a", x + 10.0F, y + HEIGHT - 11.0F,
            ColorUtil.mulAlpha(settingsHover || selected ? Theme.ACCENT_END : Theme.TEXT_SECONDARY, alpha), 8.0F, true);
        this.toggle.render(x + w - PillToggle.WIDTH - 10.0F, y + HEIGHT - 11.0F - PillToggle.HEIGHT / 2.0F, this.module.isEnabled(), alpha);
    }

    public boolean isOverSettings(float x, float y, float w, int mouseX, int mouseY) {
        return RenderUtil.hovered(mouseX, mouseY, x + 6.0F, y + HEIGHT - 20.0F, 56.0F, 18.0F);
    }
}
