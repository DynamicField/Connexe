package fr.connexe.algo;

import fr.connexe.GraphMaze;
import java.util.ArrayList;
import java.util.List;

public class SuggestionPasAPas {

    public static void solveDFSStepByStep(GraphMaze maze, int current, List<Integer> visited) {
        // Dans le cas oû on est arrivé à la fin du labyrinthe
        if (current == maze.getEnd()) {
            System.out.println("✅ Arrivé au bout : " + current);
            return;
        }

        // Pour affiche le sommet visité actuellement
        System.out.println("➡️ Visite : " + current);

        // Pause (simulation du pas-à-pas)
        try {
            Thread.sleep(300); // 300 millisecondes
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Parcours des voisins non encore visités
        for (int voisin : maze.getEdges()[current]) {
            if (!visited.contains(voisin)) {
                visited.add(voisin); // marquer comme visité
                solveDFSStepByStep(maze, voisin, visited); // appel récursif
            }
        }
    }

    // Méthode de démarrage pour tester
    public static void run(GraphMaze maze) {
        List<Integer> visited = new ArrayList<>();
        visited.add(maze.getStart());
        solveDFSStepByStep(maze, maze.getStart(), visited);
    }
}
