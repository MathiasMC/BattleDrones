package me.MathiasMC.BattleDrones.listeners;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.api.Type;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import me.MathiasMC.BattleDrones.api.events.DroneSpawnEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerTeleport implements Listener {

    private final BattleDrones plugin;

    public PlayerTeleport(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onTeleport(PlayerTeleportEvent e) {
        final Player player = e.getPlayer();
        final String uuid = player.getUniqueId().toString();
        plugin.getSupport().tp(player);
        plugin.drone_targets.remove(uuid);
        final PlayerConnect playerConnect = plugin.getPlayerConnect(uuid);
        if (playerConnect.isActive()) {
            final DroneSpawnEvent droneSpawnEvent = new DroneSpawnEvent(player, playerConnect, plugin.getDroneHolder(uuid, playerConnect.getActive()));
            droneSpawnEvent.setBypassWait(true);
            droneSpawnEvent.setBypassDroneAmount(true);
            droneSpawnEvent.setBypassLocation(true);
            droneSpawnEvent.setType(Type.TELEPORT);
            droneSpawnEvent.setSpawnCommands(null);
            droneSpawnEvent.spawn();
        }
    }
}