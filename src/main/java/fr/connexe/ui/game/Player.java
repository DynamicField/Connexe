package fr.connexe.ui.game;

import fr.connexe.algo.GraphMaze;
import fr.connexe.algo.Point;
import fr.connexe.ui.game.input.ControllerHub;
import fr.connexe.ui.game.input.PlayerInputSource;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.function.Function;

/// A player in a [game session][GameSession], moving around to maze to reach the end.
///
/// This class works by using a state machine to handle all different states the player might be in.
public class Player {
    // --- Simulation state ---
    private final PlayerInputSource inputSource; // The input source of the player
    private final Random rng; // Some randomness for the stun animation
    private final PlayerProfile profile; // The profile used to create this player
    private final int index; // The index in the players list

    // The position of the player in the maze, in the maze coordinate space.
    // It can be between two cells [example: (0.5, 0)] to indicate movement.
    private Vector2 localPos;
    // The offset applied to the player's avatar, in the maze coordinate space.
    private Vector2 offset;
    // The cell we're currently standing on, updated after moving from one cell to another has completed
    // successfully.
    // This is a "Point" since it indicates integer coordinates in the maze, not floating point.
    private Point currentCell;

    // Timer tracking for how much time the player inputs a direction that will
    // just make them hurt a wall.
    private double headingIntoWallTime = 0.0;
    // True when the player did some move input this frame.
    private boolean receivedMoveInputThisFrame = false;
    // True when the player did some move input LAST frame.
    private boolean receivedMoveInputLastFrame = false;

    // Duration of the current pulse effect, when in furtivity game mode.
    private double pulseProgress = 1; // 1 by default so we recalculate on first frame

    // The current state of the player (Idle, Moving, Colliding, ReachedEnd)
    private State state;

    // The number of moves done by the player
    private int movesDone;
    // The time (Epoch nanos) at which the player reached the end; -1 if not reached yet.
    private long reachedEndAt = -1;

    // --- JavaFX state ---
    private final Circle icon; // The circular icon of the player; quite minimalistic to be honest
    private final @Nullable Circle pulse; // The pulse effect of the player for the furtivity mode; null if not furtive

    /// Creates a new player based off a player profile and its index in the player list.
    ///
    /// @param profile  The profile of the player.
    /// @param startPos The starting position of the player in the maze
    /// @param gameMode The game mode of the game session this player participates in
    /// @param index    The index of the player in the players list.
    public Player(PlayerProfile profile, Point startPos, GameMode gameMode, int index) {
        // Initialize every field of this class with default values, and some data given by the profile.
        this.profile = profile;
        this.index = index;
        this.inputSource = profile.getInputSource();
        this.rng = new Random();
        this.localPos = Vector2.fromPoint(startPos);
        this.offset = new Vector2(0, 0);
        this.currentCell = startPos;
        this.icon = new Circle();
        this.state = new State.Idle();
        this.movesDone = 0;

        // Initialize the pulse circle for furtivity mode only.
        if (gameMode == GameMode.FURTIVITY) {
            this.pulse = new Circle();
            pulse.setFill(Color.TRANSPARENT);
            pulse.setStroke(profile.getColor());
            pulse.setStrokeWidth(2);
        } else {
            this.pulse = null;
        }

        // Configure the icon's color with a slightly transparent color so we can see multiple players at once.
        this.icon.setFill(profile.getColor().deriveColor(0, 1, 1, 0.8));
    }

    /// Places JavaFX objects of this player into the game overlay.
    ///
    /// @param gameOverlay The maze overlay to add the player to.
    public void initializeFX(Pane gameOverlay) {
        if (pulse != null) {
            // If the player has a pulse, we need to add it to the overlay.
            gameOverlay.getChildren().add(pulse);
        }
        // Add the icon to the overlay.
        gameOverlay.getChildren().add(icon);
    }

