package me.MathiasMC.BattleDrones.api;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import me.MathiasMC.BattleDrones.managers.CalculateManager;
import me.MathiasMC.BattleDrones.utils.FileUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class DroneRegistry {

    private final BattleDrones plugin;

    private final FileUtils fileUtils;

    private final CalculateManager calculateManager;

    private final Plugin registeredPlugin;

    public final String droneName;

    public final String droneCategory;

    public DroneRegistry(final Plugin plugin, final String droneName, final String droneCategory) {
        this.droneName = droneName;
        this.droneCategory = droneCategory;
        this.registeredPlugin = plugin;
        this.plugin = BattleDrones.getInstance();
        this.fileUtils = this.plugin.getFileUtils();
        this.calculateManager = this.plugin.getCalculateManager();
    }

    public void register() {
        fileUtils.initialize(registeredPlugin, droneName, droneCategory);
        plugin.database.createDroneTable(droneName);
        plugin.droneRegistry.put(droneName, this);
        List<String> list = new ArrayList<>();
        if (plugin.category.containsKey(droneCategory)) {
            list = plugin.category.get(droneCategory);
        }
        list.add(droneName);
        plugin.category.put(droneCategory, list);
        fileUtils.loadDroneFiles();
        fileUtils.loadGUIFiles();
        plugin.drones.put(plugin.droneFiles.get(droneName).getString("name"), droneName);
        if (registeredPlugin == plugin) {
            plugin.getTextUtils().info(droneName + " registered in the category " + droneCategory);
        } else {
            plugin.getTextUtils().info(registeredPlugin.getName() + " " + droneName + " registered in the category " + droneCategory);
        }
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
        final double xDCustom = fileUtils.getDouble(file, path + ".position.x", 1.575);
        final double yDCustom = fileUtils.getDouble(file, path + ".position.y", 2);
        final double zDCustom = fileUtils.getDouble(file, path + ".position.z", 1.575);
        EulerAngle angle = null;
        if (file.contains(group + "." + drone_level + ".angle")) {
            angle = new EulerAngle(file.getDouble(group + "." + drone_level + ".angle"), 0, 0);
        }
        final EulerAngle finalAngle = angle;
        final double closeRange = fileUtils.getFollow(file, path, "follow.close.range");
        final double closeSpeed = fileUtils.getFollow(file, path, "follow.close.speed");
        final double middleSpeed = fileUtils.getFollow(file, path, "follow.middle.speed");
        final double farRange = fileUtils.getFollow(file, path, "follow.far.range");
        final double farSpeed = fileUtils.getFollow(file, path, "follow.far.speed");
        playerConnect.follow = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            LivingEntity target = plugin.drone_targets.get(uuid);
            final World world = player.getWorld();
            if (target != null && target.isDead() || target != null && world != target.getWorld()) {
                plugin.drone_targets.put(uuid, null);
                target = null;
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
                        int random = ThreadLocalRandom.current().nextInt(0, calculateManager.x.size() - 1);
                        final Location followPoint = targetLocation.add(calculateManager.x.get(random), calculateManager.y.get(random), calculateManager.z.get(random));
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

    public void ability(final Player player, final PlayerConnect playerConnect, final DroneHolder droneHolder) {

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

    public String onPlaceholderRequest(final Player player, final PlayerConnect playerConnect, final DroneHolder droneHolder, final String placeholder) {
        final FileConfiguration file = plugin.droneFiles.get(droneName);
        final String path = playerConnect.getGroup() + "." + droneHolder.getLevel();
        final String path_next = playerConnect.getGroup() + "." + (droneHolder.getLevel() + 1);
        if (placeholder.equals("mobs_current")) {
            if (droneHolder.getMonsters() == 1) {
                return fileUtils.language.getString("enabled");
            } else {
                return fileUtils.language.getString("disabled");
            }
        }
        if (placeholder.equals("animals_current")) {
            if (droneHolder.getAnimals() == 1) {
                return fileUtils.language.getString("enabled");
            } else {
                return fileUtils.language.getString("disabled");
            }
        }
        if (placeholder.equals("players_current")) {
            if (droneHolder.getPlayers() == 1) {
                return fileUtils.language.getString("enabled");
            } else {
                return fileUtils.language.getString("disabled");
            }
        }
        if (placeholder.equals("whitelist")) {
            return String.valueOf(droneHolder.getExclude().size());
        }
        if (placeholder.equals("health")) {
            return String.valueOf(droneHolder.getHealth());
        }
        if (placeholder.equals("health_bar")) {
            return calculateManager.getBar(droneHolder.getHealth(), file.getInt(path + ".health"), "health", "");
        }
        if (placeholder.equals("health_percentage")) {
            return String.valueOf(calculateManager.getPercent(droneHolder.getHealth(), file.getInt(path + ".health")));
        }
        if (placeholder.equals("ammo")) {
            return String.valueOf(droneHolder.getAmmo());
        }
        if (placeholder.equals("ammo_bar")) {
            return calculateManager.getBar(droneHolder.getAmmo(), file.getInt(path + ".max-ammo-slots") * 64, "ammo", "");
        }
        if (placeholder.equals("ammo_percentage")) {
            return String.valueOf(calculateManager.getPercent(droneHolder.getAmmo(), file.getInt(path + ".max-ammo-slots") * 64));
        }
        if (placeholder.equals("cost")) {
            return file.getString(playerConnect.getGroup() + "." + (droneHolder.getLevel() + 1) + ".cost");
        }
        if (placeholder.equals("max_ammo_slots")) {
            return file.getString(path + ".max-ammo-slots");
        }
        if (placeholder.equals("level")) {
            return String.valueOf(droneHolder.getLevel());
        }
        if (placeholder.equals("min_max")) {
            return file.getString(path + ".min") + "-" + file.getString(path + ".max");
        }
        if (placeholder.equals("shield_generator_damage")) {
            return calculateManager.getProcentFromDouble(file.getDouble(path + ".min")) + "-" + calculateManager.getProcentFromDouble(file.getDouble(path + ".max"));
        }
        if (placeholder.equals("range")) {
            return file.getString(playerConnect.getGroup() + "." + droneHolder.getLevel() + ".range");
        }
        if (placeholder.equals("firerate")) {
            return calculateManager.getFirerate(file.getDouble(playerConnect.getGroup() + "." + droneHolder.getLevel() + ".cooldown"));
        }
        if (placeholder.equals("cooldown")) {
            return file.getString(path + ".cooldown");
        }
        if (placeholder.equals("chance")) {
            return String.valueOf(calculateManager.getProcentFromDouble(file.getDouble(path + ".chance")));
        }
        if (placeholder.equals("radius")) {
            return file.getString(path + ".radius");
        }
        if (placeholder.equals("setfire_chance")) {
            return String.valueOf(calculateManager.getProcentFromDouble(file.getDouble(path + ".setfire-chance")));
        }
        if (placeholder.equals("explosion_chance")) {
            return String.valueOf(calculateManager.getProcentFromDouble(file.getDouble(path + ".explosion-chance")));
        }
        if (placeholder.equals("explosion_power")) {
            return file.getString(path + ".explosion-power");
        }
        if (placeholder.equals("burning_time")) {
            return file.getString(path + ".burning-time");
        }
        if (placeholder.equals("knockback")) {
            return file.getString(path + ".knockback");
        }
        if (placeholder.equals("rocket_speed")) {
            return file.getString(path + ".rocket-speed");
        }
        if (placeholder.equals("rocket_radius")) {
            return file.getString(path + ".rocket-radius");
        }
        if (placeholder.equals("rocket_time")) {
            return file.getString(path + ".rocket-time");
        }
        if (placeholder.equals("healing_health")) {
            return file.getString(path + ".healing.health");
        }
        if (placeholder.equals("healing_delay")) {
            return file.getString(path + ".healing.delay");
        }
        if (placeholder.equals("max_ammo_slots_next")) {
            return file.getString(path_next + ".max-ammo-slots");
        }
        if (placeholder.equals("level_next")) {
            return String.valueOf((droneHolder.getLevel() + 1));
        }
        if (placeholder.equals("min_max_next")) {
            return file.getString(path_next + ".min") + "-" + file.getString(path_next + ".max");
        }
        if (placeholder.equals("shield_generator_damage_next")) {
            return calculateManager.getProcentFromDouble(file.getDouble(path_next + ".min")) + "-" + calculateManager.getProcentFromDouble(file.getDouble(path_next + ".max"));
        }
        if (placeholder.equals("range_next")) {
            return file.getString(path_next + ".range");
        }
        if (placeholder.equals("firerate_next")) {
            return calculateManager.getFirerate(file.getDouble(path_next + ".cooldown"));
        }
        if (placeholder.equals("cooldown_next")) {
            return file.getString(path_next + ".cooldown");
        }
        if (placeholder.equals("chance_next")) {
            return String.valueOf(calculateManager.getProcentFromDouble(file.getDouble(path_next + ".chance")));
        }
        if (placeholder.equals("radius_next")) {
            return file.getString(path_next + ".radius");
        }
        if (placeholder.equals("setfire_chance_next")) {
            return String.valueOf(calculateManager.getProcentFromDouble(file.getDouble(path_next + ".setfire-chance")));
        }
        if (placeholder.equals("explosion_chance_next")) {
            return String.valueOf(calculateManager.getProcentFromDouble(file.getDouble(path_next + ".explosion-chance")));
        }
        if (placeholder.equals("explosion_power_next")) {
            return file.getString(path_next + ".explosion-power");
        }
        if (placeholder.equals("burning_time_next")) {
            return file.getString(path_next + ".burning-time");
        }
        if (placeholder.equals("knockback_next")) {
            return file.getString(path_next + ".knockback");
        }
        if (placeholder.equals("rocket_speed_next")) {
            return file.getString(path_next + ".rocket-speed");
        }
        if (placeholder.equals("rocket_radius_next")) {
            return file.getString(path_next + ".rocket-radius");
        }
        if (placeholder.equals("rocket_time_next")) {
            return file.getString(path_next + ".rocket-time");
        }
        if (placeholder.equals("healing_health_next")) {
            return file.getString(path_next + ".healing.health");
        }
        if (placeholder.equals("healing_delay_next")) {
            return file.getString(path_next + ".healing.delay");
        }
        if (placeholder.equals("teleport_ammo")) {
            return String.valueOf(file.getInt(path + ".ammo"));
        }
        if (placeholder.equals("teleport_distance")) {
            return String.valueOf(file.getDouble(path + ".teleport"));
        }
        if (placeholder.equals("teleport_ammo_next")) {
            return String.valueOf(file.getInt(path_next + ".ammo"));
        }
        if (placeholder.equals("teleport_distance_next")) {
            return String.valueOf(file.getDouble(path_next + ".teleport"));
        }
        return null;
    }
}
