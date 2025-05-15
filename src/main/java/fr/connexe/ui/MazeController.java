package fr.connexe.ui;

import fr.connexe.algo.GraphMaze;
import fr.connexe.algo.MazeSerializationException;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.*;

///  Controller to display any Maze related operations on the view (creation, editing, solving, etc...)
public class MazeController {

    private MazeRenderer mazeRenderer;

    @FXML
    private VBox vboxLayout;

    ///  Display a maze in the VBox of the main scene
    public void createMazeFX(){
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

        // Console view
        System.out.println(mazeRenderer.getGraphMaze());
    }

    /// Saves the current rendered maze into a file
    public void saveMaze(File file) throws MazeSerializationException, FileNotFoundException {
        assert mazeRenderer.getGraphMaze() != null : "MazeRenderer must have a maze to be saved";
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        mazeRenderer.getGraphMaze().save(fileOutputStream);
    }

    /// Loads a file containing maze data and renders it on the view
    public void loadMaze(File file) throws MazeSerializationException, FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(file);
        GraphMaze maze = GraphMaze.load(fileInputStream);
        mazeRenderer.setGraphMaze(maze);
        mazeRenderer.setLog(null); // remove log of previous generation
        createMazeFX();
    }

    public void setMazeRenderer(MazeRenderer mazeRenderer) {
        this.mazeRenderer = mazeRenderer;
    }

    public MazeRenderer getMazeRenderer() {
        return mazeRenderer;
    }
}
