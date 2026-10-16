package dev.oryvex.client.mixin;

import dev.oryvex.client.modules.misc.InterfaceMod;
import dev.oryvex.client.render.RoundedRenderer;
import dev.oryvex.client.ui.theme.Theme;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiOptionSlider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Replaces the textured slider knob of option sliders with an electric-blue track + knob.
 */
@Mixin(GuiOptionSlider.class)
public abstract class MixinGuiOptionSlider {

    @Shadow
    private float sliderValue;

    @Redirect(method = "mouseDragged", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/GuiOptionSlider;drawTexturedModalRect(IIIIII)V"))
    private void oryvex$drawKnob(GuiOptionSlider slider, int x, int y, int textureX, int textureY, int width, int height) {
        if (!InterfaceMod.themed()) {
            ((Gui) slider).drawTexturedModalRect(x, y, textureX, textureY, width, height);
            return;
        }
        if (textureX != 0) {
            return;
        }
        int left = slider.xPosition;
        int right = slider.xPosition + slider.getButtonWidth();
        float fill = (right - left - 8) * this.sliderValue + 4.0F;
        RoundedRenderer.gradient(left + 2.0F, y + 2.0F, Math.max(2.0F, fill - 2.0F), height - 4.0F, 3.0F,
            0x66007BFF, 0x8800B4FF, false);
        RoundedRenderer.glow(x, y + 2.0F, 8.0F, height - 4.0F, 3.0F, 4.0F, 0x5500E5FF);
        RoundedRenderer.bordered(x, y + 2.0F, 8.0F, height - 4.0F, 3.0F, Theme.ACCENT_END, 1.0F, Theme.GLOW);
    }
}
