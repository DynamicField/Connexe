package fr.connexe.ui.game;

public sealed interface PlayerInputSource {
    record KeyboardZQSD() implements PlayerInputSource {}
    record KeyboardArrows() implements PlayerInputSource {}
    record Controller(int slot) implements PlayerInputSource {}
}
