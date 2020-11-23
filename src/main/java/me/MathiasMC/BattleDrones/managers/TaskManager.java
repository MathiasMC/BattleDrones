package me.MathiasMC.BattleDrones.managers;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class TaskManager {

    private final BattleDrones plugin;

    public TaskManager(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    public void find(final Player player, final PlayerConnect playerConnect, final DroneHolder droneHolder) {
        final String uuid = player.getUniqueId().toString();
        final FileConfiguration file = plugin.droneFiles.get(droneHolder.getDrone());
        final long drone_level = droneHolder.getLevel();
        int TEMPmonsters = droneHolder.getMonsters();
        int TEMPanimals = droneHolder.getAnimals();
        int TEMPplayers = droneHolder.getPlayers();
        boolean TEMPreverseExclude;
        boolean TEMPhpCheck;
        final List<String> exclude = new ArrayList<>(droneHolder.getExclude());
        exclude.add(player.getName().toLowerCase());
        TEMPreverseExclude = false;
        TEMPhpCheck = false;
        if (droneHolder.getDrone().equalsIgnoreCase("shield_generator")) {
            TEMPanimals = 0;
        } else if (droneHolder.getDrone().equalsIgnoreCase("healing")) {
            TEMPplayers = 1;
            TEMPreverseExclude = true;
            TEMPhpCheck = true;
        }
        final boolean reverseExclude = TEMPreverseExclude;
        final boolean hpCheck = TEMPhpCheck;
        final ArmorStand head = playerConnect.head;
        final String path = playerConnect.getGroup() + "." + drone_level;
        final double radius = file.getDouble(path + ".range");
        final int animals = TEMPanimals;
        final int players = TEMPplayers;
        final int monsters = TEMPmonsters;
        final List<String> entityList = file.getStringList(path + ".exclude");
        playerConnect.find = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            LivingEntity target;
            if (plugin.drone_targets.get(uuid) != null) {
                target = plugin.drone_targets.get(uuid);
            } else {
                target = plugin.getEntityManager().getClosestLivingEntity(head, radius, monsters, animals, players, entityList, exclude, reverseExclude, hpCheck);
            }
            String type = "players";
            if (plugin.getEntityManager().isMonster(target)) {
                type = "monsters";
            } else if (plugin.getEntityManager().isAnimal(target)) {
                type = "animals";
            }
            if (!plugin.getSupport().canTarget(player, target, file, playerConnect.getGroup() + "." + drone_level + ".worldguard." + type)) {
                plugin.drone_targets.put(uuid, null);
                return;
            }
            if (target == null) {
                return;
            }
            if (!playerConnect.isAutomatic()) {
                return;
            } else if (head.getLocation().distance(target.getLocation()) > radius) {
                plugin.drone_targets.put(uuid, null);
                return;
            }
            if (!head.hasLineOfSight(target)) {
                return;
            }
            if (plugin.drone_targets.get(uuid) != target) {
                plugin.drone_targets.put(uuid, target);
            }
        }, 5, file.getInt(path + ".find-target")).getTaskId();
    }

    public void follow(final Player player, final PlayerConnect playerConnect, final DroneHolder droneHolder) {
        final String uuid = player.getUniqueId().toString();
        final FileConfiguration file = plugin.droneFiles.get(droneHolder.getDrone());
        final long drone_level = droneHolder.getLevel();
        final String group = playerConnect.getGroup();
        final ArmorStand head = playerConnect.head;
        final ArmorStand name = playerConnect.name;
        final EulerAngle eulerAngle = new EulerAngle(0, 0, 0);
        final String path = group + "." + drone_level;
        final double xDCustom = plugin.getFileUtils().getDouble(file, path + ".position.x", 1.575);
        final double yDCustom = plugin.getFileUtils().getDouble(file, path + ".position.y", 2);
        final double zDCustom = plugin.getFileUtils().getDouble(file, path + ".position.z", 1.575);
        EulerAngle angle = null;
        if (file.contains(group + "." + drone_level + ".angle")) {
            angle = new EulerAngle(file.getDouble(group + "." + drone_level + ".angle"), 0, 0);
        }
        final EulerAngle finalAngle = angle;
        final double closeRange = plugin.getFileUtils().getFollow(file, path, "follow.close.range");
        final double closeSpeed = plugin.getFileUtils().getFollow(file, path, "follow.close.speed");
        final double middleSpeed = plugin.getFileUtils().getFollow(file, path, "follow.middle.speed");
        final double farRange = plugin.getFileUtils().getFollow(file, path, "follow.far.range");
        final double farSpeed = plugin.getFileUtils().getFollow(file, path, "follow.far.speed");
        playerConnect.follow = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            LivingEntity target = plugin.drone_targets.get(uuid);
            final World world = player.getWorld();
            if (target != null && target.isDead() || target != null && world != target.getWorld()) {
                plugin.drone_targets.put(uuid, null);
                target = null;
                plugin.drone_follow.remove(uuid);
            }
            Vector direction = null;
            Location tp = head.getLocation();
            if (!plugin.park.contains(uuid)) {
                final Location location = player.getLocation().add(0, yDCustom, 0);
                direction = location.getDirection();
                float yaw = location.getYaw();
                final double xD = Math.sin(-0.0175 * yaw + xDCustom) + location.getX();
                final double zD = Math.cos(-0.0175 * yaw + zDCustom) + location.getZ();
                tp = new Location(world, xD, location.getY(), zD);
            }
            if (target != null) {
                final Location headLocation = head.getLocation();
                final Vector tpVector = headLocation.toVector();
                if (plugin.drone_follow.contains(uuid)) {
                    final Location targetLocation = target.getLocation();
                    World pointWorld = null;
                    if (playerConnect.dronePoint != null) {
                        pointWorld = playerConnect.dronePoint.getWorld();
                    }
                    if (playerConnect.dronePoint == null || pointWorld != null && pointWorld != world || headLocation.distance(playerConnect.dronePoint) < 1) {
                        int random = ThreadLocalRandom.current().nextInt(0, plugin.getCalculateManager().x.size() - 1);
                        final Location followPoint = targetLocation.add(plugin.getCalculateManager().x.get(random), plugin.getCalculateManager().y.get(random), plugin.getCalculateManager().z.get(random));
                        if (world.rayTraceBlocks(headLocation, followPoint.toVector().subtract(headLocation.toVector()).normalize(), headLocation.distance(followPoint)) == null) {
                            playerConnect.dronePoint = followPoint;
                        } else {
                            return;
                        }
                    }
                    final double distance = targetLocation.distance(headLocation);
                    if (distance < closeRange) {
                        tpVector.add(playerConnect.dronePoint.toVector().subtract(tpVector).normalize().multiply(closeSpeed));
                    } else if (distance > farRange) {
                        tpVector.add(playerConnect.dronePoint.toVector().subtract(tpVector).normalize().multiply(farSpeed));
                    } else {
                        tpVector.add(playerConnect.dronePoint.toVector().subtract(tpVector).normalize().multiply(middleSpeed));
                    }
                    tp = tpVector.toLocation(world);
                }
                if (finalAngle != null) {
                    head.setHeadPose(finalAngle);
                }
                final Location targetLocation = target.getLocation();
                if (finalAngle == null) {
                    plugin.getEntityManager().lookAT(head, targetLocation.clone());
                }
                head.teleport(tp.setDirection(targetLocation.toVector().subtract(tpVector).normalize()));
                plugin.getEntityManager().setCustomName(head, name, drone_level, group, file, "target", player);
            } else {
                head.setHeadPose(eulerAngle);
                if (direction != null) {
                    head.teleport(tp.setDirection(direction));
                } else {
                    head.teleport(tp);
                }
                plugin.getEntityManager().setCustomName(head, name, drone_level, group, file, "searching", player);
            }
            if (name == null) {
                return;
            }
            name.teleport(tp.add(0, 0.3, 0));
        }, 5, 1).getTaskId();
    }

    public void healing(final Player player, final PlayerConnect playerConnect, final DroneHolder droneHolder) {
        final String path = playerConnect.getGroup() + "." + droneHolder.getLevel();
        final FileConfiguration file = plugin.droneFiles.get(droneHolder.getDrone());
        final int health = file.getInt(path + ".healing.health");
        if (file.getLong(path + ".healing.delay") != 0) {
            playerConnect.healing = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
                if (playerConnect.isHealing()) {
                    final int add_health = droneHolder.getHealth() + health;
                    if (file.getInt(path + ".health") >= add_health) {
                        droneHolder.setHealth(add_health);
                    }
                }
            }, file.getLong(path + ".healing.delay") * 20, file.getLong(path + ".healing.delay") * 20).getTaskId();
        }
    }
}
