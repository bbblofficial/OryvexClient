package dev.oryvex.client.anim;

/**
 * CSS-style cubic-bezier(x1, y1, x2, y2) timing function with P0 = (0,0) and P3 = (1,1).
 * Solves x(t) = progress with Newton-Raphson and falls back to bisection.
 */
public final class CubicBezier implements Easing {

    private static final int NEWTON_ITERATIONS = 8;
    private static final float NEWTON_EPSILON = 1.0E-6F;
    private static final int BISECTION_ITERATIONS = 24;

    private final float cx;
    private final float bx;
    private final float ax;
    private final float cy;
    private final float by;
    private final float ay;

    public CubicBezier(float x1, float y1, float x2, float y2) {
        if (x1 < 0.0F || x1 > 1.0F || x2 < 0.0F || x2 > 1.0F) {
            throw new IllegalArgumentException("Bezier x control points must be within [0, 1]");
        }
        this.cx = 3.0F * x1;
        this.bx = 3.0F * (x2 - x1) - this.cx;
        this.ax = 1.0F - this.cx - this.bx;
        this.cy = 3.0F * y1;
        this.by = 3.0F * (y2 - y1) - this.cy;
        this.ay = 1.0F - this.cy - this.by;
    }

    private float sampleX(float t) {
        return ((this.ax * t + this.bx) * t + this.cx) * t;
    }

    private float sampleY(float t) {
        return ((this.ay * t + this.by) * t + this.cy) * t;
    }

    private float sampleDerivativeX(float t) {
        return (3.0F * this.ax * t + 2.0F * this.bx) * t + this.cx;
    }

    private float solveT(float x) {
        float t = x;
        for (int i = 0; i < NEWTON_ITERATIONS; i++) {
            float error = this.sampleX(t) - x;
            if (Math.abs(error) < NEWTON_EPSILON) {
                return t;
            }
            float derivative = this.sampleDerivativeX(t);
            if (Math.abs(derivative) < NEWTON_EPSILON) {
                break;
            }
            t -= error / derivative;
        }
        float lo = 0.0F;
        float hi = 1.0F;
        t = x;
        for (int i = 0; i < BISECTION_ITERATIONS; i++) {
            float value = this.sampleX(t);
            if (Math.abs(value - x) < NEWTON_EPSILON) {
                return t;
            }
            if (x > value) {
                lo = t;
            } else {
                hi = t;
            }
            t = (lo + hi) * 0.5F;
        }
        return t;
    }

    @Override
    public float apply(float t) {
        if (t <= 0.0F) {
            return 0.0F;
        }
        if (t >= 1.0F) {
            return 1.0F;
        }
        return this.sampleY(this.solveT(t));
    }
}
