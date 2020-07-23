package me.MathiasMC.BattleDrones.listeners;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityDamageByEntity implements Listener {

    private final BattleDrones plugin;

    public EntityDamageByEntity(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntity(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player) {
            String entityUUID = e.getEntity().getUniqueId().toString();
            PlayerConnect playerConnect = plugin.get(entityUUID);
            if (playerConnect.getActive().equalsIgnoreCase("shield_generator")) {
                DroneHolder droneHolder = plugin.getDroneHolder(entityUUID, "shield_generator");
                if (!plugin.shieldGenerator.cooldown.contains(entityUUID)) {
                    if (droneHolder.getAmmo() > 0) {
                        plugin.shieldGenerator.cooldown.add(entityUUID);
                        playerConnect.setRegen(false);
                        FileConfiguration shield_generator = plugin.droneFiles.get("shield_generator");
                        String path = playerConnect.getGroup() + "." + droneHolder.getLevel() + ".";
                        double finalDamage = e.getFinalDamage();
                        double randomReduce = plugin.randomDouble(shield_generator.getDouble(path + "min"), shield_generator.getDouble(path + "max"));
                        double reducedDamage = finalDamage - finalDamage * randomReduce;
                        e.setDamage(reducedDamage);
                        if (e.getDamager() instanceof Player) {
                            shieldGeneratorRun(shield_generator, path + "run.player", playerConnect.head.getLocation(), e.getEntity().getName(), String.valueOf(plugin.calculateManager.getProcentFromDouble(randomReduce)));
                        } else if (e.getDamager() instanceof Monster) {
                            shieldGeneratorRun(shield_generator, path + "run.monster", playerConnect.head.getLocation(), e.getEntity().getName(), String.valueOf(plugin.calculateManager.getProcentFromDouble(randomReduce)));
                        }
                        if (shield_generator.contains(path + "particle-circle.ability")) {
                            double radius = shield_generator.getInt(path + "particle-circle.ability.radius");
                            int size = shield_generator.getInt(path + "particle-circle.ability.size");
                            int amount = shield_generator.getInt(path + "particle-circle.ability.amount");
                            int rows = shield_generator.getInt(path + "particle-circle.ability.rows");
                            int r = shield_generator.getInt(path + "particle-circle.ability.rgb.r");
                            int g = shield_generator.getInt(path + "particle-circle.ability.rgb.g");
                            int b = shield_generator.getInt(path + "particle-circle.ability.rgb.b");
                            plugin.calculateManager.sphere(playerConnect.head.getLocation().add(0, 0.8, 0), radius, rows, r, g, b, size, amount);
                        }
                        plugin.droneManager.takeAmmo(playerConnect, droneHolder, shield_generator, path, e.getEntity().getName());
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                            plugin.shieldGenerator.cooldown.remove(entityUUID);
                            playerConnect.setRegen(true);
                        }, shield_generator.getLong(path + "cooldown") * 20);
                    }
                }
            }
        }
    }

    private void shieldGeneratorRun(FileConfiguration file, String path, Location location, String targetName, String reduce) {
        for (String command : file.getStringList(path)) {
            BattleDrones.call.getServer().dispatchCommand(BattleDrones.call.consoleSender, command
                    .replace("{world}", location.getWorld().getName())
                    .replace("{x}", String.valueOf(location.getBlockX()))
                    .replace("{y}", String.valueOf(location.getBlockY()))
                    .replace("{z}", String.valueOf(location.getBlockZ()))
                    .replace("{damage}", reduce)
                    .replace("{target}", targetName));
        }
    }
}
