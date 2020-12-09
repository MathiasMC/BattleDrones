package me.MathiasMC.BattleDrones.listeners;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.api.Type;
import me.MathiasMC.BattleDrones.api.events.DroneRemoveEvent;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import me.MathiasMC.BattleDrones.api.events.DroneSpawnEvent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import java.util.ArrayList;
import java.util.List;

public class PlayerSwapHandItems implements Listener {

    private final BattleDrones plugin;

    public PlayerSwapHandItems(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSwap(PlayerSwapHandItemsEvent e) {
        final Player player = e.getPlayer();
        final String uuid = player.getUniqueId().toString();
        if (plugin.getFileUtils().config.getBoolean("swap.shift") && !player.isSneaking()) {
            return;
        }
        final PlayerConnect playerConnect = plugin.getPlayerConnect(uuid);
        if (!playerConnect.isActive()) {
            if (playerConnect.isLastActive()) {
                final FileConfiguration file = plugin.droneFiles.get(playerConnect.getLastActive());
                for (String get : file.getStringList("swap.activate")) {
                    plugin.getParticleManager().displayParticle(get, plugin.getFileUtils().particles.getString(get + ".particle"), player.getLocation().add(0, plugin.getFileUtils().particles.getDouble(get + ".y-offset"), 0), plugin.getFileUtils().particles.getInt(get + ".rgb.r"), plugin.getFileUtils().particles.getInt(get + ".rgb.g"), plugin.getFileUtils().particles.getInt(get + ".rgb.b"), plugin.getFileUtils().particles.getInt(get + ".size"), plugin.getFileUtils().particles.getInt(get + ".amount"));
                }
                final DroneSpawnEvent droneSpawnEvent = new DroneSpawnEvent(player, playerConnect, plugin.getDroneHolder(uuid, playerConnect.getLastActive()));
                droneSpawnEvent.setBypassWait(true);
                droneSpawnEvent.setBypassDroneAmount(false);
                droneSpawnEvent.setBypassLocation(false);
                droneSpawnEvent.setType(Type.SWAP);
                droneSpawnEvent.setSpawnCommands(plugin.getFileUtils().language.getStringList("swap.activate"));
                droneSpawnEvent.spawn();
            }
        } else {
            final DroneRemoveEvent droneRemoveEvent = new DroneRemoveEvent(player, playerConnect, plugin.getDroneHolder(uuid, playerConnect.getActive()));
            final FileConfiguration file = plugin.droneFiles.get(playerConnect.getActive());
            if (file.getInt("swap.cost") != 0) {
                final long cost = file.getLong("swap.cost");

                if (!plugin.getSupport().vault.withdraw(player, cost)) {
                    plugin.getDroneManager().runCommands(player, plugin.getFileUtils().language, "swap.enough");
                    return;
                } else {
                    final List<String> list = new ArrayList<>();
                    for (String command : plugin.getFileUtils().language.getStringList("swap.deactivate-cost")) {
                        list.add(command.replace("{drone}", plugin.getPlaceholderManager().getActiveDrone(playerConnect.getActive())).replace("{cost}", String.valueOf(cost)));
                    }
                    droneRemoveEvent.setRemoveCommands(list);
                }

            } else {
                droneRemoveEvent.setRemoveCommands(plugin.getFileUtils().language.getStringList("swap.deactivate"));
            }
            for (String get : file.getStringList("swap.deactivate")) {
                plugin.getParticleManager().displayParticle(get, plugin.getFileUtils().particles.getString(get + ".particle"), player.getLocation().add(0, plugin.getFileUtils().particles.getDouble(get + ".y-offset"), 0), plugin.getFileUtils().particles.getInt(get + ".rgb.r"), plugin.getFileUtils().particles.getInt(get + ".rgb.g"), plugin.getFileUtils().particles.getInt(get + ".rgb.b"), plugin.getFileUtils().particles.getInt(get + ".size"), plugin.getFileUtils().particles.getInt(get + ".amount"));
            }
            droneRemoveEvent.setType(Type.SWAP);
            droneRemoveEvent.remove();
        }
    }
}
