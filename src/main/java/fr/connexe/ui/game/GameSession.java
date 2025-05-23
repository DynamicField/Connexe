package fr.connexe.ui.game;

import fr.connexe.ConnexeApp;
import fr.connexe.algo.ArrayMaze;
import fr.connexe.algo.GraphMaze;
import fr.connexe.algo.MazeSolver;
import fr.connexe.algo.Point;
import fr.connexe.ui.game.hud.EfficiencyHUDController;
import fr.connexe.ui.game.hud.FurtivityHUDController;
import fr.connexe.ui.game.hud.HUDController;
import fr.connexe.ui.game.hud.SwiftnessHUDController;
import fr.connexe.ui.game.input.ControllerHub;
import fr.connexe.ui.game.input.KeyboardHub;
import fr.connexe.ui.game.input.PlayerInputSource;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

/// An ongoing game in arcade mode; handles player input, game state, and rendering.
///
/// A [GameSession] can be started with a [GameStartConfig] containing all players and the chosen game mode, as well as
/// the maze to play with, and input hubs (keyboard/controller).
///
/// ## UI Structure
///
/// The game session is composed of three UI elements:
/// - the **game overlay**: the **main UI element** where players are displayed on top of the maze
/// - the **game over overlay**: displayed on top of the maze when the game is over
/// - the **HUD**: displayed below the maze, contains info about the game: game mode, leaderboard, etc.
///   (controlled by a [HUDController])
///
/// The UI is automatically created by the [GameSession] using the [#deploy(StackPane, Pane)]
/// method. It requires two parent panes (one for overlays; one for the HUD).
///
/// The [GameSession] can be stopped at any time by using the [#undeploy()] method, which will
/// remove all UI elements and stop the game.
///
/// When the main overlay is detached from its parent, the session is automatically undeployed.
///
/// ## Coordinate spaces
///
/// The [GameSession] uses two coordinate spaces, both work using floating points:
/// - the **maze/world coordinate space**: based off the center of the maze cells; the point at `(x, y)` refers
///   to the **center of the `(x, y)` cell displayed in JavaFX maze**.
/// - the **JavaFX coordinate space**: based off the raw JavaFX coordinates using the `Pane`'s width and height.
///
/// We usually work with the **maze coordinate space**, which allows us to easily place players on the screen.
/// For instance, when a player is at the cell `(1, 2)`, its position is `(1, 2)`!
///
/// ## Input
///
/// The [GameSession] uses two input mechanisms:
/// - the [KeyboardHub] to receive currently pressed keys
/// - the [ControllerHub] to receive controller input (optional)
///
/// Controller input is polled every frame using [ControllerHub#poll()].
///
/// Players move using whichever input source they've chosen using the [PlayerInputSource] class.
///
/// Disconnected controllers are silently ignored.
///
/// ## Game loop
///
/// The game loop is handled by an [AnimationTimer] which calls the `tick` method every frame.
/// We use a variable delta time to run any animations or game logic.
///
/// During each frame, we:
/// - poll input from the keyboard and controller
/// - update the players' positions and animations based on their input
/// - update the JavaFX nodes for each player (even if game is over)
/// - update the HUD
/// - check if the game is over
public class GameSession {
    private final GraphMaze maze; // The maze where the action happens!
    private final KeyboardHub keyboardHub; // To receive keyboard key presses
    private final @Nullable ControllerHub controllerHub; // To receive controller input
    private final Runnable closeGame; // The function called to undeploy the game properly (by the MazeController)
    private final Consumer<ArrayMaze> displayMaze; // The function called to display a modified maze for furtivity mode
    private final AnimationTimer tickTimer; // Timer called every frame to run the tick() function

    private final Pane gameOverlay; // The main game overlay with all players on top of the maze
    private @Nullable Pane gameOverOverlay; // The overlay displayed when the game's over; null when gameOver = false
    private @Nullable StackPane overlayParent; // The parent container of all overlays; null when not initialized yet

