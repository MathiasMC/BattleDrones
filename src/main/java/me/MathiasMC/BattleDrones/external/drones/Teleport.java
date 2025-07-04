package me.MathiasMC.BattleDrones.external.drones;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.api.DroneRegistry;
import me.MathiasMC.BattleDrones.api.Type;
import me.MathiasMC.BattleDrones.api.events.DroneDeathEvent;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.List;

public class Teleport extends DroneRegistry {

    private final BattleDrones plugin;

    public Teleport(BattleDrones plugin, String droneName, String droneCategory) {
        super(plugin, droneName, droneCategory);
        this.plugin = plugin;
    }

    @Override
    public void ability(Player player, PlayerConnect playerConnect, DroneHolder droneHolder) {

    }

    @Override
    public void find(Player player, PlayerConnect playerConnect, DroneHolder droneHolder) {

    }

    @Override
    public void follow(Player player, PlayerConnect playerConnect, DroneHolder droneHolder) {
        String uuid = player.getUniqueId().toString();
        String group = playerConnect.getGroup();
        String drone = droneHolder.getDrone();
        long level = droneHolder.getLevel();
        String path = group + "." + level;

        FileConfiguration file = plugin.droneFiles.get(drone);
        ArmorStand head = playerConnect.head;
        ArmorStand name = playerConnect.name;
        EulerAngle defaultPose = new EulerAngle(0, 0, 0);

        double xOffset = file.getDouble(path + ".position-x", 1.575);
        double yOffset = file.getDouble(path + ".position-y", 2);
        double zOffset = file.getDouble(path + ".position-z", 1.575);

        double closeRange = file.getDouble(path + ".follow-close-range");
        double closeSpeed = file.getDouble(path + ".follow-close-speed");
        double middleSpeed = file.getDouble(path + ".follow-middle-speed");
        double farRange = file.getDouble(path + ".follow-far-range");
        double farSpeed = file.getDouble(path + ".follow-far-speed");

        playerConnect.follow = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            LivingEntity target = plugin.drone_targets.get(uuid);
            World world = player.getWorld();
            Location baseLocation = player.getLocation().add(0, yOffset, 0);
            Vector direction = null;
            Location headLocation = head.getLocation();
            Location teleportTarget = headLocation;

            if (!plugin.park.contains(uuid)) {
                float yaw = baseLocation.getYaw();
                direction = baseLocation.getDirection();

                double x = Math.sin(-0.0175 * yaw + xOffset) + baseLocation.getX();
                double z = Math.cos(-0.0175 * yaw + zOffset) + baseLocation.getZ();
                teleportTarget = new Location(world, x, baseLocation.getY(), z);
            }

            if (target != null) {
                Location targetLocation = target.getLocation();

                if (!player.hasPermission("battledrones.bypass.ammo." + droneName)) {
                    int cost = file.getInt(path + ".ammo");
                    if (droneHolder.getAmmo() < cost) {
                        plugin.getDroneManager().runCommands(player, file, path + ".ammo-commands");
                        plugin.drone_targets.put(uuid, null);
                        return;
                    }
                }

                if (headLocation.distance(targetLocation) < file.getDouble(path + ".teleport")) {
                    plugin.drone_targets.put(uuid, null);

                    if (head.getLocation().getBlock().getType().isSolid()) {
                        plugin.getDroneManager().runCommands(player, file, path + ".teleport-cancelled");
                        return;
                    }

                    int ammoCost = file.getInt(path + ".ammo");
                    if (droneHolder.getAmmo() >= ammoCost) {
                        droneHolder.setAmmo(droneHolder.getAmmo() - ammoCost);
                    }

                    player.teleport(headLocation.add(0, 1, 0));
                    for (String command : file.getStringList(path + ".teleport-commands")) {
                        plugin.getServer().dispatchCommand(
                                plugin.consoleSender,
                                ChatColor.translateAlternateColorCodes('&',
                                        plugin.getPlaceholderManager()
                                                .replacePlaceholders(player, command.replace("{target}", target.getName()))
                                )
                        );
                    }
                    return;
                }

                Vector moveDirection = targetLocation.toVector().subtract(headLocation.toVector()).normalize();
                double distance = headLocation.distance(targetLocation);
                double speed = (distance < closeRange) ? closeSpeed : (distance > farRange ? farSpeed : middleSpeed);

                Vector newPosition = headLocation.toVector().add(moveDirection.multiply(speed));
                Location moveTo = newPosition.toLocation(world);

                plugin.getEntityManager().lookAT(head, targetLocation.clone());
                head.teleport(moveTo.setDirection(moveDirection));
                plugin.getEntityManager().setCustomName(head, name, level, group, file, "target", player);

            } else {
                head.setHeadPose(defaultPose);
                if (direction != null) {
                    head.teleport(teleportTarget.setDirection(direction));
                } else {
                    head.teleport(teleportTarget);
                }
                plugin.getEntityManager().setCustomName(head, name, level, group, file, "searching", player);
            }

            if (name != null) {
                name.teleport(head.getLocation().clone().add(0, 0.3, 0));
            }

            if (droneHolder.getWear() > 0) {
                droneHolder.setWear(droneHolder.getWear() - 1);
            } else {
                if (droneHolder.getHealth() - 1 >= 0) {
                    droneHolder.setWear(file.getInt(path + "wear-and-tear"));
                    droneHolder.setHealth(droneHolder.getHealth() - 1);
                } else {
                    DroneDeathEvent droneDeathEvent = new DroneDeathEvent(player, playerConnect, droneHolder);
                    droneDeathEvent.setType(Type.WEAR);
                    plugin.getServer().getPluginManager().callEvent(droneDeathEvent);
                    if (!droneDeathEvent.isCancelled()) {
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

                        dispatchCommands(file.getStringList("dead.wear"), player);
                    }
                    return;
                }
            }

            if (droneHolder.getHealth() - 1 > 0) {
                droneHolder.setHealth(droneHolder.getHealth() - 1);

                List<String> hitCommands = file.getStringList(path + ".hit-commands");
                dispatchCommands(hitCommands, player);

                long currentHealth = droneHolder.getHealth();
                long maxHealth = file.getLong(path + ".health");

                long percentLeft = (long) Math.floor(currentHealth * (100D / maxHealth));
                long previousPercent = (long) Math.floor((currentHealth + 1) * (100D / maxHealth));

                if ((percentLeft == previousPercent && percentLeft != 0L) || (percentLeft == 0L && currentHealth != 1)) {
                    return;
                }

                dispatchCommands(file.getStringList("low-health" + "." + percentLeft), player);
            }
        }, 5, 1).getTaskId();
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
