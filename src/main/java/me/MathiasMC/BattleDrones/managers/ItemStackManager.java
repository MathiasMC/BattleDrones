package me.MathiasMC.BattleDrones.managers;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.MathiasMC.BattleDrones.BattleDrones;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class ItemStackManager {

    private final BattleDrones plugin;

    public ItemStackManager(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    public void setupGUI(final Inventory inventory, final FileConfiguration file, final Player player) {
        for (String key : Objects.requireNonNull(file.getConfigurationSection("")).getKeys(false)) {
            if (!key.equalsIgnoreCase("settings")) {
                ItemStack itemStack;
                if (!file.contains(key + ".HEAD")) {
                    if (!file.contains(key + ".MODEL-DATA")) {
                        final String material = file.getString(key + ".MATERIAL");
                        itemStack = getItemStack(material, file.getInt(key + ".AMOUNT"));
                        if (itemStack == null) {
                            plugin.getTextUtils().gui(player, "gui", material);
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
                        plugin.getTextUtils().gui(player, "head", material);
                        return;
                    }
                }
                final ItemMeta itemMeta = itemStack.getItemMeta();
                if (itemMeta == null) {
                    return;
                }
                itemMeta.setDisplayName(plugin.getPlaceholderManager().replacePlaceholders(player, ChatColor.translateAlternateColorCodes('&',
                        Objects.requireNonNull(file.getString(key + ".NAME")))));
                final ArrayList<String> list = new ArrayList<>();
                for (String lores : file.getStringList(key + ".LORES")) {
                    list.add(plugin.getPlaceholderManager().replacePlaceholders(player, ChatColor.translateAlternateColorCodes('&', lores)));
                }
                itemMeta.setLore(list);
                if (file.contains(key + ".CATEGORY")) {
                    final NamespacedKey nameKey = new NamespacedKey(plugin, "drone");
                    itemMeta.getPersistentDataContainer().set(nameKey, PersistentDataType.STRING, file.getString(key + ".CATEGORY"));
                }
                itemStack.setItemMeta(itemMeta);
                glow(itemStack, file, key + ".OPTIONS");
                inventory.setItem(Integer.parseInt(key), itemStack);
            }
        }
    }

    public ItemStack getItemStack(final String bb, final int amount) {
        try {
            return new ItemStack(Material.getMaterial(bb), amount);
        } catch (Exception e) {
            return null;
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

    public ItemStack getHeadTexture(final String texture) {
        final ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD, 1);
        final SkullMeta itemMeta = (SkullMeta) itemStack.getItemMeta();
        final GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "");
        gameProfile.getProperties().put("textures", new Property("textures", texture));
        final Field profileField;
        try {
            profileField = itemMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(itemMeta, gameProfile);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            plugin.getTextUtils().exception(e.getStackTrace(), e.getMessage());
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
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
}