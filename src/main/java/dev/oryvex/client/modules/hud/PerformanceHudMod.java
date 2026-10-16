package dev.oryvex.client.modules.hud;

import dev.oryvex.client.font.Fonts;
import dev.oryvex.client.hud.HudElement;
import dev.oryvex.client.hud.HudStyle;
import dev.oryvex.client.module.Category;
import dev.oryvex.client.module.Module;
import dev.oryvex.client.setting.BooleanSetting;
import dev.oryvex.client.setting.ColorSetting;
import dev.oryvex.client.ui.theme.Theme;
import dev.oryvex.client.util.CombatTracker;
import dev.oryvex.client.util.CpsTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.EnumFacing;

import java.util.function.Supplier;

public final class PerformanceHudMod extends Module {

    private static final float TEXT = 8.5F;

    private final BooleanSetting fps = this.add(new BooleanSetting("FPS", true));
    private final BooleanSetting ping = this.add(new BooleanSetting("Ping", true));
    private final BooleanSetting cps = this.add(new BooleanSetting("CPS", true));
    private final BooleanSetting reach = this.add(new BooleanSetting("Reach Display", true));
    private final BooleanSetting combo = this.add(new BooleanSetting("Combo Counter", true));
    private final BooleanSetting coords = this.add(new BooleanSetting("Coordinates", true));
    private final BooleanSetting background = this.add(new BooleanSetting("Background", true));
    private final ColorSetting labelColor = this.add(new ColorSetting("Label Color", Theme.ACCENT_END));
    private final ColorSetting valueColor = this.add(new ColorSetting("Value Color", Theme.TEXT_PRIMARY));

    public PerformanceHudMod() {
        super("PerformanceHUD", "نمایشگر عملکرد", "Draggable FPS, ping, CPS, reach, combo and coordinate widgets.", Category.HUD, true);
        this.addHud(new StatWidget("fps", "FPS", 0.005F, 0.01F, "FPS", () -> String.valueOf(Minecraft.getDebugFPS())).showWhen(() -> this.fps.isOn()));
        this.addHud(new StatWidget("ping", "Ping", 0.005F, 0.065F, "PING", this::pingText).showWhen(() -> this.ping.isOn()));
        this.addHud(new StatWidget("cps", "CPS", 0.005F, 0.12F, "CPS", () -> CpsTracker.left() + " | " + CpsTracker.right()).showWhen(() -> this.cps.isOn()));
        this.addHud(new StatWidget("reach", "Reach", 0.005F, 0.175F, "REACH", this::reachText).showWhen(() -> this.reach.isOn()));
        this.addHud(new StatWidget("combo", "Combo", 0.005F, 0.23F, "COMBO", () -> String.valueOf(CombatTracker.getCombo())).showWhen(() -> this.combo.isOn()));
        this.addHud(new CoordinatesWidget().showWhen(() -> this.coords.isOn()));
    }

    private String pingText() {
        if (this.mc.thePlayer == null || this.mc.getNetHandler() == null) {
            return "--";
        }
        if (this.mc.isSingleplayer()) {
            return "0 ms";
        }
        NetworkPlayerInfo info = this.mc.getNetHandler().getPlayerInfo(this.mc.thePlayer.getUniqueID());
        return info == null ? "--" : info.getResponseTime() + " ms";
    }

    private String reachText() {
        double value = CombatTracker.getReach(2500L);
        return value < 0.0D ? "--" : String.format("%.2f", value);
    }

    private final class StatWidget extends HudElement {

        private final String label;
        private final Supplier<String> value;

        StatWidget(String id, String name, float x, float y, String label, Supplier<String> value) {
            super(PerformanceHudMod.this, id, name, x, y);
            this.label = label;
            this.value = value;
        }

        private String valueText(boolean preview) {
            String v = this.value.get();
            if (preview && PerformanceHudMod.this.mc.thePlayer == null) {
                return "144";
            }
            return v;
        }

