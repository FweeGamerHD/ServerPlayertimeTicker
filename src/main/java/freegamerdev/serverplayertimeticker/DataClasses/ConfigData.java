package freegamerdev.serverplayertimeticker.DataClasses;

public class ConfigData {
    private int maxPlaytimeMinutes;
    private String actionbarText;

    public ConfigData() {
        maxPlaytimeMinutes = 120;
        actionbarText = "Remaining Playtime: %m minutes and %s seconds";
    }

    public int getMaxPlaytimeMinutes() {
        return maxPlaytimeMinutes;
    }

    public void setMaxPlaytimeMinutes(int maxPlaytimeMinutes) {
        this.maxPlaytimeMinutes = maxPlaytimeMinutes;
    }

    public String getActionbarText() {
        return actionbarText;
    }

    public void setActionbarText(String actionbarText) {
        this.actionbarText = actionbarText;
    }
}
