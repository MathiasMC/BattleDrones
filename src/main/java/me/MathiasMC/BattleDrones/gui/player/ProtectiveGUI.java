package me.MathiasMC.BattleDrones.gui.player;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import me.MathiasMC.BattleDrones.gui.GUI;
import me.MathiasMC.BattleDrones.gui.Menu;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.HashMap;
import java.util.Objects;

public class ProtectiveGUI extends GUI {

    private final BattleDrones plugin = BattleDrones.call;
    private final FileConfiguration file;
    private final Player player = playerMenu.getPlayer();
    private final String uuid = playerMenu.getUuid();
    private final PlayerConnect playerConnect = playerMenu.getPlayerConnect();
    private final String shield_generator_id;
    private final DroneHolder shield_generator_droneHolder;
    private final FileConfiguration shield_generator_file;
    private final String healing_id;
    private final DroneHolder healing_droneHolder;
    private final FileConfiguration healing_file;

    public ProtectiveGUI(Menu playerMenu) {
        super(playerMenu);
        this.file = plugin.guiFiles.get("player_protective");
        this.shield_generator_id = "shield_generator";
        this.shield_generator_droneHolder = plugin.getDroneHolder(uuid, shield_generator_id);
        this.shield_generator_file = plugin.droneFiles.get(shield_generator_id);
        this.healing_id = "healing";
        this.healing_droneHolder = plugin.getDroneHolder(uuid, healing_id);
        this.healing_file = plugin.droneFiles.get(healing_id);
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
        if (shield_generator_file.getInt("gui.POSITION") == slot && shield_generator_droneHolder.getUnlocked() == 1) {
            plugin.guiManager.playerGUI(e, player, playerConnect, shield_generator_droneHolder, shield_generator_id, shield_generator_file);
        } else if (healing_file.getInt("gui.POSITION") == slot && healing_droneHolder.getUnlocked() == 1) {
            plugin.guiManager.playerGUI(e, player, playerConnect, healing_droneHolder, healing_id, healing_file);
        } else if (file.getStringList(slot + ".OPTIONS").contains("BACK")) {
            new PlayerGUI(plugin.getPlayerMenu(player)).open();
        }
        if (file.contains(String.valueOf(slot))) {
            plugin.guiManager.dispatchCommand(file, slot, player);
        }
    }

    @Override
    public void setItems() {
        plugin.guiManager.setGUIItemStack(inventory, file, player);
        HashMap<String, Integer> drones = new HashMap<>();
        if (shield_generator_droneHolder.getUnlocked() == 1) {
            drones.put(shield_generator_id, shield_generator_droneHolder.getLevel());
        }
        if (healing_droneHolder.getUnlocked() == 1) {
            drones.put(healing_id, healing_droneHolder.getLevel());
        }
        plugin.guiManager.setDrones(uuid, drones, inventory);
    }
}