package fr.connexe.ui.game.hud;

import fr.connexe.ui.game.Player;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.ResourceBundle;

/// The HUD controller for the [swiftness game mode][fr.connexe.ui.game.GameMode#SWIFTNESS].
public final class SwiftnessHUDController extends HUDController {
    private FlowPane playersPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gameModeLabel.setText("RAPIDITÉ");
        playersPane = attachPlayersPane();
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

            // Make all elements of the player box.
            Label place = makePlaceLabel(placeIndex);
            Circle icon = new Circle(14, p.getProfile().getColor());
            Label playerName = new Label("Joueur " + (p.getIndex() + 1));
            Label timeLabel = new Label();

            // Configure the player icon.
            icon.getStyleClass().add("player-icon");

            // Configure the player name.
            HBox.setMargin(playerName, new Insets(0, 8, 0, 8));
            playerName.getStyleClass().add("player-name");

            // Find the number of elapsed nanosecs since the start of the race.
            long elapsedNS;
            if (p.hasReachedEnd()) {
                // Players has reached the end: "freeze" the timer.
                elapsedNS = p.getReachedEndAt() - startTimestamp;
            } else {
                // End not yet reached: use the current time.
                elapsedNS = currentTime - startTimestamp;
            }

            // Format the time in XX:XX:XX format. (minutes/seconds/centiseconds)
            Duration elapsedDuration = Duration.ofNanos(elapsedNS);
            String timeText = String.format("%02d:%02d:%02d",
                    elapsedDuration.toMinutesPart(),
                    elapsedDuration.toSecondsPart(),
                    elapsedDuration.toMillisPart() / 10);

            // Then configure the time label with the computed text (+ styling).
            timeLabel.setText(timeText);
            timeLabel.getStyleClass().addAll("player-score", "player-time");
            HBox.setHgrow(timeLabel, Priority.ALWAYS);

            // Add all children
            box.getChildren().addAll(place, icon, playerName, timeLabel);

            playersPane.getChildren().add(box);
        }
    }
}
