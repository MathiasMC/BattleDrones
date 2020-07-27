package me.MathiasMC.BattleDrones.listeners;

import me.MathiasMC.BattleDrones.BattleDrones;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityDamageByEntity implements Listener {

    private final BattleDrones plugin;

    public EntityDamageByEntity(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntity(EntityDamageByEntityEvent e) {
        plugin.shieldGenerator.onEntity(e);
    }
}
