package dev.oryvex.client.font;

import dev.oryvex.client.module.ModuleManager;
import dev.oryvex.client.modules.misc.InterfaceMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

/**
 * Decides when vanilla FontRenderer calls are routed through the BiDi engine.
 * Persian/Arabic text is ALWAYS routed (vanilla cannot shape it); Latin text is routed
 * when the Interface module's font mode is "All Text".
 */
public final class FontHooks {

    private FontHooks() {
    }

    public static boolean handles(FontRenderer renderer, String text) {
        if (text == null || text.isEmpty() || !FontRegistry.isLoaded()) {
            return false;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || renderer != mc.fontRendererObj || !mc.isCallingFromMinecraftThread()) {
            return false;
        }
        InterfaceMod ui = ModuleManager.module(InterfaceMod.class);
        if (ui != null && ui.isEnabled() && ui.customFontEverywhere()) {
            return true;
        }
        return Scripts.containsComplex(text);
    }

    public static boolean handlesChar(FontRenderer renderer, char c) {
        if (!FontRegistry.isLoaded()) {
            return false;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || renderer != mc.fontRendererObj) {
            return false;
        }
        InterfaceMod ui = ModuleManager.module(InterfaceMod.class);
        if (ui != null && ui.isEnabled() && ui.customFontEverywhere()) {
            return true;
        }
        return Scripts.isComplex(c);
    }

    public static float vanillaSize() {
        InterfaceMod ui = ModuleManager.module(InterfaceMod.class);
        return ui != null ? ui.fontSize() : 9.0F;
    }
}
