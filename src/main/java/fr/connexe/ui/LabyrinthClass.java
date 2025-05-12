package fr.connexe.ui;

import javafx.scene.layout.*;

// placeholder class pour le vrai labyrinth generator
public class LabyrinthClass {

    private int rows;
    private int cols;

    public GridPane buildGrid() {
        GridPane grid = new GridPane();

        double cellScale = Math.max(rows, cols);

        // Set column constraints (once per column)
        for (int i = 0; i < cols; i++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setPercentWidth(100.0 / cellScale);
            grid.getColumnConstraints().add(colConstraints);
        }

        // Set row constraints (once per row)
        for (int i = 0; i < rows; i++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setPercentHeight(100.0 / cellScale);
            grid.getRowConstraints().add(rowConstraints);
        }

        // Add empty regions to each cell
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Region cell = new Region();
                cell.setStyle("-fx-background-color: white; -fx-border-color: black;");
                GridPane.setHgrow(cell, Priority.ALWAYS);
                GridPane.setVgrow(cell, Priority.ALWAYS);
                cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                grid.add(cell, col, row);
            }
        }

        return grid;
    }


    public int getCols() {
        return cols;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }
}
