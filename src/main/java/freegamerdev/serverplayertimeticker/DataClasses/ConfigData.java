package freegamerdev.serverplayertimeticker.DataClasses;

public class ConfigData {
    private int maxPlaytimeMinutes;
    private String actionbarText;
    private String kickMessage;

    public ConfigData() {
        maxPlaytimeMinutes = 120;
        actionbarText = "Remaining Playtime: %m minutes and %s seconds";
        kickMessage = "You have exceeded the maximum playtime for today.";
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

    public String getKickMessage() {
        return kickMessage;
    }

    public void setKickMessage(String kickMessage) {
        this.kickMessage = kickMessage;
    }
}
