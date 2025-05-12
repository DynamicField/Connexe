package fr.connexe.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;

public class NewMazeDialogController {

    private Stage dialogStage;
    private LabyrinthClass maze;


    @FXML
    private Spinner<Integer> rowSpinner;

    @FXML
    private Spinner<Integer> colSpinner;

    private boolean okClicked = false;

    @FXML
    private void initialize() {
        int MIN_SIZE = 2;
        int MAX_SIZE = 50;

        SpinnerValueFactory<Integer> factory1 = new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_SIZE, MAX_SIZE, 1);
        SpinnerValueFactory<Integer> factory2 = new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_SIZE, MAX_SIZE, 1);
        factory1.setValue(10);
        factory2.setValue(10);
        rowSpinner.setValueFactory(factory1);
        colSpinner.setValueFactory(factory2);
    }

    /**
     * Called when the user clicks ok.
     */
    @FXML
    private void handleOk() {
        maze.setCols(colSpinner.getValue());
        maze.setRows(rowSpinner.getValue());
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

    public void setMaze(LabyrinthClass maze) {
        this.maze = maze;
    }
}
