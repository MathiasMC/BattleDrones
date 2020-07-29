package me.MathiasMC.BattleDrones.managers;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;

public class DroneManager {

    private final BattleDrones plugin;

    public DroneManager(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    public void checkAmmo(FileConfiguration file, String path, long ammo, String name) {
        final int maxAmmo = file.getInt(path + "max-ammo-slots") * 64;
        final long ammoLeft = Double.valueOf(Math.floor(ammo * (100D / maxAmmo))).longValue();
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
        final String x = String.valueOf(location.getBlockX());
        final String y = String.valueOf(location.getBlockY());
        final String z = String.valueOf(location.getBlockZ());
        final String world = location.getWorld().getName();
        for (String command : laser.getStringList(path)) {
            BattleDrones.call.getServer().dispatchCommand(BattleDrones.call.consoleSender, command
                    .replace("{world}", world)
                    .replace("{x}", x)
                    .replace("{y}", y)
                    .replace("{z}", z)
                    .replace("{target}", targetName));
        }
    }

    public void waitSchedule(String uuid, FileConfiguration file) {
        BattleDrones.call.drone_players.add(uuid);
        BattleDrones.call.getServer().getScheduler().runTaskLater(BattleDrones.call, () -> {
            BattleDrones.call.drone_players.remove(uuid);
        }, file.getInt("gui.WAIT-SECONDS") * 20);
    }

    public void wait(Player player, FileConfiguration file) {
        final Location location = player.getLocation();
        final String x = String.valueOf(location.getBlockX());
        final String y = String.valueOf(location.getBlockY());
        final String z = String.valueOf(location.getBlockZ());
        final String world = player.getWorld().getName();
        final String name = player.getName();
        for (String command : file.getStringList("gui.WAIT")) {
            BattleDrones.call.getServer().dispatchCommand(BattleDrones.call.consoleSender, command
                    .replace("{player}", name)
                    .replace("{world}", world)
                    .replace("{x}", x)
                    .replace("{y}", y)
                    .replace("{z}", z)
            );
        }
    }

    public void runCommands(Player player, PlayerConnect playerConnect, FileConfiguration file, String path, boolean bypass) {
        if ((!playerConnect.hasActive() && path.equalsIgnoreCase("gui.SPAWN-COMMANDS")) || (playerConnect.hasActive() && path.equalsIgnoreCase("gui.REMOVE-COMMANDS")) || bypass) {
            final Location location = player.getLocation();
            final String x = String.valueOf(location.getBlockX());
            final String y = String.valueOf(location.getBlockY());
            final String z = String.valueOf(location.getBlockZ());
            final String world = player.getWorld().getName();
            final String name = player.getName();
            for (String command : file.getStringList(path)) {
                BattleDrones.call.getServer().dispatchCommand(BattleDrones.call.consoleSender, command
                        .replace("{player}", name)
                        .replace("{world}", world)
                        .replace("{x}", x)
                        .replace("{y}", y)
                        .replace("{z}", z)
                );
            }
        }
    }

    public void cleanUP() {
        final ArrayList<ArmorStand> armorStands = new ArrayList<>();
        for (World world : plugin.getServer().getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof ArmorStand) {
                    final ArmorStand armorStand = (ArmorStand) entity;
                    final String key = armorStand.getPersistentDataContainer().get(new NamespacedKey(plugin, "drone_uuid"), PersistentDataType.STRING);
                    if (key != null) {
                        armorStands.add(armorStand);
                        armorStand.remove();
                    }
                }
            }
        }
        plugin.textUtils.info("CleanUP found: ( " + armorStands.size() + " ) drones removed.");
        armorStands.clear();
    }

    public void regen(PlayerConnect playerConnect, DroneHolder droneHolder, FileConfiguration file, long drone_level) {
        final String group = playerConnect.getGroup();
        final String path = group + "." + drone_level + ".";
        final int health = file.getInt(path + "regen.health");
        if (file.getLong(path + "regen.delay") != 0) {
            playerConnect.RegenTaskID = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
                if (playerConnect.canRegen()) {
                    int add_health = droneHolder.getHealth() + health;
                    if (file.getInt(path + "health") >= add_health) {
                        droneHolder.setHealth(add_health);
                    }
                }
            }, file.getLong(path + "regen.delay") * 20, file.getLong(path + "regen.delay") * 20).getTaskId();
        }
    }


    public void spawnDrone(Player player, String drone, boolean bypass, boolean bypassChecks) {
        String uuid = player.getUniqueId().toString();
        FileConfiguration file = plugin.droneFiles.get(drone);
        if (!plugin.drone_players.contains(player.getUniqueId().toString()) || player.hasPermission("battledrones.bypass.activate") || bypassChecks) {
            plugin.droneManager.waitSchedule(uuid, file);
            plugin.loadDroneHolder(uuid, drone);
            DroneHolder droneHolder = plugin.getDroneHolder(uuid, drone);
            if (droneHolder.getUnlocked() == 1) {
                PlayerConnect playerConnect = plugin.get(uuid);
                if (plugin.drone_amount.size() < plugin.config.get.getInt("drone-amount") || player.hasPermission("battledrones.bypass.drone-amount") || bypassChecks) {
                    plugin.droneManager.runCommands(player, playerConnect, file, "gui.SPAWN-COMMANDS", bypass);
                    playerConnect.stopDrone();
                    playerConnect.spawn(player, file.getString(playerConnect.getGroup() + "." + droneHolder.getLevel() + ".head"));
                    if (drone.equalsIgnoreCase("shield_generator")) {
                        plugin.aiManager.defaultAI(player,
                                playerConnect,
                                file,
                                droneHolder.getLevel(),
                                droneHolder.getMonsters(),
                                0,
                                droneHolder.getPlayers(),
                                droneHolder.getExclude(),
                                false, false, true);
                    } else if (drone.equalsIgnoreCase("healing")) {
                        plugin.aiManager.defaultAI(player,
                                playerConnect,
                                file,
                                droneHolder.getLevel(),
                                droneHolder.getMonsters(),
                                droneHolder.getAnimals(),
                                1,
                                droneHolder.getExclude(),
                                true, true, true);
                    } else {
                        plugin.aiManager.defaultAI(player,
                                playerConnect,
                                file,
                                droneHolder.getLevel(),
                                droneHolder.getMonsters(),
                                droneHolder.getAnimals(),
                                droneHolder.getPlayers(),
                                droneHolder.getExclude(),
                                false, false, true);
                    }
                    if (drone.equalsIgnoreCase("laser")) {
                        plugin.laser.shot(player);
                    } else if (drone.equalsIgnoreCase("rocket")) {
                        plugin.rocket.shot(player);
                    } else if (drone.equalsIgnoreCase("machine_gun")) {
                        plugin.machineGun.shot(player);
                    } else if (drone.equalsIgnoreCase("shield_generator")) {
                        plugin.shieldGenerator.shot(player);
                    } else if (drone.equalsIgnoreCase("healing")) {
                        plugin.healing.shot(player);
                    } else if (drone.equalsIgnoreCase("flamethrower")) {
                        plugin.flamethrower.shot(player);
                    }
                    playerConnect.setActive(drone);
                    playerConnect.setRegen(true);
                    plugin.droneManager.regen(playerConnect, droneHolder, file, droneHolder.getLevel());
                } else {
                    plugin.droneManager.runCommands(player, playerConnect, BattleDrones.call.language.get, "gui.drone.amount-reached", true);
                }
            } else {
                for (String message : plugin.language.get.getStringList("battledrones.activate.unlocked")) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                }
            }
        } else {
            plugin.droneManager.wait(player, file);
        }
    }
}