    /// Runs frame update logic for this player.
    /// Updates the current state and runs any animation (moving, stunned and all that),
    ///
    /// @param maze          The maze to use for collision checking.
    /// @param deltaTime     The time since the last frame, in seconds.
    /// @param lastTimestamp The last timestamp in nanoseconds.
    /// @param controllerHub The controller hub to use for haptic feedback.
    public void update(GraphMaze maze, double deltaTime, long lastTimestamp, @Nullable ControllerHub controllerHub) {
        final double MOV_DURATION = 0.15f; // Duration of the moving animation, in seconds

        // Do stuff based on the current state of the player.
        switch (state) {
            case State.Idle _ -> {
                // Idle, nothing to do
            }
            case State.Moving mov -> {
                // Moving from one cell to another.

                // Update the progress % of the current moving animation.
                // beware: might skip collision checking if frame lasts too long
                mov.progress += deltaTime / MOV_DURATION;
                if (mov.progress >= 1) {
                    // Movement done, update both positions (localPos for avatar and currentCell for maze)
                    localPos = Vector2.fromPoint(mov.target);
                    currentCell = mov.target;

                    // That's actually a successful movement: increase the number of moves done by one
                    movesDone++;

                    if (maze.toVertexId(currentCell) == maze.getEnd()) {
                        // We've reached the end!!! Move to the "ReachedEnd" state and save the timestamp at which
                        // we've done it!
                        state = new State.ReachedEnd();
                        reachedEndAt = lastTimestamp;
                    } else {
                        // Nope, not the end yet. Update the local position to the new cell and go back to idle.
                        state = new State.Idle();
                    }
                } else {
                    // Continue moving to the target cell.

                    // Apply a smooth "ease-out" effect to the current t (which is in [0, 1])
                    // This will simulate a quick "leap" of some sort, with a smooth landing.
                    double easedProgress = GameMath.easeExp(mov.progress, -1.5);

                    // Check for collisions when we're colliding with the wall.
                    // I'm too lazy to do sphere-line overlap checking, so let's just use this hacky
                    // "ehh the sphere is probably over the line depending on the progress" check.
                    boolean checkCollisions = easedProgress > 0.4 && easedProgress < 0.8;
                    if (checkCollisions && !maze.isConnected(
                            maze.toVertexId(currentCell),
                            maze.toVertexId(mov.target)
                    )) {
                        // We've hurt the wall!! Move to the "Colliding" state.
                        state = new State.Colliding(localPos, mov.source);
                    } else {
                        // Continue moving using a simple linear interpolation.
                        localPos = GameMath.lerp(mov.source, Vector2.fromPoint(mov.target), easedProgress);
                    }
                }
            }
            case State.Colliding col -> {
                // Decrease the stun timer.
                col.stunDurationLeft -= deltaTime;

                if (col.stunDurationLeft > 0) {
                    // We're still stunned! Apply some random offset to our avatar.
                    offset = new Vector2(
                            rng.nextFloat(-0.05f, 0.05f),
                            rng.nextFloat(-0.05f, 0.05f)
                    );
                } else {
                    // Not stunned; now let's move back to the previous cell.

                    // Reset the offset we've set previously while being stunned.
                    offset = new Vector2(0, 0);

                    // Continue through a simple move animation with a lerp.
                    col.progress += deltaTime / MOV_DURATION;
                    if (col.progress >= 1) {
                        // Movement complete, back to idle.
                        localPos = col.target;
                        headingIntoWallTime = 0;
                        state = new State.Idle();
                    } else {
                        // Continue moving.
                        localPos = GameMath.lerp(col.source, col.target, col.progress);
                    }
                }
            }
            case State.ReachedEnd _ -> {
                // Nothing to do either
            }
        }

        // Update the pulse animation if we're in furtivity mode.
        if (pulse != null) {
            if (hasReachedEnd()) {
                // Stop pulsing when we reach the end.
                pulseProgress = 0;
            } else {
                // Calculate the Manhattan distance to the end cell. We'll use this to adjust the pulse speed.
                Point end = maze.getEndPoint();
                int dist = Math.abs(end.x() - currentCell.x()) + Math.abs(end.y() - currentCell.y());

                if (dist > 0) {
                    // The further we are from the end, the longer the pulse lasts.
                    // Down to 0.12 when adjacent to the end (for extra feels).
                    double fullPulseDuration = dist == 1 ? 0.12 : Math.min(3, dist * 0.15);

                    // Update the pulse progress, modulo 1 to loop back to 0.
                    double newProgress = pulseProgress + deltaTime / fullPulseDuration;
                    pulseProgress = newProgress % 1;

                    // See if we can vibrate the controller after a complete pulse cycle.
                    if (newProgress > pulseProgress
                            && inputSource instanceof PlayerInputSource.Controller(int controllerIndex)
                            && controllerHub != null) {
                        // We completed a pulse cycle, let's do haptic feedback!

                        // Increase the intensity if we're near the end goal.
                        double intensity = GameMath.lerp(1.0, 0.4, (dist - 1) / 4.0);

                        // Make the controller go BRRRR
                        controllerHub.vibrateController(controllerIndex, intensity, intensity, 65);
                    }
                }
            }
        }

        // Update the receivedMoveInputThisFrame/receivedMoveInputLastFrame state.
        if (receivedMoveInputThisFrame) {
            // Next frame will have receivedMoveInputLastFrame set to true.
            receivedMoveInputLastFrame = true;
            receivedMoveInputThisFrame = false;
        } else {
            // Continuous movement input stopped: reset the wall time.
            receivedMoveInputLastFrame = false;
            headingIntoWallTime = 0;
        }
    }

