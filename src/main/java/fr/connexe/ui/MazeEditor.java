package fr.connexe.ui;

import fr.connexe.algo.GraphMaze;
import fr.connexe.algo.Point;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

/// Class to manage the selection of walls in the maze.
public class MazeEditor {
    //When selecting another wall, the old one must be unselected
    private Region lastSelectedCell = null;
    private Region lastNeighborCell = null;
    private Boolean isEditMode = false;
    private GraphMaze graphMaze;

    /// Constructor for the maze editor.
    public MazeEditor() {}

    /// Sets the maze this editor is working on.
    ///
    /// @param graphMaze the maze to set
    public void setGraphMaze(GraphMaze graphMaze) {
        this.graphMaze = graphMaze;
    }

    /// Enables maze editing.
    ///
    /// @param editMode true to enable editing, false to disable it
    public void setEditMode(boolean editMode) {
        this.isEditMode = editMode;
    }

    /// Four possible directions for the walls of the labyrinth.
    public enum Side {
        /// The wall is located at the top of the cell.
        TOP,
        /// The wall is located at the right of the cell.
        RIGHT,
        /// The wall is located at the bottom of the cell.
        BOTTOM,
        /// The wall is located at the left of the cell.
        LEFT
    }

    /// Select a wall when it's clicked. There is a margin of 10 around the cell to make it easier to click.
    ///
    /// @param gridCell the cell that was clicked
    /// @param row      the row of the cell
    /// @param col      the column of the cell
    /// @param grid     the grid containing the maze cells (used to retrieve the cell at the clicked position)
    public void configureCellClick(Region gridCell, int row, int col, GridPane grid) {
        //We retrieve the x/y coordinates of the user's click and take the nearest wall with a margin of error of 10 pixels
        gridCell.setOnMouseClicked(event -> {
            // Don't react when NOT in edit mode
            if (!isEditMode) return;

            //Required to use keyboard commands
            gridCell.requestFocus();

            double x = event.getX();
            double y = event.getY();
            double width = gridCell.getWidth();
            double height = gridCell.getHeight();
            double margin = 10;

            Side selectedSide = null;
            //Walls consist of the current cell wall (top, right, bottom, left) and the wall adjacent to the current cell wall (so for top -> bottom, right -> left, bottom -> top, left -> right)
            //So to delete and recover a "wall" in the graphical interface, you must be able to delete and reappear these two walls at the same time.
            Side neighborSide = null;
            int neighborRow = row, neighborCol = col;

            if (y < margin) { // Top wall
                selectedSide = Side.TOP;
                neighborRow = row - 1;
                neighborSide = Side.BOTTOM;
            } else if (y > height - margin) { // Bottom wall
                selectedSide = Side.BOTTOM;
                neighborRow = row + 1;
                neighborSide = Side.TOP;
            } else if (x < margin) { // Left wall
                selectedSide = Side.LEFT;
                neighborCol = col - 1;
                neighborSide = Side.RIGHT;
            } else if (x > width - margin) { // Right wall
                selectedSide = Side.RIGHT;
                neighborCol = col + 1;
                neighborSide = Side.LEFT;
            }

            // If we're selected a wall on the border of the maze, don't do anything!
            if (neighborRow < 0 || neighborCol < 0 || neighborRow >= graphMaze.getHeight() || neighborCol >= graphMaze.getWidth()) {
                return;
            }

            //Resets the previous wall
            if (lastSelectedCell != null) {
                resetBorderColor(lastSelectedCell);
            }
            //(and the neighbor's)
            if (lastNeighborCell != null) {
                resetBorderColor(lastNeighborCell);
            }

            //Updates the selected wall if it exists
            if (selectedSide != null) {
                setBorderColor(gridCell, selectedSide);
                lastSelectedCell = gridCell;

                Region neighborCell = getCellFromGrid(grid, neighborCol, neighborRow);
                if (neighborCell != null) {
                    setBorderColor(neighborCell, neighborSide);
                    lastNeighborCell = neighborCell;
                } else {
                    lastNeighborCell = null;
                }
            }
        });
    }

