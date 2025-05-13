package fr.connexe.algo.generation;

import fr.connexe.algo.ArrayMaze;
import fr.connexe.algo.GraphMaze;
import fr.connexe.algo.MazeSolver;
import fr.connexe.algo.Point;

import java.util.*;
import java.util.List;

/// Generates mazes (perfect and not perfect) using various algorithms.
///
/// Every function of this class returns a [MazeGenResult], which contains:
/// - the generated maze, using [GraphMaze]
/// - the complete generation log, using [MazeGenLog]
///
/// They also accept a seed for generating random numbers, which can be set to `null` to use a random seed.
public class MazeGenerator {
    /// Main function to debug the maze generator
    ///
    /// @param args command line args
    public static void main(String[] args) {
        MazeGenResult rand = makeDFS(4, 4, null);
        System.out.println(rand);
        introduceChaos(rand, 0.5f, null);
        System.out.println(rand);
    }

    /// Generates a **perfect maze** randomly, using Prim's algorithm.
    ///
    /// @param width  the width of the maze to generate
    /// @param height the height of the maze to generate
    /// @param seed   an optional seed for the RNG; a `null` value will generate a seed randomly.
    /// @return the generated perfect maze and its log, inside a [MazeGenResult]
    public static MazeGenResult makePrim(int width, int height, Long seed) {
        // Make an instance of the Random class, with the given seed. Generate one when the seed is empty.
        var random = seed != null ? new Random(seed) : new Random();

        // An empty maze, which will be the output of this algorithm.
        // We're going to connect vertices of this maze during Prim's algorithm.
        var maze = new GraphMaze(width, height);
        // The generation log which will contain events corresponding to each step of the algorithm.
        var log = new MazeGenLog(width, height);

        // A weighted graph with one vertex per maze cell.
        // Each cell is connected with its adjacent cells, with random edge weights.
        // This graph will be used by the Prim algorithm to make an MST (minimal spanning tree).
        // Example: [3x3; numbers in parentheses are edge weights]
        //   0 --(7)-- 1 --(24)--2
        //   |         |         |
        //  (10)      (15)      (31)
        //   |         |         |  
        //   3 --(4)-- 4 --(17)- 5
        //   |         |         |
        //  (22)      (12)      (19)
        //   |         |         |
        //   6 --(8)-- 7 --(11)- 8
        var primGraph = new RandomPrimGraph(maze, random);

        // The set of all vertices we've visited. The n'th element of this array tells if vertex n is visited or not.
        var visitedVertices = new boolean[maze.getNumCells()];
        // The priority queue with edges of the primGraph. Once an edge is dequeued, it will be added to the MST.
        var edgeQueue = new PriorityQueue<PrimEdge>(Comparator.comparingInt(c -> c.weight));

        // Initialize the MST with the first vertex (arbitrarily).
        visitedVertices[0] = true;
        // Add all edges of the first vertex to the Edge Queue.
        edgeQueue.addAll(primGraph.edges(0));

        // The main Prim algorithm loop
        while (!edgeQueue.isEmpty()) {
            // Dequeue an edge from the Edge Queue.
            PrimEdge edge = edgeQueue.poll();

            // Take the Chosen One, and connect its vertices in our maze; add its related event to the log.
            log.add(maze, new MazeGenEvent.Connect(edge.a, edge.b));

            // Find which vertex is the new one, the one which isn't yet in the MST; mark it as visited.
            int newVertex = visitedVertices[edge.a] ? edge.b : edge.a;
            visitedVertices[newVertex] = true;

            // Update the Edge Queue with:
            // - edges that are now eligible coming from the new vertex we have to add that weren't in the edge tree.
            // - edges that became ineligible, since they're now contained fully in the MST
            // Either way, we only need to check edges we've "discovered", i.e. the edges adjacent to the new vertex.
            for (PrimEdge adjEdge : primGraph.adjList[newVertex]) {
                // See if either end of the vertex has been visited.
                boolean aVisited = newVertex == adjEdge.a || visitedVertices[adjEdge.a];
                boolean bVisited = newVertex == adjEdge.b || visitedVertices[adjEdge.b];

                if (aVisited && bVisited) {
                    // This edge is entirely in the MST, we need to remove it!
                    edgeQueue.remove(adjEdge);
                } else {
                    // This edge is partially in the MST (one end of the edge isn't), add it!
                    edgeQueue.add(adjEdge);
                }
            }
        }

        // The Prim algorithm is done, and we just need to set the start and end vertices and log that out.
        // Technically, since this generated maze is perfect, we can choose any vertex located in the border
        // of the maze.
        log.add(new MazeGenEvent.SetEndpoints(0, maze.getNumCells() - 1));

        // Return the generated maze!
        return new MazeGenResult(maze, log);
    }

