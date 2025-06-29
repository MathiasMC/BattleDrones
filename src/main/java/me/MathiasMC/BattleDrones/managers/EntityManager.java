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
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class EntityManager {

    private final BattleDrones plugin;

    public EntityManager(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    public ArrayList<LivingEntity> getLivingEntitiesAround(
            Entity entity,
            double radius,
            int monsters,
            int animal,
            int player,
            List<String> entityList,
            List<String> excludePlayers,
            boolean reverse
    ) {
        ArrayList<LivingEntity> list = new ArrayList<>();
        Collection<Entity> nearbyEntities = entity.getNearbyEntities(radius, radius, radius);

        for (Entity current : nearbyEntities) {
            String entityTypeName = current.getType().name().toLowerCase();
            if (entityList.contains(entityTypeName)) continue;

            if (monsters == 1 && isMonster(current)) {
                list.add((LivingEntity) current);
                continue;
            }

            if (animal == 1 && isAnimal(current)) {
                list.add((LivingEntity) current);
                continue;
            }

            if (player == 1 && current instanceof Player playerEntity) {
                if (!playerEntity.isOnline() || playerEntity.getGameMode() != GameMode.SURVIVAL) continue;

                String playerName = playerEntity.getName().toLowerCase();
                boolean isExcluded = excludePlayers.contains(playerName);

                if ((reverse && isExcluded) || (!reverse && !isExcluded)) {
                    list.add(playerEntity);
                }
            }
        }
        return list;
    }

    public LivingEntity getClosestLivingEntity(
            Entity entity,
            double radius,
            int monsters,
            int animal,
            int player,
            List<String> entityList,
            List<String> exclude,
            boolean reverse,
            boolean lowHealth
    ) {
        LivingEntity livingEntity = null;
        LivingEntity living = (LivingEntity) entity;
        double maxDistance = Double.MAX_VALUE;
        double livingMaxHealth = Objects.requireNonNull(living.getAttribute(Attribute.MAX_HEALTH)).getValue();

        if (lowHealth && living.getHealth() < livingMaxHealth) {
            return living;
        }

        Location entityLocation = entity.getLocation();

        for (LivingEntity candidate : getLivingEntitiesAround(entity, radius, monsters, animal, player, entityList, exclude, reverse)) {
            double distance = candidate.getLocation().distance(entityLocation);

            if (distance < maxDistance) {
                if (!lowHealth) {
                    maxDistance = distance;
                    livingEntity = candidate;
                } else {
                    double candidateMaxHealth = Objects.requireNonNull(candidate.getAttribute(Attribute.MAX_HEALTH)).getValue();
                    if (candidate.getHealth() < candidateMaxHealth) {
                        maxDistance = distance;
                        livingEntity = candidate;
                    }
                }
            }
        }
        return livingEntity;
    }

    public boolean hasBlockSight(Location start, Location end, List<String> list) {
        if (list == null || list.isEmpty()) return true;
        World world = start.getWorld();
        if (world == null) return false;

        Vector direction = end.toVector().subtract(start.toVector()).normalize();
        double distance = start.distance(end);

        RayTraceResult result = world.rayTraceBlocks(start, direction, distance, FluidCollisionMode.NEVER, true);

        if (result != null && result.getHitBlock() != null) {
            Material hitMaterial = result.getHitBlock().getType();
            return list.contains(hitMaterial.name());
        }
        return true;
    }

    public boolean isMonster(Entity entity) {
        return entity instanceof Monster
                || entity instanceof Slime
                || entity instanceof Phantom
                || entity instanceof IronGolem
                || entity instanceof Ghast
                || entity instanceof Shulker;
    }

    public boolean isAnimal(Entity entity) {
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

    public void setCustomName(ArmorStand head,
                              ArmorStand name,
                              long droneLevel,
                              String group,
                              FileConfiguration file,
                              String message,
                              Player player
    ) {
        String basePath = group + "." + droneLevel + ".";

        String headRaw = file.getString(basePath + "text." + message);
        if (headRaw != null && !headRaw.isEmpty()) {
            String headText = ChatColor.translateAlternateColorCodes('&', headRaw.replace("{name}", player.getName()));
            headText = plugin.getPlaceholderManager().replacePlaceholders(player, headText);

            if (!headText.equalsIgnoreCase(Objects.toString(head.getCustomName(), ""))) {
                head.setCustomName(headText);
            }
            if (!head.isCustomNameVisible()) {
                head.setCustomNameVisible(true);
            }
        } else if (head.isCustomNameVisible()) {
            head.setCustomNameVisible(false);
        }

        if (name != null) {
            String nameRaw = file.getString(basePath + "name." + message);
            if (nameRaw != null && !nameRaw.isEmpty()) {
                String nameText = ChatColor.translateAlternateColorCodes('&', nameRaw.replace("{name}", player.getName()));
                nameText = plugin.getPlaceholderManager().replacePlaceholders(player, nameText);

                if (!nameText.equalsIgnoreCase(Objects.toString(name.getCustomName(), ""))) {
                    name.setCustomName(nameText);
                }
                if (!name.isCustomNameVisible()) {
                    name.setCustomNameVisible(true);
                }
            } else if (name.isCustomNameVisible()) {
                name.setCustomNameVisible(false);
            }
        }
    }

    public ArmorStand getArmorStand(Location location) {
        ArmorStand as = Objects.requireNonNull(location.getWorld()).spawn(location, ArmorStand.class);
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
        if (playerConnect.isActive() && playerConnect.getActive().equals(drone) && !droneSpawnEvent.getType().equals(Type.UPGRADE) && !droneSpawnEvent.getType().equals(Type.MOVE)) {
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
        playerConnect.stopDrone(droneSpawnEvent.isRemoveTarget(), droneSpawnEvent.isRemovePark());
        if (droneHolder.isStationary()) {
            plugin.park.add(uuid);
        }
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
        Location playerLocation = droneSpawnEvent.getLocation();
        if (playerLocation == null) {
            playerLocation = player.getLocation().add(0, plugin.getFileUtils().getDouble(file, path + ".position.y", 2), 0);
            final float yaw = playerLocation.getYaw();
            final double xD = Math.sin(-0.0175 * yaw + plugin.getFileUtils().getDouble(file, path + ".position.x", 1.575)) + playerLocation.getX();
            final double zD = Math.cos(-0.0175 * yaw + plugin.getFileUtils().getDouble(file, path + ".position.z", 1.575)) + playerLocation.getZ();
            playerLocation = new Location(player.getWorld(), xD, playerLocation.getY(), zD);
        }
        playerLocation.setDirection(playerLocation.getDirection());
        final ArmorStand head = getArmorStand(playerLocation);
        final EntityEquipment equipment = head.getEquipment();
        if (equipment != null) {
            equipment.setHelmet(itemStack);
        }
        head.setCustomName(" ");
        head.setCustomNameVisible(true);
        head.getPersistentDataContainer().set(plugin.droneKey, PersistentDataType.STRING, uuid);
        playerConnect.head = head;
        if (Objects.requireNonNull(file.getString(path + ".name.searching")).length() > 0 || Objects.requireNonNull(file.getString(path + ".name.target")).length() > 0) {
            final ArmorStand name = getArmorStand(playerLocation.add(0, 0.3, 0));
            name.setCustomName(" ");
            name.setCustomNameVisible(true);
            name.getPersistentDataContainer().set(plugin.droneKey, PersistentDataType.STRING, uuid);
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

    public void spawnDroneSilent(final Player player, final PlayerConnect playerConnect, final String drone, final Type type) {
        final DroneSpawnEvent droneSpawnEvent = new DroneSpawnEvent(player, playerConnect, plugin.getDroneHolder(playerConnect.getUniqueId(), drone));
        droneSpawnEvent.setBypassWait(true);
        droneSpawnEvent.setBypassDroneAmount(true);
        droneSpawnEvent.setBypassLocation(true);
        droneSpawnEvent.setType(type);
        droneSpawnEvent.setSpawnCommands(null);
        droneSpawnEvent.spawn();
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
        final DroneHolder droneHolder = droneRemoveEvent.getDroneHolder();
        playerConnect.stopDrone(true, true);
        playerConnect.saveDrone(droneHolder);
        playerConnect.save();
        if (droneRemoveEvent.getRemoveCommands() == null) {
            return;
        }
        plugin.getDroneManager().runCommands(player, droneRemoveEvent.getRemoveCommands());
    }
}
