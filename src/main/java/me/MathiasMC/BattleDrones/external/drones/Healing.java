package me.MathiasMC.BattleDrones.external.drones;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.api.DroneRegistry;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

public class Healing extends DroneRegistry {

    private final BattleDrones plugin;

    public Healing(BattleDrones plugin, String droneName, String droneCategory) {
        super(plugin, droneName, droneCategory);
        this.plugin = plugin;
    }

    @Override
    public void ability(final Player player, final PlayerConnect playerConnect, final DroneHolder droneHolder) {
        final String drone = "healing";
        final String uuid = player.getUniqueId().toString();
        final String group = playerConnect.getGroup();
        final FileConfiguration healing = plugin.droneFiles.get(drone);
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
        final List<String> list = plugin.getFileUtils().getBlockCheck(healing, path);
        playerConnect.ability = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            final LivingEntity target = plugin.drone_targets.get(uuid);
            if (target != null) {
                if (droneHolder.getAmmo() > 0 || player.hasPermission("battledrones.bypass.ammo." + drone)) {
                    final double health = target.getHealth();
                    final double maxHealth = Objects.requireNonNull(target.getAttribute(Attribute.MAX_HEALTH)).getValue();
                    final Location location = armorStand.getEyeLocation();
                    final Location targetLocation = target.getEyeLocation();
                    if (armorStand.hasLineOfSight(target) && health < maxHealth && plugin.getEntityManager().hasBlockSight(armorStand, location, targetLocation, list)) {
                        final double add = plugin.getCalculateManager().randomDouble(healing.getDouble(path + "min"), healing.getDouble(path + "max"));
                        target.setHealth(Math.min(health + add, maxHealth));
                        if (particleFile.contains(droneName)) {
                            final Location armorstand = armorStand.getEyeLocation().add(0, yOffset, 0);
                            plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.getParticleManager().displayLineParticle(particleType, armorstand, targetLocation, armorstand.distance(targetLocation), space, r, g, b, amount, size), delay);
                        }
                        plugin.getDroneManager().checkMessage(droneHolder.getAmmo(), healing.getLong(path + "max-ammo-slots") * 64, player, "ammo");
                        plugin.getDroneManager().checkShot(player, target, healing, location, path, "run");
                        plugin.getDroneManager().takeAmmo(player, playerConnect, droneHolder, healing, path);
                    }
                }
                playerConnect.setHealing(false);
            } else {
                playerConnect.setHealing(true);
            }
        }, 0, healing.getLong(path + "cooldown") * 20).getTaskId();
    }
}