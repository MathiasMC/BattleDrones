package me.MathiasMC.BattleDrones.drones;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

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
        playerConnect.ShootTaskID = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            double damage = machine_gun.getDouble(path + "min");
            final ArmorStand armorStand = playerConnect.head;
            final LivingEntity target = plugin.drone_targets.get(uuid);
            if (target != null) {
                if (droneHolder.getAmmo() > 0) {
                    if (armorStand.hasLineOfSight(target)) {
                        burst(armorStand.getEyeLocation().add(0, 0.4, 0), target.getEyeLocation(), machine_gun, path);
                        if (plugin.randomChance() <= machine_gun.getDouble(path + "accuracy")) {
                            damage = plugin.randomDouble(damage, machine_gun.getDouble(path + "max"));
                        }
                        BattleDrones.call.calculateManager.damage(target, damage);
                        BattleDrones.call.droneManager.checkAmmo(machine_gun, path, droneHolder.getAmmo(), player.getName());
                        BattleDrones.call.droneManager.checkShot(target, machine_gun, armorStand.getLocation(), path, "run");
                        BattleDrones.call.droneManager.takeAmmo(playerConnect, droneHolder, machine_gun, path, player.getName());
                    }
                }
                playerConnect.setRegen(false);
            } else {
                playerConnect.setRegen(true);
            }
        }, 0, machine_gun.getLong(path + "cooldown")).getTaskId();
    }

    public void burst(Location start, Location end, FileConfiguration file, String path) {
        final double distance = file.getDouble(path + "particle-line.length");
        final Vector p1 = start.toVector();
        double space = file.getDouble(path + "particle-line.space");
        int r = file.getInt(path + "particle-line.rgb.r");
        int g = file.getInt(path + "particle-line.rgb.g");
        int b = file.getInt(path + "particle-line.rgb.b");
        int amount = file.getInt(path + "particle-line.amount");
        int size = file.getInt(path + "particle-line.size");
        final Vector vector = end.toVector().clone().subtract(p1).normalize().multiply(space);
        double length = 0;
        for (; length < distance; p1.add(vector)) {
            Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(r, g, b), size);
            start.getWorld().spawnParticle(Particle.REDSTONE, p1.toLocation(start.getWorld()), amount, 0, 0, 0, 0F, dustOptions);
            length += space;
        }
    }
}
