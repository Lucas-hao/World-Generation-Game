package byow.Core;

import byow.TileEngine.*;

import java.util.*;

public class World {

    // Some default parameter setting for the world
    private static final int MIN_ROOM_NUM = 10;
    private static final int MAX_ROOM_NUM = 20;
    private static final int MIN_ROOM_WIDTH = 4;
    private static final int MAX_ROOM_WIDTH = 8;
    private static final int MIN_ROOM_HEIGHT = 4;
    private static final int MAX_ROOM_HEIGHT = 8;
    public static final int HEIGHT = 30;
    public static final int WIDTH = 80;
    public static final int MAX_DEPTH = 4;


    private final TETile[][] world = new TETile[WIDTH][HEIGHT];
    // Rooms in the world
    private final List<Room> rooms = new ArrayList<>();
    private Random r;
    private Player player = null;
    private int numOfClosedDoors = 0;

    public World() {
        r = new Random();
        // init the world with nothing
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                world[x][y] = Tileset.NOTHING;
            }
        }
    }

    public void setSeeds(long seed) {
        r = new Random(seed);
    }

    public void generateWorld() {
        initTile();
        generateRooms();
        generateConnections();
        generateDoors();
        generateLights();
        generatePlayer();
    }

    public TETile[][] getWorld() {
        return world;
    }

    public Player getPlayer() {
        return player;
    }

    public List<Position> getLightPositions() {
        List<Position> result = new ArrayList<>();
        for (Room room : rooms) {
            if (room.getLightPos() != null) {
                result.add(room.getLightPos());
            }
        }
        return result;
    }

    public boolean isValidPosition(int x, int y) {
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT;
    }

    public void toggleDoor(int x, int y) {
        if (isValidPosition(x, y)) {
            if (this.world[x][y] == Tileset.LOCKED_DOOR) {
                this.world[x][y] = Tileset.UNLOCKED_DOOR;
                this.numOfClosedDoors--;
            } else if (world[x][y] == Tileset.UNLOCKED_DOOR) {
                this.world[x][y] = Tileset.LOCKED_DOOR;
                this.numOfClosedDoors++;
            }
        }
    }

    public void toggleLight(int x, int y) {
        if (isValidPosition(x, y)) {
            if (this.world[x][y] == Tileset.LIGHT) {
                this.world[x][y] = Tileset.LIGHT_OFF;
            } else if (world[x][y] == Tileset.LIGHT_OFF) {
                this.world[x][y] = Tileset.LIGHT;
            }
        }
    }

    public int getNumOfClosedDoors() {
        return numOfClosedDoors;
    }

    private void initTile() {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                world[x][y] = Tileset.GRASS;
            }
        }
    }

    private void generateRooms() {
        int num = RandomUtils.uniform(r, MIN_ROOM_NUM, MAX_ROOM_NUM);
        System.out.println("Room Number: " + num);
        for (int i = 0; i < num; ) {
            int x = RandomUtils.uniform(r, WIDTH - MIN_ROOM_WIDTH);
            int y = RandomUtils.uniform(r, HEIGHT - MIN_ROOM_HEIGHT);
            int h = Math.min(HEIGHT - y, RandomUtils.uniform(r, MIN_ROOM_HEIGHT, MAX_ROOM_HEIGHT));
            int w = Math.min(WIDTH - x, RandomUtils.uniform(r, MIN_ROOM_WIDTH, MAX_ROOM_WIDTH));
            Room room = new Room(w, h, x, y);
            // Make sure the rooms are not overlapped
            if (!room.isOverlap(world)) {
                System.out.printf("%2d Generated room info: ( x: %2d, y: %2d, w: %2d, h: %2d)\n", i,
                        x, y, w, h);
                rooms.add(room);
                drawRoom(room);
                i++;
            }
        }
    }


    private void generateConnections() {
        for (Room room : rooms) {
            Room closestRoom = room.findClosestUnConnectedRoom(rooms);
            if (closestRoom != null) {
                List<PathPosition> path = findClosestRoadConnection(room, closestRoom);
                if (path != null) {
                    drawPath(path);
                    room.addToConnected(closestRoom);
                    closestRoom.addToConnected(room);
                }
            } else {
                System.out.println("[Warning] Cannot find the closest room");
            }
        }
    }


    /**
     * @param roomA startRoom
     * @param roomB targetRoom
     * @return a List represents the shortest road from A to B
     * @source <a href="https://www.redblobgames.com/pathfinding/a-star/introduction.html">
     * Introduction to A* algorithm</a>
     */
    private List<PathPosition> findClosestRoadConnection(Room roomA, Room roomB) {
        PathPosition start = new PathPosition(roomA.getCenterX(), roomA.getCenterY());
        PathPosition target = new PathPosition(roomB.getCenterX(), roomB.getCenterY());
        List<PathPosition> path = new ArrayList<>();
        HashMap<PathPosition, Integer> fCostMap = new HashMap<>();
        PriorityQueue<PathPosition> queue =
                new PriorityQueue<>(Comparator.comparingInt(a -> a.cost));
        queue.add(start);
        fCostMap.put(start, 0);
        while (!queue.isEmpty()) {
            PathPosition currPos = queue.poll();
            if (currPos.equals(target)) {
                target.last = currPos.last;
                break;
            }
            updateAStar(currPos, new PathPosition(currPos.x - 1, currPos.y), target, fCostMap,
                    queue);
            updateAStar(currPos, new PathPosition(currPos.x, currPos.y - 1), target, fCostMap,
                    queue);
            updateAStar(currPos, new PathPosition(currPos.x + 1, currPos.y), target, fCostMap,
                    queue);
            updateAStar(currPos, new PathPosition(currPos.x, currPos.y + 1), target, fCostMap,
                    queue);
        }
        // iterate the path into the arraylist
        if (target.last == null) {
            System.out.println("[Warning] Unable to find the closest path");
            return null;
        }
        PathPosition backtrace = target;
        while (backtrace != null) {
            path.add(backtrace);
            backtrace = backtrace.last;
        }
        Collections.reverse(path);
        return path;
    }


    private void updateAStar(PathPosition currPos,
                             PathPosition next,
                             PathPosition target,
                             HashMap<PathPosition, Integer> fCostMap,
                             PriorityQueue<PathPosition> queue) {
        // next position is outside the world
        if (next.x < 0 || next.x >= WIDTH || next.y < 0 || next.y >= HEIGHT) {
            return;
        }
        int fNewCost = fCostMap.get(currPos) + 1;
        if (!fCostMap.containsKey(next) || fNewCost < fCostMap.get(next)) {
            fCostMap.put(next, fNewCost);
            next.cost = fNewCost + Utils.manhattanDistance(next, target);
            queue.add(next);
            next.last = currPos;
        }
    }

    private void generateDoors() {
        for (Room room : rooms) {
            List<Position> doors = checkRoomExit(room);
            for (Position doorPos : doors) {
                world[doorPos.x][doorPos.y] = Tileset.LOCKED_DOOR;
                numOfClosedDoors++;
            }
        }
    }

    private void generatePlayer() {
        // random init the player in the center of a random room.
        int idx = RandomUtils.uniform(r, rooms.size());
        Room initRoom = rooms.get(idx);
        while (!world[initRoom.getCenterX()][initRoom.getCenterY()].equals(Tileset.FLOOR)) {
            idx = RandomUtils.uniform(r, rooms.size());
            initRoom = rooms.get(idx);
        }
        player = new Player(initRoom.getCenterX(), initRoom.getCenterY(), Tileset.FLOOR);
        world[initRoom.getCenterX()][initRoom.getCenterY()] = player.getFigureTile();
    }

    private void generateLights() {
        for (Room room : rooms) {
            if (RandomUtils.bernoulli(r)) {
                Position pos = room.generateLight(r);
                this.world[pos.x][pos.y] = Tileset.LIGHT;
            }
        }
    }

    private void drawRoom(Room room) {
        /*
         3--2
         |  |
         0--1
         */
        int x0 = room.getX();
        int y0 = room.getY();
        int x1 = room.getX() + room.getWidth() - 1;
        int y1 = room.getY();
        int x2 = room.getX() + room.getWidth() - 1;
        int y2 = room.getY() + room.getHeight() - 1;
        int x3 = room.getX();
        int y3 = room.getY() + room.getHeight() - 1;
        drawHorizontalLine(Tileset.WALL, x0, y0, x1, y1);
        drawHorizontalLine(Tileset.WALL, x3, y3, x2, y2);
        drawVerticalLine(Tileset.WALL, x0, y0, x3, y3);
        drawVerticalLine(Tileset.WALL, x1, y1, x2, y2);
        drawRect(Tileset.FLOOR, x0 + 1, y0 + 1,
                room.getWidth() - 2, room.getHeight() - 2);
    }


    private void drawHorizontalLine(TETile tile, int startX, int startY, int endX, int endY) {
        assert startY == endY;
        int minX = Math.min(startX, endX);
        int maxX = Math.max(startX, endX);
        for (int x = minX; x <= maxX; x++) {
            this.world[x][startY] = tile;
        }
    }

    private void drawVerticalLine(TETile tile, int startX, int startY, int endX, int endY) {
        assert startX == endX;
        int minY = Math.min(startY, endY);
        int maxY = Math.max(startY, endY);
        for (int y = minY; y <= maxY; y++) {
            this.world[startX][y] = tile;
        }

    }

    private void drawRect(TETile tile, int originX, int originY, int width, int height) {
        assert originX >= 0 && originX + width <= WIDTH && originY >= 0 &&
                originY + height <= HEIGHT;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                this.world[originX + x][originY + y] = tile;
            }
        }
    }

    private void drawPath(List<PathPosition> path) {
        for (PathPosition pos : path) {
            this.world[pos.x][pos.y] = Tileset.FLOOR;
        }
        for (PathPosition pos : path) {
            checkAndAssignWall(pos.x, pos.y - 1);
            checkAndAssignWall(pos.x - 1, pos.y);
            checkAndAssignWall(pos.x, pos.y + 1);
            checkAndAssignWall(pos.x + 1, pos.y);
            checkAndAssignWall(pos.x - 1, pos.y - 1);
            checkAndAssignWall(pos.x - 1, pos.y + 1);
            checkAndAssignWall(pos.x + 1, pos.y - 1);
            checkAndAssignWall(pos.x + 1, pos.y + 1);
        }
    }

    private void checkAndAssignWall(int x, int y) {
        if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT) {
            return;
        }
        if (this.world[x][y].equals(Tileset.FLOOR) || this.world[x][y].equals(Tileset.LOCKED_DOOR)
                || this.world[x][y].equals(Tileset.UNLOCKED_DOOR)) {
            return;
        }
        this.world[x][y] = Tileset.WALL;
    }

    private List<Position> checkRoomExit(Room room) {
        List<Position> doors = new ArrayList<>();
        for (int row = room.getY() + 1; row < room.getY() + room.getHeight() - 1; row++) {
            if (this.world[room.getX()][row] == Tileset.FLOOR
                    && this.world[room.getX()][row - 1] == Tileset.WALL
                    && this.world[room.getX()][row + 1] == Tileset.WALL) {
                doors.add(new Position(room.getX(), row));
            }
            if (this.world[room.getX() + room.getWidth() - 1][row] == Tileset.FLOOR
                    && this.world[room.getX() + room.getWidth() - 1][row - 1] == Tileset.WALL
                    && this.world[room.getX() + room.getWidth() - 1][row + 1] == Tileset.WALL) {
                doors.add(new Position(room.getX() + room.getWidth() - 1, row));
            }
        }
        for (int col = room.getX() + 1; col < room.getX() + room.getWidth() - 1; col++) {
            if (this.world[col][room.getY()] == Tileset.FLOOR
                    && this.world[col - 1][room.getY()] == Tileset.WALL
                    && this.world[col + 1][room.getY()] == Tileset.WALL) {
                doors.add(new Position(col, room.getY()));
            }
            if (this.world[col][room.getY() + room.getHeight() - 1] == Tileset.FLOOR
                    && this.world[col - 1][room.getY() + room.getHeight() - 1] == Tileset.WALL
                    && this.world[col + 1][room.getY() + room.getHeight() - 1] == Tileset.WALL) {
                doors.add(new Position(col, room.getY() + room.getHeight() - 1));
            }
        }
        return doors;
    }
}
