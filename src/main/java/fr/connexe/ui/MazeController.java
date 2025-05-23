package fr.connexe.ui;

import fr.connexe.algo.GraphMaze;
import fr.connexe.algo.MazeSerializationException;
import fr.connexe.algo.Point;
import fr.connexe.ui.game.GameStartConfig;
import fr.connexe.ui.game.IncompatibleMazeException;
import fr.connexe.ui.game.input.ControllerHub;
import fr.connexe.ui.game.GameSession;
import fr.connexe.ui.game.input.KeyboardHub;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import org.jetbrains.annotations.Nullable;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;

///  Controller to display any Maze related operations on the view (creation, editing, solving, etc...)
///
/// Also handles game sessions displayed on top of the maze, with the [#beginGame(fr.connexe.ui.game.GameStartConfig)]
/// and [#stopGame()] methods.
public class MazeController {
    // --- Maze algorithms state ---
    private MazeRenderer mazeRenderer;
    private List<Stack<Integer>> stepByStepPath;
    private boolean isDFS; // necessary for solving animation method having a different behavior for DFS

    // --- Gaming state ---
    private KeyboardHub keyboardHub; // Receives keyboard input
    private @Nullable ControllerHub controllerHub; // Receives controller input; can be null
    private @Nullable GameSession gameSession; // The current game session displayed on top of the maze.
    private final BooleanProperty gameRunning = new SimpleBooleanProperty(); // Indicates if the game's running

    @FXML
    private VBox vboxLayout;

    private StackPane root; // Root element of the vboxLayout
    private VBox statsContainer;

    /// Creates a new MazeController.
    ///
    /// This constructor is used by JavaFX when loading the FXML file.
    public MazeController() {}

    ///  Display a maze in the VBox of the main scene. If an arcade game is running, stops it immediately.
    public void createMazeFX(){
        // Stop the current game session if we already have one.
        stopGame();

        vboxLayout.getChildren().clear();

        // Build the maze for the current renderer and retrieve its grid to add it to the view
        mazeRenderer.buildGrid();
        GridPane dynamicGrid = mazeRenderer.getGrid();

        // Wrap the grid in a StackPane to center and constrain its size
        root = new StackPane();
        root.getChildren().add(dynamicGrid);

        // Add margin for the root StackPane, and make it grow to fill the VBox as much as possible
        VBox.setMargin(root, new Insets(10));
        VBox.setVgrow(root, Priority.ALWAYS);

        // Add the root StackPane to the VBox
        vboxLayout.getChildren().add(root);

        // Console view
        System.out.println(mazeRenderer.getGraphMaze());
    }

    /// Replays the generation of the maze step by step as an animation
    /// @param delaySupplier supplier to provide the delay between frames (in ms) (= speed) dynamically on demand,
    /// as the speed might change during the animation so it is necessary to query it each time
    /// @param onFinished piece of code to run later when the animation is finished.
    /// Used to re-enable buttons.
    public void playStepByStepGeneration(Supplier<Double> delaySupplier, Runnable onFinished){
        if(mazeRenderer.getLog() != null) {
            stopGame(); // Stop the ongoing game session if we're currently playing.
            mazeRenderer.setDelaySupplier(delaySupplier);
            mazeRenderer.animateGridBuild(onFinished);
        }
    }

