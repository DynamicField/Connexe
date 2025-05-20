package fr.connexe.ui.game.lobby;

import fr.connexe.ConnexeApp;
import fr.connexe.ui.game.ControllerHub;
import fr.connexe.ui.game.Player;
import fr.connexe.ui.game.PlayerInputSource;
import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PlayArcadeDialogController implements Initializable {
    private Stage dialogStage;
    private boolean okClicked = false;

    private final ObservableList<LobbyPlayer> players = FXCollections.observableArrayList(
            new LobbyPlayer(randomColor(), new PlayerInputSource.KeyboardArrows())
    );

    private @Nullable AnimationTimer gamepadPollingTimer;

    @FXML
    private ListView<LobbyPlayer> playerList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        playerList.setCellFactory(_ -> new PlayerCell());
        playerList.setItems(players);
    }

    public void setup(Stage dialogStage, @Nullable ControllerHub controllerHub) {
        this.dialogStage = dialogStage;

        if (controllerHub != null) {
            gamepadPollingTimer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    ControllerHub.State state = controllerHub.poll();
                    updateControllerConnectivity(state);
                }
            };
            gamepadPollingTimer.start();

            dialogStage.setOnCloseRequest(_ -> gamepadPollingTimer.stop());
        }
    }

    public boolean isOKClicked() {
        return okClicked;
    }

    /// Deletes a player from the player list.
    ///
    /// @param player The player to delete.
    public void deletePlayer(LobbyPlayer player) {
        players.remove(player);
    }

    @FXML
    public void handleGameStart() {
        okClicked = true;
        dialogStage.close();

        if (gamepadPollingTimer != null) {
            gamepadPollingTimer.stop();
        }
    }

    @FXML
    private void handleGameCancel() {
        dialogStage.close();

        if (gamepadPollingTimer != null) {
            gamepadPollingTimer.stop();
        }
    }

    @FXML
    private void handleAddPlayer() {
        players.add(new LobbyPlayer(randomColor(), nextAvailableInput()));
    }

    private void updateControllerConnectivity(ControllerHub.State controllers) {
        for (LobbyPlayer player : players) {
            if (player.getInputSource() instanceof PlayerInputSource.Controller(int slot)) {
                // Check if the controller is connected
                player.setControllerDetected(controllers.getController(slot).isPresent());
            }
        }
    }

    private PlayerInputSource nextAvailableInput() {
        final PlayerInputSource arrows = new PlayerInputSource.KeyboardArrows();
        final PlayerInputSource zqsd = new PlayerInputSource.KeyboardZQSD();

        // If there's no player using the arrows input, return it
        if (players.stream().noneMatch(x -> x.getInputSource().equals(arrows))) {
            return arrows;
        }

        // If there's no player using the zqsd input, return it
        if (players.stream().noneMatch(x -> x.getInputSource().equals(zqsd))) {
            return zqsd;
        }

        // Find the next available controller slot and return it.
        int i = 0;
        while (true) {
            final PlayerInputSource controller = new PlayerInputSource.Controller(i);

            // See if any player doesn't use the controller at slot i
            if (players.stream().noneMatch(x -> x.getInputSource().equals(controller))) {
                return controller;
            }

            // Onto the next controller!
            i++;
        }
    }

    private static Color randomColor() {
        return Color.color(Math.random(), Math.random(), Math.random());
    }

    private class PlayerCell extends ListCell<LobbyPlayer> {
        private final PlayerItemController controller;
        private final Node view;

        public PlayerCell() {
            try {
                FXMLLoader loader = new FXMLLoader(ConnexeApp.class.getResource("player-item.fxml"));
                view = loader.load();
                controller = loader.getController();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void updateItem(LobbyPlayer item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(null);
                setGraphic(view);
                controller.setup(PlayArcadeDialogController.this, item);
            }
        }

        @Override
        public void updateIndex(int newIndex) {
            super.updateIndex(newIndex);

            controller.setNumber(newIndex + 1);
        }
    }
}
