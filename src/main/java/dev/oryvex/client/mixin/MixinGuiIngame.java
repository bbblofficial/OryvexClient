package dev.oryvex.client.mixin;

import dev.oryvex.client.modules.visuals.ScoreboardMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.scoreboard.ScoreObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Replaces vanilla scoreboard rendering while leaving the rest of the HUD untouched. */
@Mixin(GuiIngame.class)
public abstract class MixinGuiIngame {
    @Shadow
    protected Minecraft mc;

    @Inject(method = "renderScoreboard", at = @At("HEAD"), cancellable = true)
    private void oryvex$renderScoreboard(ScoreObjective objective, ScaledResolution resolution, CallbackInfo ci) {
        ScoreboardMod mod = ScoreboardMod.active();
        if (mod != null) {
            ci.cancel();
            mod.renderObjective(objective, resolution);
        }
    }
}