    /// Updates the JavaFX avatar of this player. Must be called every frame after updating all entities.
    ///
    /// @param toFXCoordinates The function to convert world coordinates to JavaFX coordinates.
    /// @param cellHeight      The height of the maze cell in JavaFX coordinates.
    /// @param cellWidth       The width of the maze cell in JavaFX coordinates.
    public void render(Function<Vector2, Vector2> toFXCoordinates, double cellHeight, double cellWidth) {
        // Resize the icon based on the dimensions of the maze.
        // Make sure it fits inside the cell without touching the borders.
        icon.setRadius(Math.max(2, Math.min(cellHeight, cellWidth) * 0.45 - 3));

        // Take our world coordinates and convert them to JavaFX coordinates.
        Vector2 fxCoordinates = toFXCoordinates.apply(getWorldPosition());
        Vector2 iconCoordinates = fxCoordinates.subtract(
                new Vector2(icon.getRadius(), icon.getRadius())
        );

        // Apply the position changes to the JavaFX icon.
        icon.relocate(iconCoordinates.x(), iconCoordinates.y());

        // Update the pulsing circle for furtivity mode.
        if (pulse != null) {
            if (hasReachedEnd()) {
                // Stop pulsing when we reach the end.
                pulse.setOpacity(0);
                return;
            }

            // Smooth out the 0 to 1 progress time for a nice feel.
            double smoothProgress = GameMath.easeExp(pulseProgress, -1.5);

            // Increase the radius the further we're in the pulse animation.
            double pulseRadius = icon.getRadius() * GameMath.lerp(0.9, 1.4, smoothProgress);
            // Decrease the opacity down to zero in the [0.6, 1] range of the animation.
            double pulseOpacity = GameMath.lerp(1, 0, (smoothProgress - 0.6) / 0.4);

            // Calculate the coordinates to center the circle correctly, including the stroke width!!!
            Vector2 pulseCoordinates = fxCoordinates.subtract(
                    new Vector2(pulseRadius + icon.getStrokeWidth(), pulseRadius + icon.getStrokeWidth())
            );

            // Add a hint of blur near the end of the animation
            double blurRadius = GameMath.lerp(0, 10, (smoothProgress - 0.6) / 0.4);

            // Configure the JavaFX circle.
            pulse.setEffect(new GaussianBlur(blurRadius));
            pulse.setRadius(pulseRadius);
            pulse.setOpacity(pulseOpacity);
            pulse.relocate(pulseCoordinates.x(), pulseCoordinates.y());
        }
    }

