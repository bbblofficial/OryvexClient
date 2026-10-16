package dev.oryvex.client.bridge;

import net.minecraft.util.Session;

/**
 * Duck interface implemented on {@code Minecraft} by MixinMinecraft.
 * The session field is shadowed there as {@code @Shadow @Final @Mutable} (Mixin 0.7.11's
 * {@code @Mutable} only applies to fields, so it lives on the shadow, not on an accessor).
 */
public interface IMixinMinecraft {

    void oryvex$setSession(Session session);

    Session oryvex$getSession();
}
