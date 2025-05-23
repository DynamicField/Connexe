package fr.connexe.ui.game.input;

/// Indicates which peripheral the player uses to move in the maze during a game.
public sealed interface PlayerInputSource {
    /// The player uses the keyboard with the ZQSD keys (AZERTY) to move in the maze.
    record KeyboardZQSD() implements PlayerInputSource {}
    /// The player uses the keyboard with arrow keys to move in the maze.
    record KeyboardArrows() implements PlayerInputSource {}
    /// The player uses the n-th connected controller to move in the maze.
    ///
    /// @param slot the index of the controller in the `controllers` array; starts at 0
    record Controller(int slot) implements PlayerInputSource {}
}
