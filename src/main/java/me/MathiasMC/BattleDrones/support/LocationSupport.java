package me.MathiasMC.BattleDrones.support;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.Island;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import me.angeschossen.lands.api.integration.LandsIntegration;
import me.angeschossen.lands.api.player.LandPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocationSupport {

    private final BattleDrones plugin;

    private LandsIntegration landsIntegration = null;

    public LocationSupport(final BattleDrones plugin) {
        this.plugin = plugin;
        if (plugin.getServer().getPluginManager().getPlugin("Lands") != null) {
            this.landsIntegration = new LandsIntegration(plugin);
        }
    }

    public boolean inWorldGuardRegion(Entity entity) {
        if (plugin.getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            final List<String> list = plugin.config.get.getStringList("worldguard");
            if (list.isEmpty()) {
                return true;
            }
            final Location location = BukkitAdapter.adapt(entity.getLocation());
            final RegionContainer container = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer();
            final RegionQuery query = container.createQuery();
            final ApplicableRegionSet set = query.getApplicableRegions(location);
            for (ProtectedRegion region : set) {
                if (list.contains(region.getId())) {
                    return false;
                }
            }
            return true;
        }
        return true;
    }

    public boolean inLocation(Player player, String drone) {
        if (plugin.config.get.getBoolean("iridium-skyblock.use") && plugin.getServer().getPluginManager().getPlugin("IridiumSkyblock") != null) {
            final Island island = IridiumSkyblock.getIslandManager().getIslandViaLocation(player.getLocation());
            if (island != null) {
                if (island.getMembers().contains(player.getUniqueId().toString())) {
                    return true;
                } else {
                    for (String command : plugin.config.get.getStringList("iridium-skyblock.island")) {
                        plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", player.getName()));
                    }
                }
            } else {
                for (String command : plugin.config.get.getStringList("iridium-skyblock.no-island")) {
                    plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", player.getName()));
                }
            }
            return false;
        }
        if (plugin.config.get.contains("drone-worlds")) {
            if (plugin.config.get.contains("drone-worlds.list." + player.getWorld().getName() + "." + drone)) {
                if (plugin.config.get.getBoolean("drone-worlds.blacklist")) {
                    if (!player.hasPermission("battledrones.bypass.drone-worlds." + player.getWorld().getName())) {
                        for (String command : plugin.config.get.getStringList("drone-worlds.list." + player.getWorld().getName() + "." + drone)) {
                            plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", player.getName()));
                        }
                        return false;
                    } else {
                        return true;
                    }
                }
            } else {
                if (!plugin.config.get.getBoolean("drone-worlds.blacklist")) {
                    if (!player.hasPermission("battledrones.bypass.drone-worlds." + player.getWorld().getName())) {
                        for (String command : plugin.config.get.getStringList("drone-worlds.whitelist")) {
                            plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", player.getName()).replace("{drone}", plugin.internalPlaceholders.getActiveDrone(drone)).replace("{world}", player.getWorld().getName()));
                        }
                        return false;
                    } else {
                        return true;
                    }
                }
            }
        }
        return true;
    }

    public void tp(final Player player) {
        if (plugin.config.get.contains("iridium-skyblock.toggle") && plugin.getServer().getPluginManager().getPlugin("IridiumSkyblock") != null) {
            final List<String> options = plugin.config.get.getStringList("iridium-skyblock.toggle.disabled");
            final Island island = IridiumSkyblock.getIslandManager().getIslandViaLocation(player.getLocation());
            if (island != null && island.isVisit()) {
                for (Player islandPlayer : island.getPlayersOnIsland()) {
                    final String member = islandPlayer.getUniqueId().toString();
                    if (island.getMembers().contains(member)) {
                        toggle(islandPlayer, options, "iridium-skyblock.toggle.commands");
                    }
                }
            }
        }
    }

    public void toggle(final Player player, final List<String> options, final String path) {
        final String uuid = player.getUniqueId().toString();
        final PlayerConnect playerConnect = plugin.get(uuid);
        if (playerConnect.hasActive()) {
            final String drone = playerConnect.getActive();
            if (plugin.listDroneHolder().contains(uuid) && plugin.getDroneHolderUUID(uuid).containsKey(drone)) {
                final DroneHolder droneHolder = plugin.getDroneHolder(uuid, drone);
                final ArrayList<String> list = new ArrayList<>();
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
                    for (String command : plugin.config.get.getStringList(path)) {
                        plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", player.getName()).replace("{types}", list.toString().replace("[", "").replace("]", "")));
                    }
                    playerConnect.stopAI();
                    playerConnect.stopFindTargetAI();
                    plugin.droneManager.startAI(player, playerConnect, droneHolder, plugin.droneFiles.get(drone), drone);
                }
            }
        }
    }

    public boolean canTarget(final Player player, final LivingEntity target) {
        if (plugin.config.get.getBoolean("lands") && landsIntegration != null && target instanceof Player) {
            final LandPlayer landPlayer = landsIntegration.getLandPlayer(player.getUniqueId());
            final LandPlayer targetLandPlayer = landsIntegration.getLandPlayer(target.getUniqueId());
            if (landPlayer != null && targetLandPlayer != null) {
                return Collections.disjoint(landPlayer.getLands(), targetLandPlayer.getLands());
            }
        }
        return true;
    }
}
