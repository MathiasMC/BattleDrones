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

public class ShopSpecialGUI extends GUI {

    private final BattleDrones plugin = BattleDrones.call;
    private final FileConfiguration file;
    private final Player player = playerMenu.getPlayer();
    private final String uuid = playerMenu.getUuid();
    private final PlayerConnect playerConnect = playerMenu.getPlayerConnect();
    private final String flamethrower_id;
    private final String lightning_id;

    public ShopSpecialGUI(Menu playerMenu) {
        super(playerMenu);
        this.file = plugin.guiFiles.get("shop_special");
        this.flamethrower_id = "flamethrower";
        this.lightning_id = "lightning";
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
            if (file.getStringList(slot + ".OPTIONS").contains("DRONE_FLAMETHROWER_BUY")) {
                plugin.guiManager.shopGUI(slot, player, uuid, playerConnect, file, flamethrower_id, flamethrower_id);
            } else if (file.getStringList(slot + ".OPTIONS").contains("DRONE_LIGHTNING_BUY")) {
                plugin.guiManager.shopGUI(slot, player, uuid, playerConnect, file, lightning_id, lightning_id);
            } else if (file.getStringList(slot + ".OPTIONS").contains("BACK")) {
                new ShopGUI(plugin.getPlayerMenu(player)).open();
            }
        }
    }

    @Override
    public void setItems() {
        plugin.guiManager.setGUIItemStack(inventory, file, player);
    }
}