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

    public ArmorStand getArmorStand(Location location, boolean visible, boolean mini) {
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

    public void setCustomName(PlayerConnect playerConnect, long droneLevel, String group, FileConfiguration file, String message, Player player) {
        final String text = ChatColor.translateAlternateColorCodes('&', file.getString(group + "." + droneLevel + ".messages.text." + message).replace("{name}", player.getName()));
        final ArmorStand armorStandText = playerConnect.head;
        if (!armorStandText.getCustomName().equalsIgnoreCase(text)) {
            armorStandText.setCustomName(text);
        }
        final String name = ChatColor.translateAlternateColorCodes('&', file.getString(group + "." + droneLevel + ".messages.name." + message).replace("{name}", player.getName()));
        final ArmorStand armorStandName = playerConnect.name;
        if (!armorStandName.getCustomName().equalsIgnoreCase(name)) {
            armorStandName.setCustomName(name);
        }
    }

    public void lookAT(ArmorStand armorStand, Location location) {
        Location direction = location.subtract(armorStand.getLocation());
        armorStand.setHeadPose(new EulerAngle(Math.atan2(Math.sqrt(direction.getX()*direction.getX() + direction.getZ()*direction.getZ()), direction.getY()) - Math.PI / 2, 0, 0));
    }

    public ArrayList<LivingEntity> getEntityAround(Entity entity, double radius, int monsters, int animal, int player, List<String> excludePlayers, boolean reverseExclude) {
        if (!plugin.worldGuard.inRegion(entity)) {
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
                        || currentEntity instanceof Cod) {
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

    public LivingEntity getClose(Entity entity, double radius, int monsters, int animal, int player, List<String> excludePlayers, boolean reverseExclude, boolean lowHP) {
        double max = Double.MAX_VALUE;
        LivingEntity livingEntity = null;
        final LivingEntity living = (LivingEntity) entity;
        if (lowHP && living.getHealth() < living.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) {
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
                        if (key.getHealth() < key.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) {
                            livingEntity = key;
                        }
                    }
                }
        }
        return livingEntity;
    }

    public boolean hasBlockSight(Location start, Location end) {
        if (plugin.config.get.getBoolean("better-block-check")) {
            BlockIterator block = new BlockIterator(start.getWorld(), start.toVector(), new Vector(end.getBlockX() - start.getBlockX(), end.getBlockY() - start.getBlockY(), end.getBlockZ() - start.getBlockZ()), 0, (int) Math.floor(start.distanceSquared(end)));
            while (block.hasNext()) {
                Material material = block.next().getType();
                if (material.equals(Material.LAVA) || material.equals(Material.WATER)) {
                    return false;
                }
            }
        }
        return true;
    }
}