package fr.connexe.ui;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

///  Controller to display any Maze related operations on the view (creation, editing, solving, etc...)
public class MazeController {

    @FXML
    private VBox vboxLayout;

    ///  Display a maze in the VBox of the main scene
    public void createMazeFX(MazeRenderer mazeRenderer){
        vboxLayout.getChildren().clear();
        GridPane dynamicGrid = mazeRenderer.buildGrid();

        // Wrap the grid in a StackPane to center and constrain its size
        StackPane root = new StackPane();
        root.getChildren().add(dynamicGrid);

        // Add margin space to the grid inside the StackPane
        StackPane.setMargin(dynamicGrid, new Insets(20)); // 20 px margin on all sides

        // Bind GridPane size to VBox size minus margin (for dynamic growth)
        dynamicGrid.maxWidthProperty().bind(vboxLayout.widthProperty().subtract(40));
        dynamicGrid.maxHeightProperty().bind(vboxLayout.heightProperty().subtract(40));
        dynamicGrid.prefWidthProperty().bind(vboxLayout.widthProperty().subtract(40));
        dynamicGrid.prefHeightProperty().bind(vboxLayout.heightProperty().subtract(40));

        dynamicGrid.setAlignment(Pos.CENTER);
        vboxLayout.getChildren().add(root);
    }

}
