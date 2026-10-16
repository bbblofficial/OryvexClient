package dev.oryvex.client.mixin;

import dev.oryvex.client.font.BiDiFontRenderer;
import dev.oryvex.client.font.FontHooks;
import dev.oryvex.client.modules.misc.ChatTweaksMod;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Persian (RTL) typing support for every text field, including the chat input: the
 * visible text is shaped and reordered, and the caret is placed at its visual position.
 */
@Mixin(GuiTextField.class)
public abstract class MixinGuiTextField extends Gui {

    @Shadow
    @Final
    private FontRenderer fontRendererInstance;

    @Shadow
    public int xPosition;

    @Shadow
    public int yPosition;

    @Shadow
    @Final
    private int height;

    @Shadow
    private String text;

    @Shadow
    private int cursorCounter;

    @Shadow
    private boolean isFocused;

    @Shadow
    private boolean isEnabled;

    @Shadow
    private int lineScrollOffset;

    @Shadow
    private int cursorPosition;

    @Shadow
    private int enabledColor;

    @Shadow
    private int disabledColor;

    @Shadow
    public abstract boolean getVisible();

    @Shadow
    public abstract boolean getEnableBackgroundDrawing();

    @Shadow
    public abstract int getWidth();

    @Inject(method = "drawTextBox", at = @At("HEAD"), cancellable = true)
    private void oryvex$drawRtl(CallbackInfo ci) {
        if (!this.getVisible()) {
            return;
        }
        ci.cancel();
        int boxWidth = this.getWidth() + (this.getEnableBackgroundDrawing() ? 8 : 0);
        if (this.getEnableBackgroundDrawing()) {
            drawRect(this.xPosition - 1, this.yPosition - 1, this.xPosition + boxWidth + 1, this.yPosition + this.height + 1, 0xFFA0A0A0);
            drawRect(this.xPosition, this.yPosition, this.xPosition + boxWidth, this.yPosition + this.height, 0xFF000000);
        }
        int color = this.isEnabled ? this.enabledColor : this.disabledColor;
        int offset = Math.max(0, Math.min(this.lineScrollOffset, this.text.length()));
        String visible = this.fontRendererInstance.trimStringToWidth(this.text.substring(offset), this.getWidth());
        int x = this.getEnableBackgroundDrawing() ? this.xPosition + 4 : this.xPosition;
        int y = this.getEnableBackgroundDrawing() ? this.yPosition + (this.height - 8) / 2 : this.yPosition;
        float typing = ChatTweaksMod.typingProgress(this, this.text);
        float settle = (1.0F - typing) * 4.0F;
        int textAlpha = Math.max(1, Math.min(255, Math.round(210.0F + 45.0F * typing)));
        int animatedColor = (textAlpha << 24) | (color & 0x00FFFFFF);
        this.fontRendererInstance.drawStringWithShadow(visible, x + settle, y, animatedColor);

        int caret = this.cursorPosition - offset;
        boolean caretVisible = this.isFocused && this.cursorCounter / 6 % 2 == 0 && caret >= 0 && caret <= visible.length();
        if (caretVisible) {
            float caretX = x + settle + BiDiFontRenderer.get().getCaretX(visible, caret, FontHooks.vanillaSize());
            int cx = Math.round(caretX);
            drawRect(cx, y - 1, cx + 1, y + 1 + this.fontRendererInstance.FONT_HEIGHT, 0xFFD0D0D0);
        }
    }
}
