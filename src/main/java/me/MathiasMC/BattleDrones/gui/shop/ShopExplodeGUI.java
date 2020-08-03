package me.MathiasMC.BattleDrones.gui.shop;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import me.MathiasMC.BattleDrones.gui.GUI;
import me.MathiasMC.BattleDrones.gui.Menu;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Objects;

public class ShopExplodeGUI extends GUI {

    private final BattleDrones plugin = BattleDrones.call;
    private final FileConfiguration file;
    private final Player player = playerMenu.getPlayer();
    private final String uuid = playerMenu.getUuid();
    private final PlayerConnect playerConnect = playerMenu.getPlayerConnect();
    private final String rocket_id;
    private final String faf_missile_id;
    private final String mortar_id;

    public ShopExplodeGUI(Menu playerMenu) {
        super(playerMenu);
        this.file = plugin.guiFiles.get("shop_explode");
        this.rocket_id = "rocket";
        this.faf_missile_id = "faf_missile";
        this.mortar_id = "mortar";
    }

    @Override
    public String getName() {
        return ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(file.getString("settings.name")));
    }

    @Override
    public int getSize() {
        return file.getInt("settings.size");
    }

    @Override
    public void click(InventoryClickEvent e) {
        final int slot = e.getSlot();
        if (file.contains(String.valueOf(slot))) {
            if (file.getStringList(slot + ".OPTIONS").contains("DRONE_ROCKET_BUY")) {
                plugin.guiManager.shopGUI(slot, player, uuid, playerConnect, file, rocket_id, rocket_id);
            } else if (file.getStringList(slot + ".OPTIONS").contains("DRONE_FAFMISSILE_BUY")) {
                plugin.guiManager.shopGUI(slot, player, uuid, playerConnect, file, faf_missile_id, "faf.missile");
            } else if (file.getStringList(slot + ".OPTIONS").contains("DRONE_MORTAR_BUY")) {
                plugin.guiManager.shopGUI(slot, player, uuid, playerConnect, file, mortar_id, mortar_id);
            } else if (file.getStringList(slot + ".OPTIONS").contains("BACK")) {
                new ShopGUI(plugin.getPlayerMenu(player)).open();
            }
            plugin.guiManager.dispatchCommand(file, slot, player);
        }
    }

    @Override
    public void setItems() {
        plugin.guiManager.setGUIItemStack(inventory, file, player);
    }
}