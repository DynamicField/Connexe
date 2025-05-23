package fr.connexe.ui.game;

import fr.connexe.ui.game.input.PlayerInputSource;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.paint.Color;

/// Contains all data for a player who's about to play a game: input settings and appearance.
public class PlayerProfile {
    private Color color;
    private PlayerInputSource inputSource;
    private final BooleanProperty controllerDetected = new SimpleBooleanProperty(false);

    /// Creates a new player profile with the given color and input source.
    ///
    /// @param color The color of the player.
    /// @param inputSource The input source of the player.
    public PlayerProfile(Color color, PlayerInputSource inputSource) {
        this.color = color;
        this.inputSource = inputSource;
    }

    /// Returns the color of the player's avatar.
    ///
    /// @return The color of the player's avatar.
    public Color getColor() {
        return color;
    }

    /// Sets the color of the player's avatar.
    ///
    /// @param color The new color of the player's avatar.
    public void setColor(Color color) {
        this.color = color;
    }

    /// Returns the input source of the player.
    ///
    /// @return The input source of the player.
    public PlayerInputSource getInputSource() {
        return inputSource;
    }

    /// Sets the input source of the player.
    ///
    /// @param inputSource The new input source of the player.
    public void setInputSource(PlayerInputSource inputSource) {
        this.inputSource = inputSource;
    }

    /// Returns the property that indicates whether a controller is detected for the player.
    /// This property can be used to listen for changes in the controller detection status.
    ///
    /// This is only used by the JavaFX UI, so it doesn't really matter for the [GameSession].
    /// A cleaner approach would be to separate the UI-state from the raw data, but this would be way
    /// too complex for something so dumb.
    ///
    /// Is false for keyboard inputs.
    ///
    /// @return The controller detected property.
    public BooleanProperty controllerDetectedProperty() {
        return controllerDetected;
    }

    /// Returns whether a controller is detected for the player. Is false for keyboard inputs.
    ///
    /// @return True if a controller is detected, false otherwise.
    public boolean isControllerDetected() {
        return controllerDetected.get();
    }

    /// Sets whether a controller is detected for the player. Is false for keyboard inputs.
    ///
    /// @param controllerDetected True if a controller is detected, false otherwise.
    public void setControllerDetected(boolean controllerDetected) {
        this.controllerDetected.set(controllerDetected);
    }
}
