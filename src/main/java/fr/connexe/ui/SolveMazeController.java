package fr.connexe.ui;

import fr.connexe.algo.MazeSolver;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.stage.Stage;

import java.util.List;
import java.util.Stack;

public class SolveMazeController {
    private Stage dialogStage;
    private MazeController mazeController;
    boolean okClicked = false;

    @FXML
    private RadioButton dijkstraRadio;

    @FXML
    private RadioButton dfsRadio;

    @FXML
    private RadioButton clockwiseRadio;

    ///  Called when a user clicks on the Solve button
    @FXML
    private void handleOk() {
        okClicked = true;

        // Contains steps in reverse chronological order + final path at last
        // Index 0 : final step (everything visited)
        // Index n-1 : first step
        // Index n : final solution path
        List<Stack<Integer>> stepByStepPath;

        long startTime;
        long endTime;

        mazeController.setDFS(false); // reset

        // Check which radio button was selected for solving method,
        // and executes the solving algorithm to pass the solution to MazeController
        if(dijkstraRadio.isSelected()) { // Solve for Dijkstra
            startTime = System.nanoTime();
            stepByStepPath = MazeSolver.solveDijkstra2(mazeController.getMazeRenderer().getGraphMaze());
            endTime = System.nanoTime();
            //solutionPath = MazeSolver.solveDijkstra(mazeController.getMazeRenderer().getGraphMaze());
        }
        else if (dfsRadio.isSelected()) { // Solve for DFS
            startTime = System.nanoTime();
            stepByStepPath = MazeSolver.prepDFS2(mazeController.getMazeRenderer().getGraphMaze());
            endTime = System.nanoTime();
            mazeController.setDFS(true); // necessary for differenciated behavior of DFS animation
        }
        else if (clockwiseRadio.isSelected()){ // Solve for Clockwise
            startTime = System.nanoTime();
            stepByStepPath = MazeSolver.prepClockwise2(mazeController.getMazeRenderer().getGraphMaze());
            endTime = System.nanoTime();
        }
        else { // Solve for Left-Hand
            startTime = System.nanoTime();
            stepByStepPath = MazeSolver.prepLeftHand2(mazeController.getMazeRenderer().getGraphMaze());
            endTime = System.nanoTime();
        }

        // Build the solution path
        mazeController.setStepByStepPath(stepByStepPath);
        long executionTime = endTime - startTime;
        mazeController.buildSolutionPath(executionTime);
        System.out.println("Solution path : " + stepByStepPath.getLast());

        dialogStage.close();
    }

    ///  Called when a user clicks on the Cancel button
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setMazeController(MazeController mazeController) {
        this.mazeController = mazeController;
    }

    public boolean isOkClicked() {
        return okClicked;
    }
}
