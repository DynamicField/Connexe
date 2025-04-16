package fr.connexe;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/// Our entire application, which persists until it is closed.
///
/// Spins up the [Scene] and [Stage] of the app. Does it manage multiple pages too? We haven't decided yet.
public class ConnexeApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Load the FXML file and create a new scene with it.
        // That will automatically spin up a new MainController.
        //
        // Every FXML file should be present under the fr/connexe folder inside the resources folder,
        // as this module is named "fr.connexe".
        FXMLLoader fxmlLoader = new FXMLLoader(ConnexeApp.class.getResource("hello-view.fxml"));

        // Create the scene with the FXML file, set its title and size, and show it.
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Coucou");
        stage.setScene(scene);
        stage.show();
    }

    /// Launches the application.
    public static void main(String[] args) {
        launch();
    }
}