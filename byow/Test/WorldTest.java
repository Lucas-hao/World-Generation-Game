package byow.Test;

import byow.Core.Engine;
import byow.Core.World;
import byow.TileEngine.TETile;
import org.junit.*;

import static org.junit.Assert.assertEquals;

public class WorldTest {
    @Test
    public void sameInputSameResultTest1() {
        Engine engine = new Engine();
        TETile[][] world1 = engine.interactWithInputString("n22s");
        TETile[][] world2 = engine.interactWithInputString("n22s");
        for (int i = 0; i < World.WIDTH; i++) {
            for (int j = 0; j < World.HEIGHT; j++) {
                assertEquals(world1[i][j].description(), world2[i][j].description());
            }
        }
    }

    @Test
    public void sameInputSameResultTest2() {
        Engine engine = new Engine();
        TETile[][] world1 = engine.interactWithInputString("n455857754086099036s");
        TETile[][] world2 = engine.interactWithInputString("n455857754086099036s");
        for (int i = 0; i < World.WIDTH; i++) {
            for (int j = 0; j < World.HEIGHT; j++) {
                assertEquals(world1[i][j].description(), world2[i][j].description());
            }
        }
    }

    @Test
    public void saveGameSameResultTest1() {
        Engine engine = new Engine();
        TETile[][] world1 = engine.interactWithInputString("n22s");
        TETile[][] world2 = engine.interactWithInputString("n22s:q");
        for (int i = 0; i < World.WIDTH; i++) {
            for (int j = 0; j < World.HEIGHT; j++) {
                assertEquals(world1[i][j].description(), world2[i][j].description());
            }
        }
    }

    @Test
    public void loadGameNoInputTest1() {
        Engine engine = new Engine();
        TETile[][] world1 = engine.interactWithInputString("n22s");
        engine.interactWithInputString("n22s:q");
        TETile[][] world2 = engine.interactWithInputString("l");
        for (int i = 0; i < World.WIDTH; i++) {
            for (int j = 0; j < World.HEIGHT; j++) {
                assertEquals(world1[i][j].description(), world2[i][j].description());
            }
        }
    }

    @Test
    public void loadGameWithInputTest1() {
        Engine engine = new Engine();
        TETile[][] world1 = engine.interactWithInputString("n22swwwsssbb:q");
        TETile[][] world2 = engine.interactWithInputString("l");
        for (int i = 0; i < World.WIDTH; i++) {
            for (int j = 0; j < World.HEIGHT; j++) {
                assertEquals(world1[i][j].description(), world2[i][j].description());
            }
        }
    }


    @Test
    public void conjMultipleInputTest() {
        Engine engine = new Engine();
        engine.interactWithInputString("n22swd:q");
        TETile[][] world1 = engine.interactWithInputString("laawddsswwddss:q");
        TETile[][] world2 = engine.interactWithInputString("n22swdlaawddsswwddss");
        for (int i = 0; i < World.WIDTH; i++) {
            for (int j = 0; j < World.HEIGHT; j++) {
                assertEquals(world1[i][j].description(), world2[i][j].description());
            }
        }
    }
}
