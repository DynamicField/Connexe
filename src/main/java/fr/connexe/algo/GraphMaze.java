package fr.connexe.algo;

import java.io.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/// A **rectangular maze** using a **graph structure** to represent connections between cells, called vertices.
///
/// This graph is undirected and non weighted.
///
/// ## Vertices
///
/// Each **vertex** corresponds to a **cell of the maze**, identified by an integer in `[0, width*height[`.
///
/// A 2D point can be converted to a vertex using [#toVertexId(Point)], using the `x+height*y` formula.
///
/// For example, in a 4x4 maze:
/// ```
/// 0  1  2  3
/// 4  5  6  7
/// 8  9  10 11
/// 12 13 14 15
///```
///
/// ## Edges
///
/// Each **edge** corresponds to a **missing wall** between two cells that are next to each other.
///
/// Cells that are too far apart (diagonals, distance ≥ 2) cannot be connected at all. We'll assume
/// that vertices A and B are always next to each other in the maze.
///
/// When an edge exists between A and B, there's no wall between A and B: you can walk from A to B.
///
/// When an edge doesn't exist between A and B, there's a wall between A and B: you cannot walk from A to B.
///
/// ## Endpoints
///
/// The maze can have a **start** and an **end** vertex. They can be either:
/// - not defined, both equal to -1
/// - defined, both different and valid vertices.
///
/// The existence of a path between the start and end vertices is not guaranteed.
///
/// ## Querying the maze
///
/// - [#isConnected(int, int)]: Tells if two vertices are connected.
/// - [#getAdjacentVertices(int)]: Get all atteignable vertices based on a start vertex.
///
/// You can also iterate through all vertices of this graph with a simple `for` loop:
///
/// ```java
/// GraphMaze gm;
/// for (int i = 0; i < gm.getNumCells(); i++){
///     // ... Do stuff with i ...
///}
///```
///
/// ## Changing the maze
///
/// - [#connect(int, int)]: Add an edge between two vertices.
/// - [#disconnect(int, int)]: Remove an edge between two vertices.
/// - [#setConnected(int, int, boolean)]: Add/remove an edge between two vertices.
/// - [#setEndpoints(int, int)]: Set the start and end points of the maze
public class GraphMaze implements Serializable {
    // Version of this class for Java serialization. Increment it when changing the class!
    @Serial
    private static final long serialVersionUID = 1L;

    /// The width of the maze. (number of columns)
    private final int width;
    /// The height of the maze. (number of rows)
    private final int height;
    /// The total number of cells in the maze (= width * height).
    private final int numCells;
    /// The start vertex. A value of -1 indicates no start vertex.
    private int start;
    /// The end vertex. A value of -1 indicates no end vertex.
    private int end;
    /// The adjacency list of the graph. Each vertex has a list of vertices it is connected to.
    private final List<Integer>[] edges; // Like a Map<Integer, List<Integer>> (https://i.imgur.com/HNeObED.png)

    /// Makes an empty maze with inaccessible cells, and no start/end points.
    ///
    /// @param width  The width of the maze. (number of columns)
    /// @param height The height of the maze. (number of rows)
    @SuppressWarnings("unchecked") // For the (List<Integer>[]) cast
    public GraphMaze(int width, int height) {
        // Check the width and height.
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width and height must be positive (not zero)");
        }

        // Set all attributes
        this.width = width;
        this.height = height;
        this.numCells = Math.multiplyExact(width, height); // Avoid integer overflow!
        this.start = -1;
        this.end = -1;

