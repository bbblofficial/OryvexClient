package dev.oryvex.client.ui.widget;

import dev.oryvex.client.anim.Animation;
import dev.oryvex.client.anim.Easing;
import dev.oryvex.client.render.ColorUtil;
import dev.oryvex.client.render.RoundedRenderer;
import dev.oryvex.client.ui.theme.Theme;

/**
 * Animated blue pill switch (22 x 11 by default).
 */
public final class PillToggle {

    public static final float WIDTH = 22.0F;
    public static final float HEIGHT = 11.0F;

    private final Animation knob = new Animation(Easing.BACK_OUT, 340.0F, 0.0F);
    private final Animation fill = new Animation(Easing.EXPO_OUT, 260.0F, 0.0F);
    private boolean initialised;

    public void render(float x, float y, boolean on, float alpha) {
        if (!this.initialised) {
            this.knob.set(on ? 1.0F : 0.0F);
            this.fill.set(on ? 1.0F : 0.0F);
            this.initialised = true;
        }
        float k = this.knob.animateTo(on ? 1.0F : 0.0F).get();
        float f = this.fill.animateTo(on ? 1.0F : 0.0F).get();
        float r = HEIGHT / 2.0F;
        if (f > 0.02F) {
            RoundedRenderer.glow(x, y, WIDTH, HEIGHT, r, 5.0F, ColorUtil.mulAlpha(Theme.GLOW, 0.35F * f * alpha));
        }
        RoundedRenderer.bordered(x, y, WIDTH, HEIGHT, r, ColorUtil.mulAlpha(Theme.OBSIDIAN_SOLID, alpha), 1.0F,
            ColorUtil.mulAlpha(Theme.BORDER, alpha));
        if (f > 0.01F) {
            RoundedRenderer.gradient(x, y, WIDTH, HEIGHT, r, ColorUtil.mulAlpha(Theme.ACCENT_START, f * alpha),
                ColorUtil.mulAlpha(Theme.ACCENT_END, f * alpha), false);
        }
        float knobSize = HEIGHT - 4.0F;
        float kx = x + 2.0F + (WIDTH - knobSize - 4.0F) * k;
        RoundedRenderer.circle(kx + knobSize / 2.0F, y + HEIGHT / 2.0F, knobSize / 2.0F,
            ColorUtil.mulAlpha(ColorUtil.lerp(Theme.TEXT_SECONDARY, Theme.TEXT_PRIMARY, f), alpha));
    }
}
