package fr.connexe.ui;

import fr.connexe.algo.ArrayMaze;
import fr.connexe.algo.Cell;
import fr.connexe.algo.GraphMaze;
import fr.connexe.algo.Point;
import fr.connexe.algo.generation.MazeGenResult;
import javafx.scene.layout.*;

///  Renderer for a GraphMaze as a JavaFX GridPane
public class MazeRenderer {

    private MazeGenResult mazeGenResult;
    private GraphMaze graphMaze;

    /// Initialize a maze renderer about to take a maze which parameters will be set by a user
    public MazeRenderer(){
    }

    ///  For testing purposes
    public void setDefaultExample(){
        var g = new GraphMaze(4, 4);
        g.connect(0, 1);
        g.connect(0, 4);
        g.connect(2, 3);
        g.connect(3, 7);
        g.connect(1, 2);
        g.connect(4, 5);
        g.connect(4, 8);
        g.connect(6, 7);
        g.connect(6, 10);
        g.connect(8, 9);
        g.connect(8, 12);
        g.connect(9, 13);
        g.connect(9, 10);
        g.connect(10, 11);
        g.connect(10, 14);
        g.connect(11, 15);
        g.connect(14, 15);
        g.setStart(0);
        g.setEnd(15);
        this.graphMaze = g;
        System.out.println(g);
    }

    ///  Build a JavaFX GridPane to represent the maze and its walls
    public GridPane buildGrid() {
        int rows = graphMaze.getHeight();
        int cols = graphMaze.getWidth();
        ArrayMaze arrayMaze = graphMaze.toArrayMaze();

        GridPane grid = new GridPane();
        double cellScale = Math.max(rows, cols);

        // Set column constraints (once per column)
        for (int i = 0; i < cols; i++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setPercentWidth(100.0 / cellScale);
            grid.getColumnConstraints().add(colConstraints);
        }

        // Set row constraints (once per row)
        for (int i = 0; i < rows; i++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setPercentHeight(100.0 / cellScale);
            grid.getRowConstraints().add(rowConstraints);
        }

        // Add empty regions to each cell
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Region gridCell = new Region();

                // Add a style to the region (make each cell white)
                StringBuilder style = new StringBuilder("-fx-background-color: white;");

                Point vertexCoordinates = new Point(col, row);
                Cell mazeCell = arrayMaze.getCell(vertexCoordinates);

                // Calculate border widths if wall exists in that cell
                int topWidth = mazeCell.wallUp() ? 2 : 0;
                int rightWidth = mazeCell.wallRight() ? 2 : 0;
                int bottomWidth = mazeCell.wallDown() ? 2 : 0;
                int leftWidth = mazeCell.wallLeft() ? 2 : 0;

                // Boost outer borders to 4px to visually balance internal shared borders
                if (row == 0 && topWidth > 0) topWidth = 4;
                if (row == rows - 1 && bottomWidth > 0) bottomWidth = 4;
                if (col == 0 && leftWidth > 0) leftWidth = 4;
                if (col == cols - 1 && rightWidth > 0) rightWidth = 4;

                // Apply style on cell
                style.append("-fx-border-color: black;");
                style.append(" -fx-border-width: ")
                        .append(topWidth).append(" ")
                        .append(rightWidth).append(" ")
                        .append(bottomWidth).append(" ")
                        .append(leftWidth).append(";");

                gridCell.setStyle(style.toString());

                // Allow dynamic resizing of the cell
                GridPane.setHgrow(gridCell, Priority.ALWAYS);
                GridPane.setVgrow(gridCell, Priority.ALWAYS);
                gridCell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                grid.add(gridCell, col, row);
            }
        }

        return grid;
    }

    public GraphMaze getGraphMaze() {
        return graphMaze;
    }

    public void setGraphMaze(GraphMaze graphMaze) {
        this.graphMaze = graphMaze;
    }

    public MazeGenResult getMazeGenResult() {
        return mazeGenResult;
    }

    public void setMazeGenResult(MazeGenResult mazeGenResult) {
        this.mazeGenResult = mazeGenResult;
        this.graphMaze = mazeGenResult.maze();
    }

    public void setStartVertex(int startVertex) {
        graphMaze.setStart(startVertex);
    }

    public void setEndVertex(int endVertex) {
        graphMaze.setEnd(endVertex);
    }
}
