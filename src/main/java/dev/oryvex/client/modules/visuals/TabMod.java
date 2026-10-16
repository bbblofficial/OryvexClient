package dev.oryvex.client.modules.visuals;

import dev.oryvex.client.module.Category;
import dev.oryvex.client.module.Module;
import dev.oryvex.client.module.ModuleManager;
import dev.oryvex.client.setting.BooleanSetting;
import dev.oryvex.client.setting.NumberSetting;

public final class TabMod extends Module {
    public final BooleanSetting background = this.add(new BooleanSetting("Background", true));
    public final NumberSetting opacity = this.add(new NumberSetting("Background Opacity", 0.82D, 0.15D, 1.0D, 0.05D));
    public final NumberSetting scale = this.add(new NumberSetting("Scale", 1.0D, 0.75D, 1.5D, 0.05D));
    public final BooleanSetting ping = this.add(new BooleanSetting("Show Ping", true));
    public final BooleanSetting health = this.add(new BooleanSetting("Show Health", false));
    public TabMod() { super("Tab", "", "Customize the player list background, scale, ping and health display.", Category.VISUALS, true); }
    public static TabMod active() { TabMod m = ModuleManager.module(TabMod.class); return m != null && m.isEnabled() ? m : null; }
}
