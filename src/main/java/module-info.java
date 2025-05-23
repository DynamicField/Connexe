/// The Java module of the Connexe app
open module fr.connexe {
    // Add common JavaFX modules to the list of modules we need at runtime.
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.prefs;
    requires sdl2gdx;
    requires static org.jetbrains.annotations;

    exports fr.connexe;
    exports fr.connexe.algo;
    exports fr.connexe.ui;
    exports fr.connexe.ui.game;
    exports fr.connexe.algo.generation;
    exports fr.connexe.ui.game.lobby;
    exports fr.connexe.ui.game.input;
}