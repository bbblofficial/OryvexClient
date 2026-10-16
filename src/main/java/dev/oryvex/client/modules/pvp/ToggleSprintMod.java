package dev.oryvex.client.modules.pvp;

import dev.oryvex.client.font.Fonts;
import dev.oryvex.client.hud.HudElement;
import dev.oryvex.client.hud.HudStyle;
import dev.oryvex.client.module.Category;
import dev.oryvex.client.module.Module;
import dev.oryvex.client.setting.BooleanSetting;
import dev.oryvex.client.setting.ColorSetting;
import dev.oryvex.client.setting.ModeSetting;
import dev.oryvex.client.ui.theme.Theme;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public final class ToggleSprintMod extends Module {

    private final BooleanSetting toggleSprint = this.add(new BooleanSetting("Toggle Sprint", true));
    private final BooleanSetting toggleSneak = this.add(new BooleanSetting("Toggle Sneak", false));
    private final BooleanSetting showHud = this.add(new BooleanSetting("Show Label", true));
    private final ModeSetting labelStyle = this.add(new ModeSetting("Label Style", "Brackets", "Brackets", "Plain", "Compact"));
    private final BooleanSetting background = this.add(new BooleanSetting("Background", true));
    private final ColorSetting labelColor = this.add(new ColorSetting("Label Color", Theme.ACCENT_END));

    private boolean sprintToggled = true;
    private boolean sneakToggled;

    public ToggleSprintMod() {
        super("ToggleSprint", "دویدن خودکار", "Sprint and sneak toggles with a customizable status label.", Category.PVP, true);
        this.addHud(new HudElement(this, "togglesprint", "Sprint Status", 0.01F, 0.97F) {
            @Override
            public float getWidth(boolean preview) {
                return Fonts.width(ToggleSprintMod.this.label(preview), 8.5F, true) + (ToggleSprintMod.this.background.isOn() ? 12.0F : 0.0F);
            }

            @Override
            public float getHeight(boolean preview) {
                return ToggleSprintMod.this.background.isOn() ? 16.0F : 10.0F;
            }

            @Override
            protected void render(float partialTicks, boolean preview) {
                String text = ToggleSprintMod.this.label(preview);
                if (text.isEmpty()) {
                    return;
                }
                boolean bg = ToggleSprintMod.this.background.isOn();
                float w = this.getWidth(preview);
                float h = this.getHeight(preview);
                HudStyle.panel(0.0F, 0.0F, w, h, bg);
                Fonts.drawMiddle(text, bg ? 6.0F : 0.0F, h / 2.0F, ToggleSprintMod.this.labelColor.getColor(), 8.5F, true);
            }
        }.showWhen(() -> this.showHud.isOn()));
    }

    @Override
    protected void onDisable() {
        if (this.mc.gameSettings != null) {
            release(this.mc.gameSettings.keyBindSprint);
            release(this.mc.gameSettings.keyBindSneak);
        }
    }

    private static void release(KeyBinding binding) {
        int code = binding.getKeyCode();
        KeyBinding.setKeyBindState(code, code > 0 && Keyboard.isKeyDown(code));
    }

    @Override
    public void onKeyPressed(int keyCode) {
        if (keyCode <= 0) {
            return;
        }
        if (this.toggleSprint.isOn() && keyCode == this.mc.gameSettings.keyBindSprint.getKeyCode()) {
            this.sprintToggled = !this.sprintToggled;
            if (!this.sprintToggled) {
                release(this.mc.gameSettings.keyBindSprint);
            }
        }
        if (this.toggleSneak.isOn() && keyCode == this.mc.gameSettings.keyBindSneak.getKeyCode()) {
            this.sneakToggled = !this.sneakToggled;
            if (!this.sneakToggled) {
                KeyBinding.setKeyBindState(keyCode, false);
            }
        }
    }

    @Override
    public void onTick() {
        EntityPlayerSP player = this.mc.thePlayer;
        if (player == null) {
            return;
        }
        if (this.toggleSprint.isOn() && this.sprintToggled && this.mc.currentScreen == null) {
            KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindSprint.getKeyCode(), true);
        }
        if (this.toggleSneak.isOn() && this.sneakToggled && this.mc.currentScreen == null) {
            KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindSneak.getKeyCode(), true);
        }
        if (!this.toggleSneak.isOn() && this.sneakToggled) {
            this.sneakToggled = false;
            release(this.mc.gameSettings.keyBindSneak);
        }
    }

    String label(boolean preview) {
        EntityPlayerSP player = this.mc.thePlayer;
        String state;
        if (preview && player == null) {
            state = this.format("Sprinting", "Toggled");
        } else if (player == null) {
            return "";
        } else if (player.capabilities.isFlying) {
            state = this.format("Flying", null);
        } else if (player.isRiding()) {
            state = this.format("Riding", null);
        } else if (player.isSneaking()) {
            state = this.format("Sneaking", this.sneakToggled && this.toggleSneak.isOn() ? "Toggled" : "Key Held");
        } else if (player.isSprinting()) {
            boolean toggled = this.sprintToggled && this.toggleSprint.isOn();
            boolean held = Keyboard.isKeyDown(Math.max(1, this.mc.gameSettings.keyBindSprint.getKeyCode()));
            state = this.format("Sprinting", toggled ? "Toggled" : held ? "Key Held" : "Vanilla");
        } else if (this.sprintToggled && this.toggleSprint.isOn()) {
            state = this.format("Sprint", "Ready");
        } else {
            state = preview ? this.format("Sprinting", "Toggled") : "";
        }
        return state;
    }

    private String format(String action, String detail) {
        if (this.labelStyle.is("Plain")) {
            return detail == null ? action : action + " (" + detail + ")";
        }
        if (this.labelStyle.is("Compact")) {
            return detail == null ? action : action + ": " + detail;
        }
        return detail == null ? "[" + action + "]" : "[" + action + " (" + detail + ")]";
    }
}
