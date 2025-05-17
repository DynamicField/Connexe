package fr.connexe.ui;

import fr.connexe.algo.ArrayMaze;
import fr.connexe.algo.Cell;
import fr.connexe.algo.GraphMaze;
import fr.connexe.algo.Point;
import fr.connexe.algo.generation.MazeGenLog;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.*;

public class MazeRenderer {

    private MazeGenLog log;
    private GraphMaze graphMaze;

    // Pour mémoriser le dernier mur sélectionné
    private Region lastSelectedCell = null;
    private String lastSelectedSide = null;
    private Region lastNeighborCell = null;
    private String lastNeighborSide = null;

    public MazeRenderer() {}

    /// Exemple par défaut pour test
    public void setDefaultExample(){
        var g = new GraphMaze(4, 4);
        g.connect(0, 1);
        g.connect(0, 4);
        g.connect(2, 3);
        g.connect(3, 7);
        g.connect(1, 2);
        g.connect(4, 5);
        g.connect(4, 8);
        g.connect(6, 7);
        g.connect(6, 10);
        g.connect(8, 9);
        g.connect(8, 12);
        g.connect(9, 13);
        g.connect(9, 10);
        g.connect(10, 11);
        g.connect(10, 14);
        g.connect(11, 15);
        g.connect(14, 15);
        g.setStart(0);
        g.setEnd(15);
        this.graphMaze = g;
        this.log = null;
        System.out.println(g);
    }

