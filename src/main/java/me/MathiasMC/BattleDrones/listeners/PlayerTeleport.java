package me.MathiasMC.BattleDrones.listeners;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.Island;
import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.List;

public class PlayerTeleport implements Listener {

    private final BattleDrones plugin;

    public PlayerTeleport(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        final Player player = e.getPlayer();
        final String uuid = player.getUniqueId().toString();
        if (plugin.config.get.contains("player-teleport-commands")) {
            if (plugin.list().contains(uuid)) {
                final PlayerConnect playerConnect = plugin.get(uuid);
                if (playerConnect.hasActive()) {
                    for (String command : plugin.config.get.getStringList("player-teleport-commands")) {
                        plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", player.getName()));
                    }
                }
                playerConnect.stopDrone();
            }
        }
        if (plugin.config.get.getBoolean("iridium-skyblock.use") && plugin.config.get.contains("iridium-skyblock.toggle") && plugin.getServer().getPluginManager().getPlugin("IridiumSkyblock") != null) {
            final List<String> options = plugin.config.get.getStringList("iridium-skyblock.toggle.disabled");
            final Island island = IridiumSkyblock.getIslandManager().getIslandViaLocation(player.getLocation());
            if (island != null && island.isVisit()) {
                for (Player islandPlayer : island.getPlayersOnIsland()) {
                    final String member = islandPlayer.getUniqueId().toString();
                    if (island.getMembers().contains(member)) {
                        PlayerConnect playerConnect = plugin.get(member);
                        if (playerConnect.hasActive()) {
                            final String drone = playerConnect.getActive();
                            if (plugin.listDroneHolder().contains(member) && plugin.getDroneHolderUUID(member).containsKey(drone)) {
                                final DroneHolder droneHolder = plugin.getDroneHolder(member, drone);
                                ArrayList<String> list = new ArrayList<>();
                                if (options.contains("PLAYERS") && droneHolder.getPlayers() != 0) {
                                    droneHolder.setPlayers(0);
                                    list.add("players");
                                }
                                if (options.contains("ANIMALS") && droneHolder.getAnimals() != 0) {
                                    droneHolder.setAnimals(0);
                                    list.add("animals");
                                }
                                if (options.contains("MONSTERS") && droneHolder.getMonsters() != 0) {
                                    droneHolder.setMonsters(0);
                                    list.add("monsters");
                                }
                                if (!list.isEmpty()) {
                                    for (String command : plugin.config.get.getStringList("iridium-skyblock.toggle.commands")) {
                                        plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", islandPlayer.getName()).replace("{types}", list.toString().replace("[", "").replace("]", "")));
                                    }
                                    playerConnect.stopAI();
                                    playerConnect.stopFindTargetAI();
                                    plugin.droneManager.startAI(islandPlayer, playerConnect, droneHolder, plugin.droneFiles.get(drone), drone);
                                }
                            }
                        }
                    }
                }
            }
        }
        plugin.drone_targets.remove(uuid);
    }
}