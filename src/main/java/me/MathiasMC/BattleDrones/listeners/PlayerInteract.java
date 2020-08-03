package me.MathiasMC.BattleDrones.listeners;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
                        if (!e.getPlayer().getUniqueId().toString().equalsIgnoreCase(key)) {
                            if (!plugin.locationSupport.inWorldGuardRegion(entity)) {
                                return;
                            }
                            final PlayerConnect playerConnect = plugin.get(key);
                            final DroneHolder droneHolder = plugin.getDroneHolder(key, playerConnect.getActive());
                            if (droneHolder.getHealth() - 1 >= 0) {
                                droneHolder.setHealth(droneHolder.getHealth() - 1);
                                hitCommands(armorStand, plugin.droneFiles.get(playerConnect.getActive()), playerConnect.getGroup(), droneHolder.getLevel());
                                return;
                            }
                            droneDeath(plugin.getServer().getPlayer(UUID.fromString(key)), playerConnect.getActive(), playerConnect, droneHolder);
                        }
                    }
                }
                break;
            }
        }
    }

    private void hitCommands(ArmorStand armorStand, FileConfiguration file, String group, long drone_level) {
        for (String command : file.getStringList(group+ "." + drone_level + ".hit-commands")) {
            BattleDrones.call.getServer().dispatchCommand(BattleDrones.call.consoleSender, command
                    .replace("{world}", armorStand.getWorld().getName())
                    .replace("{x}", String.valueOf(armorStand.getLocation().getBlockX()))
                    .replace("{y}", String.valueOf(armorStand.getLocation().getBlockY()))
                    .replace("{z}", String.valueOf(armorStand.getLocation().getBlockZ())));
        }
    }

    private void droneDeath(Player target, String type, PlayerConnect playerConnect, DroneHolder droneHolder) {
        if (target != null) {
            for (String command : BattleDrones.call.config.get.getStringList("dead.player")) {
                BattleDrones.call.getServer().dispatchCommand(BattleDrones.call.consoleSender, command.replace("{player}", target.getName()));
            }
        }
        droneHolder.setUnlocked(BattleDrones.call.config.get.getInt("dead.unlocked"));
        if (BattleDrones.call.config.get.getLong("dead.set-level") != 0) {
            droneHolder.setLevel(BattleDrones.call.config.get.getInt("dead.set-level"));
        }
        if (!BattleDrones.call.config.get.getBoolean("dead.ammo")) {
            droneHolder.setAmmo(0);
        }
        playerConnect.stopDrone();
    }
}