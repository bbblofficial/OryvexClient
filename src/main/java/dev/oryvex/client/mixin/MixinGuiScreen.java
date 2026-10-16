package dev.oryvex.client.mixin;

import dev.oryvex.client.core.ThemedBackgrounds;
import dev.oryvex.client.modules.misc.InterfaceMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Dark-blue backgrounds for every vanilla screen (GuiOptions, GuiMultiplayer, ...).
 */
@Mixin(GuiScreen.class)
public abstract class MixinGuiScreen extends Gui {

    @Shadow
    public Minecraft mc;

    @Shadow
    public int width;

    @Shadow
    public int height;

    @Inject(method = "drawWorldBackground", at = @At("HEAD"), cancellable = true)
    private void oryvex$worldBackground(int tint, CallbackInfo ci) {
        if (InterfaceMod.themed() && this.mc.theWorld != null) {
            ThemedBackgrounds.world(this.width, this.height);
            ci.cancel();
        }
    }

    @Inject(method = "drawBackground", at = @At("HEAD"), cancellable = true)
    private void oryvex$menuBackground(int tint, CallbackInfo ci) {
        if (InterfaceMod.themed()) {
            ThemedBackgrounds.menu(this.width, this.height);
            ci.cancel();
        }
    }
}
