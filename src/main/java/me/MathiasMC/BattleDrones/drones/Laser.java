package me.MathiasMC.BattleDrones.drones;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;

public class Laser {

    private final BattleDrones plugin;

    public Laser(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    public void shot(final Player player) {
        final String drone = "laser";
        final String uuid = player.getUniqueId().toString();
        final PlayerConnect playerConnect = plugin.get(uuid);
        final DroneHolder droneHolder = plugin.getDroneHolder(uuid, drone);
        final String group = playerConnect.getGroup();
        final FileConfiguration laser = plugin.droneFiles.get(drone);
        final String path = group + "." + droneHolder.getLevel() + ".";
        final ArmorStand armorStand = playerConnect.head;
        final double minDamage = laser.getDouble(path + "min");
        playerConnect.ShootTaskID = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            double damage = minDamage;
                final LivingEntity target = plugin.drone_targets.get(uuid);
                if (target != null) {
                    if (droneHolder.getAmmo() > 0) {
                        final Location location = armorStand.getLocation();
                        final Location targetLocation = target.getEyeLocation();
                        if (armorStand.hasLineOfSight(target) && plugin.armorStandManager.hasBlockSight(location, targetLocation)) {
                            plugin.calculateManager.line(armorStand.getEyeLocation().add(0, 0.4, 0), targetLocation, laser, path);
                            boolean getKnockback = false;
                            if (plugin.randomChance() <= laser.getDouble(path + "accuracy")) {
                                getKnockback = true;
                                damage = plugin.randomDouble(damage, laser.getDouble(path + "max"));
                            }
                            BattleDrones.call.calculateManager.damage(target, damage);
                            final double knockback = laser.getDouble(path + "knockback");
                            if (getKnockback && knockback > 0) {
                                target.setVelocity(target.getLocation().getDirection().setY(0).normalize().multiply(knockback));
                            }
                            BattleDrones.call.droneManager.checkAmmo(laser, path, droneHolder.getAmmo(), player.getName());
                            BattleDrones.call.droneManager.checkShot(target, laser, location, path, "run");
                            BattleDrones.call.droneManager.takeAmmo(playerConnect, droneHolder, laser, path, player.getName());
                        }
                    }
                    playerConnect.setRegen(false);
                } else {
                    playerConnect.setRegen(true);
                }
        }, 0, laser.getLong(path + "cooldown")).getTaskId();
    }
}