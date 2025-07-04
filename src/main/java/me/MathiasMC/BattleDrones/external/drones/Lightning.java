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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Lightning extends DroneRegistry {

    private final BattleDrones plugin;

    public Lightning(BattleDrones plugin,
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
        String group = playerConnect.getGroup();
        FileConfiguration file = plugin.droneFiles.get(droneName);
        String path = group + "." + droneHolder.getLevel() + ".";
        ArmorStand head = playerConnect.head;

        List<String> blockCheckList = plugin.getFileUtils().getBlockCheck(file, path);

        long cooldown = file.getLong(path + "cooldown");

        double chance = file.getDouble(path + "chance");
        int burnTime = file.getInt(path + "burning-time");
        double explosionFireChance = file.getDouble(path + "setfire-chance");
        double explosionChance = file.getDouble(path + "explosion-chance");
        double explosionPower = file.getDouble(path + "explosion-power");
        boolean explosionFire = file.getBoolean(path + "explosion-fire");
        boolean explosionBlock = file.getBoolean(path + "explosion-block");

        int tickDead = file.getInt(path + "tick-dead");

        double minDamage = file.getDouble(path + "min");
        double maxDamage = file.getDouble(path + "max");

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

            World world = targetLocation.getWorld();
            if (world == null) return;

            world.strikeLightningEffect(targetLocation);
            ArrayList<String> exclude = new ArrayList<>();
            ArrayList<LivingEntity> livingEntities = plugin.getEntityManager().getLivingEntitiesAround(target, file.getDouble(path + "radius"),  1, 1, 1, exclude, exclude, false);
            double random = plugin.getCalculateManager().randomDouble(minDamage, maxDamage);
            for (LivingEntity livingEntity : livingEntities) {
                plugin.getCalculateManager().damage(livingEntity, random);
            }
            plugin.getCalculateManager().damage(target, random);
            if (plugin.getCalculateManager().randomChance() <= explosionFireChance) {
                target.setFireTicks(burnTime);
            }
            if (file.getBoolean(path + "explosion")) {
                if (plugin.getCalculateManager().randomChance() <= explosionChance) {
                    world.createExplosion(targetLocation.getX(), targetLocation.getY(), targetLocation.getZ(), (float) explosionPower, explosionFire, explosionBlock);
                }
            }
            if (plugin.getCalculateManager().randomChance() <= chance && target instanceof Player) {
                if (file.contains(path + "chance-commands")) {
                    for (String command : file.getStringList(path + "chance-commands")) {
                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', command.replace("{player}", target.getName())));
                    }
                }
            }

            dispatchTargetCommands(target, player, headLocation, path + "ability", file);

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (target.isDead()) {
                    dispatchTargetCommands(target, player, targetLocation, path + "killed", file);
                }
            }, tickDead);

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
