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
        }, 5, 1).getTaskId();
    }
}
