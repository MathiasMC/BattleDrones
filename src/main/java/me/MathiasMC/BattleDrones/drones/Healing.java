package me.MathiasMC.BattleDrones.drones;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class Healing {

    private final BattleDrones plugin;

    public Healing(final BattleDrones plugin) {
        this.plugin = plugin;
    }


    public void shot(final Player player) {
        final String drone = "healing";
        final String uuid = player.getUniqueId().toString();
        final PlayerConnect playerConnect = plugin.get(uuid);
        final DroneHolder droneHolder = plugin.getDroneHolder(uuid, drone);
        final String group = playerConnect.getGroup();
        final FileConfiguration healing = plugin.droneFiles.get(drone);
        final String path = group + "." + droneHolder.getLevel() + ".";
        final ArmorStand armorStand = playerConnect.head;
        playerConnect.ShootTaskID = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            final LivingEntity target = plugin.drone_targets.get(uuid);
            if (target != null) {
                if (droneHolder.getAmmo() > 0) {
                    double health = target.getHealth();
                    double maxHealth = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                    if (armorStand.hasLineOfSight(target) && health < maxHealth) {
                        double add = plugin.randomDouble(healing.getDouble(path + "min"), healing.getDouble(path + "max"));
                        if (health + add > maxHealth) {
                            target.setHealth(maxHealth);
                        } else {
                            target.setHealth(health + add);
                        }
                        plugin.calculateManager.line(armorStand.getEyeLocation().add(0, 0.4, 0), target.getEyeLocation(), healing, path);
                        BattleDrones.call.droneManager.checkAmmo(healing, path, droneHolder.getAmmo(), player.getName());
                        BattleDrones.call.droneManager.checkShot(target, healing, armorStand.getLocation(), path, "run");
                        BattleDrones.call.droneManager.takeAmmo(playerConnect, droneHolder, healing, path, player.getName());
                    }
                }
                playerConnect.setRegen(false);
            } else {
                playerConnect.setRegen(true);
            }
        }, 0, healing.getLong(path + "cooldown") * 20).getTaskId();
    }
}
