package fr.connexe.ui.game.lobby;

import fr.connexe.ui.game.Player;
import fr.connexe.ui.game.PlayerInputSource;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class PlayerItemController implements Initializable {
    private PlayArcadeDialogController parentController;
    private LobbyPlayer player;
    private int number;

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

    @FXML
    private void handleDelete() {
        assert parentController != null;

        parentController.deletePlayer(player);
    }

    public void setup(PlayArcadeDialogController parentController, LobbyPlayer player) {
        this.parentController = parentController;
        this.player = Objects.requireNonNull(player);

        // Update the UI in turn
        icon.setFill(player.getColor());

        // Set the player input source in the combo box. Add it to the combobox list if it doesn't exist.
        if (!inputComboBox.getItems().contains(player.getInputSource())) {
            inputComboBox.getItems().add(player.getInputSource());
        }
        inputComboBox.setValue(player.getInputSource());

        player.controllerDetectedProperty().addListener((_, _, conn) -> {
            gamepadLabel.setText(conn.toString());
        });
    }

    public LobbyPlayer getPlayer() {
        return player;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;

        playerLabel.setText("Joueur " + number);
    }

    private String inputSourceName(PlayerInputSource src) {
        return switch(src) {
            case PlayerInputSource.KeyboardArrows _ -> "Clavier : Flèches";
            case PlayerInputSource.KeyboardZQSD _ -> "Clavier : ZQSD";
            case PlayerInputSource.Controller(int slot) -> "Manette " + (slot + 1);
        };
    }
}
