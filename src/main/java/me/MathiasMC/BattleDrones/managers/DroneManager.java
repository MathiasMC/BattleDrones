package me.MathiasMC.BattleDrones.managers;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import me.MathiasMC.BattleDrones.api.events.DroneDamageEvent;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class DroneManager {

    private final BattleDrones plugin;

    public DroneManager(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    public void checkMessage(final long amount, final long maxAmount, final String name, final String type) {
        final long left = Double.valueOf(Math.floor(amount * (100D / maxAmount))).longValue();
        if (left != 0L) {
            if (left == Double.valueOf(Math.floor((amount + 1) * (100D / maxAmount))).longValue()) {
                return;
            }
        } else if (amount != 1) {
            return;
        }
        if (plugin.getFileUtils().config.contains("low-" + type + "." + left)) {
            for (String command : plugin.getFileUtils().config.getStringList("low-" + type + "." + left)) {
                plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", name));
            }
        }
    }


    public void checkShot(final Player player, final LivingEntity target, final FileConfiguration file, final Location location, final String listPath, final String type) {
        String path = "";
        if (target instanceof Player) {
            path = listPath + type + ".player";
        } else if (plugin.getEntityManager().isMonster(target)) {
            path = listPath + type + ".monster";
        } else if (plugin.getEntityManager().isAnimal(target)) {
            path = listPath + type + ".animal";
        }
        final String x = String.valueOf(location.getBlockX());
        final String y = String.valueOf(location.getBlockY());
        final String z = String.valueOf(location.getBlockZ());
        final String world = Objects.requireNonNull(location.getWorld()).getName();
        String targetName = target.getName();
        final String translate = targetName.toUpperCase().replace(" ", "_");
        if (plugin.getFileUtils().language.contains("translate." + translate)) {
            targetName = String.valueOf(plugin.getFileUtils().language.getString("translate." + translate));
        }
        if (file.contains(path)) {
            for (String command : file.getStringList(path)) {
                plugin.getServer().dispatchCommand(plugin.consoleSender, command
                        .replace("{world}", world)
                        .replace("{x}", x)
                        .replace("{y}", y)
                        .replace("{z}", z)
                        .replace("{player}", player.getName())
                        .replace("{target}", targetName));
            }
        }
    }

    public void takeAmmo(final Player player, final PlayerConnect playerConnect, final DroneHolder droneHolder, final FileConfiguration file, final String path) {
        if (droneHolder.getAmmo() < 1) {
            return;
        }
        droneHolder.setAmmo(droneHolder.getAmmo() - 1);
        if (droneHolder.getWear() > 0) {
            droneHolder.setWear(droneHolder.getWear() - 1);
        } else {
            if (droneHolder.getHealth() - 1 >= 0) {
                droneHolder.setWear(file.getInt(path + "wear-and-tear"));
                droneHolder.setHealth(droneHolder.getHealth() - 1);
            } else {
                droneDeath(playerConnect, droneHolder, file);
                for (String command : file.getStringList("dead.wear")) {
                    plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", player.getName()));
                }
            }
        }
    }

    public void waitSchedule(final String uuid, final FileConfiguration file) {
        plugin.drone_wait.add(uuid);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.drone_wait.remove(uuid), file.getInt("gui.WAIT-SECONDS") * 20);
    }

    public boolean canBypassDroneAmount(final Player player) {
        return plugin.drone_amount.size() < plugin.getFileUtils().config.getInt("drone-amount") || player.hasPermission("battledrones.bypass.drone-amount");
    }

    public void runCommands(final Player player, final List<String> list) {
        for (String command : list) {
            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', plugin.getPlaceholderManager().replacePlaceholders(player, command)));
        }
    }

    public void damage(final Player damager, final String key, final ArmorStand entity) {
        final PlayerConnect playerConnect = plugin.getPlayerConnect(key);
        final DroneHolder droneHolder = plugin.getDroneHolder(key, playerConnect.getActive());
        final DroneDamageEvent droneDamageEvent = new DroneDamageEvent(damager, playerConnect, droneHolder);
        plugin.getServer().getPluginManager().callEvent(droneDamageEvent);
        if (droneDamageEvent.isCancelled()) {
            return;
        }
        final FileConfiguration file = plugin.droneFiles.get(playerConnect.getActive());
        if (!plugin.getSupport().canTarget(damager, entity, file, playerConnect.getGroup() + "." + droneHolder.getLevel() + ".worldguard.damage")) {
            return;
        }
        final String name = plugin.getServer().getOfflinePlayer(UUID.fromString(key)).getName();
        if (droneHolder.getHealth() - 1 > 0) {
            droneHolder.setHealth(droneHolder.getHealth() - 1);
            for (String command : file.getStringList(playerConnect.getGroup() + "." + droneHolder.getLevel() + ".hit-commands")) {
                plugin.getServer().dispatchCommand(plugin.consoleSender, command
                        .replace("{world}", entity.getWorld().getName())
                        .replace("{x}", String.valueOf(entity.getLocation().getBlockX()))
                        .replace("{y}", String.valueOf(entity.getLocation().getBlockY()))
                        .replace("{z}", String.valueOf(entity.getLocation().getBlockZ())));
            }
            checkMessage(droneHolder.getHealth(), file.getLong(playerConnect.getGroup() + "." + droneHolder.getLevel() + ".health"), name, "health");
            return;
        }
        droneDeath(playerConnect, droneHolder, file);
        for (String command : file.getStringList("dead.player")) {
            plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", name));
        }
    }

    public void checkTarget(final Player player, final LivingEntity target, final FileConfiguration file, final Location end, final String path, final int ticks) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (target.isDead()) {
                checkShot(player, target, file, end, path, "killed");
            }
        }, ticks);
    }

    private void droneDeath(final PlayerConnect playerConnect, final DroneHolder droneHolder, final FileConfiguration file) {
        playerConnect.stopDrone();
        playerConnect.setLastActive("");
        droneHolder.setUnlocked(file.getInt("dead.unlocked"));
        if (file.getLong("dead.set-level") != 0) {
            droneHolder.setLevel(file.getInt("dead.set-level"));
        }
        if (!file.getBoolean("dead.ammo")) {
            droneHolder.setAmmo(0);
        }
        droneHolder.save();
    }
}