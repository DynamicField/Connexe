package fr.connexe.ui;

import fr.connexe.algo.ArrayMaze;
import fr.connexe.algo.Cell;
import fr.connexe.algo.GraphMaze;
import fr.connexe.algo.Point;
import fr.connexe.algo.generation.MazeGenLog;
import javafx.scene.layout.*;

public class MazeRenderer {

    private MazeGenLog log;
    private GraphMaze graphMaze;
    private final MazeSelector mazeSelector = new MazeSelector();

    public MazeRenderer() {}

    public void setDefaultExample() {
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
        this.log = null;
        System.out.println(g);
    }

    public GridPane buildGrid() {
        assert graphMaze != null : "GraphMaze must be set before calling buildGrid()";

        int rows = graphMaze.getHeight();
        int cols = graphMaze.getWidth();
        ArrayMaze arrayMaze = graphMaze.toArrayMaze();

        GridPane grid = new GridPane();
        double cellScale = Math.max(rows, cols);

        for (int i = 0; i < cols; i++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setPercentWidth(100.0 / cellScale);
            grid.getColumnConstraints().add(colConstraints);
        }
        for (int i = 0; i < rows; i++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setPercentHeight(100.0 / cellScale);
            grid.getRowConstraints().add(rowConstraints);
        }

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Region gridCell = new Region();
                StringBuilder style = new StringBuilder("-fx-background-color: white;");

                Point vertexCoordinates = new Point(col, row);
                Cell mazeCell = arrayMaze.getCell(vertexCoordinates);

                int topWidth = 2, rightWidth = 2, bottomWidth = 2, leftWidth = 2;
                String topColor = mazeCell.wallUp() ? "black" : "transparent";
                String rightColor = mazeCell.wallRight() ? "black" : "transparent";
                String bottomColor = mazeCell.wallDown() ? "black" : "transparent";
                String leftColor = mazeCell.wallLeft() ? "black" : "transparent";

                if (row == 0) topWidth = 4;
                if (row == rows - 1) bottomWidth = 4;
                if (col == 0) leftWidth = 4;
                if (col == cols - 1) rightWidth = 4;

                style.append("-fx-border-color: ")
                        .append(topColor).append(" ")
                        .append(rightColor).append(" ")
                        .append(bottomColor).append(" ")
                        .append(leftColor).append(";");

                style.append(" -fx-border-width: ")
                        .append(topWidth).append(" ")
                        .append(rightWidth).append(" ")
                        .append(bottomWidth).append(" ")
                        .append(leftWidth).append(";");

                mazeSelector.selectWall(gridCell, arrayMaze, row, col, grid);

                gridCell.setStyle(style.toString());
                // Mémorise le style initial pour le reset
                gridCell.getProperties().put("initialStyle", style.toString());

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

    public MazeGenLog getLog() {
        return log;
    }

    public void setLog(MazeGenLog log) {
        this.log = log;
    }

    public void setStartVertex(int startVertex) {
        graphMaze.setStart(startVertex);
    }

    public void setEndVertex(int endVertex) {
        graphMaze.setEnd(endVertex);
    }
}