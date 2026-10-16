package dev.oryvex.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.lwjgl.input.Keyboard;

/** LWJGL key code (0 = unbound). */
public final class KeybindSetting extends Setting<Integer> {

    public KeybindSetting(String name, int defaultKey) {
        super(name, defaultKey);
    }

    public int getKey() {
        return this.value;
    }

    public boolean isBound() {
        return this.value > 0;
    }

    public String getKeyName() {
        if (this.value <= 0) {
            return "None";
        }
        String name = Keyboard.getKeyName(this.value);
        return name == null ? "Key " + this.value : name;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(this.value);
    }

    @Override
    public void fromJson(JsonElement element) {
        if (element != null && element.isJsonPrimitive()) {
            this.value = element.getAsInt();
        }
    }
}
