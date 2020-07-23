package me.MathiasMC.BattleDrones.listeners;

import me.MathiasMC.BattleDrones.gui.GUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public class InventoryClick implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        InventoryHolder inventoryHolder = e.getInventory().getHolder();
        if (inventoryHolder instanceof GUI) {
            e.setCancelled(true);
            if (e.getCurrentItem() == null) { return; }
            GUI gui = (GUI) inventoryHolder;
            gui.click(e);
        }
    }
}