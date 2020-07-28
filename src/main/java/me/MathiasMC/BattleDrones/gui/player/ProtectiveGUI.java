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
import java.util.HashMap;
import java.util.Objects;

public class ProtectiveGUI extends GUI {

    private final FileConfiguration file = BattleDrones.call.guiFiles.get("player_protective");

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
        final Player player = playerMenu.getPlayer();
        final String uuid = playerMenu.getUuid();
        final PlayerConnect playerConnect = BattleDrones.call.get(playerMenu.getUuid());
        final DroneHolder droneHolder = BattleDrones.call.getDroneHolder(playerMenu.getUuid(), "shield_generator");
        final FileConfiguration shield_generator = BattleDrones.call.droneFiles.get("shield_generator");
        final DroneHolder healingHolder = BattleDrones.call.getDroneHolder(playerMenu.getUuid(), "healing");
        final FileConfiguration healing = BattleDrones.call.droneFiles.get("healing");
        if (shield_generator.getInt("gui.POSITION") == slot && droneHolder.getUnlocked() == 1) {
            if (player.hasPermission("battledrones.player.shield.generator")) {
                if (e.isLeftClick()) {
                    if (!BattleDrones.call.drone_players.contains(uuid)) {
                        if (BattleDrones.call.drone_amount.size() < BattleDrones.call.config.get.getInt("drone-amount") || player.hasPermission("battledrones.bypass.drone-amount")) {
                            BattleDrones.call.droneManager.runCommands(player, playerConnect, shield_generator, "gui.SPAWN-COMMANDS", false);
                            playerConnect.stopDrone();
                            spawnShieldGenerator(player, playerConnect, shield_generator);
                            BattleDrones.call.droneManager.waitSchedule(uuid, shield_generator);
                        } else {
                            BattleDrones.call.droneManager.runCommands(player, playerConnect, BattleDrones.call.language.get, "gui.drone.amount-reached", true);
                        }
                    } else {
                        BattleDrones.call.droneManager.wait(player, shield_generator);
                    }
                } else if (e.isRightClick()) {
                    BattleDrones.call.droneManager.runCommands(player, playerConnect, shield_generator, "gui.REMOVE-COMMANDS", false);
                    playerConnect.stopDrone();
                    playerConnect.saveDrone(droneHolder);
                    playerConnect.save();
                } else if (e.getClick().equals(ClickType.MIDDLE)) {
                    new DroneMenu(BattleDrones.call.getPlayerMenu(player), "shield_generator").open();
                }
            } else {
                BattleDrones.call.droneManager.runCommands(player, playerConnect, shield_generator, "gui.PERMISSION", true);
            }
        } else if (healing.getInt("gui.POSITION") == slot && healingHolder.getUnlocked() == 1) {
            if (player.hasPermission("battledrones.player.healing")) {
                if (e.isLeftClick()) {
                    if (!BattleDrones.call.drone_players.contains(uuid)) {
                        if (BattleDrones.call.drone_amount.size() < BattleDrones.call.config.get.getInt("drone-amount") || player.hasPermission("battledrones.bypass.drone-amount")) {
                            BattleDrones.call.droneManager.runCommands(player, playerConnect, healing, "gui.SPAWN-COMMANDS", false);
                            playerConnect.stopDrone();
                            spawnHealing(player, playerConnect, healing);
                            BattleDrones.call.droneManager.waitSchedule(uuid, healing);
                        } else {
                            BattleDrones.call.droneManager.runCommands(player, playerConnect, BattleDrones.call.language.get, "gui.drone.amount-reached", true);
                        }
                    } else {
                        BattleDrones.call.droneManager.wait(player, healing);
                    }
                } else if (e.isRightClick()) {
                    BattleDrones.call.droneManager.runCommands(player, playerConnect, healing, "gui.REMOVE-COMMANDS", false);
                    playerConnect.stopDrone();
                    playerConnect.saveDrone(healingHolder);
                    playerConnect.save();
                } else if (e.getClick().equals(ClickType.MIDDLE)) {
                    new DroneMenu(BattleDrones.call.getPlayerMenu(player), "healing").open();
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
        BattleDrones.call.guiManager.setGUIItemStack(inventory, file, playerMenu.getPlayer());
        final PlayerConnect playerConnect = BattleDrones.call.get(playerMenu.getUuid());
        final DroneHolder droneHolder = BattleDrones.call.getDroneHolder(playerMenu.getUuid(), "shield_generator");
        final DroneHolder healing = BattleDrones.call.getDroneHolder(playerMenu.getUuid(), "healing");
        HashMap<String, Integer> drones = new HashMap<>();
        if (droneHolder.getUnlocked() == 1) {
            drones.put("shield_generator", droneHolder.getLevel());
        }
        if (healing.getUnlocked() == 1) {
            drones.put("healing", healing.getLevel());
        }
        for (String drone : drones.keySet()) {
            final FileConfiguration file = BattleDrones.call.droneFiles.get(drone);
            final ItemStack itemStack = BattleDrones.call.drone_heads.get(file.getString(playerConnect.getGroup() + "." + drones.get(drone) + ".head"));
            final ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta == null) {
                return;
            }
            itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(file.getString("gui.NAME"))));
            final ArrayList<String> lores = new ArrayList<>();
            for (String lore : file.getStringList("gui.LORES")) {
                lores.add(ChatColor.translateAlternateColorCodes('&', lore));
            }
            itemMeta.setLore(lores);
            itemStack.setItemMeta(itemMeta);
            inventory.setItem(file.getInt("gui.POSITION"), itemStack);
        }
    }

    private void spawnShieldGenerator(Player player, PlayerConnect playerConnect, FileConfiguration file) {
        DroneHolder droneHolder = BattleDrones.call.getDroneHolder(playerMenu.getUuid(), "shield_generator");
        playerConnect.spawn(player, file.getString(playerConnect.getGroup() + "." + droneHolder.getLevel() + ".head"));
        BattleDrones.call.aiManager.defaultAI(player,
                playerConnect,
                file,
                droneHolder.getLevel(),
                droneHolder.getMonsters(),
                0,
                droneHolder.getPlayers(),
                droneHolder.getExclude(),
                false, false, true);
        BattleDrones.call.shieldGenerator.shot(player);
        playerConnect.setActive("shield_generator");
        playerConnect.setRegen(true);
        BattleDrones.call.droneManager.regen(playerConnect, droneHolder, file, droneHolder.getLevel());
    }

    private void spawnHealing(Player player, PlayerConnect playerConnect, FileConfiguration file) {
        DroneHolder droneHolder = BattleDrones.call.getDroneHolder(playerMenu.getUuid(), "healing");
        playerConnect.spawn(player, file.getString(playerConnect.getGroup() + "." + droneHolder.getLevel() + ".head"));
        BattleDrones.call.aiManager.defaultAI(player,
                playerConnect,
                file,
                droneHolder.getLevel(),
                droneHolder.getMonsters(),
                droneHolder.getAnimals(),
                1,
                droneHolder.getExclude(),
                true, true, true);
        BattleDrones.call.healing.shot(player);
        playerConnect.setActive("healing");
        playerConnect.setRegen(true);
        BattleDrones.call.droneManager.regen(playerConnect, droneHolder, file, droneHolder.getLevel());
    }
}