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

import java.util.List;

public class Gun extends DroneRegistry {

    private final BattleDrones plugin;

    public Gun(BattleDrones plugin,
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
        String uuid = player.getUniqueId().toString();
        String group = playerConnect.getGroup();
        FileConfiguration file = plugin.droneFiles.get(droneName);
        String path = group + "." + droneHolder.getLevel() + ".";
        ArmorStand head = playerConnect.head;

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

        double knockback = plugin.getFileUtils().getDouble(file, path + "knockback", 0);

        double min = file.getDouble(path + "min");
        double max = file.getDouble(path + "max");

        playerConnect.ability = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            final LivingEntity target = plugin.drone_targets.get(uuid);
            if (target == null) {
                playerConnect.setHealing(true);
                return;
            }

            playerConnect.setHealing(false);

            boolean hasAmmo = droneHolder.getAmmo() > 0 || player.hasPermission("battledrones.bypass.ammo." + droneName);
            if (!hasAmmo) return;

            Location headLocation = head.getLocation();
            Location targetLocation = target.getEyeLocation();

            boolean canSeeTarget = head.hasLineOfSight(target) && plugin.getEntityManager().hasBlockSight(headLocation, targetLocation, blockCheckList);

            if (!canSeeTarget) return;

            if (particleFile.contains(droneName)) {

                Location start = head.getEyeLocation().add(0, yOffset, 0);

                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    double distancenew = distance;
                    if (droneName.equalsIgnoreCase("laser")) {
                        distancenew = start.distance(targetLocation);
                    }
                    plugin.getParticleManager().displayLineParticle(particleType, start, targetLocation, distancenew, space, r, g, b, amount, size);
                }, delay);
            }

            double damage = min;

            if (plugin.getCalculateManager().randomChance() <= file.getDouble(path + "chance")) {
                if (knockback != 0D) {
                    target.setVelocity(target.getLocation().setDirection(headLocation.getDirection()).getDirection().setY(0).normalize().multiply(knockback));
                }
                damage = plugin.getCalculateManager().randomDouble(damage, max);
            }

            plugin.getCalculateManager().damage(target, damage);

            plugin.getDroneManager().checkMessage(droneHolder.getAmmo(), maxAmmoSlots, player, "ammo");
            plugin.getDroneManager().checkShot(player, target, file, headLocation, path, "run");
            plugin.getDroneManager().takeAmmo(player, playerConnect, droneHolder, file, path);
            plugin.getDroneManager().checkTarget(player, target, file, targetLocation, path, 2);

        }, 0, cooldown).getTaskId();
    }
}