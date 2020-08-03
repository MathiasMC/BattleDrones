package me.MathiasMC.BattleDrones.listeners;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerTeleport implements Listener {

    private final BattleDrones plugin;

    public PlayerTeleport(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        final Player player = e.getPlayer();
        final String uuid = player.getUniqueId().toString();
        if (plugin.config.get.contains("player-teleport-commands")) {
            if (plugin.list().contains(uuid)) {
                final PlayerConnect playerConnect = plugin.get(uuid);
                if (playerConnect.hasActive()) {
                    for (String command : plugin.config.get.getStringList("player-teleport-commands")) {
                        plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", player.getName()));
                    }
                }
                playerConnect.stopDrone();
            }
        }
        plugin.locationSupport.tp(player);
        plugin.drone_targets.remove(uuid);
    }
}