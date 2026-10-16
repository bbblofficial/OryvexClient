package dev.oryvex.client.mixin;

import dev.oryvex.client.OryvexClient;
import dev.oryvex.client.bridge.IMixinMinecraft;
import dev.oryvex.client.core.ScreenRouter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.Session;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft implements IMixinMinecraft {

    @Shadow
    @Final
    @Mutable
    private Session session;

    @Shadow
    public WorldClient theWorld;

    @Override
    public void oryvex$setSession(Session newSession) {
        this.session = newSession;
    }

    @Override
    public Session oryvex$getSession() {
        return this.session;
    }

    /** Swaps vanilla title / pause screens for the Oryvex versions before Forge sees them. */
    @ModifyVariable(method = "displayGuiScreen", at = @At("HEAD"), argsOnly = true)
    private GuiScreen oryvex$routeScreen(GuiScreen requested) {
        return ScreenRouter.route(requested, this.theWorld != null);
    }

    @Inject(method = "shutdownMinecraftApplet", at = @At("HEAD"))
    private void oryvex$onShutdown(CallbackInfo ci) {
        OryvexClient.onShutdown();
    }
}
