package dev.oryvex.client.modules.visuals;

import dev.oryvex.client.font.Fonts;
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
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collection;

/** Clean, readable scoreboard replacement. */
public final class ScoreboardMod extends Module {
    private final BooleanSetting background = this.add(new BooleanSetting("Background", true));
    private final NumberSetting opacity = this.add(new NumberSetting("Background Opacity", 0.88D, 0.15D, 1.0D, 0.05D));
    private final NumberSetting scale = this.add(new NumberSetting("Scale", 1.0D, 0.75D, 1.5D, 0.05D));
    private final BooleanSetting numbers = this.add(new BooleanSetting("Score Numbers", true));
    private final ColorSetting textColor = this.add(new ColorSetting("Text Color", Theme.TEXT_PRIMARY));

    public ScoreboardMod() {
        super("Scoreboard", "", "Readable scoreboard with opacity, scale and score-number controls.", Category.VISUALS, true);
    }

    public static ScoreboardMod active() {
        ScoreboardMod mod = ModuleManager.module(ScoreboardMod.class);
        return mod != null && mod.isEnabled() ? mod : null;
    }

    public void onOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.type != RenderGameOverlayEvent.ElementType.SCOREBOARD || this.mc.theWorld == null) return;
        Scoreboard board = this.mc.theWorld.getScoreboard();
        ScoreObjective objective = board.getObjectiveInDisplaySlot(1);
        if (objective == null) return;
        event.setCanceled(true);
        this.render(objective, event.resolution);
    }

    private void render(ScoreObjective objective, ScaledResolution sr) {
        Collection<Score> raw = objective.getScoreboard().getSortedScores(objective);
        int shown = 0;
        for (Score score : raw) if (score.getPlayerName() != null && !score.getPlayerName().startsWith("#")) shown++;
        int max = Math.min(shown, 15);
        float s = this.scale.getFloat();
        int right = sr.getScaledWidth() - 8;
        float line = 10.0F * s;
        float width = Fonts.width(objective.getDisplayName(), 9.0F, true);
        int index = 0;
        for (Score score : raw) {
            if (score.getPlayerName().startsWith("#") || index >= max) continue;
            String name = ScorePlayerTeam.formatPlayerName(score.getScoreboard().getPlayersTeam(score.getPlayerName()), score.getPlayerName());
            String value = this.numbers.isOn() ? " " + score.getScorePoints() : "";
            width = Math.max(width, Fonts.width(name + value, 8.5F, false));
            index++;
        }
        float boxW = width + 14.0F;
        float boxH = (max + 1) * line + 8.0F;
        float x = right - boxW;
        float y = 6.0F;
        if (this.background.isOn()) RenderUtil.rect(x, y, boxW, boxH, ((int)(opacity.getFloat() * 255.0F) << 24) | 0x080B12);
        Fonts.drawCenterX(objective.getDisplayName(), x + boxW / 2.0F, y + 4.0F, this.textColor.getColor() | 0xFF000000, 9.0F * s, true);
        index = 0;
        for (Score score : raw) {
            if (score.getPlayerName().startsWith("#") || index >= max) continue;
            String name = ScorePlayerTeam.formatPlayerName(score.getScoreboard().getPlayersTeam(score.getPlayerName()), score.getPlayerName());
            String value = this.numbers.isOn() ? " " + score.getScorePoints() : "";
            Fonts.draw(name + value, x + 7.0F, y + 8.0F + (index + 1) * line, this.textColor.getColor() | 0xFF000000, 8.5F * s, false);
            index++;
        }
    }
}
