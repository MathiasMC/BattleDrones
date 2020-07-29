package me.MathiasMC.BattleDrones.gui.player;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import me.MathiasMC.BattleDrones.gui.DroneMenu;
import me.MathiasMC.BattleDrones.gui.GUI;
import me.MathiasMC.BattleDrones.gui.Menu;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.HashMap;
import java.util.Objects;

public class SpecialGUI extends GUI {

    private final FileConfiguration file = BattleDrones.call.guiFiles.get("player_special");
    private final Player player = playerMenu.getPlayer();
    private final String uuid = playerMenu.getUuid();

    public SpecialGUI(Menu playerMenu) {
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
        final PlayerConnect playerConnect = BattleDrones.call.get(uuid);
        final String flamethrowerDrone = "flamethrower";
        final DroneHolder droneHolder = BattleDrones.call.getDroneHolder(uuid, flamethrowerDrone);
        final FileConfiguration laser = BattleDrones.call.droneFiles.get(flamethrowerDrone);
        if (laser.getInt("gui.POSITION") == slot && droneHolder.getUnlocked() == 1) {
            if (player.hasPermission("battledrones.player.flamethrower")) {
                if (e.isLeftClick()) {
                    BattleDrones.call.droneManager.spawnDrone(player, flamethrowerDrone, false, false);
                } else if (e.isRightClick()) {
                    BattleDrones.call.droneManager.runCommands(player, playerConnect, laser, "gui.REMOVE-COMMANDS", false);
                    playerConnect.stopDrone();
                    playerConnect.saveDrone(droneHolder);
                    playerConnect.save();
                } else if (e.getClick().equals(ClickType.MIDDLE)) {
                    new DroneMenu(BattleDrones.call.getPlayerMenu(player), flamethrowerDrone).open();
                }
            } else {
                BattleDrones.call.droneManager.runCommands(player, playerConnect, laser, "gui.PERMISSION", true);
            }
        } else if (file.getStringList(slot + ".OPTIONS").contains("BACK")) {
            new PlayerGUI(BattleDrones.call.getPlayerMenu(player)).open();
        }
    }

    @Override
    public void setItems() {
        BattleDrones.call.guiManager.setGUIItemStack(inventory, file, player);
        final String flamethrowerDrone = "flamethrower";
        final DroneHolder flamethrower = BattleDrones.call.getDroneHolder(uuid, flamethrowerDrone);
        final HashMap<String, Integer> drones = new HashMap<>();
        if (flamethrower.getUnlocked() == 1) {
            drones.put(flamethrowerDrone, flamethrower.getLevel());
        }
        BattleDrones.call.guiManager.setDrones(uuid, drones, inventory);
    }
}