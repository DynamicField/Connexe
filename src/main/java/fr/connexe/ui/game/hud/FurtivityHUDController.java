package fr.connexe.ui.game.hud;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.net.URL;
import java.util.ResourceBundle;

/// The HUD controller for the [furtivity game mode][fr.connexe.ui.game.GameMode#FURTIVITY].
public final class FurtivityHUDController extends HUDController {
    /// Creates a new instance of the [FurtivityHUDController].
    ///
    /// The [#initialize()] function will be called by JavaFX.
    public FurtivityHUDController() {}

    @FXML
    protected void initialize() {
        gameModeLabel.setText("FURTIVITÉ");

        Label gameModeInfo = new Label("Soyez le premier à trouver la sortie cachée !");
        gameModeInfo.setFont(Font.font(24));
        gameModeInfo.setMaxWidth(Double.MAX_VALUE);
        gameModeInfo.setMaxHeight(Double.MAX_VALUE);
        gameModeInfo.setAlignment(Pos.CENTER);
        gameModeInfo.setTextAlignment(TextAlignment.CENTER);

        sidePane.getChildren().add(gameModeInfo);
    }
}
