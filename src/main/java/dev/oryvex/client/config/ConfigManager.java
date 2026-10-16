package dev.oryvex.client.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.oryvex.client.OryvexClient;
import dev.oryvex.client.module.Module;
import dev.oryvex.client.module.ModuleManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Profile-based persistence: oryvex/profiles/&lt;name&gt;.json holds every module's state,
 * settings and HUD layout; oryvex/client.json remembers the active profile.
 */
public final class ConfigManager {

    public static final String DEFAULT_PROFILE = "Default";

    private final ModuleManager modules;
    private String activeProfile = DEFAULT_PROFILE;

    public ConfigManager(ModuleManager modules) {
        this.modules = modules;
    }

    public void loadActive() {
        File clientFile = OryvexPaths.file("client.json");
        if (clientFile.isFile()) {
            try {
                JsonElement element = JsonFiles.read(clientFile);
                if (element.isJsonObject() && element.getAsJsonObject().has("activeProfile")) {
                    this.activeProfile = sanitize(element.getAsJsonObject().get("activeProfile").getAsString());
                }
            } catch (Exception e) {
                OryvexClient.LOGGER.warn("Could not read client.json", e);
            }
        }
        this.loadProfile(this.activeProfile);
    }

    public void save() {
        this.saveProfile(this.activeProfile);
        JsonObject client = new JsonObject();
        client.addProperty("activeProfile", this.activeProfile);
        try {
            JsonFiles.write(OryvexPaths.file("client.json"), client);
        } catch (IOException e) {
            OryvexClient.LOGGER.error("Could not save client.json", e);
        }
    }

    public String getActiveProfile() {
        return this.activeProfile;
    }

    public List<String> listProfiles() {
        List<String> names = new ArrayList<>();
        File[] files = OryvexPaths.profiles().listFiles((dir, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                names.add(file.getName().substring(0, file.getName().length() - 5));
            }
        }
        if (!names.contains(this.activeProfile)) {
            names.add(this.activeProfile);
        }
        if (!names.contains(DEFAULT_PROFILE)) {
            names.add(DEFAULT_PROFILE);
        }
        Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
        return names;
    }

    public void switchProfile(String name) {
        String target = sanitize(name);
        if (target.equals(this.activeProfile)) {
            return;
        }
        this.saveProfile(this.activeProfile);
        this.activeProfile = target;
        this.loadProfile(target);
        this.save();
    }

    /** Creates a new profile cloned from the current state and activates it. */
    public String createProfile() {
        List<String> existing = this.listProfiles();
        int index = existing.size();
        String name;
        do {
            name = "Profile " + index++;
        } while (existing.contains(name));
        this.saveProfile(this.activeProfile);
        this.activeProfile = name;
        this.save();
        return name;
    }

    public void cycleProfile(int direction) {
        List<String> names = this.listProfiles();
        int index = names.indexOf(this.activeProfile);
        int next = ((index + direction) % names.size() + names.size()) % names.size();
        this.switchProfile(names.get(next));
    }

    private void loadProfile(String name) {
        File file = new File(OryvexPaths.profiles(), name + ".json");
        if (!file.isFile()) {
            for (Module module : this.modules.getModules()) {
                module.resetToDefaults();
            }
            return;
        }
        try {
            JsonElement element = JsonFiles.read(file);
            if (!element.isJsonObject()) {
                return;
            }
            JsonObject root = element.getAsJsonObject();
            JsonObject moduleObject = root.has("modules") ? root.getAsJsonObject("modules") : new JsonObject();
            for (Module module : this.modules.getModules()) {
                if (moduleObject.has(module.getName()) && moduleObject.get(module.getName()).isJsonObject()) {
                    module.fromJson(moduleObject.getAsJsonObject(module.getName()));
                } else {
                    module.resetToDefaults();
                }
            }
        } catch (Exception e) {
            OryvexClient.LOGGER.error("Could not load profile " + name, e);
        }
    }

    private void saveProfile(String name) {
        JsonObject root = new JsonObject();
        root.addProperty("version", 1);
        JsonObject moduleObject = new JsonObject();
        for (Module module : this.modules.getModules()) {
            moduleObject.add(module.getName(), module.toJson());
        }
        root.add("modules", moduleObject);
        try {
            JsonFiles.write(new File(OryvexPaths.profiles(), name + ".json"), root);
        } catch (IOException e) {
            OryvexClient.LOGGER.error("Could not save profile " + name, e);
        }
    }

    private static String sanitize(String name) {
        String cleaned = name == null ? "" : name.replaceAll("[^A-Za-z0-9 _-]", "").trim();
        return cleaned.isEmpty() ? DEFAULT_PROFILE : cleaned;
    }
}
