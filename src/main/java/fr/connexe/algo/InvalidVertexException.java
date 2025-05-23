package fr.connexe.algo;

/// Thrown when an invalid vertex is used as an argument in [GraphMaze].
public class InvalidVertexException extends RuntimeException {
    /// Constructs an [InvalidVertexException] with the specified detail message.
    ///
    /// @param message the detail message
    public InvalidVertexException(String message) {
        super(message);
    }
}
