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

    public ProjectileHit(BattleDrones plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onHit(ProjectileHitEvent e) {
        if (!(e.getEntity() instanceof Arrow arrow)) return;
        if (!(e.getHitEntity() instanceof ArmorStand armorStand)) return;

        String key = armorStand.getPersistentDataContainer().get(
                new NamespacedKey(plugin, "drone_uuid"),
                PersistentDataType.STRING
        );
        if (key == null) return;

        Player shooter = (arrow.getShooter() instanceof Player player) ? player : null;
        if (shooter != null && shooter.getUniqueId().toString().equalsIgnoreCase(key)) return;

        arrow.remove();
        plugin.getDroneManager().damage(shooter, key, armorStand);
    }
}