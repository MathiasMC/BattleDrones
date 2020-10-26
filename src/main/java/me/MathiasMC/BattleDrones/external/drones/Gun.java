package me.MathiasMC.BattleDrones.external.drones;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.api.DroneRegistry;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class Gun extends DroneRegistry {

    private final BattleDrones plugin;

    public Gun(BattleDrones plugin, String droneName, String droneCategory) {
        super(plugin, droneName, droneCategory);
        this.plugin = plugin;
    }

    @Override
    public void ability(final Player player, final PlayerConnect playerConnect, final DroneHolder droneHolder) {
        final String uuid = player.getUniqueId().toString();
        final String drone = playerConnect.getActive();
        final String group = playerConnect.getGroup();
        final FileConfiguration file = plugin.droneFiles.get(drone);
        final String path = group + "." + droneHolder.getLevel() + ".";
        final ArmorStand armorStand = playerConnect.head;
        final double minDamage = file.getDouble(path + "min");
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
        final List<String> list = plugin.getFileUtils().getBlockCheck(file, path);
        final double knockback = plugin.getFileUtils().getDouble(file, path + "knockback", 0);
        playerConnect.ability = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            double damage = minDamage;
            final LivingEntity target = plugin.drone_targets.get(uuid);
            if (target != null) {
                if (droneHolder.getAmmo() > 0 || player.hasPermission("battledrones.bypass.ammo." + drone)) {
                    final Location location = armorStand.getLocation();
                    final Location targetLocation = target.getEyeLocation();
                    if (armorStand.hasLineOfSight(target) && plugin.getEntityManager().hasBlockSight(armorStand, location, targetLocation, list)) {
                        if (particleFile.contains(droneName)) {
                            final Location armorstand = armorStand.getEyeLocation().add(0, yOffset, 0);
                            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                                double distance = particleFile.getDouble(droneName + ".distance");
                                if (drone.equalsIgnoreCase("laser")) {
                                    distance = armorstand.distance(targetLocation);
                                }
                                plugin.getParticleManager().displayLineParticle(particleType, armorstand, targetLocation, distance, space, r, g, b, amount, size);
                            }, delay);
                        }
                        if (plugin.getCalculateManager().randomChance() <= file.getDouble(path + "chance")) {
                            if (knockback != 0D) {
                                target.setVelocity(target.getLocation().setDirection(location.getDirection()).getDirection().setY(0).normalize().multiply(knockback));
                            }
                            damage = plugin.getCalculateManager().randomDouble(damage, file.getDouble(path + "max"));
                        }
                        plugin.getCalculateManager().damage(target, damage);
                        plugin.getDroneManager().checkMessage(droneHolder.getAmmo(), file.getInt(path + "max-ammo-slots") * 64, player, "ammo");
                        plugin.getDroneManager().checkShot(player, target, file, location, path, "run");
                        plugin.getDroneManager().takeAmmo(player, playerConnect, droneHolder, file, path);
                        plugin.getDroneManager().checkTarget(player, target, file, targetLocation, path, 2);
                    }
                }
                playerConnect.setHealing(false);
            } else {
                playerConnect.setHealing(true);
            }
        }, 0, file.getLong(path + "cooldown")).getTaskId();
    }
}