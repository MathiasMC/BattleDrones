package me.MathiasMC.BattleDrones.managers;

import me.MathiasMC.BattleDrones.BattleDrones;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;

import java.util.List;

public class DroneManager {

    private final BattleDrones plugin;

    public DroneManager(final BattleDrones plugin) {
        this.plugin = plugin;
    }



    public void waitSchedule(final String uuid, final FileConfiguration file) {
        plugin.drone_wait.add(uuid);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.drone_wait.remove(uuid), file.getInt("gui.WAIT-SECONDS") * 20);
    }

    public boolean canBypassDroneAmount(final Player player) {
        return plugin.drone_amount.size() < plugin.getFileUtils().config.getInt("drone-amount") || player.hasPermission("battledrones.bypass.drone-amount");
    }

    public void runCommands(final Player player, FileConfiguration file, final String path) {
        if (!file.contains(path)) {
            return;
        }
        for (String command : file.getStringList(path)) {
            dispatchCommand(player, command);
        }
    }

    public void runCommands(final Player player, final List<String> list) {
        for (String command : list) {
            dispatchCommand(player, command);
        }
    }

    private void dispatchCommand(final Player player, final String command) {
        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', plugin.getPlaceholderManager().replacePlaceholders(player, command)));
    }

}