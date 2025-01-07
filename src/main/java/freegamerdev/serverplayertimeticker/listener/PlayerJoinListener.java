package freegamerdev.serverplayertimeticker.listener;

import freegamerdev.serverplayertimeticker.ServerPlayertimeTicker;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashMap;

public class PlayerJoinListener {

    public static void onPlayerJoin(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server){
        ServerPlayerEntity pl = handler.getPlayer();

        HashMap<String, Integer> playerPlaytimes = ServerPlayertimeTicker.getPlayerPlaytimes();
        synchronized (playerPlaytimes){
            if(playerPlaytimes.get(pl.getUuid().toString())!=null){
                final int playtime = playerPlaytimes.get(pl.getUuid().toString());
                if(playtime>=ServerPlayertimeTicker.MAX_PLAYTIME_SECONDS){
                    handler.disconnect(Text.of(ServerPlayertimeTicker.KICK_MESSAGE));
                }
            }
        }
    }

}
