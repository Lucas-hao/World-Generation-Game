package byow.Core;

import byow.Input.InputSource;
import byow.Input.KeyboardInputSource;
import byow.Input.StringInputDevice;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.algs4.StdDraw;

import java.awt.*;
import java.util.*;
import java.util.List;


public class Engine {

    private enum GameState {
        MENU, INIT, GAME, PAUSE, END
    }

    private enum TextAlign {
        LEFT, CENTER, RIGHT
    }

    private enum GameResult {
        NONE, WIN, LOSE
    }

    private final TERenderer ter;
    private World world;
    private GameRecord gameRecord;
    private GameState gameState;
    private GameResult gameResult;
    /* Feel free to change the width and height. */
    public static final int WIN_WIDTH = 80;
    public static final int WIN_HEIGHT = 35;
    private boolean enableRendering = false;
    private boolean partialRendering = false;
    private int seed;

    public Engine() {
        ter = new TERenderer();
    }

    private void resetEngine(boolean rendering) {
        this.enableRendering = rendering;
        this.seed = 0;
        this.world = new World();
        this.gameRecord = new GameRecord();
        this.gameState = GameState.MENU;
        this.gameResult = GameResult.NONE;
    }

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        resetEngine(true);
        InputSource inputSource = new KeyboardInputSource();
        tick(inputSource);
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     * <p>
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     * <p>
     * In other words, both of these calls:
     * - interactWithInputString("n123sss:q")
     * - interactWithInputString("lww")
     * <p>
     * should yield the exact same world state as:
     * - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.
        resetEngine(false);
        InputSource inputSource = new StringInputDevice(input);
        return tick(inputSource);
    }

    /**
     * tick function runs every frame until the end of the input
     *
     * @param input - the input source of the game
     */
    private TETile[][] tick(InputSource input) {
        drawInit();
        drawMenu(null, null);
        while (input.possibleNextInput()) {
            char key = input.getNextKey();
            switch (gameState) {
                case MENU -> processMenuInput(input, key);
                case INIT -> processInitInput(input, key);
                case GAME -> processGameInput(input, key);
                // After the game ended, return the world
                case END -> {
                    drawEndGame();
                    if (key == '\n') {
                        if (this.enableRendering) {
                            System.exit(0);
                        }
                        return world.getWorld();
                    } else if (key == 'R') {
                        resetEngine(this.enableRendering);
                    }
                }
                case PAUSE -> {
                    if (key == 'P') {
                        gameState = GameState.GAME;
                        drawGame(input.getCurrentKey(), true);
                    } else {
                        drawPauseGame(input.getCurrentKey());
                    }
                }
                default -> {}
            }
        }
        return world.getWorld();
    }

    private void processMenuInput(InputSource input, char key) {
        switch (key) {
            case 'N' -> {
                this.gameState = GameState.INIT;
                drawMenu(input.getCurrentKey(), "Please input your seed : " + seed);
            }
            case 'Q' -> {
                this.gameState = GameState.END;
                drawEndGame();
            }
            case 'L' -> {
                if (gameRecord.loadGameRecord()) {
                    this.world.setSeeds(gameRecord.getSeed());
                    this.world.generateWorld();
                    Player player = this.world.getPlayer();
                    assert player != null;
                    player.moveByHistory(gameRecord.getInputHistory(), this.world);
                    this.gameState = GameState.GAME;
                    drawGame(input.getCurrentKey(), true);
                } else {
                    drawMenu(input.getCurrentKey(), "No game record. Please start a new game");
                }
            }
            case 'R' -> {
                if (gameRecord.loadGameRecord()) {
                    this.world.setSeeds(gameRecord.getSeed());
                    this.world.generateWorld();
                    this.gameState = GameState.GAME;
                    drawGame(input.getCurrentKey(), true);
                } else {
                    drawMenu(input.getCurrentKey(), "No game record. Please start a new game");
                }
            }
            default -> drawMenu(input.getCurrentKey(), null);
        }
    }

    private void processInitInput(InputSource input, char key) {
        // input the seed
        if (Character.isDigit(key)) {
            seed = seed * 10 + Character.getNumericValue(key);
        }
        if (key == 'S') {
            // press S to start the game
            gameState = GameState.GAME;
            world.setSeeds(seed);
            world.generateWorld();
            gameRecord.setSeed(seed);
            drawGame(input.getCurrentKey(), true);
        } else if (key != '\0') {
            drawMenu(input.getCurrentKey(), "Please input your seed : " + seed);
        }
    }

    private void processGameInput(InputSource input, char key) {
        Player player = this.world.getPlayer();
        if (Player.isPlayerInput(key)) {
            this.gameRecord.saveKey(key);
            assert player != null;
            player.playerMove(key, this.world);
            if (this.world.getNumOfClosedDoors() <= 0) {
                this.gameState = GameState.END;
                this.gameResult = GameResult.WIN;
                this.gameRecord.saveGameRecord();
                return;
            }
        }
        switch (key) {
            case 'P' -> {
                this.gameState = GameState.PAUSE;
                drawPauseGame(input.getCurrentKey());
            }
            case 'Q' -> {
                if (input.getLastKey() == ':') {
                    this.gameRecord.saveGameRecord();
                    this.gameState = GameState.END;
                    if (this.world.getNumOfClosedDoors() > 0) {
                        this.gameResult = GameResult.LOSE;
                    }
                }
                drawGame(input.getCurrentKey(), true);
            }
            case 'E' -> {
                this.partialRendering = !this.partialRendering;
                drawGame(input.getCurrentKey(), true);
            }
            default -> drawGame(input.getCurrentKey(), true);
        }
    }

    private void drawInit() {
        if (this.enableRendering) {
            this.ter.initialize(WIN_WIDTH, WIN_HEIGHT, 0, 0);
        }
    }

    private void drawMenu(Character c, String command) {
        if (!this.enableRendering) {
            return;
        }
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);

        // Draw title
        Font fontTitle = new Font("Monaco", Font.BOLD, 60);
        Font fontMenu = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(fontTitle);
        StdDraw.text(0.5 * WIN_WIDTH, 0.75 * WIN_HEIGHT, "61B: THE GAME");
        // Draw menu series
        StdDraw.setFont(fontMenu);
        // draw input screen
        StdDraw.text(0.5 * WIN_WIDTH, 5.0 / 10.0 * WIN_HEIGHT, "New Game (N)");
        StdDraw.text(0.5 * WIN_WIDTH, 4.0 / 10.0 * WIN_HEIGHT, "Load Game (L)");
        StdDraw.text(0.5 * WIN_WIDTH, 3.0 / 10.0 * WIN_HEIGHT, "Replay (R)");
        StdDraw.text(0.5 * WIN_WIDTH, 2.0 / 10.0 * WIN_HEIGHT, "Quit (Q)");
        // draw the key pressed
        if (c != null && c != '\0') {
            StdDraw.text(0.9 * WIN_WIDTH, 0.9 * WIN_HEIGHT, "Pressed : " + c);
        }

        // draw the input hint
        if (command != null && !command.isEmpty()) {
            StdDraw.text(0.5 * WIN_WIDTH, 0.1 * WIN_HEIGHT, command);
        }

        StdDraw.show();
    }

    private void drawGame(Character key, boolean isShow) {
        if (!this.enableRendering) {
            return;
        }
        if (this.world != null) {
            Font gameFont = new Font("Monaco", Font.BOLD, TERenderer.TILE_SIZE - 2);
            StdDraw.setFont(gameFont);
            if (partialRendering) {
                Position playerPos = world.getPlayer().getPosition();
                ter.renderPartialFrame(this.world.getWorld(), playerPos, 4, false);
            } else {
                ter.renderFrame(this.world.getWorld(), false);
                drawLightings();
            }
        }
        Font hudFont = new Font("Monaco", Font.BOLD, 16);
        StdDraw.setFont(hudFont);
        if (key != null && key != '\0') {
            String keyHint = "CurrentKey : " + key;
            drawText(keyHint, WIN_WIDTH - keyHint.length() / 4.0, WIN_HEIGHT - 0.5,
                    new Color(0.5f, 0.8f, 0.5f, 0.5f), TextAlign.RIGHT);
        }
        double x = StdDraw.mouseX();
        double y = StdDraw.mouseY();
        if (world != null && x > 0 && x < World.WIDTH && y > 0 && y < World.HEIGHT) {
            String description = "Current Tile: "
                    + world.getWorld()[(int) x][(int) y].description();
            drawText(description, WIN_WIDTH - description.length() / 4.0, WIN_HEIGHT - 1.5,
                    new Color(0.6f, 0.7f, 0.8f, 0.5f), TextAlign.RIGHT);
        }
        if (this.world != null) {
            Position playerPosition = this.world.getPlayer().getPosition();
            String positionHint = String.format("Current Position (%2d, %2d)", playerPosition.x,
                    playerPosition.y);
            drawText(positionHint, WIN_WIDTH - positionHint.length() / 4.0, WIN_HEIGHT - 2.5,
                    new Color(0.7f, 0.5f, 0.3f, 0.5f), TextAlign.RIGHT);
            int numOfClosedDoors = this.world.getNumOfClosedDoors();
            String doorHint = String.format("Remaining closed doors : %d", numOfClosedDoors);
            drawText(doorHint, WIN_WIDTH - doorHint.length() / 4.0, WIN_HEIGHT - 3.5,
                    new Color(0.8f, 0.6f, 0.8f, 0.5f), TextAlign.RIGHT);
        }
        drawText("Pause (P)", "Pause (P)".length() / 4.0, WIN_HEIGHT - 0.5,
                new Color(0.4f, 0.2f, 0.9f, 0.5f), TextAlign.LEFT);
        drawText("Quit and Save (:Q)", "Quit and Save (:Q)".length() / 4.0,
                WIN_HEIGHT - 1.5, new Color(0.1f, 0.7f, 0.6f, 0.5f), TextAlign.LEFT);
        drawText("Toggle Partial Rendering (E)", "Toggle Partial Rendering (E)".length() / 4.0,
                WIN_HEIGHT - 2.5, new Color(0.9f, 0.5f, 0.1f, 0.5f), TextAlign.LEFT);
        if (isShow) {
            StdDraw.show();
        }
    }

    private void drawText(String toDraw, double x, double y, Color bgColor, TextAlign align) {
        if (!this.enableRendering) {
            return;
        }
        StdDraw.setPenColor(bgColor);
        StdDraw.filledRectangle(x, y, toDraw.length() / 4.0, 0.5);
        StdDraw.setPenColor(Color.WHITE);
        switch (align) {
            case CENTER -> StdDraw.text(x, y, toDraw);
            case LEFT -> StdDraw.textLeft(x - toDraw.length() / 4.0, y, toDraw);
            case RIGHT -> StdDraw.textRight(x + toDraw.length() / 4.0, y, toDraw);
        }
    }

    private void drawPauseGame(Character key) {
        if (!this.enableRendering) {
            return;
        }
        drawGame(key, false);
        Font pauseFont = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(pauseFont);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(0.5 * WIN_WIDTH, 0.5 * WIN_HEIGHT, "The Game has Paused!");
        StdDraw.text(0.5 * WIN_WIDTH, 0.6 * WIN_HEIGHT, "Press P to continue the game.");
        StdDraw.show();
    }

    private void drawEndGame() {
        if (!enableRendering) {
            return;
        }
        StdDraw.clear(Color.BLACK);
        Font fontTitle = new Font("Monaco", Font.BOLD, 60);
        StdDraw.setFont(fontTitle);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(0.5 * WIN_WIDTH, 0.75 * WIN_HEIGHT, "End of Game");
        switch (this.gameResult) {
            case WIN -> {
                StdDraw.setPenColor(Color.YELLOW);
                StdDraw.text(0.5 * WIN_WIDTH, 0.5 * WIN_HEIGHT, "Congratulations! YOU WIN!");
            }
            case LOSE -> {
                StdDraw.setPenColor(Color.RED);
                StdDraw.text(0.5 * WIN_WIDTH, 0.5 * WIN_HEIGHT, "Sorry. You Lose.");
            }
            default -> {}
        }
        Font hudFont = new Font("Monaco", Font.BOLD, 16);
        StdDraw.setFont(hudFont);
        drawText("Quit (Enter)", 0.45 * WIN_WIDTH, 0.6 * WIN_HEIGHT, Color.RED, TextAlign.CENTER);
        drawText("Restart (R)", 0.55 * WIN_WIDTH, 0.6 * WIN_HEIGHT, Color.GREEN, TextAlign.CENTER);

        StdDraw.show();
    }

    private void drawLightings() {
        List<Position> lightPositions = world.getLightPositions();
        Set<Position> visited = new HashSet<>();
        for (Position pos : lightPositions) {
            if (world.getWorld()[pos.x][pos.y] == Tileset.LIGHT) {
                Deque<Position> queue = new LinkedList<>();
                queue.addLast(pos);
                int depth = 1;
                while (!queue.isEmpty()) {
                    List<Position> posList = new ArrayList<>();
                    while (!queue.isEmpty()) {
                        posList.add(queue.pollFirst());
                    }
                    for (Position lightPos : posList) {
                        visited.add(lightPos);
                        StdDraw.setPenColor(
                                new Color(0.2f / depth, 0.5f / depth, 0.8f / depth, 0.2f));
                        StdDraw.filledSquare(lightPos.x + 0.5, lightPos.y + 0.5, 0.5);
                        if (isValidLightingPosition(lightPos.x - 1, lightPos.y, visited)) {
                            queue.addLast(new Position(lightPos.x - 1, lightPos.y));
                        }
                        if (isValidLightingPosition(lightPos.x + 1, lightPos.y, visited)) {
                            queue.addLast(new Position(lightPos.x + 1, lightPos.y));
                        }
                        if (isValidLightingPosition(lightPos.x, lightPos.y - 1, visited)) {
                            queue.addLast(new Position(lightPos.x, lightPos.y - 1));
                        }
                        if (isValidLightingPosition(lightPos.x, lightPos.y + 1, visited)) {
                            queue.addLast(new Position(lightPos.x, lightPos.y + 1));
                        }
                        if (isValidLightingPosition(lightPos.x - 1, lightPos.y + 1, visited)) {
                            queue.addLast(new Position(lightPos.x - 1, lightPos.y));
                        }
                        if (isValidLightingPosition(lightPos.x + 1, lightPos.y + 1, visited)) {
                            queue.addLast(new Position(lightPos.x + 1, lightPos.y));
                        }
                        if (isValidLightingPosition(lightPos.x - 1, lightPos.y - 1, visited)) {
                            queue.addLast(new Position(lightPos.x - 1, lightPos.y - 1));
                        }
                        if (isValidLightingPosition(lightPos.x + 1, lightPos.y - 1, visited)) {
                            queue.addLast(new Position(lightPos.x + 1, lightPos.y - 1));
                        }
                    }
                    depth++;
                    if (depth > World.MAX_DEPTH) {
                        break;
                    }
                }
            }
        }
    }

    private boolean isValidLightingPosition(int x, int y, Set<Position> visited) {
        TETile[][] worldMap = world.getWorld();
        return world.isValidPosition(x, y) && worldMap[x][y] != Tileset.WALL
                && worldMap[x][y] != Tileset.LOCKED_DOOR && worldMap[x][y] != Tileset.UNLOCKED_DOOR
                && !visited.contains(new Position(x, y));
    }

}
