package fr.connexe.algo.generation;

import fr.connexe.algo.GraphMaze;
import fr.connexe.algo.InvalidVertexException;

/// Contains both the start and end vertex of a maze to generate.
///
/// Both values should be valid vertices within the maze. Having only one endpoint (start/end) is not allowed.
///
/// Verification of bounds is done by the [GraphMaze].
///
/// @param startVertex the start vertex of the maze; must be valid and different from `endVertex`
/// @param endVertex   the end vertex of the maze; must be valid and different from `startVertex`
public record Endpoints(int startVertex, int endVertex) {
    /// Makes a new [Endpoints] instance with the start and end vertices.
    ///
    /// @param startVertex the start vertex of the maze; must be valid and different from `endVertex`
    /// @param endVertex   the end vertex of the maze; must be valid and different from `startVertex`
    /// @throws InvalidVertexException when the start or end vertex is invalid, or when both are equal
    public Endpoints {
        if (startVertex < 0 || endVertex < 0) {
            throw new InvalidVertexException("startVertex and endVertex cannot be negative: they must be >= 0. "
                    + "(startVertex=" + startVertex + ", endVertex=" + endVertex + ")");
        }
        if (startVertex == endVertex) {
            throw new InvalidVertexException("startVertex and endVertex cannot be the same: they must be different.");
        }
    }
}
