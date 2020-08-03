package me.MathiasMC.BattleDrones.gui.shop;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.gui.GUI;
import me.MathiasMC.BattleDrones.gui.Menu;
import me.MathiasMC.BattleDrones.gui.player.PlayerGUI;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Objects;

public class ShopGUI extends GUI {

    private final BattleDrones plugin = BattleDrones.call;
    private final FileConfiguration file;
    private final Player player = playerMenu.getPlayer();
    private final String uuid = playerMenu.getUuid();

    public ShopGUI(Menu playerMenu) {
        super(playerMenu);
        this.file =  plugin.guiFiles.get("shop");
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
        if (file.contains(String.valueOf(slot))) {
            if (file.getStringList(slot + ".OPTIONS").contains("DRONE_SHOP_ENERGY")) {
                BattleDrones.call.loadDroneHolder(uuid, "laser");
                new ShopEnergyGUI(BattleDrones.call.getPlayerMenu(player)).open();
            } else if (file.getStringList(slot + ".OPTIONS").contains("DRONE_SHOP_KINETIC")) {
                BattleDrones.call.loadDroneHolder(uuid, "machine_gun");
                new ShopKineticGUI(BattleDrones.call.getPlayerMenu(player)).open();
            } else if (file.getStringList(slot + ".OPTIONS").contains("DRONE_SHOP_EXPLODE")) {
                BattleDrones.call.loadDroneHolder(uuid, "rocket");
                BattleDrones.call.loadDroneHolder(uuid, "faf_missile");
                BattleDrones.call.loadDroneHolder(uuid, "mortar");
                new ShopExplodeGUI(BattleDrones.call.getPlayerMenu(player)).open();
            } else if (file.getStringList(slot + ".OPTIONS").contains("DRONE_SHOP_PROTECTIVE")) {
                BattleDrones.call.loadDroneHolder(uuid, "shield_generator");
                BattleDrones.call.loadDroneHolder(uuid, "healing");
                new ShopProtectiveGUI(BattleDrones.call.getPlayerMenu(player)).open();
            } else if (file.getStringList(slot + ".OPTIONS").contains("DRONE_SHOP_SPECIAL")) {
                BattleDrones.call.loadDroneHolder(uuid, "flamethrower");
                new ShopSpecialGUI(BattleDrones.call.getPlayerMenu(player)).open();
            } else if (file.getStringList(slot + ".OPTIONS").contains("DRONE_PLAYER")) {
                new PlayerGUI(BattleDrones.call.getPlayerMenu(player)).open();
            }
            BattleDrones.call.guiManager.dispatchCommand(file, slot, player);
        }
    }

    @Override
    public void setItems() {
        BattleDrones.call.guiManager.setGUIItemStack(inventory, file, player);
    }
}