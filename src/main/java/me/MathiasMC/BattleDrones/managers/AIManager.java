package me.MathiasMC.BattleDrones.managers;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.List;

public class AIManager {

    private final BattleDrones plugin;

    public AIManager(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    public void defaultAI(final Player player, final PlayerConnect playerConnect, final FileConfiguration file, final long drone_level, final int monsters, final int animals, final int players, final List<String> exclude, final boolean reverseExclude, final boolean hpCheck, final boolean lookAI) {
        final String uuid = player.getUniqueId().toString();
        final String group = playerConnect.getGroup();
        final ArmorStand head = playerConnect.head;
        final ArmorStand name = playerConnect.name;
        final EulerAngle eulerAngle = new EulerAngle(0, 0, 0);
        final double radius = file.getDouble(group + "." + drone_level + ".range");
        playerConnect.AItaskID = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            LivingEntity target = plugin.armorStandManager.getClose(player, radius, monsters, animals, players, exclude, reverseExclude, hpCheck);
            if (target != null && !head.hasLineOfSight(target)) {
                target = null;
            }
            if (plugin.drone_targets.get(uuid) != target) {
                plugin.drone_targets.put(uuid, target);
            }
            final Location location = player.getLocation().add(0, 2, 0);
            float yaw = location.getYaw();
            final Vector direction = location.getDirection();
            final double xD = Math.sin(-0.0175 * yaw + 1.575) + location.getX();
            final double zD = Math.cos(-0.0175 * yaw + 1.575) + location.getZ();
            final Location tp = new Location(player.getWorld(), xD, location.getY(), zD);
            if (target != null) {
                if (lookAI) {
                    final Location targetLocation = target.getLocation();
                    plugin.armorStandManager.lookAT(head, targetLocation.clone());
                    head.teleport(tp.setDirection(targetLocation.toVector().subtract(head.getLocation().toVector()).normalize()));
                } else {
                    head.teleport(tp.setDirection(direction));
                }
                plugin.armorStandManager.setCustomName(playerConnect, drone_level, group, file, "target", player);
            } else {
                head.setHeadPose(eulerAngle);
                head.teleport(tp.setDirection(direction));
                plugin.armorStandManager.setCustomName(playerConnect, drone_level, group, file, "searching", player);
            }
            name.teleport(tp.add(0, 0.3, 0));
        }, 5, 1).getTaskId();
    }
}
