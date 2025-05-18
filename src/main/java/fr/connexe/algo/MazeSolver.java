package fr.connexe.algo;

import java.util.*;

/// Solves mazes with various algorithms.
public class MazeSolver {
    public static void main(String[] args) {
        // 0 - 1 - 2 - 3
        // -           -
        // 4 - 5 - 6 - 7
        // -   -
        // 8 - 9 - 10 - 11
        // -        -    -
        // 12- 13  14 - 15
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
        g.connect(9, 10);
        g.connect(10, 11);
        g.connect(10, 14);
        g.connect(11,15);
        g.connect(12,13);
        g.connect(14,15);

        g.setStart(0);
        g.setEnd(15);
        System.out.println(g.toArrayMaze());
        Stack<Integer> pile;
        pile=MazeSolver.prepDFS(g,1);
        System.out.println("DFS:" +pile);
        pile=MazeSolver.prepClockwise(g);
        System.out.println("Clockwise:" +pile);
        pile=MazeSolver.solveDjisktra(g);
        System.out.println("Djikstra" +pile);
        Stack<Stack<Integer>> pile2;
        List<Stack<Integer>> pile3;
        pile3 = MazeSolver.prepDFS2(g);
        System.out.println("DFS2:" +pile3);
        pile2 = MazeSolver.prepClockwise2(g);
        System.out.println("Clockwise2:"+pile2);
        pile2=MazeSolver.solveDjisktra2(g);
        System.out.println("Dijkstra2:" +pile2);
    }


    /**Goes through every path and compare them to get the best path to take to finish the maze
     * @param maze the maze to solve
     * @param num       the node currently visited
     * @param visited   boolean for all nodes to know if they are already visited or not in
     * @param currentPath the stack of nodes representing the path that is actually visited
     * @param allPaths list of stack of nodes representing all paths to the end
     */
    @SuppressWarnings("unchecked")
    private static void solveDFS(GraphMaze maze, int num, boolean[] visited, Stack<Integer> currentPath, List<Stack<Integer>> allPaths) {
        //the actual node is noted as visited and is pushed in the current path
        visited[num] = true;
        currentPath.push(num);
        //if the current node is the end, clone the current path in the stack of all paths
        if (num == maze.getEnd()) {
            allPaths.add((Stack<Integer>) currentPath.clone());
        }
        else {
            //for each son, it checks if the son was already visited in this path and if it isn't, call this function with the son as the actual node
            for (int son : maze.getEdges()[num]) {
                if (!visited[son]) {
                    solveDFS(maze, son, visited, currentPath, allPaths);
                }
            }
        }
        //backtrack once all the paths created from this node are saved in allPaths
        currentPath.pop();
        visited[num] = false;
    }

    /**
     *
     * @param allPaths the list of stacks of nodes which represent all the paths to the end of a maze
     */
    private static  Stack<Integer> shortest(List<Stack<Integer>> allPaths, GraphMaze g) {
        //create a new stack and whenever there is a path shorter than the ones before this one, it is copied and replace the last shortest if there was one
        Stack<Integer> shortest = new Stack<>();
        for (Stack<Integer> path : allPaths) {
            if ((shortest.isEmpty() || path.size() < shortest.size())&& path.contains(g.getEnd())) {
                shortest = path;
            }
        }
        //return the shortest path from all paths
        return shortest;
    }


    /** create whatever the function DFS needs to call it and returns the shortest path from the paths solveDFS returned
     * @param maze the maze to solve
     * @param mode mode for an Easter egg (and because Yani wanted to keep mode in the parameters for this function)
     * @return stack of nodes to visit to solve the maze in the shortest way (if no path can solve it then return an empty stack)
     */
    public static Stack<Integer> prepDFS(GraphMaze maze, int mode) {
        //Easter egg
        if(mode==1) {
            System.out.println("Romu");
        }
        else{
            System.out.println("Eva");

        }
        //creation of everything that is needed for the function solveDFS
        boolean[] visited = new boolean[maze.getEdges().length];
        Stack<Integer> currentPath = new Stack<>();
        List<Stack<Integer>> allPaths = new ArrayList<>();
        //call of the function solveDFS
        solveDFS(maze, maze.getStart(), visited, currentPath, allPaths);
        //call of the function shortest and returns what it returns
        return shortest(allPaths, maze);
    }


