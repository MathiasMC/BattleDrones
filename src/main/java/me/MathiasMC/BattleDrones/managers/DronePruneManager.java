package me.MathiasMC.BattleDrones.managers;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import me.MathiasMC.BattleDrones.utils.TextUtils;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.UUID;

public class DronePruneManager {

    private final BattleDrones plugin;
    private final TextUtils textUtils;
    private int taskId;

    public DronePruneManager(BattleDrones plugin) {
        this.plugin = plugin;
        this.textUtils = plugin.getTextUtils();
    }

    public void start(long intervalMinutes) {
        long intervalTicks = intervalMinutes * 1200;
        textUtils.info(String.format("[Prune] Started, running every %d minutes.", intervalMinutes));
        taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            long removed = cleanUP(null, false);
            if (removed > 0) {
                textUtils.info(String.format("[Prune] Removed %d entities.", removed));
            }
        }, intervalTicks, intervalTicks);
    }

    public void stop() {
        if (taskId != 0) {
            plugin.getServer().getScheduler().cancelTask(taskId);
        }
    }

    public long cleanUP(Boolean projectiles, boolean stopDrone) {
        long amount = 0;

        for (World world : plugin.getServer().getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof ArmorStand armorStand)) continue;

                String key = armorStand.getPersistentDataContainer().get(plugin.droneKey, PersistentDataType.STRING);

                if (key == null) {

                    if (Boolean.TRUE.equals(projectiles)|| projectiles == null) {
                        amount += removeProjectile(armorStand) ? 1 : 0;
                    }
                    continue;
                }

                PlayerConnect playerConnect = plugin.getPlayerConnect(key);
                if (playerConnect == null) continue;

                if (!playerConnect.isActive()) {
                    amount += getRemoveAmount(projectiles, key, armorStand);
                } else if (stopDrone) {
                    amount += getRemoveAmount(projectiles, key, armorStand);

                    OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(UUID.fromString(key));
                    if (offlinePlayer.isOnline() && offlinePlayer.getName() != null) {
                        DroneHolder droneHolder = plugin.getDroneHolder(key, playerConnect.getActive());
                        List<String> commandList = plugin.getFileUtils().language.getStringList("prune.drone");
                        for (String message : commandList) {
                            String formatted = ChatColor.translateAlternateColorCodes('&',
                                    message.replace("{target}", offlinePlayer.getName())
                                            .replace("{drone}", plugin.getPlaceholderManager().getActiveDrone(droneHolder.getDrone()))
                                            .replace("{amount}", String.valueOf(amount))

                            );
                            plugin.getServer().dispatchCommand(plugin.consoleSender, formatted);
                        }
                    }
                    playerConnect.stopDrone(true, true);

                }

            }
        }
        return amount;
    }

    private long getRemoveAmount(Boolean projectiles, String key, ArmorStand armorStand) {
        long amount = 0;
        if (projectiles == null) {
            amount += removeDrone(armorStand, key) ? 1 : 0;
            amount += removeProjectile(armorStand) ? 1 : 0;
        } else if (projectiles) {
            amount += removeProjectile(armorStand) ? 1 : 0;
        } else {
            amount += removeDrone(armorStand, key) ? 1 : 0;
        }
        return amount;
    }

    private boolean removeDrone(ArmorStand armorStand, String key) {
        if (key != null) {
            armorStand.remove();
            return true;
        }
        return false;
    }

    private boolean removeProjectile(ArmorStand armorStand) {
        String key = armorStand.getPersistentDataContainer().get(plugin.projectileKey, PersistentDataType.STRING);
        if (key != null) {
            armorStand.remove();
            return true;
        }
        return false;
    }
}
