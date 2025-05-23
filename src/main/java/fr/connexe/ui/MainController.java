package fr.connexe.ui;

import fr.connexe.ConnexeApp;
import fr.connexe.ui.game.IncompatibleMazeException;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.stage.FileChooser;
import javafx.scene.control.MenuItem;

import java.io.File;
import java.io.IOException;

/// The controller of the main layout (menu bar, and overall ui). Is created automatically by the `hello-view.xml` FXML file.
public class MainController {

    private ConnexeApp connexeApp;
    private MazeController mazeController;

    @FXML
    private Button genButton;

    @FXML
    private Slider speedSlider;

    @FXML
    private Label speedLabel;

    @FXML
    private MenuItem change;

    @FXML
    private Button solveButton;

    @FXML
    private Button stopButton;

    @FXML
    private Button arcadeButton;

    private IntegerProperty animationSpeed;

    @FXML
    private void initialize() {
        // Default animation delay between frames is 500ms (x1 speed)
        // IntegerProperty allows to dynamically check its value changes and update the animation accordingly
        animationSpeed = new SimpleIntegerProperty(500);

        // Listen to slider value changes to update the animation delay
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            speedLabel.setText("x" + newVal.intValue());
            switch(newVal.intValue()){
                case 2:
                    animationSpeed.set(300);
                    break;
                case 3:
                    animationSpeed.set(100);
                    break;
                case 4:
                    animationSpeed.set(50);
                    break;
                case 5:
                    animationSpeed.set(20);
                    break;
                case 6:
                    animationSpeed.set(10);
                    break;
                case 7:
                    animationSpeed.set(5);
                    break;
                case 8:
                    animationSpeed.set(1);
                    break;
                default:
                    animationSpeed.set(500);
                    break;
            }
        });
    }

    /// Option to create a new maze.
    /// Will build a MazeRenderer to take on a generated maze from user custom parameters,
    /// then render the resulting maze into the view
    @FXML
    private void handleNew() throws IOException {
        // Initialize a renderer taking a maze generated from user parameters through the creation dialog box
        MazeRenderer mazeRenderer = new MazeRenderer();
        boolean okClicked = connexeApp.showNewMazeDialog(mazeRenderer);
        if (okClicked) { // Maze is generated, now query the controller to display it on the view
            mazeController.setMazeRenderer(mazeRenderer);
            genButton.setDisable(false);
            solveButton.setDisable(true);
            change.setDisable(false);
            arcadeButton.setDisable(false); // Make Arcade available for this loaded maze.

            // Create the maze grid on the view (also displays the maze in the console)
            mazeController.createMazeFX();
            System.out.println(mazeController.getMazeRenderer().getLog()); // show generation logs in console

            connexeApp.updateStageTitle(null);
            connexeApp.setMazeFilePath(null);
        }
    }

    /// Opens a FileChooser to let the user select a maze file to load.
    @FXML
    private void handleOpen() {
        // Init the FileChooser and retrieve the selected file
        FileChooser fileChooser = initFileChooser();
        fileChooser.setTitle("Ouvrir un labyrinthe");

        // Retrieve selected file
        File selected = fileChooser.showOpenDialog(connexeApp.getStage());

        if (selected != null) {
            // update the last opened directory (persists across sessions)
            Settings.setLastVisitedDirectory(selected.getParentFile());
            try{
                // Initialize a renderer and give the loaded maze to the renderer to display on the view
                MazeRenderer mazeRenderer = new MazeRenderer();
                mazeController.setMazeRenderer(mazeRenderer);
                mazeController.loadMaze(selected);
                genButton.setDisable(true); // Disable generation animation for opened files (no gen log)
                solveButton.setDisable(true);
                arcadeButton.setDisable(false); // Make Arcade available for this loaded maze.

                // Update current opened file path in the app
                connexeApp.setMazeFilePath(selected);
                connexeApp.updateStageTitle(selected.getName());
            } catch(Exception e){
                showError("Erreur lors du chargement du labyrinthe", e.getMessage());
            }
        }
    }

    /// Saves the currently opened maze file
    /// If the current maze has never been saved before, the "save as" dialog is shown.
    @FXML
    private void handleSave() {
        File mazeFile = connexeApp.getMazeFilePath();
        if (mazeFile != null) { // If file has already been saved once, just save over it
            try{
                mazeController.saveMaze(mazeFile);
            } catch(Exception e){
                showError("Erreur lors de la sauvegarde du labyrinthe", e.getMessage());
            }
        } else { // else, act as "Save as..." option
            handleSaveAs();
        }
    }

    /// Opens a FileChooser to let the user save a maze into a .con file
    @FXML
    private void handleSaveAs() {
        setMazeEditor(false);
        if(mazeController.getMazeRenderer() != null){
            // Init the FileChooser and retrieve the file
            FileChooser fileChooser = initFileChooser();
            fileChooser.setTitle("Enregistrer le labyrinthe");

            // Show save file dialog on top of stage
            File mazeFile = fileChooser.showSaveDialog(connexeApp.getStage());

            if (mazeFile != null) {
                // Make sure it has the correct extension
                if (!mazeFile.getPath().endsWith(".con")) {
                    mazeFile = new File(mazeFile.getPath() + ".con");
                }
                Settings.setLastVisitedDirectory(mazeFile.getParentFile());
                try{
                    // Save the maze to the selected file
                    mazeController.saveMaze(mazeFile);

                    // Update the app title with the current saved file
                    connexeApp.setMazeFilePath(mazeFile);
                    connexeApp.updateStageTitle(mazeFile.getName());
                } catch (Exception e) {
                    showError("Erreur lors de la sauvegarde du labyrinthe", e.getMessage());
                }
            }
        } else {
            showError("Aucun labyrinthe créé", "Veuillez créer un labyrinthe avant de le sauvegarder.");
        }
    }

    /// Enables the maze editor mode. Content moved in setMazeEditor() for better reusability
    public void handleChange(){
        setMazeEditor(true);
    }

    /// Enables the maze editor mode
    /// @param editMode true to enable the maze editor, false to disable it
    public void setMazeEditor(boolean editMode) {
        MazeRenderer renderer = this.mazeController.getMazeRenderer();
        if (renderer == null) {
            return;
        }

        MazeEditor mazeEditor = this.mazeController.getMazeRenderer().getMazeSelector();
        mazeEditor.setEditMode(editMode);
        if(!editMode){
            javafx.scene.layout.GridPane grid = this.mazeController.getMazeRenderer().getGrid();
            mazeEditor.clearAllRedBorders(grid);
        }
    }

    ///  Closes the app
    @FXML
    private void handleExit() {
        System.exit(0);
    }

    /// Menu action to solve a maze. When clicked, shows the dialog box with the solving algorithm options for the user to select.
    @FXML
    private void handleSolve() throws IOException {
        setMazeEditor(false);
        if(mazeController.getMazeRenderer() != null){
            boolean okClicked = connexeApp.showSolveMazeDialog(mazeController);
            if(okClicked){
                solveButton.setDisable(false);
            }
        } else {
            showError("Aucun labyrinthe", "Veuillez créer/ouvrir un labyrinthe avant de le résoudre.");
        }
    }

    /// Building button to show the generation step by step animation, for newly created mazes
    @FXML
    private void handleGenerationAnimation(){
        setMazeEditor(false);
        if(mazeController.getMazeRenderer() != null){
            // Disable buttons when playing animation to prevent unwanted behaviors
            genButton.setDisable(true);
            solveButton.setDisable(true);
            stopButton.setDisable(false); // enable stop button to stop animation
            arcadeButton.setDisable(true); // Disable arcade button during animation

            // Pass a dynamic delay supplier, so the renderer can query it during animation to change speed
            mazeController.playStepByStepGeneration(() -> (double) animationSpeed.get(), () -> {
                genButton.setDisable(false); // re-enable button when animation is finished
                if(mazeController.getStepByStepPath() != null){
                    solveButton.setDisable(false); // re-enable solve button too if user already used a solving algorithm once
                }
                stopButton.setDisable(true); // animation is finished, disable stop button
                arcadeButton.setDisable(false); // Animation done, re-enable arcade button
            });
        } else {
            showError("Aucun labyrinthe créé", "Veuillez créer un labyrinthe avant de visualiser la génération pas à pas.");
        }
    }

    /// Building button to show the generation step by step animation, for newly created mazes
    @FXML
    private void handleSolveAnimation(){
        setMazeEditor(false);
        if(mazeController.getMazeRenderer() != null && mazeController.getStepByStepPath() != null){
            // Disable buttons when playing animation to prevent unwanted behaviors
            genButton.setDisable(true);
            solveButton.setDisable(true);
            stopButton.setDisable(false); // enable stop button to stop animation
            arcadeButton.setDisable(true); // Disable arcade button during animation

            // Pass a dynamic delay supplier, so the renderer can query it during animation to change speed
            mazeController.playStepByStepSolution(() -> (double) animationSpeed.get(), () -> {
                if(mazeController.getMazeRenderer().getLog() != null){
                    genButton.setDisable(false);  // if maze was generated, re-enable generation animation button
                }
                solveButton.setDisable(false);// re-enable button when animation is finished
                stopButton.setDisable(true); // animation is finished, disable stop button
                arcadeButton.setDisable(false); // Animation done, re-enable arcade button
            });
        } else {
            showError("Aucune précédente solution", "Veuillez d'abord résoudre le labyrinthe avec une méthode choisie avant de jouer l'animation.");
        }
    }

    /// Handle behavior of stop animation button. When clicked, stops the current running animation and fast-forward
    /// to the end result of the animation.
    @FXML
    private void handleStopAnimation(){
        setMazeEditor(false);
        mazeController.endCurrentAnimation();
        if(mazeController.getMazeRenderer().getLog() != null){
            genButton.setDisable(false); // re-enable generation animation button if maze was generated
        }
        if(mazeController.getStepByStepPath() != null){
            solveButton.setDisable(false); // re-enable solving animation button if maze was solved with one chosen algorithm
        }
        stopButton.setDisable(true); // disable stop button after the animation is stopped.
        arcadeButton.setDisable(false); // Animation done, re-enable arcade button
    }

    /// Handle the click of the "arcade" button
    @FXML
    private void handleArcade() throws IOException {
        assert mazeController.getMazeRenderer() != null && mazeController.getMazeRenderer().getGraphMaze() != null;

        if (mazeController.isGameRunning()) {
            // A game's already running; stop it.
            mazeController.stopGame();
        } else {
            // Launch a new dialog and start a game if the user clicked "Launch game" with correct settings.
            connexeApp.showPlayArcadeDialog().ifPresent(config -> {
                try {
                    mazeController.beginGame(config);
                } catch (IncompatibleMazeException e) {
                    // The maze is incompatible with the game mode! Tell the user about that with an alert.
                    showError("Labyrinthe incompatible", e.getMessage());
                }
            });
        }
    }

    /// Initialize config for a FileChooser
    /// Only accepts a file of ".con" extension
    /// Opens by default the last directory a user saved into/opened
    private FileChooser initFileChooser(){
        setMazeEditor(false);
        // Create a new file chooser dialog popup and set the opened directory to the last visited one
        FileChooser fileChooser = new FileChooser();
        File lastDir = Settings.getLastVisitedDirectory();
        if (lastDir != null) fileChooser.setInitialDirectory(lastDir);

        // Set extension filter to only allow .con files
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "Fichiers CON (*.con)", "*.con");
        fileChooser.getExtensionFilters().add(extFilter);
        return fileChooser;
    }

    /// Show an error dialog box
    /// @param error the title displayed in the dialog box
    /// @param details the content of the error message
    private void showError(String error, String details){
        setMazeEditor(false);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(error);
        alert.setContentText(details);

        alert.showAndWait();
    }

    /// Is called by the main application to give a reference back to itself.
    /// @param connexeApp main application
    public void setConnexeApp(ConnexeApp connexeApp) {
        this.connexeApp = connexeApp;
    }

    /// References the MazeController to call its building methods from the menu bar options (new, edit...)
    /// @param mazeController the MazeController to use maze related methods from (building, etc...)
    public void setMazeController(MazeController mazeController){
        this.mazeController = mazeController;

        // Now that we have a maze controller, change the "arcade" button text depending on the game state
        arcadeButton.textProperty().bind(
                mazeController.gameRunningProperty().map(x -> x ? "Terminer la partie" : "Arcade")
        );
    }
}