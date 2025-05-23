package fr.connexe.ui.game.input;

/// Thrown when something went wrong with SDL2. It can be due to:
/// - some SDL2 function erroring out
/// - the SDL2 library failing to load
public class InputSystemException extends Exception {
    /// Creates a new InputSystemException with the given message.
    ///
    /// @param message the message to display
    public InputSystemException(String message) {
        super(message);
    }

    /// Creates a new InputSystemException with the given message and cause.
    ///
    /// @param message the message to display
    /// @param cause the cause of the exception
    public InputSystemException(String message, Throwable cause) {
        super(message, cause);
    }
}
