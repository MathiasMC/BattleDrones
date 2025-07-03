package me.MathiasMC.BattleDrones.listeners;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.configuration.file.FileConfiguration;
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
        String uuid = e.getEntity().getUniqueId().toString();
        PlayerConnect playerConnect = plugin.getPlayerConnect(uuid);

        if (!playerConnect.isActive()) return;

        FileConfiguration file = plugin.droneFiles.get(playerConnect.getActive());

        boolean stopDrone = file.getBoolean("dead.remove");
        if (!stopDrone) return;

        playerConnect.stopDrone(true, true);
        plugin.drone_targets.remove(uuid);
    }
}