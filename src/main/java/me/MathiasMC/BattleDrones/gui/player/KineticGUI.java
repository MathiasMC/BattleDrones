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

public class KineticGUI extends GUI {

    private final BattleDrones plugin = BattleDrones.call;
    private final FileConfiguration file;
    private final Player player = playerMenu.getPlayer();
    private final String uuid = playerMenu.getUuid();
    private final PlayerConnect playerConnect = playerMenu.getPlayerConnect();
    private final String machine_gun_id;
    private final DroneHolder machine_gun_droneHolder;
    private final FileConfiguration machine_gun_file;

    public KineticGUI(Menu playerMenu) {
        super(playerMenu);
        this.file = plugin.guiFiles.get("player_kinetic");
        this.machine_gun_id = "machine_gun";
        this.machine_gun_droneHolder = plugin.getDroneHolder(uuid, machine_gun_id);
        this.machine_gun_file = plugin.droneFiles.get(machine_gun_id);
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
        if (machine_gun_file.getInt("gui.POSITION") == slot && machine_gun_droneHolder.getUnlocked() == 1) {
            plugin.guiManager.playerGUI(e, player, playerConnect, machine_gun_droneHolder, machine_gun_id, machine_gun_file, "machine.gun");
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
        if (machine_gun_droneHolder.getUnlocked() == 1) {
            drones.put(machine_gun_id, machine_gun_droneHolder.getLevel());
        }
        plugin.guiManager.setDrones(uuid, drones, inventory);
    }
}