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

public class ExplodeGUI extends GUI {

    private final BattleDrones plugin = BattleDrones.call;
    private final FileConfiguration file;
    private final Player player = playerMenu.getPlayer();
    private final String uuid = playerMenu.getUuid();
    private final PlayerConnect playerConnect = playerMenu.getPlayerConnect();
    private final String rocket_id;
    private final String faf_missile_id;
    private final String mortar_id;
    private final DroneHolder rocket_droneHolder;
    private final DroneHolder faf_missile_droneHolder;
    private final DroneHolder mortar_droneHolder;
    private final FileConfiguration rocket_file;
    private final FileConfiguration faf_missile_file;
    private final FileConfiguration mortar_file;

    public ExplodeGUI(Menu playerMenu) {
        super(playerMenu);
        this.file = plugin.guiFiles.get("player_explode");
        this.rocket_id = "rocket";
        this.faf_missile_id = "faf_missile";
        this.mortar_id = "mortar";
        this.rocket_droneHolder = plugin.getDroneHolder(uuid, rocket_id);
        this.faf_missile_droneHolder = plugin.getDroneHolder(uuid, faf_missile_id);
        this.mortar_droneHolder = plugin.getDroneHolder(uuid, mortar_id);
        this.rocket_file = plugin.droneFiles.get(rocket_id);
        this.faf_missile_file = plugin.droneFiles.get(faf_missile_id);
        this.mortar_file = plugin.droneFiles.get(mortar_id);
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
        if (rocket_file.getInt("gui.POSITION") == slot && rocket_droneHolder.getUnlocked() == 1) {
            plugin.guiManager.playerGUI(e, player, playerConnect, rocket_droneHolder, rocket_id, rocket_file, rocket_id);
        } else if (faf_missile_file.getInt("gui.POSITION") == slot && faf_missile_droneHolder.getUnlocked() == 1) {
            plugin.guiManager.playerGUI(e, player, playerConnect, faf_missile_droneHolder, faf_missile_id, faf_missile_file, "faf.missile");
        } else if (mortar_file.getInt("gui.POSITION") == slot && mortar_droneHolder.getUnlocked() == 1) {
            plugin.guiManager.playerGUI(e, player, playerConnect, mortar_droneHolder, mortar_id, mortar_file, mortar_id);
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
        final HashMap<String, Integer> drones = new HashMap<>();
        if (rocket_droneHolder.getUnlocked() == 1) {
            drones.put(rocket_id, rocket_droneHolder.getLevel());
        }
        if (faf_missile_droneHolder.getUnlocked() == 1) {
            drones.put(faf_missile_id, faf_missile_droneHolder.getLevel());
        }
        if (mortar_droneHolder.getUnlocked() == 1) {
            drones.put(mortar_id, mortar_droneHolder.getLevel());
        }
        plugin.guiManager.setDrones(uuid, drones, inventory);
    }
}