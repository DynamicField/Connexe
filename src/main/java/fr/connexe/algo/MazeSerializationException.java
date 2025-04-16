package fr.connexe.algo;

/// Thrown when loading or saving a graph fails.
///
/// May contain a cause with additional info.
public class MazeSerializationException extends Exception {
    public MazeSerializationException(String message) {
        super(message);
    }

    public MazeSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
