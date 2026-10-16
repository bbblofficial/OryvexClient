package dev.oryvex.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class NumberSetting extends Setting<Double> {

    private final double min;
    private final double max;
    private final double step;
    private final String suffix;

    public NumberSetting(String name, double defaultValue, double min, double max, double step) {
        this(name, defaultValue, min, max, step, "");
    }

    public NumberSetting(String name, double defaultValue, double min, double max, double step, String suffix) {
        super(name, defaultValue);
        this.min = min;
        this.max = max;
        this.step = step;
        this.suffix = suffix;
    }

    @Override
    public void set(Double v) {
        double clamped = Math.max(this.min, Math.min(this.max, v));
        if (this.step > 0.0D) {
            clamped = Math.round(clamped / this.step) * this.step;
        }
        this.value = Math.max(this.min, Math.min(this.max, clamped));
    }

    public float getFloat() {
        return this.value.floatValue();
    }

    public int getInt() {
        return (int) Math.round(this.value);
    }

    public double getMin() {
        return this.min;
    }

    public double getMax() {
        return this.max;
    }

    public double getStep() {
        return this.step;
    }

    public double getRatio() {
        return (this.value - this.min) / (this.max - this.min);
    }

    public void setRatio(double ratio) {
        this.set(this.min + Math.max(0.0D, Math.min(1.0D, ratio)) * (this.max - this.min));
    }

    public String format() {
        String text = this.step >= 1.0D ? String.valueOf(Math.round(this.value))
            : this.step >= 0.1D ? String.format("%.1f", this.value) : String.format("%.2f", this.value);
        return text + this.suffix;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(this.value);
    }

    @Override
    public void fromJson(JsonElement element) {
        if (element != null && element.isJsonPrimitive()) {
            this.set(element.getAsDouble());
        }
    }
}
