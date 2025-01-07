package freegamerdev.serverplayertimeticker;

import freegamerdev.serverplayertimeticker.DataClasses.ConfigData;
import freegamerdev.serverplayertimeticker.DataClasses.ConfigDataManager;
import freegamerdev.serverplayertimeticker.DataClasses.PlaytimeData;
import freegamerdev.serverplayertimeticker.DataClasses.PlaytimeDataManager;
import freegamerdev.serverplayertimeticker.listener.PlayerJoinListener;
import freegamerdev.serverplayertimeticker.utils.Utils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerPlayertimeTicker implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("serverplayertimeticker");

    public static int MAX_PLAYTIME_SECONDS = 5 * 60 * 60;
    public static String ACTIONBAR_TEXT = "§e{{time}} {{progressbar}}";
    public static String KICK_MESSAGE = "You have exceeded the maximum playtime for today.";
    public static String RESET_TEXT = "§eIt's new day! Playtime has been resetted.";

    private long lastUpdateTime = 0;

    private static HashMap<String, Integer> playerPlaytimes = new HashMap<>();
    private PlaytimeData playtimeData;
    private HashMap<String, Integer> playerTotalPlayed = new HashMap<>();

    private ScheduledExecutorService executorService;

    synchronized public static HashMap<String, Integer> getPlayerPlaytimes(){
        return playerPlaytimes;
    }

    @Override
    public void onInitialize() {

        try {
            playtimeData = PlaytimeDataManager.loadPlaytimeData();
            if (playtimeData.getLastCheckedDate().isEqual(LocalDate.now())) {
                playerPlaytimes = playtimeData.getPlayerPlaytimes();
            } else {
                playtimeData.setLastCheckedDate(LocalDate.now());
            }
            playerTotalPlayed = playtimeData.getPlayerTotalPlayed();

        } catch (IOException e) {
            e.printStackTrace();
            playtimeData = new PlaytimeData();
        }

        try {
            ConfigData configData = ConfigDataManager.loadConfigData();
            MAX_PLAYTIME_SECONDS = configData.getMaxPlaytimeSeconds();
            ACTIONBAR_TEXT = configData.getActionbarText();
            KICK_MESSAGE = configData.getKickMessage();
            RESET_TEXT = configData.getResetMessage();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        executorService = Executors.newScheduledThreadPool(2);

        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            onShutdown();
        });

        ServerPlayConnectionEvents.JOIN.register(PlayerJoinListener::onPlayerJoin);
    }

    private void onServerTick(MinecraftServer server) {
        long currentTimeMillis = System.currentTimeMillis();
        long elapsedTime = currentTimeMillis - lastUpdateTime;

        if (elapsedTime >= 1000) {
            lastUpdateTime = currentTimeMillis;


            synchronized (playerPlaytimes) {
                // Iterate through all online players
                Iterator<ServerPlayerEntity> playerIterator = server.getPlayerManager().getPlayerList().iterator();
                while (playerIterator.hasNext()) {
                    ServerPlayerEntity player = playerIterator.next();
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

                    // Increment player total played by 1 second
                    playerTotalPlayed.put(playerUUID, playerTotalPlayed.getOrDefault(playerUUID, 0) + 1);

                    final int playtime = playerPlaytimes.get(playerUUID);

                    // Check if player playtime exceeds the maximum playtime
                    if (playtime >= MAX_PLAYTIME_SECONDS) {
                        // Kick the player from the server
                        player.networkHandler.disconnect(Text.of(KICK_MESSAGE));
                    }

                    final int remainingSeconds = MAX_PLAYTIME_SECONDS-playtime;
                    String remainingTimeFormatted = Utils.formatSeconds(remainingSeconds);
                    final double percent = Utils.getPercent(MAX_PLAYTIME_SECONDS, remainingSeconds);

                    //progress bar
                    int barChars = 70;
                    String bar = "";
                    int barFill = (int) ((percent / 100) * barChars);
                    int barRest = barChars - barFill;
                    for (int i = 1; i <= barFill; i++) {
                        if(percent<=10) {
                            bar += "§c|";
                        }else if(percent<=50){
                            bar += "§e|";
                        }else{
                            bar += "§b|";
                        }
                    }
                    for (int i = 1; i <= barRest; i++) {
                        bar += "§8|";
                    }

                    // Construct the message for the action bar
                    String actionbarText = ACTIONBAR_TEXT.replace("{{time}}", remainingTimeFormatted).replace("{{progressbar}}", bar);
                    Text actionBarText = Text.of(actionbarText);

                    // Send action bar message to player
                    player.sendMessage(actionBarText, true);

                }

                // Update PlaytimeData with player playtimes
                playtimeData.setPlayerPlaytimes(playerPlaytimes);
                playtimeData.setPlayerTotalPlayed(playerTotalPlayed);
            }

            // Save playtime data
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
                server.sendMessage(Text.of(RESET_TEXT));
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