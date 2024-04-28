package freegamerdev.serverplayertimeticker;

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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;

public class ServerPlayertimeTicker implements ModInitializer {

    private long lastUpdateTime = 0;

    // Define the maximum playtime in minutes
    private static final int MAX_PLAYTIME_SECONDS = 2 * 60;

    // Map to store player playtimes
    private HashMap<String, Integer> playerPlaytimes = new HashMap<>();

    // Playtime data
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
                Text actionBarText = Text.of("Remaining Playtime: " + remainingMinutes + " minutes and " + remainingSeconds + " seconds");

                // Send action bar message to player
                player.sendMessage(actionBarText, true);

                // Check if player playtime exceeds the maximum playtime
                if (remainingPlaytime >= MAX_PLAYTIME_SECONDS) {
                    // Kick the player from the server
                    player.networkHandler.disconnect(Text.of("You have exceeded the maximum playtime for today."));
                }
            }

            // Update PlaytimeData with player playtimes
            playtimeData.setPlayerPlaytimes(playerPlaytimes);

            // Save playtime data
            try {
                PlaytimeDataManager.savePlaytimeData(playtimeData);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Reset playtime for the next day
            LocalDate currentDate = Instant.ofEpochMilli(currentTimeMillis).atZone(ZoneId.systemDefault()).toLocalDate();
            if (currentDate.isAfter(LocalDate.ofYearDay(currentDate.getYear(), currentDate.getDayOfYear()))) {
                playerPlaytimes.clear();
            }
        }
    }

}