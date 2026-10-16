package dev.oryvex.client.modules.hud;

import dev.oryvex.client.anim.Animation;
import dev.oryvex.client.anim.Easing;
import dev.oryvex.client.font.Fonts;
import dev.oryvex.client.hud.HudElement;
import dev.oryvex.client.module.Category;
import dev.oryvex.client.module.Module;
import dev.oryvex.client.render.ColorUtil;
import dev.oryvex.client.render.RoundedRenderer;
import dev.oryvex.client.setting.BooleanSetting;
import dev.oryvex.client.setting.ColorSetting;
import dev.oryvex.client.setting.NumberSetting;
import dev.oryvex.client.ui.theme.Theme;
import dev.oryvex.client.util.CpsTracker;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public final class KeystrokesMod extends Module {

    private static final int W = 0;
    private static final int A = 1;
    private static final int S = 2;
    private static final int D = 3;
    private static final int LMB = 4;
    private static final int RMB = 5;
    private static final int SPACE = 6;

    private final BooleanSetting mouseButtons = this.add(new BooleanSetting("Mouse Buttons", true));
    private final BooleanSetting showCps = this.add(new BooleanSetting("Show CPS", true));
    private final BooleanSetting spaceBar = this.add(new BooleanSetting("Space Bar", true));
    private final BooleanSetting pressGlow = this.add(new BooleanSetting("Press Glow", true));
    private final NumberSetting keySize = this.add(new NumberSetting("Key Size", 22.0D, 16.0D, 30.0D, 1.0D, "px"));
    private final NumberSetting gap = this.add(new NumberSetting("Gap", 2.0D, 0.0D, 6.0D, 0.5D, "px"));
    private final ColorSetting pressedColor = this.add(new ColorSetting("Pressed Color", Theme.ACCENT_END));
    private final Animation[] presses = new Animation[7];

    public KeystrokesMod() {
        super("Keystrokes", "نمایش کلیدها", "WASD, mouse buttons and live CPS with a black-blue press animation.",
            Category.HUD, true);
        for (int i = 0; i < this.presses.length; i++) {
            this.presses[i] = new Animation(Easing.EXPO_OUT, 140.0F, 0.0F);
        }
        this.addHud(new HudElement(this, "keystrokes", "Keystrokes", 0.01F, 0.42F) {
            @Override
            public float getWidth(boolean preview) {
                return KeystrokesMod.this.size() * 3.0F + KeystrokesMod.this.gap() * 2.0F;
            }

            @Override
            public float getHeight(boolean preview) {
                return KeystrokesMod.this.totalHeight();
            }

            @Override
            protected void render(float partialTicks, boolean preview) {
                KeystrokesMod.this.renderKeys();
            }
        });
    }

    private float size() {
        return this.keySize.getFloat();
    }

    private float gap() {
        return this.gap.getFloat();
    }

    private float totalHeight() {
        float s = this.size();
        float g = this.gap();
        float h = s * 2.0F + g;
        if (this.mouseButtons.isOn()) {
            h += g + s * 0.9F;
        }
        if (this.spaceBar.isOn()) {
            h += g + s * 0.5F;
        }
        return h;
    }

    private boolean down(KeyBinding binding) {
        int code = binding.getKeyCode();
        if (this.mc.currentScreen != null) {
            return false;
        }
        if (code < 0) {
            return org.lwjgl.input.Mouse.isButtonDown(code + 100);
        }
        return code > 0 && Keyboard.isKeyDown(code);
    }

    private void renderKeys() {
        float s = this.size();
        float g = this.gap();
        float full = s * 3.0F + g * 2.0F;
        this.key(s + g, 0.0F, s, s, this.label(this.mc.gameSettings.keyBindForward, "W"), null, W, this.down(this.mc.gameSettings.keyBindForward));
        this.key(0.0F, s + g, s, s, this.label(this.mc.gameSettings.keyBindLeft, "A"), null, A, this.down(this.mc.gameSettings.keyBindLeft));
        this.key(s + g, s + g, s, s, this.label(this.mc.gameSettings.keyBindBack, "S"), null, S, this.down(this.mc.gameSettings.keyBindBack));
        this.key((s + g) * 2.0F, s + g, s, s, this.label(this.mc.gameSettings.keyBindRight, "D"), null, D, this.down(this.mc.gameSettings.keyBindRight));
        float y = s * 2.0F + g * 2.0F;
        if (this.mouseButtons.isOn()) {
            float half = (full - g) / 2.0F;
            float h = s * 0.9F;
            String left = this.showCps.isOn() ? CpsTracker.left() + " CPS" : null;
            String right = this.showCps.isOn() ? CpsTracker.right() + " CPS" : null;
            this.key(0.0F, y, half, h, "LMB", left, LMB, this.mc.currentScreen == null && org.lwjgl.input.Mouse.isButtonDown(0));
            this.key(half + g, y, half, h, "RMB", right, RMB, this.mc.currentScreen == null && org.lwjgl.input.Mouse.isButtonDown(1));
            y += h + g;
        }
        if (this.spaceBar.isOn()) {
            float h = s * 0.5F;
            float t = this.presses[SPACE].animateTo(this.down(this.mc.gameSettings.keyBindJump) ? 1.0F : 0.0F).get();
            this.base(0.0F, y, full, h, t);
            float barW = full * 0.4F;
            RoundedRenderer.rect((full - barW) / 2.0F, y + h / 2.0F - 0.75F, barW, 1.5F, 0.75F,
                ColorUtil.lerp(Theme.TEXT_SECONDARY, Theme.TEXT_PRIMARY, t));
        }
    }

    private String label(KeyBinding binding, String fallback) {
        int code = binding.getKeyCode();
        if (code > 0) {
            String name = Keyboard.getKeyName(code);
            if (name != null && name.length() <= 3) {
                return name;
            }
        }
        return fallback;
    }

    private void base(float x, float y, float w, float h, float t) {
        if (this.pressGlow.isOn() && t > 0.02F) {
            RoundedRenderer.glow(x, y, w, h, 4.0F, 6.0F, ColorUtil.mulAlpha(Theme.GLOW, 0.45F * t));
        }
        RoundedRenderer.bordered(x, y, w, h, 4.0F, Theme.SLATE, 1.0F, ColorUtil.lerp(Theme.BORDER, Theme.ACCENT_END, t));
        if (t > 0.01F) {
            RoundedRenderer.gradient(x, y, w, h, 4.0F, ColorUtil.mulAlpha(Theme.ACCENT_START, t),
                ColorUtil.mulAlpha(this.pressedColor.getColor(), t), false);
        }
    }

    private void key(float x, float y, float w, float h, String label, String sub, int index, boolean pressed) {
        float t = this.presses[index].animateTo(pressed ? 1.0F : 0.0F).get();
        this.base(x, y, w, h, t);
        int color = ColorUtil.lerp(Theme.TEXT_SECONDARY, Theme.TEXT_PRIMARY, t);
        float scale = 1.0F - 0.06F * t;
        float size = 8.5F * scale * (this.size() / 22.0F);
        if (sub == null) {
            Fonts.drawCenter(label, x + w / 2.0F, y + h / 2.0F, color, size, true);
        } else {
            Fonts.drawCenter(label, x + w / 2.0F, y + h / 2.0F - size * 0.45F, color, size, true);
            Fonts.drawCenter(sub, x + w / 2.0F, y + h / 2.0F + size * 0.55F, ColorUtil.mulAlpha(color, 0.8F), size * 0.72F, false);
        }
    }
}
