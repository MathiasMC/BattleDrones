package me.MathiasMC.BattleDrones.external.drones;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.api.DroneRegistry;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Lightning extends DroneRegistry {

    private final BattleDrones plugin;

    public Lightning(BattleDrones plugin,
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

        List<String> blockCheckList = plugin.getFileUtils().getBlockCheck(file, path);

        long cooldown = file.getLong(path + "cooldown");

        long maxAmmoSlots = file.getLong(path + "max-ammo-slots") * 64;

        double chance = file.getDouble(path + "chance");
        int burnTime = file.getInt(path + "burning-time");
        double explosionFireChance = file.getDouble(path + "setfire-chance");
        double explosionChance = file.getDouble(path + "explosion-chance");
        double explosionPower = file.getDouble(path + "explosion-power");
        boolean explosionFire = file.getBoolean(path + "explosion-fire");
        boolean explosionBlock = file.getBoolean(path + "explosion-block");
        int targetDead = file.getInt(path + "target-dead");

        double minDamage = file.getDouble(path + "min");
        double maxDamage = file.getDouble(path + "max");

        playerConnect.ability = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            LivingEntity target = plugin.drone_targets.get(uuid);
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

            World world = targetLocation.getWorld();
            if (world == null) return;

            world.strikeLightningEffect(targetLocation);
            ArrayList<String> exclude = new ArrayList<>();
            ArrayList<LivingEntity> livingEntities = plugin.getEntityManager().getLivingEntitiesAround(target, file.getDouble(path + "radius"),  1, 1, 1, exclude, exclude, false);
            double random = plugin.getCalculateManager().randomDouble(minDamage, maxDamage);
            for (LivingEntity livingEntity : livingEntities) {
                plugin.getCalculateManager().damage(livingEntity, random);
            }
            plugin.getCalculateManager().damage(target, random);
            if (plugin.getCalculateManager().randomChance() <= explosionFireChance) {
                target.setFireTicks(burnTime);
            }
            if (file.getBoolean(path + "explosion")) {
                if (plugin.getCalculateManager().randomChance() <= explosionChance) {
                    world.createExplosion(targetLocation.getX(), targetLocation.getY(), targetLocation.getZ(), (float) explosionPower, explosionFire, explosionBlock);
                }
            }
            if (plugin.getCalculateManager().randomChance() <= chance && target instanceof Player) {
                if (file.contains(path + "chance-commands")) {
                    for (String command : file.getStringList(path + "chance-commands")) {
                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', command.replace("{player}", target.getName())));
                    }
                }
            }

            plugin.getDroneManager().checkTarget(player, target, file, targetLocation, path, targetDead);
            plugin.getDroneManager().checkMessage(droneHolder.getAmmo(), maxAmmoSlots, player, "ammo");
            plugin.getDroneManager().checkShot(player, target, file, headLocation, path, "run");
            plugin.getDroneManager().takeAmmo(player, playerConnect, droneHolder, file, path);

        }, 0, cooldown).getTaskId();
    }
}
