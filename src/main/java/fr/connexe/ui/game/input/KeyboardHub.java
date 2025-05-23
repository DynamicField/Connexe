package fr.connexe.ui.game.input;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

import java.util.EnumSet;

/// Receives keyboard input from a [JavaFX scene][Scene], providing the currently pressed keys at any time.
///
/// Uses JavaFX's API ([Scene#getOnKeyPressed()] and [Scene#getOnKeyReleased()]) to listen for key events.
public class KeyboardHub {
    private final Scene scene; // The scene we're listening to
    private final EnumSet<KeyCode> pressedKeys = EnumSet.noneOf(KeyCode.class); // The currently pressed keys

    /// Creates a new [KeyboardHub] and sets up the key event listeners on the given scene.
    ///
    /// Only one [KeyboardHub] can listen to key events on a scene at a time.
    ///
    /// @param scene the JavaFX scene to listen for key events
    public KeyboardHub(Scene scene) {
        this.scene = scene;

        this.scene.setOnKeyPressed(e -> pressedKeys.add(e.getCode()));
        this.scene.setOnKeyReleased(e -> pressedKeys.remove(e.getCode()));
    }

    /// Returns all currently pressed keys.
    ///
    /// @return a copy of the set of currently pressed keys
    public EnumSet<KeyCode> getPressedKeys() {
        return pressedKeys.clone();
    }
}
