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
}