package me.MathiasMC.BattleDrones.api;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public abstract class DroneRegistry {

    private final BattleDrones plugin;

    private final Plugin registeredPlugin;

    public final String droneName;

    public final String droneCategory;

    public DroneRegistry(final Plugin plugin, final String droneName, final String droneCategory) {
        this.droneName = droneName;
        this.droneCategory = droneCategory;
        this.registeredPlugin = plugin;
        this.plugin = BattleDrones.getInstance();
    }

    public void register() {
        plugin.getFileUtils().initialize(registeredPlugin, droneName, droneCategory);
        plugin.database.createDroneTable(droneName);
        plugin.droneRegistry.put(droneName, this);
        List<String> list = new ArrayList<>();
        if (plugin.category.containsKey(droneCategory)) {
            list = plugin.category.get(droneCategory);
        }
        list.add(droneName);
        plugin.category.put(droneCategory, list);
        plugin.getFileUtils().loadDroneFiles();
        plugin.getFileUtils().loadGUIFiles();
        final String name = plugin.droneFiles.get(droneName).getString("name");
        plugin.drones.put(name, droneName);
        if (registeredPlugin == plugin) {
            plugin.getTextUtils().info(name + " in category " + droneCategory + " registered");
        } else {
            plugin.getTextUtils().info(registeredPlugin.getName() + " " + name + " in category " + droneCategory + " registered");
        }
    }

    public abstract void ability(final Player player, final PlayerConnect playerConnect, final DroneHolder droneHolder);

    public void find(final Player player, final PlayerConnect playerConnect, final DroneHolder droneHolder) {
        plugin.getTaskManager().find(player, playerConnect, droneHolder);
    }

    public void follow(final Player player, final PlayerConnect playerConnect, final DroneHolder droneHolder) {
        plugin.getTaskManager().follow(player, playerConnect, droneHolder);
    }

    public void healing(final Player player, final PlayerConnect playerConnect, final DroneHolder droneHolder) {
        plugin.getTaskManager().healing(player, playerConnect, droneHolder);
    }

    public String onPlaceholderRequest(final Player player, final PlayerConnect playerConnect, final DroneHolder droneHolder, final String placeholder) {
        return plugin.getPlaceholderManager().onPlaceholderRequest(playerConnect, droneHolder, placeholder);
    }
}
