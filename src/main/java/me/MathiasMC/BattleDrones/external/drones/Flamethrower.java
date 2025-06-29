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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class Flamethrower extends DroneRegistry {

    private final BattleDrones plugin;

    public Flamethrower(BattleDrones plugin,
                        String droneName,
                        String droneCategory
    ) {
        super(plugin, droneName, droneCategory);
        this.plugin = plugin;
    }

    @Override
    public void ability(Player player,
                        PlayerConnect playerConnect,
                        DroneHolder droneHolder
    ) {
        String drone = "flamethrower";
        String uuid = player.getUniqueId().toString();
        String group = playerConnect.getGroup();
        FileConfiguration file = plugin.droneFiles.get(drone);
        String path = group + "." + droneHolder.getLevel() + ".";
        ArmorStand armorStand = playerConnect.head;

        FileConfiguration particleFile = plugin.getFileUtils().particles;
        String particleType = particleFile.getString(droneName + ".particle");
        int size = particleFile.getInt(droneName + ".size");
        int amount = particleFile.getInt(droneName + ".amount");
        int r = particleFile.getInt(droneName + ".rgb.r");
        int g = particleFile.getInt(droneName + ".rgb.g");
        int b = particleFile.getInt(droneName + ".rgb.b");
        int delay = particleFile.getInt(droneName + ".delay");
        double yOffset = particleFile.getDouble(droneName + ".y-offset");
        double space = particleFile.getDouble(droneName + ".space");
        double distance = particleFile.getDouble(droneName + ".distance");

        List<String> blockCheckList = plugin.getFileUtils().getBlockCheck(file, path);

        long cooldown = file.getLong(path + "cooldown");

        long maxAmmoSlots = file.getLong(path + "max-ammo-slots") * 64;

        playerConnect.ability = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            LivingEntity target = plugin.drone_targets.get(uuid);
            if (target == null) {
                playerConnect.setHealing(true);
                return;
            }

            playerConnect.setHealing(false);

            boolean hasAmmo = droneHolder.getAmmo() > 0 || player.hasPermission("battledrones.bypass.ammo." + drone);
            if (!hasAmmo) return;

            Location location = armorStand.getLocation();
            Location targetLocation = target.getEyeLocation();

            boolean canSeeTarget = armorStand.hasLineOfSight(target) && plugin.getEntityManager().hasBlockSight(armorStand, location, targetLocation, blockCheckList);

            if (!canSeeTarget) return;

            if (particleFile.contains(droneName)) {

                Location start = location.add(0, 0.4, 0);

                World world = start.getWorld();
                if (world == null) return;

                double projectileSpace = file.getDouble(path + "projectile-space");
                double damage = plugin.getCalculateManager().randomDouble(file.getDouble(path + "min"), file.getDouble(path + "max"));
                double radius = file.getDouble(path + "projectile-radius");
                double chance = file.getDouble(path + "setfire-chance");
                int burnTime = file.getInt(path + "burning-time");
                int projectileSpeed = file.getInt(path + "projectile-speed");
                int maxTicks = 8 * 20;

                new BukkitRunnable() {
                    final Vector p1 = start.toVector();
                    final Vector vector = targetLocation.toVector().clone().subtract(p1).normalize().multiply(projectileSpace);
                    int timer = 0;

                    @Override
                    public void run() {

                        Location location = p1.toLocation(world);

                        List<Entity> nearby = (List<Entity>) world.getNearbyEntities(location, radius, radius, radius);

                        if (nearby.contains(target)) {
                            if (plugin.getCalculateManager().randomChance() <= chance) {
                                target.setFireTicks(burnTime);
                            }
                            target.damage(damage);
                            this.cancel();
                            plugin.getDroneManager().checkTarget(player, target, file, location, path, 2);
                        }

                        if (timer > maxTicks) {
                            cancel();
                        }

                        if (droneName != null) {
                            location.add(0, yOffset, 0);
                            plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.getParticleManager().displayLineParticle(particleType, location, start, distance, space, r, g, b, amount, size), delay);
                        }

                        timer++;
                        p1.add(vector);
                    }
                }.runTaskTimer(plugin, 0, projectileSpeed);
            }

            plugin.getDroneManager().checkMessage(droneHolder.getAmmo(), maxAmmoSlots, player, "ammo");
            plugin.getDroneManager().checkShot(player, target, file, location, path, "run");
            plugin.getDroneManager().takeAmmo(player, playerConnect, droneHolder, file, path);

        }, 0, cooldown).getTaskId();
    }
}