    /// Get the colors of the borders of a cell.
    ///
    /// @param style style of the cell
    /// @return the colors of the borders of the cell
    public String[] getColors(String style) {
        //Search the style for the border color
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("-fx-border-color: ([^;]+);")
                .matcher(style);
        if (m.find()) {
            //and make a table of it
            return m.group(1).split(" ");
        }
        return null;
    }

    /// Sets the border color of a cell, which was selected by selectWall, to red.
    ///
    /// @param gridCell the cell that was clicked
    /// @param side     the wall that was clicked (top, right, bottom, left)
    public void setBorderColor(Region gridCell, Side side) {
        //Style format (only '-fx-border-color' is important here): -fx-background-color: white; -fx-border-color: top:color1 right:color2 left:color3 bottom:color4; -fx-border-width: 2 2 2 2;
        String style = gridCell.getStyle();
        String[] colors = getColors(style);
        colors[side.ordinal()] = "red";
        style = style.replaceAll("-fx-border-color: [^;]+;", "");
        style += "-fx-border-color: " + String.join(" ", colors) + ";";
        gridCell.setStyle(style);
    }

    /// Resets all the border colors of a cell to their initial colors (black or transparent).
    ///
    /// @param gridCell the cell that was clicked (used to retrieve the initial style)
    public void resetBorderColor(Region gridCell) {
        //Retrieves the style before the change that we saved in MazeRenderer and applies it again to the cell
        String initialStyle = (String) gridCell.getProperties().get("initialStyle");
        String style = gridCell.getStyle();
        String[] colors = getColors(style);
        String[] initialColors = getColors(initialStyle);
        for (int i = 0; i < colors.length; i++) {
            if (colors[i].equals("red")) {
                colors[i] = initialColors[i];
            }
        }
        // Convert the colors array into a CSS string: -fx-border-color: color1 color2 color3 color4;
        style = style.replaceAll("-fx-border-color: [^;]+;", "");
        style += "-fx-border-color: " + String.join(" ", colors) + ";";
        gridCell.setStyle(style);
    }

    /// Returns the cell at the given position in the grid.
    ///
    /// @param grid Labyrinth Grid
    /// @param col  the column of the cell
    /// @param row  the row of the cell
    /// @return the cell at the given position in the grid, or null if the cell doesn't exist in the grid or is not a Region.
    public Region getCellFromGrid(GridPane grid, int col, int row) {
        for (Node node : grid.getChildren()) {
            Integer nodeCol = GridPane.getColumnIndex(node);
            Integer nodeRow = GridPane.getRowIndex(node);
            if (nodeCol != null && nodeRow != null && nodeCol == col && nodeRow == row) {
                if (node instanceof Region) {
                    return (Region) node;
                }
            }
        }
        return null;
    }

