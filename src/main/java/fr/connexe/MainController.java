package fr.connexe;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/// The controller of the main page. Is created automatically by the `hello-view.xml` FXML file.
public class MainController {
    /// The label named "welcomeText" in the FXML file.
    @FXML
    private Label welcomeText;

    /// Function called by the FXML when the button is clicked (self-explanatory) --> `onAction="#onHelloButtonClick"`
    @FXML
    protected void onHelloButtonClick() {
        // Update the text of the label. Previously it's just empty.
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}