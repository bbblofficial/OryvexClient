package dev.oryvex.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

/**
 * Display-only combat statistics: reach of the last attack and current combo (hits landed
 * without taking damage). Nothing here alters gameplay.
 */
public final class CombatTracker {

    private static final long COMBO_TIMEOUT_MS = 3000L;
    private static final long CONFIRM_WINDOW_MS = 600L;

    private static double lastReach = -1.0D;
    private static long lastReachTime;
    private static int combo;
    private static long lastHitTime;
    private static int lastTargetId = -1;
    private static EntityLivingBase pendingTarget;
    private static int pendingHurtTime;
    private static long pendingTime;
    private static int previousHurtTime;

    private CombatTracker() {
    }

    public static void onAttack(Entity target) {
        Minecraft mc = Minecraft.getMinecraft();
        MovingObjectPosition hit = mc.objectMouseOver;
        if (mc.thePlayer != null && hit != null && hit.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY
            && hit.entityHit == target && hit.hitVec != null) {
            Vec3 eyes = mc.thePlayer.getPositionEyes(1.0F);
            lastReach = eyes.distanceTo(hit.hitVec);
            lastReachTime = System.currentTimeMillis();
        }
        if (target instanceof EntityLivingBase) {
            pendingTarget = (EntityLivingBase) target;
            pendingHurtTime = pendingTarget.hurtTime;
            pendingTime = System.currentTimeMillis();
        }
    }

    public static void tick() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) {
            combo = 0;
            pendingTarget = null;
            return;
        }
        long now = System.currentTimeMillis();
        int hurt = mc.thePlayer.hurtTime;
        if (hurt > previousHurtTime) {
            combo = 0;
        }
        previousHurtTime = hurt;

        if (pendingTarget != null) {
            if (pendingTarget.hurtTime > pendingHurtTime || (pendingHurtTime == 0 && pendingTarget.hurtTime > 0)) {
                int id = pendingTarget.getEntityId();
                if (id == lastTargetId && now - lastHitTime < COMBO_TIMEOUT_MS) {
                    combo++;
                } else {
                    combo = 1;
                }
                lastTargetId = id;
                lastHitTime = now;
                pendingTarget = null;
            } else if (now - pendingTime > CONFIRM_WINDOW_MS || pendingTarget.isDead) {
                pendingTarget = null;
            }
        }
        if (combo > 0 && now - lastHitTime > COMBO_TIMEOUT_MS) {
            combo = 0;
        }
    }

    public static int getCombo() {
        return combo;
    }

    /** Last reach in blocks, or -1 if older than {@code maxAgeMs}. */
    public static double getReach(long maxAgeMs) {
        if (lastReach < 0.0D || System.currentTimeMillis() - lastReachTime > maxAgeMs) {
            return -1.0D;
        }
        return lastReach;
    }
}
