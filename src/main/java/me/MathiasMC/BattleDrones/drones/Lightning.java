package me.MathiasMC.BattleDrones.drones;

import me.MathiasMC.BattleDrones.BattleDrones;
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

public class Lightning {

    private final BattleDrones plugin;

    public Lightning(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    public void shot(final Player player) {
        final String drone = "lightning";
        final String uuid = player.getUniqueId().toString();
        final PlayerConnect playerConnect = plugin.get(uuid);
        final DroneHolder droneHolder = plugin.getDroneHolder(uuid, drone);
        final String group = playerConnect.getGroup();
        final FileConfiguration file = plugin.droneFiles.get(drone);
        final String path = group + "." + droneHolder.getLevel() + ".";
        final ArmorStand armorStand = playerConnect.head;
        playerConnect.ShootTaskID = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            final LivingEntity target = plugin.drone_targets.get(uuid);
            if (target != null) {
                if (droneHolder.getAmmo() > 0) {
                    final Location location = armorStand.getLocation();
                    final Location targetLocation = target.getEyeLocation();
                    if (armorStand.hasLineOfSight(target) && plugin.armorStandManager.hasBlockSight(armorStand, location, targetLocation)) {
                        line(targetLocation, player, target, file, path);
                        plugin.droneManager.checkMessage(droneHolder.getAmmo(), file.getInt(path + "max-ammo-slots") * 64, player.getName(), "ammo");
                        plugin.droneManager.checkShot(player, target, file, location, path, "run");
                        plugin.droneManager.takeAmmo(playerConnect, droneHolder, file, path, player.getName());
                    }
                }
                playerConnect.setRegen(false);
            } else {
                playerConnect.setRegen(true);
            }
        }, 0, file.getLong(path + "cooldown")).getTaskId();
    }

    private void line(final Location end, final Player player, final LivingEntity target, final FileConfiguration file, final String path) {
        final World world = end.getWorld();
        if (world == null) {
            return;
        }
        final ArrayList<String> exclude = new ArrayList<>();
        final double chance = file.getDouble(path + "chance");
        final double setfire_chance = file.getDouble(path + "setfire-chance");
        final int burnTime = file.getInt(path + "burning-time");
        final double explosion_chance = file.getDouble(path + "explosion-chance");
        world.strikeLightningEffect(end);
        final ArrayList<LivingEntity> livingEntities = plugin.armorStandManager.getEntityAround(target, file.getDouble(path + "radius"),  1, 1, 1, exclude, false);
        final double random = plugin.randomDouble(file.getDouble(path + "min"), file.getDouble(path + "max"));
        for (LivingEntity livingEntity : livingEntities) {
            plugin.calculateManager.damage(livingEntity, random);
        }
        plugin.calculateManager.damage(target, random);
        if (plugin.randomChance() <= setfire_chance) {
            target.setFireTicks(burnTime);
        }
        if (file.getBoolean(path + "explosion")) {
            if (plugin.randomChance() <= explosion_chance) {
                world.createExplosion(end.getX(), end.getY(), end.getZ(), (float) file.getDouble(path + "explosion-power"), file.getBoolean(path + "explosion-fire"), file.getBoolean(path + "explosion-block"));
            }
        }
        if (plugin.randomChance() <= chance) {
            if (target instanceof Player) {
                if (file.contains(path + "chance-commands")) {
                    for (String command : file.getStringList(path + "chance-commands")) {
                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', command.replace("{player}", target.getName())));
                    }
                }
            }
        }
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (target.isDead()) {
                plugin.droneManager.checkShot(player, target, file, end, path, "killed");
            }
        }, file.getInt(path + "target-dead"));
    }
}
