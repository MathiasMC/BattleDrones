package me.MathiasMC.BattleDrones.managers;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.api.Type;
import me.MathiasMC.BattleDrones.api.events.DroneDeathEvent;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import me.MathiasMC.BattleDrones.api.events.DroneDamageEvent;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class DroneManager {

    private final BattleDrones plugin;

    public DroneManager(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    public void checkMessage(final long amount, final long maxAmount, final Player player, final String type) {
        final long left = Double.valueOf(Math.floor(amount * (100D / maxAmount))).longValue();
        if (left != 0L) {
            if (left == Double.valueOf(Math.floor((amount + 1) * (100D / maxAmount))).longValue()) {
                return;
            }
        } else if (amount != 1) {
            return;
        }
        runCommands(player, plugin.getFileUtils().config, "low-" + type + "." + left);
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
                droneDeath(player, playerConnect, droneHolder, file, Type.WEAR);
                runCommands(player, file, "dead.wear");
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

    public void runCommands(final Player player, FileConfiguration file, final String path) {
        if (!file.contains(path)) {
            return;
        }
        for (String command : file.getStringList(path)) {
            dispatchCommand(player, command);
        }
    }

    public void runCommands(final Player player, final List<String> list) {
        for (String command : list) {
            dispatchCommand(player, command);
        }
    }

    private void dispatchCommand(final Player player, final String command) {
        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', plugin.getPlaceholderManager().replacePlaceholders(player, command)));
    }

    public void damage(final Player damager, final String key, final ArmorStand entity) {
        final PlayerConnect playerConnect = plugin.getPlayerConnect(key);
        if (!playerConnect.isActive()) {
            return;
        }
        final DroneHolder droneHolder = plugin.getDroneHolder(key, playerConnect.getActive());
        final String path = playerConnect.getGroup() + "." + droneHolder.getLevel();
        final DroneDamageEvent droneDamageEvent = new DroneDamageEvent(damager, playerConnect, droneHolder);
        plugin.getServer().getPluginManager().callEvent(droneDamageEvent);
        if (droneDamageEvent.isCancelled()) {
            return;
        }
        final FileConfiguration file = plugin.droneFiles.get(playerConnect.getActive());
        if (!plugin.getSupport().canTarget(damager, entity, file, path + ".worldguard.damage")) {
            return;
        }
        final OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(UUID.fromString(key));
        if (!offlinePlayer.isOnline()) {
            return;
        }
        final Player player = (Player) offlinePlayer;
        if (droneHolder.getHealth() - 1 > 0) {
            droneHolder.setHealth(droneHolder.getHealth() - 1);
            runCommands(player, file, path + ".hit-commands");
            checkMessage(droneHolder.getHealth(), file.getLong(path + ".health"), player, "health");
            return;
        }
        runCommands(player, file, "dead.player");
        droneDeath(player, playerConnect, droneHolder, file, Type.PLAYER);
    }

    public void checkTarget(final Player player, final LivingEntity target, final FileConfiguration file, final Location end, final String path, final int ticks) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (target.isDead()) {
                checkShot(player, target, file, end, path, "killed");
            }
        }, ticks);
    }

    private void droneDeath(final Player player, final PlayerConnect playerConnect, final DroneHolder droneHolder, final FileConfiguration file, final Type type) {
        final DroneDeathEvent droneDeathEvent = new DroneDeathEvent(player, playerConnect, droneHolder);
        droneDeathEvent.setType(type);
        plugin.getServer().getPluginManager().callEvent(droneDeathEvent);
        if (droneDeathEvent.isCancelled()) {
            return;
        }
        playerConnect.stopDrone(true, true);
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

    public long cleanUP(final Boolean projectiles, final boolean stopDrone) {
        long amount = 0;
        for (World world : plugin.getServer().getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof ArmorStand) {
                    final ArmorStand armorStand = (ArmorStand) entity;
                    final String key = armorStand.getPersistentDataContainer().get(plugin.droneKey, PersistentDataType.STRING);
                    final PlayerConnect playerConnect = plugin.getPlayerConnect(key);
                    if (!playerConnect.isActive()) {
                        amount = amount + getRemoveAmount(projectiles, key, armorStand);
                    } else if (stopDrone) {
                        amount = amount + getRemoveAmount(projectiles, key, armorStand);
                        if (key != null) {
                            final OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(UUID.fromString(key));
                            if (offlinePlayer.isOnline() && offlinePlayer.getName() != null) {
                                final DroneHolder droneHolder = plugin.getDroneHolder(key, playerConnect.getActive());
                                for (String message : plugin.getFileUtils().language.getStringList("cleanup.drone")) {
                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{target}", offlinePlayer.getName()).replace("{drone}", plugin.getPlaceholderManager().getActiveDrone(droneHolder.getDrone())).replace("{amount}", String.valueOf(amount))));
                                }
                            }
                        }
                        playerConnect.stopDrone(true, true);
                    }
                }
            }
        }
        return amount;
    }

    private long getRemoveAmount(final Boolean projectiles, final String key, final ArmorStand armorStand) {
        long amount = 0;
        if (projectiles == null) {
            if (key != null) {
                armorStand.remove();
                amount++;
            }
            if (removeProjectile(armorStand)) {
                amount++;
            }
        } else if (projectiles) {
            if (removeProjectile(armorStand)) {
                amount++;
            }
        } else {
            if (key != null) {
                armorStand.remove();
                amount++;
            }
        }
        return amount;
    }

    private boolean removeProjectile(final ArmorStand armorStand) {
        final String keyString = armorStand.getPersistentDataContainer().get(plugin.projectileKey, PersistentDataType.STRING);
        if (keyString != null) {
            armorStand.remove();
            return true;
        }
        return false;
    }
}