    /// Generates a **perfect maze** randomly, using a randomized depth-first search algorithm.
    ///
    /// @param width  the width of the maze to generate
    /// @param height the height of the maze to generate
    /// @param seed   an optional seed for the RNG; a `null` value will generate a seed randomly.
    /// @return the generated perfect maze and its log, inside a [MazeGenResult]
    public static MazeGenResult makeDFS(int width, int height, Long seed) {
        // Make the maze and generation log with the right dimensions.
        var maze = new GraphMaze(width, height);
        var log = new MazeGenLog(width, height);

        // Create the random instance using the given seed (or none).
        var random = seed != null ? new Random(seed) : new Random();

        // Make the visited array, where visited[n] <=> n'th vertex is visited.
        var visited = new boolean[maze.getNumCells()];

        // Start the DFS algorithm.
        dfsRandom(maze, log, visited, random, 0);

        // Finally, set both start and end points to be furthest apart and return the final maze!
        log.add(maze, new MazeGenEvent.SetEndpoints(0, maze.getNumCells() - 1));
        return new MazeGenResult(maze, log);
    }

    /// Transforms a **perfect maze** into a **non-perfect maze** by randomly introducing chaos: some
    /// walls will appear, and some will be removed, randomly.
    ///
    /// The resulting maze is guaranteed to be **non-perfect**.
    ///
    /// @param result the result from a previous generation, which will be modified with chaos!
    /// @param probability the probability of a wall being removed (0.0 to 1.0); will automatically be increased
    ///                    if it's too small for the given maze.
    /// @param seed an optional seed for the RNG; a `null` value will generate a seed randomly.
    public static void introduceChaos(MazeGenResult result, float probability, Long seed) {
        // Grab the maze and log from the result, so we can change them!
        GraphMaze maze = result.maze();
        MazeGenLog log = result.log();

        // Make the Random instance based off the seed.
        var random = seed != null ? new Random(seed) : new Random();

        // --- How the algorithm works ---
        // Assuming we have a perfect maze, we know that there's only one path from the start to the end.
        //
        // To make it "imperfect", we can arbitrarily add and remove walls to add cycles, or make some vertices
        // completely inaccessible.
        //
        // However, while doing this, we need to make sure that the path from start to end IS LEFT UNTOUCHED!
        // Else, we would risk having a labyrinth with no way to go from start to end!
        //
        // Essentially, we need to tamper with the maze randomly WITHOUT REMOVING ANY EDGE OF THE PATH.

        // Make sure the maze has proper start/end points.
        assert maze.getStart() != -1 && maze.getEnd () != -1;

        // Cap the probability to be in a reasonable range so the algorithm doesn't loop indefinitely.
        // Put a minimum of 1/(4*numCells), 4*numCells being approximately the number of edges in the maze.
        probability = Math.clamp(probability, 1.0f/(4 * maze.getNumCells()), 1.0f);

        // Find the path from the start point to the end point using the DFS algorithm.
        Integer[] path = MazeSolver.prepDFS(maze, 0).toArray(new Integer[0]);

        if (path.length < 2) {
            // Path is too short, can't introduce any chaos!
            return;
        }

        // A pair of two vertices that are blacklisted.
        record Blacklist(int a, int b) {
            // Returns true when this adjacent vertex is allowed.
            boolean allows(int v) {
                return a != v && b != v;
            }
        }

        // Associates every vertex with an optional list of neighbors that should NOT be tampered with.
        // Will be null for vertices that aren't in the path.
        Blacklist[] neighborBlacklists = new Blacklist[maze.getNumCells()];

        // Add a blacklist for every vertex in the path.
        // First vertex of the path: only the next vertex is blacklisted.
        neighborBlacklists[path[0]] = new Blacklist(path[1], -1);
        for (int i = 1; i < path.length-1; i++) {
            // In the middle of the path: both the previous and next vertices are blacklisted.
            int prev = i-1;
            int next = i+1;
            neighborBlacklists[path[i]] = new Blacklist(path[prev], path[next]);
        }
        // Last vertex of the path: only the previous vertex is blacklisted.
        neighborBlacklists[path[path.length-1]] = new Blacklist(-1, path[path.length-2]);

        // Iterate through all vertices and mess around with their neighbors, without removing edges of the path.
        // Repeat this process until we've done at least one operation to guarantee imperfectness.
        // The probability is set to be high enough so we don't repeat this over and over.
        boolean changedTheMaze = false;
        while (!changedTheMaze) {
            for (int v = 0; v < maze.getNumCells(); v++) {
                // Grab the blacklist for this vertex. CAN BE NULL!
                Blacklist blacklist = neighborBlacklists[v];

                // Look at its neighbors.
                for (int neighbor : neighbors(maze, v)) {
                    // Ignore neighbors that we're already processed before. Else, we would process edges twice!
                    if (neighbor < v) {
                        continue;
                    }

                    // See if we're going to mess up this neighbor. If not, go to the next neighbor.
                    if (random.nextFloat() > probability) {
                        continue;
                    }

                    // Connect or disconnect this neighbor from our current vertex.
                    boolean connected = maze.isConnected(v, neighbor);
                    if (connected && (blacklist == null || blacklist.allows(neighbor))) {
                        // This neighbor is connected, and it's not part of the path; disconnect it!
                        log.add(maze, new MazeGenEvent.Disconnect(v, neighbor));
                        changedTheMaze = true;
                    } else if (!connected) {
                        // This neighbor isn't connected, connect it! It won't invalidate the existing path anyway,
                        // it will just create a cycle.
                        log.add(maze, new MazeGenEvent.Connect(v, neighbor));
                        changedTheMaze = true;
                    }
                }
            }
        }
    }

