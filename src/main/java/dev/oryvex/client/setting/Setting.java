package dev.oryvex.client.setting;

import com.google.gson.JsonElement;

import java.util.function.BooleanSupplier;

/**
 * Typed, persistable module option.
 */
public abstract class Setting<T> {

    private final String name;
    private final T defaultValue;
    protected T value;
    private BooleanSupplier visibility = () -> true;

    protected Setting(String name, T defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public String getName() {
        return this.name;
    }

    public T get() {
        return this.value;
    }

    public void set(T value) {
        this.value = value;
    }

    public T getDefault() {
        return this.defaultValue;
    }

    public void reset() {
        this.set(this.defaultValue);
    }

    @SuppressWarnings("unchecked")
    public <S extends Setting<T>> S visibleWhen(BooleanSupplier condition) {
        this.visibility = condition;
        return (S) this;
    }

    public boolean isVisible() {
        return this.visibility.getAsBoolean();
    }

    public abstract JsonElement toJson();

    public abstract void fromJson(JsonElement element);
}
