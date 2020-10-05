package me.MathiasMC.BattleDrones.support;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.Island;
import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Support {

    private final BattleDrones plugin;

    private Lands lands = null;

    private Factions factions = null;

    private TownyAdvanced towny = null;

    private Residence residence = null;

    public WorldGuard worldGuard = null;

    public Support(final BattleDrones plugin) {
        this.plugin = plugin;
        if (plugin.getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            this.worldGuard = new WorldGuard();
            plugin.textUtils.info("Found WorldGuard");
        }
        if (plugin.getServer().getPluginManager().getPlugin("Lands") != null) {
            this.lands = new Lands(this.plugin);
            plugin.textUtils.info("Found Lands");
        }
        if (plugin.getServer().getPluginManager().getPlugin("Factions") != null) {
            this.factions = new Factions();
            plugin.textUtils.info("Found Factions");
        }
        if (plugin.getServer().getPluginManager().getPlugin("Towny") != null) {
            this.towny = new TownyAdvanced();
            plugin.textUtils.info("Found Towny");
        }
        if (plugin.getServer().getPluginManager().getPlugin("Residence") != null) {
            this.residence = new Residence();
            plugin.textUtils.info("Found Residence");
        }
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
            final String worldName = player.getWorld().getName();
            if (plugin.config.get.contains("drone-worlds.list." + worldName + "." + drone)) {
                if (plugin.config.get.getBoolean("drone-worlds.blacklist")) {
                    if (!player.hasPermission("battledrones.bypass.drone-worlds." + worldName)) {
                        for (String command : plugin.config.get.getStringList("drone-worlds.list." + worldName + "." + drone)) {
                            plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", player.getName()));
                        }
                        return false;
                    } else {
                        return true;
                    }
                }
            } else {
                if (!plugin.config.get.getBoolean("drone-worlds.blacklist")) {
                    if (!player.hasPermission("battledrones.bypass.drone-worlds." + worldName)) {
                        for (String command : plugin.config.get.getStringList("drone-worlds.whitelist")) {
                            plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", player.getName()).replace("{drone}", plugin.internalPlaceholders.getActiveDrone(drone)).replace("{world}", worldName));
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
        if (plugin.list().contains(uuid)) {
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
                        if (playerConnect.hasActive()) {
                            playerConnect.stopAI();
                            playerConnect.stopFindTargetAI();
                            plugin.droneManager.startAI(player, playerConnect, droneHolder, plugin.droneFiles.get(drone), drone);
                        }
                    }
                }
            }
        }
    }

    public boolean canTarget(final Player player, final LivingEntity target) {
        if (target instanceof Player) {
            if (plugin.config.get.getBoolean("lands")) {
                return lands.canTarget(player, target);
            }
            if (plugin.config.get.getBoolean("factions")) {
                return factions.canTarget(player, target);
            }
            if (plugin.config.get.getBoolean("towny-advanced")) {
                return towny.canTarget(player, target);
            }
            if (plugin.config.get.getBoolean("residence")) {
                return residence.canTarget(player, target);
            }
        }
        return true;
    }
}