    private static void dfsRandom(GraphMaze maze, MazeGenLog log, boolean[] visited, Random random, int vertex) {
        // Mark this vertex as visited.
        visited[vertex] = true;

        // Find all neighbors of this vertex, then shuffle it so we traverse the graph randomly.
        int[] neighbors = neighbors(maze, vertex);
        shuffleArray(random, neighbors);

        // Classic DFS stuff, iterate through all the neighbors of that vertex.
        for (int neighbor : neighbors) {
            if (!visited[neighbor]) {
                // This neighbor hasn't been visited; it's the first time we've seen it.
                // Connect it to this vertex and visit it in turn!
                log.add(maze, new MazeGenEvent.Connect(vertex, neighbor));
                dfsRandom(maze, log, visited, random, neighbor);
            }
        }
    }

    // Returns all the neighbors of a given vertex in a particular maze.
    private static int[] neighbors(GraphMaze maze, int vertex) {
        // The array which will contain all the neighbors.
        int[] neighbors = new int[4];
        // The index of the next neighbors to add in the array.
        int idx = 0;

        // Calculate the position of the vertex in the maze.
        Point pos = maze.toPoint(vertex);

        // Add the right neighbor if possible.
        Point right = pos.add(1, 0);
        if (maze.isValidPos(right)) {
            neighbors[idx++] = maze.toVertexId(right);
        }

        // Add the left neighbor if possible.
        Point left = pos.add(-1, 0);
        if (maze.isValidPos(left)) {
            neighbors[idx++] = maze.toVertexId(left);
        }

        // Add the up neighbor if possible.
        Point up = pos.add(0, 1);
        if (maze.isValidPos(up)) {
            neighbors[idx++] = maze.toVertexId(up);
        }

        // Add the down neighbor if possible.
        Point down = pos.add(0, -1);
        if (maze.isValidPos(down)) {
            neighbors[idx++] = maze.toVertexId(down);
        }

        // Return the array, resized if necessary.
        if (idx == 4) {
            // No need to resize, we have all the neighbors.
            return neighbors;
        } else {
            // Some neighbors are missing (must be a border vertex), resize the array.
            return Arrays.copyOf(neighbors, idx);
        }
    }

