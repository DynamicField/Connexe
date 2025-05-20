package fr.connexe.ui.game;

import org.libsdl.SDL;
import org.libsdl.SDL_Error;
import org.libsdl.SDL_GameController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ControllerHub {
    private static boolean sdlInitialized;
    private final List<LocalController> controllers = new ArrayList<>();
    private volatile State lastState = State.EMPTY;

    private static void initializeSDL() throws InputSystemException {
        if (!sdlInitialized) {
            SDL.SDL_SetHint("SDL_JOYSTICK_ALLOW_BACKGROUND_EVENTS", "1");
            SDL.SDL_SetHint("SDL_ACCELEROMETER_AS_JOYSTICK", "0");
            SDL.SDL_SetHint("SDL_MAC_BACKGROUND_APP", "1");
            SDL.SDL_SetHint("SDL_XINPUT_ENABLED", "1");
            // rawinput is broken for some reason with DPAD buttons
            SDL.SDL_SetHint("SDL_JOYSTICK_RAWINPUT", "0");

            // Initialize SDL
            sdlInitialized = true;
            if (SDL.SDL_Init(SDL.SDL_INIT_GAMECONTROLLER | SDL.SDL_INIT_JOYSTICK | SDL.SDL_INIT_EVENTS) != 0) {
                throw new InputSystemException("Failed to initalize SDL.", new SDL_Error());
            }

            try {
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

    public ControllerHub() throws InputSystemException {
        try {
            initializeSDL();
        } catch (InputSystemException e) {
            throw e;
        } catch (Throwable t) {
            throw new InputSystemException("Failed to initialize the controller hub", t);
        }
    }

    public State poll() {
        SDL.SDL_PumpEvents();
        SDL.SDL_GameControllerUpdate();

        int n = SDL.SDL_NumJoysticks();

        for (int i = controllers.size() - 1; i >= 0; i--) {
            LocalController controller = controllers.get(i);
            if (!controller.sdl.getAttached()) {
                removeController(controller);
            }
        }

        for (int i = 0; i < n; i++) {
            if (SDL.SDL_IsGameController(i)) {
                int instanceId = SDL.SDL_JoystickGetDeviceInstanceID(i);
                if (controllerAlreadyConnected(instanceId)) {
                    continue;
                }

                // Connect the new controller
                SDL_GameController sdlController = SDL_GameController.GameControllerOpen(i);
                addController(new LocalController(sdlController, instanceId));
            }
        }

        // Update the state of the controllers
        ControllerState[] snapshots = new ControllerState[controllers.size()];
        for (int i = 0; i < controllers.size(); i++) {
            LocalController controller = controllers.get(i);
            if (controller == null) {
                continue;
            }

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

        lastState = new State(snapshots);
        return lastState;
    }

    private void addController(LocalController controller) {
        for (int i = 0; i < controllers.size(); i++) {
            if (controllers.get(i) == null) {
                controllers.set(i, controller);
                return;
            }
        }

        controllers.add(controller);

        System.out.println("Controller now connected: " + controller);
    }

    private void removeController(LocalController controller) {
        boolean removed = controllers.remove(controller);
        assert removed;

        if (!controllers.isEmpty() && controllers.getLast() == null) {
            controllers.removeLast();
        }

        System.out.println("Controller now disconnected: " + controller);

        controller.sdl.close();
    }

    private boolean controllerAlreadyConnected(int instanceId) {
        for (LocalController controller : controllers) {
            if (controller.instanceId == instanceId) {
                return true;
            }
        }

        return false;
    }

    public State getLastState() {
        return lastState;
    }

    public record State(
            ControllerState[] controllers
    ) {
        public static State EMPTY = new State(new ControllerState[0]);

        public Optional<ControllerState> getController(int index) {
            assert index >= 0;

            if (index >= controllers.length) {
                return Optional.empty();
            }

            return Optional.ofNullable(controllers[index]);
        }

        @Override
        public String toString() {
            return Arrays.toString(controllers);
        }
    }

    private record LocalController(SDL_GameController sdl, int instanceId) {
        @Override
        public String toString() {
            return "LocalController[" + sdl.name() + ", " + instanceId + "]";
        }
    }
}
