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
    private static final Path PLAYTIME_DATA_FILE = Paths.get("playtime_data.json");

    public static PlaytimeData loadPlaytimeData() throws IOException {
        if (Files.exists(PLAYTIME_DATA_FILE)) {
            String jsonData = Files.readString(PLAYTIME_DATA_FILE);
            return gson.fromJson(jsonData, PLAYTIME_DATA_TYPE);
        } else {
            return new PlaytimeData();
        }
    }

    public static void savePlaytimeData(PlaytimeData playtimeData) throws IOException {
        String jsonData = gson.toJson(playtimeData, PLAYTIME_DATA_TYPE);
        Files.writeString(PLAYTIME_DATA_FILE, jsonData);
    }
}

