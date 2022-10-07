package byow.Input;

/**
 * Created by hug.
 */
public class StringInputDevice extends InputSource {
    private String input;
    private int index;

    public StringInputDevice(String s) {
        index = 0;
        input = s;
    }

    public char getNextKey() {
        char returnChar =  Character.toUpperCase(input.charAt(index));
        this.lastKey = this.currentKey;
        this.currentKey = returnChar;
        index += 1;
        return returnChar;
    }

    public boolean possibleNextInput() {
        return index < input.length();
    }
}
