package me.MathiasMC.BattleDrones.managers;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.api.DroneRegistry;
import me.MathiasMC.BattleDrones.api.Type;
import me.MathiasMC.BattleDrones.api.events.DroneRemoveEvent;
import me.MathiasMC.BattleDrones.api.events.DroneSpawnEvent;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.EulerAngle;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EntityManager {

    private final BattleDrones plugin;

    public EntityManager(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    public ArrayList<LivingEntity> getLivingEntitiesAround(final Entity entity, final double radius, final int monsters, final int animal, final int player, final List<String> entityList, final List<String> excludePlayers, final boolean reverse) {
        final ArrayList<LivingEntity> list = new ArrayList<>();
        for (Entity currentEntity : entity.getNearbyEntities(radius, radius, radius)) {
            if (!entityList.contains(currentEntity.getType().name().toLowerCase())) {
                if (monsters == 1 && isMonster(currentEntity)) {
                    list.add((LivingEntity) currentEntity);
                }
                if (animal == 1 && isAnimal(currentEntity)) {
                    list.add((LivingEntity) currentEntity);
                }
                if (player == 1 && currentEntity instanceof Player) {
                    final Player playerEntity = (Player) currentEntity;
                    if (playerEntity.isOnline() && playerEntity.getGameMode().equals(GameMode.SURVIVAL)) {
                        final String name = playerEntity.getName().toLowerCase();
                        if (reverse) {
                            if (excludePlayers.contains(name)) {
                                list.add(playerEntity);
                            }
                        } else {
                            if (!excludePlayers.contains(name)) {
                                list.add(playerEntity);
                            }
                        }
                    }
                }
            }
        }
        return list;
    }

    public LivingEntity getClosestLivingEntity(final Entity entity, final double radius, final int monsters, final int animal, final int player, final List<String> entityList, final List<String> exclude, final boolean reverse, final boolean lowHealth) {
        double max = Double.MAX_VALUE;
        LivingEntity livingEntity = null;
        final LivingEntity living = (LivingEntity) entity;
        if (lowHealth && living.getHealth() < Objects.requireNonNull(living.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue()) {
            return (LivingEntity) entity;
        }
        final Location entityLocation = entity.getLocation();
        for(LivingEntity key : getLivingEntitiesAround(entity, radius, monsters, animal, player, entityList, exclude, reverse)) {
            double distance = key.getLocation().distance(entityLocation);
            if (max == Double.MAX_VALUE || distance < max) {
                max = distance;
                if (!lowHealth) {
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

    public boolean hasBlockSight(final ArmorStand armorStand, final Location start, final Location end, final List<String> list) {
        if (list != null) {
            final World world = start.getWorld();
            if (world == null) {
                return false;
            }
            final Material material = armorStand.getTargetBlock(null, (int) Math.floor(start.distanceSquared(end))).getType();
            return !list.contains(material.name());
        }
        return true;
    }

    public boolean isMonster(final Entity entity) {
        return entity instanceof Monster
                || entity instanceof Slime
                || entity instanceof Phantom
                || entity instanceof IronGolem
                || entity instanceof Ghast
                || entity instanceof Shulker;
    }

    public boolean isAnimal(final Entity entity) {
        return entity instanceof Animals
                || entity instanceof Villager
                || entity instanceof WanderingTrader
                || entity instanceof Dolphin
                || entity instanceof PufferFish
                || entity instanceof Squid
                || entity instanceof TropicalFish
                || entity instanceof Bat
                || entity instanceof Cod
                || entity instanceof Salmon;
    }

    public void lookAT(final ArmorStand armorStand, final Location location) {
        final Location direction = location.subtract(armorStand.getLocation());
        armorStand.setHeadPose(new EulerAngle(Math.atan2(Math.sqrt(direction.getX()*direction.getX() + direction.getZ()*direction.getZ()), direction.getY()) - Math.PI / 2, 0, 0));
    }

    public void setCustomName(final ArmorStand head, final ArmorStand name, final long droneLevel, final String group, final FileConfiguration file, final String message, final Player player) {
        final String text = plugin.getPlaceholderManager().replacePlaceholders(player, ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(file.getString(group + "." + droneLevel + ".text." + message)).replace("{name}", player.getName())));
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
            final String nameText = plugin.getPlaceholderManager().replacePlaceholders(player, ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(file.getString(group + "." + droneLevel + ".name." + message)).replace("{name}", player.getName())));
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

    public ArmorStand getArmorStand(final Location location) {
        final ArmorStand as = Objects.requireNonNull(location.getWorld()).spawn(location, ArmorStand.class);
        as.setVisible(false);
        as.setSmall(true);
        as.setBasePlate(false);
        as.setArms(false);
        as.setInvulnerable(true);
        as.setCanPickupItems(false);
        as.setGravity(false);
        return as;
    }

    public void spawnDrone(final DroneSpawnEvent droneSpawnEvent) {
        final Player player = droneSpawnEvent.getPlayer();
        final String uuid = player.getUniqueId().toString();
        final PlayerConnect playerConnect = plugin.getPlayerConnect(uuid);
        final DroneHolder droneHolder = droneSpawnEvent.getDroneHolder();
        final String drone = droneHolder.getDrone();
        if (playerConnect.isActive() && playerConnect.getActive().equals(drone) && !droneSpawnEvent.getType().equals(Type.UPGRADE)) {
            return;
        }
        if (!droneSpawnEvent.isBypassLocation() && !droneSpawnEvent.isInLocation()) {
            return;
        }
        final FileConfiguration file = plugin.droneFiles.get(drone);
        if (droneHolder.getUnlocked() == 0) {
            plugin.getDroneManager().runCommands(player, file, "gui.UNLOCKED");
            return;
        }
        if (droneSpawnEvent.hasWait()) {
            if (!player.hasPermission("battledrones.bypass.activate")) {
                plugin.getDroneManager().runCommands(player, file, "gui.WAIT");
                return;
            }
        } else if (!droneSpawnEvent.isBypassWait()) {
            plugin.getDroneManager().waitSchedule(uuid, file);
        }
        if (plugin.drone_amount.size() >= plugin.getFileUtils().config.getInt("drone-amount")) {
            if (!plugin.getDroneManager().canBypassDroneAmount(player) || !droneSpawnEvent.isBypassDroneAmount()) {
                plugin.getDroneManager().runCommands(player, file, "gui.MAX-REACHED");
                return;
            }
        }
        plugin.getServer().getPluginManager().callEvent(droneSpawnEvent);
        if (droneSpawnEvent.isCancelled()) {
            return;
        }
        playerConnect.stopDrone();
        final String path = playerConnect.getGroup() + "." + droneHolder.getLevel();
        ItemStack itemStack = plugin.drone_heads.get(file.getString(path + ".head"));
        if (file.contains(path + ".model-data")) {
            itemStack = new ItemStack(Material.STICK);
            final ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null) {
                itemMeta.setCustomModelData(file.getInt(path + ".model-data"));
                itemStack.setItemMeta(itemMeta);
            }
        }
        final Location playerLocation = player.getLocation().add(0, plugin.getFileUtils().getDouble(file, path + ".position.y", 2), 0);
        final float yaw = playerLocation.getYaw();
        final double xD = Math.sin(-0.0175 * yaw + plugin.getFileUtils().getDouble(file, path + ".position.x", 1.575)) + playerLocation.getX();
        final double zD = Math.cos(-0.0175 * yaw + plugin.getFileUtils().getDouble(file, path + ".position.z", 1.575)) + playerLocation.getZ();
        final Location spawnLocation = new Location(player.getWorld(), xD, playerLocation.getY(), zD);
        spawnLocation.setDirection(playerLocation.getDirection());
        final NamespacedKey key = new NamespacedKey(plugin, "drone_uuid");
        final ArmorStand head = getArmorStand(spawnLocation);
        final EntityEquipment equipment = head.getEquipment();
        if (equipment != null) {
            equipment.setHelmet(itemStack);
        }
        head.setCustomName(" ");
        head.setCustomNameVisible(true);
        head.getPersistentDataContainer().set(key, PersistentDataType.STRING, uuid);
        playerConnect.head = head;
        if (Objects.requireNonNull(file.getString(path + ".name.searching")).length() > 0 || Objects.requireNonNull(file.getString(path + ".name.target")).length() > 0) {
            final ArmorStand name = getArmorStand(spawnLocation.add(0, 0.3, 0));
            name.setCustomName(" ");
            name.setCustomNameVisible(true);
            name.getPersistentDataContainer().set(key, PersistentDataType.STRING, uuid);
            playerConnect.name = name;
        }
        plugin.getEntityManager().setCustomName(playerConnect.head, playerConnect.name, droneHolder.getLevel(), playerConnect.getGroup(), file, "searching", player);
        playerConnect.setActive(drone);
        playerConnect.setLastActive(drone);
        playerConnect.setHealing(true);
        final DroneRegistry droneRegistry = plugin.droneRegistry.get(drone);
        droneRegistry.follow(player, playerConnect, droneHolder);
        droneRegistry.find(player, playerConnect, droneHolder);
        droneRegistry.ability(player, playerConnect, droneHolder);
        droneRegistry.healing(player, playerConnect, droneHolder);
        if (droneSpawnEvent.getSpawnCommands() == null) {
            return;
        }
        plugin.getDroneManager().runCommands(player, droneSpawnEvent.getSpawnCommands());
    }

    public void removeDrone(final DroneRemoveEvent droneRemoveEvent) {
        plugin.getServer().getPluginManager().callEvent(droneRemoveEvent);
        final Player player = droneRemoveEvent.getPlayer();
        final PlayerConnect playerConnect = droneRemoveEvent.getPlayerConnect();
        if (!playerConnect.isActive()) {
            return;
        }
        if (droneRemoveEvent.isCancelled()) {
            return;
        }
        if (droneRemoveEvent.getRemoveCommands() == null) {
            return;
        }
        plugin.getDroneManager().runCommands(player, droneRemoveEvent.getRemoveCommands());
        final DroneHolder droneHolder = droneRemoveEvent.getDroneHolder();
        playerConnect.stopDrone();
        playerConnect.saveDrone(droneHolder);
        playerConnect.save();
    }
}
