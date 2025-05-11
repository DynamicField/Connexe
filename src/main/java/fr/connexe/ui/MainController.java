package fr.connexe.ui;

import fr.connexe.ConnexeApp;
import javafx.fxml.FXML;
import javafx.stage.FileChooser;

import java.io.File;

/// The controller of the main layout (menu bar, and overall ui). Is created automatically by the `hello-view.xml` FXML file.
public class MainController {

    private ConnexeApp connexeApp;
    private MazeController mazeController;

    /**
     * Is called by the main application to give a reference back to itself.
     * @param connexeApp description
     */
    public void setConnexeApp(ConnexeApp connexeApp) {
        this.connexeApp = connexeApp;
    }

    /**
     * References the MazeController to call its building methods from the menu bar options (new, edit...)
     * @param mazeController
     */
    public void setMazeController(MazeController mazeController){
        this.mazeController = mazeController;
    }

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

    /**
     * To create a new maze file
     */
    @FXML
    private void handleNew() {
        // To do : popup to create a new maze with custom settings
        System.out.println("New triggered....");
        mazeController.createMazeFX();
    }

    /**
     * Opens a FileChooser to let the user select a maze file to load.
     */
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

    /**
     * Saves the file to the person file that is currently open. If there is no
     * open file, the "save as" dialog is shown.
     */
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

    /**
     * Opens a FileChooser to let the user select a file to save to.
     */
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

    /**
     * Closes the application
     */
    @FXML
    private void handleExit() {
        System.exit(0);
    }
}