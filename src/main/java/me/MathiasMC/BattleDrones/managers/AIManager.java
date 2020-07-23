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

    public void defaultAI(final Player player, PlayerConnect playerConnect, FileConfiguration file, long drone_level, int monsters, int animals, int players, List<String> exclude, boolean reverseExclude, boolean hpCheck, boolean lookAI) {
        final String uuid = player.getUniqueId().toString();
        final String group = playerConnect.getGroup();
        playerConnect.AItaskID = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            LivingEntity target = plugin.armorStandManager.getClose(player, file.getInt(group + "." + drone_level + ".range"), monsters, animals, players, exclude, reverseExclude, hpCheck);
            if (plugin.drone_targets.get(uuid) != target) {
                plugin.drone_targets.put(uuid, target);
            }
            Location location = player.getLocation().add(0, 2, 0);
            float yaw = player.getLocation().getYaw();
            Vector direction = player.getLocation().getDirection();
            double xD = Math.sin(-0.0175 * yaw + 1.575) + location.getX();
            double zD = Math.cos(-0.0175 * yaw + 1.575) + location.getZ();
            if (target != null) {
                ArmorStand head = playerConnect.head;
                ArmorStand name = playerConnect.name;
                Location tp = new Location(player.getWorld(), xD, location.getY(), zD);
                if (lookAI) {
                    plugin.armorStandManager.lookAT(head, target.getLocation());
                    head.teleport(tp.setDirection(target.getLocation().toVector().subtract(head.getLocation().toVector()).normalize()));
                } else {
                    head.teleport(tp.setDirection(direction));
                }
                name.teleport(tp.clone().add(0, 0.3, 0));
                plugin.armorStandManager.setCustomName(playerConnect, drone_level, group, file, "target", player);
            } else {
                ArmorStand head = playerConnect.head;
                ArmorStand name = playerConnect.name;
                head.setHeadPose(new EulerAngle(0, 0, 0));
                Location tp = new Location(player.getWorld(), xD, location.getY(), zD);
                head.teleport(tp.setDirection(direction));
                name.teleport(tp.clone().add(0, 0.3, 0));
                plugin.armorStandManager.setCustomName(playerConnect, drone_level, group, file, "searching", player);
            }
        }, 5, 1).getTaskId();
    }
}
