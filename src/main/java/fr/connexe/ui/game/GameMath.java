package fr.connexe.ui.game;

public final class GameMath {
    private GameMath() {}

    public static double lerp(double a, double b, double t) {
        return a * (1 - t) + b * t;
    }

    public static Vector2 lerp(Vector2 a, Vector2 b, double t) {
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
    /// @param t the input value, clamped to `[0, 1]``
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
