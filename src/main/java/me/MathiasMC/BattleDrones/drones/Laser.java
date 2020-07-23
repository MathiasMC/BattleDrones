package me.MathiasMC.BattleDrones.drones;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
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
        playerConnect.ShootTaskID = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            double damage = laser.getDouble(path + "min");
                final ArmorStand armorStand = playerConnect.head;
                final LivingEntity target = plugin.drone_targets.get(uuid);
                if (target != null) {
                    if (droneHolder.getAmmo() > 0) {
                        if (armorStand.hasLineOfSight(target)) {
                            BattleDrones.call.droneManager.checkAmmo(laser, path, droneHolder.getAmmo(), player.getName());
                            plugin.calculateManager.line(armorStand.getEyeLocation().add(0, 0.4, 0), target.getEyeLocation(), laser, path);
                            boolean getKnockback = false;
                            BattleDrones.call.droneManager.checkShot(target, laser, armorStand.getLocation(), path, "run");
                            if (plugin.randomChance() <= laser.getDouble(path + "accuracy")) {
                                getKnockback = true;
                                damage = plugin.randomDouble(damage, laser.getDouble(path + "max"));
                            }
                            BattleDrones.call.calculateManager.damage(target, damage);
                            final double knockback = laser.getDouble(path + "knockback");
                            if (getKnockback && knockback > 0) {
                                target.setVelocity(target.getLocation().getDirection().setY(0).normalize().multiply(knockback));
                            }
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