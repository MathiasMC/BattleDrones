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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Objects;

public class EnergyGUI extends GUI {

    private final FileConfiguration file = BattleDrones.call.guiFiles.get("player_energy");

    public EnergyGUI(Menu playerMenu) {
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
        final Player player = playerMenu.getPlayer();
        final String uuid = playerMenu.getUuid();
        final PlayerConnect playerConnect = BattleDrones.call.get(playerMenu.getUuid());
        final DroneHolder droneHolder = BattleDrones.call.getDroneHolder(playerMenu.getUuid(), "laser");
        final FileConfiguration laser = BattleDrones.call.droneFiles.get("laser");
        if (laser.getInt("gui.POSITION") == slot && droneHolder.getUnlocked() == 1) {
            if (e.isLeftClick()) {
                if (!BattleDrones.call.drone_players.contains(uuid)) {
                    if (BattleDrones.call.drone_amount.size() < BattleDrones.call.config.get.getInt("drone-amount") || player.hasPermission("battledrones.bypass.drone-amount")) {
                        BattleDrones.call.droneManager.runCommands(player, playerConnect, laser, "gui.SPAWN-COMMANDS", false);
                        playerConnect.stopDrone();
                        spawnLaser(player, droneHolder, laser);
                        BattleDrones.call.droneManager.waitSchedule(uuid, laser);
                    } else {
                        BattleDrones.call.droneManager.runCommands(player, playerConnect, BattleDrones.call.language.get, "gui.drone.amount-reached", true);
                    }
                } else {
                    BattleDrones.call.droneManager.wait(player, laser);
                }
            } else if (e.isRightClick()) {
                BattleDrones.call.droneManager.runCommands(player, playerConnect, laser, "gui.REMOVE-COMMANDS", false);
                playerConnect.stopDrone();
                playerConnect.saveDrone(droneHolder);
                playerConnect.save();
            } else if (e.getClick().equals(ClickType.MIDDLE)) {
                new DroneMenu(BattleDrones.call.getPlayerMenu(player), "laser").open();
            }
        } else if (file.getStringList(slot + ".OPTIONS").contains("BACK")) {
            new PlayerGUI(BattleDrones.call.getPlayerMenu(player)).open();
        }
    }

    @Override
    public void setItems() {
        BattleDrones.call.guiManager.setGUIItemStack(inventory, file, playerMenu.getPlayer());
        final PlayerConnect playerConnect = BattleDrones.call.get(playerMenu.getUuid());
        final DroneHolder droneHolder = BattleDrones.call.getDroneHolder(playerMenu.getUuid(), "laser");
        if (droneHolder.getUnlocked() == 1) {
            final FileConfiguration laser = BattleDrones.call.droneFiles.get("laser");
            final ItemStack itemStack = BattleDrones.call.drone_heads.get(laser.getString(playerConnect.getGroup() + "." + droneHolder.getLevel() + ".head"));
            final ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta == null) {
                return;
            }
            itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(laser.getString("gui.NAME"))));
            final ArrayList<String> lores = new ArrayList<>();
            for (String lore : laser.getStringList("gui.LORES")) {
                lores.add(ChatColor.translateAlternateColorCodes('&', lore));
            }
            itemMeta.setLore(lores);
            itemStack.setItemMeta(itemMeta);
            inventory.setItem(laser.getInt("gui.POSITION"), itemStack);
        }
    }

    private void spawnLaser(Player player, DroneHolder droneHolder, FileConfiguration file) {
        PlayerConnect playerConnect = BattleDrones.call.get(playerMenu.getUuid());
        playerConnect.spawn(player, file.getString(playerConnect.getGroup() + "." + droneHolder.getLevel() + ".head"));
        BattleDrones.call.aiManager.defaultAI(player,
                playerConnect,
                file,
                droneHolder.getLevel(),
                droneHolder.getMonsters(),
                droneHolder.getAnimals(),
                droneHolder.getPlayers(),
                droneHolder.getExclude(),
                false, false, true);
        BattleDrones.call.laser.shot(player);
        playerConnect.setActive("laser");
        BattleDrones.call.droneManager.regen(playerConnect, droneHolder, file, droneHolder.getLevel());
    }
}