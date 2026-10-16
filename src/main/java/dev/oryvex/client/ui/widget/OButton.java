package dev.oryvex.client.ui.widget;

import dev.oryvex.client.anim.Animation;
import dev.oryvex.client.anim.Easing;
import dev.oryvex.client.font.Fonts;
import dev.oryvex.client.render.ColorUtil;
import dev.oryvex.client.render.RenderUtil;
import dev.oryvex.client.render.RoundedRenderer;
import dev.oryvex.client.ui.theme.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

/**
 * Animated Oryvex button (hover easing, press bounce, glowing primary style).
 */
public class OButton {

    public enum Style {
        PRIMARY,
        SECONDARY,
        DANGER,
        GHOST
    }

    public float x;
    public float y;
    public float w;
    public float h;
    public String label;
    public Style style;
    public Runnable action;
    public boolean enabled = true;
    public boolean visible = true;
    public float textSize = 9.5F;

    private final Animation hover = new Animation(Easing.EXPO_OUT, 220.0F, 0.0F);
    private final Animation press = new Animation(Easing.BACK_OUT, 320.0F, 0.0F);

    public OButton(String label, Style style, Runnable action) {
        this.label = label;
        this.style = style;
        this.action = action;
    }

    public OButton bounds(float x, float y, float w, float h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        return this;
    }

    public boolean isHovered(int mouseX, int mouseY) {
        return this.visible && RenderUtil.hovered(mouseX, mouseY, this.x, this.y, this.w, this.h);
    }

    public void render(int mouseX, int mouseY, float alpha) {
        if (!this.visible) {
            return;
        }
        boolean hovered = this.enabled && this.isHovered(mouseX, mouseY);
        float t = this.hover.animateTo(hovered ? 1.0F : 0.0F).get();
        float p = this.press.animateTo(0.0F).get();
        float a = alpha * (this.enabled ? 1.0F : 0.45F);
        float shrink = Math.max(0.0F, p) * Math.min(this.w, this.h) * 0.05F;
        float bx = this.x + shrink;
        float by = this.y + shrink;
        float bw = this.w - shrink * 2.0F;
        float bh = this.h - shrink * 2.0F;
        float r = Math.min(Theme.RADIUS_BUTTON, bh / 2.0F);
        int text;
        boolean bold = false;
        switch (this.style) {
            case PRIMARY: {
                RoundedRenderer.glow(bx, by, bw, bh, r, 7.0F + 5.0F * t, ColorUtil.mulAlpha(Theme.GLOW, (0.30F + 0.35F * t) * a));
                int from = ColorUtil.lerp(Theme.ACCENT_START, Theme.ACCENT_END, 0.25F * t);
                int to = ColorUtil.lerp(Theme.ACCENT_END, Theme.GLOW, 0.45F * t);
                RoundedRenderer.gradientBordered(bx, by, bw, bh, r, ColorUtil.mulAlpha(from, a), ColorUtil.mulAlpha(to, a),
                    1.0F, ColorUtil.mulAlpha(ColorUtil.lerp(0x6600E5FF, Theme.GLOW, t), a));
                text = ColorUtil.mulAlpha(Theme.TEXT_PRIMARY, a);
                bold = true;
                break;
            }
            case DANGER: {
                int fill = ColorUtil.lerp(Theme.SLATE, 0xD92A0E16, t);
                RoundedRenderer.bordered(bx, by, bw, bh, r, ColorUtil.mulAlpha(fill, a), 1.0F,
                    ColorUtil.mulAlpha(ColorUtil.lerp(Theme.BORDER, Theme.DANGER, t), a));
                text = ColorUtil.mulAlpha(ColorUtil.lerp(Theme.TEXT_SECONDARY, Theme.DANGER, t), a);
                break;
            }
            case GHOST: {
                if (t > 0.01F) {
                    RoundedRenderer.rect(bx, by, bw, bh, r, ColorUtil.mulAlpha(Theme.SLATE_RAISED, t * a));
                }
                text = ColorUtil.mulAlpha(ColorUtil.lerp(Theme.TEXT_SECONDARY, Theme.ACCENT_END, t), a);
                break;
            }
            default: {
                int fill = ColorUtil.lerp(Theme.SLATE, Theme.SLATE_RAISED, t);
                RoundedRenderer.bordered(bx, by, bw, bh, r, ColorUtil.mulAlpha(fill, a), 1.0F,
                    ColorUtil.mulAlpha(ColorUtil.lerp(Theme.BORDER, Theme.ACCENT_END, t * 0.85F), a));
                if (t > 0.01F) {
                    RoundedRenderer.gradient(bx + 1.0F, by + bh - 2.0F, bw - 2.0F, 1.0F, 0.5F,
                        ColorUtil.mulAlpha(Theme.ACCENT_START, t * a), ColorUtil.mulAlpha(Theme.ACCENT_END, t * a), false);
                }
                text = ColorUtil.mulAlpha(ColorUtil.lerp(Theme.TEXT_SECONDARY, Theme.TEXT_PRIMARY, t), a);
                break;
            }
        }
        Fonts.drawCenter(this.label, bx + bw / 2.0F, by + bh / 2.0F, text, this.textSize, bold);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (!this.visible || !this.enabled || button != 0 || !this.isHovered(mouseX, mouseY)) {
            return false;
        }
        this.press.set(1.0F);
        playClick();
        if (this.action != null) {
            this.action.run();
        }
        return true;
    }

    public static void playClick() {
        Minecraft.getMinecraft().getSoundHandler().playSound(
            PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
    }
}
