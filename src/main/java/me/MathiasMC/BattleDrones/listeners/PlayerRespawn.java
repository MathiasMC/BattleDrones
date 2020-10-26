package me.MathiasMC.BattleDrones.listeners;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.api.Type;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
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
        if (!plugin.getFileUtils().config.contains("player-death-commands")) {
            final Player player = e.getPlayer();
            final String uuid = player.getUniqueId().toString();
            final PlayerConnect playerConnect = plugin.getPlayerConnect(uuid);
            if (playerConnect.isLastActive()) {
                plugin.getEntityManager().spawnDroneSilent(player, playerConnect, playerConnect.getLastActive(), Type.RESPAWN);
            }
        }
    }
}
