package me.MathiasMC.BattleDrones.external.drones;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.api.DroneRegistry;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

public class Healing extends DroneRegistry {

    private final BattleDrones plugin;

    public Healing(BattleDrones plugin, String droneName, String droneCategory) {
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

        List<String> blockCheckList = plugin.getFileUtils().getBlockCheck(file, path);

        long cooldown = file.getLong(path + "cooldown") * 20;

        long maxAmmoSlots = file.getLong(path + "max-ammo-slots") * 64;

        double min = file.getDouble(path + "min");
        double max = file.getDouble(path + "max");

        playerConnect.ability = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            LivingEntity target = plugin.drone_targets.get(uuid);
            if (target == null) {
                playerConnect.setHealing(true);
                return;
            }

            playerConnect.setHealing(false);

            boolean hasAmmo = droneHolder.getAmmo() > 0 || player.hasPermission("battledrones.bypass.ammo." + droneName);
            if (!hasAmmo) return;

            Location headLocation = head.getEyeLocation();
            Location targetLocation = target.getEyeLocation();

            double health = target.getHealth();
            double maxHealth = Objects.requireNonNull(target.getAttribute(Attribute.MAX_HEALTH)).getValue();

            boolean canSeeTarget = head.hasLineOfSight(target) && health < maxHealth && plugin.getEntityManager().hasBlockSight(headLocation, targetLocation, blockCheckList);

            if (!canSeeTarget) return;

            if (particleFile.contains(droneName)) {
                Location start = head.getEyeLocation().add(0, yOffset, 0);
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.getParticleManager().displayLineParticle(particleType, start, targetLocation, start.distance(targetLocation), space, r, g, b, amount, size), delay);
            }

            double add = plugin.getCalculateManager().randomDouble(min, max);
            target.setHealth(Math.min(health + add, maxHealth));


            // ADD LOGIC


            String newPath = "";
            if (target instanceof Player) {
                newPath = path + "run" + ".player";
            } else if (plugin.getEntityManager().isMonster(target)) {
                newPath = path + "run" + ".monster";
            } else if (plugin.getEntityManager().isAnimal(target)) {
                newPath = path + "run" + ".animal";
            }
            final String x = String.valueOf(headLocation.getBlockX());
            final String y = String.valueOf(headLocation.getBlockY());
            final String z = String.valueOf(headLocation.getBlockZ());
            final String world = Objects.requireNonNull(headLocation.getWorld()).getName();
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

        }, 0, cooldown).getTaskId();
    }
}