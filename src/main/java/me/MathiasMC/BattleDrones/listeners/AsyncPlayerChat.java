package me.MathiasMC.BattleDrones.listeners;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;

public class AsyncPlayerChat implements Listener {

    private final BattleDrones plugin;

    public AsyncPlayerChat(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        final Player player = e.getPlayer();
        final String uuid = player.getUniqueId().toString();
        if (plugin.drone_whitelist.containsKey(uuid)) {
                final DroneHolder droneHolder = plugin.getDroneHolder(uuid, plugin.drone_whitelist.get(uuid));
                final List<String> players = droneHolder.getExclude();
                final String message = e.getMessage().toLowerCase();
                if (players != null && !players.contains(message)) {
                    if (!player.getName().toLowerCase().equalsIgnoreCase(message)) {
                        players.add(message);
                        droneHolder.setExclude(players);
                        droneHolder.save();
                        plugin.getServer().getScheduler().runTask(plugin, () -> {
                            for (String command : plugin.language.get.getStringList("gui.whitelist.add")) {
                                BattleDrones.call.getServer().dispatchCommand(BattleDrones.call.consoleSender, command.replace("{player}", player.getName()).replace("{name}", message));
                            }
                        });
                    } else {
                        plugin.getServer().getScheduler().runTask(plugin, () -> {
                            for (String command : plugin.language.get.getStringList("gui.whitelist.own")) {
                                BattleDrones.call.getServer().dispatchCommand(BattleDrones.call.consoleSender, command.replace("{player}", player.getName()).replace("{name}", message));
                            }
                        });
                    }
                } else {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                    for (String command : plugin.language.get.getStringList("gui.whitelist.same")) {
                        BattleDrones.call.getServer().dispatchCommand(BattleDrones.call.consoleSender, command.replace("{player}", player.getName()).replace("{name}", message));
                    }
                    });
                }
            e.setCancelled(true);
            plugin.drone_whitelist.remove(uuid);
        }
    }
}