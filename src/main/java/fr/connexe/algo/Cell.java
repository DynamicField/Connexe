package fr.connexe.algo;

import java.util.Objects;

/// A cell located within an [ArrayMaze].
///
/// Cells indicate their boundaries: walls present in either direction (left/right, up/down).
///
/// @param pos the position of the cell within the maze, starting at (0, 0).
/// @param wallLeft indicates if the cell has a wall to the left.
/// @param wallRight indicates if the cell has a wall to the right.
/// @param wallUp indicates if the cell has a wall above.
/// @param wallDown indicates if the cell has a wall below.
public record Cell(Point pos,
                   boolean wallLeft,
                   boolean wallRight,
                   boolean wallUp,
                   boolean wallDown) {
    /// Creates a new cell by checking the position.
    public Cell {
        Objects.requireNonNull(pos, "The position of a cell cannot be null");
        if (pos.x() < 0 || pos.y() < 0) {
            throw new IllegalArgumentException("The position of a cell cannot be negative");
        }
    }

    /// Returns the same cell with a different position.
    ///
    /// @param pos the new position of the cell.
    /// @return the same cell with a different position.
    public Cell withPos(Point pos) {
        return new Cell(pos, wallLeft, wallRight, wallUp, wallDown);
    }
}
