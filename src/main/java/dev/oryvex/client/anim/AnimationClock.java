package dev.oryvex.client.anim;

/**
 * Global frame clock. Ticked once per rendered frame; every animation advances by the
 * same delta so motion stays frame-rate independent.
 */
public final class AnimationClock {

    private static final float MAX_DELTA_MS = 50.0F;

    private static long lastNanos = System.nanoTime();
    private static float deltaMs = 16.0F;
    private static long frame;

    private AnimationClock() {
    }

    public static void tick() {
        long now = System.nanoTime();
        deltaMs = Math.min(MAX_DELTA_MS, Math.max(1.0F, (now - lastNanos) / 1_000_000.0F));
        lastNanos = now;
        frame++;
    }

    public static float deltaMs() {
        return deltaMs;
    }

    public static long frame() {
        return frame;
    }
}