    /** The methode of the left hand if we consider that the object/ person in the maze is facing at the right wall
     * @param maze    the maze to solve
     * @param num     the node currently visited
     * @param visited the nodes already visited without the dead ends
     * @param blocked the nodes that lead to a dead end
     * @return visited (if no path is found then it's a stack with only the start that is returned)
     */
    private static Stack<Integer> solveClockwise(GraphMaze maze, int num, Stack<Integer> visited, Stack<Integer> blocked) {
        if (maze.getEnd() == num) {
            return visited;
        }
        List<Integer> subList = maze.getEdges()[num];

        //if the node has at least one son check if it's possible to go the neighbouring node and if it was already visited or blocked clockwise
        // and if it's possible calls this function with the new actual node as the node it is possible to go (it's going to visit every node until there's a path to the end from one)
        if (!subList.isEmpty()) {
            if (subList.contains(num - maze.getWidth()) && !visited.contains(num - maze.getWidth()) && !blocked.contains(num - maze.getWidth())) {
                visited.push(num - maze.getWidth());
                return solveClockwise(maze, num - maze.getWidth(), visited, blocked);
            } else if (subList.contains(num + 1) && !visited.contains(num + 1) && !blocked.contains(num + 1)) {
                visited.push(num + 1);
                return solveClockwise(maze, num + 1, visited, blocked);
            } else if (subList.contains(num + maze.getWidth()) && !visited.contains(num + maze.getWidth()) && !blocked.contains(num + maze.getWidth())) {
                visited.push(num + maze.getWidth());
                return solveClockwise(maze, num + maze.getWidth(), visited, blocked);
            } else if (subList.contains(num - 1) && !visited.contains(num - 1) && !blocked.contains(num - 1)) {
                visited.push(num - 1);
                return solveClockwise(maze, num - 1, visited, blocked);
            } else { //if there is no possibilities from the current node, it is considered as a dead end (remove it from the visited nodes),blocks it and calls the node who called this one
                blocked.push(num);
                visited.pop();
                if (visited.isEmpty()) {
                    return visited;
                }
                return solveClockwise(maze, visited.peek(), visited, blocked);
            }
        } else {//if the node doesn't have any son, consider it as a dead end (remove it from the visited nodes),blocks it and calls the node who called this one if it isn't the start (if it's the start, just return an empty stack)
            blocked.push(num);
            visited.pop();
            if(!visited.isEmpty()){
                return visited;
            }
            return solveClockwise(maze, visited.peek(), visited, blocked);
        }
    }

    /** create whatever the function Clockwise needs to call it and returns what it returns
     * @param maze the maze to solve
     * @return stack of nodes to visit to solve the maze (not always the shortest way) (if no path is found, return an empty stack)
     */
    public static Stack<Integer> prepClockwise(GraphMaze maze) {
        //create whatever the function solveClockwise needs
        Stack<Integer> pile = new Stack<>();
        Stack<Integer> visited = new Stack<>();
        visited.push(maze.getStart());
        Stack<Integer> blocked = new Stack<>();
        //calls the function clockwise
        visited = solveClockwise(maze, maze.getStart(), visited, blocked);
        //since the top of the stack is the end, flip upside down the stack and return it
        while (!visited.isEmpty()) {
            pile.push(visited.pop());
        }
        return pile;
    }

    /** Goes through the maze as a BFS until it reaches the end or if it doesn't have any more place to go
     * Then returns the best path to the end (if there isn't a path, it returns an empty stack)
     * @param maze the maze to solve
     * @return stack of nodes which is the best path (if no path is found, return an empty stack)
     */
    public static Stack<Integer> solveDjisktra(GraphMaze maze) {
        //create whatever the function needs
        int n = maze.getEdges().length;
        int[] dist = new int[n];
        int[] fathers = new int[n];
        boolean[] visited = new boolean[n];
        //fill all distances as max value except for the start which is 0 for later
        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(fathers, -1);
        dist[maze.getStart()] = 0;
        //create a queue where the lowest distances are first to go out to have the shortest path and add start to the queue
        PriorityQueue<Integer> queue = new PriorityQueue<>(Comparator.comparingInt(x -> dist[x]));
        queue.add(maze.getStart());
        //as long as queue isn't empty take the first to go out of the queue, if it isn't visited, tag him as visited
        //and if it isn't the end take all of his sons and give them his distance+1 if they have a greater distance than this then add them to the queue
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
        //when there is no more node in the queue check if the distance of the end was modified
        Stack<Integer> path = new Stack<>();
        if (dist[maze.getEnd()] == Integer.MAX_VALUE) {
            return path;
        }
        //if it is modified, regress from the end to the start to get the shortest path to it.
        for (int p = maze.getEnd(); p != maze.getStart(); p = fathers[p]) {
            path.push(p);
        }
        path.push(maze.getStart());
        //return the shortest path to the end
        return path;
    }

