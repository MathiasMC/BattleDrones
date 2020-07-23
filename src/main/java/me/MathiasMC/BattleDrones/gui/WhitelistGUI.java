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

    private final FileConfiguration file;

    private final String drone;

    public WhitelistGUI(Menu playerMenu, String drone) {
        super(playerMenu);
        this.drone = drone;
        file = BattleDrones.call.guiFiles.get(drone + "_whitelist");
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
        final Player player = playerMenu.getPlayer();
        if (file.contains(String.valueOf(slot))) {
            if (file.getStringList(slot + ".OPTIONS").contains("BACK")) {
                new DroneMenu(BattleDrones.call.getPlayerMenu(player), drone).open();
            } else if (file.getStringList(slot + ".OPTIONS").contains("DRONE_WHITELIST_ADD")) {
                final String uuid = playerMenu.getUuid();
                if (!BattleDrones.call.drone_whitelist.containsKey(uuid)) {
                    player.closeInventory();
                    for (String command : BattleDrones.call.language.get.getStringList("gui.whitelist.time")) {
                        BattleDrones.call.getServer().dispatchCommand(BattleDrones.call.consoleSender, command.replace("{player}", player.getName()));
                    }
                    BattleDrones.call.drone_whitelist.put(uuid, drone);
                    BattleDrones.call.getServer().getScheduler().runTaskLater(BattleDrones.call, () ->
                                    BattleDrones.call.drone_whitelist.remove(uuid),
                            20L * BattleDrones.call.language.get.getLong("gui.whitelist.seconds"));
                } else {
                    for (String command : BattleDrones.call.language.get.getStringList("gui.whitelist.active")) {
                        BattleDrones.call.getServer().dispatchCommand(BattleDrones.call.consoleSender, command.replace("{player}", player.getName()));
                    }
                }
            }
        }
        final ItemStack itemStack = e.getCurrentItem();
        if (itemStack != null && itemStack.getType().equals(Material.PLAYER_HEAD) && itemStack.getItemMeta() != null && e.isRightClick()) {
            final DroneHolder droneHolder = BattleDrones.call.getDroneHolder(player.getUniqueId().toString(), drone);
            final List<String> players = droneHolder.getExclude();
            final String name = ChatColor.stripColor(itemStack.getItemMeta().getDisplayName());
            if (players.contains(name)) {
                players.remove(name);
                droneHolder.setExclude(players);
                new WhitelistGUI(BattleDrones.call.getPlayerMenu(player), drone).open();
            }
        }
    }

    @Override
    public void setItems() {
        BattleDrones.call.guiManager.setGUIItemStack(inventory, file, playerMenu.getPlayer());
        List<String> players = BattleDrones.call.getDroneHolder(playerMenu.getPlayer().getUniqueId().toString(), drone).getExclude();
        int index = 0;
        int set = 0;
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack == null) {
                if (players.size() > index) {
                    final ItemStack itemStackS = BattleDrones.call.drone_heads.get("whitelist");
                    final ItemMeta itemMeta = itemStackS.getItemMeta();
                    if (itemMeta == null) { return; }
                    itemMeta.setDisplayName(BattleDrones.call.calculateManager.getChatColor(Objects.requireNonNull(BattleDrones.call.language.get.getString("gui.whitelist.name-color"))) + players.get(index));
                    final ArrayList<String> lores = new ArrayList<>();
                    for (String lore : BattleDrones.call.language.get.getStringList("gui.whitelist.lores")) {
                        lores.add(ChatColor.translateAlternateColorCodes('&', lore));
                    }
                    itemMeta.setLore(lores);
                    itemStackS.setItemMeta(itemMeta);
                    inventory.setItem(set, itemStackS);
                }
                index++;
            }
            set++;
        }
    }
}