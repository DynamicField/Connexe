package fr.connexe.ui.game;


/// Thrown when a maze's topology is incompatible with a particular [GameMode].
///
/// Multiple scenarios can throw this exception:
/// - there's no path from the start cell to the end cell
/// - the start cell is a surrounded with walls
public class IncompatibleMazeException extends Exception {
    /// Makes a new IncompatibleMazeException with the given message.
    ///
    /// @param message The message to display to the end user, must be localized in French
    public IncompatibleMazeException(String message) {
        super(message);
    }
}