    /// Build the solution path of the selected algorithm in the [SolveMazeController]
    /// Display the stats of the chosen solving algorithm
    /// (solving algorithm execution time, number of cells in path, number of visited cells)
    /// @param executionTime execution time of the solving method
    public void buildSolutionPath(long executionTime){
        GraphMaze maze = mazeRenderer.getGraphMaze();

        // Clear maze view beforehand
        mazeRenderer.clearGridColor();
        vboxLayout.getChildren().removeIf(node -> node instanceof VBox);

        // Solution path must always be at the end of the step by step list
        Stack<Integer> solutionPath = stepByStepPath.getLast();

        // Color each cell of the solution path
        for(int i : solutionPath) {
            Point nodeCoordinates = maze.toPoint(i);
            Node cell = mazeRenderer.getCellNode(nodeCoordinates);
            mazeRenderer.setCellColor(cell, "cell-color-path");
        }

        double executionTimeMs = executionTime / 1000000.0;

        statsContainer = new VBox();
        statsContainer.setAlignment(Pos.CENTER);

        // Display stats (solving algorithm execution time, number of cells in path, number of visited cells)
        Label timeLabel = new Label("Temps de résolution (algorithme seulement) : " + executionTimeMs + " Ms");
        Label pathLength = new Label("Cases du chemin final : " + solutionPath.size());
        Label visitedLength = new Label("Cases visitées : " + getUniqueVisitedNodeCount(stepByStepPath));
        statsContainer.getChildren().addAll(timeLabel, pathLength, visitedLength);
        vboxLayout.getChildren().add(statsContainer);
    }

    /// Play step by step animation of the solving algorithm
    /// @param delaySupplier supplier to provide the delay between frames (in ms) (= speed) dynamically on demand,
    /// as the speed might change during the animation so it is necessary to query it each time
    /// @param onFinished piece of code to run later when the animation is finished.
    /// Used to re-enable buttons.
    public void playStepByStepSolution(Supplier<Double> delaySupplier, Runnable onFinished){
        assert stepByStepPath != null : "StepByStepPath must be set before calling playStepByStepSolution()";

        stopGame(); // Stop the ongoing game session if we're currently playing.
        mazeRenderer.setDelaySupplier(delaySupplier);
        mazeRenderer.animateSolution(stepByStepPath, isDFS, onFinished);
    }

    /// End the current running animation and display the end view
    /// If the current running animation was the maze generation, display the end maze
    /// If the current running animation was the solving, show the end result with the path found and visited cells colored
    public void endCurrentAnimation() {
        mazeRenderer.stopAnimation();
        resetMaze();
    }

    // Resets the maze to its "idle" state, the state where no animation is running, and no game's playing.
    private void resetMaze() {
        if (mazeRenderer.isLastAnimIsGeneration() || stepByStepPath == null) {
            createMazeFX(); // Rebuild generated grid as it was by default
        } else {
            // Rebuild grid with end state of animation (visited cells + final path)
            mazeRenderer.finishStepByStepSolving(stepByStepPath, isDFS);
        }

        if (statsContainer != null && statsContainer.getParent() == null) {
            // Bring back the solving stats if they were cleared by createMazeFX
            vboxLayout.getChildren().add(statsContainer);
        }
    }

