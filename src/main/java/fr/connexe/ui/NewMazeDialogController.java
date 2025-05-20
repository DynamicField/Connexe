package fr.connexe.ui;

import fr.connexe.algo.InvalidVertexException;
import fr.connexe.algo.generation.Endpoints;
import fr.connexe.algo.generation.MazeGenResult;
import fr.connexe.algo.generation.MazeGenerator;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import static fr.connexe.algo.generation.MazeGenerator.introduceChaos;

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
    private RadioButton seedRadio;

    @FXML
    private Spinner<Integer> seedSpinner;

    @FXML
    private CheckBox perfectMazeCheckBox;

    @FXML
    private Label chaosPercentageLabel;

    @FXML
    private Spinner<Double> chaosPercentageSpinner;

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

        // Restrict input values for seed spinner
        SpinnerValueFactory.IntegerSpinnerValueFactory seedFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
        seedSpinner.setValueFactory(seedFactory);

        // Enable / Disable input field for seed depending on whether radio button for seed is selected or not
        seedRadio.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            seedSpinner.setDisable(!isNowSelected);
        });

        // Restrict input values for chaos spinner
        SpinnerValueFactory.DoubleSpinnerValueFactory chaosFactory =
                new SpinnerValueFactory.DoubleSpinnerValueFactory(0.00, 1.00, 0.20, 0.01);
        chaosPercentageSpinner.setValueFactory(chaosFactory);

        // If perfect maze is checked, disable custom probability for chaos. Else, enable custom probability for chaos.
        perfectMazeCheckBox.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            chaosPercentageSpinner.setDisable(isNowSelected);
            chaosPercentageLabel.setDisable(isNowSelected);
        });
    }

    /// Called when a user clicks ok.
    /// Generates a maze based on the given custom parameters and closes the dialog box.
    @FXML
    private void handleOk() {
        MazeGenResult generatedMaze = null;
        Long seed = null;

        // Retrieve seed value if generation with seed is selected
        if(seedRadio.isSelected()) {
            seed = seedSpinner.getValue().longValue();
        }

        try{
            // Setup entry/exit of maze with given vertex IDs
            Endpoints endpoints = new Endpoints(startSpinner.getValue(), endSpinner.getValue());

            // Use the chosen generation method depending on the selected radio button
            if(primRadio.isSelected()) {
                generatedMaze = MazeGenerator.makePrim(colSpinner.getValue(), rowSpinner.getValue(), endpoints, seed);
            } else {
                generatedMaze = MazeGenerator.makeDFS(colSpinner.getValue(), rowSpinner.getValue(), endpoints, seed);
            }

            mazeRenderer.setGraphMaze(generatedMaze.maze());
            mazeRenderer.setLog(generatedMaze.log());

        } catch(InvalidVertexException e) {
            showWarning("Entrée / Sortie invalide(s)",
                    "L'ID de l'entrée et/ou de sortie n'est/ne sont pas valide(s).\n" +
                            "Des valeurs par défaut ont été choisies.");

            // No endpoints specified = default fallback values set by the generation methods (start = 0; end = n-1)
            if(primRadio.isSelected()) {
                generatedMaze = MazeGenerator.makePrim(colSpinner.getValue(), rowSpinner.getValue(), null, seed);
            } else {
                generatedMaze = MazeGenerator.makeDFS(colSpinner.getValue(), rowSpinner.getValue(), null, seed);
            }

            mazeRenderer.setGraphMaze(generatedMaze.maze());
            mazeRenderer.setLog(generatedMaze.log());

        } finally {
            // Introduce chaos to perfect maze to make it non-perfect if checkbox isn't selected
            // Required to do AFTER setting the start and end
            if(!perfectMazeCheckBox.isSelected()) {
                MazeGenResult mazeGenResult = new MazeGenResult(mazeRenderer.getGraphMaze(), mazeRenderer.getLog());
                introduceChaos(mazeGenResult, chaosPercentageSpinner.getValue().floatValue(), seed);
                mazeRenderer.setGraphMaze(mazeGenResult.maze());
                mazeRenderer.setLog(mazeGenResult.log());
            }
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
    /// @param title title of the box
    /// @param content warning message inside the box
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
    /// @param spinner the spinner in which we want to listen to user inputs
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
    /// @param spinner the spinner in which we want to listen to user inputs
    private void setupCommitOnFocusOrEnter(Spinner<Integer> spinner) {
        spinner.getEditor().setOnAction(e -> commitEditorText(spinner)); // on enter

        // On loss of focus :
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

        // Recalculate maximum possible vertex IDs with the new total of cells in the maze
        startFactory.setMax(total - 1);
        endFactory.setMax(total - 1);

        // Clamp start/end values to the valid range
        int clampedStart = Math.max(0, Math.min(startSpinner.getValue(), total - 1));
        int clampedEnd = Math.max(0, Math.min(endSpinner.getValue(), total - 1));
        startFactory.setValue(clampedStart);
        endFactory.setValue(clampedEnd);
    }
}
