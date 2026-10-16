package dev.oryvex.client.mixin;

import dev.oryvex.client.modules.misc.ChatTweaksMod;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Transparent background, smooth slide-in for new lines and unlimited history.
 */
@Mixin(GuiNewChat.class)
public abstract class MixinGuiNewChat {

    @Inject(method = "drawChat", at = @At("HEAD"))
    private void oryvex$beginSmooth(int updateCounter, CallbackInfo ci) {
        GlStateManager.pushMatrix();
        float offset = ChatTweaksMod.slideOffset();
        if (offset != 0.0F) {
            GlStateManager.translate(0.0F, offset, 0.0F);
        }
    }

    @Inject(method = "drawChat", at = @At("RETURN"))
    private void oryvex$endSmooth(int updateCounter, CallbackInfo ci) {
        GlStateManager.popMatrix();
    }

    @Redirect(method = "drawChat", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/GuiNewChat;drawRect(IIIII)V", ordinal = 0))
    private void oryvex$lineBackground(int left, int top, int right, int bottom, int color) {
        int adjusted = ChatTweaksMod.backgroundColor(color);
        if ((adjusted >>> 24) > 0) {
            Gui.drawRect(left, top, right, bottom, adjusted);
        }
    }

    @ModifyConstant(method = "setChatLine", constant = @Constant(intValue = 100))
    private int oryvex$historyLimit(int vanilla) {
        return ChatTweaksMod.historyLimit(vanilla);
    }

    @Inject(method = "printChatMessageWithOptionalDeletion", at = @At("HEAD"))
    private void oryvex$onMessage(IChatComponent component, int chatLineId, CallbackInfo ci) {
        ChatTweaksMod.onNewMessage();
    }
}
