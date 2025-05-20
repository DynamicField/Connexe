package fr.connexe.ui.game;

import fr.connexe.algo.Point;

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
        return (double) Math.sqrt(x * x + y * y);
    }

    /// Creates a normalized version of this vector (with magnitude of 1).
    ///
    /// @return A new normalized vector
    /// @throws ArithmeticException if the vector's magnitude is zero
    public Vector2 normalize() {
        double mag = magnitude();
        if (mag == 0) {
            throw new ArithmeticException("Cannot normalize a zero vector");
        }
        return divide(mag);
    }

    /// Calculates the dot product of this vector and another vector.
    ///
    /// @param other The other vector
    /// @return The dot product
    public double dot(Vector2 other) {
        return x * other.x + y * other.y;
    }

    /// Calculates the cross product of this vector and another vector.
    /// For 2D vectors, this is a scalar value.
    ///
    /// @param other The other vector
    /// @return The z-component of the cross product
    public double cross(Vector2 other) {
        return x * other.y - y * other.x;
    }

    /// Calculates the distance between this vector and another vector.
    ///
    /// @param other The other vector
    /// @return The distance between vectors
    public double distance(Vector2 other) {
        double dx = x - other.x;
        double dy = y - other.y;
        return (double) Math.sqrt(dx * dx + dy * dy);
    }

    /// Calculates the angle between this vector and another vector, in radians.
    ///
    /// @param other The other vector
    /// @return The angle in radians
    public double angle(Vector2 other) {
        double dot = dot(other);
        double mags = magnitude() * other.magnitude();
        if (mags == 0) {
            throw new ArithmeticException("Cannot calculate angle with zero vector");
        }
        return (double) Math.acos(Math.min(Math.max(dot / mags, -1.0), 1.0));
    }

    /// Creates a new zero vector (0,0).
    ///
    /// @return A new zero vector
    public static Vector2 zero() {
        return new Vector2(0, 0);
    }

    /// Creates a unit vector pointing right (1,0).
    ///
    /// @return A new right unit vector
    public static Vector2 right() {
        return new Vector2(1, 0);
    }

    /// Creates a unit vector pointing up (0,1).
    ///
    /// @return A new up unit vector
    public static Vector2 up() {
        return new Vector2(0, 1);
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
