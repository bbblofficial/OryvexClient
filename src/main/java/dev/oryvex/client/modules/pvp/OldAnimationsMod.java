package dev.oryvex.client.modules.pvp;

import dev.oryvex.client.module.Category;
import dev.oryvex.client.module.Module;
import dev.oryvex.client.module.ModuleManager;
import dev.oryvex.client.setting.BooleanSetting;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.MovingObjectPosition;

/**
 * 1.7.10-style first-person animations. Hooks live in MixinItemRenderer; everything here
 * is purely visual (no packets are sent).
 */
public final class OldAnimationsMod extends Module {

    private final BooleanSetting blockHit = this.add(new BooleanSetting("Block Hitting", true));
    private final BooleanSetting eatHit = this.add(new BooleanSetting("Eat / Drink Swing", true));
    private final BooleanSetting bowHit = this.add(new BooleanSetting("Bow Swing", true));
    private final BooleanSetting oldRod = this.add(new BooleanSetting("1.7 Rod Position", true));
    private final BooleanSetting visualSwing = this.add(new BooleanSetting("Swing While Using", true));

    public OldAnimationsMod() {
        super("OldAnimations", "انیمیشن کلاسیک", "1.7.10 block-hitting, bow, eating and rod animations.", Category.PVP, true);
    }

    private static OldAnimationsMod active() {
        OldAnimationsMod mod = ModuleManager.module(OldAnimationsMod.class);
        return mod != null && mod.isEnabled() ? mod : null;
    }

    public static boolean blockHit() {
        OldAnimationsMod mod = active();
        return mod != null && mod.blockHit.isOn();
    }

    public static boolean eatHit() {
        OldAnimationsMod mod = active();
        return mod != null && mod.eatHit.isOn();
    }

    public static boolean bowHit() {
        OldAnimationsMod mod = active();
        return mod != null && mod.bowHit.isOn();
    }

    public static boolean oldRod() {
        OldAnimationsMod mod = active();
        return mod != null && mod.oldRod.isOn();
    }

    @Override
    public void onTick() {
        if (!this.visualSwing.isOn() || this.mc.currentScreen != null) {
            return;
        }
        EntityPlayerSP player = this.mc.thePlayer;
        MovingObjectPosition target = this.mc.objectMouseOver;
        if (player != null && player.isUsingItem() && this.mc.gameSettings.keyBindAttack.isKeyDown()
            && target != null && target.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            if (!player.isSwingInProgress || player.swingProgressInt >= 3 || player.swingProgressInt < 0) {
                player.swingProgressInt = -1;
                player.isSwingInProgress = true;
            }
        }
    }
}
