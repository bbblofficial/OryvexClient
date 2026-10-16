package dev.oryvex.client.anim;

/**
 * Delta-time driven tween between two values. Safe to read multiple times per frame:
 * time only advances once per {@link AnimationClock} frame.
 */
public final class Animation {

    private final Easing easing;
    private final float durationMs;
    private float from;
    private float to;
    private float value;
    private float elapsed;
    private long lastFrame = -1L;

    public Animation(Easing easing, float durationMs, float initial) {
        this.easing = easing;
        this.durationMs = Math.max(1.0F, durationMs);
        this.from = initial;
        this.to = initial;
        this.value = initial;
        this.elapsed = this.durationMs;
    }

    public Animation animateTo(float target) {
        if (Float.compare(target, this.to) != 0) {
            this.update();
            this.from = this.value;
            this.to = target;
            this.elapsed = 0.0F;
        }
        return this;
    }

    /** Jumps immediately to a value without animating. */
    public Animation set(float v) {
        this.from = v;
        this.to = v;
        this.value = v;
        this.elapsed = this.durationMs;
        this.lastFrame = AnimationClock.frame();
        return this;
    }

    public float get() {
        this.update();
        return this.value;
    }

    public float getTarget() {
        return this.to;
    }

    public boolean isDone() {
        return this.elapsed >= this.durationMs;
    }

    private void update() {
        long frame = AnimationClock.frame();
        if (frame == this.lastFrame) {
            return;
        }
        this.lastFrame = frame;
        if (this.elapsed < this.durationMs) {
            this.elapsed = Math.min(this.durationMs, this.elapsed + AnimationClock.deltaMs());
            float t = this.elapsed / this.durationMs;
            this.value = this.from + (this.to - this.from) * this.easing.apply(t);
        } else {
            this.value = this.to;
        }
    }
}
