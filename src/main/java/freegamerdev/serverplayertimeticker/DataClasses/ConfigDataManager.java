package freegamerdev.serverplayertimeticker.DataClasses;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigDataManager {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Type CONFIG_DATA_TYPE = new TypeToken<ConfigData>() {}.getType();
    private static final Path CONFIG_DATA_FILE = Paths.get("config/serverplayertime/config.json");

    public static ConfigData loadConfigData() throws IOException {
        if (Files.exists(CONFIG_DATA_FILE)) {
            String jsonData = Files.readString(CONFIG_DATA_FILE);
            return gson.fromJson(jsonData, CONFIG_DATA_TYPE);
        } else {
            // If the file doesn't exist, create it and initialize with default data
            ConfigData defaultData = new ConfigData();
            saveConfigData(defaultData); // Save default data to file
            return defaultData;
        }
    }

    public static void saveConfigData(ConfigData configData) throws IOException {
        String jsonData = gson.toJson(configData, CONFIG_DATA_TYPE);
        Files.createDirectories(CONFIG_DATA_FILE.getParent()); // Create directories if they don't exist
        Files.writeString(CONFIG_DATA_FILE, jsonData);
    }
}

