package fr.connexe.ui.game.hud;

import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

/// The HUD controller for the [furtivity game mode][fr.connexe.ui.game.GameMode#FURTIVITY].
public final class FurtivityHUDController extends HUDController {
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gameModeLabel.setText("FURTIVITÉ");
    }
}