    /**
     * @param maze the maze to solve
     * @param num       the node currently visited
     * @param visited   boolean for all nodes to know if they are already visited or not in
     * @param currentPath the stack of nodes representing the path that is actually visited
     * @param allPaths list of stack of nodes representing all paths to the end
    */
    @SuppressWarnings("unchecked")
    private static void solveDFS2(GraphMaze maze, int num, boolean[] visited, Stack<Integer> currentPath, List<Stack<Integer>> allPaths) {
        visited[num] = true;
        currentPath.push(num);
        boolean deadEnd = true;
        //if the current node is the end, clone the current path in the stack of all paths
        if (num == maze.getEnd()) {
            allPaths.add((Stack<Integer>) currentPath.clone());
        }
        else {
            for (int son : maze.getEdges()[num]) {
                if (!visited[son]) {
                    deadEnd = false;
                    solveDFS2(maze, son, visited, currentPath, allPaths);
                }
            }
        }
        if (deadEnd) {
            allPaths.add((Stack<Integer>) currentPath.clone());
        }
        currentPath.pop();
        visited[num] = false;
    }


    /**
     * @param maze the maze to solve
     * @return stack of stacks of nodes to visit to solve the maze in the shortest way with a copy of the best path on top of the stack
    */
    public static List<Stack<Integer>> prepDFS2(GraphMaze maze) {
        boolean[] visited = new boolean[maze.getEdges().length];
        Stack<Integer> currentPath = new Stack<>();
        List<Stack<Integer>> allPaths = new ArrayList<>();

        solveDFS2(maze, maze.getStart(), visited, currentPath, allPaths);
        Stack<Integer> shortest;
        shortest=shortest(allPaths, maze);
        Collections.reverse(allPaths);
        allPaths.add(shortest);

        return allPaths;
    }

    /**
     * @param maze the maze to solve
     * @param num the node where currently visited
     * @param visited nodes that we visited without the dead ends
     * @param blocked the nodes that lead to a dead end
     * @return visited
     */
    @SuppressWarnings("unchecked")
    private static Stack<Stack<Integer>> solveClockwise2(GraphMaze maze, int num, Stack<Integer> visited, Stack<Integer> blocked, Stack<Stack<Integer>> pile) {
        // at each call, save the current path
        pile.push((Stack<Integer>) visited.clone());

        // if we're at the end, we stop
        if (maze.getEnd() == num) {
            return pile;
        }

        List<Integer> subList = maze.getEdges()[num];
        if (subList != null && !subList.isEmpty()) {
            //if the node has at least one son check if it's possible to go the neighbouring node and if it was already visited or blocked clockwise
            // and if it's possible calls this function with the new actual node as the node it is possible to go (it's going to visit every node until there's a path to the end from one)
            if (subList.contains(num - maze.getWidth()) && !visited.contains(num - maze.getWidth()) && !blocked.contains(num - maze.getWidth())) {
                visited.push(num - maze.getWidth());
                return solveClockwise2(maze, num - maze.getWidth(), visited, blocked, pile);
            } else if (subList.contains(num + 1) && !visited.contains(num + 1) && !blocked.contains(num + 1)) {
                visited.push(num + 1);
                return solveClockwise2(maze, num + 1, visited, blocked, pile);
            } else if (subList.contains(num + maze.getWidth()) && !visited.contains(num + maze.getWidth()) && !blocked.contains(num + maze.getWidth())) {
                visited.push(num + maze.getWidth());
                return solveClockwise2(maze, num + maze.getWidth(), visited, blocked, pile);
            } else if (subList.contains(num - 1) && !visited.contains(num - 1) && !blocked.contains(num - 1)) {
                visited.push(num - 1);
                return solveClockwise2(maze, num - 1, visited, blocked, pile);
            } else {
                // if no sons usable, it is considered as a dead end (remove it from visited), blocks it and calls the last node visited before it if there is one
                blocked.push(num);
                visited.pop();
                if (visited.isEmpty()) {
                    return pile;
                }
                return solveClockwise2(maze, visited.peek(), visited, blocked, pile);
            }
        } else {
            // if no sons, it considers it as a dead end (remove it from visited), blocks it and calls the last node visited before it if there is one
            blocked.push(num);
            visited.pop();
            if (visited.isEmpty()) {
                return pile;  //no node visited means we're at the start and we return an empty stack
            }

            return solveClockwise2(maze, visited.peek(), visited, blocked, pile);
        }
    }


