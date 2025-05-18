package fr.connexe.ui;

import fr.connexe.algo.ArrayMaze;
import fr.connexe.algo.Cell;
import fr.connexe.algo.GraphMaze;
import fr.connexe.algo.Point;
import fr.connexe.algo.generation.MazeGenLog;
import javafx.animation.PauseTransition;
import javafx.scene.Node;
import javafx.scene.layout.*;

import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Supplier;

///  Renderer for a GraphMaze as a JavaFX GridPane
public class MazeRenderer {

    private MazeGenLog log;
    private GraphMaze graphMaze;
    private GridPane grid; // currently displayed maze grid
    private Supplier<Double> delaySupplier; // supplier to query speed value during animations


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

                // Retrieve information of the cell from the ArrayMaze
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

                // Apply inline style (has priority over class styles) on cell for borders (= walls)
                String style = "-fx-border-color: black; -fx-border-width: "
                        + topWidth + " "
                        + rightWidth + " "
                        + bottomWidth + " "
                        + leftWidth + ";";

                gridCell.setStyle(style);

                // Apply default background
                gridCell.getStyleClass().add("cell-color-default");

                // Allow dynamic resizing of the cell
                GridPane.setHgrow(gridCell, Priority.ALWAYS);
                GridPane.setVgrow(gridCell, Priority.ALWAYS);
                gridCell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                grid.add(gridCell, col, row);
            }
        }
    }

    public void animateSolution(List<Stack<Integer>> totalSteps,  boolean isDFS, Runnable onFinished){
        assert graphMaze != null : "GraphMaze must be set before calling animateDijkstraSolution()";
        assert grid != null : "Grid must be built first before calling animateDijkstraSolution()";

        clearGridColor();

        // Reverse history of all steps in chronological order
        List<Stack<Integer>> chronologicalSteps = totalSteps.reversed();

        if(isDFS){
            // Start playing animation from the step of the DFS algorithm (first tested path) (index 0 is the final path)
            // Flatten all paths into a singular one (skip final path at index 0) to animate node by node
            List<Integer> flattened = flattenPaths(chronologicalSteps.subList(1, chronologicalSteps.size()));
            playNodeByNodeDFS(0, flattened, () -> {
                // When it's finished, play the build of the final solution path
                playFinalSolutionStep(0, totalSteps.getLast(), onFinished);
            });
        } else {
            // Start playing animation from the first step of the solving algorithm (index 0 is the final path)
            playSolutionStep(1, chronologicalSteps, () -> {
                // When it's finished, play the build of the final solution path
                playFinalSolutionStep(0, totalSteps.getLast(), onFinished);
            });
        }
    }

    private void playSolutionStep(int step, List<Stack<Integer>> totalSteps, Runnable onFinished){
        if (step > totalSteps.size()-1) { // animation is finished
            if (onFinished != null) onFinished.run();
            return;
        }
        // Retrieve intermediate step (current stack history)
        Stack<Integer> stepStack = totalSteps.get(step);

        // Dijkstra and Clockwise keep an entire history of visited nodes, the top of the stack being the newest visited one
        // Retrieve newest visited vertex (top of the stack) and color its cell visited
        int visitedVertex = stepStack.peek();
        Point vertexCoordinates = graphMaze.toPoint(visitedVertex);
        Node cell = getCellNode(vertexCoordinates);
        setCellColor(cell, "cell-color-visited");

        // Query the current delay (animation speed) from the supplier
        double currentDelayMs = delaySupplier != null ? delaySupplier.get() : 500;

        // Wait for a certain time delay without freezing the UI thread then go to the next step
        PauseTransition pause = new PauseTransition(Duration.millis(currentDelayMs));
        pause.setOnFinished(e -> playSolutionStep(step + 1, totalSteps, onFinished));
        pause.play();
    }

    private void playFinalSolutionStep(int step, Stack<Integer> solution, Runnable onFinished){
        if (step > solution.size()-1) { // animation is finished
            if (onFinished != null) onFinished.run();
            return;
        }

        Point vertexCoordinates = graphMaze.toPoint(solution.get(step));
        Node cell = getCellNode(vertexCoordinates);
        setCellColor(cell, "cell-color-path");

        // Query the current delay (animation speed) from the supplier
        double currentDelayMs = delaySupplier != null ? delaySupplier.get() : 500;

        // Wait for a certain time delay without freezing the UI thread then go to the next step
        PauseTransition pause = new PauseTransition(Duration.millis(currentDelayMs));
        pause.setOnFinished(e -> playFinalSolutionStep(step + 1, solution, onFinished));
        pause.play();

        if(onFinished != null) onFinished.run();
    }

    private void playNodeByNodeDFS(int index, List<Integer> visitedNodes, Runnable onFinished) {
        if (index > visitedNodes.size()-1) { // animation is finished
            if (onFinished != null) onFinished.run();
            return;
        }

        // Retrieve first visited node, color it visited
        int visitedVertex = visitedNodes.get(index);
        Point vertexCoordinates = graphMaze.toPoint(visitedVertex);
        Node cell = getCellNode(vertexCoordinates);
        setCellColor(cell, "cell-color-visited");

        // Query the current delay (animation speed) from the supplier
        double currentDelayMs = delaySupplier != null ? delaySupplier.get() : 500;

        // Wait for a certain time delay without freezing the UI thread then go to the next visited node
        PauseTransition pause = new PauseTransition(Duration.millis(currentDelayMs));
        pause.setOnFinished(e -> playNodeByNodeDFS(index + 1, visitedNodes, onFinished));
        pause.play();
    }

    private List<Integer> flattenPaths(List<Stack<Integer>> totalSteps) {
        List<Integer> steps = new ArrayList<>();
        for (Stack<Integer> path : totalSteps) {
            for (Integer node : path) {
                steps.add(node); // Append all visited nodes in order
            }
        }
        return steps;
    }


    /// Access the children of the maze's [GridPane] at a specific column and row index given by [Point] coordinates.
    /// @param coordinates (col, row) coordinates of the cell to retrieve
    public Node getCellNode(Point coordinates) {
        for (Node node : grid.getChildren()) {
            Integer rowIndex = GridPane.getRowIndex(node);
            Integer colIndex = GridPane.getColumnIndex(node);

            // Handle nulls (they default to 0)
            if (rowIndex == null) rowIndex = 0;
            if (colIndex == null) colIndex = 0;

            if (rowIndex == coordinates.y() && colIndex == coordinates.x()) {
                return node;
            }
        }
        return null; // not found
    }

    public void setCellColor(Node node, String colorClass){
        assert colorClass.startsWith("cell-color-") : "colorClass must start with 'cell-color-'";

        node.getStyleClass().removeIf(styleClass -> styleClass.startsWith("cell-color-"));
        node.getStyleClass().add(colorClass);
    }

    public void clearGridColor(){
        for (Node node : grid.getChildren()) {
            setCellColor(node, "cell-color-default");
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
