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

public class SpecialGUI extends GUI {

    private final BattleDrones plugin = BattleDrones.call;
    private final FileConfiguration file;
    private final Player player = playerMenu.getPlayer();
    private final String uuid = playerMenu.getUuid();
    private final PlayerConnect playerConnect = playerMenu.getPlayerConnect();
    private final String flamethrower_id;
    private final DroneHolder flamethrower_droneHolder;
    private final FileConfiguration flamethrower_file;
    private final String lightning_id;
    private final DroneHolder lightning_droneHolder;
    private final FileConfiguration lightning_file;

    public SpecialGUI(Menu playerMenu) {
        super(playerMenu);
        this.file = plugin.guiFiles.get("player_special");
        this.flamethrower_id = "flamethrower";
        this.flamethrower_droneHolder = plugin.getDroneHolder(uuid, flamethrower_id);
        this.flamethrower_file = plugin.droneFiles.get(flamethrower_id);
        this.lightning_id = "lightning";
        this.lightning_droneHolder = plugin.getDroneHolder(uuid, lightning_id);
        this.lightning_file = plugin.droneFiles.get(lightning_id);
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
        if (flamethrower_file.getInt("gui.POSITION") == slot && flamethrower_droneHolder.getUnlocked() == 1) {
            plugin.guiManager.playerGUI(e, player, playerConnect, flamethrower_droneHolder, flamethrower_id, flamethrower_file);
        } else if (lightning_file.getInt("gui.POSITION") == slot && lightning_droneHolder.getUnlocked() == 1) {
            plugin.guiManager.playerGUI(e, player, playerConnect, lightning_droneHolder, lightning_id, lightning_file);
        } else if (file.getStringList(slot + ".OPTIONS").contains("BACK")) {
            new PlayerGUI(plugin.getPlayerMenu(player)).open();
        }
    }

    @Override
    public void setItems() {
        plugin.guiManager.setGUIItemStack(inventory, file, player);
        final HashMap<String, Integer> drones = new HashMap<>();
        if (flamethrower_droneHolder.getUnlocked() == 1) {
            drones.put(flamethrower_id, flamethrower_droneHolder.getLevel());
        }
        if (lightning_droneHolder.getUnlocked() == 1) {
            drones.put(lightning_id, lightning_droneHolder.getLevel());
        }
        plugin.guiManager.setDrones(uuid, drones, inventory);
    }
}