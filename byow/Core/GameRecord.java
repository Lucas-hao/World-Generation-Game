package byow.Core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class GameRecord {
    private static final String RECORD_FILE_NAME = "game_record.txt";
    private long seed;
    private String inputHistory;
    private int gameDuration;
    private boolean end;

    public GameRecord() {
        this.seed = 0;
        inputHistory = "";
        gameDuration = 0;
        end = false;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public void saveKey(char ch) {
        inputHistory += ch;
    }

    public void saveGameRecord(int gameDuration, boolean end) {
        try {
            FileWriter writer = new FileWriter(RECORD_FILE_NAME);
            writer.write(String.valueOf(seed) + '\n');
            writer.write(inputHistory + '\n');
            writer.write(String.valueOf(gameDuration) + '\n');
            writer.write(String.valueOf(end));
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
                this.seed = Long.parseLong(reader.nextLine());
            }
            if (reader.hasNext()) {
                this.inputHistory = reader.nextLine();
            }
            if (reader.hasNextInt()) {
                this.gameDuration = Integer.parseInt(reader.nextLine());
            }
            if (reader.hasNextBoolean()) {
                this.end = Boolean.parseBoolean(reader.nextLine());
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void clearRecord() {
        inputHistory = "";
        gameDuration = 0;
        end = false;
    }

    public long getSeed() {
        return seed;
    }

    public String getInputHistory() {
        return inputHistory;
    }

    public int getGameDuration() {return gameDuration;}

    public boolean getEnd() {return end;}
}
