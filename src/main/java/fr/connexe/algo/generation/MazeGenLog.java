package fr.connexe.algo.generation;

import fr.connexe.algo.GraphMaze;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/// A sequence of [maze generation events][MazeGenEvent] describing **all steps taken during a maze generation algorithm**.
///
/// Logs are **created by maze generation algorithms**, alongside the final generated maze.
///
/// Logs allow you to **scrub through the algorithm step by step**, by using the [#buildMazeUntil(int)] method. This is
/// possible by **applying events to mazes** (using [#applyEvent(GraphMaze, MazeGenEvent)]).
///
/// ## Examples
///
/// ### DFS-generated imperfect maze
///
/// Using the following code to generate an imperfect maze:
/// ```java
/// MazeGenResult result = MazeGenerator.makeDFS(3, 3, null);
/// MazeGenerator.introduceChaos(result, 0.15f, null);
/// System.out.println(result);
/// ```
/// Gives this output:
/// ```
/// ===========================================
///              GENERATED MAZE
/// ===========================================
/// #   #---#---#
///         |   |
///  000 001|002|
///         |   |
/// #---#   #   #
/// |       |   |
/// |003 004|005|
/// |       |   |
/// #   #   #---#
/// |
/// |006 007 008
/// |
/// #---#---#   #
/// ===========================================
///            MAZE GENERATION LOG
/// ===========================================
/// MazeGenLog [3x3] 11 events:
///   0   : Connect[vertexA=0, vertexB=1]
///   1   : Connect[vertexA=1, vertexB=4]
///   2   : Connect[vertexA=4, vertexB=7]
///   3   : Connect[vertexA=7, vertexB=6]
///   4   : Connect[vertexA=6, vertexB=3]
///   5   : Connect[vertexA=7, vertexB=8]
///   6   : Connect[vertexA=8, vertexB=5]
///   7   : Connect[vertexA=5, vertexB=2]
///   8   : SetEndpoints[startVertex=0, endVertex=8]
///   9   : Connect[vertexA=3, vertexB=4]
///   10  : Disconnect[vertexA=5, vertexB=8]
/// ```
///
/// @see MazeGenEvent
/// @see MazeGenResult
public class MazeGenLog implements Iterable<MazeGenEvent> {
    private final List<MazeGenEvent> events;
    private final int mazeWidth;
    private final int mazeHeight;

    /// Creates an empty maze generation log, with the given dimensions.
    ///
    /// @param mazeWidth  the width of the maze
    /// @param mazeHeight the height of the maze
    public MazeGenLog(int mazeWidth, int mazeHeight) {
        // Check width/height
        if (mazeWidth <= 0 || mazeHeight <= 0) {
            throw new IllegalArgumentException("mazeWidth and mazeHeight must be positive (not zero and not negative)");
        }

        // Initialize all fields
        this.mazeWidth = mazeWidth;
        this.mazeHeight = mazeHeight;
        this.events = new ArrayList<>();
    }

    /// Adds a new event to the log and apply it to the given maze.
    ///
    /// @param maze the maze to apply the event to
    /// @param event the event to add and apply to the maze
    /// @see #applyEvent(GraphMaze, MazeGenEvent)
    public void add(GraphMaze maze, MazeGenEvent event) {
        applyEvent(maze, event);
        events.add(event);
    }

    /// Adds a new event to the log. Events are not validated yet.
    ///
    /// @param event the event to add
    public void add(MazeGenEvent event) {
        events.add(event);
    }

    /// Gets the event at a specific index.
    ///
    /// @param idx the index of the event to get
    /// @return the event at the given index
    /// @throws IndexOutOfBoundsException the index is out of bounds
    public MazeGenEvent get(int idx) {
        return events.get(idx);
    }

    /// Returns the total number of events.
    ///
    /// @return the number of events
    public int size() {
        return events.size();
    }

    /// Applies an event to change a maze's state.
    ///
    /// The maze will be modified with the "instruction" given by the event.
    ///
    /// Cosmetic events won't change the maze at all.
    ///
    /// @param maze the maze to apply the event to
    /// @param event the event to apply
    public void applyEvent(GraphMaze maze, MazeGenEvent event) {
        switch (event) {
            case MazeGenEvent.Connect(int vertexA, int vertexB) -> maze.connect(vertexA, vertexB);
            case MazeGenEvent.Disconnect(int vertexA, int vertexB) -> maze.disconnect(vertexA, vertexB);
            case MazeGenEvent.SetEndpoints(int startVertex, int endVertex) -> maze.setEndpoints(startVertex, endVertex);
        }
    }

    /// Builds a [GraphMaze] at a particular point in time, by applying all events **STRICTLY BEFORE `maxEventIndex`**.
    /// The event at index `maxEventidx` **will not be applied**.
    ///
    /// An index of 0 will give the initial maze (default, all walls, no connections).
    ///
    /// An index of [#size()] will give the final maze (the algorithm is done).
    ///
    /// @param maxEventIndex the exclusive event index before which all events are applied;
    ///                                       must be in the interval `[0, size]`.
    /// @return the maze created by applying all events in the interval `[0, maxEventIndex[`
    public GraphMaze buildMazeUntil(int maxEventIndex) {
        // Make sure the index isn't out of bounds
        if (maxEventIndex < 0 || maxEventIndex > events.size()) {
            throw new IllegalArgumentException("maxEventIndex must be a valid index, in [0, " + events.size() + "]");
        }

        // Make the initial maze
        var maze = new GraphMaze(mazeWidth, mazeHeight);
        for (int i = 0; i < maxEventIndex; i++) {
            // Find the event and apply it to the maze.
            MazeGenEvent event = events.get(i);
            applyEvent(maze, event);
        }

        return maze;
    }

    /// Builds the final maze by replaying all events.
    ///
    /// @return the final maze generated by the algorithm.
    public GraphMaze buildMaze() {
        return buildMazeUntil(events.size());
    }

    /// {@inheritDoc}
    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("MazeGenLog [")
                .append(mazeWidth)
                .append("x")
                .append(mazeHeight)
                .append("] ")
                .append(size())
                .append(" events:\n");
        for (int i = 0; i < size(); i++) {
            sb.append(String.format("  %-3s : %s\n", i, events.get(i).toString()));
        }
        return sb.toString();
    }

    /// Gets the raw list of all events.
    ///
    /// @return the list of events
    public List<MazeGenEvent> getEvents() {
        return events;
    }

    /// Gets the width of the maze.
    ///
    /// @return the width
    public int getMazeWidth() {
        return mazeWidth;
    }

    /// Gets the height of the maze.
    ///
    /// @return the height
    public int getMazeHeight() {
        return mazeHeight;
    }

    /// {@inheritDoc}
    @Override
    public Iterator<MazeGenEvent> iterator() {
        return events.iterator();
    }
}

