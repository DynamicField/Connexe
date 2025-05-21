package fr.connexe.ui.game.input;

import org.libsdl.SDL;
import org.libsdl.SDL_Error;
import org.libsdl.SDL_GameController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/// Receives game controller input, all in one place, using SDL2.
///
/// The controller hub can [poll][#poll()] new input data from SDL2, and produces snapshots of each controller
/// and their state (pressed buttons, joysticks, etc.).
///
/// Polling must be done as often as possible, ideally every frame.
///
/// @see ControllerHub.State
public class ControllerHub {
    private static boolean sdlInitialized; // true if SDL has been initialized globally
    // The list of all connected controllers. It may contains "null" values if a controller has been disconnected.
    // In which case, the next connected controller will take over the next available slot.
    private final List<LocalController> controllers = new ArrayList<>();
    private volatile State lastState = State.EMPTY; // The last recorded state of the controllers

    private static void initializeSDL() throws InputSystemException {
        if (!sdlInitialized) {
            // Initialize a bunch of hints for SDL
            SDL.SDL_SetHint("SDL_JOYSTICK_ALLOW_BACKGROUND_EVENTS", "1");
            SDL.SDL_SetHint("SDL_ACCELEROMETER_AS_JOYSTICK", "0");
            SDL.SDL_SetHint("SDL_MAC_BACKGROUND_APP", "1");
            SDL.SDL_SetHint("SDL_XINPUT_ENABLED", "1");
            // rawinput is broken for some reason with DPAD buttons
            SDL.SDL_SetHint("SDL_JOYSTICK_RAWINPUT", "0");

            // Actually Initialize SDL
            sdlInitialized = true;
            if (SDL.SDL_Init(SDL.SDL_INIT_GAMECONTROLLER | SDL.SDL_INIT_JOYSTICK | SDL.SDL_INIT_EVENTS) != 0) {
                throw new InputSystemException("Failed to initalize SDL.", new SDL_Error());
            }

            try {
                // Read the controller mappings to recognize as much controllers as possible
                if (!SDL_GameController.addMappingsFromFile("/gamecontrollerdb.txt", SDL.class)) {
                    throw new InputSystemException("Failed to load game controller mappings.", new SDL_Error());
                }
            } catch (IOException e) {
                throw new InputSystemException("Failed to fetch the game controller database file", e);
            } catch (SDL_Error e) {
                throw new InputSystemException("Failed to process the game controller database file using SDL", e);
            }
        }
    }

    /// Creates a new [ControllerHub] and initializes SDL2 if necessary.
    ///
    /// @throws InputSystemException when something went wrong with SDL2
    public ControllerHub() throws InputSystemException {
        try {
            initializeSDL();
        } catch (InputSystemException e) {
            throw e;
        } catch (Throwable t) {
            // In case we have a "catastrophic" error (i.e. UnsatisfiedLinkError), just continue
            // without a controller hub.
            throw new InputSystemException("Failed to initialize the controller hub", t);
        }
    }

