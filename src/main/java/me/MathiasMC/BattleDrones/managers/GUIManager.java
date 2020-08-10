package me.MathiasMC.BattleDrones.managers;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import me.MathiasMC.BattleDrones.gui.DroneMenu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class GUIManager {

    private final BattleDrones plugin;

    public GUIManager(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    public void setGUIItemStack(final Inventory inventory, final FileConfiguration file, final Player player) {
        final PlayerConnect droneHolder = plugin.get(player.getUniqueId().toString());
        for (String key : Objects.requireNonNull(file.getConfigurationSection("")).getKeys(false)) {
            if (!key.equalsIgnoreCase("settings")) {
                ItemStack itemStack;
                if (!file.contains(key + ".HEAD")) {
                    if (!file.contains(key + ".MODEL-DATA")) {
                        final String material = file.getString(key + ".MATERIAL");
                        itemStack = plugin.getItemStack(material, file.getInt(key + ".AMOUNT"));
                        if (itemStack == null) {
                            plugin.textUtils.gui(player, "gui", material);
                            return;
                        }
                    } else {
                        itemStack = new ItemStack(Material.STICK);
                        final ItemMeta itemMeta = itemStack.getItemMeta();
                        if (itemMeta != null) {
                            itemMeta.setCustomModelData(file.getInt(key + ".MODEL-DATA"));
                            itemStack.setItemMeta(itemMeta);
                        }
                    }
                } else {
                    final String material = file.getString(key + ".HEAD");
                    itemStack = plugin.drone_heads.get(material);
                    if (itemStack == null) {
                        plugin.textUtils.gui(player, "head", material);
                        return;
                    }
                }
                final ItemMeta itemMeta = itemStack.getItemMeta();
                if (itemMeta == null) {
                    return;
                }
                itemMeta.setDisplayName(plugin.replacePlaceholders(player, ChatColor.translateAlternateColorCodes('&',
                        Objects.requireNonNull(file.getString(key + ".NAME"))
                        .replace("{coins}", String.valueOf(droneHolder.getCoins())))));
                final ArrayList<String> list = new ArrayList<>();
                for (String lores : file.getStringList(key + ".LORES")) {
                    list.add(plugin.replacePlaceholders(player, ChatColor.translateAlternateColorCodes('&', lores
                            .replace("{coins}", String.valueOf(droneHolder.getCoins())))));
                }
                itemMeta.setLore(list);
                itemStack.setItemMeta(itemMeta);
                plugin.guiManager.glow(itemStack, file, key + ".OPTIONS");
                inventory.setItem(Integer.parseInt(key), itemStack);
            }
        }
    }

    public void setDrones(final String uuid, final HashMap<String, Integer> drones, final Inventory inventory) {
        final PlayerConnect playerConnect = plugin.get(uuid);
        for (String drone : drones.keySet()) {
            final FileConfiguration file = plugin.droneFiles.get(drone);
            final String path = playerConnect.getGroup() + "." + drones.get(drone) + ".";
            ItemStack itemStack;
            if (file.contains(path + "model-data-gui")) {
                itemStack = new ItemStack(Material.STICK);
                final ItemMeta itemMeta = itemStack.getItemMeta();
                if (itemMeta != null) {
                    itemMeta.setCustomModelData(file.getInt(path + "model-data-gui"));
                    itemStack.setItemMeta(itemMeta);
                }
            } else {
                itemStack = plugin.drone_heads.get(file.getString(path + "head"));
            }
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

    public void dispatchCommand(final FileConfiguration file, final int slot, final Player player) {
        if (file.contains(slot + ".COMMANDS")) {
            if (player.hasPermission(Objects.requireNonNull(file.getString(slot + ".COMMANDS.PERMISSION")))) {
                if (file.contains(slot + ".COMMANDS.CONSOLE")) {
                    for (String command : file.getStringList(slot + ".COMMANDS.CONSOLE")) {
                        plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", player.getName()));
                    }
                }
                if (file.contains(slot + ".COMMANDS.PLAYER")) {
                    for (String command : file.getStringList(slot + ".COMMANDS.PLAYER")) {
                        player.performCommand(command.replace("{player}", player.getName()));
                    }
                }
            } else {
                if (file.contains(slot + ".COMMANDS.NO-PERMISSION")) {
                    for (String command : file.getStringList(slot + ".COMMANDS.NO-PERMISSION")) {
                        plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", player.getName()));
                    }
                }
            }
        }
    }

    public void glow(final ItemStack itemStack, final FileConfiguration file, final String path) {
        if (file.contains(path) && file.getStringList(path).contains("GLOW")) {
            itemStack.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 0);
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta == null) {
                return;
            }
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            itemStack.setItemMeta(itemMeta);
        }
    }

    public void playerGUI(final InventoryClickEvent e, final Player player, final PlayerConnect playerConnect, final DroneHolder droneHolder, final String drone, final FileConfiguration file) {
        if (player.hasPermission("battledrones.gui.menu." + drone)) {
            if (e.isLeftClick()) {
                plugin.droneManager.spawnDrone(player, drone, false, false);
            } else if (e.isRightClick()) {
                plugin.droneManager.runCommands(player, playerConnect, file, "gui.REMOVE-COMMANDS", false);
                playerConnect.stopDrone();
                playerConnect.saveDrone(droneHolder);
                playerConnect.save();
            } else if (e.getClick().equals(ClickType.MIDDLE)) {
                new DroneMenu(plugin.getPlayerMenu(player), drone).open();
            }
        } else {
            plugin.droneManager.runCommands(player, playerConnect, file, "gui.PERMISSION", true);
        }
    }

    public void shopGUI(final int slot, final Player player, final String uuid, final PlayerConnect playerConnect, final FileConfiguration file, final String drone, final String permission) {
        if (player.hasPermission("battledrones.gui.shop." + permission)) {
            final DroneHolder droneHolder = plugin.getDroneHolder(uuid, drone);
            if (droneHolder.getUnlocked() != 1) {
                final long coins = playerConnect.getCoins();
                final long cost = file.getLong(slot + ".COST");
                if (!plugin.config.get.getBoolean("vault") && coins >= cost ||
                        plugin.config.get.getBoolean("vault") &&
                                plugin.getEconomy() != null &&
                                plugin.getEconomy().withdrawPlayer(player, cost).transactionSuccess()) {
                    if (!plugin.config.get.getBoolean("vault")) {
                        playerConnect.setCoins(coins - cost);
                    }
                    droneHolder.setUnlocked(1);
                    droneHolder.setHealth(plugin.droneFiles.get(drone).getInt(playerConnect.getGroup() + "." + droneHolder.getLevel() + ".health"));
                    droneHolder.save();
                    for (String command : file.getStringList(slot + ".SHOP-COMMANDS.BOUGHT")) {
                        plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", player.getName()));
                    }
                } else {
                    for (String command : file.getStringList(slot + ".SHOP-COMMANDS.COINS")) {
                        plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", player.getName()));
                    }
                }
            } else {
                for (String command : file.getStringList(slot + ".SHOP-COMMANDS.HAVE")) {
                    plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", player.getName()));
                }
            }
        } else {
            for (String command : file.getStringList(slot + ".SHOP-COMMANDS.PERMISSION")) {
                plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", player.getName()));
            }
        }
    }
}