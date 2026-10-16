package dev.oryvex.client.modules.visuals;

import dev.oryvex.client.module.Category;
import dev.oryvex.client.module.Module;
import dev.oryvex.client.module.ModuleManager;
import dev.oryvex.client.setting.BooleanSetting;
import dev.oryvex.client.setting.ColorSetting;
import dev.oryvex.client.setting.NumberSetting;
import dev.oryvex.client.ui.theme.Theme;

public final class NametagMod extends Module {
    public final BooleanSetting background = this.add(new BooleanSetting("Background", false));
    public final BooleanSetting hideHealth = this.add(new BooleanSetting("Hide Health", false));
    public final BooleanSetting hideDistance = this.add(new BooleanSetting("Hide Distance", false));
    public final BooleanSetting hideInvisible = this.add(new BooleanSetting("Hide Invisible Players", false));
    public final NumberSetting opacity = this.add(new NumberSetting("Background Opacity", 0.70D, 0.10D, 1.0D, 0.05D));
    public final NumberSetting scale = this.add(new NumberSetting("Scale", 1.0D, 0.75D, 1.5D, 0.05D));
    public final ColorSetting textColor = this.add(new ColorSetting("Text Color", Theme.TEXT_PRIMARY));
    public NametagMod() { super("Nametag", "", "Customize nametag background, scale, health and visibility.", Category.VISUALS, true); }
    public static NametagMod active() { NametagMod m = ModuleManager.module(NametagMod.class); return m != null && m.isEnabled() ? m : null; }
}
