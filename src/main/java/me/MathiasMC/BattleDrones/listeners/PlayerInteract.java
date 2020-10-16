package me.MathiasMC.BattleDrones.listeners;

import me.MathiasMC.BattleDrones.BattleDrones;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;

public class PlayerInteract implements Listener {


    private final BattleDrones plugin;

    public PlayerInteract(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInteract(PlayerInteractEvent e) {
        final Player player = e.getPlayer();
        plugin.getDroneControllerManager().selectTarget(player, e.getHand(), e.getAction());
        for (Entity entity : player.getNearbyEntities(2, 2, 2)) {
            if (entity instanceof ArmorStand) {
                final ArmorStand armorStand = (ArmorStand) entity;
                final String key = armorStand.getPersistentDataContainer().get(new NamespacedKey(plugin, "drone_uuid"), PersistentDataType.STRING);
                if (key != null) {
                    if (getEntityLook(player, armorStand)) {
                        e.setCancelled(true);
                        if (!player.getUniqueId().toString().equalsIgnoreCase(key) && e.getAction() == Action.LEFT_CLICK_AIR) {
                            plugin.getDroneManager().damage(player, key, armorStand);
                        }
                    }
                }
                break;
            }
        }
    }

    private boolean getEntityLook(final Player player, final Entity entity) {
        final Location location = player.getEyeLocation();
        return entity.getLocation().add(0, 1, 0).toVector().subtract(location.toVector()).normalize().dot(location.getDirection()) > 0.95D;
    }
}