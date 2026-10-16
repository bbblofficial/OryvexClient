package dev.oryvex.client.ui.clickgui;

import dev.oryvex.client.font.Fonts;
import dev.oryvex.client.render.ColorUtil;
import dev.oryvex.client.render.RenderUtil;
import dev.oryvex.client.setting.BooleanSetting;
import dev.oryvex.client.ui.theme.Theme;
import dev.oryvex.client.ui.widget.OButton;
import dev.oryvex.client.ui.widget.PillToggle;

public final class BooleanComponent extends SettingComponent<BooleanSetting> {

    private final PillToggle toggle = new PillToggle();

    public BooleanComponent(BooleanSetting setting) {
        super(setting);
    }

    @Override
    public float getHeight() {
        return 20.0F;
    }

    @Override
    public void render(float x, float y, float w, int mouseX, int mouseY, float alpha) {
        boolean hovered = RenderUtil.hovered(mouseX, mouseY, x, y, w, this.getHeight());
        Fonts.drawMiddle(this.setting.getName(), x, y + 10.0F,
            ColorUtil.mulAlpha(hovered ? Theme.TEXT_PRIMARY : Theme.TEXT_SECONDARY, alpha), 8.5F, false);
        this.toggle.render(x + w - PillToggle.WIDTH, y + (20.0F - PillToggle.HEIGHT) / 2.0F, this.setting.isOn(), alpha);
    }

    @Override
    public boolean mouseClicked(float x, float y, float w, int mouseX, int mouseY, int button) {
        if (button == 0 && RenderUtil.hovered(mouseX, mouseY, x, y, w, this.getHeight())) {
            this.setting.toggle();
            OButton.playClick();
            return true;
        }
        return false;
    }
}
