package byow.Core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class GameRecord {
    private static final String RECORD_FILE_NAME = "game_record.txt";
    private long seed;
    private String inputHistory;

    public GameRecord() {
        this.seed = 0;
        inputHistory = "";
    }

    public GameRecord(long seed) {
        this.seed = seed;
        inputHistory = "";
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public void saveKey(char ch) {
        inputHistory += ch;
    }

    public void saveGameRecord() {
        try {
            FileWriter writer = new FileWriter(RECORD_FILE_NAME);
            writer.write(String.valueOf(seed) + '\n');
            writer.write(inputHistory);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean loadGameRecord() {
        try {
            File file = new File(RECORD_FILE_NAME);
            if (!file.exists()) {
                return false;
            }
            Scanner reader = new Scanner(file);
            if (reader.hasNextLong()) {
                this.seed = reader.nextLong();
            }
            if (reader.hasNext()) {
                this.inputHistory = reader.next();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public long getSeed() {
        return seed;
    }

    public String getInputHistory() {
        return inputHistory;
    }
}
