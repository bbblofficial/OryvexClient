package dev.oryvex.client.mixin;

import dev.oryvex.client.modules.pvp.OldAnimationsMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 1.7.10 first-person animations. transformFirstPersonItem(equip, swing) is called with
 * swing = 0 while an item is in use; feeding the real swing progress restores 1.7
 * block-hitting / eat-hitting / bow-hitting. Ordinals follow the switch in
 * renderItemInFirstPerson: 0 NONE, 1 EAT/DRINK, 2 BLOCK, 3 BOW, 4 idle.
 */
@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {

    @Shadow
    @Final
    private Minecraft mc;

    @Shadow
    private ItemStack itemToRender;

    private float oryvex$swing;

    @Inject(method = "renderItemInFirstPerson", at = @At("HEAD"))
    private void oryvex$captureSwing(float partialTicks, CallbackInfo ci) {
        this.oryvex$swing = this.mc.thePlayer != null ? this.mc.thePlayer.getSwingProgress(partialTicks) : 0.0F;
    }

    @ModifyArg(method = "renderItemInFirstPerson", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/ItemRenderer;transformFirstPersonItem(FF)V", ordinal = 1), index = 1)
    private float oryvex$eatSwing(float swing) {
        return OldAnimationsMod.eatHit() ? this.oryvex$swing : swing;
    }

    @ModifyArg(method = "renderItemInFirstPerson", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/ItemRenderer;transformFirstPersonItem(FF)V", ordinal = 2), index = 1)
    private float oryvex$blockSwing(float swing) {
        return OldAnimationsMod.blockHit() ? this.oryvex$swing : swing;
    }

    @ModifyArg(method = "renderItemInFirstPerson", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/ItemRenderer;transformFirstPersonItem(FF)V", ordinal = 3), index = 1)
    private float oryvex$bowSwing(float swing) {
        return OldAnimationsMod.bowHit() ? this.oryvex$swing : swing;
    }

    @Inject(method = "renderItemInFirstPerson", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/ItemRenderer;renderItem(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemCameraTransforms$TransformType;)V"))
    private void oryvex$oldRod(float partialTicks, CallbackInfo ci) {
        if (OldAnimationsMod.oldRod() && this.itemToRender != null && this.itemToRender.getItem() instanceof ItemFishingRod) {
            GlStateManager.translate(0.08F, -0.027F, -0.33F);
            GlStateManager.scale(0.93F, 1.0F, 1.0F);
        }
    }
}
