package me.MathiasMC.BattleDrones.gui;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Objects;

public class AmmoGUI extends GUI {

    private final BattleDrones plugin = BattleDrones.call;
    private final FileConfiguration file;
    private final String drone;
    private final Player player = playerMenu.getPlayer();
    private final String uuid = playerMenu.getUuid();

    public AmmoGUI(Menu playerMenu, String drone) {
        super(playerMenu);
        this.drone = drone;
        file = plugin.guiFiles.get(drone + "_ammo");
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
        if (file.contains(String.valueOf(slot))) {
            if (file.getStringList(slot + ".OPTIONS").contains("BACK")) {
                new DroneMenu(plugin.getPlayerMenu(player), drone).open();
            }
            plugin.guiManager.dispatchCommand(file, slot, player);
        }
        if (e.getClickedInventory() == null) {
            return;
        }
        if (e.getClickedInventory().getType().equals(InventoryType.PLAYER)) {
            final ItemStack itemStack = e.getCurrentItem();
            if (itemStack == null) { return; }
            final ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta == null) { return; }
            final DroneHolder droneHolder = plugin.getDroneHolder(uuid, drone);
            final ArrayList<String> lores = new ArrayList<>();
            FileConfiguration droneFile = plugin.droneFiles.get(drone);
            final String ammoName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(droneFile.getString("gui.AMMO.NAME"))));
            for (String lore : droneFile.getStringList("gui.AMMO.LORES")) {
                lores.add(ChatColor.translateAlternateColorCodes('&', lore));
            }
            final ArrayList<String> getLores = new ArrayList<>();
            if (itemMeta.getLore() != null) {
                for (String lore : itemMeta.getLore()) {
                    getLores.add(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', lore)));
                }
            }
            if (ChatColor.stripColor(itemMeta.getDisplayName()).equals(ammoName) || Objects.equals(getLores, lores)) {
                final PlayerConnect playerConnect = plugin.get(uuid);
                final int ammo = droneHolder.getAmmo();
                if (ammo < (droneFile.getInt(playerConnect.getGroup() + "." + droneHolder.getLevel() + ".max-ammo-slots") * 64)) {
                    droneHolder.setAmmo(ammo + itemStack.getAmount());
                    player.getInventory().setItem(slot, new ItemStack(Material.AIR));
                    new AmmoGUI(plugin.getPlayerMenu(player), drone).open();
                    droneHolder.save();
                } else {
                    for (String command : plugin.language.get.getStringList("gui.ammo.full")) {
                        plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", player.getName()));
                    }
                }
            } else {
                for (String command : plugin.language.get.getStringList("gui.ammo.not")) {
                    plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", player.getName()));
                }
            }
        }
    }

    @Override
    public void setItems() {
        plugin.guiManager.setGUIItemStack(inventory, file, player);
        final FileConfiguration droneFile = plugin.droneFiles.get(drone);
        final String material = droneFile.getString("gui.AMMO.MATERIAL");
        ItemStack itemStack = plugin.getItemStack(material, 64);
        if (itemStack == null) {
            plugin.textUtils.gui(player, "ammo", material);
            return;
        }
        final ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return;
        }
        final ArrayList<String> lores = new ArrayList<>();
        for (String lore : droneFile.getStringList("gui.AMMO.LORES")) {
            lores.add(ChatColor.translateAlternateColorCodes('&', lore));
        }
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(droneFile.getString("gui.AMMO.NAME"))));
        itemMeta.setLore(lores);
        itemStack.setItemMeta(itemMeta);
        plugin.guiManager.glow(itemStack, droneFile, "gui.AMMO.OPTIONS");
        int ammo = plugin.getDroneHolder(uuid, drone).getAmmo();
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                if (ammo > 64) {
                    inventory.setItem(i, itemStack.clone());
                    ammo = ammo - 64;
                } else if (ammo > 0) {
                    ItemStack itemStack1 = itemStack.clone();
                    itemStack1.setAmount(ammo);
                    inventory.setItem(i, itemStack1);
                    ammo = 0;
                }
            }
        }
    }
}