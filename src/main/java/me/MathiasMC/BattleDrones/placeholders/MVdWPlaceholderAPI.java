package me.MathiasMC.BattleDrones.placeholders;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import me.MathiasMC.BattleDrones.BattleDrones;
import org.bukkit.OfflinePlayer;

public class MVdWPlaceholderAPI {

    public void register(final BattleDrones plugin) {
        PlaceholderAPI.registerPlaceholder(plugin, "battledrones_coins",
                event -> {
                    OfflinePlayer offlinePlayer = event.getPlayer();
                    if (offlinePlayer == null) {
                        return "Player needed!";
                    }
                    return String.valueOf(plugin.get(offlinePlayer.getUniqueId().toString()).getCoins());
                });
        PlaceholderAPI.registerPlaceholder(plugin, "battledrones_group",
                event -> {
                    OfflinePlayer offlinePlayer = event.getPlayer();
                    if (offlinePlayer == null) {
                        return "Player needed!";
                    }
                    return String.valueOf(plugin.get(offlinePlayer.getUniqueId().toString()).getGroup());
                });
        PlaceholderAPI.registerPlaceholder(plugin, "battledrones_active",
                event -> {
                    OfflinePlayer offlinePlayer = event.getPlayer();
                    if (offlinePlayer == null) {
                        return "Player needed!";
                    }
                    return plugin.internalPlaceholders.getActiveDrone(plugin.get(offlinePlayer.getUniqueId().toString()).getActive());
                });
        PlaceholderAPI.registerPlaceholder(plugin, "battledrones_health",
                event -> {
                    OfflinePlayer offlinePlayer = event.getPlayer();
                    if (offlinePlayer == null) {
                        return "Player needed!";
                    }
                    return String.valueOf(plugin.internalPlaceholders.getDroneHealth(offlinePlayer.getUniqueId().toString()));
                });
        PlaceholderAPI.registerPlaceholder(plugin, "battledrones_health_max",
                event -> {
                    OfflinePlayer offlinePlayer = event.getPlayer();
                    if (offlinePlayer == null) {
                        return "Player needed!";
                    }
                    return String.valueOf(plugin.internalPlaceholders.getDroneMaxHealth(offlinePlayer.getUniqueId().toString()));
                });
        PlaceholderAPI.registerPlaceholder(plugin, "battledrones_health_percent",
                event -> {
                    OfflinePlayer offlinePlayer = event.getPlayer();
                    if (offlinePlayer == null) {
                        return "Player needed!";
                    }
                    final String uuid = offlinePlayer.getUniqueId().toString();
                    return String.valueOf(plugin.calculateManager.getPercent(plugin.internalPlaceholders.getDroneHealth(uuid), plugin.internalPlaceholders.getDroneMaxHealth(uuid)));
                });
        PlaceholderAPI.registerPlaceholder(plugin, "battledrones_health_bar",
                event -> {
                    OfflinePlayer offlinePlayer = event.getPlayer();
                    if (offlinePlayer == null) {
                        return "Player needed!";
                    }
                    return plugin.internalPlaceholders.getDroneHealthBar(offlinePlayer.getUniqueId().toString());
                });
        PlaceholderAPI.registerPlaceholder(plugin, "battledrones_ammo",
                event -> {
                    OfflinePlayer offlinePlayer = event.getPlayer();
                    if (offlinePlayer == null) {
                        return "Player needed!";
                    }
                    return String.valueOf(plugin.internalPlaceholders.getDroneAmmo(offlinePlayer.getUniqueId().toString()));
                });
        PlaceholderAPI.registerPlaceholder(plugin, "battledrones_ammo_max",
                event -> {
                    OfflinePlayer offlinePlayer = event.getPlayer();
                    if (offlinePlayer == null) {
                        return "Player needed!";
                    }
                    return String.valueOf(plugin.internalPlaceholders.getDroneMaxAmmo(offlinePlayer.getUniqueId().toString()));
                });
    }
}
