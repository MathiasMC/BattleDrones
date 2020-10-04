package me.MathiasMC.BattleDrones.drones;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashSet;

public class ShieldGenerator {

    private final BattleDrones plugin;

    public ShieldGenerator(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    public final HashSet<String> cooldown = new HashSet<>();

    public void shot(final Player player) {
        final String uuid = player.getUniqueId().toString();
        final PlayerConnect playerConnect = plugin.get(uuid);
        final String drone = "shield_generator";
        final DroneHolder droneHolder = plugin.getDroneHolder(uuid, drone);
        final String group = playerConnect.getGroup();
        final FileConfiguration shield_generator = plugin.droneFiles.get(drone);
        final String path = group + "." + droneHolder.getLevel() + ".";
        final FileConfiguration particleFile = plugin.particles.get;
        if (shield_generator.contains(path + "particle.1")) {
            final String customParticle = shield_generator.getString(path + "particle.1");
            if (particleFile.contains(customParticle)) {
                final ArmorStand armorStand = playerConnect.head;
                final int delay = particleFile.getInt(customParticle + ".delay");
                final int size = particleFile.getInt(customParticle + ".size");
                final int amount = particleFile.getInt(customParticle + ".amount");
                final int r = particleFile.getInt(customParticle + ".rgb.r");
                final int g = particleFile.getInt(customParticle + ".rgb.g");
                final int b = particleFile.getInt(customParticle + ".rgb.b");
                final int yOffset = particleFile.getInt(customParticle + ".y-offset");
                playerConnect.ShootTaskID = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
                    if (!cooldown.contains(uuid) && droneHolder.getAmmo() > 0) {
                        plugin.particleManager.displayParticle(customParticle, particleFile.getString(customParticle + ".particle"), armorStand.getLocation().add(0, yOffset, 0), r, g, b, size, amount);
                    }
                }, 0, delay).getTaskId();
            }
        }
    }

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
                            plugin.droneManager.checkMessage(droneHolder.getAmmo(), shield_generator.getInt(path + "max-ammo-slots") * 64, e.getEntity().getName(), "ammo");
                            if (e.getDamager() instanceof Player) {
                                shieldGeneratorRun(shield_generator, path + "run.player", playerConnect.head.getLocation(), e.getEntity().getName(), String.valueOf(plugin.calculateManager.getProcentFromDouble(randomReduce)));
                            } else if (plugin.droneManager.isMonster(e.getDamager())) {
                                shieldGeneratorRun(shield_generator, path + "run.monster", playerConnect.head.getLocation(), e.getEntity().getName(), String.valueOf(plugin.calculateManager.getProcentFromDouble(randomReduce)));
                            }
                            if (shield_generator.contains(path + "particle.2")) {
                                final String customParticle = shield_generator.getString(path + "particle.2");
                                final FileConfiguration particleFile = plugin.particles.get;
                                if (particleFile.contains(customParticle)) {
                                    final int delay = particleFile.getInt(customParticle + ".delay");
                                    final int size = particleFile.getInt(customParticle + ".size");
                                    final int amount = particleFile.getInt(customParticle + ".amount");
                                    final int r = particleFile.getInt(customParticle + ".rgb.r");
                                    final int g = particleFile.getInt(customParticle + ".rgb.g");
                                    final int b = particleFile.getInt(customParticle + ".rgb.b");
                                    final int yOffset = particleFile.getInt(customParticle + ".y-offset");
                                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                                        plugin.particleManager.displayParticle(customParticle, particleFile.getString(customParticle + ".particle"), playerConnect.head.getLocation().add(0, yOffset, 0), r, g, b, size, amount);
                                    }, delay);
                                }
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

    private void shieldGeneratorRun(final FileConfiguration file, final String path, final Location location, final String targetName, final String reduce) {
        final World world = location.getWorld();
        if (world == null) {
            return;
        }
        final String worldName = world.getName();
        final String x = String.valueOf(location.getBlockX());
        final String y = String.valueOf(location.getBlockX());
        final String z = String.valueOf(location.getBlockX());
        for (String command : file.getStringList(path)) {
            plugin.getServer().dispatchCommand(plugin.consoleSender, command
                    .replace("{world}", worldName)
                    .replace("{x}", x)
                    .replace("{y}", y)
                    .replace("{z}", z)
                    .replace("{damage}", reduce)
                    .replace("{target}", targetName));
        }
    }
}
