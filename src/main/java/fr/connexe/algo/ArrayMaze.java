package fr.connexe.algo;


/// A **rectangular maze** using an **array structure** to represent cells and walls in a 2D space.
///
/// @see Cell
public class ArrayMaze {
    private final Cell[][] maze;
    private final int width;
    private final int height;
    private final int numCells;

    /// Creates a new maze with the given dimensions and cells. Cells will have the correct position.
    ///
    /// The array is in the following format: `cells[y][x]`
    ///
    /// @param cells The cells of the maze.
    /// @param width The width of the maze.
    /// @param height The height of the maze.
    public ArrayMaze(Cell[][] cells, int width, int height) {
        this.maze = new Cell[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Point pos = new Point(x, y);
                Cell cell = cells[y][x];

                // Take the original array and copy its cell with the right position if necessary.
                if (cell.pos().equals(pos)) {
                    this.maze[y][x] = cell;
                } else {
                    this.maze[y][x] = cell.withPos(pos);
                }
            }
        }

        this.width = width;
        this.height = height;
        this.numCells = width * height;
    }

    /// Get a cell at the given point.
    ///
    /// @param pos the position of the cell.
    /// @return the cell at the given position.
    public Cell getCell(Point pos) {
        return this.maze[pos.y()][pos.x()];
    }

    /// Returns the width of the maze.
    /// @return the width
    public int getWidth() {
        return width;
    }

    /// Returns the height of the maze.
    /// @return the height
    public int getHeight() {
        return height;
    }

    /// Returns the total number of cells in the maze.
    /// @return the number of cells
    public int getNumCells() {
        return numCells;
    }

    @Override
    public String toString() {
        // Each cell is a NxN square.
        // ### (size 3)
        // # #
        // ###

        // 5x5
        // ######
        // #    #
        // #    #
        // #    #
        // ######
        // We're going to skip lines and columns that duplicate borders in the inside of the maze.

        // The size of each cell, NxN square.
        final int SIZE = 5;
        // The 2D matrix of ASCII pixels to print.
        char[][] pixels = new char[height * SIZE][width * SIZE];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Cell cell = maze[y][x];

                // Origin position of the cell in the ASCII matrix.
                int px0 = x * SIZE;
                int py0 = y * SIZE;

                // Draw the center of the cell with the 'o' character.
                pixels[py0 + SIZE / 2][px0 + SIZE / 2] = 'o';

                // Draw the top wall of the cell.
                if (cell.wallUp()) {
                    for (int i = 0; i < SIZE; i++) {
                        pixels[py0][px0 + i] = '-';
                    }
                }
                // Draw the left wall of the cell.
                if (cell.wallDown()) {
                    for (int i = 0; i < SIZE; i++) {
                        pixels[py0 + SIZE - 1][px0 + i] = '-';
                    }
                }
                // Draw the right wall of the cell.
                if (cell.wallLeft()) {
                    for (int i = 0; i < SIZE; i++) {
                        pixels[py0 + i][px0] = '|';
                    }
                }
                // Draw the bottom wall of the cell.
                if (cell.wallRight()) {
                    for (int i = 0; i < SIZE; i++) {
                        pixels[py0 + i][px0 + SIZE - 1] = '|';
                    }
                }

                // Draw corners of the cell.
                pixels[py0 + SIZE - 1][px0] = '#';
                pixels[py0 + SIZE - 1][px0 + SIZE - 1] = '#';
                pixels[py0][px0] = '#';
                pixels[py0][px0 + SIZE - 1] = '#';
            }
        }

        // Prepare a string builder to make the final string.
        // The given capacity is an estimation of the characters based off the ASCII matrix size.
        // The actual size will be slightly smaller though, as we're skipping lines/columns.
        var sb = new StringBuilder(width * height * (SIZE * SIZE) + height);
        for (int py = 0; py < height * SIZE; py++) {
            // Skip duplicate lines that aren't the border of the maze.
            if (py != 0 && py != height * SIZE - 1 && py % SIZE == 0) {
                continue;
            }

            for (int px = 0; px < width * SIZE; px++) {
                // Skip duplicate columns that aren't the border of the maze.
                if (px != 0 && px != width * SIZE - 1 && px % SIZE == 0) {
                    continue;
                }

                // Print the character.
                // Default is null char so we need to print a space when that happens
                char c = pixels[py][px];
                if (c == '\0') {
                    sb.append(' ');
                } else {
                    sb.append(c);
                }
            }

            // Write a new line.
            sb.append('\n');
        }

        // The end!
        return sb.toString();
    }
}
