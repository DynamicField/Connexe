package fr.connexe.ui.game.lobby;

import fr.connexe.ui.game.PlayerProfile;
import fr.connexe.ui.game.input.PlayerInputSource;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

/// The controller for a single player in the player list of the "play arcade dialog".
///
/// @see PlayArcadeDialogController
public class PlayerItemController implements Initializable {
    private PlayArcadeDialogController parentController;
    private PlayerProfile player;

    @FXML
    private Circle icon;

    @FXML
    private Label playerLabel;

    @FXML
    private ComboBox<PlayerInputSource> inputComboBox;

    @FXML
    private Label gamepadLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Update the cell factory so to give the right string when the combobox is opened
        inputComboBox.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(PlayerInputSource item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(inputSourceName(item));
                }
            }
        });

        // Update the button cell to display the right string when the combobox item is selected
        inputComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(PlayerInputSource item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(inputSourceName(item));
                }
            }
        });

        // Put a bunch of default inputs in the combobox
        inputComboBox.getItems().addAll(
                new PlayerInputSource.KeyboardArrows(),
                new PlayerInputSource.KeyboardZQSD(),
                new PlayerInputSource.Controller(0),
                new PlayerInputSource.Controller(1),
                new PlayerInputSource.Controller(2),
                new PlayerInputSource.Controller(3),
                new PlayerInputSource.Controller(4)
        );
    }

    /// Handles a click on the "delete" button, which simply calls the parent controller.
    @FXML
    private void handleDelete() {
        assert parentController != null;

        // Ask the parent controller to kindly delete this player from the list.
        parentController.deletePlayer(player);
    }

    /// Sets up this player item with its parent controller and player profile this controller manages.
    ///
    /// @param parentController The parent controller of this player item.
    /// @param player The player profile this controller manages.
    public void setup(PlayArcadeDialogController parentController, PlayerProfile player) {
        // Set the relevant fields.
        this.parentController = parentController; // I can hardly believe this one would be null
        this.player = Objects.requireNonNull(player); // Make sure it isn't null

        // Update the icon color from the player data
        icon.setFill(player.getColor());

        // Set the player input source in the combo box. Add it to the combobox list if it doesn't exist.
        if (!inputComboBox.getItems().contains(player.getInputSource())) {
            inputComboBox.getItems().add(player.getInputSource());
        }
        inputComboBox.setValue(player.getInputSource());

        // Update the player input method when the combo box selected value changes
        inputComboBox.valueProperty().addListener((_, _, value) -> {
            player.setInputSource(value);
            updateControllerDetectedText();
        });

        // Update the "controller detected" text when the player input source changes. Also update it now!
        updateControllerDetectedText();
        player.controllerDetectedProperty().addListener(_ -> updateControllerDetectedText());
    }

    private void updateControllerDetectedText() {
        if (player.getInputSource() instanceof PlayerInputSource.Controller) {
            // Show the gamepad label when the player uses a controller.
            gamepadLabel.setVisible(true);
            if (player.isControllerDetected()) {
                // Show the "controller detected" text in green if the controller is detected.
                gamepadLabel.setText("Manette détectée");
                gamepadLabel.setTextFill(Color.DARKGREEN);
            } else {
                // Show the "controller not detected" text in red if the controller is not detected.
                gamepadLabel.setText("Manette non détectée");
                gamepadLabel.setTextFill(Color.INDIANRED); // Red but it's indian
            }
        } else {
            // A keyboard's always connected, so no need for this label.
            gamepadLabel.setVisible(false);
        }
    }

    /// Gets the player profile this controller manages
    /// @return The player profile this controller manages.
    public PlayerProfile getPlayer() {
        return player;
    }

    /// Update the player number in the UI. (i.e. its index in the list + 1)
    ///
    /// @param number The new player number.
    public void setNumber(int number) {
        playerLabel.setText("Joueur " + number);
    }

    // Converts the input source to a string for the combobox
    private String inputSourceName(PlayerInputSource src) {
        return switch(src) {
            case PlayerInputSource.KeyboardArrows _ -> "Clavier : Flèches";
            case PlayerInputSource.KeyboardZQSD _ -> "Clavier : ZQSD";
            case PlayerInputSource.Controller(int slot) -> "Manette " + (slot + 1);
        };
    }
}
