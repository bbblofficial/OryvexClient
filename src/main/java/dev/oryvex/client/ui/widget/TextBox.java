package dev.oryvex.client.ui.widget;

import dev.oryvex.client.anim.Animation;
import dev.oryvex.client.anim.Easing;
import dev.oryvex.client.font.Fonts;
import dev.oryvex.client.render.ColorUtil;
import dev.oryvex.client.render.RenderUtil;
import dev.oryvex.client.render.RoundedRenderer;
import dev.oryvex.client.ui.theme.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;

/**
 * Rounded input wrapping vanilla GuiTextField (native editing, selection and clipboard).
 * Text renders through the BiDi engine, so Persian input shapes and reorders correctly.
 */
public final class TextBox {

    public float x;
    public float y;
    public float w;
    public float h;
    private final String placeholder;
    private final int maxLength;
    private GuiTextField field;
    private final Animation focus = new Animation(Easing.EXPO_OUT, 220.0F, 0.0F);

    public TextBox(String placeholder, int maxLength) {
        this.placeholder = placeholder;
        this.maxLength = maxLength;
        this.field = this.create(0, 0, 10);
    }

    private GuiTextField create(int x, int y, int width) {
        GuiTextField created = new GuiTextField(0, Minecraft.getMinecraft().fontRendererObj, x, y, Math.max(10, width), 10);
        created.setMaxStringLength(this.maxLength);
        created.setEnableBackgroundDrawing(false);
        created.setTextColor(0xFFFFFF);
        return created;
    }

    /** Positions the box; the inner vanilla field is rebuilt because its size is final. */
    public TextBox bounds(float x, float y, float w, float h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        String text = this.field.getText();
        boolean focused = this.field.isFocused();
        this.field = this.create(Math.round(x + 7.0F), Math.round(y + (h - 8.0F) / 2.0F), Math.round(w - 14.0F));
        this.field.setText(text);
        this.field.setFocused(focused);
        return this;
    }

    public void render(int mouseX, int mouseY, float alpha) {
        float t = this.focus.animateTo(this.field.isFocused() ? 1.0F : 0.0F).get();
        if (t > 0.02F) {
            RoundedRenderer.glow(this.x, this.y, this.w, this.h, Theme.RADIUS_BUTTON, 4.0F, ColorUtil.mulAlpha(Theme.GLOW, 0.18F * t * alpha));
        }
        RoundedRenderer.bordered(this.x, this.y, this.w, this.h, Theme.RADIUS_BUTTON,
            ColorUtil.mulAlpha(Theme.OBSIDIAN, alpha), 1.0F,
            ColorUtil.mulAlpha(ColorUtil.lerp(Theme.BORDER, Theme.ACCENT_END, t), alpha));
        if (this.field.getText().isEmpty() && !this.field.isFocused()) {
            Fonts.drawMiddle(this.placeholder, this.x + 7.0F, this.y + this.h / 2.0F, ColorUtil.mulAlpha(Theme.TEXT_MUTED, alpha), 9.0F, false);
        }
        this.field.drawTextBox();
    }

    public void update() {
        this.field.updateCursorCounter();
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        boolean inside = RenderUtil.hovered(mouseX, mouseY, this.x, this.y, this.w, this.h);
        if (inside) {
            this.field.setFocused(true);
            this.field.mouseClicked(Math.max(mouseX, this.field.xPosition), this.field.yPosition + 1, button);
        } else {
            this.field.setFocused(false);
        }
        return inside;
    }

    public boolean keyTyped(char typed, int keyCode) {
        return this.field.isFocused() && this.field.textboxKeyTyped(typed, keyCode);
    }

    public boolean isFocused() {
        return this.field.isFocused();
    }

    public void setFocused(boolean focused) {
        this.field.setFocused(focused);
    }

    public String getText() {
        return this.field.getText();
    }

    public void setText(String text) {
        this.field.setText(text);
    }
}
