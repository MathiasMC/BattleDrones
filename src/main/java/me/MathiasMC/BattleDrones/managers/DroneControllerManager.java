package me.MathiasMC.BattleDrones.managers;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Objects;

public class DroneControllerManager {

    private final BattleDrones plugin;

    public DroneControllerManager(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    public void selectTarget(final Player player, final EquipmentSlot equipmentSlot, final Action action) {
        if (!plugin.config.get.getBoolean("controller.use")) {
            return;
        }
        if (equipmentSlot != EquipmentSlot.HAND) {
            return;
        }
        final String uuid = player.getUniqueId().toString();
        if (!plugin.list().contains(uuid)) {
            return;
        }
        final PlayerConnect playerConnect = plugin.get(uuid);
        if (!playerConnect.hasActive()) {
            return;
        }
        final PlayerInventory playerInventory = player.getInventory();
        final int range = getRange(playerInventory.getItemInMainHand(), playerInventory.getItemInOffHand());
        if (range == 0) {
            return;
        }
        final String active = playerConnect.getActive();
        final boolean sneaking = player.isSneaking();
        if (sneaking) {
            if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                plugin.drone_targets.put(uuid, null);
                if (!plugin.manual.contains(uuid)) {
                    plugin.manual.add(uuid);
                    dispatchCommands("controller.manual", player.getName(), "", active);
                    return;
                }
                plugin.manual.remove(uuid);
                dispatchCommands("controller.automatic", player.getName(), "", active);
            }
            return;
        }
        if (!plugin.manual.contains(uuid)) {
            return;
        }
        if (action == Action.LEFT_CLICK_AIR) {
            final LivingEntity target = getEntityInSight(player, range);
            if (target == null) {
                dispatchCommands("controller.found", player.getName(), "", active);
                return;
            }
            if (plugin.drone_targets.get(uuid) != null) {
                return;
            }
            String targetName = target.getName();
            final String translate = targetName.toUpperCase().replace(" ", "_");
            if (plugin.language.get.contains("translate." + translate)) {
                targetName = String.valueOf(plugin.language.get.getString("translate." + translate));
            }
            if (!plugin.support.canTarget(player, target)) {
                dispatchCommands("controller.plugin", player.getName(), targetName, active);
                return;
            }
            String type = "players";
            if (plugin.droneManager.isMonster(target)) {
                type = "monsters";
            } else if (plugin.droneManager.isAnimal(target)) {
                type = "animals";
            }
            if (!plugin.support.worldGuard.canTarget(player, plugin.config.get.getStringList("worldguard." + active + "." + plugin.getDroneHolder(uuid, active) + "." + type))) {
                dispatchCommands("controller.location", player.getName(), targetName, active);
                return;
            }
            final Location start = player.getEyeLocation();
            if (start.distance(playerConnect.head.getLocation()) > range) {
                dispatchCommands("controller.far", player.getName(), targetName, active);
                return;
            }
            if (target instanceof Player) {
                if (((Player) target).getGameMode() != GameMode.SURVIVAL) {
                    dispatchCommands("controller.cannot", player.getName(), targetName, active);
                    return;
                }
            } else {
                final List<String> entityList = plugin.config.get.getStringList("controller.exclude");
                if (entityList.contains(target.getType().name().toLowerCase().replace(" ", "_"))) {
                    dispatchCommands("controller.cannot", player.getName(), targetName, active);
                    return;
                }
            }
            plugin.drone_targets.put(uuid, target);
            final Location end = target.getEyeLocation();
            if (plugin.config.get.getBoolean("controller.particle.use")) {
                plugin.particleManager.line("REDSTONE", start, end, start.distance(end), plugin.config.get.getDouble("controller.particle.space"), plugin.config.get.getInt("controller.particle.r"), plugin.config.get.getInt("controller.particle.g"), plugin.config.get.getInt("controller.particle.b"), plugin.config.get.getInt("controller.particle.amount"), plugin.config.get.getInt("controller.particle.size"));
            }
            dispatchCommands("controller.select", player.getName(), targetName, active);
            return;
        }
        if (action == Action.RIGHT_CLICK_AIR) {
            if (plugin.drone_targets.get(uuid) != null) {
                final LivingEntity target = plugin.drone_targets.get(uuid);
                String targetName = target.getName();
                final String translate = targetName.toUpperCase().replace(" ", "_");
                if (plugin.language.get.contains("translate." + translate)) {
                    targetName = String.valueOf(plugin.language.get.getString("translate." + translate));
                }
                plugin.drone_targets.put(uuid, null);
                dispatchCommands("controller.remove", player.getName(), targetName, active);
                return;
            }
            dispatchCommands("controller.no-target", player.getName(), "", active);
        }
    }

    private void dispatchCommands(final String path, final String playerName, final String targetName, final String active) {
        for (String message : plugin.language.get.getStringList(path)) {
            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", playerName).replace("{target}", targetName).replace("{drone}", plugin.internalPlaceholders.getActiveDrone(active))));
        }
    }

    private int getRange(final ItemStack mainHand, final ItemStack offHand) {
        final NamespacedKey key = new NamespacedKey(plugin, "drone_controller");
        int range = 0;
        if (offHand.hasItemMeta()) {
            final PersistentDataContainer persistentDataContainer = Objects.requireNonNull(offHand.getItemMeta()).getPersistentDataContainer();
            if (persistentDataContainer.has(key, PersistentDataType.INTEGER)) {
                range = persistentDataContainer.getOrDefault(key, PersistentDataType.INTEGER, 0);
            }
        }
        if (mainHand.hasItemMeta()) {
            final PersistentDataContainer persistentDataContainer = Objects.requireNonNull(mainHand.getItemMeta()).getPersistentDataContainer();
            if (persistentDataContainer.has(key, PersistentDataType.INTEGER)) {
                range = persistentDataContainer.getOrDefault(key, PersistentDataType.INTEGER, 0);
            }
        }
        return range;
    }

    private boolean isLookAT(final Player player, final LivingEntity target) {
        final Location location = player.getEyeLocation();
        final Vector vector = target.getEyeLocation().toVector().subtract(location.toVector());
        return vector.normalize().dot(location.getDirection()) > 0.99D;
    }

    private LivingEntity getEntityInSight(final Player player, final int range) {
        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (player.hasLineOfSight(entity) && entity instanceof LivingEntity) {
                final LivingEntity target = (LivingEntity) entity;
                if (isLookAT(player, target)) {
                    return target;
                }
            }
        }
        return null;
    }
}