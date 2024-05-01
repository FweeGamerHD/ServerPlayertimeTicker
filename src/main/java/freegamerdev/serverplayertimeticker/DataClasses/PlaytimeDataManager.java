package freegamerdev.serverplayertimeticker.DataClasses;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PlaytimeDataManager {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Type PLAYTIME_DATA_TYPE = new TypeToken<PlaytimeData>() {}.getType();
    private static final Path PLAYTIME_DATA_FILE = Paths.get("config/serverplayertime/playtime_data.json");

    public static PlaytimeData loadPlaytimeData() throws IOException {
        if (Files.exists(PLAYTIME_DATA_FILE)) {
            String jsonData = Files.readString(PLAYTIME_DATA_FILE);
            return gson.fromJson(jsonData, PLAYTIME_DATA_TYPE);
        } else {
            // If the file doesn't exist, create it and initialize with default data
            PlaytimeData defaultData = new PlaytimeData();
            savePlaytimeData(defaultData); // Save default data to file
            return defaultData;
        }
    }

    public static void savePlaytimeData(PlaytimeData playtimeData) throws IOException {
        String jsonData = gson.toJson(playtimeData, PLAYTIME_DATA_TYPE);
        Files.createDirectories(PLAYTIME_DATA_FILE.getParent()); // Create directories if they don't exist
        Files.writeString(PLAYTIME_DATA_FILE, jsonData);
    }
}

