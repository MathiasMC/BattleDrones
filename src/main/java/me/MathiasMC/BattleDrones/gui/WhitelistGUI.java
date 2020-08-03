package me.MathiasMC.BattleDrones.gui;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WhitelistGUI extends GUI {

    private final BattleDrones plugin = BattleDrones.call;
    private final FileConfiguration file;
    private final String drone;
    private final Player player = playerMenu.getPlayer();
    private final String uuid = playerMenu.getUuid();

    public WhitelistGUI(Menu playerMenu, String drone) {
        super(playerMenu);
        this.drone = drone;
        file = plugin.guiFiles.get(drone + "_whitelist");
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
            } else if (file.getStringList(slot + ".OPTIONS").contains("DRONE_WHITELIST_ADD")) {
                if (!plugin.drone_whitelist.containsKey(uuid)) {
                    player.closeInventory();
                    for (String command : plugin.language.get.getStringList("gui.whitelist.time")) {
                        plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", player.getName()));
                    }
                    plugin.drone_whitelist.put(uuid, drone);
                    plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                                    plugin.drone_whitelist.remove(uuid),
                            20L * plugin.language.get.getLong("gui.whitelist.seconds"));
                } else {
                    for (String command : plugin.language.get.getStringList("gui.whitelist.active")) {
                        plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", player.getName()));
                    }
                }
            }
            plugin.guiManager.dispatchCommand(file, slot, player);
        }
        final ItemStack itemStack = e.getCurrentItem();
        if (itemStack != null && itemStack.getType().equals(Material.PLAYER_HEAD) && itemStack.getItemMeta() != null && e.isRightClick()) {
            final DroneHolder droneHolder = plugin.getDroneHolder(player.getUniqueId().toString(), drone);
            final List<String> players = droneHolder.getExclude();
            final String name = ChatColor.stripColor(itemStack.getItemMeta().getDisplayName());
            if (players.contains(name)) {
                players.remove(name);
                droneHolder.setExclude(players);
                droneHolder.save();
                new WhitelistGUI(plugin.getPlayerMenu(player), drone).open();
            }
        }
    }

    @Override
    public void setItems() {
        plugin.guiManager.setGUIItemStack(inventory, file, player);
        final List<String> players = plugin.getDroneHolder(uuid, drone).getExclude();
        for (int index = 0; index < 54; index++) {
            if (players.size() > index) {
                final ItemStack itemStackS = plugin.drone_heads.get("whitelist");
                if (itemStackS == null) {
                    plugin.textUtils.gui(player, "head", "whitelist");
                    return;
                }
                final ItemMeta itemMeta = itemStackS.getItemMeta();
                if (itemMeta == null) {
                    return;
                }
                itemMeta.setDisplayName(plugin.calculateManager.getChatColor(Objects.requireNonNull(plugin.language.get.getString("gui.whitelist.name-color"))) + players.get(index));
                final ArrayList<String> lores = new ArrayList<>();
                for (String lore : plugin.language.get.getStringList("gui.whitelist.lores")) {
                    lores.add(ChatColor.translateAlternateColorCodes('&', lore));
                }
                itemMeta.setLore(lores);
                itemStackS.setItemMeta(itemMeta);
                inventory.setItem(index, itemStackS);
            }
        }
    }
}