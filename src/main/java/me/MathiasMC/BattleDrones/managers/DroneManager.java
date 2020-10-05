package me.MathiasMC.BattleDrones.managers;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Objects;

public class DroneManager {

    private final BattleDrones plugin;

    public DroneManager(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    public void checkMessage(final long amount, final long maxAmount, final String name, final String type) {
        final long left = Double.valueOf(Math.floor(amount * (100D / maxAmount))).longValue();
        if (left != 0L) {
            if (left != Double.valueOf(Math.floor((amount + 1) * (100D / maxAmount))).longValue()) {
                sendCheckMessage(left, name, type);
            }
        } else {
            if (amount == 1) {
                sendCheckMessage(left, name, type);
            }
        }
    }

    private void sendCheckMessage(final long left, final String name, final String type) {
        if (plugin.config.get.contains("low-" + type + "." + left)) {
            for (String command : plugin.config.get.getStringList("low-" + type + "." + left)) {
                plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", name));
            }
        }
    }

    public void checkShot(final Player player, final LivingEntity target, final FileConfiguration file, final Location location, final String path, final String type) {
        if (target instanceof Player) {
            shotCommands(file, path + type + ".player", location, player.getName(), target.getName());
        } else if (isMonster(target)) {
            shotCommands(file, path + type + ".monster", location, player.getName(), target.getName());
        } else if (isAnimal(target)) {
            shotCommands(file, path + type + ".animal", location, player.getName(), target.getName());
        }
    }

    public void takeAmmo(final PlayerConnect playerConnect, final DroneHolder droneHolder, final FileConfiguration file, final String path, final String name) {
        droneHolder.setAmmo(droneHolder.getAmmo() - 1);
        if (droneHolder.getLeft() > 0) {
            droneHolder.setLeft(droneHolder.getLeft() - 1);
        } else {
            if (droneHolder.getHealth() - 1 >= 0) {
                droneHolder.setLeft(file.getInt(path + "wear-and-tear"));
                droneHolder.setHealth(droneHolder.getHealth() - 1);
            } else {
                playerConnect.stopDrone();
                for (String command : plugin.config.get.getStringList("dead.wear")) {
                    plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", name));
                }
                droneHolder.setUnlocked(plugin.config.get.getInt("dead.unlocked"));
                if (plugin.config.get.getLong("dead.set-level") != 0) {
                    droneHolder.setLevel(plugin.config.get.getInt("dead.set-level"));
                }
                if (!plugin.config.get.getBoolean("dead.ammo")) {
                    droneHolder.setAmmo(0);
                }
            }
        }
    }

    private void shotCommands(final FileConfiguration laser, final String path, final Location location, final String player, String targetName) {
        final String x = String.valueOf(location.getBlockX());
        final String y = String.valueOf(location.getBlockY());
        final String z = String.valueOf(location.getBlockZ());
        final String world = Objects.requireNonNull(location.getWorld()).getName();
        final String translate = targetName.toUpperCase().replace(" ", "_");
        if (plugin.language.get.contains("translate." + translate)) {
            targetName = String.valueOf(plugin.language.get.getString("translate." + translate));
        }
        if (laser.contains(path)) {
            for (String command : laser.getStringList(path)) {
                plugin.getServer().dispatchCommand(plugin.consoleSender, command
                        .replace("{world}", world)
                        .replace("{x}", x)
                        .replace("{y}", y)
                        .replace("{z}", z)
                        .replace("{player}", player)
                        .replace("{target}", targetName));
            }
        }
    }

    public void waitSchedule(final String uuid, final FileConfiguration file) {
        plugin.drone_players.add(uuid);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.drone_players.remove(uuid), file.getInt("gui.WAIT-SECONDS") * 20);
    }

    public void wait(final Player player, final FileConfiguration file) {
        final Location location = player.getLocation();
        final String x = String.valueOf(location.getBlockX());
        final String y = String.valueOf(location.getBlockY());
        final String z = String.valueOf(location.getBlockZ());
        final String world = player.getWorld().getName();
        final String name = player.getName();
        for (String command : file.getStringList("gui.WAIT")) {
            plugin.getServer().dispatchCommand(plugin.consoleSender, command
                    .replace("{player}", name)
                    .replace("{world}", world)
                    .replace("{x}", x)
                    .replace("{y}", y)
                    .replace("{z}", z)
            );
        }
    }

