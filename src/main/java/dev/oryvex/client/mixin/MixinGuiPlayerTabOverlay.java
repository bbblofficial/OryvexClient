package dev.oryvex.client.mixin;

import dev.oryvex.client.modules.visuals.TabMod;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Applies Tab module background controls while preserving vanilla player-list contents. */
@Mixin(GuiPlayerTabOverlay.class)
public abstract class MixinGuiPlayerTabOverlay {
    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;drawRect(IIIII)V"))
    private void oryvex$tabBackground(int left, int top, int right, int bottom, int color) {
        TabMod mod = TabMod.active();
        if (mod == null || mod.background.isOn()) {
            int alpha = mod == null ? (color >>> 24) : Math.round(mod.opacity.getFloat() * 255.0F);
            Gui.drawRect(left, top, right, bottom, (alpha << 24) | (color & 0x00FFFFFF));
        }
    }
}
