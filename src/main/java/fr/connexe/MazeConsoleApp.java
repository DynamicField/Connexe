package fr.connexe;

import fr.connexe.algo.GraphMaze;
import fr.connexe.sauvegarde.LabyrinthSerializer;

import java.io.*;
import java.util.Scanner;

public class MazeConsoleApp {
    public static void afficherLabyrinthe(GraphMaze maze) {
        int width = maze.getWidth();
        int height = maze.getHeight();

        for (int y = 0; y < height; y++) {
            // Ligne des murs du haut
            for (int x = 0; x < width; x++) {
                int id = y * width + x;
                System.out.print("+");
                System.out.print(maze.isConnected(id, id - width) ? "   " : "---");
            }
            System.out.println("+");

            // Ligne des cellules et des murs à gauche
            for (int x = 0; x < width; x++) {
                int id = y * width + x;
                System.out.print(maze.isConnected(id, id - 1) ? " " : "|");
                if (id == maze.getStart()) {
                    System.out.print(" S ");
                } else if (id == maze.getEnd()) {
                    System.out.print(" E ");
                } else {
                    System.out.print("   ");
                }
            }
            System.out.println("|");
        }

        // Dernière ligne de bas
        for (int x = 0; x < width; x++) {
            System.out.print("+---");
        }
        System.out.println("+");
    }


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        GraphMaze maze = null;

        while (true) {
            System.out.println("\n=== Menu ===");
            System.out.println("1. Générer un labyrinthe");
            System.out.println("2. Résoudre le labyrinthe");
            System.out.println("3. Sauvegarder");
            System.out.println("4. Restaurer");
            System.out.println("0. Quitter");

            System.out.print("Votre choix : ");
            int choix = scanner.nextInt();

            switch (choix) {
                case 1:
                    System.out.print("Largeur : ");
                    int largeur = scanner.nextInt();
                    System.out.print("Hauteur : ");
                    int hauteur = scanner.nextInt();

                    maze = new GraphMaze(largeur, hauteur);

                    // Appel à la fonction de génération automatique
                    maze.generatePerfectMaze(); //  à adapter selon le nom réel dans votre projet

                    maze.setStart(0);
                    maze.setEnd((largeur * hauteur) - 1);

                    System.out.println("Labyrinthe généré !");
                    afficherLabyrinthe(maze);
                    break;


                case 2:
                    if (maze == null) {
                        System.out.println("Veuillez d'abord générer un labyrinthe.");
                        break;
                    }
                    System.out.println("Résolution en cours (à intégrer plus tard)...");
                    break;

                case 3:
                    if (maze == null) {
                        System.out.println("Aucun labyrinthe à sauvegarder.");
                        break;
                    }
                    try {
                        LabyrinthSerializer.saveLabyrinth(maze, new File("maze.dat"));
                        System.out.println("Sauvegarde effectuée.");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                case 4:
                    try {
                        maze = LabyrinthSerializer.loadLabyrinth(new FileInputStream("maze.dat"));
                        System.out.println("Labyrinthe restauré !");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case 0:
                    System.out.println("Au revoir !");
                    return;

                default:
                    System.out.println("Choix invalide.");
            }
        }
    }
}
