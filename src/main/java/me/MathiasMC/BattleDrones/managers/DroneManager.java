package me.MathiasMC.BattleDrones.managers;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;

public class DroneManager {

    private final BattleDrones plugin;

    public DroneManager(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    public void checkAmmo(FileConfiguration file, String path, long ammo, String name) {
        int maxAmmo = file.getInt(path + "max-ammo-slots") * 64;
        long ammoLeft = Double.valueOf(Math.floor(ammo * (100D / maxAmmo))).longValue();
        if (ammoLeft != 0L) {
            if (ammoLeft != Double.valueOf(Math.floor((ammo + 1) * (100D / maxAmmo))).longValue()) {
                ammoMessage(ammoLeft, name);
            }
        } else {
            if (ammo == 1) {
                ammoMessage(ammoLeft, name);
            }
        }
    }

    private void ammoMessage(long ammoLeftProcent, String name) {
        if (BattleDrones.call.config.get.contains("low-ammo." + ammoLeftProcent)) {
            for (String command : BattleDrones.call.config.get.getStringList("low-ammo." + ammoLeftProcent)) {
                BattleDrones.call.getServer().dispatchCommand(BattleDrones.call.consoleSender, command.replace("{player}", name));
            }
        }
    }

    public void checkShot(LivingEntity target, FileConfiguration file, Location location, String path, String type) {
        if (target instanceof Player) {
            shotCommands(file, path + type + ".player", location, target.getName());
        } else if (target instanceof Monster) {
            shotCommands(file, path + type + ".monster", location, target.getName());
        } else if (target instanceof Animals) {
            shotCommands(file, path + type + ".animal", location, target.getName());
        }
    }

    public void takeAmmo(PlayerConnect playerConnect, DroneHolder droneHolder, FileConfiguration file, String path, String name) {
        droneHolder.setAmmo(droneHolder.getAmmo() - 1);
        if (droneHolder.getLeft() > 0) {
            droneHolder.setLeft(droneHolder.getLeft() - 1);
        } else {
            if (droneHolder.getHealth() - 1 >= 0) {
                droneHolder.setLeft(file.getInt(path + "wear-and-tear"));
                droneHolder.setHealth(droneHolder.getHealth() - 1);
            } else {
                playerConnect.stopDrone();
                for (String command : BattleDrones.call.config.get.getStringList("dead.wear")) {
                    BattleDrones.call.getServer().dispatchCommand(BattleDrones.call.consoleSender, command.replace("{player}", name));
                }
                droneHolder.setUnlocked(BattleDrones.call.config.get.getInt("dead.unlocked"));
                if (BattleDrones.call.config.get.getLong("dead.set-level") != 0) {
                    droneHolder.setLevel(BattleDrones.call.config.get.getInt("dead.set-level"));
                }
                if (!BattleDrones.call.config.get.getBoolean("dead.ammo")) {
                    droneHolder.setAmmo(0);
                }
            }
        }
    }

    private void shotCommands(FileConfiguration laser, String path, Location location, String targetName) {
        for (String command : laser.getStringList(path)) {
            BattleDrones.call.getServer().dispatchCommand(BattleDrones.call.consoleSender, command
                    .replace("{world}", location.getWorld().getName())
                    .replace("{x}", String.valueOf(location.getBlockX()))
                    .replace("{y}", String.valueOf(location.getBlockY()))
                    .replace("{z}", String.valueOf(location.getBlockZ())
                            .replace("{target}", targetName)));
        }
    }

    public void waitSchedule(String uuid, FileConfiguration file) {
        BattleDrones.call.drone_players.add(uuid);
        BattleDrones.call.getServer().getScheduler().runTaskLater(BattleDrones.call, () -> {
            BattleDrones.call.drone_players.remove(uuid);
        }, file.getInt("gui.WAIT-SECONDS") * 20);
    }

    public void wait(Player player, FileConfiguration file) {
        Location location = player.getLocation();
        for (String command : file.getStringList("gui.WAIT")) {
            BattleDrones.call.getServer().dispatchCommand(BattleDrones.call.consoleSender, command
                    .replace("{player}", player.getName())
                    .replace("{world}", player.getWorld().getName())
                    .replace("{x}", String.valueOf(location.getBlockX()))
                    .replace("{y}", String.valueOf(location.getBlockY()))
                    .replace("{z}", String.valueOf(location.getBlockZ()))
            );
        }
    }

    public void spawnCommands(Player player, PlayerConnect playerConnect, FileConfiguration file) {
        if (!playerConnect.hasActive()) {
            Location location = player.getLocation();
            for (String command : file.getStringList("gui.SPAWN-COMMANDS")) {
                BattleDrones.call.getServer().dispatchCommand(BattleDrones.call.consoleSender, command
                        .replace("{player}", player.getName())
                        .replace("{world}", player.getWorld().getName())
                        .replace("{x}", String.valueOf(location.getBlockX()))
                        .replace("{y}", String.valueOf(location.getBlockY()))
                        .replace("{z}", String.valueOf(location.getBlockZ()))
                );
            }
        }
    }

    public void removeCommands(Player player, PlayerConnect playerConnect, FileConfiguration file) {
        if (playerConnect.hasActive()) {
            Location location = player.getLocation();
            for (String command : file.getStringList("gui.REMOVE-COMMANDS")) {
                BattleDrones.call.getServer().dispatchCommand(BattleDrones.call.consoleSender, command
                        .replace("{player}", player.getName())
                        .replace("{world}", player.getWorld().getName())
                        .replace("{x}", String.valueOf(location.getBlockX()))
                        .replace("{y}", String.valueOf(location.getBlockY()))
                        .replace("{z}", String.valueOf(location.getBlockZ()))
                );
            }
        }
    }

    public void regen(PlayerConnect playerConnect, DroneHolder droneHolder, FileConfiguration file, final String type, long drone_level) {
        final String group = playerConnect.getGroup();
        final String path = group + "." + drone_level + ".";
        if (file.getLong(path + "regen.delay") != 0) {
            playerConnect.RegenTaskID = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
                if (playerConnect.canRegen()) {
                    int add_health = droneHolder.getHealth() + file.getInt(path + "regen.health");
                    if (file.getInt(path + "health") >= add_health) {
                        droneHolder.setHealth(add_health);
                    }
                }
            }, file.getLong(path + "regen.delay") * 20, file.getLong(path + "regen.delay") * 20).getTaskId();
        }
    }
}