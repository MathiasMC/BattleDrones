package me.MathiasMC.BattleDrones.external.drones;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.api.DroneRegistry;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class Flamethrower extends DroneRegistry {

    private final BattleDrones plugin;

    public Flamethrower(BattleDrones plugin, String droneName, String droneCategory) {
        super(plugin, droneName, droneCategory);
        this.plugin = plugin;
    }

    @Override
    public void ability(Player player, PlayerConnect playerConnect, DroneHolder droneHolder) {
        String uuid = playerConnect.getUniqueId();
        String group = playerConnect.getGroup();
        FileConfiguration file = plugin.droneFiles.get(droneName);
        String path = group + "." + droneHolder.getLevel() + ".";
        ArmorStand head = playerConnect.head;

        FileConfiguration particleFile = plugin.getFileUtils().particles;
        String particleType = particleFile.getString(droneName + ".particle");
        int size = particleFile.getInt(droneName + ".size");
        int amount = particleFile.getInt(droneName + ".amount");
        int r = particleFile.getInt(droneName + ".rgb.r");
        int g = particleFile.getInt(droneName + ".rgb.g");
        int b = particleFile.getInt(droneName + ".rgb.b");
        int delay = particleFile.getInt(droneName + ".delay");
        double yOffset = particleFile.getDouble(droneName + ".y-offset");
        double space = particleFile.getDouble(droneName + ".space");
        double distance = particleFile.getDouble(droneName + ".distance");

        List<String> blockCheckList = plugin.getFileUtils().getBlockCheck(file, path);

        long cooldown = file.getLong(path + "cooldown");

        double projectileSpace = file.getDouble(path + "projectile-space");
        double damage = plugin.getCalculateManager().randomDouble(file.getDouble(path + "min"), file.getDouble(path + "max"));
        double radius = file.getDouble(path + "projectile-radius");
        double chance = file.getDouble(path + "setfire-chance");
        int burnTime = file.getInt(path + "burning-time");
        int projectileSpeed = file.getInt(path + "projectile-speed");
        int maxTicks = 8 * 20;

        playerConnect.ability = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            LivingEntity target = plugin.drone_targets.get(uuid);
            if (target == null) {
                playerConnect.setHealing(true);
                return;
            }

            playerConnect.setHealing(false);

            boolean hasAmmo = droneHolder.getAmmo() > 0 || player.hasPermission("battledrones.bypass.ammo." + droneName);
            if (!hasAmmo) return;

            Location headLocation = head.getLocation();
            Location targetLocation = target.getEyeLocation();

            boolean canSeeTarget = head.hasLineOfSight(target) && plugin.getEntityManager().hasBlockSight(headLocation, targetLocation, blockCheckList);

            if (!canSeeTarget) return;

            if (particleFile.contains(droneName)) {

                Location start = headLocation.add(0, 0.4, 0);

                World world = start.getWorld();
                if (world == null) return;

                new BukkitRunnable() {
                    final Vector p1 = start.toVector();
                    final Vector vector = targetLocation.toVector().clone().subtract(p1).normalize().multiply(projectileSpace);
                    int timer = 0;

                    @Override
                    public void run() {

                        Location location = p1.toLocation(world);

                        List<Entity> nearby = (List<Entity>) world.getNearbyEntities(location, radius, radius, radius);

                        if (nearby.contains(target)) {
                            if (plugin.getCalculateManager().randomChance() <= chance) {
                                target.setFireTicks(burnTime);
                            }
                            target.damage(damage);
                            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                                if (target.isDead()) {
                                    dispatchTargetCommands(target, player, location, path + "killed", file);
                                }
                            }, 2);
                            cancel();
                        }

                        if (timer > maxTicks) {
                            cancel();
                        }

                        if (droneName != null) {
                            location.add(0, yOffset, 0);
                            plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.getParticleManager().displayLineParticle(particleType, location, start, distance, space, r, g, b, amount, size), delay);
                        }

                        timer++;
                        p1.add(vector);
                    }
                }.runTaskTimer(plugin, 0, projectileSpeed);
            }

            // ADD LOGIC

            dispatchTargetCommands(target, player, headLocation, path + "ability", file);

        }, 0, cooldown).getTaskId();
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

    private void dispatchTargetCommands(Entity target, Player player, Location headLocation, String path, FileConfiguration file) {
        String type = null;
        if (target instanceof Player) {
            type = "player";
        } else if (plugin.getEntityManager().isMonster(target)) {
            type = "monster";
        } else if (plugin.getEntityManager().isAnimal(target)) {
            type = "animal";
        }

        if (type == null) return;

        String fullPath = path + "-commands-" + type;
        if (!file.contains(fullPath)) return;

        World worldObj = headLocation.getWorld();
        if (worldObj == null) return;

        String x = Integer.toString(headLocation.getBlockX());
        String y = Integer.toString(headLocation.getBlockY());
        String z = Integer.toString(headLocation.getBlockZ());
        String world = worldObj.getName();

        String targetName = target.getName();
        String translateKey = "translate." + targetName.toUpperCase().replace(" ", "_");

        if (plugin.getFileUtils().language.contains(translateKey)) {
            targetName = plugin.getFileUtils().language.getString(translateKey);
        }
        for (String command : file.getStringList(fullPath)) {
            plugin.getServer().dispatchCommand(
                    plugin.consoleSender,
                    command
                            .replace("{world}", world)
                            .replace("{x}", x)
                            .replace("{y}", y)
                            .replace("{z}", z)
                            .replace("{player}", player.getName())
                            .replace("{target}", targetName)
            );
        }
    }
}