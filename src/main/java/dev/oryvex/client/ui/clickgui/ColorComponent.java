package dev.oryvex.client.ui.clickgui;

import dev.oryvex.client.anim.Animation;
import dev.oryvex.client.anim.Easing;
import dev.oryvex.client.font.Fonts;
import dev.oryvex.client.render.ColorUtil;
import dev.oryvex.client.render.RenderUtil;
import dev.oryvex.client.render.RoundedRenderer;
import dev.oryvex.client.setting.ColorSetting;
import dev.oryvex.client.ui.theme.Theme;
import dev.oryvex.client.ui.widget.OButton;

/**
 * HSB color picker: saturation/brightness square, hue strip and alpha strip.
 */
public final class ColorComponent extends SettingComponent<ColorSetting> {

    private static final float HEADER = 20.0F;
    private static final float PICKER = 76.0F;

    private final Animation expand = new Animation(Easing.EXPO_OUT, 260.0F, 0.0F);
    private boolean open;
    private int dragTarget = -1;
    private float hue;
    private float saturation;
    private float brightness;
    private int alpha;

    public ColorComponent(ColorSetting setting) {
        super(setting);
        this.syncFromSetting();
    }

    private void syncFromSetting() {
        int color = this.setting.getColor();
        float[] hsb = ColorUtil.toHsb(color);
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.brightness = hsb[2];
        this.alpha = ColorUtil.alpha(color);
    }

    private void push() {
        this.setting.set(ColorUtil.fromHsb(this.hue, this.saturation, this.brightness, this.alpha));
    }

    @Override
    public float getHeight() {
        return HEADER + PICKER * this.expand.animateTo(this.open ? 1.0F : 0.0F).get();
    }

    @Override
    public void render(float x, float y, float w, int mouseX, int mouseY, float alpha) {
        Fonts.drawMiddle(this.setting.getName(), x, y + 10.0F, ColorUtil.mulAlpha(Theme.TEXT_SECONDARY, alpha), 8.5F, false);
        int color = this.setting.getColor();
        String hex = ColorUtil.toHex(color);
        float swatchX = x + w - 18.0F;
        Fonts.drawRightMiddle(hex, swatchX - 5.0F, y + 10.0F, ColorUtil.mulAlpha(Theme.TEXT_MUTED, alpha), 7.5F, false);
        RoundedRenderer.bordered(swatchX, y + 4.0F, 18.0F, 12.0F, 3.0F, ColorUtil.mulAlpha(color | 0xFF000000, alpha), 1.0F,
            ColorUtil.mulAlpha(Theme.BORDER, alpha));

        float e = this.expand.get();
        if (e < 0.02F) {
            return;
        }
        float a = alpha * e;
        float top = y + HEADER + 2.0F;
        float squareW = w - 30.0F;
        float squareH = PICKER - 18.0F;
        int hueColor = ColorUtil.fromHsb(this.hue, 1.0F, 1.0F, 255);
        RenderUtil.gradient(x, top, squareW, squareH, ColorUtil.mulAlpha(0xFFFFFFFF, a), ColorUtil.mulAlpha(hueColor, a), false);
        RenderUtil.gradient(x, top, squareW, squareH, 0x00000000, ColorUtil.mulAlpha(0xFF000000, a), true);
        float sx = x + squareW * this.saturation;
        float sy = top + squareH * (1.0F - this.brightness);
        RoundedRenderer.circle(sx, sy, 3.0F, ColorUtil.mulAlpha(0xFFFFFFFF, a));
        RoundedRenderer.circle(sx, sy, 2.0F, ColorUtil.mulAlpha(color | 0xFF000000, a));

        float hueX = x + squareW + 6.0F;
        float segment = squareH / 6.0F;
        for (int i = 0; i < 6; i++) {
            int from = ColorUtil.fromHsb(i / 6.0F, 1.0F, 1.0F, 255);
            int to = ColorUtil.fromHsb((i + 1) / 6.0F, 1.0F, 1.0F, 255);
            RenderUtil.gradient(hueX, top + segment * i, 8.0F, segment + 0.5F, ColorUtil.mulAlpha(from, a), ColorUtil.mulAlpha(to, a), true);
        }
        RenderUtil.rect(hueX - 1.0F, top + squareH * this.hue - 0.75F, 10.0F, 1.5F, ColorUtil.mulAlpha(0xFFFFFFFF, a));

        float alphaX = hueX + 12.0F;
        RenderUtil.gradient(alphaX, top, 8.0F, squareH, ColorUtil.mulAlpha(color | 0xFF000000, a), ColorUtil.mulAlpha(0x00000000, a), true);
        RenderUtil.outline(alphaX, top, 8.0F, squareH, 0.5F, ColorUtil.mulAlpha(Theme.BORDER, a));
        RenderUtil.rect(alphaX - 1.0F, top + squareH * (1.0F - this.alpha / 255.0F) - 0.75F, 10.0F, 1.5F, ColorUtil.mulAlpha(0xFFFFFFFF, a));

        Fonts.draw("A " + Math.round(this.alpha / 2.55F) + "%", x, top + squareH + 4.0F, ColorUtil.mulAlpha(Theme.TEXT_MUTED, a), 7.0F);
    }

    @Override
    public boolean mouseClicked(float x, float y, float w, int mouseX, int mouseY, int button) {
        if (RenderUtil.hovered(mouseX, mouseY, x, y, w, HEADER)) {
            this.open = !this.open;
            if (this.open) {
                this.syncFromSetting();
            }
            OButton.playClick();
            return true;
        }
        if (!this.open) {
            return false;
        }
        float top = y + HEADER + 2.0F;
        float squareW = w - 30.0F;
        float squareH = PICKER - 18.0F;
        if (RenderUtil.hovered(mouseX, mouseY, x, top, squareW, squareH)) {
            this.dragTarget = 0;
        } else if (RenderUtil.hovered(mouseX, mouseY, x + squareW + 5.0F, top, 10.0F, squareH)) {
            this.dragTarget = 1;
        } else if (RenderUtil.hovered(mouseX, mouseY, x + squareW + 17.0F, top, 10.0F, squareH)) {
            this.dragTarget = 2;
        } else {
            return false;
        }
        this.mouseDragged(x, y, w, mouseX, mouseY);
        return true;
    }

    @Override
    public void mouseDragged(float x, float y, float w, int mouseX, int mouseY) {
        if (this.dragTarget < 0) {
            return;
        }
        float top = y + HEADER + 2.0F;
        float squareW = w - 30.0F;
        float squareH = PICKER - 18.0F;
        float ry = Math.max(0.0F, Math.min(1.0F, (mouseY - top) / squareH));
        if (this.dragTarget == 0) {
            this.saturation = Math.max(0.0F, Math.min(1.0F, (mouseX - x) / squareW));
            this.brightness = 1.0F - ry;
        } else if (this.dragTarget == 1) {
            this.hue = Math.min(0.999F, ry);
        } else {
            this.alpha = Math.round((1.0F - ry) * 255.0F);
        }
        this.push();
    }

    @Override
    public void mouseReleased() {
        this.dragTarget = -1;
    }
}
