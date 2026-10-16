package dev.oryvex.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class ModeSetting extends Setting<String> {

    private final List<String> modes;

    public ModeSetting(String name, String defaultValue, String... modes) {
        super(name, defaultValue);
        this.modes = Collections.unmodifiableList(Arrays.asList(modes));
        if (!this.modes.contains(defaultValue)) {
            throw new IllegalArgumentException("Default mode " + defaultValue + " is not one of " + this.modes);
        }
    }

    public List<String> getModes() {
        return this.modes;
    }

    public boolean is(String mode) {
        return this.value.equalsIgnoreCase(mode);
    }

    public int index() {
        return Math.max(0, this.modes.indexOf(this.value));
    }

    public void cycle(int direction) {
        int size = this.modes.size();
        int next = ((this.index() + direction) % size + size) % size;
        this.value = this.modes.get(next);
    }

    @Override
    public void set(String v) {
        for (String mode : this.modes) {
            if (mode.equalsIgnoreCase(v)) {
                this.value = mode;
                return;
            }
        }
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(this.value);
    }

    @Override
    public void fromJson(JsonElement element) {
        if (element != null && element.isJsonPrimitive()) {
            this.set(element.getAsString());
        }
    }
}
