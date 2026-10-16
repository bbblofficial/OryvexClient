package dev.oryvex.client.core;

import dev.oryvex.client.modules.misc.InterfaceMod;
import dev.oryvex.client.ui.screen.OryvexIngameMenu;
import dev.oryvex.client.ui.screen.OryvexMainMenu;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;

/**
 * Maps vanilla screens to their Oryvex replacements (called from MixinMinecraft).
 */
public final class ScreenRouter {

    private static boolean ready;

    private ScreenRouter() {
    }

    public static void markReady() {
        ready = true;
    }

    public static GuiScreen route(GuiScreen requested, boolean worldLoaded) {
        if (!ready || !InterfaceMod.customMenus()) {
            return requested;
        }
        if (requested == null) {
            return worldLoaded ? null : new OryvexMainMenu();
        }
        Class<?> type = requested.getClass();
        if (type == GuiMainMenu.class) {
            return new OryvexMainMenu();
        }
        if (type == GuiIngameMenu.class) {
            return new OryvexIngameMenu();
        }
        return requested;
    }
}
