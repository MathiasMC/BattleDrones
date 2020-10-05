package me.MathiasMC.BattleDrones.drones;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class Flamethrower {

    private final BattleDrones plugin;

    public Flamethrower(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    public void shot(final Player player) {
        final String drone = "flamethrower";
        final String uuid = player.getUniqueId().toString();
        final PlayerConnect playerConnect = plugin.get(uuid);
        final DroneHolder droneHolder = plugin.getDroneHolder(uuid, drone);
        final String group = playerConnect.getGroup();
        final FileConfiguration file = plugin.droneFiles.get(drone);
        final String path = group + "." + droneHolder.getLevel() + ".";
        final ArmorStand armorStand = playerConnect.head;
        final FileConfiguration particleFile = plugin.particles.get;
        final String customParticle = file.getString(path + "particle.1");
        final String particleType = particleFile.getString(customParticle + ".particle");
        final int size = particleFile.getInt(customParticle + ".size");
        final int amount = particleFile.getInt(customParticle + ".amount");
        final int r = particleFile.getInt(customParticle + ".rgb.r");
        final int g = particleFile.getInt(customParticle + ".rgb.g");
        final int b = particleFile.getInt(customParticle + ".rgb.b");
        final int delay = particleFile.getInt(customParticle + ".delay");
        final double yOffset = particleFile.getDouble(customParticle + ".y-offset");
        final double space = particleFile.getDouble(customParticle + ".space");
        final double distance = particleFile.getDouble(customParticle + ".distance");
        final List<String> list = plugin.config.get.getStringList("better-block-check.list");
        playerConnect.ShootTaskID = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            final LivingEntity target = plugin.drone_targets.get(uuid);
            if (target != null) {
                if (droneHolder.getAmmo() > 0) {
                    final Location location = armorStand.getLocation();
                    final Location targetLocation = target.getEyeLocation();
                    if (armorStand.hasLineOfSight(target) && plugin.armorStandManager.hasBlockSight(armorStand, location, targetLocation, list)) {
                        line(location.add(0, 0.4, 0), targetLocation, player, target, file, path, customParticle, delay, size, amount, distance, space, r, g, b, yOffset, particleType);
                        plugin.droneManager.checkMessage(droneHolder.getAmmo(), file.getInt(path + "max-ammo-slots") * 64, player.getName(), "ammo");
                        plugin.droneManager.checkShot(player, target, file, location, path, "run");
                        plugin.droneManager.takeAmmo(playerConnect, droneHolder, file, path, player.getName());

                    }
                }
                playerConnect.setRegen(false);
            } else {
                playerConnect.setRegen(true);
            }
        }, 0, file.getLong(path + "cooldown")).getTaskId();
    }

    private void line(final Location start, final Location end, final Player player, final LivingEntity target, final FileConfiguration file, final String path
            , final String customParticle, final int delay, final int size, final int amount, final double distance, final double space, final int r, final int g, final int b, final double yOffset, final String particleType) {
        final World world = start.getWorld();
        if (world == null) {
            return;
        }
        new BukkitRunnable() {
            final Vector p1 = start.toVector();
            final Vector vector = end.toVector().clone().subtract(p1).normalize().multiply(file.getDouble(path + "projectile-space"));
            int timer = 0;
            final double damage = plugin.randomDouble(file.getDouble(path + "min"), file.getDouble(path + "max"));
            final double radius = file.getDouble(path + "projectile-radius");
            final double chance = file.getDouble(path + "setfire-chance");
            final int burnTime = file.getInt(path + "burning-time");

            @Override
            public void run() {
                final Location location = p1.toLocation(world);
                List<Entity> nearby = (List<Entity>) world.getNearbyEntities(location, radius, radius, radius);
                if (nearby.contains(target)) {
                    if (plugin.randomChance() <= chance) {
                        target.setFireTicks(burnTime);
                    }
                    target.damage(damage);
                    this.cancel();
                    if (target.isDead()) {
                        plugin.droneManager.checkShot(player, target, file, location, path, "killed");
                    }
                }
                if (timer > 8 * 20) {
                    this.cancel();
                }
                if (customParticle != null) {
                    location.add(0, yOffset, 0);
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        plugin.particleManager.line(particleType, location, start, distance, space, r, g, b, amount, size);
                    }, delay);
                }
                timer++;
                p1.add(vector);
            }
        }.runTaskTimer(plugin, 0, file.getInt(path + "projectile-speed"));
    }
}