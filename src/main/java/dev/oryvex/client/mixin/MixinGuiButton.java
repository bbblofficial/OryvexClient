package dev.oryvex.client.mixin;

import dev.oryvex.client.anim.AnimationClock;
import dev.oryvex.client.font.Fonts;
import dev.oryvex.client.modules.misc.InterfaceMod;
import dev.oryvex.client.render.ColorUtil;
import dev.oryvex.client.render.RoundedRenderer;
import dev.oryvex.client.ui.theme.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Restyles every vanilla GuiButton (options, multiplayer, containers...) in the
 * deep-black / electric-blue theme with an eased hover state.
 */
@Mixin(GuiButton.class)
public abstract class MixinGuiButton extends Gui {

    @Shadow
    protected int width;

    @Shadow
    protected int height;

    @Shadow
    public int xPosition;

    @Shadow
    public int yPosition;

    @Shadow
    public String displayString;

    @Shadow
    public boolean enabled;

    @Shadow
    public boolean visible;

    @Shadow
    protected boolean hovered;

    @Shadow(remap = false)
    public int packedFGColour;

    @Shadow
    protected abstract void mouseDragged(Minecraft mc, int mouseX, int mouseY);

    private float oryvex$hover;
    private long oryvex$frame = -1L;

    @Inject(method = "drawButton", at = @At("HEAD"), cancellable = true)
    private void oryvex$drawButton(Minecraft mc, int mouseX, int mouseY, CallbackInfo ci) {
        if (!InterfaceMod.themed()) {
            return;
        }
        ci.cancel();
        if (!this.visible) {
            return;
        }
        this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition
            && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
        long frame = AnimationClock.frame();
        if (frame != this.oryvex$frame) {
            this.oryvex$frame = frame;
            float target = this.hovered && this.enabled ? 1.0F : 0.0F;
            float k = 1.0F - (float) Math.exp(-AnimationClock.deltaMs() / 70.0F);
            this.oryvex$hover += (target - this.oryvex$hover) * k;
        }
        float t = this.oryvex$hover;
        float x = this.xPosition;
        float y = this.yPosition;
        float w = this.width;
        float h = this.height;
        float r = Math.min(Theme.RADIUS_BUTTON, h / 2.0F);
        if (!this.enabled) {
            RoundedRenderer.bordered(x, y, w, h, r, 0xB30B0E14, 1.0F, 0xFF151B26);
        } else {
            if (t > 0.02F) {
                RoundedRenderer.glow(x, y, w, h, r, 5.0F, ColorUtil.mulAlpha(Theme.GLOW, 0.22F * t));
            }
            RoundedRenderer.bordered(x, y, w, h, r, ColorUtil.lerp(Theme.SLATE, Theme.SLATE_RAISED, t), 1.0F,
                ColorUtil.lerp(Theme.BORDER, Theme.ACCENT_END, t));
            if (t > 0.02F) {
                RoundedRenderer.gradient(x + 2.0F, y + h - 2.0F, w - 4.0F, 1.0F, 0.5F,
                    ColorUtil.mulAlpha(Theme.ACCENT_START, t), ColorUtil.mulAlpha(Theme.ACCENT_END, t), false);
            }
        }
        this.mouseDragged(mc, mouseX, mouseY);
        int color;
        if (this.packedFGColour != 0) {
            color = 0xFF000000 | this.packedFGColour;
        } else if (!this.enabled) {
            color = Theme.TEXT_MUTED;
        } else {
            color = ColorUtil.lerp(Theme.TEXT_SECONDARY, Theme.TEXT_PRIMARY, t);
        }
        String label = this.displayString == null ? "" : this.displayString;
        float size = Math.min(9.5F, h * 0.5F);
        Fonts.drawCenter(Fonts.trim(label, w - 6.0F, size, false), x + w / 2.0F, y + h / 2.0F, color, size, false);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
