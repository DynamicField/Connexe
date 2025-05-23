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
    /// @param k the easing mode (0 for linear, >0 for ease-in, <0 for ease-out)
    /// @return the eased value
    public static double easeExp(double t, double k) {
        if (k == 0) {
            return t;
        }

        t = Math.clamp(t, 0, 1);

        // Here's the intuition for this.
        // ----
        // You surely know that exp(0) = 1.
        // Consider the function exp(k*t). At t=0, we have exp(k*t) = 1.
        // So, considering exp(k*t) - 1, at t=0, we now have 0.
        //
        // Therefore, all we need to do is have exp(k*t) - 1 = 1 when t=1.
        // To do so, we just need to divide by exp(k*1) - 1, which is just exp(k) - 1.
        //
        // Dividing keeps the exp(k*t)-1=0 at t=0,
        // and at t=1 we have (exp(k*1)-1)/(exp(k)-1) which is just 1.
        //
        // As exp(k*t) is a strictly increasing function, we can be sure that the result is in [0, 1].
        return (Math.exp(k * t) - 1) / (Math.exp(k) - 1);
    }
}
