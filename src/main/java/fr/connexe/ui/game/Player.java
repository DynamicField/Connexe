package fr.connexe.ui.game;

import fr.connexe.algo.GraphMaze;
import fr.connexe.algo.Point;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.Random;

public class Player {
    // Simulation state
    private final PlayerInputSource inputSource;
    private final Random rng;
    private Vector2 localPos;
    private Vector2 offset;
    private Point currentCell;
    private State state;
    private int movesDone;

    // JavaFX state
    private final Circle icon;

    public Player(PlayerInputSource inputSource) {
        this.inputSource = inputSource;
        this.rng = new Random();
        this.localPos = new Vector2(0, 0);
        this.offset = new Vector2(0, 0);
        this.currentCell = new Point(0, 0);
        this.icon = new Circle(15);
        this.state = new State.Idle();
        this.movesDone = 0;
        this.icon.setFill(Color.DARKKHAKI);
    }

    public void update(GraphMaze maze, double deltaTime) {
        final double MOV_DURATION = 0.15f;

        switch (state) {
            case State.Idle _ -> {
            }
            case State.Moving mov -> {
                // beware: might skip collision checking if frame lasts too long
                mov.progress += deltaTime / MOV_DURATION;
                if (mov.progress >= 1) {
                    localPos = Vector2.fromPoint(mov.target);
                    currentCell = mov.target;
                    movesDone++;
                    state = new State.Idle();
                } else {
                    double easedProgress = GameMath.easeExp(mov.progress, -1.5);

                    boolean checkCollisions = easedProgress > 0.5 && easedProgress < 0.8;
                    if (checkCollisions && !maze.isConnected(
                            maze.toVertexId(currentCell),
                            maze.toVertexId(mov.target)
                    )) {
                        state = new State.Colliding(localPos, mov.source);
                    } else {
                        localPos = GameMath.lerp(mov.source, Vector2.fromPoint(mov.target), easedProgress);
                    }
                }
            }
            case State.Colliding col -> {
                col.stunDurationLeft -= deltaTime;

                if (col.stunDurationLeft > 0) {
                    offset = new Vector2(
                            rng.nextFloat(-0.05f, 0.05f),
                            rng.nextFloat(-0.05f, 0.05f)
                    );
                } else {
                    offset = new Vector2(0, 0);

                    col.progress += deltaTime / MOV_DURATION;
                    if (col.progress >= 1) {
                        localPos = col.target;
                        state = new State.Idle();
                    } else {
                        localPos = GameMath.lerp(col.source, col.target, col.progress);
                    }
                }
            }
        }
    }

    public void acceptMoveInput(int offsetX, int offsetY) {
        if (!(state instanceof State.Idle)) {
            return;
        }

        if (Math.abs(offsetX) != 1 && Math.abs(offsetY) != 1) {
            System.err.println("Invalid player offset: " + offsetX + ", " + offsetY);
            return;
        }

        state = new State.Moving(localPos, currentCell.add(offsetX, offsetY));
    }

    public boolean isIdle() {
        return state instanceof State.Idle;
    }

    public Vector2 getOffset() {
        return offset;
    }

    public Vector2 getWorldPosition() {
        return localPos.add(offset);
    }

    public Circle getIcon() {
        return icon;
    }

    public Point getCurrentCell() {
        return currentCell;
    }

    public PlayerInputSource getInputSource() {
        return inputSource;
    }

    public int getMovesDone() {
        return movesDone;
    }

    // Wow. That's a mouthful of keywords.
    private static abstract sealed class State {
        static final class Idle extends State {}

        static final class Moving extends State {
            public Moving(Vector2 source, Point target) {
                this.source = source;
                this.target = target;
            }

            Vector2 source;
            Point target;
            double progress = 0;
        }

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
    }
}
