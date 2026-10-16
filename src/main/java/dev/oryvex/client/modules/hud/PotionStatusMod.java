package dev.oryvex.client.modules.hud;

import dev.oryvex.client.font.Fonts;
import dev.oryvex.client.hud.HudElement;
import dev.oryvex.client.hud.HudStyle;
import dev.oryvex.client.module.Category;
import dev.oryvex.client.module.Module;
import dev.oryvex.client.render.ColorUtil;
import dev.oryvex.client.setting.BooleanSetting;
import dev.oryvex.client.ui.theme.Theme;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public final class PotionStatusMod extends Module {

    private static final ResourceLocation INVENTORY = new ResourceLocation("textures/gui/container/inventory.png");
    private static final float ROW = 22.0F;

    private final BooleanSetting icons = this.add(new BooleanSetting("Icons", true));
    private final BooleanSetting showLevel = this.add(new BooleanSetting("Show Level", true));
    private final BooleanSetting blink = this.add(new BooleanSetting("Blink When Expiring", true));
    private final BooleanSetting background = this.add(new BooleanSetting("Background", true));

    private List<PotionEffect> previewEffects;

    public PotionStatusMod() {
        super("PotionStatus", "وضعیت معجون", "Active potion effects with live timers.", Category.HUD, true);
        this.addHud(new HudElement(this, "potionstatus", "Potion Status", 0.99F, 0.30F) {
            @Override
            public float getWidth(boolean preview) {
                return PotionStatusMod.this.width(PotionStatusMod.this.effects(preview));
            }

            @Override
            public float getHeight(boolean preview) {
                List<PotionEffect> list = PotionStatusMod.this.effects(preview);
                return list.isEmpty() ? 0.0F : list.size() * ROW + (PotionStatusMod.this.background.isOn() ? 6.0F : 0.0F);
            }

            @Override
            protected void render(float partialTicks, boolean preview) {
                PotionStatusMod.this.renderEffects(PotionStatusMod.this.effects(preview), this.getWidth(preview), this.getHeight(preview));
            }
        });
    }

    private List<PotionEffect> effects(boolean preview) {
        List<PotionEffect> list = new ArrayList<>();
        if (this.mc.thePlayer != null) {
            list.addAll(this.mc.thePlayer.getActivePotionEffects());
        }
        if (list.isEmpty() && preview) {
            if (this.previewEffects == null) {
                this.previewEffects = new ArrayList<>();
                this.previewEffects.add(new PotionEffect(Potion.moveSpeed.id, 20 * 95, 1));
                this.previewEffects.add(new PotionEffect(Potion.damageBoost.id, 20 * 8, 0));
                this.previewEffects.add(new PotionEffect(Potion.fireResistance.id, 20 * 300, 0));
            }
            list.addAll(this.previewEffects);
        }
        return list;
    }

    private String name(PotionEffect effect) {
        Potion potion = Potion.potionTypes[effect.getPotionID()];
        String name = potion == null ? effect.getEffectName() : I18n.format(potion.getName());
        if (this.showLevel.isOn() && effect.getAmplifier() > 0) {
            name += " " + I18n.format("enchantment.level." + (effect.getAmplifier() + 1));
        }
        return name;
    }

    private float width(List<PotionEffect> effects) {
        if (effects.isEmpty()) {
            return 0.0F;
        }
        float max = 40.0F;
        for (PotionEffect effect : effects) {
            max = Math.max(max, Fonts.width(this.name(effect), 8.5F, true));
            max = Math.max(max, Fonts.width(Potion.getDurationString(effect), 7.5F));
        }
        return max + (this.icons.isOn() ? 24.0F : 4.0F) + (this.background.isOn() ? 8.0F : 0.0F);
    }

    private void renderEffects(List<PotionEffect> effects, float w, float h) {
        if (effects.isEmpty()) {
            return;
        }
        boolean bg = this.background.isOn();
        HudStyle.panel(0.0F, 0.0F, w, h, bg);
        float ox = bg ? 4.0F : 0.0F;
        float oy = bg ? 3.0F : 0.0F;
        long time = System.currentTimeMillis();
        for (int i = 0; i < effects.size(); i++) {
            PotionEffect effect = effects.get(i);
            float y = oy + i * ROW;
            float alpha = 1.0F;
            if (this.blink.isOn() && effect.getDuration() < 200 && !effect.getIsPotionDurationMax()) {
                alpha = 0.45F + 0.55F * (float) Math.abs(Math.sin(time / 180.0D));
            }
            float tx = ox;
            Potion potion = Potion.potionTypes[effect.getPotionID()];
            if (this.icons.isOn() && potion != null && potion.hasStatusIcon()) {
                this.mc.getTextureManager().bindTexture(INVENTORY);
                GlStateManager.enableBlend();
                GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
                int index = potion.getStatusIconIndex();
                Gui.drawModalRectWithCustomSizedTexture(Math.round(ox), Math.round(y + 2.0F), index % 8 * 18, 198 + index / 8 * 18, 18, 18, 256.0F, 256.0F);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                tx += 22.0F;
            }
            Fonts.draw(this.name(effect), tx, y + 2.5F, ColorUtil.mulAlpha(Theme.TEXT_PRIMARY, alpha), 8.5F, true);
            Fonts.draw(Potion.getDurationString(effect), tx, y + 12.0F, ColorUtil.mulAlpha(effect.getDuration() < 200 ? Theme.DANGER : Theme.TEXT_SECONDARY, alpha), 7.5F);
        }
    }
}