    /// Construit la grille JavaFX représentant le labyrinthe
    public GridPane buildGrid() {
        assert graphMaze != null : "GraphMaze must be set before calling buildGrid()";

        int rows = graphMaze.getHeight();
        int cols = graphMaze.getWidth();
        ArrayMaze arrayMaze = graphMaze.toArrayMaze();

        GridPane grid = new GridPane();
        double cellScale = Math.max(rows, cols);

        for (int i = 0; i < cols; i++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setPercentWidth(100.0 / cellScale);
            grid.getColumnConstraints().add(colConstraints);
        }
        for (int i = 0; i < rows; i++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setPercentHeight(100.0 / cellScale);
            grid.getRowConstraints().add(rowConstraints);
        }

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Region gridCell = new Region();
                StringBuilder style = new StringBuilder("-fx-background-color: white;");

                Point vertexCoordinates = new Point(col, row);
                Cell mazeCell = arrayMaze.getCell(vertexCoordinates);

                int topWidth = 2; int rightWidth = 2; int bottomWidth = 2; int leftWidth = 2;
                String topColor = mazeCell.wallUp() ? "black" : "transparent";
                String rightColor = mazeCell.wallRight() ? "black" : "transparent";
                String bottomColor = mazeCell.wallDown() ? "black" : "transparent";
                String leftColor = mazeCell.wallLeft() ? "black" : "transparent";

                if (row == 0) topWidth = 4;
                if (row == rows - 1) bottomWidth = 4;
                if (col == 0) leftWidth = 4;
                if (col == cols - 1) rightWidth = 4;

                style.append("-fx-border-color: ")
                        .append(topColor).append(" ")
                        .append(rightColor).append(" ")
                        .append(bottomColor).append(" ")
                        .append(leftColor).append(";");

                style.append(" -fx-border-width: ")
                        .append(topWidth).append(" ")
                        .append(rightWidth).append(" ")
                        .append(bottomWidth).append(" ")
                        .append(leftWidth).append(";");

                gridCell.setStyle(style.toString());

                selectWall(gridCell, arrayMaze, row, col, grid);

                GridPane.setHgrow(gridCell, Priority.ALWAYS);
                GridPane.setVgrow(gridCell, Priority.ALWAYS);
                gridCell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                grid.add(gridCell, col, row);
            }
        }
        return grid;
    }

    // Gestion du clic sur un mur : affichage en pointillé et synchronisation avec la cellule voisine
    public void selectWall(Region gridCell, ArrayMaze arrayMaze, int row, int col, GridPane grid) {
        gridCell.setOnMouseClicked(event -> {
            double x = event.getX();
            double y = event.getY();
            double width = gridCell.getWidth();
            double height = gridCell.getHeight();
            double margin = 15;

            String selectedSide = null;
            int neighborRow = row, neighborCol = col;
            String neighborSide = null;

            if (y < margin) {
                selectedSide = "top";
                neighborRow = row - 1;
                neighborSide = "bottom";
            } else if (y > height - margin) {
                selectedSide = "bottom";
                neighborRow = row + 1;
                neighborSide = "top";
            } else if (x < margin) {
                selectedSide = "left";
                neighborCol = col - 1;
                neighborSide = "right";
            } else if (x > width - margin) {
                selectedSide = "right";
                neighborCol = col + 1;
                neighborSide = "left";
            }

            // Réinitialise le mur précédent (et celui du voisin)
            if (lastSelectedCell != null && lastSelectedSide != null) {
                resetBorderColor(lastSelectedCell, lastSelectedSide);
            }
            if (lastNeighborCell != null && lastNeighborSide != null) {
                resetBorderColor(lastNeighborCell, lastNeighborSide);
            }

            if (selectedSide != null) {
                setBorderColor(gridCell, selectedSide);
                lastSelectedCell = gridCell;
                lastSelectedSide = selectedSide;

                // Met à jour le mur opposé de la cellule voisine si elle existe
                if (neighborRow >= 0 && neighborCol >= 0 &&
                        neighborRow < arrayMaze.getHeight() && neighborCol < arrayMaze.getWidth()) {
                    Region neighborCell = getCellFromGrid(grid, neighborCol, neighborRow);
                    if (neighborCell != null && neighborSide != null) {
                        setBorderColor(neighborCell, neighborSide);
                        lastNeighborCell = neighborCell;
                        lastNeighborSide = neighborSide;
                    } else {
                        lastNeighborCell = null;
                        lastNeighborSide = null;
                    }
                } else {
                    lastNeighborCell = null;
                    lastNeighborSide = null;
                }
            }
        });
    }

    // Met le bord sélectionné en pointillé
    private void setBorderColor(Region cell, String side) {
        String style = cell.getStyle();
        // Récupère les couleurs actuelles
        String[] colors = {"black", "black", "black", "black"};
        // Recherche les couleurs actuelles dans le style
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("-fx-border-color: ([^;]+);")
                .matcher(style);
        if (m.find()) {
            String[] currentColors = m.group(1).split(" ");
            for (int i = 0; i < Math.min(4, currentColors.length); i++) {
                colors[i] = currentColors[i];
            }
        }
        // Modifie seulement le côté sélectionné
        switch (side) {
            case "top":    colors[0] = "red"; break;
            case "right":  colors[1] = "red"; break;
            case "bottom": colors[2] = "red"; break;
            case "left":   colors[3] = "red"; break;
        }
        // Reconstruit le style
        style = style.replaceAll("-fx-border-color: [^;]+;", "");
        style += "-fx-border-color: " + String.join(" ", colors) + ";";
        cell.setStyle(style);
    }

    private void resetBorderColor(Region cell, String side) {
        String style = cell.getStyle();
        // Récupère les couleurs actuelles
        String[] colors = {"black", "black", "black", "black"};
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("-fx-border-color: ([^;]+);")
                .matcher(style);
        if (m.find()) {
            String[] currentColors = m.group(1).split(" ");
            for (int i = 0; i < Math.min(4, currentColors.length); i++) {
                colors[i] = currentColors[i];
            }
        }
        // Remet la couleur d'origine (noir ou transparent) sur le côté concerné
        // Si le mur était rouge, on regarde la couleur initiale dans le style de base
        String[] initialColors = {"black", "black", "black", "black"};
        java.util.regex.Matcher mInit = java.util.regex.Pattern
                .compile("-fx-border-color: ([^;]+);")
                .matcher(cell.getProperties().getOrDefault("initialStyle", style).toString());
        if (mInit.find()) {
            String[] baseColors = mInit.group(1).split(" ");
            for (int i = 0; i < Math.min(4, baseColors.length); i++) {
                initialColors[i] = baseColors[i];
            }
        }
        switch (side) {
            case "top":    colors[0] = colors[0].equals("red") ? initialColors[0] : colors[0]; break;
            case "right":  colors[1] = colors[1].equals("red") ? initialColors[1] : colors[1]; break;
            case "bottom": colors[2] = colors[2].equals("red") ? initialColors[2] : colors[2]; break;
            case "left":   colors[3] = colors[3].equals("red") ? initialColors[3] : colors[3]; break;
        }
        style = style.replaceAll("-fx-border-color: [^;]+;", "");
        style += "-fx-border-color: " + String.join(" ", colors) + ";";
        cell.setStyle(style);
    }

    // Récupère la Region d'une cellule voisine dans le GridPane
    private Region getCellFromGrid(Parent grid, int col, int row) {
        if (!(grid instanceof GridPane)) return null;
        for (Node node : ((GridPane) grid).getChildren()) {
            Integer nodeCol = GridPane.getColumnIndex(node);
            Integer nodeRow = GridPane.getRowIndex(node);
            if (nodeCol != null && nodeRow != null && nodeCol == col && nodeRow == row) {
                return (Region) node;
            }
        }
        return null;
    }

    public GraphMaze getGraphMaze() {
        return graphMaze;
    }

    public void setGraphMaze(GraphMaze graphMaze) {
        this.graphMaze = graphMaze;
    }

    public MazeGenLog getLog() {
        return log;
    }

    public void setLog(MazeGenLog log) {
        this.log = log;
    }

    public void setStartVertex(int startVertex) {
        graphMaze.setStart(startVertex);
    }

    public void setEndVertex(int endVertex) {
        graphMaze.setEnd(endVertex);
    }
}