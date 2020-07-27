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
        final ArmorStand armorStand = playerConnect.head;
        final FileConfiguration particleFile = plugin.particles.get;
        final String customParticle_1 = rocket.getString(path + "particle.1");
        final int delay_1 = particleFile.getInt(customParticle_1 + ".delay");
        final int size_1 = particleFile.getInt(customParticle_1 + ".size");
        final int amount_1 = particleFile.getInt(customParticle_1 + ".amount");
        final int r_1 = particleFile.getInt(customParticle_1 + ".rgb.r");
        final int g_1 = particleFile.getInt(customParticle_1 + ".rgb.g");
        final int b_1 = particleFile.getInt(customParticle_1 + ".rgb.b");
        final double yOffset_1 = particleFile.getDouble(customParticle_1 + ".y-offset");
        final String particleType_1 = particleFile.getString(customParticle_1 + ".particle");
        final String customParticle_2 = rocket.getString(path + "particle.2");
        final int delay_2 = particleFile.getInt(customParticle_2 + ".delay");
        final int size_2 = particleFile.getInt(customParticle_2 + ".size");
        final int amount_2 = particleFile.getInt(customParticle_2 + ".amount");
        final int r_2 = particleFile.getInt(customParticle_2 + ".rgb.r");
        final int g_2 = particleFile.getInt(customParticle_2 + ".rgb.g");
        final int b_2 = particleFile.getInt(customParticle_2 + ".rgb.b");
        final double yOffset_2 = particleFile.getDouble(customParticle_2 + ".y-offset");
        final String particleType_2 = particleFile.getString(customParticle_2 + ".particle");
        final int tick_2 = particleFile.getInt(customParticle_2 + ".tick");
        final String customParticle_3 = rocket.getString(path + "particle.3");
        final int delay_3 = particleFile.getInt(customParticle_3 + ".delay");
        final int size_3 = particleFile.getInt(customParticle_3 + ".size");
        final int amount_3 = particleFile.getInt(customParticle_3 + ".amount");
        final int r_3 = particleFile.getInt(customParticle_3 + ".rgb.r");
        final int g_3 = particleFile.getInt(customParticle_3 + ".rgb.g");
        final int b_3 = particleFile.getInt(customParticle_3 + ".rgb.b");
        final double yOffset_3 = particleFile.getDouble(customParticle_3 + ".y-offset");
        final String particleType_3 = particleFile.getString(customParticle_3 + ".particle");
        final int tick_3 = particleFile.getInt(customParticle_3 + ".tick");
        playerConnect.ShootTaskID = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            final LivingEntity target = plugin.drone_targets.get(uuid);
            if (target != null) {
                if (droneHolder.getAmmo() > 0) {
                    final Location location = armorStand.getLocation();
                    final Location targetLocation = target.getLocation();
                    if (armorStand.hasLineOfSight(target) && plugin.armorStandManager.hasBlockSight(location, targetLocation)) {
                            ArmorStand rock = plugin.armorStandManager.getArmorStand(armorStand.getLocation(), false, true);
                            rock.setHelmet(plugin.drone_heads.get(rocket.getString(path + "rocket-head")));
                            rockets.add(rock);
                            line(location, targetLocation, rocket.getDouble(path + "rocket-speed"), rock, target, rocket, path, armorStand.getHeadPose(), player
                                    ,customParticle_1, delay_1, size_1, amount_1, r_1, g_1, b_1, yOffset_1, particleType_1
                                    ,customParticle_2, delay_2, size_2, amount_2, r_2, g_2, b_2, yOffset_2, particleType_2, tick_2
                                    ,customParticle_3, delay_3, size_3, amount_3, r_3, g_3, b_3, yOffset_3, particleType_3, tick_3
                            );
                            BattleDrones.call.droneManager.checkAmmo(rocket, path, droneHolder.getAmmo(), player.getName());
                            BattleDrones.call.droneManager.checkShot(target, rocket, location, path, "run");
                            BattleDrones.call.droneManager.takeAmmo(playerConnect, droneHolder, rocket, path, player.getName());

                        }
                }
                playerConnect.setRegen(false);
            } else {
                playerConnect.setRegen(true);
            }
        }, 0, rocket.getLong(path + "cooldown")).getTaskId();
    }

    private void line(Location start, Location end, double space, ArmorStand armorStand, LivingEntity target, FileConfiguration file, String path, EulerAngle eulerAngle, Player player
            , String customParticle_1, int delay_1, int size_1, int amount_1, int r_1, int g_1, int b_1, double yOffset_1, String particleType_1
            , String customParticle_2, int delay_2, int size_2, int amount_2, int r_2, int g_2, int b_2, double yOffset_2, String particleType_2, int tick_1
            , String customParticle_3, int delay_3, int size_3, int amount_3, int r_3, int g_3, int b_3, double yOffset_3, String particleType_3, int tick_2
    ) {
        new BukkitRunnable() {
            final Vector p1 = start.toVector();
            final Vector vector = end.toVector().clone().subtract(p1).normalize().multiply(space);
            double length = 0;
            int timer = 0;
            int particle1 = 0;
            int particle2 = 0;
            final ArrayList<String> exclude = new ArrayList<>();
            final World world = start.getWorld();
            @Override
            public void run() {
                p1.add(vector);
                armorStand.setHeadPose(eulerAngle);
                armorStand.teleport(p1.toLocation(world));
                length += space;
                final Location armorStandLocation = armorStand.getLocation();
                ArrayList<LivingEntity> rocket = plugin.armorStandManager.getEntityAround(armorStand, 1,  1, 1, 1, exclude, false);
                rocket.remove(player);
                if (timer > (file.getLong(path + "rocket-time") * 20) || rocket.size() > 0 || armorStand.getTargetBlock(null, 1).getType() != Material.AIR) {
                    this.cancel();
                    ArrayList<LivingEntity> livingEntities = plugin.armorStandManager.getEntityAround(armorStand, file.getInt(path + "rocket-radius"),  1, 1, 1, exclude, false);
                    for (LivingEntity livingEntity : livingEntities) {
                        plugin.calculateManager.damage(livingEntity, plugin.randomDouble(file.getDouble(path + "min"), file.getDouble(path + "max")));
                    }
                    if (file.getBoolean(path + "rocket-explosion")) {
                        world.createExplosion(p1.toLocation(world), file.getInt(path + "rocket-explosion-power"), file.getBoolean(path + "rocket-explosion-fire"), file.getBoolean(path + "rocket-explosion-block"));
                    }
                    BattleDrones.call.droneManager.checkShot(target, file, armorStandLocation, path, "explode");
                    if (customParticle_1 != null) {
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                            Location location = armorStandLocation.add(0, yOffset_1, 0);
                            plugin.particleManager.displayParticle(customParticle_1, particleType_1, location, r_1, g_1, b_1, size_1, amount_1);
                        }, delay_1);
                    }
                    armorStand.remove();
                }
                timer++;
                if (customParticle_2 != null) {
                    if (particle1 == 0) {
                        particle1 = tick_1 + 1;
                    }
                    if (particle1 > tick_1) {
                        final Location location = armorStandLocation.clone().add(0, yOffset_2, 0);
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                            plugin.particleManager.displayParticle(customParticle_2, particleType_2, location, r_2, g_2, b_2, size_2, amount_2);
                        }, delay_2);
                        particle1 = 1;
                    }
                    particle1++;
                }
                if (customParticle_3 != null) {
                        if (particle2 == 0) {
                            particle2 = tick_2 + 1;
                        }
                        if (particle2 > tick_2) {
                            final Location location = armorStandLocation.clone().add(0, yOffset_3, 0);
                            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                                plugin.particleManager.displayParticle(customParticle_3, particleType_3, location, r_3, g_3, b_3, size_3, amount_3);
                            }, delay_3);
                            particle2 = 1;
                        }
                    particle2++;
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }
}