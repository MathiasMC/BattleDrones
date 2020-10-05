package me.MathiasMC.BattleDrones.listeners;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class PlayerChangedWorld implements Listener {

    private final BattleDrones plugin;

    public PlayerChangedWorld(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChange(PlayerChangedWorldEvent e) {
        final Player player = e.getPlayer();
        final String uuid = player.getUniqueId().toString();
        final String worldName = e.getPlayer().getWorld().getName();
        if (plugin.config.get.contains("auto-disable." + worldName)) {
            plugin.support.toggle(player, plugin.config.get.getStringList("auto-disable." + worldName + ".list"), "auto-disable." + worldName + ".commands");
        }
        if (plugin.config.get.contains("player-world-change-commands")) {
            if (plugin.list().contains(uuid)) {
                final PlayerConnect playerConnect = plugin.get(uuid);
                if (playerConnect.hasActive()) {
                    for (String command : plugin.config.get.getStringList("player-world-change-commands")) {
                        plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", player.getName()));
                    }
                }
                playerConnect.stopDrone();
            }
        }
        plugin.drone_targets.remove(uuid);
    }
}
