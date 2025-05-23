package fr.connexe.algo;

/// Thrown when loading or saving a graph fails.
///
/// May contain a cause with additional info.
public class MazeSerializationException extends Exception {
    /// Constructs a [MazeSerializationException] with the specified detail message.
    ///
    /// @param message the detail message
    public MazeSerializationException(String message) {
        super(message);
    }

    /// Constructs a [MazeSerializationException] with the specified detail message and cause.
    ///
    /// @param message the detail message
    /// @param cause the cause of the exception
    public MazeSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
