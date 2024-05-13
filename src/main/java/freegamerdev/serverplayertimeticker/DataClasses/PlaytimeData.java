package freegamerdev.serverplayertimeticker.DataClasses;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class PlaytimeData {
    private String lastCheckedDate;
    private HashMap<String, Integer> playerPlaytimes;

    public PlaytimeData() {
        playerPlaytimes = new HashMap<>();
        lastCheckedDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
    }

    public LocalDate getLastCheckedDate() {
        return LocalDate.parse(lastCheckedDate, DateTimeFormatter.ISO_DATE);
    }

    public void setLastCheckedDate(LocalDate lastCheckedDate) {
        this.lastCheckedDate = lastCheckedDate.format(DateTimeFormatter.ISO_DATE);
    }

    public HashMap<String, Integer> getPlayerPlaytimes() {
        return playerPlaytimes;
    }

    public void setPlayerPlaytimes(HashMap<String, Integer> playerPlaytimes) {
        this.playerPlaytimes = playerPlaytimes;
    }
}
