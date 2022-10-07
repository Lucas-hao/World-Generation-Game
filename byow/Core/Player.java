package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

public class Player extends GameCharacter {

    public Player(int x, int y, TETile tile) {
        this.figureTile = Tileset.AVATAR;
        this.position = new Position(x, y);
        this.originalTile = tile;
    }

    public void playerMove(char key, World world) {
        switch (key) {
            case 'W' -> moveTo(position.x, position.y + 1, world.getWorld());
            case 'S' -> moveTo(position.x, position.y - 1, world.getWorld());
            case 'A' -> moveTo(position.x - 1, position.y, world.getWorld());
            case 'D' -> moveTo(position.x + 1, position.y, world.getWorld());
            case 'F' -> toggleLights(world);
            case ' ' -> toggleDoors(world);
            default -> {}
        }
    }

    public void moveByHistory(String command, World world) {
        for (char c : command.toCharArray()) {
            playerMove(c, world);
        }
    }


    private void toggleDoors(World world) {
        world.toggleDoor(this.position.x - 1, this.position.y);
        world.toggleDoor(this.position.x, this.position.y - 1);
        world.toggleDoor(this.position.x + 1, this.position.y);
        world.toggleDoor(this.position.x, this.position.y + 1);
    }

    private void toggleLights(World world) {
        world.toggleLight(this.position.x - 1, this.position.y);
        world.toggleLight(this.position.x, this.position.y - 1);
        world.toggleLight(this.position.x + 1, this.position.y);
        world.toggleLight(this.position.x, this.position.y + 1);
    }

    public static boolean isPlayerInput(char key) {
        return key == 'W' || key == 'S' || key == 'A' || key == 'D'
                || key == ' ' || key == 'F';
    }
}
