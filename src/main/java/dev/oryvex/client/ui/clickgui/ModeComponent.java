package dev.oryvex.client.ui.clickgui;

import dev.oryvex.client.font.Fonts;
import dev.oryvex.client.render.ColorUtil;
import dev.oryvex.client.render.RenderUtil;
import dev.oryvex.client.render.RoundedRenderer;
import dev.oryvex.client.setting.ModeSetting;
import dev.oryvex.client.ui.theme.Theme;
import dev.oryvex.client.ui.widget.OButton;

public final class ModeComponent extends SettingComponent<ModeSetting> {

    private static final float BOX_W = 92.0F;

    public ModeComponent(ModeSetting setting) {
        super(setting);
    }

    @Override
    public float getHeight() {
        return 22.0F;
    }

    @Override
    public void render(float x, float y, float w, int mouseX, int mouseY, float alpha) {
        Fonts.drawMiddle(this.setting.getName(), x, y + 11.0F, ColorUtil.mulAlpha(Theme.TEXT_SECONDARY, alpha), 8.5F, false);
        float bx = x + w - BOX_W;
        boolean hovered = RenderUtil.hovered(mouseX, mouseY, bx, y + 2.0F, BOX_W, 18.0F);
        RoundedRenderer.bordered(bx, y + 2.0F, BOX_W, 18.0F, 4.0F, ColorUtil.mulAlpha(Theme.OBSIDIAN, alpha), 1.0F,
            ColorUtil.mulAlpha(hovered ? Theme.ACCENT_END : Theme.BORDER, alpha));
        Fonts.drawMiddle("<", bx + 5.0F, y + 11.0F, ColorUtil.mulAlpha(Theme.ACCENT_END, alpha), 8.5F, true);
        Fonts.drawRightMiddle(">", bx + BOX_W - 5.0F, y + 11.0F, ColorUtil.mulAlpha(Theme.ACCENT_END, alpha), 8.5F, true);
        String value = Fonts.trim(this.setting.get(), BOX_W - 24.0F, 8.0F, true);
        Fonts.drawCenter(value, bx + BOX_W / 2.0F, y + 11.0F, ColorUtil.mulAlpha(Theme.TEXT_PRIMARY, alpha), 8.0F, true);
    }

    @Override
    public boolean mouseClicked(float x, float y, float w, int mouseX, int mouseY, int button) {
        float bx = x + w - BOX_W;
        if (!RenderUtil.hovered(mouseX, mouseY, bx, y + 2.0F, BOX_W, 18.0F)) {
            return false;
        }
        boolean left = mouseX < bx + BOX_W / 2.0F;
        this.setting.cycle(button == 1 || left ? -1 : 1);
        OButton.playClick();
        return true;
    }
}
