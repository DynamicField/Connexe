package fr.connexe.ui;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MazeController {

    @FXML
    private VBox vboxLayout;

    public void createMazeFX(){
        GridPane dynamicGrid = LabyrinthClass.buildGrid(20, 10);

        // Wrap the grid in a StackPane to center and constrain its size
        StackPane root = new StackPane();
        root.getChildren().add(dynamicGrid);
        vboxLayout.getChildren().add(root);
        dynamicGrid.setAlignment(Pos.CENTER);


        // Limit the grid size to avoid overflowing
        dynamicGrid.setMaxSize(600, 600);  // Fixed maximum
        dynamicGrid.setPrefSize(600, 600); // Preferred

    }

}
