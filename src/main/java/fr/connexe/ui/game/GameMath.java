package fr.connexe.ui.game;

/// Various mathematical utilities for game development: mainly interpolation and easing functions.
public final class GameMath {
    private GameMath() {}

    /// Linear interpolation between two values.
    ///
    /// @param a the start value
    /// @param b the end value
    /// @param t the interpolation factor, clamped to `[0, 1]`
    /// @return the interpolated value
    public static double lerp(double a, double b, double t) {
        t = Math.clamp(t, 0, 1);
        return a * (1 - t) + b * t;
    }

    /// Linear interpolation between two vectors.
    ///
    /// @param a the start vector
    /// @param b the end vector
    /// @param t the interpolation factor, clamped to `[0, 1]`
    /// @return the interpolated vector
    public static Vector2 lerp(Vector2 a, Vector2 b, double t) {
        t = Math.clamp(t, 0, 1);
        return new Vector2(lerp(a.x(), b.x(), t), lerp(a.y(), b.y(), t));
    }

    /// Calculates an eased value based on an exponential function.
    /// Accepts t in `[0, 1]`.
    ///
    /// - k = 0: linear
    /// - k > 0: ease-in
    /// - k < 0: ease-out
    ///
    /// Adapted from
    /// [CardLab's web client](https://github.com/ChuechTeam/CardLab/blob/main/src/web/Client/card-lab/src/duel/util.ts)
    ///
    /// @param t the input value, clamped to `[0, 1]`
    /// @param k the exponent factor
    /// @return the eased value
    public static double easeExp(double t, double k) {
        if (k == 0) {
            return t;
        }

        t = Math.min(1, Math.max(0, t));
        return (1 - Math.exp(k * t)) / (1 - Math.exp(k));
    }
}
