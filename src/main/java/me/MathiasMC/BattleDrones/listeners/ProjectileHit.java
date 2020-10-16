package me.MathiasMC.BattleDrones.listeners;

import me.MathiasMC.BattleDrones.BattleDrones;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.persistence.PersistentDataType;

public class ProjectileHit implements Listener {

    private final BattleDrones plugin;

    public ProjectileHit(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onHit(ProjectileHitEvent e) {
        if (e.getEntity() instanceof Arrow && e.getHitEntity() instanceof ArmorStand) {
            final Arrow arrow = (Arrow) e.getEntity();
            final ArmorStand armorStand = (ArmorStand) e.getHitEntity();
            final String key = armorStand.getPersistentDataContainer().get(new NamespacedKey(plugin, "drone_uuid"), PersistentDataType.STRING);
            if (key != null) {
                if (arrow.getShooter() instanceof Player) {
                    final Player player = (Player) arrow.getShooter();
                    if (player.getUniqueId().toString().equalsIgnoreCase(key)) {
                        return;
                    }
                }
                arrow.remove();
                if (arrow.getShooter() instanceof Player) {
                    plugin.getDroneManager().damage((Player) arrow.getShooter(), key, armorStand);
                } else {
                    plugin.getDroneManager().damage(null, key, armorStand);
                }
            }
        }
    }
}