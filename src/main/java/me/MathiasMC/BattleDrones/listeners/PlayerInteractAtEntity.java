package me.MathiasMC.BattleDrones.listeners;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import me.MathiasMC.BattleDrones.gui.menu.DroneGUI;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.persistence.PersistentDataType;

public class PlayerInteractAtEntity implements Listener {

    private final BattleDrones plugin;

    public PlayerInteractAtEntity(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInteract(PlayerInteractAtEntityEvent e) {
        final Entity entity = e.getRightClicked();
        if (entity instanceof ArmorStand armorStand) {
            final String key = armorStand.getPersistentDataContainer().get(new NamespacedKey(plugin, "drone_uuid"), PersistentDataType.STRING);
            if (key != null) {
                e.setCancelled(true);

                Player player = e.getPlayer();

                if (!player.isSneaking()) return;

                String uuid = player.getUniqueId().toString();

                if (!uuid.equalsIgnoreCase(key)) return;

                PlayerConnect playerConnect = plugin.getPlayerConnect(uuid);

                if (!playerConnect.isActive()) return;

                boolean can = plugin.droneFiles.get(playerConnect.getActive()).getBoolean("drone-click");
                if (!can) return;

                new DroneGUI(plugin.getPlayerMenu(player), playerConnect.getActive()).open();

            }
        }
    }
}
