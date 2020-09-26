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

    private final BattleDrones plugin = BattleDrones.call;
    private final FileConfiguration file;
    private final Player player = playerMenu.getPlayer();
    private final String uuid = playerMenu.getUuid();

    public PlayerGUI(Menu playerMenu) {
        super(playerMenu);
        this.file = plugin.guiFiles.get("player");
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
            if (file.getStringList(slot + ".OPTIONS").contains("DRONE_PLAYER_ENERGY")) {
                plugin.loadDroneHolder(uuid, "laser");
                new EnergyGUI(plugin.getPlayerMenu(player)).open();
            } else if (file.getStringList(slot + ".OPTIONS").contains("DRONE_PLAYER_KINETIC")) {
                plugin.loadDroneHolder(uuid, "machine_gun");
                new KineticGUI(plugin.getPlayerMenu(player)).open();
            } else if (file.getStringList(slot + ".OPTIONS").contains("DRONE_PLAYER_EXPLODE")) {
                plugin.loadDroneHolder(uuid, "rocket");
                plugin.loadDroneHolder(uuid, "faf_missile");
                plugin.loadDroneHolder(uuid, "mortar");
                new ExplodeGUI(plugin.getPlayerMenu(player)).open();
            } else if (file.getStringList(slot + ".OPTIONS").contains("DRONE_PLAYER_PROTECTIVE")) {
                plugin.loadDroneHolder(uuid, "shield_generator");
                plugin.loadDroneHolder(uuid, "healing");
                new ProtectiveGUI(plugin.getPlayerMenu(player)).open();
            } else if (file.getStringList(slot + ".OPTIONS").contains("DRONE_PLAYER_SPECIAL")) {
                plugin.loadDroneHolder(uuid, "flamethrower");
                plugin.loadDroneHolder(uuid, "lightning");
                new SpecialGUI(plugin.getPlayerMenu(player)).open();
            } else if (file.getStringList(slot + ".OPTIONS").contains("DRONE_SHOP")) {
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