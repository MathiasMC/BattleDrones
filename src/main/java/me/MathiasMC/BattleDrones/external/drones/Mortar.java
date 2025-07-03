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
import org.bukkit.util.Vector;

import java.util.*;

public class Mortar extends DroneRegistry {

    private final BattleDrones plugin;

    public Mortar(BattleDrones plugin, String droneName, String droneCategory) {
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

        long maxAmmoSlots = file.getLong(path + "max-ammo-slots") * 64;

        String rocketHead = file.getString(path + "rocket-head");

        double rocketSpeed = file.getDouble(path + "rocket-speed");
        double rocketRadius = file.getDouble(path + "rocket-radius");
        double rocketPower = file.getDouble(path + "rocket-explosion-power");

        double minDamage = file.getDouble(path + "min");
        double maxDamage = file.getDouble(path + "max");

        boolean rocketExplosion = file.getBoolean(path + "rocket-explosion");
        boolean rocketFire = file.getBoolean(path + "rocket-explosion-fire");
        boolean rocketBlock = file.getBoolean(path + "rocket-explosion-block");
        boolean rocketDestruction = file.getBoolean(path + "rocket-self-destruction");


        long rocketTime = file.getLong(path + "rocket-time") * 20;

        int finalPoint = plugin.getCalculateManager().getProcentFromDouble(rocketSpeed);
        double finalHeight = file.getDouble(path + "rocket-height");
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
                int timer = 0;
                int particle1 = 0;
                int particle2 = 0;
                final ArrayList<String> exclude = new ArrayList<>();
                final World world = missile.getWorld();
                final Location start = missile.getLocation();
                final double height = start.distance(target.getLocation()) / finalHeight;
                final Vector startVector = target.getLocation().toVector().subtract(start.toVector());
                final float length = (float) startVector.length();
                final float pitch = (float) (4 * height / Math.pow(length, 2));
                final Set<Material> passable = Set.of(Material.AIR, Material.WATER, Material.LAVA, Material.GRASS_BLOCK, Material.TALL_GRASS);

                @Override
                public void run() {

                    Vector vector = startVector.clone().normalize().multiply(length * timer / finalPoint);
                    float x = ((float) timer / finalPoint) * length - length / 2;
                    float y = (float) (-pitch * Math.pow(x, 2) + height);
                    start.add(vector).add(0, y, 0);
                    plugin.getEntityManager().lookAT(missile, start.clone());
                    missile.teleport(start);
                    start.subtract(0, y, 0).subtract(vector);

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
                        ArrayList<LivingEntity> affected = plugin.getEntityManager().getLivingEntitiesAround(
                                missile, rocketRadius, 1, 1, 1, exclude, exclude, false
                        );

                        for (LivingEntity entity : affected) {
                            double damage = plugin.getCalculateManager().randomDouble(minDamage, maxDamage);
                            plugin.getCalculateManager().damage(entity, damage);
                            if (plugin.getCalculateManager().randomChance() <= file.getDouble(path + "chance") && entity instanceof Player) {
                                if (file.contains(path + "chance-commands")) {
                                    for (String command : file.getStringList(path + "chance-commands")) {
                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', command.replace("{player}", entity.getName())));
                                    }
                                }
                            }
                        }

                        if (rocketExplosion) {
                            world.createExplosion(currentMissileLocation.getX(), currentMissileLocation.getY(), currentMissileLocation.getZ(),
                                    (float) rocketPower,
                                    rocketFire,
                                    rocketBlock);
                        }

                        String newPathX = "";
                        if (target instanceof Player) {
                            newPathX = path + "explode" + ".player";
                        } else if (plugin.getEntityManager().isMonster(target)) {
                            newPathX = path + "explode" + ".monster";
                        } else if (plugin.getEntityManager().isAnimal(target)) {
                            newPathX = path + "explode" + ".animal";
                        }
                        final String xX = String.valueOf(headLocation.getBlockX());
                        final String yY = String.valueOf(headLocation.getBlockY());
                        final String zZ = String.valueOf(headLocation.getBlockZ());
                        final String worldX = Objects.requireNonNull(headLocation.getWorld()).getName();
                        String targetNameX = target.getName();
                        final String translateX = targetNameX.toUpperCase().replace(" ", "_");
                        if (plugin.getFileUtils().language.contains("translate." + translateX)) {
                            targetNameX = String.valueOf(plugin.getFileUtils().language.getString("translate." + translateX));
                        }
                        if (file.contains(newPathX)) {
                            for (String command : file.getStringList(newPathX)) {
                                plugin.getServer().dispatchCommand(plugin.consoleSender, command
                                        .replace("{world}", worldX)
                                        .replace("{x}", xX)
                                        .replace("{y}", yY)
                                        .replace("{z}", zZ)
                                        .replace("{player}", player.getName())
                                        .replace("{target}", targetNameX));
                            }

                        }

                        if (particleFile.contains(droneName)) {
                            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                                Location location = currentMissileLocation.add(0, yOffset_1, 0);
                                plugin.getParticleManager().displayParticle(droneName, particleType_1, location, r_1, g_1, b_1, size_1, amount_1);
                            }, delay_1);
                        }

