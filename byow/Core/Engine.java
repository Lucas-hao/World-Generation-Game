package byow.Core;

import byow.Input.InputSource;
import byow.Input.KeyboardInputSource;
import byow.Input.StringInputDevice;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import edu.princeton.cs.algs4.StdDraw;

import java.awt.*;


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
    private long seed;

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
        // that works for many input types.
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
                        if (enableRendering) {
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
                    world.setSeeds(gameRecord.getSeed());
                    world.generateWorld();
                    gameState = GameState.GAME;
                    if (!gameRecord.getEnd()) {
                        Player player = this.world.getPlayer();
                        assert player != null;
                        player.moveByHistory(gameRecord.getInputHistory(), this.world);
                        world.setGameStartTime(System.currentTimeMillis() - 1000L * gameRecord.getGameDuration());
                    } else {
                        world.setGameStartTime(System.currentTimeMillis());
                    }
                    drawGame(input.getCurrentKey(), true);
                } else {
                    drawMenu(input.getCurrentKey(), "No game record. Please start a new game");
                }
            }
            case 'R' -> {
                if (gameRecord.loadGameRecord()) {
                    world.setSeeds(gameRecord.getSeed());
                    world.generateWorld();
                    gameState = GameState.GAME;
                    world.setGameStartTime(System.currentTimeMillis());
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
            world.setGameStartTime(System.currentTimeMillis());
            gameRecord.setSeed(seed);
            drawGame(input.getCurrentKey(), true);
        } else if (key != '\0') {
            drawMenu(input.getCurrentKey(), "Please input your seed : " + seed);
        }
    }

    private void processGameInput(InputSource input, char key) {
        Player player = world.getPlayer();
        if (Player.isPlayerInput(key)) {
            if (world.getShowMessage()) {
                world.setShowMessage(false);
            }
            this.gameRecord.saveKey(key);
            assert player != null;
            player.playerMove(key, world);
            if (world.getGameDuration() > World.TIME_LIMIT) {
                gameState = GameState.END;
                gameResult = GameResult.LOSE;
                gameRecord.saveGameRecord(world.getGameDuration(),true);
                return;
            }
            if (this.world.getNumOfLightsOff() == 0 && this.world.getExitOn()) {
                this.gameState = GameState.END;
                this.gameResult = GameResult.WIN;
                this.gameRecord.saveGameRecord(world.getGameDuration(), true);
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
                    this.gameRecord.saveGameRecord(world.getGameDuration(), false);
                    this.gameState = GameState.END;
                    if (this.world.getNumOfClosedDoors() > 0) {
                        this.gameResult = GameResult.LOSE;
                    }
                }
                drawGame(input.getCurrentKey(), true);
            }
            case 'E' -> {
                world.togglePartialRendering();
                drawGame(input.getCurrentKey(), true);
            }
            default -> drawGame(input.getCurrentKey(), true);
        }
    }

    private void drawInit() {
        if (enableRendering) {
            this.ter.initialize(WIN_WIDTH, WIN_HEIGHT, 0, 0);
        }
    }

    private void drawMenu(Character c, String command) {
        if (!enableRendering) {
            return;
        }
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);

        // Draw title
        Font fontTitle = new Font("Monaco", Font.BOLD, 60);
        Font fontMenu = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(fontTitle);
        StdDraw.text(0.5 * WIN_WIDTH, 0.75 * WIN_HEIGHT, "World Generation Adventure");
        // Draw menu series
        StdDraw.setFont(fontMenu);
        // draw input screen
        StdDraw.text(0.5 * WIN_WIDTH, 6.0 / 10.0 * WIN_HEIGHT, "New Game (N)");
        StdDraw.text(0.5 * WIN_WIDTH, 5.0 / 10.0 * WIN_HEIGHT, "Load Game (L)");
        StdDraw.text(0.5 * WIN_WIDTH, 4.0 / 10.0 * WIN_HEIGHT, "Replay (R)");
        StdDraw.text(0.5 * WIN_WIDTH, 3.0 / 10.0 * WIN_HEIGHT, "Quit (Q)");
        // draw the key pressed
        if (c != null && c != '\0') {
            StdDraw.text(0.9 * WIN_WIDTH, 0.9 * WIN_HEIGHT, "Pressed : " + c);
        }

        // draw the input hint
        if (command != null && !command.isEmpty()) {
            StdDraw.text(0.5 * WIN_WIDTH, 0.2 * WIN_HEIGHT, command);
            StdDraw.text(0.5 * WIN_WIDTH, 0.1 * WIN_HEIGHT, "Press 'S' to start");
        }

        StdDraw.show();
    }

    private void drawGame(Character key, boolean isShow) {
        if (!this.enableRendering) {
            return;
        }
        if (world != null) {
            Position playerPos = world.getPlayer().getPosition();
            int[][] lightingMap = world.generateLightingMap();
            if (world.getPartialRendering()) {
                ter.renderPartialFrame(world, 4, false);
            } else {
                ter.renderFrame(world.getWorld(), lightingMap, playerPos, false);
            }
        }
        Font hudFont = new Font("Fira Code", Font.BOLD, 14);
        StdDraw.setFont(hudFont);
        if (key != null && key != '\0') {
            String keyHint = "CurrentKey : " + key;
            drawText(keyHint, WIN_HEIGHT - 0.5, new Color(0.5f, 0.8f, 0.5f, 0.5f), TextAlign.RIGHT);
        }
        double x = StdDraw.mouseX();
        double y = StdDraw.mouseY();
        if (world != null && x > 0 && x < World.WIDTH && y > 0 && y < World.HEIGHT) {
            String description = "Current Tile: " + world.getWorld()[(int) x][(int) y].description();
            drawText(description, WIN_HEIGHT - 1.5, new Color(0.6f, 0.7f, 0.8f, 0.5f), TextAlign.RIGHT);
        }
        if (this.world != null) {
            Position playerPosition = this.world.getPlayer().getPosition();
            String positionHint = String.format("Current Position (%2d, %2d)", playerPosition.x, playerPosition.y);
            drawText(positionHint, WIN_HEIGHT - 2.5, new Color(0.7f, 0.5f, 0.3f, 0.5f), TextAlign.RIGHT);
            String doorHint = String.format("Remaining lights off: %d", world.getNumOfLightsOff());
            drawText(doorHint, WIN_HEIGHT - 3.5, new Color(0.8f, 0.6f, 0.8f, 0.5f), TextAlign.RIGHT);
            String timeHint = String.format("Remaining time is: %d", world.getRemainingTime());
            drawText(timeHint, WIN_HEIGHT - 4.5, Color.ORANGE, TextAlign.RIGHT);
            if (world.getShowMessage()) {
                drawText("Unable to open the door", WIN_HEIGHT - 0.5, Color.RED, TextAlign.CENTER);
                drawText("1: Find the key", WIN_HEIGHT - 1.5, Color.ORANGE, TextAlign.CENTER);
                drawText("2: Turn on all the lights", WIN_HEIGHT - 2.5, Color.GREEN, TextAlign.CENTER);
            }
        }
        drawText("Pause (P)", WIN_HEIGHT - 0.5, new Color(0.4f, 0.2f, 0.9f, 0.5f), TextAlign.LEFT);
        drawText("Quit and Save (:Q)", WIN_HEIGHT - 1.5, new Color(0.1f, 0.7f, 0.6f, 0.5f), TextAlign.LEFT);
        drawText("Toggle Partial Rendering (E)", WIN_HEIGHT - 2.5, new Color(0.9f, 0.5f, 0.1f, 0.5f), TextAlign.LEFT);
        if (isShow) {
            StdDraw.show();
        }
    }

    private void drawText(String toDraw, double y, Color bgColor, TextAlign align) {
        if (!this.enableRendering) {
            return;
        }
        double halfWidth = 8;

        switch (align) {
            case CENTER -> {
                StdDraw.setPenColor(bgColor);
                StdDraw.filledRectangle(WIN_WIDTH / 2.0, y, halfWidth, 0.5);
                StdDraw.setPenColor(Color.WHITE);
                StdDraw.text(WIN_WIDTH / 2.0, y, toDraw);
            }
            case LEFT -> {
                StdDraw.setPenColor(bgColor);
                StdDraw.filledRectangle(halfWidth, y, halfWidth, 0.5);
                StdDraw.setPenColor(Color.WHITE);
                StdDraw.textLeft(1, y, toDraw);
            }
            case RIGHT -> {
                StdDraw.setPenColor(bgColor);
                StdDraw.filledRectangle(WIN_WIDTH - halfWidth, y, halfWidth, 0.5);
                StdDraw.setPenColor(Color.WHITE);
                StdDraw.textRight(WIN_WIDTH - 1, y, toDraw);
            }
        }
    }

    private void drawPauseGame(Character key) {
        if (!this.enableRendering) {
            return;
        }
        drawGame(key, false);
        Font pauseFont = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(pauseFont);
        StdDraw.setPenColor(Color.BLACK);
        StdDraw.filledRectangle(0.5 * WIN_WIDTH, 0.5 * WIN_HEIGHT, 15, 15);
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
                StdDraw.text(0.5 * WIN_WIDTH, 0.35 * WIN_HEIGHT, "Congratulations! YOU WIN!");
            }
            case LOSE -> {
                StdDraw.setPenColor(Color.RED);
                StdDraw.text(0.5 * WIN_WIDTH, 0.35 * WIN_HEIGHT, "Sorry. You Lose.");
            }
            default -> {}
        }

        Font hudFont = new Font("Monaco", Font.BOLD, 24);
        StdDraw.setFont(hudFont);
        StdDraw.setPenColor(new Color(0.8f, 0.2f, 0.0f, 0.5f));
        StdDraw.filledRectangle(WIN_WIDTH / 2.0, 0.6 * WIN_HEIGHT, 8, 1);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(WIN_WIDTH / 2.0, 0.6 * WIN_HEIGHT, "Quit (Enter)");
        StdDraw.setPenColor(new Color(0.1f, 0.8f, 0.0f, 0.5f));
        StdDraw.filledRectangle(WIN_WIDTH / 2.0, 0.5 * WIN_HEIGHT, 8, 1);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(WIN_WIDTH / 2.0, 0.5 * WIN_HEIGHT, "Restart (R)");
        StdDraw.show();
    }
}
