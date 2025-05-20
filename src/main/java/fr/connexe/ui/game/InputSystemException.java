package fr.connexe.ui.game;

public class InputSystemException extends Exception {
    public InputSystemException(String message) {
        super(message);
    }

    public InputSystemException(String message, Throwable cause) {
        super(message, cause);
    }
}
