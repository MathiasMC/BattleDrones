package me.MathiasMC.BattleDrones.drones;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.*;

public class Rocket {

    private final BattleDrones plugin;

    public Rocket(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    public final HashSet<ArmorStand> rockets = new HashSet<>();

    public void shot(final Player player) {
        final String drone = "rocket";
        final String uuid = player.getUniqueId().toString();
        final PlayerConnect playerConnect = plugin.get(uuid);
        final DroneHolder droneHolder = plugin.getDroneHolder(uuid, drone);
        final String group = playerConnect.getGroup();
        final FileConfiguration rocket = plugin.droneFiles.get(drone);
        final String path = group + "." + droneHolder.getLevel() + ".";
        playerConnect.ShootTaskID = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            final LivingEntity target = plugin.drone_targets.get(uuid);
            if (target != null) {
                if (droneHolder.getAmmo() > 0) {
                    final ArmorStand armorStand = playerConnect.head;
                    if (armorStand.hasLineOfSight(target)) {
                        ArmorStand rock = plugin.armorStandManager.getArmorStand(armorStand.getLocation(), false, true);
                        rock.setHelmet(plugin.drone_heads.get(rocket.getString(path + "rocket-head")));
                        rockets.add(rock);
                        line(armorStand.getLocation(), target.getLocation(), rocket.getDouble(path + "rocket-speed"), rock, target, rocket, path, armorStand.getHeadPose());
                        BattleDrones.call.droneManager.checkAmmo(rocket, path, droneHolder.getAmmo(), player.getName());
                        BattleDrones.call.droneManager.checkShot(target, rocket, armorStand.getLocation(), path, "run");
                        BattleDrones.call.droneManager.takeAmmo(playerConnect, droneHolder, rocket, path, player.getName());
                    }
                }
                playerConnect.setRegen(false);
            } else {
                playerConnect.setRegen(true);
            }
        }, 0, rocket.getLong(path + "cooldown")).getTaskId();
    }

    private void line(Location start, Location end, double space, ArmorStand armorStand, LivingEntity target, FileConfiguration file, String path, EulerAngle eulerAngle) {
        new BukkitRunnable() {
            final Vector p1 = start.toVector();
            final Vector vector = end.toVector().clone().subtract(p1).normalize().multiply(space);
            double length = 0;
            int timer = 0;
            int sphereTime = 0;
            final ArrayList<String> exclude = new ArrayList<>();
            @Override
            public void run() {
                p1.add(vector);
                armorStand.setHeadPose(eulerAngle);
                armorStand.teleport(p1.toLocation(start.getWorld()));
                length += space;
                if (timer > (file.getLong(path + "rocket-time") * 20) || plugin.armorStandManager.getEntityAround(armorStand, 1,  1, 1, 1, exclude, false).size() > 0 || armorStand.getTargetBlock(null, 1).getType() != Material.AIR) {
                    this.cancel();
                    ArrayList<LivingEntity> livingEntities = plugin.armorStandManager.getEntityAround(armorStand, file.getInt(path + "rocket-radius"),  1, 1, 1, exclude, false);
                    for (LivingEntity livingEntity : livingEntities) {
                        plugin.calculateManager.damage(livingEntity, plugin.randomDouble(file.getDouble(path + "min"), file.getDouble(path + "max")));
                    }
                    if (file.getBoolean(path + "rocket-explosion")) {
                        start.getWorld().createExplosion(p1.toLocation(start.getWorld()), file.getInt(path + "rocket-explosion-power"), file.getBoolean(path + "rocket-explosion-fire"), file.getBoolean(path + "rocket-explosion-block"));
                    }
                    int points = file.getInt(path + "particle-explode.points");
                    double radius = file.getDouble(path + "particle-explode.radius");
                    Location location = armorStand.getLocation().add(file.getInt(path + "particle-explode.x-offset"), file.getInt(path + "particle-explode.y-offset"), file.getInt(path + "particle-explode.z-offset"));
                    BattleDrones.call.droneManager.checkShot(target, file, armorStand.getLocation(), path, "explode");
                    for (int i = 0; i < points; i++) {
                        double angle = 2 * Math.PI * i / points;
                        Location point = location.clone().add(radius * Math.sin(angle), 0.0d, radius * Math.cos(angle));
                        start.getWorld().spawnParticle(Particle.valueOf(file.getString(path + "particle-explode.particle")), randomLocation(point, file.getInt(path + "particle-explode.random-radius")), file.getInt(path + "particle-explode.amount"), 0, 0, 0, file.getDouble(path + "particle-explode.speed"));
                    }
                    armorStand.remove();
                }
                timer++;
                if (file.contains(path + "particle-circle")) {
                sphereTime++;
                if (sphereTime > file.getInt(path + "particle-circle.outside.tick")) {
                    Location location = armorStand.getLocation().add(0, 0.6, 0);
                    if (file.contains(path + "particle-circle.outside")) {
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                            plugin.calculateManager.sphere(location,
                                    file.getDouble(path + "particle-circle.outside.radius"),
                                    file.getInt(path + "particle-circle.outside.rows"),
                                    file.getInt(path + "particle-circle.outside.rgb.r"),
                                    file.getInt(path + "particle-circle.outside.rgb.g"),
                                    file.getInt(path + "particle-circle.outside.rgb.b"),
                                    file.getInt(path + "particle-circle.outside.size"),
                                    file.getInt(path + "particle-circle.outside.amount"));
                        }, file.getInt(path + "particle-circle.outside.delay"));
                    }
                    if (file.contains(path + "particle-circle.inside")) {
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                            plugin.calculateManager.sphere(location,
                                    file.getDouble(path + "particle-circle.inside.radius"),
                                    file.getInt(path + "particle-circle.inside.rows"),
                                    file.getInt(path + "particle-circle.inside.rgb.r"),
                                    file.getInt(path + "particle-circle.inside.rgb.g"),
                                    file.getInt(path + "particle-circle.inside.rgb.b"),
                                    file.getInt(path + "particle-circle.inside.size"),
                                    file.getInt(path + "particle-circle.inside.amount"));
                        }, file.getInt(path + "particle-circle.inside.delay"));
                    }
                    sphereTime = 0;
                }
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private Location randomLocation(Location location, double radius) {
        Random rand = new Random();
        double angle = rand.nextDouble() * 360;
        double x = location.getX() + (rand.nextDouble() * radius * Math.cos(Math.toRadians(angle)));
        double z = location.getZ() + (rand.nextDouble() * radius * Math.sin(Math.toRadians(angle)));
        return new Location(location.getWorld(), x, location.getY(), z);
    }
}