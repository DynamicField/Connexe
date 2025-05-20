package fr.connexe.ui.game.lobby;

import fr.connexe.ui.game.PlayerInputSource;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.paint.Color;

public class LobbyPlayer {
    private Color color;
    private PlayerInputSource inputSource;
    private BooleanProperty controllerDetected = new SimpleBooleanProperty(false);

    public LobbyPlayer(Color color, PlayerInputSource inputSource) {
        this.color = color;
        this.inputSource = inputSource;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public PlayerInputSource getInputSource() {
        return inputSource;
    }

    public void setInputSource(PlayerInputSource inputSource) {
        this.inputSource = inputSource;
    }

    public BooleanProperty controllerDetectedProperty() {
        return controllerDetected;
    }
    public boolean isControllerDetected() {
        return controllerDetected.get();
    }
    public void setControllerDetected(boolean controllerDetected) {
        this.controllerDetected.set(controllerDetected);
    }
}
