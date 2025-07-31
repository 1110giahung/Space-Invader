package game.achievements;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A concrete implementation of AchievementFile using standard file I/O.
 */
public class FileHandler implements AchievementFile {

    private String fileLocation;

    /**
     * constructor
     */
    public FileHandler() {
        this.fileLocation = DEFAULT_FILE_LOCATION;
    }

    @Override
    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    @Override
    public String getFileLocation() {
        return fileLocation;
    }

    @Override
    public void save(String data) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileLocation, true))) {
            writer.write(data);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error writing to file: " + fileLocation);
            e.printStackTrace();
        }
    }

    @Override
    public List<String> read() {
        List<String> lines = new ArrayList<>();
        File file = new File(fileLocation);
        if (!file.exists()) {
            return lines;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(fileLocation))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading from file: " + fileLocation);
            e.printStackTrace();
        }
        return lines;
    }
}