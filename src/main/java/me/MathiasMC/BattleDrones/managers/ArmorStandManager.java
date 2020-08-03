package me.MathiasMC.BattleDrones.managers;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
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

    public void setCustomName(final PlayerConnect playerConnect, final long droneLevel, final String group, final FileConfiguration file, final String message, final Player player) {
        final String text = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(file.getString(group + "." + droneLevel + ".messages.text." + message)).replace("{name}", player.getName()));
        final ArmorStand armorStandText = playerConnect.head;
        if (!Objects.requireNonNull(armorStandText.getCustomName()).equalsIgnoreCase(text)) {
            armorStandText.setCustomName(text);
        }
        final String name = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(file.getString(group + "." + droneLevel + ".messages.name." + message)).replace("{name}", player.getName()));
        final ArmorStand armorStandName = playerConnect.name;
        if (!Objects.requireNonNull(armorStandName.getCustomName()).equalsIgnoreCase(name)) {
            armorStandName.setCustomName(name);
        }
    }

    public void lookAT(final ArmorStand armorStand, final Location location) {
        final Location direction = location.subtract(armorStand.getLocation());
        armorStand.setHeadPose(new EulerAngle(Math.atan2(Math.sqrt(direction.getX()*direction.getX() + direction.getZ()*direction.getZ()), direction.getY()) - Math.PI / 2, 0, 0));
    }

    public ArrayList<LivingEntity> getEntityAround(final Entity entity, final double radius, final int monsters, final int animal, final int player, final List<String> excludePlayers, final boolean reverseExclude) {
        if (!plugin.locationSupport.inWorldGuardRegion(entity)) {
            return new ArrayList<>();
        }
        final ArrayList<LivingEntity> list = new ArrayList<>();
        final List<String> entityList = plugin.config.get.getStringList("exclude");
        for (Entity currentEntity : entity.getNearbyEntities(radius, radius, radius)) {
            if (!entityList.contains(currentEntity.getName().toLowerCase())) {
                if (monsters == 1 && currentEntity instanceof org.bukkit.entity.Monster
                        || currentEntity instanceof Slime
                        || currentEntity instanceof Phantom
                        || currentEntity instanceof IronGolem
                        || currentEntity instanceof Ghast
                        || currentEntity instanceof Shulker) {
                    list.add((LivingEntity) currentEntity);
                }
                if (animal == 1 && currentEntity instanceof org.bukkit.entity.Animals
                        || currentEntity instanceof Villager
                        || currentEntity instanceof WanderingTrader
                        || currentEntity instanceof Dolphin
                        || currentEntity instanceof PufferFish
                        || currentEntity instanceof Squid
                        || currentEntity instanceof TropicalFish
                        || currentEntity instanceof Bat
                        || currentEntity instanceof Cod
                        || currentEntity instanceof Salmon) {
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