package me.MathiasMC.BattleDrones.gui.menu;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import me.MathiasMC.BattleDrones.api.events.DroneAmmoEvent;
import me.MathiasMC.BattleDrones.gui.GUI;
import me.MathiasMC.BattleDrones.gui.Menu;
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

    private final BattleDrones plugin;
    private final FileConfiguration file;
    private final String drone;
    private final Player player = playerMenu.getPlayer();
    private final String uuid = playerMenu.getUuid();

    public AmmoGUI(Menu playerMenu, String drone) {
        super(playerMenu);
        this.plugin = BattleDrones.getInstance();
        this.drone = drone;
        file = plugin.guiFiles.get(drone + "_ammo");
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
        if (file.contains(String.valueOf(slot))) {
            if (file.getStringList(slot + ".OPTIONS").contains("BACK")) {
                new DroneGUI(plugin.getPlayerMenu(player), drone).open();
            }
            plugin.getItemStackManager().dispatchCommand(file, slot, player);
        }
        if (e.getClickedInventory() == null) {
            return;
        }
        if (e.getClickedInventory().getType().equals(InventoryType.PLAYER)) {
            final ItemStack itemStack = e.getCurrentItem();
            if (itemStack == null) {
                return;
            }
            final ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta == null) {
                return;
            }
            final DroneHolder droneHolder = plugin.getDroneHolder(uuid, drone);
            final ArrayList<String> lores = new ArrayList<>();
            final FileConfiguration droneFile = plugin.droneFiles.get(drone);
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
                final PlayerConnect playerConnect = plugin.getPlayerConnect(uuid);
                final int ammo = droneHolder.getAmmo();
                final DroneAmmoEvent droneAmmoEvent = new DroneAmmoEvent(player, playerConnect, droneHolder, itemStack.getAmount());
                plugin.getServer().getPluginManager().callEvent(droneAmmoEvent);
                if (droneAmmoEvent.isCancelled()) {
                    return;
                }
                if (ammo < (droneFile.getInt(playerConnect.getGroup() + "." + droneHolder.getLevel() + ".max-ammo-slots") * 64)) {
                    droneHolder.setAmmo(ammo + itemStack.getAmount());
                    player.getInventory().setItem(slot, new ItemStack(Material.AIR));
                    new AmmoGUI(plugin.getPlayerMenu(player), drone).open();
                    droneHolder.save();
                    return;
                }
                plugin.getDroneManager().runCommands(player, droneFile, "gui.AMMO.FULL");
                return;
            }
            plugin.getDroneManager().runCommands(player, droneFile, "gui.AMMO.NOT");
        }
    }

    @Override
    public void setItems() {
        plugin.getItemStackManager().setupGUI(inventory, file, player);
        final FileConfiguration droneFile = plugin.droneFiles.get(drone);
        final String material = droneFile.getString("gui.AMMO.MATERIAL");
        final ItemStack itemStack = plugin.getItemStackManager().getItemStack(material, 64);
        if (itemStack == null) {
            plugin.getTextUtils().gui(player, "ammo", material);
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
        plugin.getItemStackManager().addGlow(itemStack, droneFile, "gui.AMMO.OPTIONS");
        int ammo = plugin.getDroneHolder(uuid, drone).getAmmo();
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                if (ammo > 64) {
                    inventory.setItem(i, itemStack.clone());
                    ammo = ammo - 64;
                } else if (ammo > 0) {
                    final ItemStack itemStack1 = itemStack.clone();
                    itemStack1.setAmount(ammo);
                    inventory.setItem(i, itemStack1);
                    ammo = 0;
                }
            }
        }
    }
}