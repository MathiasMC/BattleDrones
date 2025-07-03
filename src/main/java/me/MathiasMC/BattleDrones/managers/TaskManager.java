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

    public final ArrayList<Double> x = new ArrayList<>();
    public final ArrayList<Double> y = new ArrayList<>();
    public final ArrayList<Double> z = new ArrayList<>();

    public TaskManager(final BattleDrones plugin) {
        this.plugin = plugin;
        for (String add : plugin.getFileUtils().config.getConfigurationSection("follow").getKeys(false)) {
            final int points = plugin.getFileUtils().config.getInt("follow." + add + ".distance");
            final double radius = plugin.getFileUtils().config.getDouble("follow." + add + ".radius");
            final double yoffset = plugin.getFileUtils().config.getDouble("follow." + add + ".y-offset");
            for (int i = 0; i < points; i++) {
                final double angle = 2 * Math.PI * i / points;
                x.add((double) Math.round(radius * Math.sin(angle)));
                y.add(yoffset);
                z.add((double) Math.round(radius * Math.cos(angle)));
            }
        }
    }

    public void find(final Player player, final PlayerConnect playerConnect, final DroneHolder droneHolder) {
        String uuid = player.getUniqueId().toString();
        FileConfiguration file = plugin.droneFiles.get(droneHolder.getDrone());
        long droneLevel = droneHolder.getLevel();
        String path = playerConnect.getGroup() + "." + droneLevel;

        int monstersTemp = droneHolder.getMonsters();
        int animalsTemp = droneHolder.getAnimals();
        int playersTemp = droneHolder.getPlayers();

        List<String> exclude = new ArrayList<>(droneHolder.getExclude());
        exclude.add(player.getName().toLowerCase());

        boolean reverseExcludeTemp = false;
        boolean hpCheckTemp = false;

        String droneName = droneHolder.getDrone();
        if (droneName.equalsIgnoreCase("shield_generator")) {
            animalsTemp = 0;
        } else if (droneName.equalsIgnoreCase("healing")) {
            playersTemp = 1;
            reverseExcludeTemp = true;
            hpCheckTemp = true;
        }

        double radius = file.getDouble(path + ".range");
        List<String> entityList = file.getStringList(path + ".exclude");
        ArmorStand head = playerConnect.head;


        final int monsters = monstersTemp;
        final int animals = animalsTemp;
        final int players = playersTemp;
        final boolean reverseExclude = reverseExcludeTemp;
        final boolean hpCheck = hpCheckTemp;
        playerConnect.find = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            LivingEntity target = plugin.drone_targets.get(uuid);

            if (target == null) {
                target = plugin.getEntityManager().getClosestLivingEntity(
                        head, radius, monsters, animals, players, entityList, exclude, reverseExclude, hpCheck);
            }

            if (target == null) {
                plugin.drone_targets.put(uuid, null);
                return;
            }

            String type = plugin.getEntityManager().isMonster(target) ? "monsters"
                    : plugin.getEntityManager().isAnimal(target) ? "animals"
                    : "players";

            if (!plugin.getSupport().canTarget(target, file, playerConnect.getGroup() + "." + droneLevel + ".worldguard." + type)) {
                plugin.drone_targets.put(uuid, null);
                return;
            }

            if (!playerConnect.isAutomatic()) {
                return;
            }

            if (head.getLocation().distance(target.getLocation()) > radius) {
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

    public void follow(Player player,
                       PlayerConnect playerConnect,
                       DroneHolder droneHolder
    ) {
        String uuid = player.getUniqueId().toString();
        String group = playerConnect.getGroup();
        long level = droneHolder.getLevel();
        String path = group + "." + level;

        FileConfiguration file = plugin.droneFiles.get(droneHolder.getDrone());
        ArmorStand head = playerConnect.head;
        ArmorStand name = playerConnect.name;
        EulerAngle defaultPose = new EulerAngle(0, 0, 0);

        double xOffset = file.getDouble(path + ".position-x", 1.575);
        double yOffset = file.getDouble(path + ".position-y", 2);
        double zOffset = file.getDouble(path + ".position-z", 1.575);

        EulerAngle customAngle = file.contains(path + ".angle")
                ? new EulerAngle(file.getDouble(path + ".angle"), 0, 0)
                : null;

        double closeRange = file.getDouble(path + ".follow-close-range");
        double closeSpeed = file.getDouble(path + ".follow-close-speed");
        double middleSpeed = file.getDouble(path + ".follow-middle-speed");
        double farRange = file.getDouble(path + ".follow-far-range");
        double farSpeed = file.getDouble(path + ".follow-far-speed");

        playerConnect.follow = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {

            LivingEntity target = plugin.drone_targets.get(uuid);
            World world = player.getWorld();
            if (target != null && target.isDead() || target != null && !target.getWorld().equals(world)) {
                plugin.drone_targets.put(uuid, null);
                plugin.drone_follow.remove(uuid);
                target = null;
            }

            Location baseLocation = player.getLocation().add(0, yOffset, 0);
            Location headLocation = head.getLocation();
            Location moveTarget = headLocation;
            Vector facingDirection = null;

            if (!plugin.park.contains(uuid)) {
                float baseYaw = baseLocation.getYaw();
                facingDirection = baseLocation.getDirection();

                double x = Math.sin(-0.0175 * baseYaw + xOffset) + baseLocation.getX();
                double z = Math.cos(-0.0175 * baseYaw + zOffset) + baseLocation.getZ();
                moveTarget = new Location(world, x, baseLocation.getY(), z);
            }

            if (target != null) {
                Location targetLocation = target.getLocation();
                Vector moveVector = headLocation.toVector();
                Vector destination;

                if (plugin.drone_follow.contains(uuid)) {
                    boolean needNewPoint = playerConnect.dronePoint == null
                            || playerConnect.dronePoint.getWorld() == null
                            || !playerConnect.dronePoint.getWorld().equals(world)
                            || headLocation.distance(playerConnect.dronePoint) < 1;

                    if (needNewPoint) {
                        int size = x.size();
                        if (size == 0) return;
                        int i = ThreadLocalRandom.current().nextInt(size);
                        Vector offset = new Vector(
                                x.get(i),
                                y.get(i),
                                z.get(i)
                        );
                        Location newPoint = targetLocation.clone().add(offset);

                        if (world.rayTraceBlocks(headLocation, offset.normalize(), headLocation.distance(newPoint)) == null) {
                            playerConnect.dronePoint = newPoint;
                        } else return;
                    }

                    double distance = headLocation.distance(targetLocation);
                    double speed = (distance < closeRange) ? closeSpeed
                            : (distance > farRange) ? farSpeed
                            : middleSpeed;
                    destination = playerConnect.dronePoint.toVector().subtract(moveVector).normalize().multiply(speed).add(moveVector);
                    moveTarget = destination.toLocation(world);
                }

                if (customAngle != null) {
                    head.setHeadPose(customAngle);
                } else {
                    plugin.getEntityManager().lookAT(head, targetLocation.clone());
                }

                Vector faceDir = targetLocation.toVector().subtract(headLocation.toVector()).normalize();
                head.teleport(moveTarget.setDirection(faceDir));
                plugin.getEntityManager().setCustomName(head, name, level, group, file, "target", player);

            } else {
                head.setHeadPose(defaultPose);
                if (facingDirection != null) {
                    head.teleport(moveTarget.setDirection(facingDirection));
                } else {
                    head.teleport(moveTarget);
                }
                plugin.getEntityManager().setCustomName(head, name, level, group, file, "searching", player);
            }
            if (name != null) {
                name.teleport(head.getLocation().add(0, 0.3, 0));
            }

        }, 5, 1).getTaskId();
    }

    public void healing(Player player,
                        PlayerConnect playerConnect,
                        DroneHolder droneHolder
    ) {
        String path = playerConnect.getGroup() + "." + droneHolder.getLevel();
        FileConfiguration file = plugin.droneFiles.get(droneHolder.getDrone());

        int healingAmount = file.getInt(path + ".healing-health");
        long delaySeconds = file.getLong(path + ".healing-delay");

        if (delaySeconds > 0 && healingAmount > 0) {
            long delayTicks = delaySeconds * 20;

            playerConnect.healing = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
                if (playerConnect.isHealing()) {
                    int currentHealth = droneHolder.getHealth();
                    int maxHealth = file.getInt(path + ".health");

                    int newHealth = currentHealth + healingAmount;

                    if (newHealth <= maxHealth) {
                        droneHolder.setHealth(newHealth);
                    }
                }
            }, delayTicks, delayTicks).getTaskId();
        }
    }
}
