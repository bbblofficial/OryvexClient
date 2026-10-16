package dev.oryvex.client.modules.visuals;

import dev.oryvex.client.font.Fonts;
import dev.oryvex.client.hud.HudElement;
import dev.oryvex.client.hud.HudStyle;
import dev.oryvex.client.render.RoundedRenderer;
import dev.oryvex.client.module.Category;
import dev.oryvex.client.module.Module;
import dev.oryvex.client.module.ModuleManager;
import dev.oryvex.client.render.RenderUtil;
import dev.oryvex.client.setting.BooleanSetting;
import dev.oryvex.client.setting.ColorSetting;
import dev.oryvex.client.setting.NumberSetting;
import dev.oryvex.client.ui.theme.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;

import java.util.Collection;

/** Clean, readable scoreboard replacement. */
public final class ScoreboardMod extends Module {
    private final BooleanSetting background = this.add(new BooleanSetting("Background", true));
    private final NumberSetting opacity = this.add(new NumberSetting("Background Opacity", 0.88D, 0.15D, 1.0D, 0.05D));
    private final NumberSetting scale = this.add(new NumberSetting("Scale", 1.0D, 0.75D, 1.5D, 0.05D));
    private final BooleanSetting numbers = this.add(new BooleanSetting("Score Numbers", true));
    private final ColorSetting textColor = this.add(new ColorSetting("Text Color", Theme.TEXT_PRIMARY));
    private final HudElement hudElement;

    public ScoreboardMod() {
        super("Scoreboard", "", "Readable scoreboard with opacity, scale and score-number controls.", Category.VISUALS, true);
        this.hudElement = this.addHud(new HudElement(this, "scoreboard", "Scoreboard", 0.99F, 0.04F) {
            @Override public float getWidth(boolean preview) { return 180.0F; }
            @Override public float getHeight(boolean preview) { return 178.0F; }
            @Override protected void render(float partialTicks, boolean preview) {
                if (!preview) return;
                HudStyle.panel(0.0F, 0.0F, getWidth(true), getHeight(true), true);
                Fonts.drawCenterX("Scoreboard", 90.0F, 7.0F, Theme.TEXT_PRIMARY, 9.0F, true);
                for (int i = 0; i < 12; i++) Fonts.draw("Player " + (i + 1) + "  " + (100 - i), 7.0F, 22.0F + i * 11.0F, Theme.TEXT_SECONDARY, 8.5F, false);
            }
        });
    }

    public static ScoreboardMod active() {
        ScoreboardMod mod = ModuleManager.module(ScoreboardMod.class);
        return mod != null && mod.isEnabled() ? mod : null;
    }

    public void renderObjective(ScoreObjective objective, ScaledResolution sr) {
        Collection<Score> raw = objective.getScoreboard().getSortedScores(objective);
        int shown = 0;
        for (Score score : raw) if (score.getPlayerName() != null && !score.getPlayerName().startsWith("#")) shown++;
        int max = Math.min(shown, 15);
        float s = this.hudElement.getScale();
        int right = Math.round(this.hudElement.getX(sr.getScaledWidth(), false) + 180.0F * s);
        float line = 10.0F * s;
        float width = Fonts.width(objective.getDisplayName(), 9.0F, true);
        int index = 0;
        for (Score score : raw) {
            if (score.getPlayerName().startsWith("#") || index >= max) continue;
            String name = ScorePlayerTeam.formatPlayerName(objective.getScoreboard().getPlayersTeam(score.getPlayerName()), score.getPlayerName());
            String value = this.numbers.isOn() ? " " + score.getScorePoints() : "";
            width = Math.max(width, Fonts.width(name + value, 8.5F, false));
            index++;
        }
        float boxW = width + 14.0F;
        float boxH = (max + 1) * line + 8.0F;
        float x = this.hudElement.getX(sr.getScaledWidth(), false);
        float y = this.hudElement.getY(sr.getScaledHeight(), false);
        if (this.background.isOn()) RenderUtil.rect(x, y, boxW, boxH, ((int)(opacity.getFloat() * 255.0F) << 24) | 0x080B12);
        Fonts.drawCenterX(objective.getDisplayName(), x + boxW / 2.0F, y + 4.0F, this.textColor.getColor() | 0xFF000000, 9.0F * s, true);
        index = 0;
        for (Score score : raw) {
            if (score.getPlayerName().startsWith("#") || index >= max) continue;
            String name = ScorePlayerTeam.formatPlayerName(objective.getScoreboard().getPlayersTeam(score.getPlayerName()), score.getPlayerName());
            String value = this.numbers.isOn() ? " " + score.getScorePoints() : "";
            Fonts.draw(name + value, x + 7.0F, y + 8.0F + (index + 1) * line, this.textColor.getColor() | 0xFF000000, 8.5F * s, false);
            index++;
        }
    }
}