        @Override
        public float getWidth(boolean preview) {
            float w = Fonts.width(this.label, TEXT, true) + 4.0F + Fonts.width(this.valueText(preview), TEXT, false);
            return Math.max(48.0F, w + (PerformanceHudMod.this.background.isOn() ? 14.0F : 0.0F));
        }

        @Override
        public float getHeight(boolean preview) {
            return PerformanceHudMod.this.background.isOn() ? 16.0F : 10.0F;
        }

        @Override
        protected void render(float partialTicks, boolean preview) {
            boolean bg = PerformanceHudMod.this.background.isOn();
            float w = this.getWidth(preview);
            float h = this.getHeight(preview);
            HudStyle.panel(0.0F, 0.0F, w, h, bg);
            if (bg) {
                HudStyle.accentBar(0.0F, 3.0F, h - 6.0F);
            }
            float x = bg ? 7.0F : 0.0F;
            x += Fonts.drawMiddle(this.label, x, h / 2.0F, PerformanceHudMod.this.labelColor.getColor(), TEXT, true) + 4.0F;
            Fonts.drawMiddle(this.valueText(preview), x, h / 2.0F, PerformanceHudMod.this.valueColor.getColor(), TEXT, false);
        }
    }

    private final class CoordinatesWidget extends HudElement {

        CoordinatesWidget() {
            super(PerformanceHudMod.this, "coordinates", "Coordinates", 0.005F, 0.30F);
        }

        private String[] lines(boolean preview) {
            if (PerformanceHudMod.this.mc.thePlayer == null) {
                return preview ? new String[]{"X  128.5", "Y  64.0", "Z  -342.7", "North (-Z)"} : new String[0];
            }
            double x = PerformanceHudMod.this.mc.thePlayer.posX;
            double y = PerformanceHudMod.this.mc.thePlayer.getEntityBoundingBox().minY;
            double z = PerformanceHudMod.this.mc.thePlayer.posZ;
            EnumFacing facing = PerformanceHudMod.this.mc.thePlayer.getHorizontalFacing();
            String axis;
            switch (facing) {
                case NORTH:
                    axis = "North (-Z)";
                    break;
                case SOUTH:
                    axis = "South (+Z)";
                    break;
                case WEST:
                    axis = "West (-X)";
                    break;
                default:
                    axis = "East (+X)";
                    break;
            }
            return new String[]{
                String.format("X  %.1f", x), String.format("Y  %.1f", y), String.format("Z  %.1f", z), axis
            };
        }

        @Override
        public float getWidth(boolean preview) {
            float max = 60.0F;
            for (String line : this.lines(preview)) {
                max = Math.max(max, Fonts.width(line, TEXT, false));
            }
            return max + (PerformanceHudMod.this.background.isOn() ? 14.0F : 0.0F);
        }

        @Override
        public float getHeight(boolean preview) {
            return this.lines(preview).length * 10.0F + (PerformanceHudMod.this.background.isOn() ? 8.0F : 0.0F);
        }

        @Override
        protected void render(float partialTicks, boolean preview) {
            String[] lines = this.lines(preview);
            if (lines.length == 0) {
                return;
            }
            boolean bg = PerformanceHudMod.this.background.isOn();
            float w = this.getWidth(preview);
            float h = this.getHeight(preview);
            HudStyle.panel(0.0F, 0.0F, w, h, bg);
            if (bg) {
                HudStyle.accentBar(0.0F, 4.0F, h - 8.0F);
            }
            float x = bg ? 7.0F : 0.0F;
            float y = bg ? 4.0F : 0.0F;
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (i < 3) {
                    float lw = Fonts.draw(line.substring(0, 1), x, y + i * 10.0F + 0.5F, PerformanceHudMod.this.labelColor.getColor(), TEXT, true);
                    Fonts.draw(line.substring(1), x + lw, y + i * 10.0F + 0.5F, PerformanceHudMod.this.valueColor.getColor(), TEXT, false);
                } else {
                    Fonts.draw(line, x, y + i * 10.0F + 0.5F, Theme.TEXT_SECONDARY, TEXT, false);
                }
            }
        }
    }
}
