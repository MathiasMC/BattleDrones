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

    private final FileConfiguration file;

    private final String drone;

    public AmmoGUI(Menu playerMenu, String drone) {
        super(playerMenu);
        this.drone = drone;
        file = BattleDrones.call.guiFiles.get(drone + "_ammo");
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
            final Player player = playerMenu.getPlayer();
            if (file.getStringList(slot + ".OPTIONS").contains("BACK")) {
                new DroneMenu(BattleDrones.call.getPlayerMenu(player), drone).open();
            }
        }
        if (e.getClickedInventory() == null) {
            return;
        }
        if (e.getClickedInventory().getType().equals(InventoryType.PLAYER)) {
            final ItemStack itemStack = e.getCurrentItem();
            if (itemStack == null) { return; }
            final ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta == null) { return; }
            final DroneHolder droneHolder = BattleDrones.call.getDroneHolder(playerMenu.getUuid(), drone);
            Player player = playerMenu.getPlayer();
            final ArrayList<String> lores = new ArrayList<>();
            FileConfiguration droneFile = BattleDrones.call.droneFiles.get(drone);
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
                final PlayerConnect playerConnect = BattleDrones.call.get(player.getUniqueId().toString());
                int ammo = droneHolder.getAmmo();
                if (ammo < (droneFile.getInt(playerConnect.getGroup() + "." + droneHolder.getLevel() + ".max-ammo-slots") * 64)) {
                    droneHolder.setAmmo(ammo + itemStack.getAmount());
                    player.getInventory().setItem(slot, new ItemStack(Material.AIR));
                    new AmmoGUI(BattleDrones.call.getPlayerMenu(player), drone).open();
                    droneHolder.save();
                } else {
                    for (String command : BattleDrones.call.language.get.getStringList("gui.ammo.full")) {
                        BattleDrones.call.getServer().dispatchCommand(BattleDrones.call.consoleSender, command.replace("{player}", player.getName()));
                    }
                }
            } else {
                for (String command : BattleDrones.call.language.get.getStringList("gui.ammo.not")) {
                    BattleDrones.call.getServer().dispatchCommand(BattleDrones.call.consoleSender, command.replace("{player}", player.getName()));
                }
            }
        }
    }

    @Override
    public void setItems() {
        BattleDrones.call.guiManager.setGUIItemStack(inventory, file, playerMenu.getPlayer());
        FileConfiguration droneFile = BattleDrones.call.droneFiles.get(drone);
        final ItemStack itemStack = BattleDrones.call.getItemStack(droneFile.getString("gui.AMMO.MATERIAL"), 64);
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
        int ammo = BattleDrones.call.getDroneHolder(playerMenu.getUuid(), drone).getAmmo();
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