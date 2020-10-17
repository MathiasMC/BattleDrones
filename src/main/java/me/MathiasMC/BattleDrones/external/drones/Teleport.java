package me.MathiasMC.BattleDrones.external.drones;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.api.DroneRegistry;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

public class Teleport extends DroneRegistry {

    private final BattleDrones plugin;

    public Teleport(BattleDrones plugin, String droneName, String droneCategory) {
        super(plugin, droneName, droneCategory);
        this.plugin = plugin;
    }

    @Override
    public void find(Player player, PlayerConnect playerConnect, DroneHolder droneHolder) {

    }

    @Override
    public void follow(Player player, PlayerConnect playerConnect, DroneHolder droneHolder) {
        final String uuid = player.getUniqueId().toString();
        final FileConfiguration file = plugin.droneFiles.get(droneHolder.getDrone());
        final long drone_level = droneHolder.getLevel();
        final String group = playerConnect.getGroup();
        final ArmorStand head = playerConnect.head;
        final ArmorStand name = playerConnect.name;
        final EulerAngle eulerAngle = new EulerAngle(0, 0, 0);
        final String path = group + "." + drone_level;
        final double xDCustom = plugin.getFileUtils().getDouble(file, path + ".position.x", 1.575);
        final double yDCustom = plugin.getFileUtils().getDouble(file, path + ".position.y", 2);
        final double zDCustom = plugin.getFileUtils().getDouble(file, path + ".position.z", 1.575);
        final double closeRange = plugin.getFileUtils().getFollow(file, path, "follow.close.range");
        final double closeSpeed = plugin.getFileUtils().getFollow(file, path, "follow.close.speed");
        final double middleSpeed = plugin.getFileUtils().getFollow(file, path, "follow.middle.speed");
        final double farRange = plugin.getFileUtils().getFollow(file, path, "follow.far.range");
        final double farSpeed = plugin.getFileUtils().getFollow(file, path, "follow.far.speed");
        playerConnect.follow = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            LivingEntity target = plugin.drone_targets.get(uuid);
            Location tp = head.getLocation();
            final World world = player.getWorld();
            final Location location = player.getLocation().add(0, yDCustom, 0);
            Vector direction = null;
            if (!plugin.park.contains(uuid)) {
                float yaw = location.getYaw();
                direction = location.getDirection();
                final double xD = Math.sin(-0.0175 * yaw + xDCustom) + location.getX();
                final double zD = Math.cos(-0.0175 * yaw + zDCustom) + location.getZ();
                tp = new Location(world, xD, location.getY(), zD);
            }
            if (target != null) {
                final Location headLocation = head.getLocation();
                final Vector tpVector = headLocation.toVector();
                final Location targetLocation = target.getLocation();
                if (!player.hasPermission("battledrones.bypass.ammo." + droneName)) {
                    if (droneHolder.getAmmo() < file.getInt(path + ".ammo")) {
                        plugin.getDroneManager().runCommands(player, file.getStringList(path + ".ammo-commands"));
                        plugin.drone_targets.put(uuid, null);
                        return;
                    }
                }
                if (headLocation.distance(targetLocation) < file.getDouble(path + ".teleport")) {
                    plugin.drone_targets.put(uuid, null);
                    if (head.getLocation().getBlock().getType().isSolid()) {
                        plugin.getDroneManager().runCommands(player, file.getStringList(path + ".teleport-cancelled"));
                        return;
                    }
                    int set = droneHolder.getAmmo() - file.getInt(path + ".ammo");
                    if (set >= 0) {
                        droneHolder.setAmmo(set);
                    }
                    player.teleport(headLocation.add(0, 1, 0));
                    for (String command : file.getStringList(path + ".teleport-commands")) {
                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', plugin.getPlaceholderManager().replacePlaceholders(player, command.replace("{target}", target.getName()))));
                    }
                }
                final double distance = targetLocation.distance(headLocation);
                if (distance < closeRange) {
                    tpVector.add(targetLocation.toVector().subtract(tpVector).normalize().multiply(closeSpeed));
                } else if (distance > farRange) {
                    tpVector.add(targetLocation.toVector().subtract(tpVector).normalize().multiply(farSpeed));
                } else {
                    tpVector.add(targetLocation.toVector().subtract(tpVector).normalize().multiply(middleSpeed));
                }
                tp = tpVector.toLocation(world);
                plugin.getEntityManager().lookAT(head, targetLocation.clone());
                head.teleport(tp.setDirection(targetLocation.toVector().subtract(tpVector).normalize()));
                plugin.getEntityManager().setCustomName(head, name, drone_level, group, file, "target", player);
            } else {
                head.setHeadPose(eulerAngle);
                if (direction != null) {
                    head.teleport(tp.setDirection(direction));
                } else {
                    head.teleport(tp);
                }
                plugin.getEntityManager().setCustomName(head, name, drone_level, group, file, "searching", player);
            }
            if (name == null) {
                return;
            }
            name.teleport(tp.add(0, 0.3, 0));
        }, 5, 1).getTaskId();
    }
}
