package fr.connexe.ui;

import fr.connexe.algo.ArrayMaze;
import fr.connexe.algo.Cell;
import fr.connexe.algo.GraphMaze;
import fr.connexe.algo.Point;
import fr.connexe.algo.generation.MazeGenLog;
import javafx.animation.PauseTransition;
import javafx.scene.layout.*;

import javafx.util.Duration;

import java.util.function.Supplier;

///  Renderer for a GraphMaze as a JavaFX GridPane
public class MazeRenderer {

    private MazeGenLog log;
    private GraphMaze graphMaze;
    private GridPane grid; // currently displayed maze grid
    private Supplier<Double> delaySupplier; // supplier to query speed value during animations
    private final MazeSelector mazeSelector = new MazeSelector();


    /// Initialize a maze renderer about to take a maze which parameters will be set by a user
    public MazeRenderer(){}

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
        this.log = null; // in case we're reusing a previous renderer for another maze for whatever reason

        System.out.println(g);
    }

    ///  Build a [GridPane] to represent the maze and its walls
    public void buildGrid() {
        assert graphMaze != null : "GraphMaze must be set before calling buildGrid()";
        ArrayMaze arrayMaze = graphMaze.toArrayMaze();

        // Initialize a new GridPane object for the renderer's grid and build the maze
        this.grid = initMazeGrid();
        buildWalls(arrayMaze);
    }

    /// Animate the whole maze grid generation
    /// @param onFinished piece of code to run later when the animation is finished.
    /// (to re-enable buttons for example)
    public void animateGridBuild(Runnable onFinished) {
        assert graphMaze != null : "GraphMaze must be set before calling animateGridBuild()";
        assert log != null : "MazeGenLog must be set before calling animateGridBuild()";
        assert grid != null : "Grid must be built first before calling animateGridBuild()";

        int totalSteps = log.size();
        playStep(1, totalSteps, onFinished);
    }

    /// Animate the current step
    /// @param step step number in the logs
    /// @param totalSteps total number of steps during generation (from the logs)
    /// @param onFinished piece of code to run later when the animation is finished.
    /// (to re-enable buttons for example)
    private void playStep(int step, int totalSteps, Runnable onFinished) {
        if (step > totalSteps) { // animation is finished
            if (onFinished != null) onFinished.run();
            return;
        }

        // Build and render this step
        GraphMaze mazeStep = log.buildMazeUntil(step);
        ArrayMaze mazeStepArray = mazeStep.toArrayMaze();

        grid.getChildren().clear();
        buildWalls(mazeStepArray);

        // Query the current delay (animation speed) from the supplier
        double currentDelayMs = delaySupplier != null ? delaySupplier.get() : 500;

        // Wait for a certain time delay without freezing the UI thread then go to the next step
        PauseTransition pause = new PauseTransition(Duration.millis(currentDelayMs));
        pause.setOnFinished(e -> playStep(step + 1, totalSteps, onFinished));
        pause.play();
    }

    /// Initialize an empty [GridPane] with the graph's dimensions.
    private GridPane initMazeGrid(){
        assert graphMaze != null : "GraphMaze must be set before calling initMazeGrid()";

        int rows = graphMaze.getHeight();
        int cols = graphMaze.getWidth();

        GridPane grid = new GridPane();
        double cellScale = Math.max(rows, cols);

        // Set constraints for the GridPane to preserve a view ratio depending on its number of rows and columns

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

        return grid;
    }

    /// For a given [ArrayMaze], initialize regions in the corresponding [GridPane] cells
    /// and build their walls (as borders).
    /// This method is used not only to build the current renderer's maze ([#buildGrid()]), but also
    /// used for the generation animation ([#animateGridBuild(Duration)])
    /// to build the grid for intermediate [ArrayMaze] steps
    /// @param arrayMaze the maze that needs to be rendered, either `MazeRenderer`'s finished `ArrayMaze`, either
    /// an intermediate step of its generation
    private void buildWalls(ArrayMaze arrayMaze){
        int rows = arrayMaze.getHeight();
        int cols = arrayMaze.getWidth();

        // Build a region holding walls (borders) on each cell
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Region gridCell = new Region();
                StringBuilder style = new StringBuilder("-fx-background-color: white;");

                // Retrieve information of the cell from the ArrayMaze
                Point vertexCoordinates = new Point(col, row);
                Cell mazeCell = arrayMaze.getCell(vertexCoordinates);

                //Black=Wall, Transparent=Empty
                int topWidth = 2, rightWidth = 2, bottomWidth = 2, leftWidth = 2;
                String topColor = mazeCell.wallUp() ? "black" : "transparent";
                String rightColor = mazeCell.wallRight() ? "black" : "transparent";
                String bottomColor = mazeCell.wallDown() ? "black" : "transparent";
                String leftColor = mazeCell.wallLeft() ? "black" : "transparent";

                //If the wall is within the border, the width of the border is increased to make it more aesthetic.
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

                gridCell.setStyle(style.toString());

                //Remembers the initial style for reset
                gridCell.getProperties().put("initialStyle", style.toString());

                mazeSelector.selectWall(gridCell, arrayMaze, row, col, grid);
                mazeSelector.interactBorder(grid,gridCell);

                // Allow dynamic resizing of the cell
                GridPane.setHgrow(gridCell, Priority.ALWAYS);
                GridPane.setVgrow(gridCell, Priority.ALWAYS);
                gridCell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                grid.add(gridCell, col, row);
            }
        }
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

    public GridPane getGrid() {
        return grid;
    }

    public void setDelaySupplier(Supplier<Double> delaySupplier) {
        this.delaySupplier = delaySupplier;
    }
}
