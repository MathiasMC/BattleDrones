package me.MathiasMC.BattleDrones.external.drones;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.api.DroneRegistry;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

public class MachineGun extends DroneRegistry {

    private final BattleDrones plugin;

    public MachineGun(BattleDrones plugin,
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

        FileConfiguration particleFile = plugin.getFileUtils().particles;
        String particleType = particleFile.getString(droneName + ".particle");
        int size = particleFile.getInt(droneName + ".size");
        int amount = particleFile.getInt(droneName + ".amount");
        int r = particleFile.getInt(droneName + ".rgb.r");
        int g = particleFile.getInt(droneName + ".rgb.g");
        int b = particleFile.getInt(droneName + ".rgb.b");
        int delay = particleFile.getInt(droneName + ".delay");
        double yOffset = particleFile.getDouble(droneName + ".y-offset");
        double space = particleFile.getDouble(droneName + ".space");
        double distance = particleFile.getDouble(droneName + ".distance");

        List<String> blockCheckList = plugin.getFileUtils().getBlockCheck(file, path);

        long cooldown = file.getLong(path + "cooldown");

        long maxAmmoSlots = file.getLong(path + "max-ammo-slots") * 64;

        double knockback = file.getDouble(path + ".knockback", 0);

        double min = file.getDouble(path + "min");
        double max = file.getDouble(path + "max");

        playerConnect.ability = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            final LivingEntity target = plugin.drone_targets.get(uuid);
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

            if (particleFile.contains(droneName)) {

                Location start = head.getEyeLocation().add(0, yOffset, 0);

                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    plugin.getParticleManager().displayLineParticle(particleType, start, targetLocation, distance, space, r, g, b, amount, size);
                }, delay);
            }

            double damage = min;

            if (plugin.getCalculateManager().randomChance() <= file.getDouble(path + "chance")) {
                if (knockback != 0D) {
                    target.setVelocity(target.getLocation().setDirection(headLocation.getDirection()).getDirection().setY(0).normalize().multiply(knockback));
                }
                damage = plugin.getCalculateManager().randomDouble(damage, max);
            }

            plugin.getCalculateManager().damage(target, damage);

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
                    final String x = String.valueOf(targetLocation.getBlockX());
                    final String y = String.valueOf(targetLocation.getBlockY());
                    final String z = String.valueOf(targetLocation.getBlockZ());
                    final String world = Objects.requireNonNull(targetLocation.getWorld()).getName();
                    String targetName = target.getName();
                    final String translate = targetName.toUpperCase().replace(" ", "_");
                    if (plugin.getFileUtils().language.contains("translate." + translate)) {
                        targetName = String.valueOf(plugin.getFileUtils().language.getString("translate." + translate));
                    }
                    if (file.contains(newPath)) {
                        for (String command : file.getStringList(newPath)) {
                            plugin.getServer().dispatchCommand(plugin.consoleSender, command
                                    .replace("{world}", world)
                                    .replace("{x}", x)
                                    .replace("{y}", y)
                                    .replace("{z}", z)
                                    .replace("{player}", player.getName())
                                    .replace("{target}", targetName));
                        }

                    }
                }
            }, 2);

        }, 0, cooldown).getTaskId();
    }
}
