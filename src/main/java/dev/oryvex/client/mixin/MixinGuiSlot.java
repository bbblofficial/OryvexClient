package dev.oryvex.client.mixin;

import dev.oryvex.client.core.ThemedBackgrounds;
import dev.oryvex.client.modules.misc.InterfaceMod;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.Tessellator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Themed list backgrounds for GuiMultiplayer, world selection, resource packs, etc.
 */
@Mixin(GuiSlot.class)
public abstract class MixinGuiSlot {

    @Shadow
    protected int width;

    @Shadow
    protected int top;

    @Shadow
    protected int bottom;

    @Shadow
    protected int left;

    @Shadow
    protected int right;

    @Inject(method = "drawContainerBackground", at = @At("HEAD"), cancellable = true, remap = false)
    private void oryvex$containerBackground(Tessellator tessellator, CallbackInfo ci) {
        if (InterfaceMod.themed()) {
            ThemedBackgrounds.list(this.left, this.top, this.right, this.bottom);
            ci.cancel();
        }
    }

    @Inject(method = "overlayBackground", at = @At("HEAD"), cancellable = true)
    private void oryvex$overlayBackground(int startY, int endY, int startAlpha, int endAlpha, CallbackInfo ci) {
        if (InterfaceMod.themed()) {
            ThemedBackgrounds.listOverlay(this.left, this.width, startY, endY);
            ci.cancel();
        }
    }
}
