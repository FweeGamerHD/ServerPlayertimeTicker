package freegamerdev.serverplayertimeticker.DataClasses;

import java.util.HashMap;

public class PlaytimeData {
    private HashMap<String, Integer> playerPlaytimes;

    public PlaytimeData() {
        playerPlaytimes = new HashMap<>();
    }

    public HashMap<String, Integer> getPlayerPlaytimes() {
        return playerPlaytimes;
    }

    public void setPlayerPlaytimes(HashMap<String, Integer> playerPlaytimes) {
        this.playerPlaytimes = playerPlaytimes;
    }
}
