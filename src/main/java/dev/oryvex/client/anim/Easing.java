package dev.oryvex.client.anim;

/**
 * Easing curve mapping linear progress t in [0, 1] to eased progress.
 */
@FunctionalInterface
public interface Easing {

    float apply(float t);

    Easing LINEAR = t -> t;

    Easing EXPO_OUT = t -> t >= 1.0F ? 1.0F : (float) (1.0D - Math.pow(2.0D, -10.0D * t));

    Easing BACK_OUT = t -> {
        float c1 = 1.70158F;
        float c3 = c1 + 1.0F;
        float x = t - 1.0F;
        return 1.0F + c3 * x * x * x + c1 * x * x;
    };

    Easing CUBIC_OUT = t -> {
        float x = 1.0F - t;
        return 1.0F - x * x * x;
    };

    /** CSS "ease". */
    Easing EASE = new CubicBezier(0.25F, 0.1F, 0.25F, 1.0F);

    /** CSS "ease-in-out". */
    Easing EASE_IN_OUT = new CubicBezier(0.42F, 0.0F, 0.58F, 1.0F);

    /** Fast start, long soft landing: used for window openings. */
    Easing SMOOTH = new CubicBezier(0.16F, 1.0F, 0.3F, 1.0F);
}
