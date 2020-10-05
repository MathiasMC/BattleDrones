package me.MathiasMC.BattleDrones.drones;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

public class Gun {

    private final BattleDrones plugin;

    public Gun(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    public void shot(final Player player, final String drone) {
        final String uuid = player.getUniqueId().toString();
        final PlayerConnect playerConnect = plugin.get(uuid);
        final DroneHolder droneHolder = plugin.getDroneHolder(uuid, drone);
        final String group = playerConnect.getGroup();
        final FileConfiguration file = plugin.droneFiles.get(drone);
        final String path = group + "." + droneHolder.getLevel() + ".";
        final ArmorStand armorStand = playerConnect.head;
        final double minDamage = file.getDouble(path + "min");
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
        List<String> tempList = null;
        if (file.contains(path + "block-check")) {
            tempList = file.getStringList(path + "block-check");
        }
        final List<String> list = tempList;
        playerConnect.ShootTaskID = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            double damage = minDamage;
            final LivingEntity target = plugin.drone_targets.get(uuid);
            if (target != null) {
                if (droneHolder.getAmmo() > 0) {
                    final Location location = armorStand.getLocation();
                    final Location targetLocation = target.getEyeLocation();
                    if (armorStand.hasLineOfSight(target) && plugin.armorStandManager.hasBlockSight(armorStand, location, targetLocation, list)) {
                        if (customParticle != null) {
                            final Location armorstand = armorStand.getEyeLocation().add(0, yOffset, 0);
                            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                                double distance = particleFile.getDouble(customParticle + ".distance");
                                if (drone.equalsIgnoreCase("laser")) {
                                    distance = armorstand.distance(targetLocation);
                                }
                                plugin.particleManager.line(particleType, armorstand, targetLocation, distance, space, r, g, b, amount, size);
                            }, delay);
                        }
                        boolean getKnockback = false;
                        if (plugin.randomChance() <= file.getDouble(path + "chance")) {
                            getKnockback = true;
                            damage = plugin.randomDouble(damage, file.getDouble(path + "max"));
                        }
                        if (file.contains(path + "knockback")) {
                            final double knockback = file.getDouble(path + "knockback");
                            if (getKnockback && knockback > 0) {
                                target.setVelocity(target.getLocation().getDirection().setY(0).normalize().multiply(knockback));
                            }
                        }
                        plugin.calculateManager.damage(target, damage);
                        plugin.droneManager.checkMessage(droneHolder.getAmmo(), file.getInt(path + "max-ammo-slots") * 64, player.getName(), "ammo");
                        plugin.droneManager.checkShot(player, target, file, location, path, "run");
                        plugin.droneManager.takeAmmo(playerConnect, droneHolder, file, path, player.getName());
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                            if (target.isDead()) {
                                plugin.droneManager.checkShot(player, target, file, targetLocation, path, "killed");
                            }
                        }, 2);
                    }
                }
                playerConnect.setRegen(false);
            } else {
                playerConnect.setRegen(true);
            }
        }, 0, file.getLong(path + "cooldown")).getTaskId();
    }
}