package fr.connexe;

import fr.connexe.ui.MainController;
import fr.connexe.ui.MazeController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

/// Our entire application, which persists until it is closed.
///
/// Spins up the [Scene] and [Stage] of the app. Does it manage multiple pages too? We haven't decided yet.
public class ConnexeApp extends Application {
    private Stage stage;
    private BorderPane rootLayout;

    /// Creates a new instance of the [ConnexeApp].
    public ConnexeApp() {}

    @Override
    public void start(Stage stage) throws IOException {
        this.stage = stage;
        this.stage.setTitle("Connexe");
        stage.setMinHeight(500);
        stage.setMinWidth(800);

        MainController mainController = initMainController();
        MazeController mazeController = initMazeController();
        mainController.setMazeController(mazeController);
    }


    public MainController initMainController() throws IOException {
        // Load the FXML file and create a new scene with it.
        // That will automatically spin up a new MainController.
        //
        // Every FXML file should be present under the fr/connexe folder inside the resources folder,
        // as this module is named "fr.connexe".
        FXMLLoader fxmlLoader = new FXMLLoader(ConnexeApp.class.getResource("root.fxml"));
        rootLayout = fxmlLoader.load();
        MainController controller = fxmlLoader.getController();

        // Create the scene with the FXML file, set its title and size, and show it.
        Scene scene = new Scene(rootLayout, 1200, 800);
        stage.setScene(scene);
        controller.setConnexeApp(this);
        stage.show();

        return controller;
    }

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

}