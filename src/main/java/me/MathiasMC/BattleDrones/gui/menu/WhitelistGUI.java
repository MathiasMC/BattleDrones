package me.MathiasMC.BattleDrones.gui.menu;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.gui.GUI;
import me.MathiasMC.BattleDrones.gui.Menu;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WhitelistGUI extends GUI {

    private final BattleDrones plugin;
    private final FileConfiguration file;
    private final String drone;
    private final Player player = playerMenu.getPlayer();
    private final String uuid = playerMenu.getUuid();

    public WhitelistGUI(Menu playerMenu, String drone) {
        super(playerMenu);
        this.plugin = BattleDrones.getInstance();
        this.drone = drone;
        file = plugin.guiFiles.get(drone + "_whitelist");
    }

    @Override
    public boolean isRegistered() {
        return this.file != null;
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
        if (!file.contains(String.valueOf(slot)) && !e.isRightClick()) {
            return;
        }
        if (file.getStringList(slot + ".OPTIONS").contains("BACK")) {
            new DroneGUI(plugin.getPlayerMenu(player), drone).open();
        } else if (file.getStringList(slot + ".OPTIONS").contains("DRONE_WHITELIST_ADD")) {
            if (!plugin.drone_whitelist.containsKey(uuid)) {
                player.closeInventory();
                for (String command : plugin.getFileUtils().language.getStringList("whitelist.time")) {
                    plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", player.getName()));
                }
                plugin.drone_whitelist.put(uuid, drone);
                plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                                plugin.drone_whitelist.remove(uuid),
                        20L * plugin.getFileUtils().language.getLong("whitelist.seconds"));
            } else {
                for (String command : plugin.getFileUtils().language.getStringList("whitelist.active")) {
                    plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", player.getName()));
                }
            }
        }
        plugin.getItemStackManager().dispatchCommand(file, slot, player);
        final ItemMeta itemMeta = e.getCurrentItem().getItemMeta();
        if (itemMeta == null) {
            return;
        }
        if (e.isRightClick()) {
            final String name = itemMeta.getPersistentDataContainer().get(new NamespacedKey(plugin, "whitelist"), PersistentDataType.STRING);
            if (name == null) {
                return;
            }
            final DroneHolder droneHolder = plugin.getDroneHolder(uuid, drone);
            final List<String> players = droneHolder.getExclude();
            players.remove(name);
            droneHolder.save();
            super.open();
        }
    }

    @Override
    public void setItems() {
        plugin.getItemStackManager().setupGUI(inventory, file, player);
        final List<String> players = plugin.getDroneHolder(uuid, drone).getExclude();
        final FileConfiguration file = plugin.droneFiles.get(drone);
        ItemStack itemStack;
        if (file.contains("gui.WHITELIST.HEAD")) {
            itemStack = plugin.drone_heads.get(file.getString("gui.WHITELIST.HEAD"));
        } else {
            itemStack = plugin.getItemStackManager().getItemStack(file.getString("gui.WHITELIST.MATERIAL"), 1);
        }
        for (int index = 0; index < 54; index++) {
            if (players.size() > index) {
                final ItemStack itemStackS = itemStack.clone();
                final ItemMeta itemMeta = itemStackS.getItemMeta();
                if (itemMeta == null) {
                    return;
                }
                itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', file.getString("gui.WHITELIST.NAME").replace("{player}", players.get(index))));
                final ArrayList<String> lores = new ArrayList<>();
                for (String lore : file.getStringList("gui.WHITELIST.LORES")) {
                    lores.add(ChatColor.translateAlternateColorCodes('&', lore.replace("{player}", players.get(index))));
                }
                itemMeta.setLore(lores);
                final NamespacedKey nameKey = new NamespacedKey(plugin, "whitelist");
                itemMeta.getPersistentDataContainer().set(nameKey, PersistentDataType.STRING, players.get(index));
                itemStackS.setItemMeta(itemMeta);
                plugin.getItemStackManager().addGlow(itemStackS, file, "gui.WHITELIST.OPTIONS");
                inventory.setItem(index, itemStackS);
            }
        }
    }
}