                        missile.remove();
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                            if (target.isDead()) {
                                String newPath = "";
                                if (target instanceof Player) {
                                    newPath = path + "killed" + ".player";
                                } else if (plugin.getEntityManager().isMonster(target)) {
                                    newPath = path + "killed" + ".monster";
                                } else if (plugin.getEntityManager().isAnimal(target)) {
                                    newPath = path + "killed" + ".animal";
                                }
                                final String xx = String.valueOf(currentMissileLocation.getBlockX());
                                final String yy = String.valueOf(currentMissileLocation.getBlockY());
                                final String zz = String.valueOf(currentMissileLocation.getBlockZ());
                                final String world = Objects.requireNonNull(currentMissileLocation.getWorld()).getName();
                                String targetName = target.getName();
                                final String translate = targetName.toUpperCase().replace(" ", "_");
                                if (plugin.getFileUtils().language.contains("translate." + translate)) {
                                    targetName = String.valueOf(plugin.getFileUtils().language.getString("translate." + translate));
                                }
                                if (file.contains(newPath)) {
                                    for (String command : file.getStringList(newPath)) {
                                        plugin.getServer().dispatchCommand(plugin.consoleSender, command
                                                .replace("{world}", world)
                                                .replace("{x}", xx)
                                                .replace("{y}", yy)
                                                .replace("{z}", zz)
                                                .replace("{player}", player.getName())
                                                .replace("{target}", targetName));
                                    }

                                }
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

            // ADD LOGIC

            String newPathX = "";
            if (target instanceof Player) {
                newPathX = path + "run" + ".player";
            } else if (plugin.getEntityManager().isMonster(target)) {
                newPathX = path + "run" + ".monster";
            } else if (plugin.getEntityManager().isAnimal(target)) {
                newPathX = path + "run" + ".animal";
            }
            final String xX = String.valueOf(headLocation.getBlockX());
            final String yY = String.valueOf(headLocation.getBlockY());
            final String zZ = String.valueOf(headLocation.getBlockZ());
            final String worldX = Objects.requireNonNull(headLocation.getWorld()).getName();
            String targetNameX = target.getName();
            final String translateX = targetNameX.toUpperCase().replace(" ", "_");
            if (plugin.getFileUtils().language.contains("translate." + translateX)) {
                targetNameX = String.valueOf(plugin.getFileUtils().language.getString("translate." + translateX));
            }
            if (file.contains(newPathX)) {
                for (String command : file.getStringList(newPathX)) {
                    plugin.getServer().dispatchCommand(plugin.consoleSender, command
                            .replace("{world}", worldX)
                            .replace("{x}", xX)
                            .replace("{y}", yY)
                            .replace("{z}", zZ)
                            .replace("{player}", player.getName())
                            .replace("{target}", targetNameX));
                }

            }

        }, 0, cooldown).getTaskId();
    }
}
