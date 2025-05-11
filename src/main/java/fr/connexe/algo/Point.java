package fr.connexe.algo;

/// Point in a 2D space for mazes.
///
/// The coordinate space is as follows:
///
/// ```
/// 0------------------> x
/// |
/// |
/// |
/// |
/// v y
///```
///
/// @param x The x coordinate (horizontal).
/// @param y The y coordinate (vertical).
public record Point(int x, int y) {
    /// The point at the origin, of coordinates `(0, 0)`.
    public static final Point ZERO = new Point(0, 0);

    /// Add two points together.
    ///
    /// @param other The other point.
    /// @return A new point with `(x + other.x, y + other.y)`
    public Point add(Point other) {
        return new Point(x + other.x, y + other.y);
    }

    /// Add x and y values to this point.
    ///
    /// @param x The x value to add.
    /// @param y The y value to add.
    /// @return A new point with `(x + x, y + y)`.
    public Point add(int x, int y) {
        return new Point(this.x + x, this.y + y);
    }

    /// Subtract two points from each other.
    ///
    /// @param other The other point.
    /// @return A new point with `(x - other.x, y - other.y)`
    public Point subtract(Point other) {
        return new Point(x - other.x, y - other.y);
    }

    /// Subtract x and y values from this point.
    ///
    /// @param x The x value to subtract.
    /// @param y The y value to subtract.
    /// @return A new point with `(x - x, y - y)`.
    public Point subtract(int x, int y) {
        return new Point(this.x - x, this.y - y);
    }
}
