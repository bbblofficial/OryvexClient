package dev.oryvex.client.anim;

/**
 * Critically-damped style follower used for smooth scrolling: approaches its target
 * exponentially using the frame delta.
 */
public final class SmoothValue {

    private final float speed;
    private float value;
    private float target;
    private long lastFrame = -1L;

    public SmoothValue(float speed) {
        this.speed = speed;
    }

    public void setTarget(float target) {
        this.target = target;
    }

    public void addTarget(float delta) {
        this.target += delta;
    }

    public float getTarget() {
        return this.target;
    }

    public void clamp(float min, float max) {
        float upper = Math.max(min, max);
        this.target = Math.max(min, Math.min(upper, this.target));
        this.value = Math.max(min - 40.0F, Math.min(upper + 40.0F, this.value));
    }

    public void snap(float v) {
        this.value = v;
        this.target = v;
    }

    public float get() {
        long frame = AnimationClock.frame();
        if (frame != this.lastFrame) {
            this.lastFrame = frame;
            float factor = 1.0F - (float) Math.exp(-AnimationClock.deltaMs() / 1000.0F * this.speed);
            this.value += (this.target - this.value) * factor;
            if (Math.abs(this.target - this.value) < 0.01F) {
                this.value = this.target;
            }
        }
        return this.value;
    }
}
