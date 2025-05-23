package fr.connexe.ui.game;

import fr.connexe.algo.Point;

/// A vector in 2D space, with double-precision floating point numbers.
public record Vector2(double x, double y) {
    /// Creates a new vector that is the sum of this vector and the given vector.
    ///
    /// @param other The vector to add
    /// @return A new vector representing the sum
    public Vector2 add(Vector2 other) {
        return new Vector2(x + other.x, y + other.y);
    }

    /// Creates a new vector that is the difference of this vector and the given vector.
    ///
    /// @param other The vector to subtract
    /// @return A new vector representing the difference
    public Vector2 subtract(Vector2 other) {
        return new Vector2(x - other.x, y - other.y);
    }

    /// Creates a new vector by multiplying this vector by a scalar value.
    ///
    /// @param scalar The scalar value to multiply by
    /// @return A new scaled vector
    public Vector2 multiply(double scalar) {
        return new Vector2(x * scalar, y * scalar);
    }

    /// Creates a new vector by dividing this vector by a scalar value.
    ///
    /// @param scalar The scalar value to divide by
    /// @return A new scaled vector
    /// @throws IllegalArgumentException if scalar is zero
    public Vector2 divide(double scalar) {
        if (scalar == 0) {
            throw new IllegalArgumentException("Cannot divide by zero");
        }
        return new Vector2(x / scalar, y / scalar);
    }

    /// Calculates the magnitude (length) of this vector.
    ///
    /// @return The magnitude of the vector
    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    /// Creates a new vector from a Point object.
    ///
    /// @param p The Point object to convert
    /// @return A new vector representing the point
    public static Vector2 fromPoint(Point p) {
        return new Vector2(p.x(), p.y());
    }

    /// Converts this vector to a Point object. This method truncates the decimal part of the coordinates.
    ///
    /// @return A new Point object representing this vector
    public Point toPoint() {
        return new Point((int) x, (int) y);
    }
}
