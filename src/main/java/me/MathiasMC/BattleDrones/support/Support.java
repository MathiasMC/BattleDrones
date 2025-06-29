package me.MathiasMC.BattleDrones.support;

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
    public WorldGuard worldGuard;
    public Vault vault;

    public Support(final BattleDrones plugin) {
        this.plugin = plugin;
        if (plugin.getServer().getPluginManager().getPlugin("Vault") != null && plugin.getFileUtils().config.getBoolean("vault")) {
            this.vault = new Vault();
            plugin.getTextUtils().info("Found Vault");
        }
        if (plugin.getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            this.worldGuard = new WorldGuard(plugin);
            plugin.getTextUtils().info("Found WorldGuard");
        }
    }

    public boolean inLocation(Player player, String drone) {
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

    public boolean canTarget(LivingEntity target, FileConfiguration file, String path) {
        return worldGuard == null || worldGuard.canTarget(target, file, path);
    }

    public boolean withdraw(Player player, long cost) {
        PlayerConnect playerConnect = plugin.getPlayerConnect(player.getUniqueId().toString());
        if (vault == null) {
            long coins = playerConnect.getCoins();
            if (coins >= cost) {
                playerConnect.setCoins(coins - cost);
                return true;
            }
            return false;
        }
        return vault.withdraw(player, cost);
    }
}
