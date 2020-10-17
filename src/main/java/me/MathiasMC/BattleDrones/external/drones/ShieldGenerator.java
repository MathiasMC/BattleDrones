package me.MathiasMC.BattleDrones.external.drones;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.api.DroneRegistry;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashSet;

public class ShieldGenerator extends DroneRegistry implements Listener {

    private final BattleDrones plugin;

    public final HashSet<String> cooldown = new HashSet<>();

    public ShieldGenerator(BattleDrones plugin, String droneName, String droneCategory) {
        super(plugin, droneName, droneCategory);
        this.plugin = plugin;
    }

    @Override
    public void ability(final Player player, final PlayerConnect playerConnect, final DroneHolder droneHolder) {
        final String uuid = player.getUniqueId().toString();
        final FileConfiguration particleFile = plugin.getFileUtils().particles;
        if (!particleFile.contains(droneName)) {
            return;
        }
        final ArmorStand armorStand = playerConnect.head;
        final String particleType = particleFile.getString(droneName + ".particle");
        final int delay = particleFile.getInt(droneName + ".delay");
        final int size = particleFile.getInt(droneName + ".size");
        final int amount = particleFile.getInt(droneName + ".amount");
        final int r = particleFile.getInt(droneName + ".rgb.r");
        final int g = particleFile.getInt(droneName + ".rgb.g");
        final int b = particleFile.getInt(droneName + ".rgb.b");
        final int yOffset = particleFile.getInt(droneName + ".y-offset");
        playerConnect.ability = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (cooldown.contains(uuid)) {
                return;
            }
            if (droneHolder.getAmmo() == 0) {
                if (!player.hasPermission("battledrones.bypass.ammo." + droneName.replace("_", "."))) {
                    return;
                }
            }
            plugin.getParticleManager().displayParticle(droneName, particleType, armorStand.getLocation().add(0, yOffset, 0), r, g, b, size, amount);
        }, 0, delay).getTaskId();
    }

    @EventHandler
    public void onEntity(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player) {
            final Player player = (Player) e.getEntity();
            final String uuid = player.getUniqueId().toString();
            final PlayerConnect playerConnect = plugin.getPlayerConnect(uuid);
            if (!playerConnect.isActive()) {
                return;
            }
            if (playerConnect.getActive().equalsIgnoreCase(droneName)) {
                if (!cooldown.contains(uuid)) {
                    final DroneHolder droneHolder = plugin.getDroneHolder(uuid, droneName);
                    if (droneHolder.getAmmo() > 0 || player.hasPermission("battledrones.bypass.ammo." + droneName)) {
                        FileConfiguration file = plugin.droneFiles.get(droneName);
                        cooldown.add(uuid);
                        playerConnect.setHealing(false);
                        final String path = playerConnect.getGroup() + "." + droneHolder.getLevel() + ".";
                        final double finalDamage = e.getFinalDamage();
                        final double randomReduce = plugin.getCalculateManager().randomDouble(file.getDouble(path + "min"), file.getDouble(path + "max"));
                        final double reducedDamage = finalDamage - finalDamage * randomReduce;
                        e.setDamage(reducedDamage);
                        if (e.getDamager() instanceof Player) {
                            shieldGeneratorRun(file, path + "run.player", player, player.getName(), String.valueOf(plugin.getCalculateManager().getProcentFromDouble(randomReduce)));
                        } else if (plugin.getEntityManager().isMonster(e.getDamager())) {
                            shieldGeneratorRun(file, path + "run.monster", player, player.getName(), String.valueOf(plugin.getCalculateManager().getProcentFromDouble(randomReduce)));
                        }
                            final String particleName = droneName + "_ability";
                            final FileConfiguration particleFile = plugin.getFileUtils().particles;
                            if (particleFile.contains(particleName)) {
                                plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.getParticleManager().displayParticle(
                                        particleName,
                                        particleFile.getString(particleName + ".particle"),
                                        playerConnect.head.getLocation().add(0, particleFile.getInt(particleName + ".y-offset"), 0),
                                        particleFile.getInt(particleName + ".rgb.r"),
                                        particleFile.getInt(particleName + ".rgb.g"),
                                        particleFile.getInt(particleName + ".rgb.b"),
                                        particleFile.getInt(particleName + ".size"),
                                        particleFile.getInt(particleName + ".amount")),
                                        particleFile.getInt(particleName + ".delay"));
                            }
                        plugin.getDroneManager().checkMessage(droneHolder.getAmmo(), file.getInt(path + "max-ammo-slots") * 64, player.getName(), "ammo");
                        plugin.getDroneManager().takeAmmo(player, playerConnect, droneHolder, file, path);
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                            cooldown.remove(uuid);
                            playerConnect.setHealing(true);
                        }, file.getLong(path + "cooldown") * 20);
                    }
                }
            }
        }
    }

    private void shieldGeneratorRun(final FileConfiguration file, final String path, final Player player, final String targetName, final String reduce) {
        for (String command : file.getStringList(path)) {
            plugin.getServer().dispatchCommand(plugin.consoleSender, plugin.getPlaceholderManager().replacePlaceholders(player, command)
                    .replace("{damage}", reduce)
                    .replace("{target}", targetName));
        }
    }
}
