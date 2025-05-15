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
        // Each cell is a NxN square, ideally N is odd so we can center numbers properly.
        // ### (size 3)
        // # #
        // ###

        // 6x6
        // ######
        // #    #
        // #    #
        // #    #
        // #    #
        // ######
        // We're going to skip lines and columns that duplicate borders in the inside of the maze.

        // The size of each cell, NxN square. Make sure it's large enough to display all vertex ids.
        final int SIZE = Math.max(5, toNearestOddNumber(countDigits(numCells)) + 2);
        // The 2D matrix of ASCII pixels to print.
        char[][] pixels = new char[height * SIZE][width * SIZE];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Cell cell = maze[y][x];

                // Origin position of the cell in the ASCII matrix.
                int px0 = x * SIZE;
                int py0 = y * SIZE;

                // Format the vertex id so it's padded by zeros, and has a length of exactly SIZE-2 characters,
                // to leave room for the walls.
                // Exampled: 1 --> 001, 48 --> 048
                String id = String.format("%0" + (SIZE-2) + "d", x+y*width);
                // Print out the vertex id of the cell in the center of the cell, digit by digit.
                for (int i = 0; i < id.length(); i++) {
                    pixels[py0 + SIZE / 2][px0 + i + 1] = id.charAt(i);
                }

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

    // Returns the odd number just above n. In other words, the smallest odd number r such that r >= n.
    // Examples: 4 --> 5, 9 --> 9, 10 --> 11.
    private static int toNearestOddNumber(int n) {
        // Turn on the LSB so we always have an odd number.
        // Examples:
        // 3: 11 -> 11  (3)
        // 2: 10 -> 11  (3)
        return n | 1;
    }

    // Returns the number of digits needed to display the given number.
    // Same as floor(log10(n)).
    // n must be positive.
    private static int countDigits(int n) {
        assert n >= 0;

        if (n < 10) {
            return 1;
        } else {
            return 1 + countDigits(n / 10);
        }
    }
}
