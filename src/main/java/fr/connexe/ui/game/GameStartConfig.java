package fr.connexe.ui.game;

/// All settings to begin a new arcade game.
///
/// @param players the players in the game; must not be empty
/// @param mode the game mode to play
public record GameStartConfig(
        PlayerProfile[] players,
        GameMode mode
) {
    /// Creates a new game start configuration.
    ///
    /// @param players the players in the game; must not be empty
    /// @param mode the game mode to play
    /// @throws IllegalArgumentException if players is empty
    public GameStartConfig {
        if (players.length == 0) {
            throw new IllegalArgumentException("Can't start a game with no players");
        }
    }
}
