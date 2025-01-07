package freegamerdev.serverplayertimeticker.DataClasses;

public class ConfigData {
    private int maxPlaytimeSeconds;
    private String actionbarText;
    private String kickMessage;
    private String resetMessage;

    public ConfigData() {
        maxPlaytimeSeconds = 5 * 60 * 60;
        actionbarText = "§e{{time}} {{progressbar}}";
        kickMessage = "You have exceeded the maximum playtime for today.";
        resetMessage = "§eIts new day! Playtime has been resetted.";
    }

    public int getMaxPlaytimeSeconds() {
        return maxPlaytimeSeconds;
    }

    public void setMaxPlaytimeSeconds(int maxPlaytimeSeconds) {
        this.maxPlaytimeSeconds = maxPlaytimeSeconds;
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

    public String getResetMessage() {
        return resetMessage;
    }

    public void setResetMessage(String resetMessage) {
        this.resetMessage = resetMessage;
    }
}
