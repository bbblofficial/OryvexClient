package dev.oryvex.client.module;

import dev.oryvex.client.hud.HudElement;
import dev.oryvex.client.modules.hud.ArmorStatusMod;
import dev.oryvex.client.modules.hud.KeystrokesMod;
import dev.oryvex.client.modules.hud.PerformanceHudMod;
import dev.oryvex.client.modules.hud.PotionStatusMod;
import dev.oryvex.client.modules.misc.ChatTweaksMod;
import dev.oryvex.client.modules.misc.InterfaceMod;
import dev.oryvex.client.modules.pvp.OldAnimationsMod;
import dev.oryvex.client.modules.pvp.ToggleSprintMod;
import dev.oryvex.client.modules.visuals.VisualsMod;
import dev.oryvex.client.modules.visuals.ScoreboardMod;
import dev.oryvex.client.modules.visuals.TabMod;
import dev.oryvex.client.modules.visuals.NametagMod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ModuleManager {

    private static ModuleManager instance;

    private final Map<Class<? extends Module>, Module> byClass = new LinkedHashMap<>();

    public ModuleManager() {
        instance = this;
    }

    public void registerAll() {
        this.register(new KeystrokesMod());
        this.register(new ToggleSprintMod());
        this.register(new ArmorStatusMod());
        this.register(new PotionStatusMod());
        this.register(new PerformanceHudMod());
        this.register(new OldAnimationsMod());
        this.register(new VisualsMod());
        this.register(new ScoreboardMod());
        this.register(new TabMod());
        this.register(new NametagMod());
        this.register(new ChatTweaksMod());
        this.register(new InterfaceMod());
    }

    private void register(Module module) {
        this.byClass.put(module.getClass(), module);
    }

    public static ModuleManager getInstance() {
        return instance;
    }

    /** Null-safe lookup usable from mixins before the client is initialised. */
    public static <T extends Module> T module(Class<T> type) {
        ModuleManager manager = instance;
        if (manager == null) {
            return null;
        }
        return type.cast(manager.byClass.get(type));
    }

    public List<Module> getModules() {
        return Collections.unmodifiableList(new ArrayList<>(this.byClass.values()));
    }

    public List<HudElement> getHudElements() {
        List<HudElement> elements = new ArrayList<>();
        for (Module module : this.byClass.values()) {
            elements.addAll(module.getHudElements());
        }
        return elements;
    }

    public Module byName(String name) {
        for (Module module : this.byClass.values()) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }
}
