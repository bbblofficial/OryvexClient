package dev.oryvex.client.mixin;

import dev.oryvex.client.modules.visuals.VisualsMod;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.Buffer;
import java.nio.FloatBuffer;

/**
 * Hit Color: replaces the red hurt overlay (written into brightnessBuffer) with the
 * configured tint right before the buffer is flipped and uploaded via glTexEnv.
 */
@Mixin(RendererLivingEntity.class)
public abstract class MixinRendererLivingEntity {

    private EntityLivingBase oryvex$entity;

    @Inject(method = "setBrightness", at = @At("HEAD"))
    private void oryvex$captureEntity(EntityLivingBase entity, float partialTicks, boolean combineTextures,
                                      CallbackInfoReturnable<Boolean> cir) {
        this.oryvex$entity = entity;
    }

    @Redirect(method = "setBrightness", at = @At(value = "INVOKE", target = "Ljava/nio/FloatBuffer;flip()Ljava/nio/Buffer;"))
    private Buffer oryvex$applyHitColor(FloatBuffer buffer) {
        EntityLivingBase entity = this.oryvex$entity;
        int tint = VisualsMod.hitTint();
        if (tint != 0 && entity != null && (entity.hurtTime > 0 || entity.deathTime > 0)) {
            float[] c = VisualsMod.tintComponents(tint);
            buffer.position(0);
            buffer.put(c[0]);
            buffer.put(c[1]);
            buffer.put(c[2]);
            buffer.put(c[3]);
        }
        return buffer.flip();
    }
}
