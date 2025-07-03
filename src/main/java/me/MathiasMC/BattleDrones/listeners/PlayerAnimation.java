package me.MathiasMC.BattleDrones.listeners;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.api.Type;
import me.MathiasMC.BattleDrones.api.events.DroneDamageEvent;
import me.MathiasMC.BattleDrones.api.events.DroneDeathEvent;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.RayTraceResult;

import java.util.List;
import java.util.UUID;

public class PlayerAnimation implements Listener {

    private final BattleDrones plugin;

    public PlayerAnimation(BattleDrones plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEvent(PlayerAnimationEvent event) {
        if (event.getAnimationType() != PlayerAnimationType.ARM_SWING) return;

        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        for (Entity entity : player.getNearbyEntities(3, 3, 3)) {
            if (!(entity instanceof ArmorStand armorStand)) continue;

            String droneOwnerUUID = armorStand.getPersistentDataContainer().get(
                    new NamespacedKey(plugin, "drone_uuid"),
                    PersistentDataType.STRING
            );

            if (droneOwnerUUID == null || playerUUID.toString().equalsIgnoreCase(droneOwnerUUID)) continue;

            RayTraceResult result = player.getWorld().rayTraceEntities(
                    player.getEyeLocation(),
                    player.getEyeLocation().getDirection(),
                    10.0,
                    e -> e.equals(armorStand)
            );

            if (result != null && result.getHitEntity() == armorStand) {

                PlayerConnect playerConnect = plugin.getPlayerConnect(droneOwnerUUID);

                if (!playerConnect.isActive()) return;

                DroneHolder droneHolder = plugin.getDroneHolder(droneOwnerUUID, playerConnect.getActive());

                String path = playerConnect.getGroup() + "." + droneHolder.getLevel();
                DroneDamageEvent droneDamageEvent = new DroneDamageEvent(player, playerConnect, droneHolder);
                plugin.getServer().getPluginManager().callEvent(droneDamageEvent);

                if (droneDamageEvent.isCancelled()) return;

                FileConfiguration file = plugin.droneFiles.get(playerConnect.getActive());
                if (!plugin.getSupport().canTarget(armorStand, file, path + ".worldguard.damage")) {
                    return;
                }

                OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(UUID.fromString(droneOwnerUUID));
                if (!offlinePlayer.isOnline()) return;
                Player offline = (Player) offlinePlayer;

                if (droneHolder.getHealth() - 1 > 0) {
                    droneHolder.setHealth(droneHolder.getHealth() - 1);

                    List<String> hitCommands = file.getStringList(path + ".hit-commands");
                    dispatchCommands(hitCommands, offline);

                    long currentHealth = droneHolder.getHealth();
                    long maxHealth = file.getLong(path + ".health");

                    long percentLeft = (long) Math.floor(currentHealth * (100D / maxHealth));
                    long previousPercent = (long) Math.floor((currentHealth + 1) * (100D / maxHealth));

                    if ((percentLeft == previousPercent && percentLeft != 0L) || (percentLeft == 0L && currentHealth != 1)) {
                        return;
                    }

                    List<String> lowHealthCommands = file.getStringList("low-" + "health" + "." + percentLeft);
                    dispatchCommands(lowHealthCommands, offline);

                    return;
                }

                List<String> deathCommands = file.getStringList("dead.player");
                dispatchCommands(deathCommands, offline);

                DroneDeathEvent droneDeathEvent = new DroneDeathEvent(offline, playerConnect, droneHolder);
                droneDeathEvent.setType(Type.PLAYER);
                plugin.getServer().getPluginManager().callEvent(droneDeathEvent);
                if (droneDeathEvent.isCancelled()) {
                    return;
                }

                playerConnect.stopDrone(true, true);
                playerConnect.setLastActive("");

                droneHolder.setUnlocked(file.getInt("dead.unlocked"));
                if (file.getLong("dead.set-level") != 0) {
                    droneHolder.setLevel(file.getInt("dead.set-level"));
                }
                if (!file.getBoolean("dead.ammo")) {
                    droneHolder.setAmmo(0);
                }
                droneHolder.save();
            }
        }
    }

    private void dispatchCommands(List<String> commands, Player player) {
        for (String command : commands) {
            plugin.getServer().dispatchCommand(
                    plugin.consoleSender,
                    plugin.getPlaceholderManager().replacePlaceholders(
                            player,
                            ChatColor.translateAlternateColorCodes('&', command)
                    )
            );
        }
    }
}