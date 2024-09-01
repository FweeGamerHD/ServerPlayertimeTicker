package freegamerdev.serverplayertimeticker;

import freegamerdev.serverplayertimeticker.DataClasses.ConfigData;
import freegamerdev.serverplayertimeticker.DataClasses.ConfigDataManager;
import freegamerdev.serverplayertimeticker.DataClasses.PlaytimeData;
import freegamerdev.serverplayertimeticker.DataClasses.PlaytimeDataManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerPlayertimeTicker implements ModInitializer {

    private int MAX_PLAYTIME_SECONDS = 120 * 60;
    private String ACTIONBAR_TEXT = "Remaining Playtime: %m minutes and %s seconds";
    private String KICK_MESSAGE = "You have exceeded the maximum playtime for today.";

    private long lastUpdateTime = 0;

    private HashMap<String, Integer> playerPlaytimes = new HashMap<>();
    private PlaytimeData playtimeData;

    private ScheduledExecutorService executorService;

    @Override
    public void onInitialize() {
        try {
            playtimeData = PlaytimeDataManager.loadPlaytimeData();
            if (playtimeData.getLastCheckedDate().isEqual(LocalDate.now())) {
                playerPlaytimes = playtimeData.getPlayerPlaytimes();
            } else {
                playtimeData.setLastCheckedDate(LocalDate.now());
            }
        } catch (IOException e) {
            e.printStackTrace();
            playtimeData = new PlaytimeData();
        }

        executorService = Executors.newScheduledThreadPool(2);

        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            onShutdown();
        });
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
                // Create a list to hold players who need to be kicked
                List<ServerPlayerEntity> playersToKick = new ArrayList<>();

                Iterator<ServerPlayerEntity> playerIterator = server.getPlayerManager().getPlayerList().iterator();
                while (playerIterator.hasNext()) {
                    ServerPlayerEntity player = playerIterator.next();
                    LuckPerms luckPerms = LuckPermsProvider.get();
                    User user = luckPerms.getUserManager().getUser(player.getUuid());

                    if (user != null && user.getCachedData().getPermissionData().checkPermission("playtime.immune").asBoolean()) {
                        continue; // Skip players with immunity
                    }

                    String playerUUID = player.getUuid().toString();
                    playerPlaytimes.put(playerUUID, playerPlaytimes.getOrDefault(playerUUID, 0) + 1);

                    int remainingPlaytime = playerPlaytimes.get(playerUUID);
                    int remainingMinutes = (MAX_PLAYTIME_SECONDS - remainingPlaytime) / 60;
                    int remainingSeconds = (MAX_PLAYTIME_SECONDS - remainingPlaytime) % 60;

                    String actionbarText = ACTIONBAR_TEXT.replace("%m", String.valueOf(remainingMinutes)).replace("%s", String.valueOf(remainingSeconds));
                    Text actionBarText = Text.of(actionbarText);

                    player.sendMessage(actionBarText, true);

                    if (remainingPlaytime >= MAX_PLAYTIME_SECONDS) {
                        playersToKick.add(player);
                    }
                }

                // Kick players after iteration
                for (ServerPlayerEntity player : playersToKick) {
                    player.networkHandler.disconnect(Text.of(KICK_MESSAGE));
                    // Do not remove the player’s playtime from the map, to ensure their playtime persists across reconnects
                    // playerPlaytimes.remove(player.getUuid().toString());
                }

                // Update PlaytimeData with player playtimes
                playtimeData.setPlayerPlaytimes(playerPlaytimes);
            }

            try {
                PlaytimeDataManager.savePlaytimeData(playtimeData);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Reset playtime for the next day
            LocalDate currentDate = LocalDate.now();

            if (!currentDate.isEqual(playtimeData.getLastCheckedDate())) {
                playtimeData.setLastCheckedDate(currentDate);
                synchronized (playerPlaytimes) {
                    playerPlaytimes.clear();
                    playtimeData.setPlayerPlaytimes(playerPlaytimes);
                }
                server.sendMessage(Text.of("Resetting playtime due to new day starting."));
            }
        }
    }

    private void onShutdown() {
        shutdownExecutorService();
    }

    private void shutdownExecutorService() {
        executorService.shutdown();

        try {
            // Wait a while for existing tasks to terminate
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Executor service did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            executorService.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}