        // Make the adjacency list with N empty lists (ArrayList<Integer>).
        this.edges = (List<Integer>[]) new List[numCells];
        for (int i = 0; i < numCells; i++) {
            edges[i] = new ArrayList<>();
        }
    }

    // Constructor for cloning
    @SuppressWarnings("unchecked") // For the (List<Integer>[]) cast
    private GraphMaze(GraphMaze other) {
        this.width = other.width;
        this.height = other.height;
        this.numCells = other.numCells;
        this.start = other.start;
        this.end = other.end;

        // Make the adjacency list with N copied lists
        this.edges = (List<Integer>[]) new List[numCells];
        for (int i = 0; i < numCells; i++) {
            edges[i] = new ArrayList<>(other.edges[i]);
        }
    }

    /// Converts 2D coordinates of a point in the maze to its corresponding vertex id.
    ///
    /// @param p The 2D coordinates of the point.
    /// @return The vertex id.
    public int toVertexId(Point p) {
        return p.x() + p.y() * width;
    }

    /// Converts a vertex id to its 2D coordinates.
    ///
    /// @param id The vertex id.
    /// @return The 2D coordinates of the point.
    public Point toPoint(int id) {
        return new Point(id % width, id / width);
    }

    /// Returns true when vertices A and B are connected: there's no wall between A and B.
    ///
    /// When both vertices are too far apart, this method will always return false.
    ///
    /// When either vertex is invalid, returns false. This is actually useful when iterating across
    /// adjacent cells, for instance.
    ///
    /// @param vertexA The first vertex; must be valid.
    /// @param vertexB The second vertex; can be invalid.
    /// @return true when vertices A and B are connected; false otherwise.
    public boolean isConnected(int vertexA, int vertexB) {
        // When vertexA is invalid, return false directly.
        if (!isValidVertex(vertexA)) {
            return false;
        }

        // Use the adjacency list to check that they are indeed connected.
        return edges[vertexA].contains(vertexB);
    }

    /// Connects two vertices A and B to make them accessible.
    ///
    /// This means that there will be no wall between A and B.
    ///
    /// The vertices must be next to each other in the maze. Diagonals or very distant connections are not allowed.
    ///
    /// @param vertexA The first vertex.
    /// @param vertexB The second vertex.
    /// @return true when they weren't connected before; false when nothing changed.
    public boolean connect(int vertexA, int vertexB) {
        return setConnected(vertexA, vertexB, true);
    }

    /// Disconnects two vertices A and B, making them unaccessible.
    ///
    /// This means that there will be a wall between A and B.
    ///
    /// The vertices must be next to each other in the maze. Diagonals or very distant connections are not allowed.
    ///
    /// @param vertexA The first vertex.
    /// @param vertexB The second vertex.
    /// @return true when they were connected before; false when nothing changed.
    public boolean disconnect(int vertexA, int vertexB) {
        return setConnected(vertexA, vertexB, false);
    }

    /// Connects or disconnects two vertices A and B, depending on the given boolean `connected`.
    ///
    /// Two vertices are connected when there exist no wall between A and B.
    ///
    /// The vertices must be next to each other in the maze. Diagonals or very distant connections are not allowed.
    ///
    /// @param vertexA The first vertex.
    /// @param vertexB The second vertex.
    /// @param connected true to connect both vertices; false to disconnect them.
    /// @return true when the graph changed; false when nothing changed.
    public boolean setConnected(int vertexA, int vertexB, boolean connected) {
        // Check that the vertices EXIST.
        checkVertex(vertexA);
        checkVertex(vertexB);

        // Make sure that both vertices are close enough to be connected.
        Point posA = toPoint(vertexA);
        Point posB = toPoint(vertexB);
        if (Math.abs(posA.x() - posB.x()) > 1 || Math.abs(posA.y() - posB.y()) > 1) {
            throw new IllegalArgumentException("Can't connect " + vertexA + " to " + vertexB + ": they are too far apart.");
        }

        // Make sure we don't have cycles in our graph. Would be not very mazey.
        if (vertexA == vertexB) {
            throw new IllegalArgumentException("Can't connect " + vertexA + " to " + vertexB + ": this graph does not allow cycles.");
        }

        if (connected) {
            // We want to connect A to B (and B to A).

            // When A is already connected to B, don't do anything
            if (edges[vertexA].contains(vertexB)) {
                return false;
            }

            // Else, connect them!
            edges[vertexA].add(vertexB);
            edges[vertexB].add(vertexA);

        } else {
            // We want to disconnect A from B.

            // When A is not connected to B, don't do anything
            if (!edges[vertexA].contains(vertexB)) {
                return false;
            }

            // Else, disconnect them!
            edges[vertexA].remove((Integer) vertexB);
            edges[vertexB].remove((Integer) vertexA);
        }

        return true;
    }

    /// Sets both start and end vertices.
    ///
    /// To indicate an absence of endpoints, both must be set to -1.
    /// Both vertices must be valid and different from each other.
    ///
    /// @throws InvalidVertexException when either vertex is invalid, unless both are -1.
    /// @throws IllegalArgumentException when start and end are the same.
    /// @param start the start vertex
    /// @param end the end vertex
    public void setEndpoints(int start, int end) {
        if (start == -1 && end == -1) {
            // Empty start/end; clear current endpoints.
            this.start = -1;
            this.end = -1;
            return;
        }

        // Check that both vertices are valid.
        checkVertex(start);
        checkVertex(end);

        // Make sure we can't get start == end
        if (start == end) {
            throw new IllegalArgumentException("Start and end vertices cannot be the same.");
        }

        // Set the start and end vertices.
        this.start = start;
        this.end = end;
    }

    /// Returns all adjacent vertices of the given vertex V. Changes to the maze are reflected in this list.
    ///
    /// @param vertex The vertex to get the adjacent vertices of
    /// @throws InvalidVertexException when the vertex is invalid.
    /// @return A list containing all adjacent vertices, unmodifiable.
    public List<Integer> getAdjacentVertices(int vertex) {
        checkVertex(vertex);

        return Collections.unmodifiableList(this.edges[vertex]);
    }

    /// Converts this maze into an [ArrayMaze], with all walls properly set to match the edges of this graph.
    ///
    /// @return a snapshot of this graph's state in [ArrayMaze] format
    /// @see #toArrayMaze(boolean)
    public ArrayMaze toArrayMaze() {
        return toArrayMaze(false);
    }

    /// Converts this maze into an [ArrayMaze], with all walls properly set to match the edges of this graph.
    ///
    /// Endpoints can be hidden (i.e. border walls won't be removed) by using the `hideEndpoints` parameter.
    ///
    /// @param hideEndpoints true when start and end points should not change the walls on the border of the maze
    /// @return a snapshot of this graph's state in [ArrayMaze] format
    public ArrayMaze toArrayMaze(boolean hideEndpoints) {
        // Make a 2D array of cells for the array maze.
        Cell[][] cells = new Cell[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Find the vertex id of this (x, y) point.
                int vertex = toVertexId(new Point(x, y));

                // Determine the endpoint status of that cell; hide them if hideEndpoints is true.
                Cell.Endpoint endpoint;
                if (vertex == start && !hideEndpoints) { endpoint = Cell.Endpoint.START; }
                else if (vertex == end && !hideEndpoints) { endpoint = Cell.Endpoint.END; }
                else { endpoint = Cell.Endpoint.NONE; }

                // When the vertex is either a start or end vertex, we need to avoid
                // creating walls on the border of the maze.
                boolean isEndpoint = endpoint != Cell.Endpoint.NONE;
                boolean noLeftWall = isEndpoint && x == 0;
                boolean noRightWall = isEndpoint && x == width - 1;
                boolean noUpWall = isEndpoint && y == 0;
                boolean noDownWall = isEndpoint && y == height - 1;

                // Compute all walls of the maze.
                boolean wallLeft = !noLeftWall && !isConnected(vertex, toVertexId(new Point(x - 1, y)));
                boolean wallRight = !noRightWall && !isConnected(vertex, toVertexId(new Point(x + 1, y)));
                boolean wallUp = !noUpWall && !isConnected(vertex, toVertexId(new Point(x, y - 1)));
                boolean wallDown = !noDownWall && !isConnected(vertex, toVertexId(new Point(x, y + 1)));

                // Put it in the array.
                cells[y][x] = new Cell(new Point(x, y), wallLeft, wallRight, wallUp, wallDown, endpoint);
            }
        }

        return new ArrayMaze(cells, width, height);
    }

    /// Clones this maze into a new instance of [GraphMaze] with identical edges, start/end points, etc.
    ///
    /// @return a clone of this maze
    @SuppressWarnings("MethodDoesntCallSuperMethod") // It's intentional!
    @Override
    public GraphMaze clone() {
        return new GraphMaze(this);
    }

    /// Saves this maze into to a stream.
    ///
    /// @param output The output to save the maze into.
    /// @throws MazeSerializationException when the maze failed to be saved.
    /// @see #load(InputStream)
    public void save(OutputStream output) throws MazeSerializationException {
        try (ObjectOutputStream out = new ObjectOutputStream(output)) {
            out.writeObject(this);
        } catch (IOException e) {
            // When we encounter an IOException, put it inside a MazeSerializationException.
            throw new MazeSerializationException("Failed to save the maze.", e);
        }
    }

    /// Loads a maze from a stream.
    ///
    /// @param input The stream to load the maze from.
    /// @throws MazeSerializationException when the maze failed to be loaded (due to IO or Java issues).
    /// @return the loaded maze, in graph format.
    public static GraphMaze load(InputStream input) throws MazeSerializationException {
        try (ObjectInputStream in = new ObjectInputStream(input)) {
            return (GraphMaze) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            // When we encounter an IOException or a ClassNotFoundException, put it inside a MazeSerializationException.
            throw new MazeSerializationException("Failed to load the maze.", e);
        }
    }

    /// Returns true if the given vertex is valid.
    /// @param v The vertex to check.
    /// @return true if the given vertex is valid.
    public boolean isValidVertex(int v) {
        return v >= 0 && v < numCells;
    }

    /// Returns true is the 2D coordinates are contained within the graph.
    /// @param p the point to check
    /// @return true when the point is inside the maze.
    public boolean isValidPos(Point p) {
        return p.x() >= 0 && p.x() < width &&
                p.y() >= 0 && p.y() < height;
    }

    /// Checks if a vertex is valid.
    private void checkVertex(int v) {
        if (!isValidVertex(v)) {
            throw new InvalidVertexException("Invalid vertex id " + v + ". It must be in [0, " + numCells + "[.");
        }
    }

    /// Returns a cool ASCII representation of the maze.
    /// @return a string representation of the maze.
    @Override
    public String toString() {
        return toArrayMaze().toString();
    }

    // --- Boring getters/setters ---

    /// Returns the width of the maze.
    /// @return the width of the maze.
    public int getWidth() {
        return width;
    }

    /// Returns the height of the maze.
    /// @return the height of the maze.
    public int getHeight() {
        return height;
    }

    /// Returns the total number of cells in the maze.
    /// @return the total number of cells in the maze.
    public int getNumCells() {
        return numCells;
    }

    /// Returns the start vertex. A value of -1 indicates no start vertex.
    /// @return the start vertex.
    public int getStart() {
        return start;
    }

    /// Returns the coordinates of the start vertex. Returns null if no start vertex.
    /// @return the coordinates of the start vertex.
    public Point getStartPoint() {
        if (start == -1) {
            return null;
        }
        return toPoint(start);
    }

    /// Returns the coordinates of the end vertex. Returns null if no end vertex.
    /// @return the coordinates of the end vertex.
    public Point getEndPoint() {
        if (end == -1) {
            return null;
        }
        return toPoint(end);
    }

    /// Returns the end vertex. A value of -1 indicates no end vertex.
    /// @return the end vertex.
    public int getEnd() {
        return end;
    }
}
