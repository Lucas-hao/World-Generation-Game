package byow.Core;

/**
 * The position used for the A* algorithm for closest path searching
 */
public class PathPosition extends Position {
    int cost;
    PathPosition last;

    public PathPosition(int x, int y) {
        super(x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PathPosition pathPosition = (PathPosition) o;
        return this.x == pathPosition.x && this.y == pathPosition.y;
    }
}
