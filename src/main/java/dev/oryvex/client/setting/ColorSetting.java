package dev.oryvex.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/** ARGB color option. */
public final class ColorSetting extends Setting<Integer> {

    public ColorSetting(String name, int defaultArgb) {
        super(name, defaultArgb);
    }

    public int getColor() {
        return this.value;
    }

    public int getRgb() {
        return this.value & 0xFFFFFF;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(String.format("#%08X", this.value));
    }

    @Override
    public void fromJson(JsonElement element) {
        if (element == null || !element.isJsonPrimitive()) {
            return;
        }
        String raw = element.getAsString().trim();
        if (raw.startsWith("#")) {
            raw = raw.substring(1);
        }
        try {
            long parsed = Long.parseLong(raw, 16);
            this.value = raw.length() <= 6 ? (int) (0xFF000000L | parsed) : (int) parsed;
        } catch (NumberFormatException ignored) {
            this.value = this.getDefault();
        }
    }
}