    /// Saves the current rendered maze into a file
    /// @param file file to save the maze to
    /// @throws MazeSerializationException when the maze failed to be serialized
    /// @throws IOException when the file can't be written
    public void saveMaze(File file) throws MazeSerializationException, IOException {
        assert mazeRenderer.getGraphMaze() != null : "MazeRenderer must have a maze to be saved";

        // Open an output stream with the given file, then close it automatically
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            mazeRenderer.getGraphMaze().save(fileOutputStream);
        }
    }

    /// Loads a file containing maze data and renders it on the view
    /// @param file file to load in the view
    /// @throws MazeSerializationException when the maze failed to be serialized
    /// @throws IOException when the file can't be written
    public void loadMaze(File file) throws MazeSerializationException, IOException {
        // Open an input stream with the given file, then close it automatically
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            GraphMaze maze = GraphMaze.load(fileInputStream);
            mazeRenderer.setGraphMaze(maze);
            mazeRenderer.setLog(null); // remove log of previous generation
            stepByStepPath = null; // Remove the step by step path of the previous maze.
            createMazeFX();
        }
    }

    /// Begins a new game session with the current maze.
    ///
    /// @param config players and chosen game modes for the game
    /// @throws IllegalStateException when there's no maze loaded
    /// @throws IncompatibleMazeException when the maze's topology is incompatible with the given game mode
    ///                                   (no path from A to B)
    public void beginGame(GameStartConfig config) throws IncompatibleMazeException {
        // Do various checks.
        if (mazeRenderer.getGraphMaze() == null) {
            throw new IllegalStateException("MazeRenderer must have a maze to start a game");
        }
        if (gameSession != null) {
            throw new IllegalStateException("Game session already started");
        }

        assert root != null; // We have a root since we have a maze loaded

        // Refresh the current maze to clear off any resolved path (that would be cheating!)
        createMazeFX();

        // Create and deploy a game session.
        gameSession = new GameSession(config, mazeRenderer.getGraphMaze(), keyboardHub, controllerHub,
                this::stopGame, mazeRenderer::renderMaze);
        gameSession.deploy(root, vboxLayout);
        gameRunning.set(true);
    }

    /// Stops the ongoing game session and removes the overlay. Does nothing if no game's running.
    public void stopGame() {
        if (gameSession != null) {
            // Undeploy the current game session and reset everything.
            gameSession.undeploy();
            gameSession = null;
            gameRunning.set(false);

            // Reset the maze to the pre-game state. If we had a generation path on screen before player,
            // this will make it come back.
            resetMaze();
        }
    }

    /// Sets keyboard and controller hubs to receive input from. Must be called when preparing the maze controller.
    ///
    /// @param keyboardHub keyboard hub to receive input from
    /// @param controllerHub controller hub to receive input from
    public void setInputHubs(KeyboardHub keyboardHub, ControllerHub controllerHub) {
        this.keyboardHub = keyboardHub;
        this.controllerHub = controllerHub;
    }

    /// Sets the maze renderer to use for this controller. Called every time a maze is created or loaded.
    /// @param mazeRenderer the maze renderer to use
    public void setMazeRenderer(MazeRenderer mazeRenderer) {
        this.mazeRenderer = mazeRenderer;
        stepByStepPath = null; // Remove the step by step path of the previous maze.
    }

    /// Returns the maze renderer used by this controller.
    /// @return the maze renderer used by this controller
    public MazeRenderer getMazeRenderer() {
        return mazeRenderer;
    }

    /// Returns the last computed step-by-step path created by a maze-solving algorithm.
    /// @return the last computed step-by-step path
    public List<Stack<Integer>> getStepByStepPath() {
        return stepByStepPath;
    }

    /// Sets the step-by-step path created by a maze-solving algorithm.
    /// @param stepByStepPath the step-by-step path to set
    public void setStepByStepPath(List<Stack<Integer>> stepByStepPath) {
        this.stepByStepPath = stepByStepPath;
    }

    /// Sets whether or not the last [step-by-step path][#setStepByStepPath(List)] was computed using a DFS algorithm.
    /// @param DFS true when the last algo was a DFS algorithm, false otherwise
    public void setDFS(boolean DFS) {
        isDFS = DFS;
    }

    /// Returns a property indicating if a game's currently running or not.
    ///
    /// @return a property indicating if a game's currently running or not.
    public BooleanProperty gameRunningProperty() {
        return gameRunning;
    }

    /// Returns whether a game is currently running or not.
    ///
    /// @return true if a game is currently running, false otherwise.
    public boolean isGameRunning() {
        return gameRunning.get();
    }

    /// Flatten a [List<Stack<Integer>>] to a [Set<Integer>]
    /// and get the total number of visited nodes of a step by step algorithm
    /// @param stepByStepPath the list of stacks (paths) to flatten
    /// @return total number of visited nodes of the `List<Stack<Integer>>` step by step solving history
    public int getUniqueVisitedNodeCount(List<Stack<Integer>> stepByStepPath) {
        Set<Integer> visited = new HashSet<>();
        for (Stack<Integer> stack : stepByStepPath) {
            visited.addAll(stack);
        }
        return visited.size();
    }
}
