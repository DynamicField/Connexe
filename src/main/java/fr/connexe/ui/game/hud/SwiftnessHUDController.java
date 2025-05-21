package fr.connexe.ui.game.hud;

import fr.connexe.ui.game.Player;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.ResourceBundle;

/// The HUD controller for the [swiftness game mode][fr.connexe.ui.game.GameMode#SWIFTNESS].
public final class SwiftnessHUDController extends HUDController {
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gameModeLabel.setText("RAPIDITÉ");
    }

    public void update(List<Player> playersSorted, long startTimestamp) {
        playersPane.getChildren().clear();

        // Styling is done in arcade.css
        int placeIndex = 0;
        final long currentTime = System.nanoTime();
        for (int i = 0; i < playersSorted.size(); i++) {
            Player p = playersSorted.get(i);

            // Keep the previous place (first, second, etc.) only when both previous and current players
            // haven't reached the end.
            if (i == 0 || playersSorted.get(i - 1).hasReachedEnd() || p.hasReachedEnd()) {
                placeIndex = i;
            }

            HBox box = makeEmptyPlayerBox(p);

            Label place = makePlaceLabel(placeIndex);
            Circle icon = new Circle(14, p.getProfile().getColor());
            Label playerName = new Label("Joueur " + (p.getIndex() + 1));
            Label timeLabel = new Label();

            icon.getStyleClass().add("player-icon");

            HBox.setMargin(playerName, new Insets(0, 8, 0, 8));
            playerName.getStyleClass().add("player-name");

            long elapsedNS;
            if (p.hasReachedEnd()) {
                elapsedNS = p.getReachedEndAt() - startTimestamp;
            } else {
                elapsedNS = currentTime - startTimestamp;
            }
            Duration elapsedDuration = Duration.ofNanos(elapsedNS);
            String timeText = String.format("%02d:%02d:%02d",
                    elapsedDuration.toMinutesPart(),
                    elapsedDuration.toSecondsPart(),
                    elapsedDuration.toMillisPart() / 10);
            timeLabel.setText(timeText);

            timeLabel.getStyleClass().addAll("player-score", "player-time");
            HBox.setHgrow(timeLabel, Priority.ALWAYS);

            box.getChildren().addAll(place, icon, playerName, timeLabel);

            playersPane.getChildren().add(box);
        }
    }
}
