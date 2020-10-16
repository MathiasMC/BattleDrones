package me.MathiasMC.BattleDrones.gui;

import me.MathiasMC.BattleDrones.BattleDrones;
import org.bukkit.ChatColor;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public abstract class GUI implements InventoryHolder {

    private final BattleDrones plugin;

    protected Menu playerMenu;
    protected Inventory inventory;

    public abstract String getName();
    public abstract int getSize();
    public abstract void click(InventoryClickEvent e);
    public abstract void setItems();

    public abstract boolean isRegistered();

    public GUI(Menu playerMenu) {
        this.plugin = BattleDrones.getInstance();
        this.playerMenu = playerMenu;
    }

    public void open() {
        if (!isRegistered()) {

            playerMenu.getPlayer().sendMessage(ChatColor.RED + "Sorry this is not registered yet");

            return;
        }
        inventory = plugin.getServer().createInventory(this, getSize(), getName());
        this.setItems();
        playerMenu.getPlayer().openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
