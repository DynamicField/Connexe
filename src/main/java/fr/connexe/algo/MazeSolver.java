package fr.connexe.algo;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/// Solves mazes with various algorithms.
public class MazeSolver {
    public static void main(String[] args) {
        // 0 - 1 - 2 - 3
        // -           -
        // 4 - 5   6 - 7
        // -
        // 8 - 9 - 10 - 11
        // -   -   -    -
        // 12  13  14 - 15
        var g = new GraphMaze(4, 4);
        g.connect(0, 1);
        g.connect(0, 4);
        g.connect(2, 3);
        g.connect(3, 7);
        g.connect(1, 2);
        g.connect(4, 5);
        g.connect(4, 8);
        g.connect(5, 9);
        g.connect(5, 6);
        g.connect(6, 7);
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
        System.out.println(g.toArrayMaze());
        Stack<Integer> pile;
        pile = MazeSolver.prepLeftHand(g, 0);
        System.out.println(pile);
        pile=MazeSolver.prepDFS(g,0);
        System.out.println(pile);
    }

    /**
     * @param labyrinth le labyrinthe à résoudre
     * @param num       le noeud dans lequel on est
     * @param visited   les noeuds déjà visités précedemment
     * @return pile la pile des noeuds à visiter pour résoudre le labyrinthe de la manière la plus courte
     */
    private static Stack<Integer> solveDFS(GraphMaze labyrinth, int num, List<Integer> visited) {
        Stack<Integer> maxflow = new Stack<Integer>();
        if (labyrinth.getEnd() == num) {
            maxflow.push(num);
            return maxflow;
        }
        Stack<Integer> subSon = new Stack<Integer>();
        List<Integer> subList = labyrinth.getEdges()[num];
        if (!subList.isEmpty()) {
            for (int son : subList) {
                if (!visited.contains(son)) {
                    List<Integer> subVisited = new ArrayList<>(visited); //création d'une copie de visited pour pouvoir y repasser à partir d'un autre chemin.
                    subVisited.add(son);
                    subSon = solveDFS(labyrinth, son, subVisited);
                    if (!subSon.isEmpty()) {
                        if (maxflow.isEmpty()) {
                            maxflow = new Stack<Integer>();
                            maxflow.addAll(subSon);
                        } else {
                            if (subSon.size() < maxflow.size()) {
                                maxflow = new Stack<Integer>();
                                maxflow.addAll(subSon);
                            }
                        }

                    }
                }
            }
        }
        if (!maxflow.isEmpty()) {
            maxflow.push(num);
        }
        return maxflow;
    }

    /**
     * @param labyrinth the maze to solve
     * @param mode      resolution mode (0 for DFS, 1 for step by step DFS)
     * @return stack of nodes to visit to solve the maze in the shortest way
     */
    public static Stack<Integer> prepDFS(GraphMaze labyrinth, int mode) {
        Stack<Integer> pile;
        List<Integer> visited = new ArrayList<>();
        visited.add(labyrinth.getStart());
        //if (mode == 0) {
        pile = solveDFS(labyrinth, labyrinth.getStart(), visited);
        //}
        /*else {
            pile=solveDFS2(labyrinth,labyrinth.getStart(), labyrinth.getStart();
        }*/
        return pile;
    }

    /**
     *
     * @param laby graph du labyrinthe
     * @param num noeud pù l'on se trouve
     * @param visited noeuds que l'on utilise pour avoir le chemin (sans les culs de sacs)
     * @param blocked noeuds qui mènent à ders culs de sacs
     * @return visited
     */
    private static Stack<Integer> solveLeftHand(GraphMaze laby, int num, Stack<Integer> visited, Stack<Integer> blocked ) {
        if (laby.getEnd() == num) {
            return visited;
        }
        List<Integer> subList = laby.getEdges()[num];
        if(!subList.isEmpty()) {
            if(subList.contains(num-laby.getWidth()) && !visited.contains(num-laby.getWidth()) && !blocked.contains(num-laby.getWidth())) {
                visited.push(num-laby.getWidth());
                return solveLeftHand(laby, num- laby.getWidth(), visited, blocked);
            }
            else if(subList.contains(num+1) && !visited.contains(num+1) && !blocked.contains(num+1)) {
                visited.push(num+1);
                return solveLeftHand(laby, num+1, visited, blocked);
            }
            else if(subList.contains(num+ laby.getWidth()) && !visited.contains(num+ laby.getWidth()) && !blocked.contains(num+laby.getWidth())) {
                visited.push(num+ laby.getWidth());
                return solveLeftHand(laby, num+laby.getWidth(), visited, blocked);
            }
            else if(subList.contains(num-1) && !visited.contains(num-1) && !blocked.contains(num-1)) {
                visited.push(num-1);
                return solveLeftHand(laby, num-1, visited, blocked);
            }
            else{
                blocked.push(num);
                visited.pop();
                if(visited.isEmpty()) {
                    return visited;
                }
                return solveLeftHand(laby, visited.peek(), visited, blocked);
            }
        }
        else {
            blocked.push(num);
            visited.pop();
            return solveLeftHand(laby, visited.peek(), visited, blocked);
        }
    }

    /**
     *
     * @param laby graph du labyrinthe
     * @param mode mode pas à pas ou non (1 ou 0)
     * @return le chemin qui mène à la sortie
     */
    public static Stack<Integer> prepLeftHand(GraphMaze laby, int mode){
        Stack<Integer> pile= new Stack<>();
        Stack<Integer> visited= new Stack<>();
        visited.push(laby.getStart());
        Stack<Integer> blocked = new Stack<>();
        //if (mode == 0) {
        visited = solveLeftHand(laby, laby.getStart(), visited, blocked);
        //}
        /*else {
            pile=solveDFS2(labyrinth,labyrinth.getStart(), labyrinth.getStart();
        }*/
        do{
            pile.push(visited.pop());
        }while(!visited.isEmpty());
        return pile;
    }

}
