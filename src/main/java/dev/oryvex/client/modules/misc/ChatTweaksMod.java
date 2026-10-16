package dev.oryvex.client.modules.misc;

import dev.oryvex.client.anim.Easing;
import dev.oryvex.client.module.Category;
import dev.oryvex.client.module.Module;
import dev.oryvex.client.module.ModuleManager;
import dev.oryvex.client.setting.BooleanSetting;
import dev.oryvex.client.setting.NumberSetting;

/**
 * Transparent smooth chat, unlimited history and Persian RTL input rendering.
 */
public final class ChatTweaksMod extends Module {

    public static final int VANILLA_HISTORY = 100;
    public static final int EXTENDED_HISTORY = 16384;
    private static final float SLIDE_MS = 180.0F;

    private final BooleanSetting transparent = this.add(new BooleanSetting("Transparent Background", true));
    private final NumberSetting opacity = this.add(new NumberSetting("Background Opacity", 25.0D, 0.0D, 100.0D, 5.0D, "%"))
        .visibleWhen(() -> this.transparent.isOn());
    private final BooleanSetting smooth = this.add(new BooleanSetting("Smooth Messages", true));
    private final BooleanSetting infiniteHistory = this.add(new BooleanSetting("Infinite History", true));
    private final BooleanSetting persianInput = this.add(new BooleanSetting("Persian RTL Input", true));

    private long lastMessageNanos;

    public ChatTweaksMod() {
        super("ChatTweaks", "بهبود چت", "Transparent smooth chat, infinite history and Persian RTL typing.", Category.MISC, true);
    }

    private static ChatTweaksMod active() {
        ChatTweaksMod mod = ModuleManager.module(ChatTweaksMod.class);
        return mod != null && mod.isEnabled() ? mod : null;
    }

    public static int historyLimit(int vanilla) {
        ChatTweaksMod mod = active();
        return mod != null && mod.infiniteHistory.isOn() ? EXTENDED_HISTORY : vanilla;
    }

    /** Adjusted chat line background color (vanilla passes alpha in the top byte). */
    public static int backgroundColor(int vanillaColor) {
        ChatTweaksMod mod = active();
        if (mod == null || !mod.transparent.isOn()) {
            return vanillaColor;
        }
        int alpha = Math.round((vanillaColor >>> 24) * mod.opacity.getFloat() / 100.0F);
        return (alpha << 24) | 0x07080A;
    }

    public static void onNewMessage() {
        ChatTweaksMod mod = active();
        if (mod != null) {
            mod.lastMessageNanos = System.nanoTime();
        }
    }

    /** Vertical offset (GUI units) applied while the newest message slides in. */
    public static float slideOffset() {
        ChatTweaksMod mod = active();
        if (mod == null || !mod.smooth.isOn() || mod.lastMessageNanos == 0L) {
            return 0.0F;
        }
        float elapsed = (System.nanoTime() - mod.lastMessageNanos) / 1_000_000.0F;
        if (elapsed >= SLIDE_MS) {
            return 0.0F;
        }
        return 9.0F * (1.0F - Easing.EXPO_OUT.apply(elapsed / SLIDE_MS));
    }

    public static boolean persianInput() {
        ChatTweaksMod mod = active();
        return mod != null && mod.persianInput.isOn();
    }
}