    // Shuffle the array using the Fisher-Yates algorithm
    // https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle
    private static void shuffleArray(Random rng, int[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            // Pick an index at random. Bound is exclusive, so we need to do + 1.
            int randomIdx = rng.nextInt(i + 1);

            // Swap the current element with the random element.
            int temp = array[randomIdx];
            array[randomIdx] = array[i];
            array[i] = temp;
        }
    }

    /// A graph like [GraphMaze], but with weighted edges of random weights.
    private static class RandomPrimGraph {
        final List<PrimEdge>[] adjList;
        final GraphMaze maze;
        final Random random;

        @SuppressWarnings("unchecked")
        RandomPrimGraph(GraphMaze maze, Random random) {
            // Initialize the adjacency list array with N lists.
            adjList = (List<PrimEdge>[]) new List[maze.getNumCells()];
            for (int i = 0; i < adjList.length; i++) {
                adjList[i] = new ArrayList<>();
            }

            // Set up the maze and some randomness.
            this.maze = maze;
            this.random = random;

            // Connect every vertex to its neighbors, using random weights.
            for (int i = 0; i < adjList.length; i++) {
                connectOffsetRandom(i, 0, 1);
                connectOffsetRandom(i, 0, -1);
                connectOffsetRandom(i, -1, 0);
                connectOffsetRandom(i, 1, 0);
            }
        }

        /// Get all adjacent edges of the given vertex
        ///
        /// @param vertex the vertex to look for adjacent edges
        /// @return the list of edges
        public List<PrimEdge> edges(int vertex) {
            return adjList[vertex];
        }

        /// Connects two vertices in the graph:
        /// - the `vertex` parameter
        /// - the same `vertex` of the [#maze], with an X and Y offset
        ///
        /// If the second vertex (the one with applied offsets) doesn't exist,
        /// nothing will be done.
        ///
        /// Only connects when the first vertex is smaller than the second vertex.
        /// This is done to avoid duplicate edges, since we ALWAYS call connect(a, b) AND
        /// connect(b, a), we just need to call connect(a, b) when a < b.
        ///
        /// Of course this won't be enough if we want this graph to be more "flexible",
        /// i.e. disconnect vertices dynamically.
        private void connectOffsetRandom(int vertex, int offsetX, int offsetY) {
            // Calculate both positions (start/end).
            Point pos = maze.toPoint(vertex);
            Point otherPos = pos.add(offsetX, offsetY);

            // Make sure this offset leads to a valid vertex.
            if (maze.isValidPos(otherPos)) {
                int otherVertex = maze.toVertexId(otherPos);

                // Preserve the "a < b" edge invariant for uniqueness.
                if (otherVertex > vertex) {
                    return;
                }

                // Add the same edge to both adjacency list
                PrimEdge e = new PrimEdge(vertex, otherVertex, random.nextInt());
                adjList[vertex].add(e);
                adjList[otherVertex].add(e);
            }
        }
    }

    /// An edge for [GraphMaze] with the following invariant: `a < b` to preserve uniqueness.
    private record PrimEdge(int a, int b, int weight) {
        PrimEdge {assert a < b;}
    }
}
