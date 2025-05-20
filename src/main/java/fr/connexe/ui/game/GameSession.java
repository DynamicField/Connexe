package fr.connexe.ui.game;

import fr.connexe.algo.GraphMaze;
import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public class GameSession {
    private final GraphMaze maze;
    private final KeyboardHub keyboardHub;
    private final @Nullable ControllerHub controllerHub;
    private final AnimationTimer tickTimer;

    private final Pane overlay;

    private long lastTimestamp = 0; // nanoseconds
    private @Nullable Pane parent; // null when not initialized yet

    private double cellWidth; // Width of a cell in the JavaFX coordinate space
    private double cellHeight; // Height of a cell in the JavaFX coordinate space

    private final List<Player> players = new ArrayList<>();

    private static final float JOYSTICK_THRESHOLD = 0.75f;

    public GameSession(GraphMaze maze, KeyboardHub keyboardHub, @Nullable ControllerHub controllerHub) {
        this.maze = maze;
        this.keyboardHub = Objects.requireNonNull(keyboardHub); // Make sure it isn't null.
        this.controllerHub = controllerHub;

        this.overlay = new Pane();
        overlay.setVisible(false); // Hidden until the first tick happens.

        addPlayer(new Player(new PlayerInputSource.KeyboardArrows()));

        tickTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTimestamp == 0) {
                    lastTimestamp = now;
                } else {
                    long deltaTime = now - lastTimestamp;
                    lastTimestamp = now;
                    tick((float) deltaTime / 1_000_000_000.0f);

                    overlay.setVisible(true); // Show the pane when the first tick happens.
                }
            }
        };
    }

    public Pane deploy(Pane overlayParent) {
        overlayParent.getChildren().add(overlay);
        tickTimer.start();

        this.parent = overlayParent;
        return overlay;
    }

    public void undeploy() {
        if (parent == null) {
            return;
        }

        if (overlay.getParent() != null) {
            parent.getChildren().remove(overlay);
        }
        tickTimer.stop();
        parent = null;
    }

    private void addPlayer(Player player) {
        players.add(player);
        overlay.getChildren().add(player.getIcon());
    }

    private void removePlayer(Player player) {
        players.remove(player);
        overlay.getChildren().remove(player.getIcon());
    }

    private void tick(float deltaTime) {
        if (overlay.getParent() == null) {
            undeploy();
            return;
        }

        overlay.requestFocus();

        // First, update the cell width and height based on the current size of the pane.
        double paneWidth = overlay.getWidth();
        double paneHeight = overlay.getHeight();
        cellWidth = paneWidth / maze.getWidth();
        cellHeight = paneHeight / maze.getHeight();

        ControllerHub.State controllers = ControllerHub.State.EMPTY;
        if (controllerHub != null) {
            controllers = controllerHub.poll();
        }

        EnumSet<KeyCode> pressedKeys = keyboardHub.getPressedKeys();

        for (Player p : players) {
            switch (p.getInputSource()) {
                case PlayerInputSource.Controller(int slot) -> controllers.getController(slot).ifPresent(s -> {
                    if (s.dpadRightPressed() || s.leftJoystickX() > JOYSTICK_THRESHOLD) {
                        p.acceptMoveInput(1, 0);
                    } else if (s.dpadLeftPressed() || s.leftJoystickX() < -JOYSTICK_THRESHOLD) {
                        p.acceptMoveInput(-1, 0);
                    } else if (s.dpadUpPressed() || s.leftJoystickY() < -JOYSTICK_THRESHOLD) {
                        p.acceptMoveInput(0, -1);
                    } else if (s.dpadDownPressed() || s.leftJoystickY() > JOYSTICK_THRESHOLD) {
                        p.acceptMoveInput(0, 1);
                    }
                });
                case PlayerInputSource.KeyboardZQSD _ -> {
                    if (pressedKeys.contains(KeyCode.Z)) {
                        p.acceptMoveInput(0, -1);
                    } else if (pressedKeys.contains(KeyCode.S)) {
                        p.acceptMoveInput(0, 1);
                    } else if (pressedKeys.contains(KeyCode.Q)) {
                        p.acceptMoveInput(-1, 0);
                    } else if (pressedKeys.contains(KeyCode.D)) {
                        p.acceptMoveInput(1, 0);
                    }
                }
                case PlayerInputSource.KeyboardArrows _ -> {
                    if (pressedKeys.contains(KeyCode.UP)) {
                        p.acceptMoveInput(0, -1);
                    } else if (pressedKeys.contains(KeyCode.DOWN)) {
                        p.acceptMoveInput(0, 1);
                    } else if (pressedKeys.contains(KeyCode.LEFT)) {
                        p.acceptMoveInput(-1, 0);
                    } else if (pressedKeys.contains(KeyCode.RIGHT)) {
                        p.acceptMoveInput(1, 0);
                    }
                }
                default -> {}
            }
        }

        for (Player p : players) {
            p.update(maze, deltaTime);
        }

        for (Player p : players) {
            // Resize the icon based on the dimensions of the maze.
            // Make sure it fits inside the cell without touching the borders.
            p.getIcon().setRadius(Math.max(2, Math.min(cellHeight, cellWidth) * 0.45 - 3));

            // Take our world coordinates and convert them to JavaFX coordinates.
            Vector2 fxCoordinates = toFXCoordinates(p.getWorldPosition());
            fxCoordinates = fxCoordinates.subtract(
                    new Vector2(
                            p.getIcon().getRadius(),
                            p.getIcon().getRadius()
                    )
            );
            p.getIcon().relocate(fxCoordinates.x(), fxCoordinates.y());
        }
    }

    private Vector2 toFXCoordinates(Vector2 v) {
        return new Vector2(
                cellWidth * v.x() + cellWidth / 2,
                cellHeight * v.y() + cellHeight / 2
        );
    }
}
