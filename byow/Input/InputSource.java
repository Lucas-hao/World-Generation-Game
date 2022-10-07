package byow.Input;

/**
 * Created by hug.
 */
public abstract class InputSource {
    protected char lastKey = 0;
    protected char currentKey = 0;

    public abstract char getNextKey();

    public abstract boolean possibleNextInput();

    public char getCurrentKey() {
        return currentKey;
    }

    public char getLastKey() {
        return lastKey;
    }
}
