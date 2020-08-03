package me.MathiasMC.BattleDrones.drones;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Objects;

public class Healing {

    private final BattleDrones plugin;

    public Healing(final BattleDrones plugin) {
        this.plugin = plugin;
    }


    public void shot(final Player player) {
        final String drone = "healing";
        final String uuid = player.getUniqueId().toString();
        final PlayerConnect playerConnect = plugin.get(uuid);
        final DroneHolder droneHolder = plugin.getDroneHolder(uuid, drone);
        final String group = playerConnect.getGroup();
        final FileConfiguration healing = plugin.droneFiles.get(drone);
        final String path = group + "." + droneHolder.getLevel() + ".";
        final ArmorStand armorStand = playerConnect.head;
        final FileConfiguration particleFile = plugin.particles.get;
        final String customParticle = healing.getString(path + "particle.1");
        final String particleType = particleFile.getString(customParticle + ".particle");
        final int size = particleFile.getInt(customParticle + ".size");
        final int amount = particleFile.getInt(customParticle + ".amount");
        final int r = particleFile.getInt(customParticle + ".rgb.r");
        final int g = particleFile.getInt(customParticle + ".rgb.g");
        final int b = particleFile.getInt(customParticle + ".rgb.b");
        final int delay = particleFile.getInt(customParticle + ".delay");
        final double yOffset = particleFile.getDouble(customParticle + ".y-offset");
        final double space = particleFile.getDouble(customParticle + ".space");
        playerConnect.ShootTaskID = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            final LivingEntity target = plugin.drone_targets.get(uuid);
            if (target != null) {
                if (droneHolder.getAmmo() > 0) {
                    final double health = target.getHealth();
                    final double maxHealth = Objects.requireNonNull(target.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue();
                    final Location location = armorStand.getEyeLocation();
                    final Location targetLocation = target.getEyeLocation();
                    if (armorStand.hasLineOfSight(target) && health < maxHealth && plugin.armorStandManager.hasBlockSight(location, targetLocation)) {
                        final double add = plugin.randomDouble(healing.getDouble(path + "min"), healing.getDouble(path + "max"));
                        target.setHealth(Math.min(health + add, maxHealth));
                        if (customParticle != null) {
                            final Location armorstand = armorStand.getEyeLocation().add(0, yOffset, 0);
                            plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.particleManager.line(particleType, armorstand, targetLocation, armorstand.distance(targetLocation), space, r, g, b, amount, size), delay);
                        }
                        plugin.droneManager.checkAmmo(healing, path, droneHolder.getAmmo(), player.getName());
                        plugin.droneManager.checkShot(player, target, healing, location, path, "run");
                        plugin.droneManager.takeAmmo(playerConnect, droneHolder, healing, path, player.getName());
                    }
                }
                playerConnect.setRegen(false);
            } else {
                playerConnect.setRegen(true);
            }
        }, 0, healing.getLong(path + "cooldown") * 20).getTaskId();
    }
}