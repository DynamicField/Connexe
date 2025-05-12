package fr.connexe.algo.generation;

import fr.connexe.algo.GraphMaze;

/// The result of a maze generation algorithm. Contains both the graph and the generation log.
///
/// @param maze the resulting [GraphMaze].
/// @param log  the [MazeGenLog] describing the steps taken during the algorithm.
public record MazeGenResult(GraphMaze maze, MazeGenLog log) {}
