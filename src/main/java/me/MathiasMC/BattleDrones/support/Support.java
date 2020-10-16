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

    public Support(final BattleDrones plugin) {
        this.plugin = plugin;
        this.vault = new Vault();
        this.worldGuard = new WorldGuard(plugin);
        if (plugin.getServer().getPluginManager().getPlugin("Lands") != null) {
            this.lands = new Lands(this.plugin);
            plugin.getTextUtils().info("Found Lands");
        }
        if (plugin.getServer().getPluginManager().getPlugin("Factions") != null) {
            this.factions = new Factions();
            plugin.getTextUtils().info("Found Factions");
        }
        if (plugin.getServer().getPluginManager().getPlugin("Towny") != null) {
            this.towny = new TownyAdvanced();
            plugin.getTextUtils().info("Found Towny");
        }
        if (plugin.getServer().getPluginManager().getPlugin("Residence") != null) {
            this.residence = new Residence();
            plugin.getTextUtils().info("Found Residence");
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
        if (plugin.getFileUtils().config.contains("drone-worlds")) {
            final String worldName = player.getWorld().getName();
            if (plugin.getFileUtils().config.contains("drone-worlds.list." + worldName + "." + drone)) {
                if (plugin.getFileUtils().config.getBoolean("drone-worlds.blacklist")) {
                    if (!player.hasPermission("battledrones.bypass.drone-worlds." + worldName)) {
                        plugin.getDroneManager().runCommands(player, plugin.getFileUtils().config.getStringList("drone-worlds.list." + worldName + "." + drone));
                        return false;
                    } else {
                        return true;
                    }
                }
            } else {
                if (!plugin.getFileUtils().config.getBoolean("drone-worlds.blacklist")) {
                    if (!player.hasPermission("battledrones.bypass.drone-worlds." + worldName)) {
                        plugin.getDroneManager().runCommands(player, plugin.getFileUtils().config.getStringList("drone-worlds.whitelist"));
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
        if (plugin.getFileUtils().config.contains("iridium-skyblock.toggle") && plugin.getServer().getPluginManager().getPlugin("IridiumSkyblock") != null) {
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
            boolean Lands = true;
            boolean Factions = true;
            boolean Towny = true;
            boolean Residence = true;
            if (plugin.getFileUtils().config.getBoolean("lands") && lands != null) {
                Lands = lands.canTarget(player, target);
            }
            if (plugin.getFileUtils().config.getBoolean("factions") && factions != null) {
                Factions = factions.canTarget(player, target);
            }
            if (plugin.getFileUtils().config.getBoolean("towny-advanced") && towny != null) {
                Towny = towny.canTarget(player, target);
            }
            if (plugin.getFileUtils().config.getBoolean("residence") && residence != null) {
                Residence = residence.canTarget(player, target);
            }
            return Lands && Factions && Towny && Residence;
        }
        return worldGuard.canTarget(target, file, path);
    }
}
