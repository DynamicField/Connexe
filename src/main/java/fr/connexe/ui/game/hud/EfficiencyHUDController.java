package fr.connexe.ui.game.hud;

import fr.connexe.ui.game.Player;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/// The HUD controller for the [efficiency game mode][fr.connexe.ui.game.GameMode#EFFICIENCY].
public final class EfficiencyHUDController extends HUDController {
    private FlowPane playersPane;

    /// Creates a new instance of the [EfficiencyHUDController].
    ///
    /// The [#initialize()] function will be called by JavaFX.
    public EfficiencyHUDController() {}

    @Override
    protected void initialize() {
        gameModeLabel.setText("EFFICACITÉ");
        playersPane = attachPlayersPane();
    }

    /// Updates the HUD with the leaderboard of players.
    ///
    /// @param playersSorted The list of players sorted by their number of moves done, ascending.
    public void update(List<Player> playersSorted) {
        playersPane.getChildren().clear();

        // Styling is done in arcade.css
        int placeIndex = 0;
        for (int i = 0; i < playersSorted.size(); i++) {
            Player p = playersSorted.get(i);

            // Keep the previous place (first, second, etc.) if the previous player
            // has the same moves done as the current player
            if (i == 0 || playersSorted.get(i-1).getMovesDone() != p.getMovesDone()) {
                placeIndex = i;
            }

            HBox box = makeEmptyPlayerBox(p);

            // Make all elements of the player box.
            Label place = makePlaceLabel(placeIndex);
            Circle icon = new Circle(14, p.getProfile().getColor());
            Label playerName = new Label("Joueur " + (p.getIndex() + 1));
            Label moves = new Label(p.getMovesDone() + " cases");

            // Configure the player icon.
            icon.getStyleClass().add("player-icon");

            // Configure the player name.
            HBox.setMargin(playerName, new Insets(0, 8, 0, 8));
            playerName.getStyleClass().add("player-name");

            // Configure the player moves.
            moves.getStyleClass().addAll("player-moves", "player-score");
            HBox.setHgrow(moves, Priority.ALWAYS);

            // Add all children.
            box.getChildren().addAll(place, icon, playerName, moves);

            playersPane.getChildren().add(box);
        }
    }
}
