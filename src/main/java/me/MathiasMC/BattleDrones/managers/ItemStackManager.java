package me.MathiasMC.BattleDrones.managers;

import me.MathiasMC.BattleDrones.BattleDrones;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerProfile;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ItemStackManager {

    private final BattleDrones plugin;

    public ItemStackManager(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    public void setupGUI(Inventory inventory, FileConfiguration file, Player player) {
        ConfigurationSection rootSection = file.getConfigurationSection("");
        if (rootSection == null) return;

        for (String key : rootSection.getKeys(false)) {
            if (key.equalsIgnoreCase("settings")) continue;

            ItemStack itemStack;

            if (file.contains(key + ".HEAD")) {
                String headKey = file.getString(key + ".HEAD");
                itemStack = plugin.drone_heads.get(headKey);
                if (itemStack == null) {
                    plugin.getTextUtils().gui(player, "head", headKey);
                    continue;
                }
            } else if (file.contains(key + ".MODEL-DATA")) {
                itemStack = new ItemStack(Material.STICK);
                ItemMeta itemMeta = itemStack.getItemMeta();
                if (itemMeta != null) {
                    itemMeta.setCustomModelData(file.getInt(key + ".MODEL-DATA"));
                    itemStack.setItemMeta(itemMeta);
                }
            } else {
                String material = file.getString(key + ".MATERIAL");
                itemStack = getItemStack(material, file.getInt(key + ".AMOUNT"));
                if (itemStack == null) {
                    plugin.getTextUtils().gui(player, "gui", material);
                    continue;
                }
            }

            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta == null) continue;

            String rawName = file.getString(key + ".NAME");
            if (rawName != null) {
                String displayName = ChatColor.translateAlternateColorCodes('&',
                        plugin.getPlaceholderManager().replacePlaceholders(player, rawName));
                itemMeta.setDisplayName(displayName);
            }

            List<String> loreList = new ArrayList<>();
            List<String> loreConfigList = file.getStringList(key + ".LORES");
            for (String lore : loreConfigList) {
                loreList.add(ChatColor.translateAlternateColorCodes('&',
                        plugin.getPlaceholderManager().replacePlaceholders(player, lore)));
            }
            itemMeta.setLore(loreList);

            String category = file.getString(key + ".CATEGORY");
            if (category != null) {
                NamespacedKey nameKey = new NamespacedKey(plugin, "drone");
                itemMeta.getPersistentDataContainer().set(nameKey,
                        PersistentDataType.STRING, category);
            }

            itemStack.setItemMeta(itemMeta);

            addGlow(itemStack, file, key + ".OPTIONS");

            try {
                inventory.setItem(Integer.parseInt(key), itemStack);
            } catch (NumberFormatException e) {
                plugin.getTextUtils().warning("Invalid inventory slot key: " + key);
            }
        }
    }

    public ItemStack getItemStack(String material, int amount) {
        Material returnMaterial = Material.matchMaterial(material);
        if (returnMaterial == null) {
            return null;
        }
        return new ItemStack(returnMaterial, amount);
    }

    public void dispatchCommand(FileConfiguration file, int slot, Player player) {
        String basePath = slot + ".COMMANDS";

        if (!file.contains(basePath)) return;

        String permission = file.getString(basePath + ".PERMISSION");
        if (permission == null || permission.trim().isEmpty()) {
            plugin.getTextUtils().warning("Missing permission node for slot " + slot);
            return;
        }

        permission = permission.trim();

        boolean hasPerm = player.hasPermission(permission);

        if (hasPerm) {

            List<String> consoleCommands = file.getStringList(basePath + ".CONSOLE");
            for (String command : consoleCommands) {
                plugin.getServer().dispatchCommand(plugin.consoleSender,
                        command.replace("{player}", player.getName()));
            }

            List<String> playerCommands = file.getStringList(basePath + ".PLAYER");
            for (String command : playerCommands) {
                player.performCommand(command.replace("{player}", player.getName()));
            }

        } else {
            List<String> noPermCommands = file.getStringList(basePath + ".NO-PERMISSION");
            for (String command : noPermCommands) {
                plugin.getServer().dispatchCommand(plugin.consoleSender,
                        command.replace("{player}", player.getName()));
            }
        }
    }

    public ItemStack getHeadTexture(String texture) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        if (meta == null) return head;

        PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());

        try {
            profile.getTextures().setSkin(URI.create(texture).toURL());
        } catch (MalformedURLException e) {
            return head;
        }

        meta.setOwnerProfile(profile);
        head.setItemMeta(meta);
        return head;
    }

    public boolean addGlow(ItemStack itemStack, FileConfiguration file, String path) {
        if (itemStack == null || file == null || path == null) {
            return false;
        }

        List<String> effects = file.getStringList(path);
        if (effects.stream().anyMatch(s -> s.equalsIgnoreCase("GLOW"))) {
            itemStack.addUnsafeEnchantment(Enchantment.INFINITY, 0);
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null) {
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                itemStack.setItemMeta(itemMeta);
            }
            return true;
        }

        return false;
    }

}