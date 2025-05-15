package fr.connexe.ui;

import fr.connexe.ConnexeApp;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;

/// The controller of the main layout (menu bar, and overall ui). Is created automatically by the `hello-view.xml` FXML file.
public class MainController {

    private ConnexeApp connexeApp;
    private MazeController mazeController;

     /// Is called by the main application to give a reference back to itself.
     /// @param connexeApp main application
    public void setConnexeApp(ConnexeApp connexeApp) {
        this.connexeApp = connexeApp;
    }

     /// References the MazeController to call its building methods from the menu bar options (new, edit...)
     /// @param mazeController the MazeController to use maze related methods from (building, etc...)
    public void setMazeController(MazeController mazeController){
        this.mazeController = mazeController;
    }

    /// Initialize config for a FileChooser
    /// Only accepts a file of ".con" extension
    /// Opens by default the last directory a user saved into/opened
    private FileChooser initFileChooser(){
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
            mazeController.createMazeFX();
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
    }

    ///  Closes the app
    @FXML
    private void handleExit() {
        System.exit(0);
    }

    ///  Menu Item option to show a hardcoded example maze
    @FXML
    private void handleExampleMaze(){
        // Initialize a renderer taking a maze generated from user parameters through the creation dialog box
        MazeRenderer mazeRenderer = new MazeRenderer();
        mazeRenderer.setDefaultExample();
        mazeController.setMazeRenderer(mazeRenderer);
        mazeController.createMazeFX();

        // Update the app title
        connexeApp.updateStageTitle("Exemple");
        connexeApp.setMazeFilePath(null);
    }

    /// Show an error dialog box
    /// @param error the title displayed in the dialog box
    /// @param details the content of the error message
    private void showError(String error, String details){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(error);
        alert.setContentText(details);

        alert.showAndWait();
    }
}