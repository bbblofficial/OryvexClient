package dev.oryvex.client.modules.hud;

import dev.oryvex.client.font.Fonts;
import dev.oryvex.client.hud.HudElement;
import dev.oryvex.client.hud.HudStyle;
import dev.oryvex.client.module.Category;
import dev.oryvex.client.module.Module;
import dev.oryvex.client.render.ColorUtil;
import dev.oryvex.client.render.RoundedRenderer;
import dev.oryvex.client.setting.BooleanSetting;
import dev.oryvex.client.setting.ModeSetting;
import dev.oryvex.client.ui.theme.Theme;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class ArmorStatusMod extends Module {

    private static final float ROW = 18.0F;
    private static final float BAR_WIDTH = 34.0F;

    private final ModeSetting layout = this.add(new ModeSetting("Layout", "Vertical", "Vertical", "Horizontal"));
    private final ModeSetting durability = this.add(new ModeSetting("Durability", "Percent", "Percent", "Value", "Bar Only"));
    private final BooleanSetting heldItem = this.add(new BooleanSetting("Held Item", true));
    private final BooleanSetting background = this.add(new BooleanSetting("Background", true));

    private ItemStack[] previewStacks;

    public ArmorStatusMod() {
        super("ArmorStatus", "وضعیت زره", "Armor and held item with clean durability bars.", Category.HUD, true);
        this.addHud(new HudElement(this, "armorstatus", "Armor Status", 0.99F, 0.70F) {
            @Override
            public float getWidth(boolean preview) {
                return ArmorStatusMod.this.width(ArmorStatusMod.this.stacks(preview));
            }

            @Override
            public float getHeight(boolean preview) {
                return ArmorStatusMod.this.height(ArmorStatusMod.this.stacks(preview));
            }

            @Override
            protected void render(float partialTicks, boolean preview) {
                ArmorStatusMod.this.renderStacks(ArmorStatusMod.this.stacks(preview), this.getWidth(preview), this.getHeight(preview));
            }
        });
    }

    private List<ItemStack> stacks(boolean preview) {
        List<ItemStack> list = new ArrayList<>();
        if (this.mc.thePlayer != null) {
            for (int i = 3; i >= 0; i--) {
                ItemStack stack = this.mc.thePlayer.inventory.armorInventory[i];
                if (stack != null) {
                    list.add(stack);
                }
            }
            if (this.heldItem.isOn() && this.mc.thePlayer.getHeldItem() != null) {
                list.add(this.mc.thePlayer.getHeldItem());
            }
        }
        if (list.isEmpty() && preview) {
            if (this.previewStacks == null) {
                this.previewStacks = new ItemStack[]{
                    new ItemStack(Items.diamond_helmet), new ItemStack(Items.diamond_chestplate),
                    new ItemStack(Items.diamond_leggings), new ItemStack(Items.diamond_boots),
                    new ItemStack(Items.diamond_sword)
                };
                this.previewStacks[1].setItemDamage(180);
                this.previewStacks[3].setItemDamage(300);
            }
            for (ItemStack stack : this.previewStacks) {
                list.add(stack);
            }
        }
        return list;
    }

    private String text(ItemStack stack) {
        if (stack.isItemStackDamageable()) {
            int max = stack.getMaxDamage();
            int left = max - stack.getItemDamage();
            if (this.durability.is("Bar Only")) {
                return "";
            }
            if (this.durability.is("Value")) {
                return String.valueOf(left);
            }
            return Math.round(left * 100.0F / max) + "%";
        }
        if (stack.stackSize > 1) {
            return "x" + stack.stackSize;
        }
        return "";
    }

    private boolean vertical() {
        return this.layout.is("Vertical");
    }

    private float width(List<ItemStack> stacks) {
        if (stacks.isEmpty()) {
            return 0.0F;
        }
        if (!this.vertical()) {
            return stacks.size() * 38.0F - 2.0F + (this.background.isOn() ? 8.0F : 0.0F);
        }
        float textWidth = BAR_WIDTH;
        for (ItemStack stack : stacks) {
            textWidth = Math.max(textWidth, Fonts.width(this.text(stack), 8.0F, true));
        }
        return 20.0F + textWidth + (this.background.isOn() ? 10.0F : 0.0F);
    }

    private float height(List<ItemStack> stacks) {
        if (stacks.isEmpty()) {
            return 0.0F;
        }
        float pad = this.background.isOn() ? 8.0F : 0.0F;
        return this.vertical() ? stacks.size() * ROW + pad : 34.0F + pad;
    }

    private void renderStacks(List<ItemStack> stacks, float w, float h) {
        if (stacks.isEmpty()) {
            return;
        }
        boolean bg = this.background.isOn();
        HudStyle.panel(0.0F, 0.0F, w, h, bg);
        float ox = bg ? 4.0F : 0.0F;
        float oy = bg ? 4.0F : 0.0F;
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            float ix;
            float iy;
            if (this.vertical()) {
                ix = ox + 1.0F;
                iy = oy + i * ROW + 1.0F;
            } else {
                ix = ox + i * 38.0F + 9.0F;
                iy = oy;
            }
            this.renderItem(stack, Math.round(ix), Math.round(iy));
            String label = this.text(stack);
            float ratio = stack.isItemStackDamageable() ? 1.0F - stack.getItemDamage() / (float) stack.getMaxDamage() : -1.0F;
            int barColor = ratio < 0.25F ? Theme.DANGER : ratio < 0.5F ? Theme.FAVORITE : Theme.ACCENT_END;
            if (this.vertical()) {
                float tx = ox + 20.0F;
                if (!label.isEmpty()) {
                    Fonts.draw(label, tx, iy + 1.0F, ratio >= 0.0F ? ColorUtil.lerp(Theme.DANGER, Theme.TEXT_PRIMARY, ratio * 1.6F) : Theme.TEXT_PRIMARY, 8.0F, true);
                }
                if (ratio >= 0.0F) {
                    this.bar(tx, iy + 11.5F, BAR_WIDTH, ratio, barColor);
                }
            } else {
                float cx = ox + i * 38.0F + 17.0F;
                if (!label.isEmpty()) {
                    Fonts.drawCenterX(label, cx, iy + 18.0F, Theme.TEXT_PRIMARY, 7.5F, true);
                }
                if (ratio >= 0.0F) {
                    this.bar(cx - 14.0F, iy + 28.0F, 28.0F, ratio, barColor);
                }
            }
        }
    }

    private void bar(float x, float y, float w, float ratio, int color) {
        RoundedRenderer.rect(x, y, w, 2.5F, 1.25F, Theme.BORDER);
        RoundedRenderer.gradient(x, y, Math.max(2.5F, w * ratio), 2.5F, 1.25F, Theme.ACCENT_START, color, false);
    }

    private void renderItem(ItemStack stack, int x, int y) {
        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        RenderHelper.enableGUIStandardItemLighting();
        this.mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
