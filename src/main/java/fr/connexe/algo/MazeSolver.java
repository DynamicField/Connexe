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

        g.setStart(0);
        g.setEnd(15);
        System.out.println(g.toArrayMaze());
        Stack<Integer> pile;
        pile=MazeSolver.prepDFS(g,1);
        System.out.println("DFS:" +pile);
        pile=MazeSolver.prepLeftHand(g);
        System.out.println("LeftHand:" +pile);
        pile=MazeSolver.solveDjisktra(g);
        System.out.println("Djikstra" +pile);
        Stack<Stack<Integer>> pile2;
        pile2 = MazeSolver.prepDFS2(g);
        System.out.println("DFS2:" +pile2);
        pile2 = MazeSolver.prepLeftHand2(g);
        System.out.println("main gauche:"+pile2);
        pile2=MazeSolver.solveDjisktra2(g);
        System.out.println("Dijkstra2:" +pile2);
    }


    /**Goes through every path and compare them to get the best path to take to finish the maze
     * @param maze the maze to solve
     * @param num       the node currently visited
     * @param visited   the nodes already visited
     * @return the stack of nodes which is the best path to solve the maze (if no path can solve it then returns an empty stack)
     */
    private static Stack<Integer> solveDFS(GraphMaze maze, int num, List<Integer> visited) {
        Stack<Integer> maxflow = new Stack<Integer>();
        if (maze.getEnd() == num) {
            maxflow.push(num);
            return maxflow;
        }
        Stack<Integer> subSon = new Stack<Integer>();
        List<Integer> subList = maze.getEdges()[num];
        if (!subList.isEmpty()) {
            for (int son : subList) {
                if (!visited.contains(son)) {
                    List<Integer> subVisited = new ArrayList<>(visited); //création d'une copie de visited pour pouvoir y repasser à partir d'un autre chemin.
                    subVisited.add(son);
                    subSon = solveDFS(maze, son, subVisited);
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

    /** create whatever the function DFS needs to call it and returns what it returns
     * @param maze the maze to solve
     * @return stack of nodes to visit to solve the maze in the shortest way (if no path can solve it then return an empty stack)
     */
    public static Stack<Integer> prepDFS(GraphMaze maze, int mode) {
        Stack<Integer> pile;
        if(mode==1) {
            System.out.println("Romu");
        }
        else{
            System.out.println("Eva");

        }
        List<Integer> visited = new ArrayList<>();
        visited.add(maze.getStart());
        pile = solveDFS(maze, maze.getStart(), visited);
        return pile;
    }

    /** The methode of the left hand if we consider that the object/ personn in the maze is facing at the right wall
     * @param maze    the maze to solve
     * @param num     the node currently visited
     * @param visited the nodes already visited without the dead ends
     * @param blocked the nodes that lead to a dead end
     * @return visited (if no path is found then it's a stack with only the start that is returned)
     */
    private static Stack<Integer> solveLeftHand(GraphMaze maze, int num, Stack<Integer> visited, Stack<Integer> blocked) {
        if (maze.getEnd() == num) {
            return visited;
        }
        List<Integer> subList = maze.getEdges()[num];
        if (!subList.isEmpty()) {
            if (subList.contains(num - maze.getWidth()) && !visited.contains(num - maze.getWidth()) && !blocked.contains(num - maze.getWidth())) {
                visited.push(num - maze.getWidth());
                return solveLeftHand(maze, num - maze.getWidth(), visited, blocked);
            } else if (subList.contains(num + 1) && !visited.contains(num + 1) && !blocked.contains(num + 1)) {
                visited.push(num + 1);
                return solveLeftHand(maze, num + 1, visited, blocked);
            } else if (subList.contains(num + maze.getWidth()) && !visited.contains(num + maze.getWidth()) && !blocked.contains(num + maze.getWidth())) {
                visited.push(num + maze.getWidth());
                return solveLeftHand(maze, num + maze.getWidth(), visited, blocked);
            } else if (subList.contains(num - 1) && !visited.contains(num - 1) && !blocked.contains(num - 1)) {
                visited.push(num - 1);
                return solveLeftHand(maze, num - 1, visited, blocked);
            } else {
                blocked.push(num);
                visited.pop();
                if (visited.isEmpty()) {
                    return visited;
                }
                return solveLeftHand(maze, visited.peek(), visited, blocked);
            }
        } else {
            blocked.push(num);
            visited.pop();
            if(!visited.isEmpty()){
                return visited;
            }
            return solveLeftHand(maze, visited.peek(), visited, blocked);
        }
    }

    /** create whatever the function LeftHand needs to call it and returns what it returns
     * @param maze the maze to solve
     * @return stack of nodes to visit to solve the maze (not always the shortest way) (if no path is found, return an empty stack)
     */
    public static Stack<Integer> prepLeftHand(GraphMaze maze) {
        Stack<Integer> pile = new Stack<>();
        Stack<Integer> visited = new Stack<>();
        visited.push(maze.getStart());
        Stack<Integer> blocked = new Stack<>();
        visited = solveLeftHand(maze, maze.getStart(), visited, blocked);
        while (!visited.isEmpty()) {
            pile.push(visited.pop());
        }
        return pile;
    }

    /** Goes through the maze as a BFS until it reaches the end or if it doesn't have any more place to go and return the best path to the end (if there isn't a path, it returns an empty stack
     * @param maze the maze to solve
     * @return stack of nodes which is the best path (if no path is found, return an empty stack)
     */
    public static Stack<Integer> solveDjisktra(GraphMaze maze) {
        int n = maze.getEdges().length;
        int[] dist = new int[n];
        int[] fathers = new int[n];
        boolean[] visited = new boolean[n];

        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(fathers, -1);
        dist[maze.getStart()] = 0;

        PriorityQueue<Integer> queue = new PriorityQueue<>(Comparator.comparingInt(x -> dist[x]));//les distances les plus faibles en premier
        queue.add(maze.getStart());

        while (!queue.isEmpty()) {
            int current = queue.poll();
            if (visited[current]) {
                continue;
            }
            visited[current] = true;

            if (current == maze.getEnd()) {
                break;
            }
            for (int edge : maze.getEdges()[current]) {
                if (dist[current] + 1 < dist[edge]) {
                    dist[edge] = dist[current] + 1;
                    fathers[edge] = current;
                    queue.add(edge);
                }
            }
        }
        Stack<Integer> path = new Stack<>();
        if (dist[maze.getEnd()] == Integer.MAX_VALUE) {
            return path;
        }
        for (int at = maze.getEnd(); at != maze.getStart(); at = fathers[at]) {
            path.push(at);
        }
        path.push(maze.getStart());

        return path;
    }

    /**
     * @param maze the maze
     * @param num the node currently visited
     * @param visited the nodes we visited
     * @return pile the stack of nodes which is the best path
    */
    @SuppressWarnings("unchecked")
    private static Stack<Stack<Integer>> solveDFS2(GraphMaze maze, int num, List<Integer> visited, Stack<Integer> currentPath) {
        Stack<Stack<Integer>> pile = new Stack<>();
        currentPath.push(num);

        List<Integer> subList = maze.getEdges()[num];
        boolean deadEnd = true;
        if (maze.getEnd() == num) {
            pile.push((Stack<Integer>) currentPath.clone());
            return pile;
        }
        if (subList != null && !subList.isEmpty()) {
            for (int son : subList) {
                if (!visited.contains(son)) {
                    deadEnd = false;
                    List<Integer> subVisited = new ArrayList<>(visited);
                    subVisited.add(son);
                    Stack<Stack<Integer>> tempPile = solveDFS2(maze, son, subVisited, (Stack<Integer>) currentPath.clone());
                    pile.addAll(tempPile);
                }
            }
        }

        // On est soit à un dead-end soit à la fin
        if (deadEnd) {
            pile.push((Stack<Integer>) currentPath.clone());
        }

        return pile;
    }


    /**
     * @param maze the maze to solve
     * @return stack of stacks of nodes to visit to solve the maze in the shortest way wtih a copy of the best path on top of the stack
    */
    public static Stack<Stack<Integer>> prepDFS2(GraphMaze maze) {
        List<Integer> visited = new ArrayList<>();
        visited.add(maze.getStart());
        Stack<Integer> currentPath = new Stack<>();

        Stack<Stack<Integer>> allPaths = solveDFS2(maze, maze.getStart(), visited, currentPath);

        Stack<Integer> bestPath=null;
        for (Stack<Integer> path : allPaths) {
            if (path.contains(maze.getEnd())) {
                if (bestPath == null || path.size() < bestPath.size()) {
                    bestPath = path;
                }
            }
        }
        if(bestPath!=null){
            allPaths.push(bestPath);
        }
        else{
            Stack<Integer> nul= new Stack<>();
            allPaths.push(nul);
        }
        return allPaths;
    }

    /**
     * @param maze    the maze to solve
     * @param num     the node where currently visited
     * @param visited nodes that we visited without the dead ends
     * @param blocked the nodes that lead to a dead end
     * @return visited
     */
    @SuppressWarnings("unchecked")
    private static Stack<Stack<Integer>> solveLeftHand2(GraphMaze maze, int num, Stack<Integer> visited, Stack<Integer> blocked, Stack<Stack<Integer>> pile) {
        // À CHAQUE APPEL, on enregistre le chemin actuel
        pile.push((Stack<Integer>) visited.clone());

        // Condition d'arrêt : on a atteint la sortie
        if (maze.getEnd() == num) {
            return pile;
        }

        List<Integer> subList = maze.getEdges()[num];
        if (subList != null && !subList.isEmpty()) {
            // Essayer chaque direction selon la règle main gauche
            if (subList.contains(num - maze.getWidth()) && !visited.contains(num - maze.getWidth()) && !blocked.contains(num - maze.getWidth())) {
                visited.push(num - maze.getWidth());
                return solveLeftHand2(maze, num - maze.getWidth(), visited, blocked, pile);
            } else if (subList.contains(num + 1) && !visited.contains(num + 1) && !blocked.contains(num + 1)) {
                visited.push(num + 1);
                return solveLeftHand2(maze, num + 1, visited, blocked, pile);
            } else if (subList.contains(num + maze.getWidth()) && !visited.contains(num + maze.getWidth()) && !blocked.contains(num + maze.getWidth())) {
                visited.push(num + maze.getWidth());
                return solveLeftHand2(maze, num + maze.getWidth(), visited, blocked, pile);
            } else if (subList.contains(num - 1) && !visited.contains(num - 1) && !blocked.contains(num - 1)) {
                visited.push(num - 1);
                return solveLeftHand2(maze, num - 1, visited, blocked, pile);
            } else {
                // Impasse : marquer le noeud comme bloqué et revenir en arrière
                blocked.push(num);
                visited.pop();
                if (visited.isEmpty()) {
                    return pile;  // Plus de noeud à visiter, on arrête
                }
                return solveLeftHand2(maze, visited.peek(), visited, blocked, pile);
            }
        } else {
            // Pas de voisin : impasse
            blocked.push(num);
            visited.pop();
            if (visited.isEmpty()) {
                return pile;  // Plus de noeud à visiter, on arrête
            }
            return solveLeftHand2(maze, visited.peek(), visited, blocked, pile);
        }
    }


    /**
     * @param maze the maze to solve
     * @return stack of stack of nodes to visit to solve the maze (not always the shortest way)
     */
    @SuppressWarnings("unchecked")
    public static Stack<Stack<Integer>> prepLeftHand2(GraphMaze maze) {
        Stack<Stack<Integer>> temPile = new Stack<>();
        Stack<Stack<Integer>> pile = new Stack<>();
        Stack<Integer> visited = new Stack<>();
        Stack<Integer> blocked = new Stack<>();
        visited.push(maze.getStart());
        temPile=solveLeftHand2(maze, maze.getStart(), visited, blocked, temPile);
        do{
            pile.push(temPile.pop());
        }while(!temPile.isEmpty());
        pile.push((Stack<Integer>) visited.clone());

        return pile;
    }

    /** Goes through the maze as a BFS until it reaches the end or if it doesn't have any more place to go and return every step with the best path to the end in first position of the pile (if there isn't a path, it returns an empty stack)
     * @param maze the maze to solve
     * @return Stack of Stacks of nodes where the best path is at the top of the stack
     */
    @SuppressWarnings("unchecked")
    public static Stack<Stack<Integer>> solveDjisktra2(GraphMaze maze) {
        int n = maze.getEdges().length;
        int[] dist = new int[n];
        int[] fathers = new int[n];
        boolean[] visited = new boolean[n];
        Stack<Integer> visit= new Stack<>();
        Stack<Stack<Integer>> pile = new Stack<>();
        Stack<Stack<Integer>> temPile = new Stack<>();


        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(fathers, -1);
        dist[maze.getStart()] = 0;

        PriorityQueue<Integer> queue = new PriorityQueue<>(Comparator.comparingInt(x -> dist[x]));//les distances les plus faibles en premier
        queue.add(maze.getStart());

        while (!queue.isEmpty()) {
            int current = queue.poll();
            if (visited[current]) {
                continue;
            }
            visited[current] = true;
            visit.push(current);
            temPile.push((Stack<Integer>)visit.clone());
            if (current == maze.getEnd()) {
                break;
            }
            for (int edge : maze.getEdges()[current]) {
                if (dist[current] + 1 < dist[edge]) {
                    dist[edge] = dist[current] + 1;
                    fathers[edge] = current;
                    queue.add(edge);
                }
            }
        }
        do {
            pile.push(temPile.pop());
        }while(!temPile.isEmpty());
        Stack<Integer> path = new Stack<>();
        if (dist[maze.getEnd()] == Integer.MAX_VALUE) {
            pile.push((Stack<Integer>)path.clone());
            return pile;
        }
        for (int at = maze.getEnd(); at != maze.getStart(); at = fathers[at]) {
            path.push(at);
        }
        path.push(maze.getStart());
        pile.push((Stack<Integer>)path.clone());
        return pile;
    }


}
