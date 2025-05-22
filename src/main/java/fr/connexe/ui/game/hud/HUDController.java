package fr.connexe.ui.game.hud;

import fr.connexe.ui.game.Player;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

/// Base class for every controller managing an arcade HUD.
public sealed abstract class HUDController
        implements Initializable
        permits EfficiencyHUDController, FurtivityHUDController, SwiftnessHUDController {

    /// The label of the left HUD, which is used to display the current game mode.
    @FXML
    protected Label gameModeLabel;

    /// The pane next to the game mode label.
    @FXML
    protected StackPane sidePane;

    /// Creates a new label for the player's place (1st, 2nd, etc.).
    protected Label makePlaceLabel(int placeIndex) {
        // French language moment: 1st is 1ᵉʳ, 2nd is 2ᵉ, etc.
        Label place = new Label(placeIndex == 0 ? "1ᵉʳ" : (placeIndex + 1) + "ᵉ");
        // Add some margin to the label
        HBox.setMargin(place, new Insets(0, 8, 0, 0));

        // Configure the CSS class based on the place.
        place.getStyleClass().add("player-place");
        if (placeIndex == 0) {
            place.getStyleClass().add("first");
        } else if (placeIndex == 1) {
            place.getStyleClass().add("second");
        } else if (placeIndex == 2) {
            place.getStyleClass().add("third");
        } else {
            place.getStyleClass().add("not-in-podium");
        }
        return place;
    }

    /// Attaches a [FlowPane] with center-left alignment inside the [#sidePane].
    protected FlowPane attachPlayersPane() {
        FlowPane pane = new FlowPane();
        pane.alignmentProperty().set(Pos.CENTER_LEFT);
        pane.setHgap(32);
        pane.setVgap(16);
        sidePane.getChildren().add(pane);
        return pane;
    }

    /// Makes a horizontal box for a player. Usually contains information like place, name, etc.
    protected HBox makeEmptyPlayerBox(Player player) {
        HBox box = new HBox();
        box.getStyleClass().add("hud-player");

        // Special class when the player reached the end of the maze
        if (player.hasReachedEnd()) {
            box.getStyleClass().add("reached-end");
        }

        return box;
    }
}
