package dev.oryvex.client.module;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.oryvex.client.hud.HudElement;
import dev.oryvex.client.setting.KeybindSetting;
import dev.oryvex.client.setting.Setting;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Base class for every Oryvex feature.
 */
public abstract class Module {

    protected final Minecraft mc = Minecraft.getMinecraft();

    private final String name;
    private final String persianName;
    private final String description;
    private final Category category;
    private final boolean enabledByDefault;
    private final List<Setting<?>> settings = new ArrayList<>();
    private final List<HudElement> hudElements = new ArrayList<>();
    private final KeybindSetting keybind = new KeybindSetting("Keybind", Keyboard.KEY_NONE);
    private boolean enabled;

    protected Module(String name, String persianName, String description, Category category, boolean enabledByDefault) {
        this.name = name;
        this.persianName = persianName;
        this.description = description;
        this.category = category;
        this.enabledByDefault = enabledByDefault;
        this.enabled = enabledByDefault;
    }

    protected <S extends Setting<?>> S add(S setting) {
        this.settings.add(setting);
        return setting;
    }

    protected <E extends HudElement> E addHud(E element) {
        this.hudElements.add(element);
        return element;
    }

    public final void toggle() {
        this.setEnabled(!this.enabled);
    }

    public final void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }
        this.enabled = enabled;
        if (enabled) {
            this.onEnable();
        } else {
            this.onDisable();
        }
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    /** Client tick (END phase) while enabled and in a world. */
    public void onTick() {
    }

    /** Key press while no screen is open (keyboard key code). */
    public void onKeyPressed(int keyCode) {
    }

    public String getName() {
        return this.name;
    }

    public String getPersianName() {
        return this.persianName;
    }

    public String getDescription() {
        return this.description;
    }

    public Category getCategory() {
        return this.category;
    }

    public KeybindSetting getKeybind() {
        return this.keybind;
    }

    /** Settings shown in the drawer (keybind last). */
    public List<Setting<?>> getSettings() {
        List<Setting<?>> all = new ArrayList<>(this.settings);
        all.add(this.keybind);
        return all;
    }

    public List<HudElement> getHudElements() {
        return Collections.unmodifiableList(this.hudElements);
    }

    public boolean matches(String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }
        String q = query.trim().toLowerCase(Locale.ROOT);
        return this.name.toLowerCase(Locale.ROOT).contains(q)
            || this.description.toLowerCase(Locale.ROOT).contains(q)
            || this.persianName.contains(query.trim())
            || this.category.getDisplayName().toLowerCase(Locale.ROOT).contains(q);
    }

    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        root.addProperty("enabled", this.enabled);
        JsonObject values = new JsonObject();
        for (Setting<?> setting : this.getSettings()) {
            values.add(setting.getName(), setting.toJson());
        }
        root.add("settings", values);
        JsonObject hud = new JsonObject();
        for (HudElement element : this.hudElements) {
            hud.add(element.getId(), element.toJson());
        }
        root.add("hud", hud);
        return root;
    }

    public void fromJson(JsonObject root) {
        if (root == null) {
            return;
        }
        if (root.has("settings") && root.get("settings").isJsonObject()) {
            JsonObject values = root.getAsJsonObject("settings");
            for (Setting<?> setting : this.getSettings()) {
                JsonElement element = values.get(setting.getName());
                if (element != null) {
                    setting.fromJson(element);
                }
            }
        }
        if (root.has("hud") && root.get("hud").isJsonObject()) {
            JsonObject hud = root.getAsJsonObject("hud");
            for (HudElement element : this.hudElements) {
                if (hud.has(element.getId()) && hud.get(element.getId()).isJsonObject()) {
                    element.fromJson(hud.getAsJsonObject(element.getId()));
                }
            }
        }
        if (root.has("enabled")) {
            this.setEnabled(root.get("enabled").getAsBoolean());
        }
    }

    public void resetToDefaults() {
        for (Setting<?> setting : this.getSettings()) {
            setting.reset();
        }
        for (HudElement element : this.hudElements) {
            element.resetPosition();
        }
        this.setEnabled(this.enabledByDefault);
    }
}
