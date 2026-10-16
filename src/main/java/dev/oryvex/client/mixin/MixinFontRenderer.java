package dev.oryvex.client.mixin;

import dev.oryvex.client.font.BiDiFontRenderer;
import dev.oryvex.client.font.FontHooks;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Routes the main FontRenderer through the BiDi engine so Persian shapes correctly
 * everywhere (chat, HUD, buttons, nametags, tooltips, scoreboard).
 */
@Mixin(FontRenderer.class)
public abstract class MixinFontRenderer {

    @Inject(method = "renderString", at = @At("HEAD"), cancellable = true)
    private void oryvex$renderString(String text, float x, float y, int color, boolean dropShadow,
                                     CallbackInfoReturnable<Integer> cir) {
        if (FontHooks.handles((FontRenderer) (Object) this, text)) {
            cir.setReturnValue(BiDiFontRenderer.get().renderVanilla(text, x, y, color, dropShadow, FontHooks.vanillaSize()));
        }
    }

    @Inject(method = "getStringWidth", at = @At("HEAD"), cancellable = true)
    private void oryvex$getStringWidth(String text, CallbackInfoReturnable<Integer> cir) {
        if (FontHooks.handles((FontRenderer) (Object) this, text)) {
            cir.setReturnValue(BiDiFontRenderer.get().vanillaWidth(text, FontHooks.vanillaSize()));
        }
    }

    @Inject(method = "getCharWidth", at = @At("HEAD"), cancellable = true)
    private void oryvex$getCharWidth(char character, CallbackInfoReturnable<Integer> cir) {
        if (FontHooks.handlesChar((FontRenderer) (Object) this, character)) {
            cir.setReturnValue(BiDiFontRenderer.get().vanillaCharWidth(character, FontHooks.vanillaSize()));
        }
    }
}
