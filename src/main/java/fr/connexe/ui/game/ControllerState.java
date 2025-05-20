package fr.connexe.ui.game;

public record ControllerState(
        int slot,
        boolean dpadLeftPressed,
        boolean dpadRightPressed,
        boolean dpadUpPressed,
        boolean dpadDownPressed,
        float leftJoystickX,
        float leftJoystickY,
        float rightJoystickX,
        float rightJoystickY
) {
}
