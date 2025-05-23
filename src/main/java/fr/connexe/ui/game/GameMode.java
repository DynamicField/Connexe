package fr.connexe.ui.game;

/// A game mode for arcade gameplay.
public enum GameMode {
    /// The path with the least moves wins.
    EFFICIENCY,
    /// The quickest player wins.
    SWIFTNESS,
    /// The end cell is hidden. Players have periodic hints to find it. "Hot and cold" style.
    FURTIVITY
}
