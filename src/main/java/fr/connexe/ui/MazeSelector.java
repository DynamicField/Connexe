package fr.connexe.ui;

import fr.connexe.algo.ArrayMaze;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

/**
 * Class to manage the selection of walls in the maze.
 */
public class MazeSelector {
    //When selecting another wall, the old one must be unselected
    private Region lastSelectedCell = null;
    private Region lastNeighborCell = null;

    public MazeSelector() {}

    /**
     Select a wall when it's clicked. There is a margin of 10 around the cell to make it easier to click.
     @param gridCell the cell that was clicked
     @param arrayMaze the maze
     @param row the row of the cell
     @param col the column of the cell
     @param grid the grid containing the maze cells (used to retrieve the cell at the clicked position)
     */
    public void selectWall(Region gridCell, ArrayMaze arrayMaze, int row, int col, GridPane grid) {
        gridCell.setOnMouseClicked(event -> {
            double x = event.getX();
            double y = event.getY();
            double width = gridCell.getWidth();
            double height = gridCell.getHeight();
            double margin = 10;

            String selectedSide = null;
            //Walls consist of the current cell wall (top, right, bottom, left) and the wall adjacent to the current cell wall (so for top -> bottom, right -> left, bottom -> top, left -> right)
            //So to delete and recover a "wall" in the graphical interface, you must be able to delete and reappear these two walls at the same time.
            String neighborSide = null;
            int neighborRow = row, neighborCol = col;

            if (y < margin) {
                selectedSide = "top";
                neighborRow = row - 1;
                neighborSide = "bottom";
            } else if (y > height - margin) {
                selectedSide = "bottom";
                neighborRow = row + 1;
                neighborSide = "top";
            } else if (x < margin) {
                selectedSide = "left";
                neighborCol = col - 1;
                neighborSide = "right";
            } else if (x > width - margin) {
                selectedSide = "right";
                neighborCol = col + 1;
                neighborSide = "left";
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

                //Updates the neighbor wall if it exists
                if (neighborRow >= 0 && neighborCol >= 0 && neighborRow < arrayMaze.getHeight() && neighborCol < arrayMaze.getWidth()) {
                    Region neighborCell = getCellFromGrid(grid, neighborCol, neighborRow);
                    if (neighborCell != null) {
                        setBorderColor(neighborCell, neighborSide);
                        lastNeighborCell = neighborCell;
                    } else {
                        lastNeighborCell = null;
                    }
                } else {
                    lastNeighborCell = null;
                }
            }
        });
    }

    /**
     * Sets the border color of a cell, which was selected by selectWall, to red.
     * @param gridCell the cell that was clicked
     * @param side the wall that was clicked (top, right, bottom, left)
     */
    public void setBorderColor(Region gridCell, String side) {
        String style = gridCell.getStyle(); //Style format (only '-fx-border-color' is important here): -fx-background-color: white; -fx-border-color: top:color1 right:color2 left:color3 bottom:color4; -fx-border-width: 2 2 2 2;
        String[] colors = {"", "", "", ""};
        //Recover the 4 colors in the style
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("-fx-border-color: ([^;]+);")
                .matcher(style);
        if (m.find()) {
            //and make a table of it
            String[] baseColors = m.group(1).split(" ");
            System.arraycopy(baseColors, 0, colors, 0, 4);
        }
        switch (side) {
            case "top":    colors[0] = "red"; break;
            case "right":  colors[1] = "red"; break;
            case "bottom": colors[2] = "red"; break;
            case "left":   colors[3] = "red"; break;
        }
        style = style.replaceAll("-fx-border-color: [^;]+;", "");
        style += "-fx-border-color: " + String.join(" ", colors) + ";";
        gridCell.setStyle(style);
    }

    /**
     * Resets all the border colors of a cell to their initial colors (black or transparent).
     * @param gridCell the cell that was clicked (used to retrieve the initial style)
     */
    public void resetBorderColor(Region gridCell){
        //Retrieves the style before the change that we saved in MazeRenderer and applies it again to the cell
        String initialStyle = (String) gridCell.getProperties().get("initialStyle");
        gridCell.setStyle(initialStyle);
    }

    /**
     * Returns the cell at the given position in the grid.
     * @param grid the grid containing the maze cells (used to retrieve the cell at the clicked position)
     * @param col the column of the cell
     * @param row the row of the cell
     * @return the cell at the given position in the grid, or null if the cell doesn't exist in the grid or is not a Region.
     */
    public Region getCellFromGrid(Parent grid, int col, int row) {
        if (!(grid instanceof GridPane)) return null;
        int nodeCol;int nodeRow;
        for (Node node : ((GridPane) grid).getChildren()) {
            nodeCol = GridPane.getColumnIndex(node);
            nodeRow = GridPane.getRowIndex(node);
            if (nodeCol == col && nodeRow == row) {
                return (Region) node;
            }
        }
        return null;
    }
}

