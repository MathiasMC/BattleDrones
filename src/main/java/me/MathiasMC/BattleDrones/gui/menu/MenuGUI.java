package me.MathiasMC.BattleDrones.gui.menu;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.api.Type;
import me.MathiasMC.BattleDrones.api.events.DroneRemoveEvent;
import me.MathiasMC.BattleDrones.api.events.DroneSpawnEvent;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import me.MathiasMC.BattleDrones.gui.GUI;
import me.MathiasMC.BattleDrones.gui.Menu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MenuGUI extends GUI {

    private final BattleDrones plugin;
    private final FileConfiguration file;
    private final Player player = playerMenu.getPlayer();
    private final String uuid = playerMenu.getUuid();
    private final PlayerConnect playerConnect = playerMenu.getPlayerConnect();

    private final List<String> drones;

    public MenuGUI(Menu playerMenu, String category) {
        super(playerMenu);
        this.plugin = BattleDrones.getInstance();
        this.file = plugin.guiFiles.get("player_" + category);
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
        if (e.getCurrentItem().getItemMeta() == null) {
            return;
        }
        final ItemMeta itemMeta = e.getCurrentItem().getItemMeta();
        final String drone = itemMeta.getPersistentDataContainer().get(new NamespacedKey(plugin, "drone"), PersistentDataType.STRING);
        if (drone != null) {
            final DroneHolder droneHolder = plugin.getDroneHolder(uuid, drone);
            final FileConfiguration file = plugin.droneFiles.get(drone);
            if (droneHolder.getUnlocked() == 1) {
                if (player.hasPermission("battledrones.gui.menu." + drone)) {
                    if (e.isLeftClick()) {
                        final DroneSpawnEvent droneSpawnEvent = new DroneSpawnEvent(player, playerConnect, droneHolder);
                        droneSpawnEvent.setBypassWait(false);
                        droneSpawnEvent.setBypassDroneAmount(false);
                        droneSpawnEvent.setBypassLocation(false);
                        droneSpawnEvent.setType(Type.GUI);
                        droneSpawnEvent.spawn();
                    } else if (e.isRightClick()) {
                        if (playerConnect.isActive()) {
                            final DroneRemoveEvent droneRemoveEvent = new DroneRemoveEvent(player, playerConnect, plugin.getDroneHolder(uuid, playerConnect.getActive()));
                            droneRemoveEvent.setType(Type.GUI);
                            droneRemoveEvent.remove();
                        }
                    } else if (e.getClick().equals(ClickType.MIDDLE)) {
                        new DroneGUI(plugin.getPlayerMenu(player), drone).open();
                    }
                } else {
                    plugin.getDroneManager().runCommands(player, file.getStringList("gui.PERMISSION"));
                }
            }
        }
        if (file.getStringList(slot + ".OPTIONS").contains("BACK")) {
            new SelectGUI(plugin.getPlayerMenu(player), "player").open();
        }
        if (file.contains(String.valueOf(slot))) {
            plugin.getItemStackManager().dispatchCommand(file, slot, player);
        }
    }

    @Override
    public void setItems() {
        plugin.getItemStackManager().setupGUI(inventory, file, player);
        final HashMap<String, Integer> drones = new HashMap<>();
        for (String drone : this.drones) {
            final DroneHolder droneHolder = plugin.getDroneHolder(uuid, drone);
            if (droneHolder.getUnlocked() == 1) {
                drones.put(drone, droneHolder.getLevel());
            }
        }
        final NamespacedKey key = new NamespacedKey(plugin, "drone");
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
            itemMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, drone);
            itemStack.setItemMeta(itemMeta);
            inventory.setItem(file.getInt("gui.POSITION"), itemStack);
        }
    }
}