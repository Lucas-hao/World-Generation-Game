package byow.Input;

import java.util.Random;

/**
 * Created by hug.
 */
public class RandomInputSource extends InputSource {
    Random r;

    public RandomInputSource(Long seed) {
        r = new Random(seed);
    }

    /**
     * Returns a random letter between a and z.
     */
    public char getNextKey() {
        this.lastKey = this.currentKey;
        this.currentKey = (char) (r.nextInt(26) + 'A');
        return this.currentKey;
    }

    public boolean possibleNextInput() {
        return true;
    }
}