    /// Queries SDL2 for currently connected controllers and their state (pressed buttons, joystick positions, etc.).
    ///
    /// Must be called often to get the latest controller input info.
    ///
    /// @return all connected controllers and their state
    public State poll() {
        // Update the SDL state
        SDL.SDL_PumpEvents();
        SDL.SDL_GameControllerUpdate();
        int n = SDL.SDL_NumJoysticks();

        // Remove disconnected controllers
        for (int i = controllers.size() - 1; i >= 0; i--) {
            LocalController controller = controllers.get(i);
            if (!controller.sdl.getAttached()) {
                // This controller isn't attached anymore, remove it!
                removeController(controller);
            }
        }

        // Check for new controllers
        for (int i = 0; i < n; i++) {
            if (SDL.SDL_IsGameController(i)) {
                // Grab its instance ID using the Joystick API
                int instanceId = SDL.SDL_JoystickGetDeviceInstanceID(i);
                if (controllerAlreadyConnected(instanceId)) {
                    // We already know this controller, let's not add it to our list.
                    continue;
                }

                // Connect the new controller
                SDL_GameController sdlController = SDL_GameController.GameControllerOpen(i);
                addController(new LocalController(sdlController, instanceId));
            }
        }

        // Fetch the complete state of all connected controllers
        ControllerState[] snapshots = new ControllerState[controllers.size()];
        for (int i = 0; i < controllers.size(); i++) {
            LocalController controller = controllers.get(i);
            if (controller == null) {
                // If it's null, then it's an empty controller slot.
                continue;
            }

            // Copy all button states we're interested in.
            snapshots[i] = new ControllerState(
                    i,
                    controller.sdl.getButton(SDL.SDL_CONTROLLER_BUTTON_DPAD_LEFT),
                    controller.sdl.getButton(SDL.SDL_CONTROLLER_BUTTON_DPAD_RIGHT),
                    controller.sdl.getButton(SDL.SDL_CONTROLLER_BUTTON_DPAD_UP),
                    controller.sdl.getButton(SDL.SDL_CONTROLLER_BUTTON_DPAD_DOWN),
                    controller.sdl.getAxis(SDL.SDL_CONTROLLER_AXIS_LEFTX),
                    controller.sdl.getAxis(SDL.SDL_CONTROLLER_AXIS_LEFTY),
                    controller.sdl.getAxis(SDL.SDL_CONTROLLER_AXIS_RIGHTX),
                    controller.sdl.getAxis(SDL.SDL_CONTROLLER_AXIS_RIGHTY)
            );
        }

        // Update the last state and return it!
        lastState = new State(snapshots);
        return lastState;
    }

    private void addController(LocalController controller) {
        // Find the next null slot if there's one
        for (int i = 0; i < controllers.size(); i++) {
            if (controllers.get(i) == null) {
                controllers.set(i, controller);
                return;
            }
        }

        // No null slot: add it at the end of the list
        controllers.add(controller);

        System.out.println("Controller now connected: " + controller);
    }

    private void removeController(LocalController controller) {
        // Find the index of the controller in the list.
        int index = controllers.indexOf(controller);
        assert index != -1;

        // Remove it from the list depending on the index.
        if (index == controllers.size() - 1) {
            // Last controller: just remove it
            controllers.removeLast();
        } else {
            // Else, free its slot with a "null" value.
            controllers.set(index, null);
        }

        System.out.println("Controller now disconnected: " + controller);

        // Make sure to free any SDL resources
        controller.sdl.close();
    }

    // Returns true when the given controller instance id is already connected.
    private boolean controllerAlreadyConnected(int instanceId) {
        for (LocalController controller : controllers) {
            if (controller.instanceId == instanceId) {
                return true;
            }
        }

        return false;
    }

    /// Returns the last polled state of the controllers. Empty when polling hasn't been done once.
    ///
    /// @return the last polled state of the controllers
    public State getLastState() {
        return lastState;
    }

    /// All connected controllers with all their currently pressed keys and joystick positions.
    ///
    /// @param controllers a sparse array with all the connected controllers; some slots may be `null`,
    ///                    indicating free slots
    public record State(
            ControllerState[] controllers
    ) {
        /// The empty state with zero connected controllers.
        public static State EMPTY = new State(new ControllerState[0]);

        /// Returns the controller at the given index if it exists. Out-of-bounds indices return an empty optional.
        ///
        /// @param index the index of the controller to fetch, must be >= 0
        /// @return an optional containing the controller at the given index, or an empty optional when there isn't one
        public Optional<ControllerState> getController(int index) {
            assert index >= 0; // A negative index is a programmer error if you ask me.

            if (index >= controllers.length) {
                // Out of bounds; return an empty optional.
                return Optional.empty();
            }

            return Optional.ofNullable(controllers[index]);
        }

        @Override
        public String toString() {
            return Arrays.toString(controllers);
        }
    }

    // A controller with its Joystick instance ID because SDL_GameController doesn't store that?!
    private record LocalController(SDL_GameController sdl, int instanceId) {
        @Override
        public String toString() {
            return "LocalController[" + sdl.name() + ", " + instanceId + "]";
        }
    }
}
