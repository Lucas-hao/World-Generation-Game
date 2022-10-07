package byow.Core;

public class Utils {
    public static int manhattanDistance(Position A, Position B) {
        return Math.abs(A.x - B.x) + Math.abs(A.y - B.y);
    }

    public static int manhattanDistance(int x0, int y0, int x1, int y1) {
        return Math.abs(x0 - x1) + Math.abs(y0 - y1);
    }

    public static double eulerDistance(Position A, Position B) {
        return Math.sqrt(eulerSquare(A.x, A.y, B.x, B.y));
    }

    public static double eulerSquare(int x0, int y0, int x1, int y1) {
        return Math.pow(x0 - x1, 2) + Math.pow(y0 - y1, 2);
    }
}