    /**
     * @param maze the maze to solve
     * @return stack of stacks of nodes to visit to solve the maze (not always the shortest way)
     */
    @SuppressWarnings("unchecked")
    public static Stack<Stack<Integer>> prepClockwise2(GraphMaze maze) {
        //create whatever is needed for solveClockwise2
        Stack<Stack<Integer>> tempPile = new Stack<>();
        Stack<Stack<Integer>> pile = new Stack<>();
        Stack<Integer> visited = new Stack<>();
        Stack<Integer> blocked = new Stack<>();
        //push the start in the stack
        visited.push(maze.getStart());
        //calls the function solveClockwise2
        tempPile=solveClockwise2(maze, maze.getStart(), visited, blocked, tempPile);
        //invert the stack of stacks
        do{
            pile.push(tempPile.pop());
        }while(!tempPile.isEmpty());
        //add the path to the end at the start of the stack
        pile.push((Stack<Integer>) visited.clone());
        //return the stack of stacks
        return pile;
    }

    /** Goes through the maze as a BFS until it reaches the end or if it doesn't have any more place to go and return every step with the best path to the end in first position of the pile (if there isn't a path, it returns an empty stack)
     * @param maze the maze to solve
     * @return Stack of Stacks of nodes where the best path is at the top of the stack
     */
    @SuppressWarnings("unchecked")
    public static Stack<Stack<Integer>> solveDjisktra2(GraphMaze maze) {
        //create whatever is needed for the function
        int n = maze.getEdges().length;
        int[] dist = new int[n];
        int[] fathers = new int[n];
        boolean[] visited = new boolean[n];
        Stack<Integer> visit= new Stack<>();
        Stack<Stack<Integer>> pile = new Stack<>();
        Stack<Stack<Integer>> tempPile = new Stack<>();

        //fill all distances as max value except for the start which is 0 for later
        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(fathers, -1);
        dist[maze.getStart()] = 0;

        //create a queue where the lowest distances are first to go out to have the shortest path and add start to the queue
        PriorityQueue<Integer> queue = new PriorityQueue<>(Comparator.comparingInt(x -> dist[x]));
        queue.add(maze.getStart());

        //as long as queue isn't empty take the first to go out of the queue, if it isn't visited, tag him as visited and push a clone of visited in the stack of stacks
        //and if it isn't the end take all of his sons and give them his distance+1 if they have a greater distance than this then add them to the queue
        while (!queue.isEmpty()) {
            int current = queue.poll();
            if (visited[current]) {
                continue;
            }
            visited[current] = true;
            visit.push(current);
            tempPile.push((Stack<Integer>)visit.clone());
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
        //invert the stack of stacks
        do {
            pile.push(tempPile.pop());
        }while(!tempPile.isEmpty());

        //when there is no more node in the queue check if the distance of the end was modified
        Stack<Integer> path = new Stack<>();
        if (dist[maze.getEnd()] == Integer.MAX_VALUE) {
            pile.push((Stack<Integer>)path.clone());
            return pile;
        }

        //if it is modified, regress from the end to the start to get the shortest path to it.
        for (int at = maze.getEnd(); at != maze.getStart(); at = fathers[at]) {
            path.push(at);
        }
        //and push the shortest path at the start of the stack of stacks
        path.push(maze.getStart());
        pile.push((Stack<Integer>)path.clone());
        return pile;
    }


}
