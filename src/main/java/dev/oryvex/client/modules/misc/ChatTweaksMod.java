package dev.oryvex.client.modules.misc;

import dev.oryvex.client.anim.Easing;
import dev.oryvex.client.module.Category;
import dev.oryvex.client.module.Module;
import dev.oryvex.client.module.ModuleManager;
import dev.oryvex.client.setting.BooleanSetting;
import dev.oryvex.client.setting.NumberSetting;

import java.util.Map;
import java.util.WeakHashMap;

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
    private static final float TYPE_MS = 120.0F;
    private final Map<Object, TypingState> typingStates = new WeakHashMap<>();

    private static final class TypingState {
        private String text = "";
        private long changedNanos;
    }

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

    /** Smoothly settles the chat input after each edit, without delaying keystrokes. */
    public static float typingProgress(Object field, String text) {
        ChatTweaksMod mod = active();
        if (mod == null || !mod.smooth.isOn() || field == null) return 1.0F;
        synchronized (mod.typingStates) {
            TypingState state = mod.typingStates.get(field);
            if (state == null) {
                state = new TypingState();
                mod.typingStates.put(field, state);
            }
            if (!String.valueOf(text).equals(state.text)) {
                state.text = String.valueOf(text);
                state.changedNanos = System.nanoTime();
            }
            float elapsed = (System.nanoTime() - state.changedNanos) / 1_000_000.0F;
            float t = Math.max(0.0F, Math.min(1.0F, elapsed / TYPE_MS));
            return Easing.CUBIC_OUT.apply(t);
        }
    }

    public static boolean persianInput() {
        ChatTweaksMod mod = active();
        return mod != null && mod.persianInput.isOn();
    }
}
