package fr.connexe.ui.game.lobby;

import fr.connexe.ConnexeApp;
import fr.connexe.ui.game.GameMode;
import fr.connexe.ui.game.GameStartConfig;
import fr.connexe.ui.game.PlayerProfile;
import fr.connexe.ui.game.input.ControllerHub;
import fr.connexe.ui.game.input.PlayerInputSource;
import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/// The controller for the "play arcade" dialog.
public class PlayArcadeDialogController implements Initializable {
    private Stage dialogStage;

    // The configuration generated once the dialog is closed and confirmed by the user.
    private @Nullable GameStartConfig finalConfig;

    // All players present in the game.
    private final ObservableList<PlayerProfile> players = FXCollections.observableArrayList(
            new PlayerProfile(randomColor(), new PlayerInputSource.KeyboardArrows())
    );

    // The timer which polls the controller hub for new controller states every frame.
    // Can be null if we have no controller hub.
    private @Nullable AnimationTimer gamepadPollingTimer;

    @FXML
    private ListView<PlayerProfile> playerList;

    @FXML
    private ToggleGroup gameMode;
    @FXML
    private RadioButton radioEfficiency;
    @FXML
    private RadioButton radioSwiftness;
    @FXML
    private RadioButton radioFurtivity;
    @FXML
    private RadioButton radioVersatility;

    @FXML
    private Button startButton;

    private GameMode selectedGameMode = GameMode.EFFICIENCY;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configure the player list to use our PlayerCell and put our list in there.
        playerList.setCellFactory(_ -> new PlayerCell());
        playerList.setItems(players);

        // Change the selected game mode based on the toggle group's chosen toggle.
        gameMode.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                RadioButton selectedRadioButton = (RadioButton) newValue;

                // Update the value!
                if (newValue == radioEfficiency) {
                    selectedGameMode = GameMode.EFFICIENCY;
                } else if (selectedRadioButton == radioSwiftness) {
                    selectedGameMode = GameMode.SWIFTNESS;
                } else if (selectedRadioButton == radioFurtivity) {
                    selectedGameMode = GameMode.FURTIVITY;
                } else if (selectedRadioButton == radioVersatility) {
                    selectedGameMode = GameMode.VERSATILITY;
                }
            }
        });

        // When the player list changes, enable/disable the start button if there's no player
        players.addListener((ListChangeListener.Change<?> _) -> startButton.setDisable(players.isEmpty()));
    }

    /// Configures this controller with its parent stage, and the (optional) controller hub to see if controllers
    /// are connected.
    ///
    /// @param dialogStage The stage to attach to this controller.
    /// @param controllerHub The controller hub to use to poll for controller states.
    public void setup(Stage dialogStage, @Nullable ControllerHub controllerHub) {
        this.dialogStage = dialogStage;

        if (controllerHub != null) {
            // We have a controller hub: configure the animation timer to poll for controller states.
            gamepadPollingTimer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    ControllerHub.State state = controllerHub.poll();
                    updateControllerConnectivity(state);
                }
            };
            gamepadPollingTimer.start(); // Start the timer now!

            // Make sure the timer stops when the dialog is *requested* to be closed.
            // This won't happen when we close the dialog programmatically!
            dialogStage.setOnCloseRequest(_ -> gamepadPollingTimer.stop());
        }
    }

    /// Deletes a player from the player list.
    ///
    /// @param player The player to delete.
    public void deletePlayer(PlayerProfile player) {
        players.remove(player);
    }

    /// Gets the configuration the user decided to play with for their next arcade game.
    ///
    /// Returns null if no configuration has been chosen.
    ///
    /// @return the chosen game config; can be null if the user didn't confirm the dialog.
    public @Nullable GameStartConfig getFinalConfig() {
        return finalConfig;
    }

    /// Handles a click on the "start" button, which closes the dialog and returns the game configuration.
    @FXML
    private void handleGameStart() {
        assert !players.isEmpty(); // The button isn't enabled if there are no players

        // Display an alert if the game mode isn't supported yet
        if (selectedGameMode == GameMode.VERSATILITY) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Mode non supporté");
            alert.setHeaderText("Mode de jeu non supporté");
            alert.setContentText("Il n'a pas encore été implémenté mais pas d'inquiétude ça arrive bientôt...");
            alert.showAndWait();
            return;
        }

        // Setup the final game configuration and close the dialog
        finalConfig = new GameStartConfig(
                players.toArray(new PlayerProfile[0]), // List to array
                selectedGameMode
        );
        dialogStage.close();

        // Make sure the background polling timer is stopped!
        if (gamepadPollingTimer != null) {
            gamepadPollingTimer.stop();
        }
    }

    /// Handles a click on the "cancel" button, which closes the dialog.
    @FXML
    private void handleGameCancel() {
        // Bye!
        dialogStage.close();

        // Make sure the background polling timer is stopped!
        if (gamepadPollingTimer != null) {
            gamepadPollingTimer.stop();
        }
    }

    /// Handles a click on the "add player" button, which adds a new player to the list.
    @FXML
    private void handleAddPlayer() {
        if (players.size() >= 8) {
            // Too many players! Show an alert.
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Trop de joueurs");
            alert.setHeaderText("Nombre max. de joueurs atteint");
            alert.setContentText("Impossible d'avoir plus de 8 joueurs dans une partie.");
            alert.showAndWait();
            return;
        }

        // Add a new player with a random color and the next available input method (keyboard first; controllers next)
        players.add(new PlayerProfile(randomColor(), nextAvailableInput()));
    }

    // Updates the "controllerDetected" property of all players, based on the all connected controllers.
    private void updateControllerConnectivity(ControllerHub.State controllers) {
        for (PlayerProfile player : players) {
            if (player.getInputSource() instanceof PlayerInputSource.Controller(int slot)) {
                // Check if the controller is connected
                player.setControllerDetected(controllers.getController(slot).isPresent());
            }
        }
    }

    // Returns the next available input method for a new player.
    // Prioritizes keyboard inputs first, then controllers.
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

    // Makes a random color. What else?
    private static Color randomColor() {
        return Color.color(Math.random(), Math.random(), Math.random());
    }

    // The cell for the player list that generates player items (using player-item.fxml) for each PlayerProfile
    private class PlayerCell extends ListCell<PlayerProfile> {
        private final PlayerItemController controller;
        private final Node view;

        public PlayerCell() {
            // Load the player item FXML and get the controller
            try {
                FXMLLoader loader = new FXMLLoader(ConnexeApp.class.getResource("player-item.fxml"));
                view = loader.load();
                controller = loader.getController();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }

        @Override
        protected void updateItem(PlayerProfile item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                // Empty item: put nothing
                setGraphic(null);
            } else {
                // We have a player profile: set up the controller.
                setGraphic(view);
                controller.setup(PlayArcadeDialogController.this, item);
            }
        }

        @Override
        public void updateIndex(int newIndex) {
            super.updateIndex(newIndex);

            // The index changed: update this on the controller so we can show "Player 1", "Player 2", etc.
            controller.setNumber(newIndex + 1);
        }
    }
}
