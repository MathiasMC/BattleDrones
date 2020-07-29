package me.MathiasMC.BattleDrones.managers;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
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

    public void setGUIItemStack(Inventory inventory, FileConfiguration file, Player player) {
        PlayerConnect droneHolder = plugin.get(player.getUniqueId().toString());
        for (String key : file.getConfigurationSection("").getKeys(false)) {
            if (!key.equalsIgnoreCase("settings")) {
                ItemStack itemStack;
                if (!file.contains(key + ".HEAD")) {
                    itemStack = plugin.getItemStack(file.getString(key + ".MATERIAL"), file.getInt(key + ".AMOUNT"));
                } else {
                    itemStack = plugin.drone_heads.get(file.getString(key + ".HEAD"));
                }
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(plugin.replacePlaceholders(player, ChatColor.translateAlternateColorCodes('&', file.getString(key + ".NAME")
                        .replace("{coins}", String.valueOf(droneHolder.getCoins())))));
                ArrayList<String> list = new ArrayList<>();
                for (String lores : file.getStringList(key + ".LORES")) {
                    list.add(plugin.replacePlaceholders(player, ChatColor.translateAlternateColorCodes('&', lores
                            .replace("{coins}", String.valueOf(droneHolder.getCoins())))));
                }
                itemMeta.setLore(list);
                itemStack.setItemMeta(itemMeta);
                inventory.setItem(Integer.parseInt(key), itemStack);
            }
        }
    }

    public void setDrones(String uuid, HashMap<String, Integer> drones, Inventory inventory) {
        final PlayerConnect playerConnect = BattleDrones.call.get(uuid);
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

    public void dispatchCommand(FileConfiguration file, int slot, Player player) {
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
}