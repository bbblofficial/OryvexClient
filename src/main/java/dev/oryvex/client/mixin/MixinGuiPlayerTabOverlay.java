package dev.oryvex.client.mixin;

import dev.oryvex.client.modules.visuals.TabMod;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.gui.ScaledResolution;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;

/** Applies Tab module background controls while preserving vanilla player-list contents. */
@Mixin(GuiPlayerTabOverlay.class)
public abstract class MixinGuiPlayerTabOverlay {
    @Inject(method = "renderPlayerlist", at = @At("HEAD"))
    private void oryvex$beginTab(int width, net.minecraft.scoreboard.ScoreObjective objective, CallbackInfo ci) {
        TabMod mod = TabMod.active();
        if (mod != null) mod.beginRender(width, new ScaledResolution(net.minecraft.client.Minecraft.getMinecraft()).getScaledHeight());
    }

    @Inject(method = "renderPlayerlist", at = @At("RETURN"))
    private void oryvex$endTab(int width, net.minecraft.scoreboard.ScoreObjective objective, CallbackInfo ci) {
        TabMod mod = TabMod.active();
        if (mod != null) mod.endRender();
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;drawRect(IIIII)V"))
    private void oryvex$tabBackground(int left, int top, int right, int bottom, int color) {
        TabMod mod = TabMod.active();
        if (mod == null || mod.background.isOn()) {
            int alpha = mod == null ? (color >>> 24) : Math.round(mod.opacity.getFloat() * 255.0F);
            Gui.drawRect(left, top, right, bottom, (alpha << 24) | (color & 0x00FFFFFF));
        }
    }
}
