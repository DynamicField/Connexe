package fr.connexe.ui.game.input;

/// The state of a controller's buttons and joysticks.
///
/// @param slot the index of the controller in the `controllers` array
/// @param dpadLeftPressed true when the left D-pad button is pressed
/// @param dpadRightPressed true when the right D-pad button is pressed
/// @param dpadUpPressed true when the up D-pad button is pressed
/// @param dpadDownPressed true when the down D-pad button is pressed
/// @param leftJoystickX position of the left joystick X-axis, ranging from -1.0 to 1.0
/// @param leftJoystickY position of the left joystick Y-axis, ranging from -1.0 to 1.0
/// @param rightJoystickX position of the right joystick X-axis, ranging from -1.0 to 1.0
/// @param rightJoystickY position of the right joystick Y-axis, ranging from -1.0 to 1.0
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
