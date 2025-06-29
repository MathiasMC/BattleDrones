package me.MathiasMC.BattleDrones.external.drones;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.api.DroneRegistry;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashSet;

public class ShieldGenerator extends DroneRegistry implements Listener {

    private final BattleDrones plugin;

    public final HashSet<String> cooldown = new HashSet<>();

    public ShieldGenerator(BattleDrones plugin,
                           String droneName,
                           String droneCategory
    ) {
        super(plugin, droneName, droneCategory);
        this.plugin = plugin;
    }

    @Override
    public void ability(Player player,
                        PlayerConnect playerConnect,
                        DroneHolder droneHolder
    ) {
        String uuid = player.getUniqueId().toString();

        FileConfiguration particleFile = plugin.getFileUtils().particles;
        ArmorStand head = playerConnect.head;

        if (!particleFile.contains(droneName)) return;

        String particleType = particleFile.getString(droneName + ".particle");
        int size = particleFile.getInt(droneName + ".size");
        int amount = particleFile.getInt(droneName + ".amount");
        int r = particleFile.getInt(droneName + ".rgb.r");
        int g = particleFile.getInt(droneName + ".rgb.g");
        int b = particleFile.getInt(droneName + ".rgb.b");
        int delay = particleFile.getInt(droneName + ".delay");
        int yOffset = particleFile.getInt(droneName + ".y-offset");
        playerConnect.ability = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (cooldown.contains(uuid)) {
                return;
            }
            if (droneHolder.getAmmo() == 0) {
                if (!player.hasPermission("battledrones.bypass.ammo." + droneName.replace("_", "."))) {
                    return;
                }
            }
            plugin.getParticleManager().displayParticle(droneName, particleType, head.getLocation().add(0, yOffset, 0), r, g, b, size, amount);
        }, 0, delay).getTaskId();
    }

    @EventHandler
    public void onEntity(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;
        String uuid = player.getUniqueId().toString();
        PlayerConnect playerConnect = plugin.getPlayerConnect(uuid);

        if (!playerConnect.isActive()) return;
        if (!playerConnect.getActive().equalsIgnoreCase(droneName)) return;
        if (cooldown.contains(uuid)) return;

        DroneHolder droneHolder = plugin.getDroneHolder(uuid, droneName);

        boolean hasAmmo = droneHolder.getAmmo() > 0 || player.hasPermission("battledrones.bypass.ammo." + droneName);
        if (!hasAmmo) return;

        FileConfiguration file = plugin.droneFiles.get(droneName);

        cooldown.add(uuid);
        playerConnect.setHealing(false);

        String path = playerConnect.getGroup() + "." + droneHolder.getLevel() + ".";
        double finalDamage = e.getFinalDamage();
        double min = file.getDouble(path + "min");
        double max = file.getDouble(path + "max");
        double randomReduce = plugin.getCalculateManager().randomDouble(min, max);
        double reducedDamage = finalDamage - finalDamage * randomReduce;
        long maxAmmoSlots = file.getLong(path + "max-ammo-slots") * 64;
        e.setDamage(reducedDamage);

        String percent = String.valueOf(plugin.getCalculateManager().getProcentFromDouble(randomReduce));
        Entity damager = e.getDamager();

        if (damager instanceof Player) {
            particleEffect(file, path + "run.player", player, player.getName(), percent);
        } else if (plugin.getEntityManager().isMonster(damager)) {
            particleEffect(file, path + "run.monster", player, player.getName(), percent);
        }

        String particleName = droneName + "_ability";
        FileConfiguration particleFile = plugin.getFileUtils().particles;
        if (particleFile.contains(particleName)) {

            String particleType = particleFile.getString(particleName + ".particle");
            int size = particleFile.getInt(particleName + ".size");
            int amount = particleFile.getInt(particleName + ".amount");
            int r = particleFile.getInt(particleName + ".rgb.r");
            int g = particleFile.getInt(particleName + ".rgb.g");
            int b = particleFile.getInt(particleName + ".rgb.b");
            int delay = particleFile.getInt(particleName + ".delay");
            double yOffset = particleFile.getInt(particleName + ".y-offset");

            Location location = playerConnect.head.getLocation().add(0, yOffset, 0);

            plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                            plugin.getParticleManager().displayParticle(
                                    particleName,
                                    particleType,
                                    location,
                                    r,
                                    g,
                                    b,
                                    size,
                                    amount
                            ), delay);
        }

        plugin.getDroneManager().checkMessage(droneHolder.getAmmo(), maxAmmoSlots, player, "ammo");
        plugin.getDroneManager().takeAmmo(player, playerConnect, droneHolder, file, path);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            cooldown.remove(uuid);
            playerConnect.setHealing(true);
        }, file.getLong(path + "cooldown") * 20);
    }

    private void particleEffect(FileConfiguration file, String path, Player player, String targetName, String reduce) {
        for (String command : file.getStringList(path)) {
            plugin.getServer().dispatchCommand(plugin.consoleSender, plugin.getPlaceholderManager().replacePlaceholders(player, command).replace("{damage}", reduce).replace("{target}", targetName));
        }
    }
}
