package me.MathiasMC.BattleDrones.drones;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class MachineGun {

    private final BattleDrones plugin;

    public MachineGun(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    public void shot(final Player player) {
        final String drone = "machine_gun";
        final String uuid = player.getUniqueId().toString();
        final PlayerConnect playerConnect = plugin.get(uuid);
        final DroneHolder droneHolder = plugin.getDroneHolder(uuid, drone);
        final String group = playerConnect.getGroup();
        final FileConfiguration machine_gun = plugin.droneFiles.get(drone);
        final String path = group + "." + droneHolder.getLevel() + ".";
        final ArmorStand armorStand = playerConnect.head;
        final double minDamage = machine_gun.getDouble(path + "min");
        final double distance = machine_gun.getDouble(path + "particle-line.length");
        final double space = machine_gun.getDouble(path + "particle-line.space");
        final int r = machine_gun.getInt(path + "particle-line.rgb.r");
        final int g = machine_gun.getInt(path + "particle-line.rgb.g");
        final int b = machine_gun.getInt(path + "particle-line.rgb.b");
        final int amount = machine_gun.getInt(path + "particle-line.amount");
        final int size = machine_gun.getInt(path + "particle-line.size");
        playerConnect.ShootTaskID = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            double damage = minDamage;
            final LivingEntity target = plugin.drone_targets.get(uuid);
            if (target != null) {
                if (droneHolder.getAmmo() > 0) {
                    final Location location = armorStand.getLocation();
                    final Location targetLocation = target.getEyeLocation();
                    if (armorStand.hasLineOfSight(target) && plugin.armorStandManager.hasBlockSight(location, targetLocation)) {
                        plugin.calculateManager.burst(armorStand.getEyeLocation().add(0, 0.4, 0), targetLocation, distance, space, r, g, b, amount, size);
                        if (plugin.randomChance() <= machine_gun.getDouble(path + "accuracy")) {
                            damage = plugin.randomDouble(damage, machine_gun.getDouble(path + "max"));
                        }
                        BattleDrones.call.calculateManager.damage(target, damage);
                        BattleDrones.call.droneManager.checkAmmo(machine_gun, path, droneHolder.getAmmo(), player.getName());
                        BattleDrones.call.droneManager.checkShot(target, machine_gun, location, path, "run");
                        BattleDrones.call.droneManager.takeAmmo(playerConnect, droneHolder, machine_gun, path, player.getName());
                    }
                }
                playerConnect.setRegen(false);
            } else {
                playerConnect.setRegen(true);
            }
        }, 0, machine_gun.getLong(path + "cooldown")).getTaskId();
    }
}
