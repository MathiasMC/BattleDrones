package me.MathiasMC.BattleDrones.listeners;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class PlayerSwapHandItems implements Listener {

    private final BattleDrones plugin;

    public PlayerSwapHandItems(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent e) {
        final Player player = e.getPlayer();
        final String uuid = player.getUniqueId().toString();
        if (plugin.list().contains(uuid)) {
            if (plugin.config.get.getBoolean("swap.shift") && !player.isSneaking()) {
                return;
            }
            final PlayerConnect playerConnect = plugin.get(uuid);
            if (!playerConnect.hasActive()) {
                if (playerConnect.hasLast_active()) {

                    if (!plugin.config.get.getBoolean("swap.automatic")) {
                        plugin.manual.add(uuid);
                    }

                    playerConnect.setActive(playerConnect.getLast_active());
                    plugin.droneManager.spawnDrone(player, playerConnect.getLast_active(), false, true);
                    for (String message : plugin.language.get.getStringList("swap.activate")) {
                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", player.getName()).replace("{drone}", plugin.internalPlaceholders.getActiveDrone(playerConnect.getLast_active()))));
                    }
                }
            } else {
                if (plugin.config.get.getInt("swap.cost") != 0) {
                    final long coins = playerConnect.getCoins();
                    final long cost = plugin.config.get.getLong("swap.cost");
                    if (!plugin.config.get.getBoolean("vault") && coins >= cost ||
                            plugin.config.get.getBoolean("vault") &&
                                    plugin.getEconomy() != null &&
                                    plugin.getEconomy().withdrawPlayer(player, cost).transactionSuccess()) {
                        if (!plugin.config.get.getBoolean("vault")) {
                            playerConnect.setCoins(coins - cost);
                        }
                        for (String message : plugin.language.get.getStringList("swap.deactivate-cost")) {
                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", player.getName()).replace("{drone}", plugin.internalPlaceholders.getActiveDrone(playerConnect.getActive())).replace("{cost}", String.valueOf(cost))));
                        }
                    } else {
                        for (String message : plugin.language.get.getStringList("swap.enough")) {
                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", player.getName())));
                        }
                        return;
                    }
                } else {
                    for (String message : plugin.language.get.getStringList("swap.deactivate")) {
                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", player.getName()).replace("{drone}", plugin.internalPlaceholders.getActiveDrone(playerConnect.getActive()))));
                    }
                }
                playerConnect.stopDrone();
            }
        }
    }
}
