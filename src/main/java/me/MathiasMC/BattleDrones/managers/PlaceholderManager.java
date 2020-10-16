package me.MathiasMC.BattleDrones.managers;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Objects;

public class PlaceholderManager {

    private final BattleDrones plugin;

    public PlaceholderManager(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    public String getActiveDrone(final String active) {
        return ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(plugin.droneFiles.get(active).getString("drone-name")));
    }

    public int getDroneHealth(final String uuid) {
        final PlayerConnect playerConnect = plugin.getPlayerConnect(uuid);
        if (playerConnect.isActive()) {
            return plugin.getDroneHolder(uuid, playerConnect.getActive()).getHealth();
        }
        return 0;
    }

    public int getDroneMaxHealth(final String uuid) {
        final PlayerConnect playerConnect = plugin.getPlayerConnect(uuid);
        if (playerConnect.isActive()) {
            DroneHolder droneHolder = plugin.getDroneHolder(uuid, playerConnect.getActive());
            return plugin.droneFiles.get(playerConnect.getActive()).getInt(playerConnect.getGroup() + "." + droneHolder.getLevel() + ".health");
        }
        return 0;
    }

    public String getDroneHealthBar(final String uuid) {
        final PlayerConnect playerConnect = plugin.getPlayerConnect(uuid);
        if (playerConnect.isActive()) {
            DroneHolder droneHolder = plugin.getDroneHolder(uuid, playerConnect.getActive());
            return plugin.getCalculateManager().getBar(droneHolder.getHealth(), plugin.droneFiles.get(playerConnect.getActive()).getInt(playerConnect.getGroup() + "." + droneHolder.getLevel() + ".health"), "health", "-placeholder");
        }
        return "";
    }

    public int getDroneMaxAmmo(final String uuid) {
        final PlayerConnect playerConnect = plugin.getPlayerConnect(uuid);
        if (playerConnect.isActive()) {
            DroneHolder droneHolder = plugin.getDroneHolder(uuid, playerConnect.getActive());
            return plugin.droneFiles.get(playerConnect.getActive()).getInt(playerConnect.getGroup() + "." + droneHolder.getLevel() + ".max-ammo-slots") * 64;
        }
        return 0;
    }

    public int getDroneAmmo(final String uuid) {
        final PlayerConnect playerConnect = plugin.getPlayerConnect(uuid);
        if (playerConnect.isActive()) {
            DroneHolder droneHolder = plugin.getDroneHolder(uuid, playerConnect.getActive());
            return droneHolder.getAmmo();
        }
        return 0;
    }

    public String getDroneAmmoBar(final String uuid) {
        final PlayerConnect playerConnect = plugin.getPlayerConnect(uuid);
        if (playerConnect.isActive()) {
            DroneHolder droneHolder = plugin.getDroneHolder(uuid, playerConnect.getActive());
            return plugin.getCalculateManager().getBar(droneHolder.getAmmo(), plugin.droneFiles.get(playerConnect.getActive()).getInt(playerConnect.getGroup() + "." + droneHolder.getLevel() + ".max-ammo-slots") * 64, "ammo", "-placeholder");
        }
        return "";
    }

    public String replacePlaceholders(final Player player, String message) {
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            message = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, message);
        }
        final Location location = player.getLocation();
        final PlayerConnect playerConnect = plugin.getPlayerConnect(player.getUniqueId().toString());
        message = message
                .replace("{player}", player.getName())
                .replace("{coins}", String.valueOf(playerConnect.getCoins()))
                .replace("{world}", player.getWorld().getName())
                .replace("{x}", String.valueOf(location.getBlockX()))
                .replace("{y}", String.valueOf(location.getBlockY()))
                .replace("{z}", String.valueOf(location.getBlockZ()));
        if (playerConnect.isActive()) {
            message = message.replace("{drone}", getActiveDrone(playerConnect.getActive()));
        }
        return message;
    }
}
