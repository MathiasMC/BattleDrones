package me.MathiasMC.BattleDrones.external.drones;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.api.DroneRegistry;
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

public class Rocket extends DroneRegistry {

    private final BattleDrones plugin;

    public Rocket(BattleDrones plugin, String droneName, String droneCategory) {
        super(plugin, droneName, droneCategory);
        this.plugin = plugin;
    }

    @Override
    public void ability(final Player player, final PlayerConnect playerConnect, final DroneHolder droneHolder) {
        final String uuid = player.getUniqueId().toString();

        final String drone = playerConnect.getActive();
        boolean homingT = false;
        boolean mortarT = false;
        if (drone.equalsIgnoreCase("faf_missile")) {
            homingT = true;
        } else if (drone.equalsIgnoreCase("mortar")) {
            mortarT = true;
        }
        final boolean homing = homingT;
        final boolean mortar = mortarT;

        final String group = playerConnect.getGroup();
        final FileConfiguration file = plugin.droneFiles.get(drone);
        final String path = group + "." + droneHolder.getLevel() + ".";
        final ArmorStand armorStand = playerConnect.head;
        final FileConfiguration particleFile = plugin.getFileUtils().particles;
        final int delay_1 = particleFile.getInt(droneName + ".delay");
        final int size_1 = particleFile.getInt(droneName + ".size");
        final int amount_1 = particleFile.getInt(droneName + ".amount");
        final int r_1 = particleFile.getInt(droneName + ".rgb.r");
        final int g_1 = particleFile.getInt(droneName + ".rgb.g");
        final int b_1 = particleFile.getInt(droneName + ".rgb.b");
        final double yOffset_1 = particleFile.getDouble(droneName + ".y-offset");
        final String particleType_1 = particleFile.getString(droneName + ".particle");


        final String customParticle_2 = droneName + "_ability_1";
        final int delay_2 = particleFile.getInt(customParticle_2 + ".delay");
        final int size_2 = particleFile.getInt(customParticle_2 + ".size");
        final int amount_2 = particleFile.getInt(customParticle_2 + ".amount");
        final int r_2 = particleFile.getInt(customParticle_2 + ".rgb.r");
        final int g_2 = particleFile.getInt(customParticle_2 + ".rgb.g");
        final int b_2 = particleFile.getInt(customParticle_2 + ".rgb.b");
        final double yOffset_2 = particleFile.getDouble(customParticle_2 + ".y-offset");
        final String particleType_2 = particleFile.getString(customParticle_2 + ".particle");
        final int tick_2 = particleFile.getInt(customParticle_2 + ".tick");


        final String customParticle_3 = droneName + "_ability_2";
        final int delay_3 = particleFile.getInt(customParticle_3 + ".delay");
        final int size_3 = particleFile.getInt(customParticle_3 + ".size");
        final int amount_3 = particleFile.getInt(customParticle_3 + ".amount");
        final int r_3 = particleFile.getInt(customParticle_3 + ".rgb.r");
        final int g_3 = particleFile.getInt(customParticle_3 + ".rgb.g");
        final int b_3 = particleFile.getInt(customParticle_3 + ".rgb.b");
        final double yOffset_3 = particleFile.getDouble(customParticle_3 + ".y-offset");
        final String particleType_3 = particleFile.getString(customParticle_3 + ".particle");
        final int tick_3 = particleFile.getInt(customParticle_3 + ".tick");



        final int finalPoint = plugin.getCalculateManager().getProcentFromDouble(file.getDouble(path + "rocket-speed"));
        final double finalHeight = file.getDouble(path + "rocket-height");
        final List<String> list = plugin.getFileUtils().getBlockCheck(file, path);
        playerConnect.ability = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            final LivingEntity target = plugin.drone_targets.get(uuid);
            if (target != null) {
                if (droneHolder.getAmmo() > 0 || player.hasPermission("battledrones.bypass.ammo." + drone)) {
                    final Location location = armorStand.getLocation();
                    final Location targetLocation = target.getLocation();
                    if (armorStand.hasLineOfSight(target) && plugin.getEntityManager().hasBlockSight(armorStand, location, targetLocation, list)) {
                            final ArmorStand rock = plugin.getEntityManager().getArmorStand(armorStand.getLocation());
                            rock.getPersistentDataContainer().set(plugin.projectileKey, PersistentDataType.STRING, uuid);
                            if (file.contains(path + "rocket-head")) {
                                final EntityEquipment equipment = rock.getEquipment();
                                if (equipment != null) {
                                    equipment.setHelmet(plugin.drone_heads.get(file.getString(path + "rocket-head")));
                                }
                            }
                            plugin.projectiles.add(rock);
                            line(homing, mortar, finalHeight, finalPoint, targetLocation, file.getDouble(path + "rocket-speed"), rock, target, file, path, armorStand.getHeadPose(), player
                                    ,droneName, delay_1, size_1, amount_1, r_1, g_1, b_1, yOffset_1, particleType_1
                                    ,customParticle_2, delay_2, size_2, amount_2, r_2, g_2, b_2, yOffset_2, particleType_2, tick_2
                                    ,customParticle_3, delay_3, size_3, amount_3, r_3, g_3, b_3, yOffset_3, particleType_3, tick_3, particleFile
                            );
                            plugin.getDroneManager().checkMessage(droneHolder.getAmmo(), file.getInt(path + "max-ammo-slots") * 64, player, "ammo");
                            plugin.getDroneManager().checkShot(player, target, file, location, path, "run");
                            plugin.getDroneManager().takeAmmo(player, playerConnect, droneHolder, file, path);
                        }
                }
                playerConnect.setHealing(false);
            } else {
                playerConnect.setHealing(true);
            }
        }, 0, file.getLong(path + "cooldown")).getTaskId();
    }

    private void line(final boolean homing, final boolean mortar, final double heightAdd, final int points, final Location end, final double space, final ArmorStand armorStand, final LivingEntity target, final FileConfiguration file, final String path, final EulerAngle eulerAngle, final Player player
            , final String customParticle_1, final int delay_1, final int size_1, final int amount_1, final int r_1, final int g_1, final int b_1, final double yOffset_1, final String particleType_1
            , final String customParticle_2, final int delay_2, final int size_2, final int amount_2, final int r_2, final int g_2, final int b_2, final double yOffset_2, final String particleType_2, final int tick_1
            , final String customParticle_3, final int delay_3, final int size_3, final int amount_3, final int r_3, final int g_3, final int b_3, final double yOffset_3, final String particleType_3, final int tick_2, final FileConfiguration particleFile
    ) {
        new BukkitRunnable() {
            final Vector p1 = armorStand.getLocation().toVector();
            Vector vector = end.toVector().clone().subtract(p1).normalize().multiply(space);
            int timer = 0;
            int particle1 = 0;
            int particle2 = 0;
            final ArrayList<String> exclude = new ArrayList<>();
            final World world = armorStand.getWorld();
            final Location start = armorStand.getLocation();
            final double height = start.distance(target.getLocation()) / heightAdd;
            final Vector startVector = target.getLocation().toVector().subtract(start.toVector());
            final float length = (float) startVector.length();
            final float pitch = (float) (4 * height / Math.pow(length, 2));
            @Override
            public void run() {
                if (!homing) {
                    if (!mortar) {
                        armorStand.setHeadPose(eulerAngle);
                        armorStand.teleport(p1.toLocation(world));
                    } else {
                        final Vector vector = startVector.clone().normalize().multiply(length * timer / points);
                        final float x = ((float) timer / points) * length - length / 2;
                        final float y = (float) (-pitch * Math.pow(x, 2) + height);
                        start.add(vector).add(0, y, 0);
                        plugin.getEntityManager().lookAT(armorStand, start.clone());
                        armorStand.teleport(start);
                        start.subtract(0, y, 0).subtract(vector);
                    }
                } else {
                    final Location targetLocation = target.getLocation();
                    vector = targetLocation.toVector().clone().subtract(p1).normalize().multiply(space);
                    plugin.getEntityManager().lookAT(armorStand, targetLocation);
                    final Vector direction = target.getLocation().toVector().subtract(armorStand.getLocation().toVector()).normalize();
                    armorStand.teleport(p1.toLocation(world).setDirection(direction));
                }
                p1.add(vector);
                final Location armorStandLocation = armorStand.getLocation();
                final ArrayList<LivingEntity> rocket = plugin.getEntityManager().getLivingEntitiesAround(armorStand, 1,  1, 1, 1, exclude, exclude, false);
                rocket.remove(player);
                final Material targetMaterial = armorStand.getTargetBlock(null, 1).getType();
                if (timer > (file.getLong(path + "rocket-time") * 20) || rocket.size() > 0 || targetMaterial != Material.AIR && targetMaterial != Material.WATER && targetMaterial != Material.LAVA && targetMaterial != Material.GRASS && targetMaterial != Material.TALL_GRASS ||  file.getBoolean(path + "rocket-self-destruction") && target.isDead()) {
                    this.cancel();
                    final ArrayList<LivingEntity> livingEntities = plugin.getEntityManager().getLivingEntitiesAround(armorStand, file.getDouble(path + "rocket-radius"),  1, 1, 1, exclude, exclude, false);
                    for (LivingEntity livingEntity : livingEntities) {
                        plugin.getCalculateManager().damage(livingEntity, plugin.getCalculateManager().randomDouble(file.getDouble(path + "min"), file.getDouble(path + "max")));
                        if (plugin.getCalculateManager().randomChance() <= file.getDouble(path + "chance")) {
                            if (livingEntity instanceof Player) {
                                if (file.contains(path + "chance-commands")) {
                                    for (String command : file.getStringList(path + "chance-commands")) {
                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', command.replace("{player}", livingEntity.getName())));
                                    }
                                }
                            }
                        }
                    }
                    if (file.getBoolean(path + "rocket-explosion")) {
                        world.createExplosion(armorStandLocation.getX(), armorStandLocation.getY(), armorStandLocation.getZ(), (float) file.getDouble(path + "rocket-explosion-power"), file.getBoolean(path + "rocket-explosion-fire"), file.getBoolean(path + "rocket-explosion-block"));
                    }
                    plugin.getDroneManager().checkShot(player, target, file, armorStandLocation, path, "explode");
                    if (particleFile.contains(customParticle_1)) {
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                            Location location = armorStandLocation.add(0, yOffset_1, 0);
                            plugin.getParticleManager().displayParticle(customParticle_1, particleType_1, location, r_1, g_1, b_1, size_1, amount_1);
                        }, delay_1);
                    }
                    armorStand.remove();
                    plugin.getDroneManager().checkTarget(player, target, file, armorStandLocation, path, 2);
                }
                timer++;
                if (particleFile.contains(customParticle_2)) {
                    if (particle1 == 0) {
                        particle1 = tick_1 + 1;
                    }
                    if (particle1 > tick_1) {
                        final Location location = armorStandLocation.clone().add(0, yOffset_2, 0);
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.getParticleManager().displayParticle(customParticle_2, particleType_2, location, r_2, g_2, b_2, size_2, amount_2), delay_2);
                        particle1 = 1;
                    }
                    particle1++;
                }
                if (particleFile.contains(customParticle_3)) {
                        if (particle2 == 0) {
                            particle2 = tick_2 + 1;
                        }
                        if (particle2 > tick_2) {
                            final Location location = armorStandLocation.clone().add(0, yOffset_3, 0);
                            plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.getParticleManager().displayParticle(customParticle_3, particleType_3, location, r_3, g_3, b_3, size_3, amount_3), delay_3);
                            particle2 = 1;
                        }
                    particle2++;
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }
}