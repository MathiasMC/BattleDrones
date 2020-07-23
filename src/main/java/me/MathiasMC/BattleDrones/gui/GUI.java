package me.MathiasMC.BattleDrones.gui;

import me.MathiasMC.BattleDrones.BattleDrones;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public abstract class GUI implements InventoryHolder {

    protected Menu playerMenu;
    protected Inventory inventory;

    public abstract String getName();
    public abstract int getSize();
    public abstract void click(InventoryClickEvent e);
    public abstract void setItems();

    public GUI(Menu playerMenu) {
        this.playerMenu = playerMenu;
    }

    public void open() {
        inventory = BattleDrones.call.getServer().createInventory(this, getSize(), getName());
        this.setItems();
        playerMenu.getPlayer().openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
