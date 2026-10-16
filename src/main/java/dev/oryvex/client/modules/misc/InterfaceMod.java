package dev.oryvex.client.modules.misc;

import dev.oryvex.client.font.BiDiFontRenderer;
import dev.oryvex.client.module.Category;
import dev.oryvex.client.module.Module;
import dev.oryvex.client.module.ModuleManager;
import dev.oryvex.client.setting.BooleanSetting;
import dev.oryvex.client.setting.ModeSetting;
import dev.oryvex.client.setting.NumberSetting;

/**
 * Global look and feel: font engine scope, custom menus, themed vanilla widgets and blur.
 */
public final class InterfaceMod extends Module {

    private final ModeSetting fontMode = this.add(new ModeSetting("Font Engine", "All Text", "All Text", "Persian Only"));
    private final NumberSetting fontSize = this.add(new NumberSetting("Font Size", 9.0D, 8.0D, 11.0D, 0.5D));
    private final BooleanSetting customMenus = this.add(new BooleanSetting("Custom Menus", true));
    private final BooleanSetting themedWidgets = this.add(new BooleanSetting("Themed Vanilla Screens", true));
    private final BooleanSetting blur = this.add(new BooleanSetting("Background Blur", true));
    private final NumberSetting blurStrength = this.add(new NumberSetting("Blur Strength", 3.0D, 1.0D, 5.0D, 1.0D))
        .visibleWhen(() -> this.blur.isOn());

    public InterfaceMod() {
        super("Interface", "رابط کاربری", "Font engine, custom menus, themed vanilla screens and glass blur.", Category.MISC, true);
    }

    private static InterfaceMod active() {
        InterfaceMod mod = ModuleManager.module(InterfaceMod.class);
        return mod != null && mod.isEnabled() ? mod : null;
    }

    public boolean customFontEverywhere() {
        return this.fontMode.is("All Text");
    }

    public float fontSize() {
        return this.fontSize.getFloat();
    }

    public static boolean customMenus() {
        InterfaceMod mod = active();
        return mod == null || mod.customMenus.isOn();
    }

    public static boolean themed() {
        InterfaceMod mod = active();
        return mod != null && mod.themedWidgets.isOn();
    }

    public static boolean blurEnabled() {
        InterfaceMod mod = active();
        return mod == null || mod.blur.isOn();
    }

    public static int blurStrength() {
        InterfaceMod mod = active();
        return mod == null ? 3 : mod.blurStrength.getInt();
    }

    @Override
    protected void onDisable() {
        BiDiFontRenderer.get().invalidate();
    }
}
