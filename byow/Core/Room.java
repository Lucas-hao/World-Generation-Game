package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class Room {
    private final HashSet<Room> connectedRooms = new HashSet<>();

    private final int height;
    private final int width;

    /* X and Y represents the right bottom corner position of the room */
    private final int cornerX;
    private final int cornerY;

    private Position lightPos = null;

    public Room(int w, int h, int x, int y) {
        this.height = h;
        this.width = w;
        this.cornerX = x;
        this.cornerY = y;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getX() {
        return cornerX;
    }

    public int getY() {
        return cornerY;
    }

    public Position getLightPos() {
        return lightPos;
    }

    public int getCenterX() {
        return cornerX + width / 2;
    }

    public int getCenterY() {
        return cornerY + height / 2;
    }

    public int totalSize() {
        return height * width;
    }

    public int innerSize() {
        return (height - 2) * (width - 2);
    }

    public Room findClosestUnConnectedRoom(List<Room> rooms) {
        double minDistance = Double.MAX_VALUE;
        Room closestRoom = null;
        for (Room room : rooms) {
            if (!this.isConnected(room)) {
                double distance = Math.pow(this.getCenterX() - room.getCenterX(), 2) + Math.pow(
                        this.getCenterY() - room.getCenterY(), 2);
                if (distance < minDistance) {
                    closestRoom = room;
                }
            }
        }
        return closestRoom;
    }

    public void addToConnected(Room room) {
        connectedRooms.add(room);
    }

    public boolean isConnected(Room room) {
        return connectedRooms.contains(room) || this.equals(room);
    }

    public Position generateLight(Random r) {
        int index = RandomUtils.uniform(r, innerSize());
        this.lightPos = new Position(cornerX + 1 + index % (this.width - 2),
                cornerY + 1 + index / (this.width - 2));
        return this.lightPos;
    }


    public boolean isOverlap(TETile[][] world) {
        for (int i = cornerX; i < cornerX + width; i++) {
            for (int j = cornerY; j < cornerY + height; j++) {
                if (world[i][j].equals(Tileset.FLOOR) || world[i][j].equals(Tileset.WALL)
                        || world[i][j].equals(Tileset.LOCKED_DOOR)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param other - another room
     * @return - whether two rooms are overlapped
     */
    public boolean isRoomOverlap(Room other) {
        boolean lb = other.isPointInsideRoom(cornerX + 1, cornerY + 1);
        boolean rb = other.isPointInsideRoom(cornerX + width - 2, cornerY);
        boolean lt = other.isPointInsideRoom(cornerX + 1, cornerY + height - 2);
        boolean rt = other.isPointInsideRoom(cornerX + width - 2, cornerY + height - 2);
        return lb || rb || lt || rt;
    }

    public boolean isPointInsideRoom(int x, int y) {
        return x > cornerX && y > cornerY && x < cornerX + width - 1 && y < cornerY + height - 1;
    }
}
