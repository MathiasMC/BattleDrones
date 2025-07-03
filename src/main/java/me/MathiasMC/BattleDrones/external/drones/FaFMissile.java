package me.MathiasMC.BattleDrones.external.drones;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.api.DroneRegistry;
import me.MathiasMC.BattleDrones.api.Type;
import me.MathiasMC.BattleDrones.api.events.DroneDeathEvent;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class FaFMissile extends DroneRegistry {

    private final BattleDrones plugin;

    public FaFMissile(BattleDrones plugin, String droneName, String droneCategory) {
        super(plugin, droneName, droneCategory);
        this.plugin = plugin;
    }

    @Override
    public void ability(Player player, PlayerConnect playerConnect, DroneHolder droneHolder) {
        String uuid = player.getUniqueId().toString();
        String group = playerConnect.getGroup();
        FileConfiguration file = plugin.droneFiles.get(droneName);
        String path = group + "." + droneHolder.getLevel() + ".";
        ArmorStand head = playerConnect.head;

        FileConfiguration particleFile = plugin.getFileUtils().particles;
        String customParticle_2 = droneName + "_ability_1";
        String customParticle_3 = droneName + "_ability_2";

        String particleType_1 = particleFile.getString(droneName + ".particle");
        String particleType_2 = particleFile.getString(customParticle_2 + ".particle");
        String particleType_3 = particleFile.getString(customParticle_3 + ".particle");

        int size_1 = particleFile.getInt(droneName + ".size");
        int size_2 = particleFile.getInt(customParticle_2 + ".size");
        int size_3 = particleFile.getInt(customParticle_3 + ".size");

        int amount_1 = particleFile.getInt(droneName + ".amount");
        int amount_2 = particleFile.getInt(customParticle_2 + ".amount");
        int amount_3 = particleFile.getInt(customParticle_3 + ".amount");

        int r_1 = particleFile.getInt(droneName + ".rgb.r");
        int r_2 = particleFile.getInt(customParticle_2 + ".rgb.r");
        int r_3 = particleFile.getInt(customParticle_3 + ".rgb.r");


        int g_1 = particleFile.getInt(droneName + ".rgb.g");
        int g_2 = particleFile.getInt(customParticle_2 + ".rgb.g");
        int g_3 = particleFile.getInt(customParticle_3 + ".rgb.g");

        int b_1 = particleFile.getInt(droneName + ".rgb.b");
        int b_2 = particleFile.getInt(customParticle_2 + ".rgb.b");
        int b_3 = particleFile.getInt(customParticle_3 + ".rgb.b");

        int delay_1 = particleFile.getInt(droneName + ".delay");
        int delay_2 = particleFile.getInt(customParticle_2 + ".delay");
        int delay_3 = particleFile.getInt(customParticle_3 + ".delay");

        double yOffset_1 = particleFile.getDouble(droneName + ".y-offset");
        double yOffset_2 = particleFile.getDouble(customParticle_2 + ".y-offset");
        double yOffset_3 = particleFile.getDouble(customParticle_3 + ".y-offset");

        int tick_2 = particleFile.getInt(customParticle_2 + ".tick");
        int tick_3 = particleFile.getInt(customParticle_3 + ".tick");

        List<String> blockCheckList = plugin.getFileUtils().getBlockCheck(file, path);

        long cooldown = file.getLong(path + "cooldown");

        String rocketHead = file.getString(path + "rocket-head");

        double rocketSpeed = file.getDouble(path + "rocket-speed");
        double rocketPower = file.getDouble(path + "rocket-explosion-power");

        boolean rocketExplosion = file.getBoolean(path + "rocket-explosion");
        boolean rocketFire = file.getBoolean(path + "rocket-explosion-fire");
        boolean rocketBlock = file.getBoolean(path + "rocket-explosion-block");
        boolean rocketDestruction = file.getBoolean(path + "rocket-self-destruction");

        long rocketTime = file.getLong(path + "rocket-time") * 20;

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
            Location targetLocation = target.getLocation();

            boolean canSeeTarget = head.hasLineOfSight(target) && plugin.getEntityManager().hasBlockSight(headLocation, targetLocation, blockCheckList);

            if (!canSeeTarget) return;

            ArmorStand missile = plugin.getEntityManager().getArmorStand(head.getLocation());
            missile.getPersistentDataContainer().set(plugin.projectileKey, PersistentDataType.STRING, uuid);
            if (file.contains(path + "rocket-head")) {
                final EntityEquipment equipment = missile.getEquipment();
                if (equipment != null) {
                    equipment.setHelmet(plugin.drone_heads.get(rocketHead));
                }
            }
            plugin.projectiles.add(missile);

            missile.setHeadPose(head.getHeadPose());

            new BukkitRunnable() {
                final Vector p1 = missile.getLocation().toVector();
                Vector vector = targetLocation.toVector().clone().subtract(p1).normalize().multiply(rocketSpeed);
                int timer = 0;
                int particle1 = 0;
                int particle2 = 0;
                final ArrayList<String> exclude = new ArrayList<>();
                final World world = missile.getWorld();
                final Set<Material> passable = Set.of(Material.AIR, Material.WATER, Material.LAVA, Material.GRASS_BLOCK, Material.TALL_GRASS);

                @Override
                public void run() {
                    Location targetLocation = target.getLocation();
                    Location missileLocation = missile.getLocation();

                    vector = targetLocation.toVector().clone().subtract(p1).normalize().multiply(rocketSpeed);
                    plugin.getEntityManager().lookAT(missile, targetLocation);
                    Vector direction = target.getLocation().toVector().subtract(missileLocation.toVector()).normalize();
                    missile.teleport(p1.toLocation(world).setDirection(direction));

                    p1.add(vector);
                    Location currentMissileLocation = missile.getLocation();
                    ArrayList<LivingEntity> hits = plugin.getEntityManager().getLivingEntitiesAround(missile, 1, 1, 1, 1, exclude, exclude, false);
                    hits.remove(player);
                    Material hitType = missile.getTargetBlock(null, 1).getType();

                    boolean shouldExplode = timer > rocketTime
                            || !hits.isEmpty()
                            || !passable.contains(hitType)
                            || (rocketDestruction && target.isDead());

                    if (shouldExplode) {
                        this.cancel();

                        if (rocketExplosion) {
                            world.createExplosion(
                                    currentMissileLocation.getX(),
                                    currentMissileLocation.getY(),
                                    currentMissileLocation.getZ(),
                                    (float) rocketPower,
                                    rocketFire,
                                    rocketBlock);
                        }

                        dispatchTargetCommands(target, player, currentMissileLocation, path + "explode", file);

                        if (particleFile.contains(droneName)) {
                            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                                Location location = currentMissileLocation.add(0, yOffset_1, 0);
                                plugin.getParticleManager().displayParticle(droneName, particleType_1, location, r_1, g_1, b_1, size_1, amount_1);
                            }, delay_1);
                        }

                        missile.remove();
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                            if (target.isDead()) {
                                dispatchTargetCommands(target, player, currentMissileLocation, path + "killed", file);
                            }
                        }, 2);
                    }

                    if (particleFile.contains(customParticle_2)) {
                        if (++particle1 > tick_2) {
                            final Location location = currentMissileLocation.clone().add(0, yOffset_2, 0);
                            plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                                    plugin.getParticleManager().displayParticle(customParticle_2, particleType_2, location, r_2, g_2, b_2, size_2, amount_2), delay_2);
                            particle1 = 1;
                        }
                    }

                    if (particleFile.contains(customParticle_3)) {
                        if (++particle2 > tick_3) {
                            final Location location = currentMissileLocation.clone().add(0, yOffset_3, 0);
                            plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                                    plugin.getParticleManager().displayParticle(customParticle_3, particleType_3, location, r_3, g_3, b_3, size_3, amount_3), delay_3);
                            particle2 = 1;
                        }
                    }

                    timer++;
                }
            }.runTaskTimer(plugin, 0, 1);

            droneHolder.setAmmo(droneHolder.getAmmo() - 1);

            dispatchTargetCommands(target, player, headLocation, path + "ability", file);


            if (droneHolder.getWear() > 0) {
                droneHolder.setWear(droneHolder.getWear() - 1);
            } else {
                if (droneHolder.getHealth() - 1 >= 0) {
                    droneHolder.setWear(file.getInt(path + "wear-and-tear"));
                    droneHolder.setHealth(droneHolder.getHealth() - 1);
                } else {
                    List<String> wearDeathCommands = file.getStringList("dead.wear");
                    dispatchCommands(wearDeathCommands, player);

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

                List<String> lowHealthCommands = file.getStringList("low-" + "health" + "." + percentLeft);
                dispatchCommands(lowHealthCommands, player);

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