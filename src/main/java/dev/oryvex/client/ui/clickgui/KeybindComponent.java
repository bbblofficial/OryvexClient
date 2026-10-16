package dev.oryvex.client.ui.clickgui;

import dev.oryvex.client.font.Fonts;
import dev.oryvex.client.render.ColorUtil;
import dev.oryvex.client.render.RenderUtil;
import dev.oryvex.client.render.RoundedRenderer;
import dev.oryvex.client.setting.KeybindSetting;
import dev.oryvex.client.ui.theme.Theme;
import dev.oryvex.client.ui.widget.OButton;
import org.lwjgl.input.Keyboard;

public final class KeybindComponent extends SettingComponent<KeybindSetting> {

    private boolean listening;

    public KeybindComponent(KeybindSetting setting) {
        super(setting);
    }

    @Override
    public float getHeight() {
        return 22.0F;
    }

    @Override
    public void render(float x, float y, float w, int mouseX, int mouseY, float alpha) {
        Fonts.drawMiddle(this.setting.getName(), x, y + 11.0F, ColorUtil.mulAlpha(Theme.TEXT_SECONDARY, alpha), 8.5F, false);
        String text = this.listening ? "Press a key..." : this.setting.getKeyName();
        float bw = Math.max(46.0F, Fonts.width(text, 8.0F, true) + 14.0F);
        float bx = x + w - bw;
        boolean hovered = RenderUtil.hovered(mouseX, mouseY, bx, y + 2.0F, bw, 18.0F);
        if (this.listening) {
            RoundedRenderer.glow(bx, y + 2.0F, bw, 18.0F, 4.0F, 5.0F, ColorUtil.mulAlpha(Theme.GLOW, 0.35F * alpha));
        }
        RoundedRenderer.bordered(bx, y + 2.0F, bw, 18.0F, 4.0F, ColorUtil.mulAlpha(Theme.OBSIDIAN, alpha), 1.0F,
            ColorUtil.mulAlpha(this.listening || hovered ? Theme.ACCENT_END : Theme.BORDER, alpha));
        Fonts.drawCenter(text, bx + bw / 2.0F, y + 11.0F,
            ColorUtil.mulAlpha(this.listening ? Theme.GLOW : Theme.TEXT_PRIMARY, alpha), 8.0F, true);
    }

    @Override
    public boolean mouseClicked(float x, float y, float w, int mouseX, int mouseY, int button) {
        if (RenderUtil.hovered(mouseX, mouseY, x, y, w, this.getHeight())) {
            this.listening = !this.listening;
            OButton.playClick();
            return true;
        }
        this.listening = false;
        return false;
    }

    @Override
    public boolean keyTyped(char typed, int keyCode) {
        if (!this.listening) {
            return false;
        }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.listening = false;
        } else if (keyCode == Keyboard.KEY_BACK || keyCode == Keyboard.KEY_DELETE) {
            this.setting.set(Keyboard.KEY_NONE);
            this.listening = false;
        } else if (keyCode != Keyboard.KEY_NONE) {
            this.setting.set(keyCode);
            this.listening = false;
        }
        return true;
    }

    @Override
    public boolean isCapturingKeys() {
        return this.listening;
    }
}
