package me.MathiasMC.BattleDrones.listeners;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeath implements Listener {

    private final BattleDrones plugin;

    public PlayerDeath(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        String uuid = e.getEntity().getUniqueId().toString();
        if (plugin.list().contains(uuid)) {
            PlayerConnect playerConnect = plugin.get(uuid);
            if (playerConnect.hasActive()) {
                for (String command : plugin.config.get.getStringList("player-death-commands")) {
                    plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", e.getEntity().getName()));
                }
            }
            playerConnect.stopDrone();
        }
        plugin.drone_targets.remove(uuid);
    }
}