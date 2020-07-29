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

public class ProtectiveGUI extends GUI {

    private final FileConfiguration file = BattleDrones.call.guiFiles.get("player_protective");
    private final Player player = playerMenu.getPlayer();
    private final String uuid = playerMenu.getUuid();

    public ProtectiveGUI(Menu playerMenu) {
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
        final String shieldGeneratorDrone = "shield_generator";
        final String healingDrone = "healing";
        final DroneHolder shieldGeneratorHolder = BattleDrones.call.getDroneHolder(uuid, shieldGeneratorDrone);
        final FileConfiguration shield_generator = BattleDrones.call.droneFiles.get(shieldGeneratorDrone);
        final DroneHolder healingHolder = BattleDrones.call.getDroneHolder(uuid, healingDrone);
        final FileConfiguration healing = BattleDrones.call.droneFiles.get(healingDrone);
        if (shield_generator.getInt("gui.POSITION") == slot && shieldGeneratorHolder.getUnlocked() == 1) {
            if (player.hasPermission("battledrones.player.shield.generator")) {
                if (e.isLeftClick()) {
                    BattleDrones.call.droneManager.spawnDrone(player, shieldGeneratorDrone, false, false);
                } else if (e.isRightClick()) {
                    BattleDrones.call.droneManager.runCommands(player, playerConnect, shield_generator, "gui.REMOVE-COMMANDS", false);
                    playerConnect.stopDrone();
                    playerConnect.saveDrone(shieldGeneratorHolder);
                    playerConnect.save();
                } else if (e.getClick().equals(ClickType.MIDDLE)) {
                    new DroneMenu(BattleDrones.call.getPlayerMenu(player), shieldGeneratorDrone).open();
                }
            } else {
                BattleDrones.call.droneManager.runCommands(player, playerConnect, shield_generator, "gui.PERMISSION", true);
            }
        } else if (healing.getInt("gui.POSITION") == slot && healingHolder.getUnlocked() == 1) {
            if (player.hasPermission("battledrones.player.healing")) {
                if (e.isLeftClick()) {
                    BattleDrones.call.droneManager.spawnDrone(player, healingDrone, false, false);
                } else if (e.isRightClick()) {
                    BattleDrones.call.droneManager.runCommands(player, playerConnect, healing, "gui.REMOVE-COMMANDS", false);
                    playerConnect.stopDrone();
                    playerConnect.saveDrone(healingHolder);
                    playerConnect.save();
                } else if (e.getClick().equals(ClickType.MIDDLE)) {
                    new DroneMenu(BattleDrones.call.getPlayerMenu(player), healingDrone).open();
                }
            } else {
                BattleDrones.call.droneManager.runCommands(player, playerConnect, healing, "gui.PERMISSION", true);
            }
        } else if (file.getStringList(slot + ".OPTIONS").contains("BACK")) {
            new PlayerGUI(BattleDrones.call.getPlayerMenu(player)).open();
        }
        if (file.contains(String.valueOf(slot))) {
            BattleDrones.call.guiManager.dispatchCommand(file, slot, player);
        }
    }

    @Override
    public void setItems() {
        BattleDrones.call.guiManager.setGUIItemStack(inventory, file, player);
        final String shieldGeneratorDrone = "shield_generator";
        final String healingDrone = "healing";
        final DroneHolder shieldGenerator = BattleDrones.call.getDroneHolder(uuid, shieldGeneratorDrone);
        final DroneHolder healing = BattleDrones.call.getDroneHolder(uuid, healingDrone);
        HashMap<String, Integer> drones = new HashMap<>();
        if (shieldGenerator.getUnlocked() == 1) {
            drones.put(shieldGeneratorDrone, shieldGenerator.getLevel());
        }
        if (healing.getUnlocked() == 1) {
            drones.put(healingDrone, healing.getLevel());
        }
        BattleDrones.call.guiManager.setDrones(uuid, drones, inventory);
    }
}