package fr.connexe;

import fr.connexe.algo.GraphMaze;
import fr.connexe.algo.MazeSolver;
import fr.connexe.algo.generation.MazeGenResult;
import fr.connexe.algo.generation.MazeGenerator;

import java.io.*;
import java.util.Scanner;
import java.util.Stack;

/// Allows users to see/generate/solve Mazes in the terminal with a menu
public class ConsoleApp {

    // Current maze instance (loaded or generated)
    private static GraphMaze currentMaze = null;

    // Input scanner
    private static final Scanner scanner = new Scanner(System.in);

    // The UTF-8 out stream because for some reason gradle doesn't want to output UTF-8 properly????
    private static PrintStream out;

    public static void main(String[] args) {
        System.out.println("ENCODING = " + System.getProperty("file.encoding"));

        try {
            out = new PrintStream(System.out, true, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            out = System.out; // Give up on UTF-8
        }

        boolean running = true;

        // Main interactive console loop
        while (running) {
            out.println("\n=== MENU CONSOLE ===");
            out.println("1. Générer un labyrinthe");
            out.println("2. Afficher le labyrinthe");
            out.println("3. Résoudre le labyrinthe");
            out.println("4. Sauvegarder dans un fichier");
            out.println("5. Charger depuis un fichier");
            out.println("6. Quitter");
            out.print("Votre choix : ");

            String input = scanner.nextLine();

            switch (input) {
                case "1" -> generateMaze();
                case "2" -> displayMaze();
                case "3" -> solveMaze();
                case "4" -> saveMaze();
                case "5" -> loadMaze();
                case "6" -> running = false;
                default -> out.println("Option invalide.");
            }
        }

        out.println("Au revoir !");
    }

    // Generates a new maze based on user input size
    private static void generateMaze() {
        //Give the Width and Height of the maze
        out.print("Taille du labyrinthe: ");
        int size;
        try {
            size = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            out.println("Taille invalide.");
            return;

        }
        out.print("La hauteur  de labyrinthe: ");
        int height;
        try {
            height = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            out.println("Taille invalide.");
            return;
        }
        if (size <= 1 || height <= 1) {
            out.println("Taille invalide.");
            return;
        }
        // Give the seed if needed
        out.print("Donne moi la seed: ");

        String s1 = scanner.nextLine();
        Long seed;
        if (s1.isBlank()) {
            seed = null;
        } else {
            try {
                seed = Long.parseLong(s1);
            } catch (NumberFormatException e) {
                out.println("seed invalide.");
                return;
            }
        }

        // Give the algorithm to generate the maze( DFS et Prim )
        out.print("Veuillez me dire votre algo de generation ( DFS ou Prim): ");
        String s = scanner.nextLine();

        if (s.equalsIgnoreCase("DFS")) {
            MazeGenResult mazeGenResult = MazeGenerator.makeDFS(size, height, seed);
            currentMaze = mazeGenResult.maze();
        } else if (s.equalsIgnoreCase("Prim")) {
            MazeGenResult mazeGenResult = MazeGenerator.makePrim(size, height, seed);
            currentMaze = mazeGenResult.maze();
        } else {
            out.println("Algo invalide.");
            return;
        }

        out.println("Labyrinthe généré.");
    }

    // Displays the maze using its ASCII representation
    private static void displayMaze() {
        if (currentMaze == null) {
            out.println("Aucun labyrinthe à afficher.");
            return;
        }

        out.println(currentMaze);
    }

    // Solves the current maze using MazeSolver
    private static void solveMaze() {
        if (currentMaze == null) {
            out.println("Aucun labyrinthe à résoudre.");
            return;
        }

        if (currentMaze.getStart() == -1 || currentMaze.getEnd() == -1) {
            currentMaze.setStart(0);
            currentMaze.setEnd(currentMaze.getNumCells() - 1);
        }

        out.println("Choisissez un algorithme de résolution :");
        out.println("1. DFS");
        out.println("2. LeftHand");
        out.println("3. Clockwise");
        out.println("4. Dijkstra");
        out.println("5. A* ");
        out.print("Votre choix : ");
        String algoChoice = scanner.nextLine();

        Stack<Integer> chemin = new Stack<>();

        switch (algoChoice) {
            case "1":
                chemin = MazeSolver.prepDFS(currentMaze, 0);
                break;
            case "2":
                chemin = MazeSolver.prepLeftHand(currentMaze);
                break;
            case "3":
                chemin = MazeSolver.prepClockwise(currentMaze);
                break;
            case "4":
                chemin = MazeSolver.solveDijkstra(currentMaze);
                break;
            case "5":
                out.println("L'algorithme A* sera ajouté prochainement par Mathis :)");
                return;
            default:
                out.println("Choix invalide.");
                return;
        }

        if (chemin.isEmpty()) {
            out.println("Aucun chemin trouvé !");
        } else {
            out.println("Chemin trouvé : " + chemin);
        }

        out.println(currentMaze);
    }


    // Saves the current maze to a file
    private static void saveMaze() {
        if (currentMaze == null) {
            out.println("Aucun labyrinthe à sauvegarder.");
            return;
        }

        out.print("Nom du fichier de sauvegarde (ex : maze1.con) : ");
        String filename = scanner.nextLine();

        try (ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(filename))) {
            stream.writeObject(currentMaze);
            out.println("Labyrinthe sauvegardé avec succès.");
        } catch (IOException e) {
            out.println("Erreur lors de la sauvegarde :");
            e.printStackTrace();
        }
    }

    // Loads a maze from a file
    private static void loadMaze() {
        out.print("Nom du fichier à charger (ex : maze1.con) : ");
        String filename = scanner.nextLine();

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            currentMaze = (GraphMaze) in.readObject();
            out.println("Labyrinthe chargé avec succès.");
        } catch (IOException | ClassNotFoundException e) {
            out.println("Erreur lors du chargement :");
            e.printStackTrace();
        }
    }
}
