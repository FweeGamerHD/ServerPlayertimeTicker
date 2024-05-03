package freegamerdev.serverplayertimeticker;

import freegamerdev.serverplayertimeticker.DataClasses.ConfigData;
import freegamerdev.serverplayertimeticker.DataClasses.ConfigDataManager;
import freegamerdev.serverplayertimeticker.DataClasses.PlaytimeData;
import freegamerdev.serverplayertimeticker.DataClasses.PlaytimeDataManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;

public class ServerPlayertimeTicker implements ModInitializer {

    private int MAX_PLAYTIME_SECONDS = 120 * 60;
    private String ACTIONBAR_TEXT = "Remaining Playtime: %m minutes and %s seconds";
    private String KICK_MESSAGE = "You have exceeded the maximum playtime for today.";

    private long lastUpdateTime = 0;
    private LocalDate lastCheckedDate = LocalDate.now();

    private HashMap<String, Integer> playerPlaytimes = new HashMap<>();
    private PlaytimeData playtimeData;

    @Override
    public void onInitialize() {
        try {
            playtimeData = PlaytimeDataManager.loadPlaytimeData();
            playerPlaytimes = playtimeData.getPlayerPlaytimes();
        } catch (IOException e) {
            e.printStackTrace();
            playtimeData = new PlaytimeData();
        }
        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
    }

    private void onServerTick(MinecraftServer server) {
        long currentTimeMillis = System.currentTimeMillis();
        long elapsedTime = currentTimeMillis - lastUpdateTime;

        if (elapsedTime >= 1000) {
            lastUpdateTime = currentTimeMillis;

            try {
                ConfigData configData = ConfigDataManager.loadConfigData();
                MAX_PLAYTIME_SECONDS = configData.getMaxPlaytimeMinutes() * 60;
                ACTIONBAR_TEXT = configData.getActionbarText();
                KICK_MESSAGE = configData.getKickMessage();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            synchronized (playerPlaytimes) {
                // Iterate through all online players
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    LuckPerms luckPerms = LuckPermsProvider.get();
                    User user = luckPerms.getUserManager().getUser(player.getUuid());

                    // Check if the player has the permission
                    if (user != null && user.getCachedData().getPermissionData().checkPermission("playtime.immune").asBoolean()) {
                        // Player has the permission, so skip processing
                        continue;
                    }

                    String playerUUID = player.getUuid().toString();

                    // Increment player playtime by 1 second
                    playerPlaytimes.put(playerUUID, playerPlaytimes.getOrDefault(playerUUID, 0) + 1);

                    int remainingPlaytime = playerPlaytimes.get(playerUUID);
                    int remainingMinutes = (MAX_PLAYTIME_SECONDS - remainingPlaytime) / 60;
                    int remainingSeconds = (MAX_PLAYTIME_SECONDS - remainingPlaytime) % 60;

                    // Construct the message for the action bar
                    String actionbarText = ACTIONBAR_TEXT.replace("%m", String.valueOf(remainingMinutes)).replace("%s", String.valueOf(remainingSeconds));
                    Text actionBarText = Text.of(actionbarText);

                    // Send action bar message to player
                    player.sendMessage(actionBarText, true);

                    // Check if player playtime exceeds the maximum playtime
                    if (remainingPlaytime >= MAX_PLAYTIME_SECONDS) {
                        // Kick the player from the server
                        player.networkHandler.disconnect(Text.of(KICK_MESSAGE));
                    }
                }

                // Update PlaytimeData with player playtimes
                playtimeData.setPlayerPlaytimes(playerPlaytimes);
            }

            // Save playtime data
            try {
                PlaytimeDataManager.savePlaytimeData(playtimeData);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Reset playtime for the next day
            LocalDate currentDate = LocalDate.now();

            if (!currentDate.isEqual(lastCheckedDate)) {
                lastCheckedDate = currentDate;
                synchronized (playerPlaytimes) {
                    playerPlaytimes.clear();
                    playtimeData.setPlayerPlaytimes(playerPlaytimes);
                }
                server.sendMessage(Text.of("Resetting playtime due to new day starting."));
            }
        }
    }
}