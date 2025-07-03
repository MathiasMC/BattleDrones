package me.MathiasMC.BattleDrones.listeners;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.api.Type;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerRespawn implements Listener {

    private final BattleDrones plugin;

    public PlayerRespawn(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onRespawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        String uuid = player.getUniqueId().toString();
        PlayerConnect playerConnect = plugin.getPlayerConnect(uuid);

        if (!playerConnect.isLastActive()) return;

        FileConfiguration file = plugin.droneFiles.get(playerConnect.getLastActive());

        boolean stopDrone = file.getBoolean("dead.remove");
        if (stopDrone) return;

        plugin.getEntityManager().spawnDroneSilent(player, playerConnect, playerConnect.getLastActive(), Type.RESPAWN);
    }
}
