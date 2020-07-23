package me.MathiasMC.BattleDrones.drones;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashSet;

public class ShieldGenerator {

    private final BattleDrones plugin;

    public ShieldGenerator(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    public final HashSet<String> cooldown = new HashSet<>();

    public void shot(final Player player) {
        final String uuid = player.getUniqueId().toString();
        final PlayerConnect playerConnect = plugin.get(uuid);
        final DroneHolder droneHolder = plugin.getDroneHolder(uuid, "shield_generator");
        final String group = playerConnect.getGroup();
        final FileConfiguration shield_generator = plugin.droneFiles.get("shield_generator");
        final String path = group + "." + droneHolder.getLevel() + ".";
        if (shield_generator.contains(path + "particle-circle.timer")) {
            double radius = shield_generator.getInt(path + "particle-circle.timer.radius");
            int size = shield_generator.getInt(path + "particle-circle.timer.size");
            int amount = shield_generator.getInt(path + "particle-circle.timer.amount");
            int rows = shield_generator.getInt(path + "particle-circle.timer.rows");
            int r = shield_generator.getInt(path + "particle-circle.timer.rgb.r");
            int g = shield_generator.getInt(path + "particle-circle.timer.rgb.g");
            int b = shield_generator.getInt(path + "particle-circle.timer.rgb.b");
            playerConnect.ShootTaskID = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
                if (!cooldown.contains(uuid) && droneHolder.getAmmo() > 0) {
                    plugin.calculateManager.sphere(playerConnect.head.getLocation().add(0, 0.4, 0), radius, rows, r, g, b, size, amount);
                }
            }, 0, shield_generator.getInt(path + "particle-circle.timer.delay")).getTaskId();
        }
    }
}
