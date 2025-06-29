package me.MathiasMC.BattleDrones.gui.menu;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.api.events.DroneBuyEvent;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import me.MathiasMC.BattleDrones.gui.GUI;
import me.MathiasMC.BattleDrones.gui.Menu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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

public class ShopGUI extends GUI {

    private final BattleDrones plugin;

    private final FileConfiguration file;
    private final Player player = playerMenu.getPlayer();
    private final String uuid = playerMenu.getUuid();
    private final PlayerConnect playerConnect = playerMenu.getPlayerConnect();

    private final List<String> drones;

    public ShopGUI(Menu playerMenu, String category) {
        super(playerMenu);
        this.plugin = BattleDrones.getInstance();
        this.file = plugin.guiFiles.get("shop_" + category);
        this.drones = plugin.category.get(category);
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
        if (!file.contains(String.valueOf(slot))) {
            return;
        }

        plugin.getItemStackManager().dispatchCommand(file, slot, player);

        if (file.getStringList(slot + ".OPTIONS").contains("BACK")) {
            new SelectGUI(plugin.getPlayerMenu(player), "shop").open();
        }

        if (e.getCurrentItem().getItemMeta() == null) {
            return;
        }

        final String drone = e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "drone"), PersistentDataType.STRING);
        if (drone == null) {
            return;
        }
        final FileConfiguration file = plugin.droneFiles.get(drone);
        if (!player.hasPermission("battledrones.gui.shop." + drone)) {
            plugin.getDroneManager().runCommands(player, file.getStringList("gui.SHOP.PERMISSION"));
            return;
        }

        final DroneHolder droneHolder = plugin.getDroneHolder(uuid, drone);

        if (droneHolder.getUnlocked() == 1) {
            plugin.getDroneManager().runCommands(player, file.getStringList("gui.SHOP.HAVE"));
            return;
        }

        if (!plugin.getSupport().withdraw(player, file.getLong("gui.SHOP.COST"))) {
            plugin.getDroneManager().runCommands(player, file.getStringList("gui.SHOP.COINS"));
            return;
        }

        final DroneBuyEvent droneBuyEvent = new DroneBuyEvent(player, playerConnect, droneHolder);
        plugin.getServer().getPluginManager().callEvent(droneBuyEvent);
        if (droneBuyEvent.isCancelled()) {
            return;
        }

        droneHolder.setUnlocked(1);
        droneHolder.setHealth(plugin.droneFiles.get(drone).getInt(playerConnect.getGroup() + "." + droneHolder.getLevel() + ".health"));
        droneHolder.save();
        plugin.getDroneManager().runCommands(player, file.getStringList("gui.SHOP.BOUGHT"));
    }

    @Override
    public void setItems() {
        plugin.getItemStackManager().setupGUI(inventory, file, player);
        final NamespacedKey key = new NamespacedKey(plugin, "drone");
        for (String drone : drones) {
            final FileConfiguration file = plugin.droneFiles.get(drone);
            ItemStack itemStack;
            if (file.contains("gui.SHOP.MODEL-DATA")) {
                itemStack = new ItemStack(Material.STICK);
                final ItemMeta itemMeta = itemStack.getItemMeta();
                if (itemMeta != null) {
                    itemMeta.setCustomModelData(file.getInt("gui.SHOP.MODEL-DATA"));
                    itemStack.setItemMeta(itemMeta);
                }
            } else {
                itemStack = plugin.drone_heads.get(file.getString("gui.SHOP.HEAD"));
            }
            final ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta == null) {
                return;
            }
            itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(file.getString("gui.SHOP.NAME"))));
            final ArrayList<String> lores = new ArrayList<>();
            for (String lore : file.getStringList("gui.SHOP.LORES")) {
                lores.add(ChatColor.translateAlternateColorCodes('&', lore));
            }
            itemMeta.setLore(lores);
            itemMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, drone);
            itemStack.setItemMeta(itemMeta);
            inventory.setItem(file.getInt("gui.SHOP.POSITION"), itemStack);
        }
    }
}