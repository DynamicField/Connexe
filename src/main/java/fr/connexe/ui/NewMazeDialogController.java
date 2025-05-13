package fr.connexe.ui;

import fr.connexe.algo.InvalidVertexException;
import fr.connexe.algo.generation.MazeGenResult;
import fr.connexe.algo.generation.MazeGenerator;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

/// Controller for the Maze Creation Dialog box (triggered when User clicks on "New" menu item)
public class NewMazeDialogController {

    private Stage dialogStage;
    private MazeRenderer mazeRenderer;

    @FXML
    private Spinner<Integer> rowSpinner;

    @FXML
    private Spinner<Integer> colSpinner;

    @FXML
    private Spinner<Integer> startSpinner;

    @FXML
    private Spinner<Integer> endSpinner;

    @FXML
    private RadioButton primRadio;

    @FXML
    private RadioButton dfsRadio;

    private boolean okClicked = false;

    @FXML
    private void initialize() {
        // Minimum input sizes for columns and rows
        int MIN_SIZE = 2;
        int MAX_SIZE = 50;

        // Restrict input values for row and column spinners
        SpinnerValueFactory.IntegerSpinnerValueFactory rowFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_SIZE, MAX_SIZE, 10);
        SpinnerValueFactory.IntegerSpinnerValueFactory colFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_SIZE, MAX_SIZE, 10);

        rowSpinner.setValueFactory(rowFactory);
        colSpinner.setValueFactory(colFactory);

        // Compute initial total cells
        int totalCells = rowFactory.getValue() * colFactory.getValue();

        // Restrict input values for start & end vertex ids spinners
        SpinnerValueFactory.IntegerSpinnerValueFactory factoryStart =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, totalCells - 1, 0);
        SpinnerValueFactory.IntegerSpinnerValueFactory factoryEnd =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, totalCells - 1, totalCells - 1);

        startSpinner.setValueFactory(factoryStart);
        endSpinner.setValueFactory(factoryEnd);

        // Attach listener to both row/col spinners to update totalCells when user updates row or col values
        ChangeListener<Number> sizeListener = (obs, oldVal, newVal) -> updateStartEndLimits();
        rowSpinner.valueProperty().addListener(sizeListener);
        colSpinner.valueProperty().addListener(sizeListener);

        // Handle update of endSpinner value when user commits new value for cols/rows
        setupCommitOnFocusOrEnter(rowSpinner);
        setupCommitOnFocusOrEnter(colSpinner);
    }

    /// Called when a user clicks ok. Generates a maze based on the given custom parameters and closes the dialog box.
    @FXML
    private void handleOk() {
        MazeGenResult generatedMaze;

        if(primRadio.isSelected()) {
            generatedMaze = MazeGenerator.makePrim(colSpinner.getValue(), rowSpinner.getValue(), null);
        } else {
            generatedMaze = MazeGenerator.makeDFS(colSpinner.getValue(), rowSpinner.getValue(), null);
        }

        mazeRenderer.setMazeGenResult(generatedMaze);

        try{
            mazeRenderer.setEndVertex(endSpinner.getValue());
            mazeRenderer.setStartVertex(startSpinner.getValue());

        } catch(InvalidVertexException e) {
            showWarning("Entrée / Sortie invalide(s)",
                    "L'ID de l'entrée et/ou de sortie n'est/ne sont pas valide(s).\n" +
                            "Des valeurs par défaut ont été choisies.");

            // Choose default fallback values
            mazeRenderer.setStartVertex(0);
            mazeRenderer.setEndVertex(generatedMaze.maze().getNumCells() - 1);
        } finally {
            okClicked = true;
            dialogStage.close();
        }
    }

    ///  Called when a user clicks on the Cancel button
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

    ///  To show an alert/warning dialog box
    /// @param title - title of the box
    /// @param content - warning message inside the box
    private void showWarning(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        // Wait so the user sees the message
        alert.showAndWait();
    }

    /// Is called when user clicks on the button to use the first cell (0) as the entry of the maze
    @FXML
    private void handleFirstVertexBtn(){
        startSpinner.getValueFactory().setValue(0);
    }

    /// Is called when user clicks on the button to use the last cell (rows*cols-1) as the exit of the maze
    @FXML
    private void handleLastVertexBtn(){
        endSpinner.getValueFactory().setValue(rowSpinner.getValue() * colSpinner.getValue() - 1);
    }


    ///  Force commit user input from a spinner's TextField to that spinner
    /// @param spinner - the spinner in which we want to listen to user inputs
    private void commitEditorText(Spinner<Integer> spinner) {
        if (!spinner.isEditable()) return;

        // Retrieve text entered in Spinner (if user enters value in the TextField instead of using up/down arrows)
        String text = spinner.getEditor().getText();
        SpinnerValueFactory<Integer> valueFactory = spinner.getValueFactory();

        if (valueFactory != null) {
            try {
                int value = Integer.parseInt(text);
                valueFactory.setValue(value); // force commit the input as the spinner's value
            } catch (NumberFormatException e) {
                // Reset to current value if invalid input
                spinner.getEditor().setText(String.valueOf(spinner.getValue()));
            }
        }
    }

    /// If the user types a value in a spinner, forces JavaFX to commit that value to the spinner’s value property when :
    /// * The user presses Enter, or
    /// * The spinner loses focus (clicks elsewhere)
    /// @param spinner - the spinner in which we want to listen to user inputs
    private void setupCommitOnFocusOrEnter(Spinner<Integer> spinner) {
        spinner.getEditor().setOnAction(e -> commitEditorText(spinner));
        spinner.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                commitEditorText(spinner);
            }
        });
    }


    /// Recalculates current size of the maze based on the user's inputs for the row/col spinners.
    /// Updates the limit of the possible id values for start/end vertices.
    private void updateStartEndLimits() {
        int rows = rowSpinner.getValue();
        int cols = colSpinner.getValue();
        int total = rows * cols;

        SpinnerValueFactory.IntegerSpinnerValueFactory startFactory =
                (SpinnerValueFactory.IntegerSpinnerValueFactory) startSpinner.getValueFactory();
        SpinnerValueFactory.IntegerSpinnerValueFactory endFactory =
                (SpinnerValueFactory.IntegerSpinnerValueFactory) endSpinner.getValueFactory();

        startFactory.setMax(total - 1);
        endFactory.setMax(total - 1);

        if (startSpinner.getValue() > total - 1) {
            startFactory.setValue(0);
        }
        if (endSpinner.getValue() > total - 1) {
            endFactory.setValue(total - 1);
        }
    }
}
