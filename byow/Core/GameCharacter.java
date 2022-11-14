package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

public abstract class GameCharacter {
    protected Position position;
    protected TETile originalTile;
    protected TETile figureTile;

    protected void moveTo(int x, int y, TETile[][] world) {
        // check if valid position
        if (x < 0 || x >= World.WIDTH || y < 0 || y >= World.HEIGHT) {
            return;
        }
        if (!isWalkableTile(world[x][y])) {
            return;
        }
        // update the position
        world[position.x][position.y] = originalTile;
        originalTile = world[x][y];
        position.x = x;
        position.y = y;
        world[position.x][position.y] = figureTile;
    }

    public boolean isWalkableTile(TETile tile) {
        return tile.equals(Tileset.FLOOR) || tile.equals(Tileset.GRASS) || tile.equals(Tileset.NOTHING)
                || tile.equals(Tileset.UNLOCKED_DOOR);
    }

    public TETile getFigureTile() {
        return this.figureTile;
    }

    public Position getPosition() {
        return this.position;
    }
}
