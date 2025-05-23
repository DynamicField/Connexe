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
        g.connect(6, 7);
        g.connect(8, 12);
        g.connect(9, 10);
        g.connect(10, 11);
        g.connect(10, 14);
        g.connect(11, 15);
        g.connect(12, 13);
        g.connect(14, 15);


        g.setStart(0);
        g.setEnd(15);
        System.out.println(g.toArrayMaze());
        Stack<Integer> pile;
        pile = MazeSolver.prepDFS(g,1);
        System.out.println("DFS:" + pile);
        pile = MazeSolver.prepAStar(g);
        System.out.println("A*" + pile);
        pile=MazeSolver.prepLeftHand(g);
        System.out.println("LeftHand:" + pile);
        pile = MazeSolver.prepClockwise(g);
        System.out.println("Clockwise:" + pile);
        pile = MazeSolver.solveDijkstra(g);
        System.out.println("Djikstra" + pile);
        Stack<Stack<Integer>> pile2;
        List<Stack<Integer>> pile3;
        pile3 = MazeSolver.prepDFS2(g);
        pile2 = MazeSolver.solveAStar(g);
        System.out.println("A*2" + pile2);
        pile2 = MazeSolver.prepLeftHand2(g);
        System.out.println("LeftHand2:" + pile2);
        System.out.println("DFS2:" + pile3);
        pile2 = MazeSolver.prepClockwise2(g);
        System.out.println("Clockwise2:" + pile2);
        pile2 = MazeSolver.solveDijkstra2(g);
        System.out.println("Dijkstra2:" + pile2);
    }


    /// Goes through every path and compare them to get the best path to take to finish the maze
    /// @param maze the maze to solve
    /// @param num the node currently visited
    /// @param visited boolean for all nodes to know if they are already visited or not in
    /// @param currentPath the stack of nodes representing the path that is actually visited
    /// @param allPaths list of stack of nodes representing all paths visited
    @SuppressWarnings("unchecked")
    private static void solveDFS(GraphMaze maze, int num, boolean[] visited, Stack<Integer> currentPath, List<Stack<Integer>> allPaths) {
        visited[num] = true;
        currentPath.push(num);
        //if the current node is the end, clone the current path in the stack of all paths
        if (num == maze.getEnd()) {
            allPaths.add((Stack<Integer>) currentPath.clone());
        } else {
            for (int son : maze.getEdges()[num]) {
                if (!visited[son]) {
                     solveDFS(maze, son, visited, currentPath, allPaths);
                }
            }
        }
        if (num != maze.getEnd()) {
            allPaths.add((Stack<Integer>) currentPath.clone());

        }
        currentPath.pop();

    }


    /// @param allPaths the list of stacks of nodes which represent all the paths visited
    /// @return the shortest path to the enf if there is one
    private static Stack<Integer> shortest(List<Stack<Integer>> allPaths, GraphMaze g) {
        //create a new stack and whenever there is a path shorter than the ones before this one, it is copied and replace the last shortest if there was one
        Stack<Integer> shortest = new Stack<>();
        for (Stack<Integer> path : allPaths) {
            if ((shortest.isEmpty() || path.size() < shortest.size()) && path.contains(g.getEnd())) {
                shortest = path;
            }
        }
        //return the shortest path from all paths
        return shortest;
    }


    /// create whatever the function DFS needs to call it and returns the shortest path from the paths solveDFS returned
    /// @param maze the maze to solve
    /// @param mode mode for an Easter egg (and because Yani wanted to keep mode in the parameters for this function)
    /// @return stack of nodes to visit to solve the maze in the shortest way (if no path can solve it then return an empty stack)
    public static Stack<Integer> prepDFS(GraphMaze maze, int mode) {
        //Easter egg
        if (mode == 1) {
            System.out.println("Romu");
        } else {
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

    /// @param maze the maze to solve
    /// @return stack of stacks of nodes to visit to solve the maze with a copy of the path on top of the stack
    public static List<Stack<Integer>> prepDFS2(GraphMaze maze) {
        boolean[] visited = new boolean[maze.getEdges().length];
        Stack<Integer> currentPath = new Stack<>();
        List<Stack<Integer>> allPaths = new ArrayList<>();

        solveDFS(maze, maze.getStart(), visited, currentPath, allPaths);
        Stack<Integer> shortest;
        shortest = shortest(allPaths, maze);
        allPaths.add(shortest);

        return allPaths;
    }

    /// The methode of the left hand if we consider that the object/ person in the maze is facing at the right wall
    /// @param maze the maze to solve
    /// @param num the node currently visited
    /// @param visited the nodes already visited without the dead ends
    /// @param blocked the nodes that lead to a dead end
    /// @return visited (if no path is found then it's a stack with only the start that is returned)
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
            if (!visited.isEmpty()) {
                return visited;
            }
            return solveClockwise(maze, visited.peek(), visited, blocked);
        }
    }

    /// create whatever the function Clockwise needs to call it and returns what it returns
    /// @param maze the maze to solve
    /// @return stack of nodes to visit to solve the maze (not always the shortest way) (if no path is found, return an empty stack)
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

    /// Goes through the maze as a BFS until it reaches the end or if it doesn't have any more place to go
    /// Then returns the best path to the end (if there isn't a path, it returns an empty stack)
    /// @param maze the maze to solve
    /// @return stack of nodes which is the best path (if no path is found, return an empty stack)
    public static Stack<Integer> solveDijkstra(GraphMaze maze) {
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


    /// @param maze the maze to solve
    /// @param num the node where currently visited
    /// @param visited nodes that we visited without the dead ends
    /// @param blocked the nodes that lead to a dead end
    /// @return visited
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


    /// @param maze the maze to solve
    /// @return stack of stacks of nodes to visit to solve the maze (not always the shortest way)
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
        tempPile = solveClockwise2(maze, maze.getStart(), visited, blocked, tempPile);
        //invert the stack of stacks
        do {
            pile.push(tempPile.pop());
        } while (!tempPile.isEmpty());
        //add the path to the end at the start of the stack
        pile.push((Stack<Integer>) visited.clone());
        //return the stack of stacks
        return pile;
    }

    /// Goes through the maze as a BFS until it reaches the end or if it doesn't have any more place to go and return every step with the best path to the end in first position of the pile (if there isn't a path, it returns an empty stack)
    /// @param maze the maze to solve
    /// @return Stack of Stacks of nodes where the best path is at the top of the stack
    @SuppressWarnings("unchecked")
    public static Stack<Stack<Integer>> solveDijkstra2(GraphMaze maze) {
        //create whatever is needed for the function
        int n = maze.getEdges().length;
        int[] dist = new int[n];
        int[] fathers = new int[n];
        boolean[] visited = new boolean[n];
        Stack<Integer> visit = new Stack<>();
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
            tempPile.push((Stack<Integer>) visit.clone());
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
        } while (!tempPile.isEmpty());

        //when there is no more node in the queue check if the distance of the end was modified
        Stack<Integer> path = new Stack<>();
        if (dist[maze.getEnd()] == Integer.MAX_VALUE) {
            pile.push((Stack<Integer>) path.clone());
            return pile;
        }

        //if it is modified, regress from the end to the start to get the shortest path to it.
        for (int at = maze.getEnd(); at != maze.getStart(); at = fathers[at]) {
            path.push(at);
        }
        //and push the shortest path at the start of the stack of stacks
        path.push(maze.getStart());
        pile.push((Stack<Integer>) path.clone());
        return pile;
    }

    /// @param maze the maze to solve
    /// @param num the node actually visited
    /// @param dir the direction faced when visiting the node
    /// @param visited the stack of nodes visited (without the blocked nodes)
    /// @param blocked the list of nodes that lead to a dead end
    /// @return the path to the end
    @SuppressWarnings("unchecked")
    private static Stack<Stack<Integer>> solveLeftHand(GraphMaze maze, int num, char dir, Stack<Integer> visited, List<Integer> blocked, Stack<Stack<Integer>> paths) {

        if(!visited.contains(num) && !blocked.contains(num)) {
            visited.push(num);//push the node actually visited in the stack of nodes visited and create a copy of this stack in the stack of steps
        }

        paths.push((Stack<Integer>) visited.clone());
        if (num == maze.getEnd()){//if it's the end, return the stack of steps with the end at the top of the stack
            return paths;
        }

        int width = maze.getWidth();
        List<Integer> sons = maze.getEdges()[num];

        int[] dirx = new int[4]; // left, front, right and behind
        char[] nextDir = new char[4];

        switch (dir) {//set up the directions (such as up, right, left and down) used in order and the directions for the next node (without the node visited just before)
            case 'L':
                dirx = new int[]{+width, -1, -width, 1};
                nextDir = new char[]{'D', 'L', 'U', 'R'};
                break;
            case 'U':
                dirx = new int[]{-1, -width, +1, +width};
                nextDir = new char[]{'L', 'U', 'R', 'D'};
                break;
            case 'R':
                dirx = new int[]{-width, +1, +width, -1};
                nextDir = new char[]{'U', 'R', 'D', 'L'};
                break;
            case 'D':
                dirx = new int[]{+1, +width, -1, -width};
                nextDir = new char[]{'R', 'D', 'L', 'U'};
                break;
        }
    //check if it's possible to go to the node at the left, then ahead, then at the right of the node depending upon the direction used
        //and return the stack of recursions of the first node usable
        for (int i = 0; i < 4; i++) {
            int next = num + dirx[i];
            if (next >= 0 && next< maze.getNumCells() && sons.contains(next) && !visited.contains(next) && !blocked.contains(next)) {
                return solveLeftHand(maze, next, nextDir[i], visited, blocked, paths);
            }
        }

        // if no allowed path, go back and block this node (remove this and the last node visited to prevent errors)
        int t = visited.pop();
        blocked.add(t);
        if (!visited.isEmpty()) {
            return solveLeftHand(maze, visited.peek(), dir, visited, blocked, paths);
        }
        paths.push((Stack<Integer>) visited.clone());
        //if the actual node is the start and there is no usable node, return an empty stack as the path to the end
        return paths;
    }


    ///prepare everything needed for solveLeftHand and calls it
    /// @param maze the maze to solve
    /// @return the path to the end
    public static Stack<Integer> prepLeftHand(GraphMaze maze) {
        Stack<Integer> pile = new Stack<>();
        Stack<Integer> visited = new Stack<>();
        List<Integer> blocked = new LinkedList<>();
        Stack<Stack<Integer>> paths = new Stack<>();
        //calls the function solveLeftHand
        paths = solveLeftHand(maze, maze.getStart(), 'R', visited, blocked, paths);
        //since the top of the stack is the end, flip upside down the stack and return it
        visited=paths.pop();
        while (!visited.isEmpty()) {
            pile.push(visited.pop());
        }
        return pile;
    }

    /// prepare everything needed for solveLeftHand and calls it
    /// @param maze the maze to solve
    /// @return a stack of stacks of nodes which represent the steps to the path to the end (if there is one) with the path at the top
    public static Stack<Stack<Integer>> prepLeftHand2(GraphMaze maze) {
        Stack<Integer> visited = new Stack<>();
        List<Integer> blocked = new LinkedList<>();
        Stack<Stack<Integer>> tempPaths = new Stack<>();
        Stack<Stack<Integer>> paths = new Stack<>();
        //calls the function solveLeftHand
        tempPaths = solveLeftHand(maze, maze.getStart(), 'R', visited, blocked, tempPaths);
        //since the top of the stack is the end, flip upside down the stack, add at the top the final path and return it
        Stack<Integer> pile = tempPaths.peek();
        do {
            paths.push(tempPaths.pop());
        }while (!tempPaths.isEmpty());
        paths.push(pile);
        return paths;
    }

    private static Stack<Stack<Integer>> inversionPaths (Stack<Stack<Integer>> tempPaths){
        Stack<Stack<Integer>> paths = new Stack<>();
        do {
            paths.push(tempPaths.pop());
        }while (!tempPaths.isEmpty());
        return paths;
    }

    /** a BFS with some conditions to be optimal (done with deepseek)
     * @param maze the maze to solve
     * @return a stack of stacks of nodes which represent the steps and at the top is the path to the end
     */
    @SuppressWarnings("unchecked")
    public static Stack<Stack<Integer>> solveAStar(GraphMaze maze) {
        // Initialisation
        Stack<Stack<Integer>> paths = new Stack<>();
        Stack<Integer> visited = new Stack<>();
        int start = maze.getStart();
        int end = maze.getEnd();
        int width = maze.getWidth();

        // data
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Map<Integer, Integer> cameFrom = new HashMap<>();
        Map<Integer, Integer> gScore = new HashMap<>();
        Map<Integer, Integer> fScore = new HashMap<>();

        // score creation
        gScore.put(start, 0);
        fScore.put(start, heuristic(start, end, width));
        openSet.add(new Node(start, fScore.get(start)));

        //start of the BFS
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            visited.push(current.id);
            paths.push((Stack<Integer>)visited.clone());
            // stop if the end is the actual node
            if (current.id == end) {
                paths=inversionPaths(paths);
                visited=reconstructPath(cameFrom, current.id);
                paths.push((Stack<Integer>)visited.clone());
                return paths;
            }

            // sons' exploration
            for (int neighbor : maze.getEdges()[current.id]) {
                // gScore: temporary score
                int tryGScore = gScore.getOrDefault(current.id, Integer.MAX_VALUE) + 1;

                if (tryGScore < gScore.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    cameFrom.put(neighbor, current.id);
                    gScore.put(neighbor, tryGScore);
                    fScore.put(neighbor, tryGScore + heuristic(neighbor, end, width));

                    // add to the queue if not already here
                    if (!openSet.contains(new Node(neighbor, 0))) {
                        openSet.add(new Node(neighbor, fScore.get(neighbor)));
                    }
                }
            }
        }
        paths.push(new Stack<>());
        return paths; // no path to the end
    }
    /// takes the path to the end from the top of the stack returned by solveAStar and return it
    /// @param maze the maze to solve
    /// @return the best path to the end
    public static Stack<Integer> prepAStar(GraphMaze maze) {
        Stack<Integer> path;
        Stack<Stack<Integer>>paths;
        paths=solveAStar(maze);
        path=paths.pop();
        return path;
    }

    ///solve the manhattan's distance
    /// @param width the width of the maze
    /// @param a the node compared to the end
    /// @param b the end
    /// @return manhattan's distance
    private static int heuristic(int a, int b, int width) {
        int x1 = a % width, y1 = a / width;
        int x2 = b % width, y2 = b / width;
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    /// creation of the path to the end
    /// @param current the end
    /// @param parent the dictionary of sons - parents
    /// @return the path to the end
    private static Stack<Integer> reconstructPath(Map<Integer, Integer> parent, int current) {
        Stack<Integer> path = new Stack<>();
        path.push(current);

        while (parent.containsKey(current)) {
            current = parent.get(current);
            path.push(current);
        }

        //inversion of the path to get start->end
        Collections.reverse(path);
        return path;
    }

    /// Class helper for priority queue
    static class Node implements Comparable<Node> {
        int id;
        int fScore;

        /// constructor of Node
        /// @param id the node
        /// @param fScore the score of the node
        public Node(int id, int fScore) {
            this.id = id;
            this.fScore = fScore;
        }

        /// compare 2 scores and return the lesser one
        /// @param other the other node
        /// @return the element of node with the lesser score
        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.fScore, other.fScore);
        }


        /// compare an object with an element of Node/
        /// @param o the object to compare
        /// @return true if the object has the same class and the same id as the element of Node
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return id == node.id;
        }

        /// @return the hashcode of the element
        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }
}

