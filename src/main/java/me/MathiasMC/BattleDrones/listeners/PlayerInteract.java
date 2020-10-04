package me.MathiasMC.BattleDrones.listeners;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class PlayerInteract implements Listener {


    private final BattleDrones plugin;

    public PlayerInteract(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        for (Entity entity : e.getPlayer().getNearbyEntities(2, 2, 2)) {
            if (entity instanceof ArmorStand) {
                final ArmorStand armorStand = (ArmorStand) entity;
                final String key = armorStand.getPersistentDataContainer().get(new NamespacedKey(plugin, "drone_uuid"), PersistentDataType.STRING);
                if (key != null) {
                    if (plugin.getEntityLook(e.getPlayer(), entity)) {
                        e.setCancelled(true);
                        if (!e.getPlayer().getUniqueId().toString().equalsIgnoreCase(key) && e.getAction() == Action.LEFT_CLICK_AIR) {
                            final PlayerConnect playerConnect = plugin.get(key);
                            final DroneHolder droneHolder = plugin.getDroneHolder(key, playerConnect.getActive());
                            if (plugin.locationSupport.worldGuard != null && plugin.config.get.getBoolean("worldguard.use") && plugin.config.get.contains("worldguard." + playerConnect.getActive() + "." + droneHolder.getLevel() + ".damage") && plugin.locationSupport.inWorldGuardRegion(entity, plugin.config.get.getStringList("worldguard." + playerConnect.getActive() + "." + droneHolder.getLevel() + ".damage"))) {
                                return;
                            }
                            if (droneHolder.getHealth() - 1 >= 0) {
                                droneHolder.setHealth(droneHolder.getHealth() - 1);
                                hitCommands(armorStand, plugin.droneFiles.get(playerConnect.getActive()), playerConnect.getGroup(), droneHolder.getLevel());
                                return;
                            }
                            droneDeath(plugin.getServer().getPlayer(UUID.fromString(key)), playerConnect, droneHolder);
                        }
                    }
                }
                break;
            }
        }
    }

    private void hitCommands(final ArmorStand armorStand, final FileConfiguration file, final String group, final long drone_level) {
        for (String command : file.getStringList(group+ "." + drone_level + ".hit-commands")) {
            plugin.getServer().dispatchCommand(plugin.consoleSender, command
                    .replace("{world}", armorStand.getWorld().getName())
                    .replace("{x}", String.valueOf(armorStand.getLocation().getBlockX()))
                    .replace("{y}", String.valueOf(armorStand.getLocation().getBlockY()))
                    .replace("{z}", String.valueOf(armorStand.getLocation().getBlockZ())));
        }
    }

    private void droneDeath(final Player target, final PlayerConnect playerConnect, final DroneHolder droneHolder) {
        if (target != null) {
            for (String command : plugin.config.get.getStringList("dead.player")) {
                plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", target.getName()));
            }
        }
        droneHolder.setUnlocked(plugin.config.get.getInt("dead.unlocked"));
        if (plugin.config.get.getLong("dead.set-level") != 0) {
            droneHolder.setLevel(plugin.config.get.getInt("dead.set-level"));
        }
        if (!plugin.config.get.getBoolean("dead.ammo")) {
            droneHolder.setAmmo(0);
        }
        playerConnect.stopDrone();
    }
}