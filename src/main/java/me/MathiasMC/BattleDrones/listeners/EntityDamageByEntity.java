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
            final String entityUUID = e.getEntity().getUniqueId().toString();
            if (plugin.list().contains(entityUUID)) {
                final PlayerConnect playerConnect = plugin.get(entityUUID);
                final String drone = "shield_generator";
                if (playerConnect.getActive().equalsIgnoreCase(drone)) {
                    final DroneHolder droneHolder = plugin.getDroneHolder(entityUUID, drone);
                    if (!plugin.shieldGenerator.cooldown.contains(entityUUID)) {
                        if (droneHolder.getAmmo() > 0) {
                            plugin.shieldGenerator.cooldown.add(entityUUID);
                            playerConnect.setRegen(false);
                            final FileConfiguration shield_generator = plugin.droneFiles.get(drone);
                            final String path = playerConnect.getGroup() + "." + droneHolder.getLevel() + ".";
                            final double finalDamage = e.getFinalDamage();
                            final double randomReduce = plugin.randomDouble(shield_generator.getDouble(path + "min"), shield_generator.getDouble(path + "max"));
                            final double reducedDamage = finalDamage - finalDamage * randomReduce;
                            e.setDamage(reducedDamage);
                            if (e.getDamager() instanceof Player) {
                                shieldGeneratorRun(shield_generator, path + "run.player", playerConnect.head.getLocation(), e.getEntity().getName(), String.valueOf(plugin.calculateManager.getProcentFromDouble(randomReduce)));
                            } else if (e.getDamager() instanceof Monster) {
                                shieldGeneratorRun(shield_generator, path + "run.monster", playerConnect.head.getLocation(), e.getEntity().getName(), String.valueOf(plugin.calculateManager.getProcentFromDouble(randomReduce)));
                            }
                            if (shield_generator.contains(path + "particle-circle.ability")) {
                                final double radius = shield_generator.getInt(path + "particle-circle.ability.radius");
                                final int size = shield_generator.getInt(path + "particle-circle.ability.size");
                                final int amount = shield_generator.getInt(path + "particle-circle.ability.amount");
                                final int rows = shield_generator.getInt(path + "particle-circle.ability.rows");
                                final int r = shield_generator.getInt(path + "particle-circle.ability.rgb.r");
                                final int g = shield_generator.getInt(path + "particle-circle.ability.rgb.g");
                                final int b = shield_generator.getInt(path + "particle-circle.ability.rgb.b");
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
    }

    private void shieldGeneratorRun(FileConfiguration file, String path, Location location, String targetName, String reduce) {
        final String world = location.getWorld().getName();
        final String x = String.valueOf(location.getBlockX());
        final String y = String.valueOf(location.getBlockX());
        final String z = String.valueOf(location.getBlockX());
        for (String command : file.getStringList(path)) {
            BattleDrones.call.getServer().dispatchCommand(BattleDrones.call.consoleSender, command
                    .replace("{world}", world)
                    .replace("{x}", x)
                    .replace("{y}", y)
                    .replace("{z}", z)
                    .replace("{damage}", reduce)
                    .replace("{target}", targetName));
        }
    }
}
