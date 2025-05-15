package fr.connexe.algo.generation;

import fr.connexe.algo.GraphMaze;

/// A **step of a maze generation algorithm**, contained in a [MazeGenLog].
///
/// Events contain **all necessary data** to be ***replayed***, building the [maze graph][GraphMaze]
/// incrementally. They are **created on the fly** by maze generation algorithms.
///
/// Some events are ***cosmetic*** and **give additional info specific to an algorithm**, which
/// doesn't affect the final maze.
/// For now, we don't have some like these, but it will happen!
///
/// ## How to use it properly
///
/// Use pattern matching for nicer syntax:
/// ```java
/// switch (event) {
///     case MazeGenEvent.Connect(int a, int b) -> doA();
///     case MazeGenEvent.SetEndpoints(int start, int end) -> { doB(); doC(); }
///     // etc...
/// }
/// ```
///
/// @see MazeGenLog
public sealed interface MazeGenEvent {
    /// **Two vertices** of the [maze graph][GraphMaze] **have been connected**:
    /// they are accessible, and **the wall between those two is now broken**.
    ///
    /// @param vertexA The first vertex to connect with `vertexB`.
    /// @param vertexB The second vertex to connect with `vertexA`.
    /// @see GraphMaze#connect
    record Connect(int vertexA, int vertexB) implements MazeGenEvent {}

    /// **Two vertices** of the [maze graph][GraphMaze] **have been disconnected**:
    /// they are no longer accessible, and **the wall between those two is now present**.
    ///
    /// @param vertexA The first vertex to disconnect from `vertexB`.
    /// @param vertexB The second vertex to disconnect from `vertexA`.
    /// @see GraphMaze#disconnect
    record Disconnect(int vertexA, int vertexB) implements MazeGenEvent {}

    /// Start and end vertices of the [maze graph][GraphMaze] have been set.
    /// Either values can be set to -1 to indicate no start or end.
    ///
    /// @param startVertex The start vertex; -1 indicates no start vertex.
    /// @param endVertex The end vertex; -1 indicates no end vertex.
    /// @see GraphMaze#setStart
    /// @see GraphMaze#setEnd
    record SetEndpoints(int startVertex, int endVertex) implements MazeGenEvent {}
}
