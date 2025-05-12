package fr.connexe.algo;

import java.util.*;
import java.util.List;

/// Generates mazes (perfect and not perfect) using various algorithms.
///
/// Also provides an "event log" to know what algorithms do, so you can replay them step by step.
@SuppressWarnings("OptionalUsedAsFieldOrParameterType") // let my Optional<T> as they are!
public class MazeGenerator {
    /// Main function to debug the maze generator
    /// @param args command line args
    public static void main(String[] args) {
        GraphMaze rand = makePrim(8, 4, OptionalLong.empty());
        System.out.println(rand);
    }

    /// Generates a **perfect maze** randomly, using Prim's algorithm.
    ///
    /// The seed can be given manually, or generated automatically with an empty optional using [OptionalLong#empty()].
    ///
    /// @param width  the width of the maze to generate
    /// @param height the height of the maze to generate
    /// @param seed   an optional seed for the RNG;
    ///                 an empty value ([OptionalLong#empty()]) will generate a seed randomly.
    /// @return the generated perfect maze, in graph format
    public static GraphMaze makePrim(int width, int height, OptionalLong seed) {
        // Make an instance of the Random class, with the given seed. Generate one when the seed is empty.
        var random = seed.isPresent() ? new Random(seed.getAsLong()) : new Random();

        // An empty maze, which will be the output of this algorithm.
        // We're going to connect vertices of this maze during Prim's algorithm.
        var maze = new GraphMaze(width, height);

        // A weighted graph with one vertex per maze cell.
        // Each cell is connected with its adjacent cells, with random edge weights.
        // This graph will be used by the Prim algorithm to make an MST (minimal spanning tree).
        var primGraph = new RandomPrimGraph(maze, random);

        // The set of all vertices we've visited.
        var visitedVertices = new HashSet<Integer>();
        // The priority queue with edges of the primGraph. Once an edge is dequeued, it will be added to the MST.
        var edgeQueue = new PriorityQueue<PrimEdge>(Comparator.comparingInt(c -> c.weight));

        // Initialize the MST with the first vertex (arbitrarily).
        visitedVertices.add(0);
        // Add all edges of the first vertex to the Edge Queue.
        edgeQueue.addAll(primGraph.edges(0));

        // The main Prim algorithm loop
        while (!edgeQueue.isEmpty()) {
            // Dequeue an edge from the Edge Queue.
            PrimEdge edge = edgeQueue.poll();

            // Take the Chosen One, and connect its vertices in our maze.
            maze.connect(edge.a, edge.b);

            // Find which vertex is the new one, the one which isn't yet in the MST; mark it as visited.
            int newVertex = visitedVertices.contains(edge.a) ? edge.b : edge.a;
            visitedVertices.add(newVertex);

            // Update the Edge Queue with:
            // - edges that are now eligible coming from the new vertex we have to add that weren't in the edge tree.
            // - edges that became ineligible, since they're now contained fully in the MST
            // Either way, we only need to check edges we've "discovered", i.e. the edges adjacent to the new vertex.
            for (PrimEdge adjEdge : primGraph.adjList[newVertex]) {
                // See if either end of the vertex has been visited.
                boolean aVisited = newVertex == adjEdge.a || visitedVertices.contains(adjEdge.a);
                boolean bVisited = newVertex == adjEdge.b || visitedVertices.contains(adjEdge.b);

                if (aVisited && bVisited) {
                    // This edge is entirely in the MST, we need to remove it!
                    edgeQueue.remove(adjEdge);
                } else {
                    // This edge is partially in the MST (one end of the edge isn't), add it!
                    edgeQueue.add(adjEdge);
                }
            }
        }

        // The Prim algorithm is done, and we just need to set the start and end vertices.
        // Technically, since this generated maze is perfect, we can choose any vertex located in the border
        // of the maze.
        maze.setStart(0);
        maze.setEnd(maze.getNumCells() - 1);

        // Return the generated maze!
        return maze;
    }

    /// A graph like [GraphMaze], but with weighted edges of random weight.
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

            // Connect every adjacency vertex
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
