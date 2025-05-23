package fr.connexe.ui;

import fr.connexe.algo.ArrayMaze;
import fr.connexe.algo.Cell;
import fr.connexe.algo.GraphMaze;
import fr.connexe.algo.Point;
import fr.connexe.algo.generation.MazeGenLog;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.*;

import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.util.Duration;

import java.util.*;
import java.util.function.Supplier;

///  Renderer for a GraphMaze as a JavaFX GridPane
public class MazeRenderer {

    private MazeGenLog log;
    private GraphMaze graphMaze;
    private GridPane grid; // currently displayed maze grid
    private Supplier<Double> delaySupplier; // supplier to query speed value during animations
    private PauseTransition currentPause;
    private boolean lastAnimIsGeneration;
    private final MazeEditor mazeEditor = new MazeEditor();


    /// Initialize a maze renderer about to take a maze which parameters will be set by a user
    public MazeRenderer(){}

    /// Get maze editor.
    /// @return mazeEditor object
    public MazeEditor getMazeSelector() {
        return mazeEditor;
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

        lastAnimIsGeneration = true;
        int totalSteps = log.size();
        playGenerationStep(1, totalSteps, onFinished);
    }

    /// Animate the current step of the generation animation
    /// @param step step number in the logs
    /// @param totalSteps total number of steps during generation (from the logs)
    /// @param onFinished piece of code to run later when the animation is finished.
    /// (to re-enable buttons for example)
    private void playGenerationStep(int step, int totalSteps, Runnable onFinished) {
        if (step > totalSteps) { // animation is finished
            if (onFinished != null) onFinished.run();
            return;
        }

        // Build and render this step
        GraphMaze mazeStep = log.buildMazeUntil(step);
        ArrayMaze mazeStepArray = mazeStep.toArrayMaze();

        renderMaze(mazeStepArray);

        // Query the current delay (animation speed) from the supplier
        double currentDelayMs = delaySupplier != null ? delaySupplier.get() : 500;

        stopCurrentPause(); // stop previous animation if needed

        // Wait for a certain time delay without freezing the UI thread then go to the next step
        currentPause = new PauseTransition(Duration.millis(currentDelayMs));
        currentPause.setOnFinished(e -> playGenerationStep(step + 1, totalSteps, onFinished));
        currentPause.play();
    }

    /// Initialize an empty [GridPane] with the graph's dimensions.
    private GridPane initMazeGrid(){
        assert graphMaze != null : "GraphMaze must be set before calling initMazeGrid()";

        int rows = graphMaze.getHeight();
        int cols = graphMaze.getWidth();

        GridPane grid = new GridPane();

        // Set constraints for the GridPane to preserve a view ratio depending on its number of rows and columns

        // Set column constraints (once per column)
        for (int i = 0; i < cols; i++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setPercentWidth(100.0 / cols);
            grid.getColumnConstraints().add(colConstraints);
        }

        // Set row constraints (once per row)
        for (int i = 0; i < rows; i++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setPercentHeight(100.0 / rows);
            grid.getRowConstraints().add(rowConstraints);
        }

        return grid;
    }

