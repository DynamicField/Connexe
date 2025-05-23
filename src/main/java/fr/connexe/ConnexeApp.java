package fr.connexe;

import fr.connexe.ui.*;
import fr.connexe.ui.game.GameStartConfig;
import fr.connexe.ui.game.input.ControllerHub;
import fr.connexe.ui.game.input.InputSystemException;
import fr.connexe.ui.game.input.KeyboardHub;
import fr.connexe.ui.game.lobby.PlayArcadeDialogController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/// Our entire application, which persists until it is closed.
///
/// Spins up the [Scene] and [Stage] of the app. Does it manage multiple pages too? We haven't decided yet.
public class ConnexeApp extends Application {
    private Stage stage;
    private BorderPane rootLayout;
    private File mazeFilePath;

    // Receives controller input for the entire app; null when SDL2 is broken.
    private @Nullable ControllerHub controllerHub;
    // Receives keyboard input for the entire app. Always initialized.
    private KeyboardHub keyboardHub; // initialized later on

    /// Creates a new instance of the [ConnexeApp] and initializes the controller hub as soon as possible.
    public ConnexeApp() {
        try {
            controllerHub = new ControllerHub();
        } catch (InputSystemException e) {
            System.err.println("Failed to initialize the controller hub! Gamepad support won't be available");
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        this.stage = stage;
        this.stage.setTitle("Connexe");

        // Min window size
        stage.setMinWidth(900);
        stage.setMinHeight(600);

        MainController mainController = initMainController();
        MazeController mazeController = initMazeController();
        mainController.setMazeController(mazeController);
    }

    /// Initializes the main controller and the root layout of the application.
    /// @throws IOException if the FXML file failed to load
    /// @return the main controller
    public MainController initMainController() throws IOException {
        // Load the FXML file and create a new scene with it.
        // That will automatically spin up a new MainController.
        //
        // Every FXML file should be present under the fr/connexe folder inside the resources folder,
        // as this module is named "fr.connexe".
        FXMLLoader fxmlLoader = new FXMLLoader(ConnexeApp.class.getResource("root.fxml"));
        rootLayout = fxmlLoader.load();
        MainController controller = fxmlLoader.getController();

        // Create the scene with the FXML file, set its title and size and bind the layout size to the scene size
        Scene scene = new Scene(rootLayout, 800, 500);
        rootLayout.prefWidthProperty().bind(scene.widthProperty());
        rootLayout.prefHeightProperty().bind(scene.heightProperty());

        // Attach scene to window and show
        stage.setScene(scene);
        controller.setConnexeApp(this);
        stage.show();

        // Create the keyboard hub now, and store it for future use in MazeController
        keyboardHub = new KeyboardHub(scene);

        return controller;
    }

    /// Initializes the maze controller with the panel containing the maze.
    /// @throws IOException if the FXML file failed to load
    /// @return the maze controller
    public MazeController initMazeController() throws IOException {
        // Load the FXML file for the maze overview.
        // That will automatically spin up a new MazeController.
        FXMLLoader fxmlLoader = new FXMLLoader(ConnexeApp.class.getResource("maze-view.fxml"));
        VBox mazeOverview = fxmlLoader.load();

        // Instead of changing the whole scene, to preserve the menubar and overall UI,
        // only change content of scene (more efficient)
        rootLayout.setCenter(mazeOverview);

        // Force VBox to take all the available space even if window is resized
        VBox.setVgrow(mazeOverview, Priority.ALWAYS);
        mazeOverview.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        MazeController mazeController = fxmlLoader.getController();
        mazeController.setInputHubs(keyboardHub, controllerHub);

        return mazeController;
    }

    /// Launches the application.
    /// @param args the command line arguments
    public static void main(String[] args) {
        launch();
    }

    /// Returns the stage of the application.
    /// @return the stage of the application.
    public Stage getStage(){
        return this.stage;
    }

    /// Setup and show the dialog box when clicking on the menu item to create a new Maze
    /// @param mazeRenderer passed by the MainController to be ready to receive a maze (to display later on the view)
    /// @throws IOException if the FXML file failed to load
    /// @return true if the user clicked OK, false otherwise
    public boolean showNewMazeDialog(MazeRenderer mazeRenderer) throws IOException{
        // Load the fxml file and create a new stage for the popup dialog.
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(ConnexeApp.class.getResource("new-maze-popup.fxml"));
        BorderPane page = loader.load();

        // Create the dialog Stage.
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Créer un labyrinthe");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(stage);

        dialogStage.setMinHeight(400);
        dialogStage.setMinWidth(600);

        Scene scene = new Scene(page);
        dialogStage.setScene(scene);

        // Attach the dialog stage to the controller
        NewMazeDialogController controller = loader.getController();
        controller.setDialogStage(dialogStage);
        controller.setMazeRenderer(mazeRenderer);

        // Show the dialog and wait until the user closes it
        dialogStage.showAndWait();

        return controller.isOkClicked();
    }

    /// Setup and show the dialog box when clicking on the menu item to solve a maze, to select solving method
    /// @param mazeController the maze controller given to the dialog to configure maze solution display
    /// @throws IOException if the FXML file failed to load
    /// @return true if the user clicked OK, false otherwise
    public boolean showSolveMazeDialog(MazeController mazeController) throws IOException{
        // Load the fxml file and create a new stage for the popup dialog.
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(ConnexeApp.class.getResource("solve-maze-popup.fxml"));
        BorderPane page = loader.load();

        // Create the dialog Stage.
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Résoudre le labyrinthe");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(stage);

        dialogStage.setMinHeight(250);
        dialogStage.setMinWidth(400);

        Scene scene = new Scene(page);
        dialogStage.setScene(scene);

        // Attach the dialog stage to the controller
        SolveMazeController controller = loader.getController();
        controller.setDialogStage(dialogStage);
        controller.setMazeController(mazeController);

        // Show the dialog and wait until the user closes it
        dialogStage.showAndWait();

        return controller.isOkClicked();
    }

    /// Shows the dialog box to start a new arcade game.
    ///
    /// @throws IOException if the FXML file failed to load
    /// @return the chosen game configuration if one has been chosen; otherwise, an empty optional will be given.
    public Optional<GameStartConfig> showPlayArcadeDialog() throws IOException {
        // Load the fxml file and create a new stage for the popup dialog.
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(ConnexeApp.class.getResource("play-arcade-popup.fxml"));
        BorderPane page = loader.load();

        // Create the dialog Stage.
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Jouer en mode arcade");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(stage);

        dialogStage.setMinHeight(300);
        dialogStage.setMinWidth(500);

        Scene scene = new Scene(page);
        dialogStage.setScene(scene);

        // Attach the dialog stage to the controller
        PlayArcadeDialogController controller = loader.getController();
        controller.setup(dialogStage, controllerHub);

        // Show the dialog and wait until the user closes it
        dialogStage.showAndWait();

        return Optional.ofNullable(controller.getFinalConfig());
    }

    /// Returns the file path of the last opened maze. Can be null if no maze was opened yet.
    /// @return the file path of the last opened maze; can be null!
    public File getMazeFilePath() {
        return mazeFilePath;
    }

    /// Sets the file path of the last opened maze.
    /// @param file the file path of the last opened maze
    public void setMazeFilePath(File file) {
        this.mazeFilePath = file;
    }

    /// Function used by controllers when opening/saving/creating maze files
    /// Set the [Stage] title to display the name of a currently opened maze file
    /// @param file The name of the currently opened file in the view. If null given,
    /// clears the app title
    public void updateStageTitle(String file){
        if(file != null){
            stage.setTitle("Connexe - " + file);
        } else {
            stage.setTitle("Connexe");
        }
    }
}