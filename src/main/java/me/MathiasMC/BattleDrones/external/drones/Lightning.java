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
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class Lightning extends DroneRegistry {

    private final BattleDrones plugin;

    public Lightning(BattleDrones plugin, String droneName, String droneCategory) {
        super(plugin, droneName, droneCategory);
        this.plugin = plugin;
    }

    @Override
    public void ability(final Player player, final PlayerConnect playerConnect, final DroneHolder droneHolder) {
        final String uuid = player.getUniqueId().toString();
        final String group = playerConnect.getGroup();
        final FileConfiguration file = plugin.droneFiles.get(droneName);
        final String path = group + "." + droneHolder.getLevel() + ".";
        final ArmorStand armorStand = playerConnect.head;
        final List<String> list = plugin.getFileUtils().getBlockCheck(file, path);
        playerConnect.ability = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            final LivingEntity target = plugin.drone_targets.get(uuid);
            if (target != null) {
                if (droneHolder.getAmmo() > 0 || player.hasPermission("battledrones.bypass.ammo." + droneName)) {
                    final Location location = armorStand.getLocation();
                    final Location targetLocation = target.getEyeLocation();
                    if (armorStand.hasLineOfSight(target) && plugin.getEntityManager().hasBlockSight(armorStand, location, targetLocation, list)) {
                        line(targetLocation, player, target, file, path);
                        plugin.getDroneManager().checkMessage(droneHolder.getAmmo(), file.getInt(path + "max-ammo-slots") * 64, player.getName(), "ammo");
                        plugin.getDroneManager().checkShot(player, target, file, location, path, "run");
                        plugin.getDroneManager().takeAmmo(player, playerConnect, droneHolder, file, path);
                    }
                }
                playerConnect.setHealing(false);
            } else {
                playerConnect.setHealing(true);
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
        final ArrayList<LivingEntity> livingEntities = plugin.getEntityManager().getLivingEntitiesAround(target, file.getDouble(path + "radius"),  1, 1, 1, exclude, exclude, false);
        final double random = plugin.getCalculateManager().randomDouble(file.getDouble(path + "min"), file.getDouble(path + "max"));
        for (LivingEntity livingEntity : livingEntities) {
            plugin.getCalculateManager().damage(livingEntity, random);
        }
        plugin.getCalculateManager().damage(target, random);
        if (plugin.getCalculateManager().randomChance() <= setfire_chance) {
            target.setFireTicks(burnTime);
        }
        if (file.getBoolean(path + "explosion")) {
            if (plugin.getCalculateManager().randomChance() <= explosion_chance) {
                world.createExplosion(end.getX(), end.getY(), end.getZ(), (float) file.getDouble(path + "explosion-power"), file.getBoolean(path + "explosion-fire"), file.getBoolean(path + "explosion-block"));
            }
        }
        if (plugin.getCalculateManager().randomChance() <= chance) {
            if (target instanceof Player) {
                if (file.contains(path + "chance-commands")) {
                    for (String command : file.getStringList(path + "chance-commands")) {
                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', command.replace("{player}", target.getName())));
                    }
                }
            }
        }
        plugin.getDroneManager().checkTarget(player, target, file, end, path, file.getInt(path + "target-dead"));
    }
}
