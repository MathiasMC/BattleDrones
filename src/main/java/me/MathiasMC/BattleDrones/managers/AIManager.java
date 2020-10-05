package me.MathiasMC.BattleDrones.managers;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.List;

public class AIManager {

    private final BattleDrones plugin;

    public AIManager(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    public void defaultAI(final Player player, final PlayerConnect playerConnect, final FileConfiguration file, final long drone_level, final int monsters, final int animals, final int players, final List<String> exclude, final boolean reverseExclude, final boolean hpCheck, final boolean lookAI) {
        final String uuid = player.getUniqueId().toString();
        final String group = playerConnect.getGroup();
        final ArmorStand head = playerConnect.head;
        final ArmorStand name = playerConnect.name;
        final EulerAngle eulerAngle = new EulerAngle(0, 0, 0);
        double xDCustom = 1.575;
        double yDCustom = 2;
        double zDCustom = 1.575;
        if (file.contains(group + "." + drone_level + ".position.x")) {
            xDCustom = file.getDouble(group + "." + drone_level + ".position.x");
        }
        if (file.contains(group + "." + drone_level + ".position.y")) {
            yDCustom = file.getDouble(group + "." + drone_level + ".position.y");
        }
        if (file.contains(group + "." + drone_level + ".position.z")) {
            zDCustom = file.getDouble(group + "." + drone_level + ".position.z");
        }
        EulerAngle angle = null;
        if (file.contains(group + "." + drone_level + ".angle")) {
            angle = new EulerAngle(file.getDouble(group + "." + drone_level + ".angle"), 0, 0);
        }
        final EulerAngle finalAngle = angle;
        final double finalXDCustom = xDCustom;
        final double finalYDCustom = yDCustom;
        final double finalZDCustom = zDCustom;
        final double radius = file.getDouble(group + "." + drone_level + ".range");
        int findTarget = 1;
        if (file.contains(group + "." + drone_level + ".find-target")) {
            findTarget = file.getInt(group + "." + drone_level + ".find-target");
        }
        playerConnect.AIfindTargetID = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            LivingEntity target = plugin.armorStandManager.getClose(player, radius, monsters, animals, players, exclude, reverseExclude, hpCheck);
            if (target != null && !head.hasLineOfSight(target)) {
                target = null;
            }
            if (plugin.support.worldGuard != null && plugin.config.get.getBoolean("worldguard.use")) {
                String type = "players";
                if (plugin.droneManager.isMonster(target)) {
                    type = "monsters";
                } else if (plugin.droneManager.isAnimal(target)) {
                    type = "animals";
                }
                if (plugin.config.get.contains("worldguard." + playerConnect.getActive() + "." + drone_level + "." + type) && plugin.support.worldGuard.canTarget(player, plugin.config.get.getStringList("worldguard." + playerConnect.getActive() + "." + drone_level + "." + type))) {
                    if (plugin.drone_targets.get(uuid) != null) {
                        plugin.drone_targets.put(uuid, null);
                    }
                    return;
                }
            }
            if (plugin.drone_targets.get(uuid) != target) {
                if (plugin.support.canTarget(player, target)) {
                    plugin.drone_targets.put(uuid, target);
                }
            }
        }, findTarget, findTarget).getTaskId();
        playerConnect.AItaskID = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            LivingEntity target = plugin.drone_targets.get(uuid);
            if (target != null && target.isDead()) {
                plugin.drone_targets.put(uuid, null);
                target = null;
            }
            if (!plugin.park.contains(uuid)) {
                final Location location = player.getLocation().add(0, finalYDCustom, 0);
                float yaw = location.getYaw();
                final Vector direction = location.getDirection();
                final double xD = Math.sin(-0.0175 * yaw + finalXDCustom) + location.getX();
                final double zD = Math.cos(-0.0175 * yaw + finalZDCustom) + location.getZ();
                final Location tp = new Location(player.getWorld(), xD, location.getY(), zD);
                if (target != null) {
                    if (finalAngle != null) {
                        head.setHeadPose(finalAngle);
                    }
                    if (lookAI) {
                        final Location targetLocation = target.getLocation();
                        if (finalAngle == null) {
                            plugin.armorStandManager.lookAT(head, targetLocation.clone());
                        }
                        head.teleport(tp.setDirection(targetLocation.toVector().subtract(head.getLocation().toVector()).normalize()));
                    } else {
                        head.teleport(tp.setDirection(direction));
                    }
                    plugin.armorStandManager.setCustomName(head, name, drone_level, group, file, "target", player);
                } else {
                    head.setHeadPose(eulerAngle);
                    head.teleport(tp.setDirection(direction));
                    plugin.armorStandManager.setCustomName(head, name, drone_level, group, file, "searching", player);
                }
                if (name == null) {
                    return;
                }
                name.teleport(tp.add(0, 0.3, 0));
            } else {
                final Location tp = head.getLocation();
                if (target != null) {
                    if (finalAngle != null) {
                        head.setHeadPose(finalAngle);
                    }
                    if (lookAI) {
                        final Location targetLocation = target.getLocation();
                        if (finalAngle == null) {
                            plugin.armorStandManager.lookAT(head, targetLocation.clone());
                        }
                        head.teleport(tp.setDirection(targetLocation.toVector().subtract(head.getLocation().toVector()).normalize()));
                    } else {
                        head.teleport(tp);
                    }
                    plugin.armorStandManager.setCustomName(head, name, drone_level, group, file, "target", player);
                } else {
                    head.setHeadPose(eulerAngle);
                    head.teleport(tp);
                    plugin.armorStandManager.setCustomName(head, name, drone_level, group, file, "searching", player);
                }
                if (name == null) {
                    return;
                }
                name.teleport(tp.clone().add(0, 0.3, 0));
            }
        }, 5, 1).getTaskId();
    }
}