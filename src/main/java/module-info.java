/// The Java module of the Connexe app
open module fr.connexe {
    // Add common JavaFX modules to the list of modules we need at runtime.
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.prefs;
    requires sdl2gdx;
    requires static org.jetbrains.annotations;

    // Only export the fr.connexe and fr.connexe.algo packages which contain necessary main methods.
    exports fr.connexe;
    exports fr.connexe.algo;
}