    /// Clears the current grid and renders the given maze in it.
    ///
    /// Doesn't replace the grid with another one. Requires a grid to be built first using [#buildGrid()].
    ///
    /// The maze must have the same number of rows and columns as the current grid.
    ///
    /// Can be useful to temporarily display a maze, such as intermediate steps, while keeping the stored
    /// maze intact.
    ///
    /// @param maze the maze to display, in array format
    public void renderMaze(ArrayMaze maze) {
        // todo: improve this design because it's frankly weird to have "renderMaze" which renders
        //       the maze under certain mysterious conditions. Ideally, I'd like a unified "renderMaze"
        //       method that configures the same grid as necessary without needing recreation or something.

        assert grid != null : "Grid must be built first before calling displayMaze()";
        assert grid.getColumnCount() == maze.getWidth() : "Grid must have the same number of columns as the maze";
        assert grid.getRowCount() == maze.getHeight() : "Grid must have the same number of rows as the maze";

        // Reset the grid's cells and build new ones
        grid.getChildren().clear();
        buildWalls(maze);
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
                StackPane gridCell = new StackPane();
                StringBuilder style = new StringBuilder();

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
                gridCell.getStyleClass().add("cell-color-default");

                //Remembers the initial style for reset
                gridCell.getProperties().put("initialStyle", style.toString());

                mazeEditor.configureCellClick(gridCell, row, col, grid);
                mazeEditor.configureCellCommands(gridCell, row, col, grid);

                // Add some padding so the icon inside the cell doesn't suddenly move
                // when an inner border is added or removed.
                // We don't care about outer borders since they are always removed on the first
                // displayed step of the generation animation.
                gridCell.setPadding(new Insets(
                        2-topWidth,
                        2-rightWidth,
                        2-bottomWidth,
                        2-leftWidth
                ));

                // Display start/end indicators if this is a start/end cell.
                if (mazeCell.endpoint() == Cell.Endpoint.START) {
                    // Display a start indicator (green arrow)
                    final double STROKE_WIDTH = 4;
                    final double ARROW_WIDTH = 9;
                    final double ARROW_HEIGHT = 20;

                    // Make the shape for the green arrow.
                    var path = new Path(
                            new MoveTo(0, 0),
                            new LineTo(ARROW_WIDTH, ARROW_HEIGHT/2),
                            new LineTo(0, ARROW_HEIGHT)
                    );
                    // Configure its stroke color and width
                    path.setStrokeWidth(STROKE_WIDTH);
                    path.setStrokeLineJoin(StrokeLineJoin.ROUND); // We like rounded corners here
                    path.setStroke(Color.GREEN);

                    // Calculate the uniform scale necessary to fit inside the cell, taking 65% of the space.
                    var scaleBinding = Bindings.min(
                            gridCell.widthProperty().divide(ARROW_WIDTH + STROKE_WIDTH),
                            gridCell.heightProperty().divide(ARROW_HEIGHT + STROKE_WIDTH)
                    ).multiply(0.65);

                    // Bind it to both X and Y scales.
                    path.scaleXProperty().bind(scaleBinding);
                    path.scaleYProperty().bind(scaleBinding);

                    // Translate the arrow to the center of the cell.
                    gridCell.getChildren().add(path);
                } else if ((mazeCell.endpoint() == Cell.Endpoint.END)) {
                    // Display an end indicator (blue rounded rectangle)

                    // Make a blue rounded rectangle.
                    var rect = new Rectangle();
                    rect.setFill(Color.DODGERBLUE);
                    rect.setArcHeight(8);
                    rect.setArcWidth(8);

                    // Calculate the uniform scale necessary to fit inside the cell, taking 75% of the space.
                    var scaleBinding = Bindings.min(
                            gridCell.widthProperty(),
                            gridCell.heightProperty()
                    ).multiply(0.65);

                    // Bind it to both X and Y scales.
                    rect.widthProperty().bind(scaleBinding);
                    rect.heightProperty().bind(scaleBinding);

                    // Add it to the cell.
                    gridCell.getChildren().add(rect);
                }

                mazeEditor.configureCellClick(gridCell, row, col, grid);
                mazeEditor.configureCellCommands(gridCell, row, col, grid);

                // Allow dynamic resizing of the cell
                GridPane.setHgrow(gridCell, Priority.ALWAYS);
                GridPane.setVgrow(gridCell, Priority.ALWAYS);
                // Force min size to be zero so the cell is entirely resized according to the available grid space,
                // and doesn't force the grid to be as large the icon the gridCell contains.
                gridCell.setMinSize(0, 0);
                gridCell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                grid.add(gridCell, col, row);
            }
        }
    }


    /// Animate the solution step by step
    /// @param totalSteps history of steps taken by the solving algorithm
    /// @param isDFS if the solving algorithm chosen is DFS, the `animateSolution` method has a different behavior
    /// @param onFinished method to execute after animation is finished
    public void animateSolution(List<Stack<Integer>> totalSteps,  boolean isDFS, Runnable onFinished){
        assert graphMaze != null : "GraphMaze must be set before calling animateDijkstraSolution()";
        assert grid != null : "Grid must be built first before calling animateDijkstraSolution()";

        clearGridColor();
        lastAnimIsGeneration = false; // for the stop button to know what end result to fast forward to

        // Reverse history of all steps in chronological order
        List<Stack<Integer>> chronologicalSteps = totalSteps.reversed();

        if(isDFS){
            // Flatten all paths into a singular one (skip final path at index 0) to animate node by node
            List<Integer> flattened = flattenPaths(chronologicalSteps.subList(1, chronologicalSteps.size()));

            // Start playing animation from the step of the DFS algorithm (first tested path) (index 0 is the final path)
            // Transmit final path for the final path animation
            playNodeByNodeDFS(0, flattened, totalSteps.getLast(), onFinished);
        } else {
            // Start playing animation from the first step of the solving algorithm (index 0 is the final path)
            playSolutionStep(1, chronologicalSteps,onFinished);
        }
    }

    /// Animate a step of the solution history, then go to the next one
    /// @param step index of the step
    /// @param totalSteps solving algorithm history of steps
    /// @param onFinished method to execute after animation is finished
    private void playSolutionStep(int step, List<Stack<Integer>> totalSteps, Runnable onFinished){
        if (step > totalSteps.size()-1) { // animation is finished, play the animation for the final path
            playFinalSolutionStep(0, totalSteps.getFirst(), onFinished); // final path must be at first index of totalSteps
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

        stopCurrentPause(); // stop previous animation if needed

        // Wait for a certain time delay without freezing the UI thread then go to the next step
        currentPause = new PauseTransition(Duration.millis(currentDelayMs));
        currentPause.setOnFinished(e -> playSolutionStep(step + 1, totalSteps, onFinished));
        currentPause.play();
    }

    /// Animate a step (node) of the solution path, then go to the next one
    /// @param step index of the solution node
    /// @param solution solution path
    /// @param onFinished method to execute after animation is finished
    private void playFinalSolutionStep(int step, Stack<Integer> solution, Runnable onFinished){
        if (step > solution.size()-1) { // animation is finished, stop the whole running animation
            if (onFinished != null) onFinished.run();
            return;
        }

        // Retrieve the current solution vertex in the solution path
        Point vertexCoordinates = graphMaze.toPoint(solution.get(step));
        Node cell = getCellNode(vertexCoordinates);
        setCellColor(cell, "cell-color-path");

        // Query the current delay (animation speed) from the supplier
        double currentDelayMs = delaySupplier != null ? delaySupplier.get() : 500;

        stopCurrentPause(); // stop previous animation if needed

        // Wait for a certain time delay without freezing the UI thread then go to the next step
        currentPause = new PauseTransition(Duration.millis(currentDelayMs));
        currentPause.setOnFinished(e -> playFinalSolutionStep(step + 1, solution, onFinished));
        currentPause.play();

        //if(onFinished != null) onFinished.run();
    }

    /// Animate a step (visited node) of the DFS algorithm, then go to the next one
    /// @param index index of the visited node
    /// @param visitedNodes list of all visited nodes flattened in order of visit
    /// @param onFinished method to execute after animation is finished
    private void playNodeByNodeDFS(int index, List<Integer> visitedNodes, Stack<Integer> solution, Runnable onFinished) {
        if (index > visitedNodes.size()-1) { // animation is finished
            playFinalSolutionStep(0, solution, onFinished);
            return;
        }

        // Retrieve first visited node, color it visited
        int visitedVertex = visitedNodes.get(index);
        Point vertexCoordinates = graphMaze.toPoint(visitedVertex);
        Node cell = getCellNode(vertexCoordinates);
        setCellColor(cell, "cell-color-visited");

        // Query the current delay (animation speed) from the supplier
        double currentDelayMs = delaySupplier != null ? delaySupplier.get() : 500;

        stopCurrentPause(); // stop previous animation if needed

        // Wait for a certain time delay without freezing the UI thread then go to the next step
        currentPause = new PauseTransition(Duration.millis(currentDelayMs));
        currentPause.setOnFinished(e -> playNodeByNodeDFS(index + 1, visitedNodes, solution, onFinished));
        currentPause.play();
    }

    /// Give the finished grid result of the step by step solving algorithm with the visited cells displayed
    /// @param totalSteps solving algorithm history of steps
    /// @param isDFS building of finished grid result has a different behavior if the chosen solving algorithm is DFS
    public void finishStepByStepSolving(List<Stack<Integer>> totalSteps,  boolean isDFS){
        assert graphMaze != null : "GraphMaze must be set before calling animateDijkstraSolution()";
        assert grid != null : "Grid must be built first before calling animateDijkstraSolution()";

        clearGridColor();

        // Reverse history of all steps in chronological order
        List<Stack<Integer>> chronologicalSteps = totalSteps.reversed();

        if(isDFS){
            // Flatten all paths into a singular one (skip final path at index 0)
            List<Integer> flattened = flattenPaths(chronologicalSteps.subList(1, chronologicalSteps.size()));
            for(int visitedVertex : flattened){ // for all visited nodes in order, color it as visited
                Point vertexCoordinates = graphMaze.toPoint(visitedVertex);
                Node cell = getCellNode(vertexCoordinates);
                setCellColor(cell, "cell-color-visited");
            }
        } else {
            // For all stack steps, retrieve the newest visited vertex (top of the stack) and color its cell visited
            for(Stack<Integer> step : chronologicalSteps){
                int visitedVertex = step.peek();
                Point vertexCoordinates = graphMaze.toPoint(visitedVertex);
                Node cell = getCellNode(vertexCoordinates);
                setCellColor(cell, "cell-color-visited");
            }
        }

        // Retrieve solution path
        Stack<Integer> solutionPath =  totalSteps.getLast();

        // Color all cells of solution path with solution color
        for(int solutionVertex : solutionPath){
            Point vertexCoordinates = graphMaze.toPoint(solutionVertex);
            Node cell = getCellNode(vertexCoordinates);
            setCellColor(cell, "cell-color-path");
        }
    }

    /// Stop current running animation
    public void stopAnimation() {
        stopCurrentPause();
    }

    /// Stop the current [PauseTransition] frame
    private void stopCurrentPause() {
        if (currentPause != null) {
            currentPause.stop();
            currentPause = null;
        }
    }

    /// Flatten a [List<Stack<Integer>>] into a simple 1-dimensional [List]
    /// Used to flatten DFS all visited paths into a singular one in order of visited nodes (no duplicates)
    /// @return a flattened 1-dimensional [List] of all visited vertex in order of visit
    /// Each node appear only once, no backtracking simulated
    private List<Integer> flattenPaths(List<Stack<Integer>> totalSteps) {
        // Convert first as a LinkedHashSet to remove duplicates while preserving order of insertion of nodes
        Set<Integer> steps = new LinkedHashSet<>();
        for (Stack<Integer> path : totalSteps) {
            // Append all visited nodes in order
            steps.addAll(path);
        }
        List<Integer> uniqueVisitedNodes = new ArrayList<>(steps);
        return uniqueVisitedNodes;
    }

    /// Access the children of the maze's [GridPane] at a specific column and row index given by [Point] coordinates.
    /// @param coordinates (col, row) coordinates of the cell to retrieve
    /// @return the JavaFX cell [Node] in the [GridPane] at the given coordinates
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

    /// Set the background color of the given cell in the [GridPane]
    /// @param node the [GridPane] node of the cell
    /// @param colorClass the CSS color class to apply on the cell. Name must start by `cell-color`.
    public void setCellColor(Node node, String colorClass){
        assert colorClass.startsWith("cell-color-") : "colorClass must start with 'cell-color-'";

        node.getStyleClass().removeIf(styleClass -> styleClass.startsWith("cell-color-"));
        node.getStyleClass().add(colorClass);
    }

    /// Clear all cells of the [GridPane] to the default background color
    public void clearGridColor(){
        for (Node node : grid.getChildren()) {
            setCellColor(node, "cell-color-default");
        }
    }

    /// Returns the maze rendered by this renderer
    /// @return the maze rendered by this renderer
    public GraphMaze getGraphMaze() {
        return graphMaze;
    }

    /// Sets the maze to be rendered by this renderer. Also updates the maze editor.
    /// @param graphMaze the maze to be rendered
    public void setGraphMaze(GraphMaze graphMaze) {
        this.graphMaze = graphMaze;
        mazeEditor.setGraphMaze(graphMaze);
    }

    /// Returns the maze generation log created while generating the rendered maze.
    ///
    /// Can be null if the maze wasn't generated (when opening a file for example).
    ///
    /// @return the maze generation log created while generating the rendered maze
    public MazeGenLog getLog() {
        return log;
    }

    /// Sets the maze generation log created while generating the rendered maze.
    /// @param log the maze generation log created while generating the rendered maze
    public void setLog(MazeGenLog log) {
        this.log = log;
    }

    /// Returns the JavaFX grid used to render the maze.
    /// @return the JavaFX grid used to render the maze
    public GridPane getGrid() {
        return grid;
    }

    /// Sets a function returning the delay in milliseconds between animation steps.
    ///
    /// Used to configure the animation speed on the fly.
    ///
    /// @param delaySupplier the function returning the delay in milliseconds between animation steps
    public void setDelaySupplier(Supplier<Double> delaySupplier) {
        this.delaySupplier = delaySupplier;
    }

    /// Returns true if the last displayed animation was a generation animation.
    /// @return true if the last displayed animation was a generation animation; false otherwise
    public boolean isLastAnimIsGeneration(){
        return lastAnimIsGeneration;
    }
}