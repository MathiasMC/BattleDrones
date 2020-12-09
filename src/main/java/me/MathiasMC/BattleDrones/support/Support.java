package me.MathiasMC.BattleDrones.support;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.Island;
import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.api.DroneRegistry;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.configuration.file.FileConfiguration;
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

    public WorldGuard worldGuard;

    public Vault vault;

    public BetterTeams betterTeams;

    public Support(final BattleDrones plugin) {
        this.plugin = plugin;
        this.vault = new Vault();
        this.worldGuard = new WorldGuard(plugin);
        if (plugin.getFileUtils().config.getBoolean("lands", false) && plugin.getServer().getPluginManager().getPlugin("Lands") != null) {
            this.lands = new Lands(plugin);
            plugin.getTextUtils().info("Added support for Lands");
        }
        if (plugin.getFileUtils().config.getBoolean("factions", false) && plugin.getServer().getPluginManager().getPlugin("Factions") != null) {
            this.factions = new Factions();
            plugin.getTextUtils().info("Added support for Factions");
        }
        if (plugin.getFileUtils().config.getBoolean("towny-advanced", false) && plugin.getServer().getPluginManager().getPlugin("Towny") != null) {
            this.towny = new TownyAdvanced();
            plugin.getTextUtils().info("Added support for Towny");
        }
        if (plugin.getFileUtils().config.getBoolean("residence", false) && plugin.getServer().getPluginManager().getPlugin("Residence") != null) {
            this.residence = new Residence();
            plugin.getTextUtils().info("Added support for Residence");
        }
        if (plugin.getFileUtils().config.getBoolean("better-teams.use", false) && plugin.getServer().getPluginManager().getPlugin("BetterTeams") != null) {
            this.betterTeams = new BetterTeams(plugin);
            plugin.getTextUtils().info("Added support for BetterTeams");
        }
    }

    public boolean inLocation(Player player, String drone) {
        if (plugin.getFileUtils().config.getBoolean("iridium-skyblock.use") && plugin.getServer().getPluginManager().getPlugin("IridiumSkyblock") != null) {
            final Island island = IridiumSkyblock.getIslandManager().getIslandViaLocation(player.getLocation());
            if (island != null) {
                if (island.getMembers().contains(player.getUniqueId().toString())) {
                    return true;
                } else {
                    for (String command : plugin.getFileUtils().config.getStringList("iridium-skyblock.island")) {
                        plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", player.getName()));
                    }
                }
            } else {
                for (String command : plugin.getFileUtils().config.getStringList("iridium-skyblock.no-island")) {
                    plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", player.getName()));
                }
            }
            return false;
        }
        if (!plugin.getFileUtils().config.contains("drone-worlds")) {
            return true;
        }
        final String worldName = player.getWorld().getName();
        final String path = "drone-worlds.list." + worldName + "." + drone;
        final boolean blacklist = plugin.getFileUtils().config.getBoolean("drone-worlds.blacklist");
        final boolean hasPermission = player.hasPermission("battledrones.bypass.drone-worlds." + worldName);
        if (plugin.getFileUtils().config.contains(path)) {
            if (blacklist) {
                if (hasPermission) {
                    return true;
                }
                plugin.getDroneManager().runCommands(player, plugin.getFileUtils().config.getStringList(path));
                return false;
            }
        } else if (!blacklist) {
            if (hasPermission) {
                return true;
            }
            for (String command : plugin.getFileUtils().config.getStringList("drone-worlds.whitelist")) {
                plugin.getServer().dispatchCommand(plugin.consoleSender, plugin.getPlaceholderManager().replacePlaceholders(player, command.replace("{drone}", plugin.getPlaceholderManager().getActiveDrone(drone))));
            }
            return false;
        }
        return true;
    }

    public void tp(final Player player) {
        if (plugin.getServer().getPluginManager().getPlugin("IridiumSkyblock") == null) {
            return;
        }
        if (!plugin.getFileUtils().config.contains("iridium-skyblock.toggle")) {
            return;
        }

        final List<String> options = plugin.getFileUtils().config.getStringList("iridium-skyblock.toggle.disabled");
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

    public void toggle(final Player player, final List<String> options, final String path) {
        final String uuid = player.getUniqueId().toString();
            final PlayerConnect playerConnect = plugin.getPlayerConnect(uuid);
            if (playerConnect.isActive()) {
                final String drone = playerConnect.getActive();
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
                    for (String command : plugin.getFileUtils().config.getStringList(path)) {
                        plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", player.getName()).replace("{types}", list.toString().replace("[", "").replace("]", "")));
                    }
                    if (playerConnect.isActive()) {
                        playerConnect.stopAI();
                        final DroneRegistry droneRegistry = plugin.droneRegistry.get(drone);
                        droneRegistry.follow(player, playerConnect, droneHolder);
                        droneRegistry.find(player, playerConnect, droneHolder);
                        droneRegistry.ability(player, playerConnect, droneHolder);
                    }
                }
            }
    }

    public boolean canTarget(final Player player, final LivingEntity target, final FileConfiguration file, final String path) {
        if (target instanceof Player) {
            final Player targetPlayer = (Player) target;
            if (lands != null && !lands.canTarget(player, targetPlayer)) return false;
            if (factions != null && !factions.canTarget(player, targetPlayer)) return false;
            if (towny != null && !towny.canTarget(player, targetPlayer)) return false;
            if (residence != null && !residence.canTarget(player, targetPlayer)) return false;
            if (betterTeams != null && !betterTeams.canTarget(player, targetPlayer)) return false;
        }
        return worldGuard.canTarget(target, file, path);
    }
}
