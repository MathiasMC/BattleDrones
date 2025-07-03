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
import org.bukkit.util.EulerAngle;
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

        String customParticle2 = droneName + "_ability_1";
        String customParticle3 = droneName + "_ability_2";

        String particleType1 = particleFile.getString(droneName + ".particle");
        String particleType2 = particleFile.getString(customParticle2 + ".particle");
        String particleType3 = particleFile.getString(customParticle3 + ".particle");

        int size1 = particleFile.getInt(droneName + ".size");
        int size2 = particleFile.getInt(customParticle2 + ".size");
        int size3 = particleFile.getInt(customParticle3 + ".size");

        int amount1 = particleFile.getInt(droneName + ".amount");
        int amount2 = particleFile.getInt(customParticle2 + ".amount");
        int amount3 = particleFile.getInt(customParticle3 + ".amount");

        int r1 = particleFile.getInt(droneName + ".rgb.r");
        int r2 = particleFile.getInt(customParticle2 + ".rgb.r");
        int r3 = particleFile.getInt(customParticle3 + ".rgb.r");


        int g1 = particleFile.getInt(droneName + ".rgb.g");
        int g2 = particleFile.getInt(customParticle2 + ".rgb.g");
        int g3 = particleFile.getInt(customParticle3 + ".rgb.g");

        int b1 = particleFile.getInt(droneName + ".rgb.b");
        int b2 = particleFile.getInt(customParticle2 + ".rgb.b");
        int b3 = particleFile.getInt(customParticle3 + ".rgb.b");

        int delay1 = particleFile.getInt(droneName + ".delay");
        int delay2 = particleFile.getInt(customParticle2 + ".delay");
        int delay3 = particleFile.getInt(customParticle3 + ".delay");

        double yOffset1 = particleFile.getDouble(droneName + ".y-offset");
        double yOffset2 = particleFile.getDouble(customParticle2 + ".y-offset");
        double yOffset3 = particleFile.getDouble(customParticle3 + ".y-offset");

        int tick2 = particleFile.getInt(customParticle2 + ".tick");
        int tick3 = particleFile.getInt(customParticle3 + ".tick");

        List<String> blockCheckList = file.getStringList(path + "block-check");

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
            EntityEquipment equipment = missile.getEquipment();
            if (equipment != null) {
                equipment.setHelmet(plugin.drone_heads.get(rocketHead));
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

                    Location directionPose = targetLocation.subtract(missile.getLocation());
                    missile.setHeadPose(new EulerAngle(Math.atan2(Math.sqrt(directionPose.getX()*directionPose.getX() + directionPose.getZ()*directionPose.getZ()), directionPose.getY()) - Math.PI / 2, 0, 0));

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
                                Location location = currentMissileLocation.add(0, yOffset1, 0);
                                plugin.getParticleManager().displayParticle(droneName, particleType1, location, r1, g1, b1, size1, amount1);
                            }, delay1);
                        }

                        missile.remove();
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                            if (target.isDead()) {
                                dispatchTargetCommands(target, player, currentMissileLocation, path + "killed", file);
                            }
                        }, 2);
                    }

                        if (++particle1 > tick2) {
                            Location location = currentMissileLocation.clone().add(0, yOffset2, 0);
                            plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                                    plugin.getParticleManager().displayParticle(customParticle2, particleType2, location, r2, g2, b2, size2, amount2), delay2);
                            particle1 = 1;
                        }

                        if (++particle2 > tick3) {
                            Location location = currentMissileLocation.clone().add(0, yOffset3, 0);
                            plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                                    plugin.getParticleManager().displayParticle(customParticle3, particleType3, location, r3, g3, b3, size3, amount3), delay3);
                            particle2 = 1;
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

                dispatchCommands(file.getStringList("low-" + "health" + "." + percentLeft), player);

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