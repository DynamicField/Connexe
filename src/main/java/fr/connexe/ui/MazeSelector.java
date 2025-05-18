package fr.connexe.ui;

import fr.connexe.algo.ArrayMaze;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

public class MazeSelector {
    //Quand on selectionne un autre mur, l'ancien doit être deselectionné
    private Region lastSelectedCell = null;
    private Region lastNeighborCell = null;

    public MazeSelector(
    ) {}

    public void selectWall(Region gridCell, ArrayMaze arrayMaze, int row, int col, GridPane grid) {
        gridCell.setOnMouseClicked(event -> {
            double x = event.getX();
            double y = event.getY();
            double width = gridCell.getWidth();
            double height = gridCell.getHeight();
            double margin = 10;

            String selectedSide = null;
            //Les murs sont constitués du mur de la cellule actuelle (haut, droite, bas, gauche) et du mur adjacent au mur de la cellule actuelle (donc pour haut -> bas, droite -> gauche, bas -> haut, gauche -> droite)
            //Donc pour effacer et récupérer un mur dans l'interface graphique, il faut pouvoir supprimer et faire réapparaitre ces deux murs en même temps
            String neighborSide = null;
            int neighborRow = row, neighborCol = col;

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

            // Réinitialise le mur précédent
            if (lastSelectedCell != null) {
                resetBorderColor(lastSelectedCell);
            }
            //(et celui du voisin)
            if (lastNeighborCell != null) {
                resetBorderColor(lastNeighborCell);
            }

            // Met à jour le mur sélectionné si il existe
            if (selectedSide != null) {
                setBorderColor(gridCell, selectedSide);
                lastSelectedCell = gridCell;

                // Met à jour le mur opposé de la cellule voisine si elle existe
                if (neighborRow >= 0 && neighborCol >= 0 && neighborRow < arrayMaze.getHeight() && neighborCol < arrayMaze.getWidth()) {
                    Region neighborCell = getCellFromGrid(grid, neighborCol, neighborRow);
                    if (neighborCell != null) {
                        setBorderColor(neighborCell, neighborSide);
                        lastNeighborCell = neighborCell;
                    } else {
                        lastNeighborCell = null;
                    }
                } else {
                    lastNeighborCell = null;
                }
            }
        });
    }

    // Remet le bord sélectionné à sa couleur d'origine (noir ou transparent)
    public void setBorderColor(Region cell, String side) {
        String style = cell.getStyle(); //Format du style (on ne s'intéresse ici qu'au couleurs) : -fx-background-color: white;-fx-border-color: haut:couleur1 droite:couleur2 gauche:couleur3 bas:couleur4; -fx-border-width: 2 2 2 2;
        String[] colors = {"", "", "", ""};
        //On récupère les 4 couleurs dans le style
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("-fx-border-color: ([^;]+);")
                .matcher(style);
        if (m.find()) {
            //Et on en fait un tableau
            String[] baseColors = m.group(1).split(" ");
            System.arraycopy(baseColors, 0, colors, 0, 4);
        }
        switch (side) {
            case "top":    colors[0] = "red"; break;
            case "right":  colors[1] = "red"; break;
            case "bottom": colors[2] = "red"; break;
            case "left":   colors[3] = "red"; break;
        }
        style = style.replaceAll("-fx-border-color: [^;]+;", "");
        style += "-fx-border-color: " + String.join(" ", colors) + ";";
        cell.setStyle(style);
    }

    public void resetBorderColor(Region cell){
        // Récupère le style avant changement qu'on aura sauvegardé dans MazeRenderer et l'applique à nouveau à la cellule
        String initialStyle = (String) cell.getProperties().get("initialStyle");
        cell.setStyle(initialStyle);
    }

    public Region getCellFromGrid(Parent grid, int col, int row) {
        if (!(grid instanceof GridPane)) return null;
        int nodeCol;int nodeRow;
        for (Node node : ((GridPane) grid).getChildren()) {
            nodeCol = GridPane.getColumnIndex(node);
            nodeRow = GridPane.getRowIndex(node);
            if (nodeCol == col && nodeRow == row) {
                return (Region) node;
            }
        }
        return null;
    }
}
