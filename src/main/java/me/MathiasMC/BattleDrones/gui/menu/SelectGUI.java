package me.MathiasMC.BattleDrones.gui.menu;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.gui.GUI;
import me.MathiasMC.BattleDrones.gui.Menu;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Objects;

public class SelectGUI extends GUI {

    private final BattleDrones plugin;
    private final FileConfiguration file;
    private final Player player = playerMenu.getPlayer();

    private final String type;

    public SelectGUI(Menu playerMenu, String type) {
        super(playerMenu);
        this.plugin = BattleDrones.getInstance();
        this.type = type;
        this.file = plugin.guiFiles.get(type);
    }

    @Override
    public boolean isRegistered() {
        return this.file != null;
    }

    @Override
    public String getName() {
        if (file != null) {
            return ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(file.getString("settings.name")));
        }
        return "";
    }

    @Override
    public int getSize() {
        return file.getInt("settings.size");
    }

    @Override
    public void click(InventoryClickEvent e) {
        final int slot = e.getSlot();
        if (!file.contains(String.valueOf(slot))) {
            return;
        }
        final ItemMeta itemMeta = e.getCurrentItem().getItemMeta();
        if (itemMeta == null) {
            return;
        }
        final List<String> list = file.getStringList(slot + ".OPTIONS");
        if (list.contains("DRONE_SHOP")) {
            new SelectGUI(plugin.getPlayerMenu(player), "shop").open();
        }
        if (list.contains("DRONE_PLAYER")) {
            new SelectGUI(plugin.getPlayerMenu(player), "player").open();
        }
        plugin.getItemStackManager().dispatchCommand(file, slot, player);
        final String drone = itemMeta.getPersistentDataContainer().get(new NamespacedKey(plugin, "drone"), PersistentDataType.STRING);
        if (drone == null) {
            return;
        }
        if (type.equalsIgnoreCase("shop")) {
            new ShopGUI(plugin.getPlayerMenu(player), drone).open();
        } else {
            new MenuGUI(plugin.getPlayerMenu(player), drone).open();
        }
    }

    @Override
    public void setItems() {
        plugin.getItemStackManager().setupGUI(inventory, file, player);
    }
}