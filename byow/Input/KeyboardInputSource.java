package byow.Input;

import edu.princeton.cs.algs4.StdDraw;

public class KeyboardInputSource extends InputSource {
    public KeyboardInputSource() {

    }

    public char getNextKey() {
        // control the fps of the game
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 100) {
            if (StdDraw.hasNextKeyTyped()) {
                // case-insensitive input
                char c = Character.toUpperCase(StdDraw.nextKeyTyped());
                this.lastKey = this.currentKey;
                this.currentKey = c;
                return c;
            }
        }
        return '\0';
    }

    public boolean possibleNextInput() {
        return true;
    }
}
