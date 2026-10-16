package dev.oryvex.client.ui.clickgui;

import dev.oryvex.client.anim.Animation;
import dev.oryvex.client.anim.Easing;
import dev.oryvex.client.font.Fonts;
import dev.oryvex.client.render.ColorUtil;
import dev.oryvex.client.render.RenderUtil;
import dev.oryvex.client.render.RoundedRenderer;
import dev.oryvex.client.setting.NumberSetting;
import dev.oryvex.client.ui.theme.Theme;

public final class NumberComponent extends SettingComponent<NumberSetting> {

    private final Animation fill = new Animation(Easing.EXPO_OUT, 200.0F, -1.0F);
    private boolean dragging;

    public NumberComponent(NumberSetting setting) {
        super(setting);
    }

    @Override
    public float getHeight() {
        return 30.0F;
    }

    @Override
    public void render(float x, float y, float w, int mouseX, int mouseY, float alpha) {
        Fonts.drawMiddle(this.setting.getName(), x, y + 8.0F, ColorUtil.mulAlpha(Theme.TEXT_SECONDARY, alpha), 8.5F, false);
        Fonts.drawRightMiddle(this.setting.format(), x + w, y + 8.0F, ColorUtil.mulAlpha(Theme.TEXT_PRIMARY, alpha), 8.5F, true);
        float trackY = y + 19.0F;
        float ratio = (float) this.setting.getRatio();
        if (this.fill.getTarget() < 0.0F) {
            this.fill.set(ratio);
        }
        float shown = this.fill.animateTo(ratio).get();
        RoundedRenderer.rect(x, trackY, w, 4.0F, 2.0F, ColorUtil.mulAlpha(Theme.BORDER, alpha));
        RoundedRenderer.gradient(x, trackY, Math.max(4.0F, w * shown), 4.0F, 2.0F,
            ColorUtil.mulAlpha(Theme.ACCENT_START, alpha), ColorUtil.mulAlpha(Theme.ACCENT_END, alpha), false);
        float knobX = x + w * shown;
        boolean hovered = RenderUtil.hovered(mouseX, mouseY, x - 4.0F, trackY - 5.0F, w + 8.0F, 14.0F);
        if (this.dragging || hovered) {
            RoundedRenderer.glow(knobX - 4.0F, trackY - 2.0F, 8.0F, 8.0F, 4.0F, 5.0F, ColorUtil.mulAlpha(Theme.GLOW, 0.4F * alpha));
        }
        RoundedRenderer.circle(knobX, trackY + 2.0F, this.dragging ? 4.5F : 4.0F, ColorUtil.mulAlpha(Theme.TEXT_PRIMARY, alpha));
    }

    @Override
    public boolean mouseClicked(float x, float y, float w, int mouseX, int mouseY, int button) {
        if (button == 0 && RenderUtil.hovered(mouseX, mouseY, x - 4.0F, y + 12.0F, w + 8.0F, 16.0F)) {
            this.dragging = true;
            this.mouseDragged(x, y, w, mouseX, mouseY);
            return true;
        }
        return false;
    }

    @Override
    public void mouseDragged(float x, float y, float w, int mouseX, int mouseY) {
        if (this.dragging) {
            this.setting.setRatio((mouseX - x) / w);
        }
    }

    @Override
    public void mouseReleased() {
        this.dragging = false;
    }
}
