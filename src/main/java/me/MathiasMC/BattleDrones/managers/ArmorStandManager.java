package me.MathiasMC.BattleDrones.managers;

import me.MathiasMC.BattleDrones.BattleDrones;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ArmorStandManager {

    private final BattleDrones plugin;

    public ArmorStandManager(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    public ArmorStand getArmorStand(final Location location, final boolean visible, final boolean mini) {
        final ArmorStand as = Objects.requireNonNull(location.getWorld()).spawn(location, ArmorStand.class);
        as.setVisible(visible);
        as.setSmall(mini);
        as.setBasePlate(false);
        as.setArms(false);
        as.setInvulnerable(true);
        as.setCanPickupItems(false);
        as.setGravity(false);
        return as;
    }

    public void setCustomName(final ArmorStand head, final ArmorStand name, final long droneLevel, final String group, final FileConfiguration file, final String message, final Player player) {
        final String text = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(file.getString(group + "." + droneLevel + ".messages.text." + message)).replace("{name}", player.getName()));
        if (text.length() > 0) {
            if (!Objects.requireNonNull(head.getCustomName()).equalsIgnoreCase(text)) {
                head.setCustomName(text);
            }
            if (!head.isCustomNameVisible()) {
                head.setCustomNameVisible(true);
            }
        } else {
            if (head.isCustomNameVisible()) {
                head.setCustomNameVisible(false);
            }
        }
        if (name != null) {
            final String nameText = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(file.getString(group + "." + droneLevel + ".messages.name." + message)).replace("{name}", player.getName()));
            if (nameText.length() > 0) {
                if (!Objects.requireNonNull(name.getCustomName()).equalsIgnoreCase(nameText)) {
                    name.setCustomName(nameText);
                }
                if (!name.isCustomNameVisible()) {
                    name.setCustomNameVisible(true);
                }
            } else {
                if (name.isCustomNameVisible()) {
                    name.setCustomNameVisible(false);
                }
            }
        }
    }

    public void lookAT(final ArmorStand armorStand, final Location location) {
        final Location direction = location.subtract(armorStand.getLocation());
        armorStand.setHeadPose(new EulerAngle(Math.atan2(Math.sqrt(direction.getX()*direction.getX() + direction.getZ()*direction.getZ()), direction.getY()) - Math.PI / 2, 0, 0));
    }

    public ArrayList<LivingEntity> getEntityAround(final Entity entity, final double radius, final int monsters, final int animal, final int player, final List<String> excludePlayers, final boolean reverseExclude) {
        final ArrayList<LivingEntity> list = new ArrayList<>();
        final List<String> entityList = plugin.config.get.getStringList("exclude");
        for (Entity currentEntity : entity.getNearbyEntities(radius, radius, radius)) {
            if (!entityList.contains(currentEntity.getName().toLowerCase())) {
                if (monsters == 1 && plugin.droneManager.isMonster(currentEntity)) {
                    list.add((LivingEntity) currentEntity);
                }
                if (animal == 1 && plugin.droneManager.isAnimal(currentEntity)) {
                    list.add((LivingEntity) currentEntity);
                }
                if (player == 1 && currentEntity instanceof org.bukkit.entity.Player) {
                    final Player playerEntity = (Player) currentEntity;
                    if (playerEntity.isOnline() && playerEntity.getGameMode().equals(GameMode.SURVIVAL)) {
                        if (reverseExclude) {
                            if (excludePlayers.contains(currentEntity.getName().toLowerCase())) {
                                list.add((LivingEntity) currentEntity);
                            }
                        } else {
                            if (!excludePlayers.contains(currentEntity.getName().toLowerCase())) {
                                list.add((LivingEntity) currentEntity);
                            }
                        }
                    }
                }
            }
        }
        return list;
    }

    public LivingEntity getClose(final Entity entity, final double radius, final int monsters, final int animal, final int player, final List<String> excludePlayers, final boolean reverseExclude, final boolean lowHP) {
        double max = Double.MAX_VALUE;
        LivingEntity livingEntity = null;
        final LivingEntity living = (LivingEntity) entity;
        if (lowHP && living.getHealth() < Objects.requireNonNull(living.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue()) {
            return (LivingEntity) entity;
        }
        final Location entityLocation = entity.getLocation();
        for(LivingEntity key : getEntityAround(entity, radius, monsters, animal, player, excludePlayers, reverseExclude)) {
            double distance = key.getLocation().distance(entityLocation);
                if (max == Double.MAX_VALUE || distance < max) {
                    max = distance;
                    if (!lowHP) {
                        livingEntity = key;
                    } else {
                        if (key.getHealth() < Objects.requireNonNull(key.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue()) {
                            livingEntity = key;
                        }
                    }
                }
        }
        return livingEntity;
    }

    public boolean hasBlockSight(final Location start, final Location end) {
        if (plugin.config.get.getBoolean("better-block-check")) {
            final World world = start.getWorld();
            if (world == null) {
                return false;
            }
            final BlockIterator block = new BlockIterator(world, start.toVector(), new Vector(end.getBlockX() - start.getBlockX(), end.getBlockY() - start.getBlockY(), end.getBlockZ() - start.getBlockZ()), 0, (int) Math.floor(start.distanceSquared(end)));
            while (block.hasNext()) {
                final Material material = block.next().getType();
                if (material.equals(Material.LAVA) || material.equals(Material.WATER)) {
                    return false;
                }
            }
        }
        return true;
    }
}