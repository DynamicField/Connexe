package fr.connexe.algo;

import java.util.*;

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
        /*pile = MazeSolver.prepLeftHand(g);
        System.out.println(pile);
        pile = MazeSolver.prepDFS(g);
        System.out.println(pile);
        pile = MazeSolver.solveDjisktra(g);
        System.out.println(pile);
*/
        Stack<Stack<Integer>> pile2;
        pile2=MazeSolver.prepDFS2(g);
        System.out.println("DFS2:" +pile2);
        pile2=MazeSolver.prepLeftHand2(g);
        System.out.println(pile2);
        pile2=MazeSolver.solveDjisktra2(g);
        System.out.println("Dijkstra2:" +pile2);
    }


    /**
     * @param labyrinth the maze to solve
     * @param num       the node we are at
     * @param visited   the nodes we already visited
     * @return the stack of nodes which is the best path
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
     * @return stack of nodes to visit to solve the maze in the shortest way
     */
    public static Stack<Integer> prepDFS(GraphMaze labyrinth) {
        Stack<Integer> pile;
        List<Integer> visited = new ArrayList<>();
        visited.add(labyrinth.getStart());
        pile = solveDFS(labyrinth, labyrinth.getStart(), visited);
        return pile;
    }

    /**
     * @param laby    the maze to solve
     * @param num     the node we are at
     * @param visited the nodes that we already visited without the dead ends
     * @param blocked the nodes that lead to a dead end
     * @return visited
     */
    private static Stack<Integer> solveLeftHand(GraphMaze laby, int num, Stack<Integer> visited, Stack<Integer> blocked) {
        if (laby.getEnd() == num) {
            return visited;
        }
        List<Integer> subList = laby.getEdges()[num];
        if (!subList.isEmpty()) {
            if (subList.contains(num - laby.getWidth()) && !visited.contains(num - laby.getWidth()) && !blocked.contains(num - laby.getWidth())) {
                visited.push(num - laby.getWidth());
                return solveLeftHand(laby, num - laby.getWidth(), visited, blocked);
            } else if (subList.contains(num + 1) && !visited.contains(num + 1) && !blocked.contains(num + 1)) {
                visited.push(num + 1);
                return solveLeftHand(laby, num + 1, visited, blocked);
            } else if (subList.contains(num + laby.getWidth()) && !visited.contains(num + laby.getWidth()) && !blocked.contains(num + laby.getWidth())) {
                visited.push(num + laby.getWidth());
                return solveLeftHand(laby, num + laby.getWidth(), visited, blocked);
            } else if (subList.contains(num - 1) && !visited.contains(num - 1) && !blocked.contains(num - 1)) {
                visited.push(num - 1);
                return solveLeftHand(laby, num - 1, visited, blocked);
            } else {
                blocked.push(num);
                visited.pop();
                if (visited.isEmpty()) {
                    return visited;
                }
                return solveLeftHand(laby, visited.peek(), visited, blocked);
            }
        } else {
            blocked.push(num);
            visited.pop();
            if(!visited.isEmpty()){
                return visited;
            }
            return solveLeftHand(laby, visited.peek(), visited, blocked);
        }
    }

    /**
     * @param laby the maze to solve
     * @return stack of nodes to visit to solve the maze (not always the shortest way
     */
    public static Stack<Integer> prepLeftHand(GraphMaze laby) {
        Stack<Integer> pile = new Stack<>();
        Stack<Integer> visited = new Stack<>();
        visited.push(laby.getStart());
        Stack<Integer> blocked = new Stack<>();
        visited = solveLeftHand(laby, laby.getStart(), visited, blocked);
        do {
            pile.push(visited.pop());
        } while (!visited.isEmpty());
        System.out.println(blocked);
        return pile;
    }

    /**
     * @param laby the maze to solve
     * @return stack of nodes which is the best path
     */
    public static Stack<Integer> solveDjisktra(GraphMaze laby) {
        int n = laby.getEdges().length;
        int[] dist = new int[n];
        int[] fathers = new int[n];
        boolean[] visited = new boolean[n];

        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(fathers, -1);
        dist[laby.getStart()] = 0;

        PriorityQueue<Integer> queue = new PriorityQueue<>(Comparator.comparingInt(x -> dist[x]));//les distances les plus faibles en premier
        queue.add(laby.getStart());

        while (!queue.isEmpty()) {
            int current = queue.poll();
            if (visited[current]) {
                continue;
            }
            visited[current] = true;

            if (current == laby.getEnd()) {
                break;
            }
            for (int edge : laby.getEdges()[current]) {
                if (dist[current] + 1 < dist[edge]) {
                    dist[edge] = dist[current] + 1;
                    fathers[edge] = current;
                    queue.add(edge);
                }
            }
        }
        Stack<Integer> path = new Stack<>();
        if (dist[laby.getStart()] == Integer.MAX_VALUE) {
            return path;
        }
        for (int at = laby.getEnd(); at != laby.getStart(); at = fathers[at]) {
            path.push(at);
        }
        path.push(laby.getStart());

        return path;
    }

    /**
     * @param labyrinth the maze
     * @param num       the node we are at
     * @param visited   the nodes we visited
     * @return pile the stack of nodes which is the best path
    */
    @SuppressWarnings("unchecked")
    private static Stack<Stack<Integer>> solveDFS2(GraphMaze labyrinth, int num, List<Integer> visited, Stack<Integer> currentPath) {
        Stack<Stack<Integer>> pile = new Stack<>();
        currentPath.push(num);

        List<Integer> subList = labyrinth.getEdges()[num];
        boolean deadEnd = true;

        if (subList != null && !subList.isEmpty()) {
            for (int son : subList) {
                if (!visited.contains(son)) {
                    deadEnd = false;
                    List<Integer> subVisited = new ArrayList<>(visited);
                    subVisited.add(son);
                    Stack<Stack<Integer>> tempPile = solveDFS2(labyrinth, son, subVisited, (Stack<Integer>) currentPath.clone());
                    pile.addAll(tempPile);
                }
            }
        }

        // On est soit à un dead-end soit à la fin
        if (deadEnd || labyrinth.getEnd() == num) {
            pile.push((Stack<Integer>) currentPath.clone());
        }

        return pile;
    }


    /**
     * @param labyrinth the maze to solve
     * @return stack of stacks of nodes to visit to solve the maze in the shortest way wtih a copy of the best path on top of the stack
    */
    public static Stack<Stack<Integer>> prepDFS2(GraphMaze labyrinth) {
        List<Integer> visited = new ArrayList<>();
        visited.add(labyrinth.getStart());
        Stack<Integer> currentPath = new Stack<>();

        Stack<Stack<Integer>> allPaths = solveDFS2(labyrinth, labyrinth.getStart(), visited, currentPath);

        Stack<Integer> bestPath = null;
        for (Stack<Integer> path : allPaths) {
            if (path.contains(labyrinth.getEnd())) {
                if (bestPath == null || path.size() < bestPath.size()) {
                    bestPath = path;
                }
            }
        }

        System.out.println("Chemin le plus court : " + bestPath);
        allPaths.push(bestPath);
        return allPaths;
    }

    /**
     * @param laby    the maze to solve
     * @param num     the node where we are at
     * @param visited nodes that we visited without the dead ends
     * @param blocked the nodes that lead to a dead end
     * @return visited
     */
    @SuppressWarnings("unchecked")
    private static Stack<Stack<Integer>> solveLeftHand2(GraphMaze laby, int num, Stack<Integer> visited, Stack<Integer> blocked, Stack<Stack<Integer>> pile) {
        // À CHAQUE APPEL, on enregistre le chemin actuel
        pile.push((Stack<Integer>) visited.clone());

        // Condition d'arrêt : on a atteint la sortie
        if (laby.getEnd() == num) {
            return pile;
        }

        List<Integer> subList = laby.getEdges()[num];
        if (subList != null && !subList.isEmpty()) {
            // Essayer chaque direction selon la règle main gauche
            if (subList.contains(num - laby.getWidth()) && !visited.contains(num - laby.getWidth()) && !blocked.contains(num - laby.getWidth())) {
                visited.push(num - laby.getWidth());
                return solveLeftHand2(laby, num - laby.getWidth(), visited, blocked, pile);
            } else if (subList.contains(num + 1) && !visited.contains(num + 1) && !blocked.contains(num + 1)) {
                visited.push(num + 1);
                return solveLeftHand2(laby, num + 1, visited, blocked, pile);
            } else if (subList.contains(num + laby.getWidth()) && !visited.contains(num + laby.getWidth()) && !blocked.contains(num + laby.getWidth())) {
                visited.push(num + laby.getWidth());
                return solveLeftHand2(laby, num + laby.getWidth(), visited, blocked, pile);
            } else if (subList.contains(num - 1) && !visited.contains(num - 1) && !blocked.contains(num - 1)) {
                visited.push(num - 1);
                return solveLeftHand2(laby, num - 1, visited, blocked, pile);
            } else {
                // Impasse : marquer le noeud comme bloqué et revenir en arrière
                blocked.push(num);
                visited.pop();
                if (visited.isEmpty()) {
                    return pile;  // Plus de noeud à visiter, on arrête
                }
                return solveLeftHand2(laby, visited.peek(), visited, blocked, pile);
            }
        } else {
            // Pas de voisin : impasse
            blocked.push(num);
            visited.pop();
            if (visited.isEmpty()) {
                return pile;  // Plus de noeud à visiter, on arrête
            }
            return solveLeftHand2(laby, visited.peek(), visited, blocked, pile);
        }
    }


    /**
     * @param laby the maze to solve
     * @return stack of stack of nodes to visit to solve the maze (not always the shortest way)
     */
    public static Stack<Stack<Integer>> prepLeftHand2(GraphMaze laby) {
        Stack<Stack<Integer>> temPile = new Stack<>();
        Stack<Stack<Integer>> pile = new Stack<>();
        Stack<Integer> visited = new Stack<>();
        Stack<Integer> blocked = new Stack<>();
        visited.push(laby.getStart());
        temPile=solveLeftHand2(laby, laby.getStart(), visited, blocked, temPile);
        do{
            pile.push(temPile.pop());
        }while(!temPile.isEmpty());

        return pile;
    }

    /**
     * @param laby the maze to solve
     * @return Stack of Stacks of nodes where the best path is at the top of the stack
     */
    @SuppressWarnings("unchecked")
    public static Stack<Stack<Integer>> solveDjisktra2(GraphMaze laby) {
        int n = laby.getEdges().length;
        int[] dist = new int[n];
        int[] fathers = new int[n];
        boolean[] visited = new boolean[n];
        Stack<Integer> visit= new Stack<>();
        Stack<Stack<Integer>> pile = new Stack<>();

        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(fathers, -1);
        dist[laby.getStart()] = 0;

        PriorityQueue<Integer> queue = new PriorityQueue<>(Comparator.comparingInt(x -> dist[x]));//les distances les plus faibles en premier
        queue.add(laby.getStart());

        while (!queue.isEmpty()) {
            int current = queue.poll();
            if (visited[current]) {
                continue;
            }
            visited[current] = true;
            visit.push(current);
            pile.push((Stack<Integer>)visit.clone());
            if (current == laby.getEnd()) {
                break;
            }
            for (int edge : laby.getEdges()[current]) {
                if (dist[current] + 1 < dist[edge]) {
                    dist[edge] = dist[current] + 1;
                    fathers[edge] = current;
                    queue.add(edge);
                }
            }
        }
        Stack<Integer> path = new Stack<>();
        if (dist[laby.getEnd()] == Integer.MAX_VALUE) {
            pile.push((Stack<Integer>)path.clone());
            return pile;
        }
        for (int at = laby.getEnd(); at != laby.getStart(); at = fathers[at]) {
            path.push(at);
        }
        path.push(laby.getStart());
        pile.push((Stack<Integer>)path.clone());
        return pile;
    }


}
