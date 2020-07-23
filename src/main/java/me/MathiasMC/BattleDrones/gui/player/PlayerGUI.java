package me.MathiasMC.BattleDrones.gui.player;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.gui.GUI;
import me.MathiasMC.BattleDrones.gui.Menu;
import me.MathiasMC.BattleDrones.gui.shop.ShopGUI;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Objects;

public class PlayerGUI extends GUI {

    private final FileConfiguration file = BattleDrones.call.guiFiles.get("player");

    public PlayerGUI(Menu playerMenu) {
        super(playerMenu);
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
            final Player player = playerMenu.getPlayer();
            if (file.getStringList(slot + ".OPTIONS").contains("DRONE_PLAYER_ENERGY")) {
                BattleDrones.call.loadDroneHolder(playerMenu.getUuid(), "laser");
                new EnergyGUI(BattleDrones.call.getPlayerMenu(player)).open();
            } else if (file.getStringList(slot + ".OPTIONS").contains("DRONE_PLAYER_KINETIC")) {
                BattleDrones.call.loadDroneHolder(playerMenu.getUuid(), "machine_gun");
                new KineticGUI(BattleDrones.call.getPlayerMenu(player)).open();
            } else if (file.getStringList(slot + ".OPTIONS").contains("DRONE_PLAYER_EXPLODE")) {
                BattleDrones.call.loadDroneHolder(playerMenu.getUuid(), "rocket");
                new ExplodeGUI(BattleDrones.call.getPlayerMenu(player)).open();
            } else if (file.getStringList(slot + ".OPTIONS").contains("DRONE_PLAYER_PROTECTIVE")) {
                BattleDrones.call.loadDroneHolder(playerMenu.getUuid(), "shield_generator");
                BattleDrones.call.loadDroneHolder(playerMenu.getUuid(), "healing");
                new ProtectiveGUI(BattleDrones.call.getPlayerMenu(player)).open();
            } else if (file.getStringList(slot + ".OPTIONS").contains("DRONE_SHOP")) {
                new ShopGUI(BattleDrones.call.getPlayerMenu(player)).open();
            }
        }
    }

    @Override
    public void setItems() {
        BattleDrones.call.guiManager.setGUIItemStack(inventory, file, playerMenu.getPlayer());
    }
}