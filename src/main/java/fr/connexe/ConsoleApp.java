package fr.connexe;

import fr.connexe.algo.GraphMaze;
import fr.connexe.algo.MazeSolver;
import fr.connexe.algo.generation.MazeGenResult;
import fr.connexe.algo.generation.MazeGenerator;
import java.util.List;
import java.util.Stack;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.InputMismatchException;
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

    /// The main method of the console application.
    ///
    /// @param args command line arguments (not used yet)
    public static void main(String[] args) {
        out = new PrintStream(System.out, true, StandardCharsets.UTF_8);

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

    // Don't allow instantiation of this class
    private ConsoleApp() {}

    // Generates a new maze based on user input size
    private static void generateMaze() {
        //Give the Width and Height of the maze
        out.print("Largeur du labyrinthe: ");
        int width;
        try {
            width = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            out.println("Taille invalide.");
            return;

        }
        out.print("Hauteur de labyrinthe: ");
        int height;
        try {
            height = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            out.println("Taille invalide.");
            return;
        }

        // Check the maze dimensions
        if (width <= 1 || height <= 1) {
            out.println("Taille trop petite.");
            return;
        }
        if (width > 50 || height > 50) {
            out.println("Taille trop grande.");
            return;
        }
        // Give the seed if needed
        out.print("Seed du labyrinthe (vide = généré auto): ");

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
        out.print("Veuillez me dire votre algo de generation (DFS ou Prim): ");
        String s = scanner.nextLine();

        // Run the generation algorithm
        MazeGenResult genResult;
        if (s.equalsIgnoreCase("DFS")) {
            genResult = MazeGenerator.makeDFS(width, height, seed);
        } else if (s.equalsIgnoreCase("Prim")) {
            genResult = MazeGenerator.makePrim(width, height, seed);
        } else {
            out.println("Algo invalide.");
            return;
        }

        // See if we want to have an imperfect maze
        out.print("Voulez-vous un labyrinthe parfait ? [O/N] ");
        String answer = scanner.nextLine();

        if (answer.startsWith("n") || answer.startsWith("N")) {
            // Ask for chaos (default to 0.2)
            out.print("Quelle quantité de chaos voulez-vous ? (nombre de 0.0 à 1.0) ");
            float chaos = 0.2f;
            try {
                chaos = scanner.nextFloat();
                chaos = Math.clamp(chaos, 0.0f, 1.0f);
            } catch (InputMismatchException e)  {
                out.println("Utilisation de la valeur par défaut : " + chaos);
            }

            // Introduce some chaos!
            MazeGenerator.introduceChaos(genResult, chaos, seed);
        }

        // Set the maze to what we generated
        currentMaze = genResult.maze();

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

        out.println("Choisissez un algorithme de résolution :");
        out.println("1. DFS");
        out.println("2. LeftHand");
        out.println("3. Clockwise");
        out.println("4. Dijkstra");
        out.println("5. A* ");
        out.print("Votre choix : ");
        String algoChoice = scanner.nextLine();

        // Ask if user wants step-by-step mode for supported algorithms
        boolean stepByStep = false;
        out.print("Souhaitez-vous une résolution pas à pas ? [O/N] : ");
        stepByStep = scanner.nextLine().trim().equalsIgnoreCase("O");

        Stack<Integer> path = null;
        List<Stack<Integer>> steps = null;

        switch (algoChoice) {
            case "1":
                if (stepByStep) {
                    steps = MazeSolver.prepDFS2(currentMaze);
                } else {
                    path = MazeSolver.prepDFS(currentMaze);
                }
                break;
            case "2":
                if (stepByStep) {
                    steps = MazeSolver.prepLeftHand2(currentMaze);
                } else {
                    path = MazeSolver.prepLeftHand(currentMaze); // no step-by-step available
                }
                break;
            case "3":
                if (stepByStep) {
                    steps = MazeSolver.prepClockwise2(currentMaze);
                } else {
                    path = MazeSolver.prepClockwise(currentMaze);
                }
                break;
            case "4":
                if (stepByStep) {
                    steps = MazeSolver.solveDijkstra2(currentMaze);
                } else {
                    path = MazeSolver.solveDijkstra(currentMaze);
                }
                break;
            case "5":
                if (stepByStep) {
                    steps = MazeSolver.solveAStar(currentMaze);
                } else {
                    path = MazeSolver.prepAStar(currentMaze);
                }
                break;
            default:
                out.println("Choix invalide.");
                return;
        }

        // Step-by-step display
        if (stepByStep) {
            for (Stack<Integer> step : steps) {
                out.println("Étape : " + step);
                try {
                    Thread.sleep(50); // 0.05 second delay between steps
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } else {
            // Not step-by-step
            if (path == null || path.isEmpty()) {
                out.println("Aucun chemin trouvé !");
            } else {
                out.println("Chemin trouvé : " + path);
            }
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
        String filename = scanner.nextLine().trim();
        if (filename.isEmpty()) {
            out.println("Nom de fichier invalide.");
            return;
        }


        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            currentMaze = (GraphMaze) in.readObject();
            out.println("Labyrinthe chargé avec succès.");
        } catch (IOException | ClassNotFoundException e) {
            out.println("Erreur lors du chargement :");
            e.printStackTrace();
        }
    }
}
