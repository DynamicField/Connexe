package fr.connexe.ui;

import fr.connexe.algo.GraphMaze;
import fr.connexe.algo.MazeSerializationException;
import fr.connexe.algo.Point;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.*;
import java.util.List;
import java.util.Stack;
import java.util.function.Supplier;

///  Controller to display any Maze related operations on the view (creation, editing, solving, etc...)
public class MazeController {

    private MazeRenderer mazeRenderer;
    private List<Stack<Integer>> stepByStepPath;
    private boolean isDFS; // necessary for solving animation method having a different behavior for DFS

    @FXML
    private VBox vboxLayout;

    ///  Display a maze in the VBox of the main scene
    public void createMazeFX(){
        vboxLayout.getChildren().clear();

        // Build the maze for the current renderer and retrieve its grid to add it to the view
        mazeRenderer.buildGrid();
        GridPane dynamicGrid = mazeRenderer.getGrid();

        // Wrap the grid in a StackPane to center and constrain its size
        StackPane root = new StackPane();
        root.getChildren().add(dynamicGrid);

        // Add margin space to the grid inside the StackPane
        StackPane.setMargin(dynamicGrid, new Insets(20)); // 20 px margin on all sides

        // Bind GridPane size to VBox size minus margin (for dynamic growth)
        dynamicGrid.maxWidthProperty().bind(vboxLayout.widthProperty().subtract(40));
        dynamicGrid.maxHeightProperty().bind(vboxLayout.heightProperty().subtract(40));
        dynamicGrid.prefWidthProperty().bind(vboxLayout.widthProperty().subtract(40));
        dynamicGrid.prefHeightProperty().bind(vboxLayout.heightProperty().subtract(40));

        dynamicGrid.setAlignment(Pos.CENTER);
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
            mazeRenderer.setDelaySupplier(delaySupplier);
            mazeRenderer.animateGridBuild(onFinished);
        }
    }

    public void buildSolutionPath(Stack<Integer> path, long executionTime){
        GraphMaze maze = mazeRenderer.getGraphMaze();
        mazeRenderer.clearGridColor();
        vboxLayout.getChildren().removeIf(node -> node instanceof Label);

        for(int i : path) {
            Point nodeCoordinates = maze.toPoint(i);
            Node cell = mazeRenderer.getCellNode(nodeCoordinates);
            mazeRenderer.setCellColor(cell, "cell-color-path");
        }

        double executionTimeMs = executionTime / 1000000.0;

        Label timeLabel = new Label("Temps de résolution (algorithme seulement) : " + executionTimeMs + " Ms");
        Label pathLength = new Label("Cases du chemin final : " + path.size());
        vboxLayout.getChildren().add(timeLabel);
        vboxLayout.getChildren().add(pathLength);
    }

    public void playStepByStepSolution(Supplier<Double> delaySupplier, Runnable onFinished){
        assert stepByStepPath != null : "StepByStepPath must be set before calling playStepByStepSolution()";

        mazeRenderer.setDelaySupplier(delaySupplier);
        mazeRenderer.animateSolution(stepByStepPath, isDFS, onFinished);
    }

    /// Saves the current rendered maze into a file
    public void saveMaze(File file) throws MazeSerializationException, IOException {
        assert mazeRenderer.getGraphMaze() != null : "MazeRenderer must have a maze to be saved";

        // Open an output stream with the given file, then close it automatically
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            mazeRenderer.getGraphMaze().save(fileOutputStream);
        }
    }

    /// Loads a file containing maze data and renders it on the view
    public void loadMaze(File file) throws MazeSerializationException, IOException {
        // Open an input stream with the given file, then close it automatically
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            GraphMaze maze = GraphMaze.load(fileInputStream);
            mazeRenderer.setGraphMaze(maze);
            mazeRenderer.setLog(null); // remove log of previous generation
            createMazeFX();
        }
    }

    public void setMazeRenderer(MazeRenderer mazeRenderer) {
        this.mazeRenderer = mazeRenderer;
    }

    public MazeRenderer getMazeRenderer() {
        return mazeRenderer;
    }

    public List<Stack<Integer>> getStepByStepPath() {
        return stepByStepPath;
    }

    public void setStepByStepPath(List<Stack<Integer>> stepByStepPath) {
        this.stepByStepPath = stepByStepPath;
    }

    public void setDFS(boolean DFS) {
        isDFS = DFS;
    }
}