    /// If the user presses backspace or delete, the wall is deleted. If he presses enter or space, the wall is added.
    ///
    /// @param gridCell the cell that was clicked and can be remembered due to the focus in selectWall
    /// @param row      the row of the cell
    /// @param col      the column of the cell
    /// @param grid     the grid containing the maze cells (used to retrieve the cell at the clicked position)
    public void configureCellCommands(Region gridCell, int row, int col, GridPane grid) {
        gridCell.setOnKeyPressed(event -> {
            if (!isEditMode) return;
            if (event.getCode() == KeyCode.BACK_SPACE || event.getCode() == KeyCode.DELETE) {
                applyBorder(gridCell, row, col, grid, "transparent");
            } else if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                applyBorder(gridCell, row, col, grid, "black");
            }
        });
    }

    /// Applies the action "type" to the cell
    ///
    /// @param gridCell the cell that was clicked
    /// @param row      the row of the cell
    /// @param col      the column of the cell
    /// @param grid     the grid containing the maze cells (used to retrieve the cell at the clicked position)
    /// @param type     the color to apply (black or transparent)
    public void applyBorder(Region gridCell, int row, int col, GridPane grid, String type) {
        // Find the border colors of the cell
        String style = gridCell.getStyle();
        String[] colors = getColors(style);
        if (colors == null) return;

        // For each side, if it's red, we apply the action "type" and we apply the same action to the neighbor
        Side[] sides = Side.values();
        for (int i = 0; i < colors.length; i++) {
            if ("red".equals(colors[i])) {
                colors[i] = type;
                //sides[i] <=> colors[i]. In fact, the colors of the walls and their position in the cell have the same index.
                applyNeighborBorder(sides[i], row, col, grid, type);
            }
        }

        style = style.replaceAll("-fx-border-color: [^;]+;", "");
        style += "-fx-border-color: " + String.join(" ", colors) + ";";
        gridCell.setStyle(style);
        gridCell.getProperties().put("initialStyle", style);
    }

    /// Clears all the red borders of the maze.
    ///
    /// @param grid the grid containing the maze cells
    public void clearAllRedBorders(GridPane grid) {
        for (Node node : grid.getChildren()) {
            if (node instanceof Region region) {
                resetBorderColor(region);
            }
        }
    }

    /// Applies the action "type" to the neighbor of the cell (split of applyBorder() for more clarity).
    ///
    /// @param side the wall that was removed/added
    /// @param row  the row of the cell
    /// @param col  the column of the cell
    /// @param grid the grid containing the maze cells (used to retrieve the cell at the clicked position)
    /// @param type the color to apply (black or transparent)
    public void applyNeighborBorder(Side side, int row, int col, GridPane grid, String type) {
        //Calculate the neighbor coordinate in the same way as for selectWall
        int neighborRow = row, neighborCol = col;

        //The neighbor is the cell that is on the other side of the wall with the same method that is used to select the wall
        Side neighborSide = switch (side) {
            case TOP -> {
                neighborRow = row - 1;
                yield Side.BOTTOM;
            }
            case RIGHT -> {
                neighborCol = col + 1;
                yield Side.LEFT;
            }
            case BOTTOM -> {
                neighborRow = row + 1;
                yield Side.TOP;
            }
            case LEFT -> {
                neighborCol = col - 1;
                yield Side.RIGHT;
            }
        };

        // Apply the changes to the graph (connect/disconnect the two vertices)
        applyGraphMaze(col, row, neighborCol, neighborRow, type);

        // Reflect this change on the both cell's borders.
        Region neighborCell = getCellFromGrid(grid, neighborCol, neighborRow);
        if (neighborCell != null) {
            String neighborStyle = neighborCell.getStyle();
            String[] neighborColors = getColors(neighborStyle);
            if (neighborColors != null) {
                neighborColors[neighborSide.ordinal()] = type;
                neighborStyle = neighborStyle.replaceAll("-fx-border-color: [^;]+;", "");
                neighborStyle += "-fx-border-color: " + String.join(" ", neighborColors) + ";";
                neighborCell.setStyle(neighborStyle);
                neighborCell.getProperties().put("initialStyle", neighborStyle);
            }
        }
    }

    /// Applies the action "type" to the graph maze by connecting/disconnecting the two vertices.
    ///
    /// @param col         the column of the cell
    /// @param row         the row of the cell
    /// @param neighborCol the column of the neighbor cell
    /// @param neighborRow the row of the neighbor cell
    /// @param type        the color to apply (black or transparent)
    public void applyGraphMaze(int col, int row, int neighborCol, int neighborRow, String type) {
        //Get the coordinates of the cell and the neighbor of the cell and create the two points and their vertices
        int vertexBorder = graphMaze.toVertexId(new Point(col, row));
        int vertexNeighborBorder = graphMaze.toVertexId(new Point(neighborCol, neighborRow));

        if (type.equals("transparent")) {
            // Remove the wall -> connect the vertices together
            graphMaze.connect(vertexBorder, vertexNeighborBorder);
        } else {
            // Add the wall -> disconnect the vertices together
            graphMaze.disconnect(vertexBorder, vertexNeighborBorder);
        }
    }
}
