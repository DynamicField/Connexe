package fr.connexe.ui;

import fr.connexe.ConnexeApp;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;

/// The controller of the main layout (menu bar, and overall ui). Is created automatically by the `hello-view.xml` FXML file.
public class MainController {

    private ConnexeApp connexeApp;
    private MazeController mazeController;

    @FXML
    private MenuItem change;

    @FXML
    public void initialize() {
        change.setDisable(true);
    }

     /// Is called by the main application to give a reference back to itself.
     /// @param connexeApp - main application
    public void setConnexeApp(ConnexeApp connexeApp) {
        this.connexeApp = connexeApp;
    }

     /// References the MazeController to call its building methods from the menu bar options (new, edit...)
     /// @param mazeController - the MazeController to use maze related methods from (building, etc...)
    public void setMazeController(MazeController mazeController){
        this.mazeController = mazeController;
    }

    /// Initialize config for a FileChooser
    /// Only accepts file of ".con" extension
    /// Opens by default the last directory a user saved into/opened
    private File initFileChooser(){
        // Create a new file chooser dialog popup and set the opened directory to the last visited one
        FileChooser fileChooser = new FileChooser();
        File lastDir = Settings.getLastVisitedDirectory();
        if (lastDir != null) fileChooser.setInitialDirectory(lastDir);

        // Set extension filter to only allow .con files
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "Fichiers CON (*.con)", "*.con");
        fileChooser.getExtensionFilters().add(extFilter);

        // Retrieve selected file to update the last opened directory (persists across sessions)
        File selected = fileChooser.showOpenDialog(connexeApp.getStage());
        if (selected != null) {
            Settings.setLastVisitedDirectory(selected.getParentFile());
        }
        return selected;
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
            change.setDisable(false);
            mazeController.createMazeFX(mazeRenderer);
        }
    }

    /// Opens a FileChooser to let the user select a maze file to load.
    @FXML
    private void handleOpen() {
        // Init the FileChooser and retrieve the selected file
        File selectedFile = initFileChooser();
        if(selectedFile != null) {
            System.out.println("Open triggered...");
            // To do : open maze from file
            //connexeApp.loadMaze(file); or something like that
        }
    }

    /// Saves the maze into a file
    /// If the current maze has never been saved before, the "save as" dialog is shown.
    @FXML
    private void handleSave() {
        System.out.println("Save triggered...");
        /*File mazeFile = connexeApp.getMazeFilePath();
        if (mazeFile != null) { // If file has already been saved once, just save above
            // To do : save maze data to export as a file
            //connexeApp.saveMaze(file); or something like that
        } else { // else, act as "Save as..." option
            handleSaveAs();
        }*/
    }

    /// Opens a FileChooser to let the user select a file to save to.
    @FXML
    private void handleSaveAs() {
        // Init the FileChooser and retrieve the selected file
        File selectedFile = initFileChooser();
        if (selectedFile != null) {
            System.out.println("Save as triggered...");
            // To do : save maze data to export as a file
            //connexeApp.saveMaze(file); or something like that
        }
    }

    /// For editing the maze (not used yet)
    public void handleChange(){}

    ///  Closes the app
    @FXML
    private void handleExit() {
        System.exit(0);
    }

    ///  Menu Item option to show a hardcoded example maze
    @FXML
    private void handleExampleMaze(){
        System.out.println("Testing default maze...");

        // Initialize a renderer taking a maze generated from user parameters through the creation dialog box
        MazeRenderer mazeRenderer = new MazeRenderer();
        mazeRenderer.setDefaultExample();
        mazeController.createMazeFX(mazeRenderer);
    }
}