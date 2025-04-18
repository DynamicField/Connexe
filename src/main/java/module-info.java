/// The Java module of the Connexe app
open module fr.connexe {
    // Add common JavaFX modules to the list of modules we need at runtime.
    requires javafx.controls;
    requires javafx.fxml;

    exports fr.connexe;
}