    private final Pane hud; // The "HUD" which is really just a JavaFX container below the maze (and below the overlay).
    private final HUDController hudController; // The controller associated with the HUD
    private @Nullable Pane hudParent; // The parent container of the HUD. Usually a VBox.

    private long startTimestamp = 0; // Epoch timestamp of the first frame (after deploy()).
    private long lastTimestamp = 0; // Epoch timestamp of the last frame.

    private double cellWidth; // Width of a cell in the JavaFX coordinate space
    private double cellHeight; // Height of a cell in the JavaFX coordinate space

    private final GameMode gameMode; // The chosen game mode
    private final List<Player> players = new ArrayList<>(); // All players on the game
    private boolean gameOver = false; // True when the game's over and we have a winner!

    private static final float JOYSTICK_THRESHOLD = 0.75f; // Threshold for a joystick axis to be considered pressed

    /// Begins a new game session with the given configuration, maze, and input systems.
    ///
    /// @param config        the game start configuration with all players and chosen game mode
    /// @param maze          the maze players are battling in
    /// @param keyboardHub   the keyboard input hub to receive keyboard key presses
    /// @param controllerHub the controller input hub to receive controller input (optional)
    /// @param closeGame     the function called when the user wants to close the game (should undeploy the session)
    /// @param displayMaze   the function called to temporarily display a modified maze (for furtivity mode)
    /// @throws IncompatibleMazeException when the maze's topology is incompatible with the given game mode
    ///                                   (no path from A to B)
    public GameSession(GameStartConfig config,
                       GraphMaze maze,
                       KeyboardHub keyboardHub,
                       @Nullable ControllerHub controllerHub,
                       Runnable closeGame,
                       Consumer<ArrayMaze> displayMaze) throws IncompatibleMazeException {
        // Initialize all our dependencies (input & maze)
        this.keyboardHub = Objects.requireNonNull(keyboardHub); // Make sure it isn't null.
        this.controllerHub = controllerHub;
        this.closeGame = closeGame;
        this.displayMaze = displayMaze;

        // Create an empty game overlay, we'll fill it with players later on.
        this.gameOverlay = new Pane();
        // Set its preferred width and height to 0, so it doesn't extend the size of the parent pane
        // but still takes over the entire maze (due to how StackPane works).
        gameOverlay.setPrefHeight(0);
        gameOverlay.setPrefWidth(0);

        // Load the HUD and setup its controller
        try {
            FXMLLoader loader = createHUDLoader(config.mode());
            this.hud = loader.load();
            this.hudController = loader.getController();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Configure the game mode
        this.gameMode = config.mode();

        // Configure the maze, with special considerations for furtivity mode.
        if (gameMode == GameMode.FURTIVITY) {
            // In furtivity mode, we need to put a random endpoint for the maze.
            GraphMaze furtiveMaze = maze.clone();
            furtiveMaze.setEnd(pickRandomEndpoint(furtiveMaze));

            this.maze = furtiveMaze;
        } else {
            // Set the maze with no particular change in this game mode.
            this.maze = maze;

            // ...But make sure there's a path from start to end!
            if (MazeSolver.prepDFS(maze).isEmpty()) {
                // No path from start to end; throw an exception.
                throw new IncompatibleMazeException("Le labyrinthe n'a pas de chemin entre le début et la fin !");
            }
        }

        // Add all players to the game overlay and to the player list.
        Point startPos = maze.getStartPoint();
        for (PlayerProfile profile : config.players()) {
            // Add a player into the list, and register its avatar in the gameOverlay.
            addPlayer(new Player(profile, startPos, gameMode, players.size()));
        }

        // Create the tick timer.
        this.tickTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Calculate the delta time and update the last time stamp.
                long deltaTime = now - lastTimestamp;
                lastTimestamp = now;

                // Tick now, and convert nanoseconds to seconds.
                tick((float) deltaTime / 1_000_000_000.0f);
            }
        };
    }

    // Makes the FXMLLoader for the HUD, with the right controller.
    private static FXMLLoader createHUDLoader(GameMode mode) {
        return new FXMLLoader(ConnexeApp.class.getResource("arcade-hud.fxml"), null, null,
                _ -> switch (mode) {
                    case EFFICIENCY -> new EfficiencyHUDController();
                    case SWIFTNESS -> new SwiftnessHUDController();
                    case FURTIVITY -> new FurtivityHUDController();
                    default -> throw new RuntimeException("Not implemented");
                });
    }

    // Picks a random cell accessible from the start point of the maze, for the furtivity game mode.
    //
    // Prioritizes vertex further away from the start cell by using weighted probabilities.
    //
    // Throws an IncompatibleMazeException when the cell is surrounded by walls.
    private static int pickRandomEndpoint(GraphMaze maze) throws IncompatibleMazeException {
        int start = maze.getStart();
        assert start != -1 : "Maze doesn't have a start point!";

        // A potential end vertex with its weight (= probability of being chosen)
        // Vertices close to the start point have a low weight.
        record Vertex(int id, double weight) {}

        // --- Do a BFS search for all vertices starting from the start cell ---
        // The BFS will give us the shortest distance from the start cell to all other cells.
        // Using this, we can calculate the weights of all vertices, and of course see which ones
        // are accessible.
        var vertices = new ArrayList<Vertex>(); // Found vertices, except the start one
        var enqueued = new boolean[maze.getNumCells()]; // True when vertex i has been in inserted in the queue once
        var dist = new int[maze.getNumCells()]; // Distance from the start vertex
        double weightSum = 0.0; // Sum of all weights

        // Queue for BFS traversal. Starts with... the start vertex! What a surprise!
        var queue = new ArrayDeque<Integer>();
        queue.offerLast(start);

        // Begin BFS traversal loop
        while (!queue.isEmpty()) {
            // Grab the first vertex in the queue, and add it to the list if it's not the start vertex.
            int cur = queue.pollFirst();
            if (cur != start) {
                // Give lower weights to very near vertices
                // dist=1 -> 0.2
                // dist>=9 -> 1.0
                double weight = GameMath.lerp(0.2, 1.0, (dist[cur] - 1) / 8.0);

                // Add it to the list and increase the total weight sum.
                vertices.add(new Vertex(cur, weight));
                weightSum += weight;
            }

            // Look at all adjacent vertices to complete the BFS search.
            for (int adj : maze.getAdjacentVertices(cur)) {
                if (!enqueued[adj]) {
                    // We didn't know about this one! Put it in the queue and "increment" the distance.
                    queue.offerLast(adj);
                    dist[adj] = dist[cur] + 1;
                    enqueued[adj] = true;
                }
            }
        }

        if (vertices.isEmpty()) {
            // No vertices found; the maze is probably surrounded by walls.
            throw new IncompatibleMazeException("La case de départ est entièrement entourée de murs, " +
                    "impossible d'être furtif dans ces conditions !");
        }

        // Pick a random number in [0, weightSum].
        var rng = new Random();
        double picked = rng.nextDouble(weightSum);

        // Run a classic linear probability scan that accumulates weights
        // to find the chosen vertex.
        // Ultimately, prefixSum will be equal to weightSum, so this loop always
        // returns a chosen vertex... Unless floating point stuff happens!
        double prefixSum = 0.0;
        for (Vertex v : vertices) {
            prefixSum += v.weight;

            // The vertex is picked if picked is in [previous sum; previous sum + vertex weight]
            // Since we're iterating in an increasing manner, we can
            // just check for "(previous sum + vertex weight) >= picked".
            if (prefixSum >= picked) {
                return v.id;
            }
        }

        // Probably a floating point error; return the last vertex.
        return vertices.getLast().id;
    }

    /// Starts the game session in the given panes.
    ///
    /// Adds all necessary UI elements into the two panes. Begins the game loop as soon as possible.
    ///
    /// @param overlayParent the parent pane for the game overlay and game over overlay
    /// @param hudParent     the parent pane for the HUD
    public void deploy(StackPane overlayParent, Pane hudParent) {
        // Configure the game overlay
        this.overlayParent = overlayParent;
        overlayParent.getChildren().add(gameOverlay);

        // Also redeploy the game over overlay if for some reason we're redeploying the game.
        if (gameOverOverlay != null) {
            overlayParent.getChildren().add(gameOverOverlay);
        }

        // Configure the HUD
        this.hudParent = hudParent;
        hudParent.getChildren().add(hud);

        if (gameMode == GameMode.FURTIVITY) {
            // Furtivity mode is a bit special: we need to update the displayed maze with hidden endpoints.
            displayMaze.accept(maze.toArrayMaze(true));
        }

        // Begin the ticking timer now. Run one tick now to setup player positions.
        tick(1 / 60f);
        lastTimestamp = System.nanoTime();
        startTimestamp = lastTimestamp; // Set the start timestamp
        tickTimer.start();
    }

    /// Stops the game session and removes all UI elements from their parents.
    ///
    /// Does nothing when the game isn't deployed already.
    public void undeploy() {
        if (overlayParent == null || hudParent == null) {
            // Not deployed; do nothing.
            return;
        }

        if (gameOverlay.getParent() != null) {
            // Remove the game overlay.
            overlayParent.getChildren().remove(gameOverlay);

            // Remove the game over overlay if it exists.
            if (gameOverOverlay != null) {
                overlayParent.getChildren().remove(gameOverOverlay);
            }
        }

        if (hud.getParent() != null) {
            // Remove the HUD.
            hudParent.getChildren().remove(hud);
        }

        // Stop the timer and free all resources.
        tickTimer.stop();
        overlayParent = null;
        hudParent = null;
        gameOverOverlay = null;
    }

    // Adds a player in the list and registers it in the overlay.
    private void addPlayer(Player player) {
        players.add(player);
        player.initializeFX(gameOverlay);
    }

    // Called every frame to update players and the HUD.
    private void tick(float deltaTime) {
        if (gameOverlay.getParent() == null) {
            // We don't have a parent anymore, so the game is invisible... Consider the game to be done and undeploy.
            undeploy();
            return;
        }

        // Make sure we have the focus so KeyboardHub can fetch all key presses correctly.
        gameOverlay.requestFocus();

        // First, update the cell width and height based on the current size of the pane.
        double paneWidth = gameOverlay.getWidth();
        double paneHeight = gameOverlay.getHeight();
        cellWidth = paneWidth / maze.getWidth();
        cellHeight = paneHeight / maze.getHeight();

        if (!gameOver) {
            // Fetch controller input; default to none
            ControllerHub.State controllers = ControllerHub.State.EMPTY;
            if (controllerHub != null) {
                controllers = controllerHub.poll();
            }

            // Fetch keyboard input
            EnumSet<KeyCode> pressedKeys = keyboardHub.getPressedKeys();

            // Process input for each player
            for (Player p : players) {
                processPlayerInput(p, controllers, pressedKeys, deltaTime);
            }

            // Update animation and current state for each player.
            for (Player p : players) {
                p.update(maze, deltaTime, lastTimestamp, controllerHub);
            }
        }

        // Render (= update JavaFX nodes) for each player, even when the game isn't over so we can handle resizes.
        for (Player p : players) {
            p.render(this::toFXCoordinates, cellHeight, cellWidth);
        }

        if (!gameOver) {
            // Update the HUD (when the game's not over)
            switch (hudController) {
                case EfficiencyHUDController eff -> eff.update(playersSorted());
                case SwiftnessHUDController swi -> swi.update(playersSorted(), startTimestamp);
                case FurtivityHUDController _ -> {/* nothing to do here! */}
            }

            // Check if the game is now over (we have a winner! chicken dinner!).
            checkGameOver();
        }
    }

    // Handle incoming keyboard/controller input for a player depending on their chosen input source.
    private void processPlayerInput(Player p, ControllerHub.State controllers, EnumSet<KeyCode> pressedKeys, double deltaTime) {
        switch (p.getInputSource()) {
            case PlayerInputSource.Controller(int slot) -> controllers.getController(slot).ifPresent(s -> {
                // We have a controller at the specified slot; now see if the buttons are pressed.
                if (s.dpadRightPressed() || s.leftJoystickX() > JOYSTICK_THRESHOLD) {
                    p.acceptMoveInput(maze, deltaTime, 1, 0);
                } else if (s.dpadLeftPressed() || s.leftJoystickX() < -JOYSTICK_THRESHOLD) {
                    p.acceptMoveInput(maze, deltaTime, -1, 0);
                } else if (s.dpadUpPressed() || s.leftJoystickY() < -JOYSTICK_THRESHOLD) {
                    p.acceptMoveInput(maze, deltaTime, 0, -1);
                } else if (s.dpadDownPressed() || s.leftJoystickY() > JOYSTICK_THRESHOLD) {
                    p.acceptMoveInput(maze, deltaTime, 0, 1);
                }
            });
            case PlayerInputSource.KeyboardZQSD _ -> {
                // Handle ZQSD keys
                if (pressedKeys.contains(KeyCode.Z)) {
                    p.acceptMoveInput(maze, deltaTime, 0, -1);
                } else if (pressedKeys.contains(KeyCode.S)) {
                    p.acceptMoveInput(maze, deltaTime, 0, 1);
                } else if (pressedKeys.contains(KeyCode.Q)) {
                    p.acceptMoveInput(maze, deltaTime, -1, 0);
                } else if (pressedKeys.contains(KeyCode.D)) {
                    p.acceptMoveInput(maze, deltaTime, 1, 0);
                }
            }
            case PlayerInputSource.KeyboardArrows _ -> {
                // Handle arrow keys.
                if (pressedKeys.contains(KeyCode.UP)) {
                    p.acceptMoveInput(maze, deltaTime, 0, -1);
                } else if (pressedKeys.contains(KeyCode.DOWN)) {
                    p.acceptMoveInput(maze, deltaTime, 0, 1);
                } else if (pressedKeys.contains(KeyCode.LEFT)) {
                    p.acceptMoveInput(maze, deltaTime, -1, 0);
                } else if (pressedKeys.contains(KeyCode.RIGHT)) {
                    p.acceptMoveInput(maze, deltaTime, 1, 0);
                }
            }
        }
    }

    // Returns the leaderboard of players if it makes sense for the current game mode.
    private List<Player> playersSorted() {
        return switch (gameMode) {
            case EFFICIENCY -> players.stream().sorted(Comparator.comparing(Player::getMovesDone)).toList();
            case SWIFTNESS -> players.stream().sorted(Comparator.comparing(p -> {
                // If the player hasn't reached the end yet, put it at the end of the list.
                if (!p.hasReachedEnd()) {
                    return Long.MAX_VALUE;
                } else {
                    return p.getReachedEndAt();
                }
            })).toList();
            default -> throw new IllegalStateException("Can't get a sorted players list for game mode " + gameMode);
        };
    }

    // Checks if the game is over.
    private void checkGameOver() {
        assert !gameOver; // Obviously don't do it when the game's already over!

        switch (gameMode) {
            case EFFICIENCY -> {
                // An efficiency game is over when all players have reached the end.
                long playersAtTheEnd = players.stream().filter(Player::hasReachedEnd).count();
                if (playersAtTheEnd == players.size()) {
                    // GAME OVER!
                    // Put all players in first place into a list, and make a "winners" string.
                    final long maxMoves = players.stream().mapToLong(Player::getMovesDone).min().orElse(0);
                    String winners = makeWinnersString(
                            players.stream().filter(x -> x.getMovesDone() == maxMoves).toList()
                    );
                    declareGameOver(winners);
                }
            }
            case SWIFTNESS -> {
                // A swiftness game is over when all players have reached the end.
                long playersAtTheEnd = players.stream().filter(Player::hasReachedEnd).count();
                if (playersAtTheEnd >= players.size()) {
                    // GAME OVER!
                    // We'll only take one winner for this though. The chances of having two players
                    // winning at the exact same frame are so slim I'd rather save five minutes of my time.
                    declareGameOver("Joueur " + (playersSorted().getFirst().getIndex() + 1) + " est premier !");
                }
            }
            case FURTIVITY -> // A furtivity game is over when one player has reached the end.
                    players.stream().filter(Player::hasReachedEnd).findFirst()
                            .ifPresent(player -> declareGameOver("Joueur " + (player.getIndex() + 1) + " a trouvé la sortie !"));
        }
    }

    // Make a sentence based on a list of winners.
    private String makeWinnersString(List<Player> players) {
        var sb = new StringBuilder();

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);

            if (i > 0) {
                // Second or later players: prepend the player with a comma; unless it's the last one then we do "et"
                sb.append(i != (players.size() - 1) ? ", " : " et ");
            }

            // Append the player string.
            sb.append("Joueur ").append(p.getIndex() + 1);
        }

        // French grammar moment
        if (players.size() > 1) {
            sb.append(" ont gagné !");
        } else {
            sb.append(" a gagné !");
        }

        return sb.toString();
    }

    // Declare the game over and build the "game over" overlay.
    private void declareGameOver(String description) {
        assert !gameOver;
        assert gameOverOverlay == null;
        assert overlayParent != null;

        // This is indeed the time at which the game is finally over.
        gameOver = true;

        // Stop all controllers from vibrating, because for some reason there's this SDL bug
        // where it'll keep vibrating over and over despite having stopped sending "JoystickRumble"
        // commands.
        if (controllerHub != null) {
            for (Player p : players) {
                if (p.getInputSource() instanceof PlayerInputSource.Controller(int slot)) {
                    // Give an intensity 0 vibration command with the lowest duration.
                    controllerHub.vibrateController(slot, 0, 0, 1);
                }
            }
        }

        // Make the game over overlay, which is just a VBox that covers all the screen.
        var overlay = new VBox();
        overlay.setBackground(Background.fill(Color.gray(0.0, 0.7)));
        overlay.setAlignment(Pos.CENTER);
        overlay.setSpacing(16);

        // Make the title label, which should be fairly large.
        var title = new Label("FIN DE LA PARTIE");
        title.setFont(Font.font("", FontWeight.BOLD, 32));
        title.setAlignment(Pos.CENTER);
        title.setMaxWidth(Double.MAX_VALUE);
        title.setTextFill(Color.WHITE);

        // Make the description label with the given text
        var desc = new Label(description);
        desc.setFont(Font.font("", FontWeight.NORMAL, 24));
        desc.setAlignment(Pos.CENTER);
        desc.setMaxWidth(Double.MAX_VALUE);
        desc.setTextFill(Color.WHITE);
        desc.setTextAlignment(TextAlignment.CENTER);

        // Make the close button which should call the "closeGame" function (and not deploy()!).
        var closeButton = new Button("Fermer");
        closeButton.setOnAction(_ -> {
            closeGame.run();
            assert overlayParent == null
                    : "The game session wasn't undeployed after running the closeGame lambda given in the constructor!";
        });
        closeButton.setMinWidth(160);
        closeButton.setAlignment(Pos.CENTER);

        // Add all children to the overlay
        overlay.getChildren().addAll(title, desc, closeButton);

        // Display the overlay!
        gameOverOverlay = overlay;
        overlayParent.getChildren().add(overlay);
    }

    // Converts a point in the maze coordinate space to the JavaFX coordinate space.
    private Vector2 toFXCoordinates(Vector2 v) {
        return new Vector2(
                cellWidth * v.x() + cellWidth / 2,
                cellHeight * v.y() + cellHeight / 2
        );
    }
}
