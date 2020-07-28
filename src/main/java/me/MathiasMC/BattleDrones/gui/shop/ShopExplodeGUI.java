package me.MathiasMC.BattleDrones.gui.shop;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import me.MathiasMC.BattleDrones.gui.GUI;
import me.MathiasMC.BattleDrones.gui.Menu;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Objects;

public class ShopExplodeGUI extends GUI {

    private final FileConfiguration file = BattleDrones.call.guiFiles.get("shop_explode");

    public ShopExplodeGUI(Menu playerMenu) {
        super(playerMenu);
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
            if (file.getStringList(slot + ".OPTIONS").contains("DRONE_ROCKET_BUY")) {
                if (player.hasPermission("battledrones.shop.rocket")) {
                    final PlayerConnect playerConnect = BattleDrones.call.get(playerMenu.getUuid());
                    final DroneHolder droneHolder = BattleDrones.call.getDroneHolder(playerMenu.getUuid(), "rocket");
                    if (droneHolder.getUnlocked() != 1) {
                        final long coins = playerConnect.getCoins();
                        final long cost = file.getLong(slot + ".COST");
                        if (!BattleDrones.call.config.get.getBoolean("vault") && coins >= cost ||
                                BattleDrones.call.config.get.getBoolean("vault") &&
                                        BattleDrones.call.getEconomy() != null &&
                                        BattleDrones.call.getEconomy().withdrawPlayer(player, cost).transactionSuccess()) {
                            if (!BattleDrones.call.config.get.getBoolean("vault")) {
                                playerConnect.setCoins(coins - cost);
                            }
                            droneHolder.setUnlocked(1);
                            droneHolder.setHealth(BattleDrones.call.droneFiles.get("rocket").getInt(playerConnect.getGroup() + "." + droneHolder.getLevel() + ".health"));
                            droneHolder.save();
                            for (String command : file.getStringList(slot + ".SHOP-COMMANDS.BOUGHT")) {
                                BattleDrones.call.getServer().dispatchCommand(BattleDrones.call.consoleSender, command.replace("{player}", player.getName()));
                            }
                        } else {
                            for (String command : file.getStringList(slot + ".SHOP-COMMANDS.COINS")) {
                                BattleDrones.call.getServer().dispatchCommand(BattleDrones.call.consoleSender, command.replace("{player}", player.getName()));
                            }
                        }
                    } else {
                        for (String command : file.getStringList(slot + ".SHOP-COMMANDS.HAVE")) {
                            BattleDrones.call.getServer().dispatchCommand(BattleDrones.call.consoleSender, command.replace("{player}", player.getName()));
                        }
                    }
                } else {
                    for (String command : file.getStringList(slot + ".SHOP-COMMANDS.PERMISSION")) {
                        BattleDrones.call.getServer().dispatchCommand(BattleDrones.call.consoleSender, command.replace("{player}", player.getName()));
                    }
                }
            } else if (file.getStringList(slot + ".OPTIONS").contains("BACK")) {
                new ShopGUI(BattleDrones.call.getPlayerMenu(player)).open();
            }
            BattleDrones.call.guiManager.dispatchCommand(file, slot, player);
        }
    }

    @Override
    public void setItems() {
        BattleDrones.call.guiManager.setGUIItemStack(inventory, file, playerMenu.getPlayer());
    }
}