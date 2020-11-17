package me.MathiasMC.BattleDrones.gui.menu;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.api.DroneRegistry;
import me.MathiasMC.BattleDrones.api.Type;
import me.MathiasMC.BattleDrones.api.events.DroneUpgradeEvent;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import me.MathiasMC.BattleDrones.api.events.DroneSpawnEvent;
import me.MathiasMC.BattleDrones.gui.GUI;
import me.MathiasMC.BattleDrones.gui.Menu;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DroneGUI extends GUI {

    private final BattleDrones plugin;
    private final FileConfiguration file;
    private final String drone;
    private final Player player = playerMenu.getPlayer();
    private final String uuid = playerMenu.getUuid();

    public DroneGUI(Menu playerMenu, String drone) {
        super(playerMenu);
        this.plugin = BattleDrones.getInstance();
        this.drone = drone;
        file = plugin.guiFiles.get(drone);
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
            final PlayerConnect playerConnect = plugin.getPlayerConnect(uuid);
            final DroneHolder droneHolder = plugin.getDroneHolder(uuid, drone);
            boolean updateAI = false;
            if (file.getStringList(slot + ".OPTIONS").contains("BACK")) {
                for (String category : plugin.category.keySet()) {
                    if (plugin.category.get(category).contains(drone)) {
                        new MenuGUI(plugin.getPlayerMenu(player), category).open();
                        break;
                    }
                }
            } else if (file.getStringList(slot + ".OPTIONS").contains("DRONE_MONSTERS")) {
                if (!player.hasPermission("battledrones.gui.toggle.monsters")) {
                    plugin.getDroneManager().runCommands(player, plugin.getFileUtils().language, "toggle.monsters");
                    return;
                }
                if (droneHolder.getMonsters() == 1) {
                    droneHolder.setMonsters(0);
                } else {
                    droneHolder.setMonsters(1);
                }
                new DroneGUI(plugin.getPlayerMenu(player), drone).open();
                updateAI = true;
            } else if (file.getStringList(slot + ".OPTIONS").contains("DRONE_ANIMALS")) {
                if (!player.hasPermission("battledrones.gui.toggle.animals")) {
                    plugin.getDroneManager().runCommands(player, plugin.getFileUtils().language, "toggle.animals");
                    return;
                }
                if (droneHolder.getAnimals() == 1) {
                    droneHolder.setAnimals(0);
                } else {
                    droneHolder.setAnimals(1);
                }
                new DroneGUI(plugin.getPlayerMenu(player), drone).open();
                updateAI = true;
            } else if (file.getStringList(slot + ".OPTIONS").contains("DRONE_PLAYERS")) {
                if (!player.hasPermission("battledrones.gui.toggle.players")) {
                    plugin.getDroneManager().runCommands(player, plugin.getFileUtils().language, "toggle.players");
                    return;
                }
                if (droneHolder.getPlayers() == 1) {
                    droneHolder.setPlayers(0);
                } else {
                    droneHolder.setPlayers(1);
                }
                new DroneGUI(plugin.getPlayerMenu(player), drone).open();
                updateAI = true;
            } else if (file.getStringList(slot + ".OPTIONS").contains("DRONE_WHITELIST")) {
                new WhitelistGUI(plugin.getPlayerMenu(player), drone).open();
            } else if (file.getStringList(slot + ".OPTIONS").contains("DRONE_AMMO")) {
                new AmmoGUI(plugin.getPlayerMenu(player), drone).open();
            } else if (file.getStringList(slot + ".OPTIONS").contains("DRONE_UPGRADE")) {
                final FileConfiguration file = plugin.droneFiles.get(drone);
                final String path = playerConnect.getGroup() + "." + (droneHolder.getLevel() + 1);
                if (file.contains(path)) {
                    final DroneUpgradeEvent droneUpgradeEvent = new DroneUpgradeEvent(player, playerConnect, droneHolder);
                    plugin.getServer().getPluginManager().callEvent(droneUpgradeEvent);
                    if (droneUpgradeEvent.isCancelled()) {
                        return;
                    }
                    if (plugin.getSupport().vault.withdraw(player, file.getLong(path + ".cost"))) {
                        droneHolder.setLevel((droneHolder.getLevel() + 1));
                        if (plugin.getFileUtils().config.getBoolean("update-upgrade") && playerConnect.isActive()) {
                            final DroneSpawnEvent droneSpawnEvent = new DroneSpawnEvent(player, playerConnect, droneHolder);
                            droneSpawnEvent.setBypassWait(true);
                            droneSpawnEvent.setBypassDroneAmount(true);
                            droneSpawnEvent.setBypassLocation(true);
                            droneSpawnEvent.setType(Type.UPGRADE);
                            if (playerConnect.head != null) {
                                droneSpawnEvent.setLocation(playerConnect.head.getLocation());
                                droneSpawnEvent.setRemoveTarget(false);
                                if (plugin.park.contains(uuid)) {
                                    droneSpawnEvent.setRemovePark(false);
                                }
                            }
                            droneSpawnEvent.spawn();
                        }
                        for (String command : file.getStringList(playerConnect.getGroup() + "." + droneHolder.getLevel() + ".commands.levelup")) {
                            plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", player.getName()));
                        }
                        droneHolder.save();
                        new DroneGUI(plugin.getPlayerMenu(player), drone).open();
                    } else {
                        for (String command : file.getStringList(playerConnect.getGroup() + "." + droneHolder.getLevel() + ".commands.enough")) {
                            plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", player.getName()));
                        }
                    }
                } else {
                    for (String command : file.getStringList(playerConnect.getGroup() + "." + droneHolder.getLevel() + ".commands.max")) {
                        plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", player.getName()));
                    }
                }
            } else if (file.getStringList(slot + ".OPTIONS").contains("CLOSE")) {
                player.closeInventory();
            }
            if (plugin.getFileUtils().config.getBoolean("update-toggle") && updateAI) {
                if (playerConnect.isActive() && playerConnect.getActive().equalsIgnoreCase(drone)) {
                    playerConnect.stopAI();
                    final DroneRegistry droneRegistry = plugin.droneRegistry.get(drone);
                    droneRegistry.follow(player, playerConnect, droneHolder);
                    droneRegistry.find(player, playerConnect, droneHolder);
                    droneRegistry.ability(player, playerConnect, droneHolder);
                }
            }
            plugin.getItemStackManager().dispatchCommand(file, slot, player);
        }
    }

    @Override
    public void setItems() {
        final DroneHolder droneHolder = plugin.getDroneHolder(uuid, drone);
        setPlayerGUI(plugin.droneFiles.get(drone),
                plugin.guiFiles.get(drone),
                plugin.getPlayerConnect(playerMenu.getUuid()).getGroup(),
                inventory,
                droneHolder.getMonsters(),
                droneHolder.getAnimals(),
                droneHolder.getPlayers(),
                droneHolder.getLevel()
        );
    }

    public void setPlayerGUI(final FileConfiguration file, final FileConfiguration gui, final String group, final Inventory inventory, final int monsters, final int animals, final int players, final int drone_level) {
        final String path_next = group + "." + (drone_level + 1);
        if (gui.contains("settings.monsters")) {
            if (monsters == 1) {
                setItemStack(inventory, gui, "settings.monsters.enabled");
            } else {
                setItemStack(inventory, gui, "settings.monsters.disabled");
            }
        }
        if (gui.contains("settings.animals")) {
            if (animals == 1) {
                setItemStack(inventory, gui, "settings.animals.enabled");
            } else {
                setItemStack(inventory, gui, "settings.animals.disabled");
            }
        }
        if (gui.contains("settings.players")) {
            if (players == 1) {
                setItemStack(inventory, gui, "settings.players.enabled");
            } else {
                setItemStack(inventory, gui, "settings.players.disabled");
            }
        }
        final PlayerConnect playerConnect = plugin.getPlayerConnect(uuid);
        DroneHolder droneHolder = plugin.getDroneHolder(uuid, drone);
        for (String key : Objects.requireNonNull(gui.getConfigurationSection("")).getKeys(false)) {
            if (!key.equalsIgnoreCase("settings")) {
                ItemStack itemStack;
                if (!gui.contains(key + ".HEAD")) {
                    itemStack = plugin.getItemStackManager().getItemStack(gui.getString(key + ".MATERIAL"), gui.getInt(key + ".AMOUNT"));
                } else {
                    itemStack = plugin.drone_heads.get(gui.getString(key + ".HEAD"));
                }
                ItemMeta itemMeta = itemStack.getItemMeta();
                if (itemMeta == null) {
                    return;
                }
                itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', setPlaceholders(playerConnect, droneHolder, gui.getString(key + ".NAME"))));
                ArrayList<String> list = new ArrayList<>();
                for (String lores : gui.getStringList(key + ".LORES")) {
                    list.add(ChatColor.translateAlternateColorCodes('&', setPlaceholders(playerConnect, droneHolder, lores)));
                }
                itemMeta.setLore(list);
                itemStack.setItemMeta(itemMeta);
                inventory.setItem(Integer.parseInt(key), itemStack);
            }
        }
        if (!file.contains(path_next)) {
            setItemStack(inventory, gui, "settings.upgrade");
        }
    }

    public String setPlaceholders(final PlayerConnect playerConnect, final DroneHolder droneHolder, String text) {
        final Matcher matcher = Pattern.compile("[{]([^{}]+)[}]").matcher(text);
        while (matcher.find()) {
            final String get = plugin.droneRegistry.get(drone).onPlaceholderRequest(player, playerConnect, droneHolder, matcher.group(1));
            if (get != null) {
                text = text.replace("{" + matcher.group(1) + "}", get);
            }
        }
        return text;
    }

    public void setItemStack(final Inventory inventory, final FileConfiguration file, final String path) {
        ItemStack itemStack;
        if (!file.contains(path + ".HEAD")) {
            itemStack = plugin.getItemStackManager().getItemStack(file.getString(path + ".MATERIAL"), file.getInt(path + ".AMOUNT"));
        } else {
            itemStack = plugin.drone_heads.get(file.getString(path + ".HEAD"));
        }
        final ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return;
        }
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(file.getString(path + ".NAME"))));
        final ArrayList<String> list = new ArrayList<>();
        for (String lores : file.getStringList(path + ".LORES")) {
            list.add(ChatColor.translateAlternateColorCodes('&', lores));
        }
        itemMeta.setLore(list);
        itemStack.setItemMeta(itemMeta);
        inventory.setItem(file.getInt(path + ".POSITION"), itemStack);
    }
}