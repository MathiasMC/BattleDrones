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

    public PlayerTeleport(BattleDrones plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onTeleport(PlayerTeleportEvent e) {
        Player player = e.getPlayer();
        String uuid = player.getUniqueId().toString();
        plugin.drone_targets.remove(uuid);
        PlayerConnect playerConnect = plugin.getPlayerConnect(uuid);
        if (!playerConnect.isActive()) return;
        plugin.getEntityManager().spawnDroneSilent(player, playerConnect, playerConnect.getActive(), Type.TELEPORT);
    }
}