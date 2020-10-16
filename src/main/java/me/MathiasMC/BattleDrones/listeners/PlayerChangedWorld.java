package me.MathiasMC.BattleDrones.listeners;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class PlayerChangedWorld implements Listener {

    private final BattleDrones plugin;

    public PlayerChangedWorld(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onChange(PlayerChangedWorldEvent e) {
        final Player player = e.getPlayer();
        final String uuid = player.getUniqueId().toString();
        final String worldName = e.getPlayer().getWorld().getName();
        if (plugin.getFileUtils().config.contains("auto-disable." + worldName)) {
            plugin.getSupport().toggle(player, plugin.getFileUtils().config.getStringList("auto-disable." + worldName + ".list"), "auto-disable." + worldName + ".commands");
        }
        if (plugin.getFileUtils().config.contains("player-world-change-commands")) {
                final PlayerConnect playerConnect = plugin.getPlayerConnect(uuid);
                if (playerConnect.isActive()) {
                    for (String command : plugin.getFileUtils().config.getStringList("player-world-change-commands")) {
                        plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", player.getName()));
                    }
                }
                playerConnect.stopDrone();
        }
        plugin.drone_targets.remove(uuid);
    }
}
