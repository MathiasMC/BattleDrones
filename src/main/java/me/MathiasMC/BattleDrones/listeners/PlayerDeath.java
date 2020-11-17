package me.MathiasMC.BattleDrones.listeners;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeath implements Listener {

    private final BattleDrones plugin;

    public PlayerDeath(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onDeath(PlayerDeathEvent e) {
        final String uuid = e.getEntity().getUniqueId().toString();
        final PlayerConnect playerConnect = plugin.getPlayerConnect(uuid);
        if (plugin.getFileUtils().config.contains("player-death-commands")) {
            if (playerConnect.isActive()) {
                for (String command : plugin.getFileUtils().config.getStringList("player-death-commands")) {
                    plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", e.getEntity().getName()));
                }
            }
        }
        playerConnect.stopDrone(true, true);
        plugin.drone_targets.remove(uuid);
    }
}