    /// Accepts a move input from the player's controller or keyboard.
    ///
    /// Only accepts inputs when the player is idle.
    ///
    /// Fails when the offsets are invalid (not 1 or -1).
    ///
    /// @param maze      the maze the player plays in
    /// @param deltaTime the delta time of this frame
    /// @param offsetX   the offset in the X axis (-1 or 1)
    /// @param offsetY   the offset in the Y axis (-1 or 1)
    public void acceptMoveInput(GraphMaze maze, double deltaTime, int offsetX, int offsetY) {
        // That's a move input registered, even if it's not successful.
        receivedMoveInputThisFrame = true;

        if (!isIdle()) {
            // We aren't idle, so we can't move!
            return;
        }

        if (Math.abs(offsetX) != 1 && Math.abs(offsetY) != 1) {
            // Invalid offset; we can only move in one direction at a time.
            System.err.println("Invalid player offset: " + offsetX + ", " + offsetY);
            return;
        }

        // Find the target cell to move to.
        Point cell = currentCell.add(offsetX, offsetY);

        // Prevent the player from accidentally hitting a wall while they're continuously moving,
        // giving them time to change their key/joystick/button/whatever.
        // After a short while, if they keep wanting to hit that wall, they will eventually!
        if (!maze.isConnected(maze.toVertexId(currentCell), maze.toVertexId(cell))
                && receivedMoveInputLastFrame) {
            headingIntoWallTime += deltaTime;

            if (headingIntoWallTime < 0.15) {
                // Grace period where the player won't hit the wall; don't move!
                return;
            }
        } else {
            // Reset the wall timer.
            headingIntoWallTime = 0;
        }

        // Begin the "moving" animation.
        state = new State.Moving(localPos, cell);
    }

    /// Returns the player profile used to create this player.
    ///
    /// @return The player profile used to create this player.
    public PlayerProfile getProfile() {
        return profile;
    }

    /// Returns the index of this player in the player list.
    ///
    /// @return The index of this player in the player list.
    public int getIndex() {
        return index;
    }

    /// Returns true when this player is in the "idle" state.
    ///
    /// @return True if this player is in the "idle" state.
    public boolean isIdle() {
        return state instanceof State.Idle;
    }

    /// Returns true when this player has reached the end of the maze.
    ///
    /// @return True if this player has reached the end of the maze.
    public boolean hasReachedEnd() {
        return state instanceof State.ReachedEnd;
    }

    /// Returns the Epoch timestamp at which the user reached the end of the maze. Returns -1 if they didn't
    /// reach the end yet.
    ///
    /// @return The Epoch timestamp at which the user reached the end of the maze, or -1 if they didn't reach it yet.
    public long getReachedEndAt() {
        return reachedEndAt;
    }

    /// Get the world position of this player (localPos + offset), in the maze coordinate space.
    ///
    /// @return The world position of this player.
    public Vector2 getWorldPosition() {
        return localPos.add(offset);
    }

    /// Returns the input source of this player: the input device (keyboard/controller) the player uses to move.
    ///
    /// @return The input source of this player.
    public PlayerInputSource getInputSource() {
        return inputSource;
    }

    /// Returns the total number of successful moves done since the beginning of the game.
    ///
    /// @return The total number of moves
    public int getMovesDone() {
        return movesDone;
    }

    // The current state of a player. (Wow. That's a mouthful of keywords.)
    private static abstract sealed class State {
        // Not doing anything; not at the end of the maze.
        static final class Idle extends State {}

        // Moving to another cell.
        static final class Moving extends State {
            public Moving(Vector2 source, Point target) {
                this.source = source;
                this.target = target;
            }

            Vector2 source;
            Point target;
            double progress = 0;
        }

        // Hurt a wall; recovering by being stunned and heading back to the previous location.
        static final class Colliding extends State {
            public Colliding(Vector2 source, Vector2 target) {
                this.source = source;
                this.target = target;
            }

            Vector2 source;
            Vector2 target;
            double stunDurationLeft = 0.33;
            double progress = 0;
        }

        // Reached the end of the maze; can't move.
        static final class ReachedEnd extends State {}
    }
}
