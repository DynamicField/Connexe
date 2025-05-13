package fr.connexe.ui;

import fr.connexe.algo.GraphMaze;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;

public class NewMazeDialogController {

    private Stage dialogStage;
    private MazeRenderer mazeRenderer;

    @FXML
    private Spinner<Integer> rowSpinner;

    @FXML
    private Spinner<Integer> colSpinner;

    private boolean okClicked = false;

    @FXML
    private void initialize() {
        // Minimum input sizes for columns and rows
        int MIN_SIZE = 2;
        int MAX_SIZE = 50;

        // Restrict the Spinner inputs to take numbers in the given range for columns and rows
        SpinnerValueFactory<Integer> factory1 = new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_SIZE, MAX_SIZE, 1);
        SpinnerValueFactory<Integer> factory2 = new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_SIZE, MAX_SIZE, 1);
        factory1.setValue(10);
        factory2.setValue(10);
        rowSpinner.setValueFactory(factory1);
        colSpinner.setValueFactory(factory2);
    }

    /// Called when a user clicks ok. Generates a maze based on the given custom parameters and closes the dialog box.
    @FXML
    private void handleOk() {
        GraphMaze maze = new GraphMaze(colSpinner.getValue(), rowSpinner.getValue());
        mazeRenderer.setGraphMaze(maze);
        okClicked = true;
        dialogStage.close();
    }

    /**
     * Called when the user clicks cancel.
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setMazeRenderer(MazeRenderer mazeRenderer) {
        this.mazeRenderer = mazeRenderer;
    }
}
