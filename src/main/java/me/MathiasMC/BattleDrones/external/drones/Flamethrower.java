package me.MathiasMC.BattleDrones.external.drones;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.api.DroneRegistry;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class Flamethrower extends DroneRegistry {

    private final BattleDrones plugin;

    public Flamethrower(Plugin plugin, String droneName, String droneCategory) {
        super(plugin, droneName, droneCategory);
        this.plugin = BattleDrones.getInstance();
    }

    @Override
    public void ability(final Player player, final PlayerConnect playerConnect, final DroneHolder droneHolder) {
        final String drone = "flamethrower";
        final String uuid = player.getUniqueId().toString();
        final String group = playerConnect.getGroup();
        final FileConfiguration file = plugin.droneFiles.get(drone);
        final String path = group + "." + droneHolder.getLevel() + ".";
        final ArmorStand armorStand = playerConnect.head;
        final FileConfiguration particleFile = plugin.getFileUtils().particles;
        final String particleType = particleFile.getString(droneName + ".particle");
        final int size = particleFile.getInt(droneName + ".size");
        final int amount = particleFile.getInt(droneName + ".amount");
        final int r = particleFile.getInt(droneName + ".rgb.r");
        final int g = particleFile.getInt(droneName + ".rgb.g");
        final int b = particleFile.getInt(droneName + ".rgb.b");
        final int delay = particleFile.getInt(droneName + ".delay");
        final double yOffset = particleFile.getDouble(droneName + ".y-offset");
        final double space = particleFile.getDouble(droneName + ".space");
        final double distance = particleFile.getDouble(droneName + ".distance");
        final List<String> list = plugin.getFileUtils().getBlockCheck(file, path);
        playerConnect.ability = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            final LivingEntity target = plugin.drone_targets.get(uuid);
            if (target != null) {
                if (droneHolder.getAmmo() > 0 || player.hasPermission("battledrones.bypass.ammo." + drone)) {
                    final Location location = armorStand.getLocation();
                    final Location targetLocation = target.getEyeLocation();
                    if (armorStand.hasLineOfSight(target) && plugin.getEntityManager().hasBlockSight(armorStand, location, targetLocation, list)) {
                        if (particleFile.contains(droneName)) {
                            line(location.add(0, 0.4, 0), targetLocation, player, target, file, path, droneName, delay, size, amount, distance, space, r, g, b, yOffset, particleType);
                        }
                        plugin.getDroneManager().checkMessage(droneHolder.getAmmo(), file.getInt(path + "max-ammo-slots") * 64, player.getName(), "ammo");
                        plugin.getDroneManager().checkShot(player, target, file, location, path, "run");
                        plugin.getDroneManager().takeAmmo(player, playerConnect, droneHolder, file, path);

                    }
                }
                playerConnect.setHealing(false);
            } else {
                playerConnect.setHealing(true);
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
            final double damage = plugin.getCalculateManager().randomDouble(file.getDouble(path + "min"), file.getDouble(path + "max"));
            final double radius = file.getDouble(path + "projectile-radius");
            final double chance = file.getDouble(path + "setfire-chance");
            final int burnTime = file.getInt(path + "burning-time");

            @Override
            public void run() {
                final Location location = p1.toLocation(world);
                List<Entity> nearby = (List<Entity>) world.getNearbyEntities(location, radius, radius, radius);
                if (nearby.contains(target)) {
                    if (plugin.getCalculateManager().randomChance() <= chance) {
                        target.setFireTicks(burnTime);
                    }
                    target.damage(damage);
                    this.cancel();
                    plugin.getDroneManager().checkTarget(player, target, file, location, path, 2);
                }
                if (timer > 8 * 20) {
                    this.cancel();
                }
                if (customParticle != null) {
                    location.add(0, yOffset, 0);
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.getParticleManager().displayLineParticle(particleType, location, start, distance, space, r, g, b, amount, size), delay);
                }
                timer++;
                p1.add(vector);
            }
        }.runTaskTimer(plugin, 0, file.getInt(path + "projectile-speed"));
    }
}