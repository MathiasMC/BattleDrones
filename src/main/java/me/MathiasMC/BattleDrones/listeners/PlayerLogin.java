package me.MathiasMC.BattleDrones.listeners;

import me.MathiasMC.BattleDrones.BattleDrones;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerLogin implements Listener {

    private final BattleDrones plugin;

    public PlayerLogin(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLogin(PlayerLoginEvent e) {
        String uuid = e.getPlayer().getUniqueId().toString();
        plugin.database.insertPlayer(uuid);
        for (String drone : plugin.drones) {
            plugin.database.insertDrone(uuid, drone);
        }
        if (!plugin.list().contains(uuid)) {
            plugin.load(uuid);
        }
    }
}