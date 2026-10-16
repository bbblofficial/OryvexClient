package dev.oryvex.client.ui.clickgui;

import dev.oryvex.client.setting.Setting;

/**
 * One row in the settings drawer.
 */
public abstract class SettingComponent<S extends Setting<?>> {

    protected final S setting;

    protected SettingComponent(S setting) {
        this.setting = setting;
    }

    public S getSetting() {
        return this.setting;
    }

    public abstract float getHeight();

    public abstract void render(float x, float y, float w, int mouseX, int mouseY, float alpha);

    public boolean mouseClicked(float x, float y, float w, int mouseX, int mouseY, int button) {
        return false;
    }

    public void mouseDragged(float x, float y, float w, int mouseX, int mouseY) {
    }

    public void mouseReleased() {
    }

    /** @return true when the key was consumed */
    public boolean keyTyped(char typed, int keyCode) {
        return false;
    }

    public boolean isCapturingKeys() {
        return false;
    }
}