    public void runCommands(final Player player, final PlayerConnect playerConnect, final FileConfiguration file, final String path, final boolean bypass) {
        if ((!playerConnect.hasActive() && path.equalsIgnoreCase("gui.SPAWN-COMMANDS")) || (playerConnect.hasActive() && path.equalsIgnoreCase("gui.REMOVE-COMMANDS")) || bypass) {
            final Location location = player.getLocation();
            final String x = String.valueOf(location.getBlockX());
            final String y = String.valueOf(location.getBlockY());
            final String z = String.valueOf(location.getBlockZ());
            final String world = player.getWorld().getName();
            final String name = player.getName();
            for (String command : file.getStringList(path)) {
                plugin.getServer().dispatchCommand(plugin.consoleSender, command
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

    public void regen(final PlayerConnect playerConnect, final DroneHolder droneHolder, final FileConfiguration file, final long drone_level) {
        final String group = playerConnect.getGroup();
        final String path = group + "." + drone_level + ".";
        final int health = file.getInt(path + "regen.health");
        if (file.getLong(path + "regen.delay") != 0) {
            playerConnect.RegenTaskID = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
                if (playerConnect.canRegen()) {
                    final int add_health = droneHolder.getHealth() + health;
                    if (file.getInt(path + "health") >= add_health) {
                        droneHolder.setHealth(add_health);
                    }
                }
            }, file.getLong(path + "regen.delay") * 20, file.getLong(path + "regen.delay") * 20).getTaskId();
        }
    }


    public void spawnDrone(final Player player, final String drone, final boolean bypass, final boolean bypassChecks) {
        if (plugin.support.inLocation(player, drone)) {
            final String uuid = player.getUniqueId().toString();
            final FileConfiguration file = plugin.droneFiles.get(drone);
            if (!plugin.drone_players.contains(player.getUniqueId().toString()) || player.hasPermission("battledrones.bypass.activate") || bypassChecks) {
                plugin.droneManager.waitSchedule(uuid, file);
                plugin.loadDroneHolder(uuid, drone);
                final DroneHolder droneHolder = plugin.getDroneHolder(uuid, drone);
                if (droneHolder.getUnlocked() == 1) {
                    final PlayerConnect playerConnect = plugin.get(uuid);
                    if (plugin.drone_amount.size() < plugin.config.get.getInt("drone-amount") || player.hasPermission("battledrones.bypass.drone-amount") || bypassChecks) {
                        plugin.droneManager.runCommands(player, playerConnect, file, "gui.SPAWN-COMMANDS", bypass);
                        playerConnect.stopDrone();
                        final String path = playerConnect.getGroup() + "." + droneHolder.getLevel() + ".";
                        ItemStack itemStack = plugin.drone_heads.get(file.getString(path + "head"));
                        if (file.contains(path + "model-data")) {
                            itemStack = new ItemStack(Material.STICK);
                            final ItemMeta itemMeta = itemStack.getItemMeta();
                            if (itemMeta != null) {
                                itemMeta.setCustomModelData(file.getInt(path + "model-data"));
                                itemStack.setItemMeta(itemMeta);
                            }
                        }
                        boolean hasName = false;
                        if (Objects.requireNonNull(file.getString(path + "messages.name.searching")).length() > 0 || Objects.requireNonNull(file.getString(path + "messages.name.target")).length() > 0) {
                            hasName = true;
                        }
                        playerConnect.spawn(player, itemStack, hasName);
                        startAI(player, playerConnect, droneHolder, file, drone);
                        if (drone.equalsIgnoreCase("laser")) {
                            plugin.gun.shot(player, "laser");
                        } else if (drone.equalsIgnoreCase("rocket")) {
                            plugin.rocket.shot(player, "rocket", false, false);
                        } else if (drone.equalsIgnoreCase("machine_gun")) {
                            plugin.gun.shot(player, "machine_gun");
                        } else if (drone.equalsIgnoreCase("shield_generator")) {
                            plugin.shieldGenerator.shot(player);
                        } else if (drone.equalsIgnoreCase("healing")) {
                            plugin.healing.shot(player);
                        } else if (drone.equalsIgnoreCase("flamethrower")) {
                            plugin.flamethrower.shot(player);
                        } else if (drone.equalsIgnoreCase("faf_missile")) {
                            plugin.rocket.shot(player, "faf_missile", true, false);
                        } else if (drone.equalsIgnoreCase("mortar")) {
                            plugin.rocket.shot(player, "mortar", false, true);
                        } else if (drone.equalsIgnoreCase("lightning")) {
                            plugin.lightning.shot(player);
                        }
                        playerConnect.setActive(drone);
                        playerConnect.setRegen(true);
                        plugin.droneManager.regen(playerConnect, droneHolder, file, droneHolder.getLevel());
                    } else {
                        plugin.droneManager.runCommands(player, playerConnect, BattleDrones.call.language.get, "gui.drone.amount-reached", true);
                    }
                } else {
                    for (String message : plugin.language.get.getStringList("activate.unlocked")) {
                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", player.getName())));
                    }
                }
            } else {
                plugin.droneManager.wait(player, file);
            }
        }
    }

    public void startAI(final Player player, final PlayerConnect playerConnect, final DroneHolder droneHolder, final FileConfiguration file, final String drone) {
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
    }

    public boolean isMonster(final Entity entity) {
        return entity instanceof Monster
                || entity instanceof Slime
                || entity instanceof Phantom
                || entity instanceof IronGolem
                || entity instanceof Ghast
                || entity instanceof Shulker;
    }

    public boolean isAnimal(final Entity entity) {
        return entity instanceof org.bukkit.entity.Animals
                || entity instanceof Villager
                || entity instanceof WanderingTrader
                || entity instanceof Dolphin
                || entity instanceof PufferFish
                || entity instanceof Squid
                || entity instanceof TropicalFish
                || entity instanceof Bat
                || entity instanceof Cod
                || entity instanceof Salmon;
    }
}