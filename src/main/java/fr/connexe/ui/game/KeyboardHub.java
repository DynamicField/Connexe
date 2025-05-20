package fr.connexe.ui.game;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

import java.util.EnumSet;

public class KeyboardHub {
    private final Scene scene;
    private final EnumSet<KeyCode> pressedKeys = EnumSet.noneOf(KeyCode.class);

    public KeyboardHub(Scene scene) {
        this.scene = scene;

        this.scene.setOnKeyPressed(e -> pressedKeys.add(e.getCode()));
        this.scene.setOnKeyReleased(e -> pressedKeys.remove(e.getCode()));
    }

    public EnumSet<KeyCode> getPressedKeys() {
        return pressedKeys.clone();
    }
}
