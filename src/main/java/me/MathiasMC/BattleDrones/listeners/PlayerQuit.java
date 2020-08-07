package me.MathiasMC.BattleDrones.listeners;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuit implements Listener {

    private final BattleDrones plugin;

    public PlayerQuit(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onQuit(PlayerQuitEvent e) {
        final String uuid = e.getPlayer().getUniqueId().toString();
        if (plugin.list().contains(uuid)) {
            final PlayerConnect playerConnect = plugin.get(uuid);
            playerConnect.stopDrone();
            plugin.unload(uuid);
        }
        if (plugin.listDroneHolder().contains(uuid)) {
            plugin.unloadDroneHolder(uuid);
        }
        plugin.drone_targets.remove(uuid);
    }
}