package me.MathiasMC.BattleDrones.listeners;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.api.Type;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
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
            plugin.getEntityManager().spawnDroneSilent(player, playerConnect, playerConnect.getActive(), Type.TELEPORT);
        }
    }
}