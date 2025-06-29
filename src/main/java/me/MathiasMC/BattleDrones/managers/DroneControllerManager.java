package me.MathiasMC.BattleDrones.managers;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import me.MathiasMC.BattleDrones.utils.ControllerUtils;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;

import java.util.List;

public class DroneControllerManager {

    private final BattleDrones plugin;

    public DroneControllerManager(final BattleDrones plugin) {
        this.plugin = plugin;
        updateFollowPath();
    }

    public void selectTarget(final Player player, final PlayerInteractEvent e) {
        final EquipmentSlot equipmentSlot = e.getHand();
        final Action action = e.getAction();
        if (!plugin.getFileUtils().config.getBoolean("controller.use")) {
            return;
        }
        if (equipmentSlot != EquipmentSlot.HAND) {
            return;
        }
        final String uuid = player.getUniqueId().toString();
        final PlayerConnect playerConnect = plugin.getPlayerConnect(uuid);
        if (!playerConnect.isActive()) {
            return;
        }
        final ControllerUtils controllerUtils = new ControllerUtils(player.getInventory());
        if (!controllerUtils.hasController()) {
            return;
        }
        final String active = playerConnect.getActive();
        if (!player.hasPermission("battledrones.player.controller")) {
            dispatchCommands("controller.permission", player, "");
            return;
        }
        final int range = controllerUtils.getRange();
        final boolean sneaking = player.isSneaking();
        if (sneaking) {
            if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                plugin.drone_targets.put(uuid, null);
                if (playerConnect.isAutomatic()) {
                    playerConnect.setAutomatic(false);
                    dispatchCommands("controller.manual", player, "");
                    if (controllerUtils.damage(player, plugin.getFileUtils().config.getInt("controller.damage.manual"))) {
                        e.setCancelled(true);
                    }
                    return;
                }
                playerConnect.setAutomatic(true);
                dispatchCommands("controller.automatic", player, "");
                if (controllerUtils.damage(player, plugin.getFileUtils().config.getInt("controller.damage.automatic"))) {
                    e.setCancelled(true);
                }
                return;
            }
            if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
                if (!player.hasPermission("battledrones.player.controller.follow")) {
                    dispatchCommands("controller.follow-permission", player, "");
                    return;
                }
                if (plugin.drone_targets.get(uuid) == null) {
                    dispatchCommands("controller.follow-no-target", player, "");
                    return;
                }

                String targetName = plugin.drone_targets.get(uuid).getName();
                final String translate = targetName.toUpperCase().replace(" ", "_");
                if (plugin.getFileUtils().language.contains("translate." + translate)) {
                    targetName = String.valueOf(plugin.getFileUtils().language.getString("translate." + translate));
                }
                playerConnect.dronePoint = null;
                if (!plugin.drone_follow.contains(uuid)) {
                    final FileConfiguration file = plugin.droneFiles.get(playerConnect.getActive());
                    if (file.getLong("follow-cost") != 0) {
                        final long cost = file.getLong("follow-cost");
                        if (plugin.getSupport().withdraw(player, cost)) {
                            for (String message : plugin.getFileUtils().language.getStringList("controller.follow-select-cost")) {
                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", player.getName()).replace("{target}", targetName).replace("{drone}", plugin.getPlaceholderManager().getActiveDrone(active)).replace("{cost}", String.valueOf(cost))));
                            }
                        } else {
                            dispatchCommands("controller.follow-select-enough", player, targetName);
                            return;
                        }
                    } else {
                        dispatchCommands("controller.follow-select", player, targetName);
                    }
                    plugin.drone_follow.add(uuid);
                    if (controllerUtils.damage(player, plugin.getFileUtils().config.getInt("controller.damage.follow"))) {
                        e.setCancelled(true);
                    }
                } else {
                    plugin.drone_follow.remove(uuid);
                    dispatchCommands("controller.follow-remove", player, targetName);
                }
            }
            return;
        }
        if (playerConnect.isAutomatic()) {
            return;
        }
        if (action == Action.LEFT_CLICK_AIR) {
            final LivingEntity target = getEntityInSight(player, range);
            if (target == null) {
                dispatchCommands("controller.found", player, "");
                return;
            }
            if (plugin.drone_targets.get(uuid) != null) {
                return;
            }
            String targetName = target.getName();
            final String translate = targetName.toUpperCase().replace(" ", "_");
            if (plugin.getFileUtils().language.contains("translate." + translate)) {
                targetName = String.valueOf(plugin.getFileUtils().language.getString("translate." + translate));
            }
            String type = "players";
            if (plugin.getEntityManager().isMonster(target)) {
                type = "monsters";
            } else if (plugin.getEntityManager().isAnimal(target)) {
                type = "animals";
            }
            if (!plugin.getSupport().canTarget(target, plugin.droneFiles.get(active), playerConnect.getGroup() + "." + plugin.getDroneHolder(uuid, active).getLevel() + ".worldguard." + type)) {
                dispatchCommands("controller.location", player, targetName);
                return;
            }
            final Location start = player.getEyeLocation();
            if (start.distance(playerConnect.head.getLocation()) > range) {
                dispatchCommands("controller.far", player, targetName);
                return;
            }
            if (target instanceof Player) {
                if (((Player) target).getGameMode() != GameMode.SURVIVAL) {
                    dispatchCommands("controller.cannot", player, targetName);
                    return;
                }
            } else {
                final List<String> entityList = plugin.getFileUtils().config.getStringList("controller.exclude");
                entityList.add("armor_stand");
                if (entityList.contains(target.getType().name().toLowerCase().replace(" ", "_"))) {
                    dispatchCommands("controller.cannot", player, targetName);
                    return;
                }
            }
            plugin.drone_targets.put(uuid, target);
            final Location end = target.getEyeLocation();
            if (plugin.getFileUtils().config.getBoolean("controller.particle.use")) {
                plugin.getParticleManager().displayLineParticle("DUST", start, end, start.distance(end), plugin.getFileUtils().config.getDouble("controller.particle.space"), plugin.getFileUtils().config.getInt("controller.particle.r"), plugin.getFileUtils().config.getInt("controller.particle.g"), plugin.getFileUtils().config.getInt("controller.particle.b"), plugin.getFileUtils().config.getInt("controller.particle.amount"), plugin.getFileUtils().config.getInt("controller.particle.size"));
            }
            dispatchCommands("controller.select", player, targetName);
            if (controllerUtils.damage(player, plugin.getFileUtils().config.getInt("controller.damage.select"))) {
                e.setCancelled(true);
            }
            return;
        }
        if (action == Action.RIGHT_CLICK_AIR) {
            if (plugin.drone_targets.get(uuid) != null) {
                final LivingEntity target = plugin.drone_targets.get(uuid);
                String targetName = target.getName();
                final String translate = targetName.toUpperCase().replace(" ", "_");
                if (plugin.getFileUtils().language.contains("translate." + translate)) {
                    targetName = String.valueOf(plugin.getFileUtils().language.getString("translate." + translate));
                }
                plugin.drone_targets.put(uuid, null);
                dispatchCommands("controller.remove", player, targetName);
                if (controllerUtils.damage(player, plugin.getFileUtils().config.getInt("controller.damage.remove"))) {
                    e.setCancelled(true);
                }
                return;
            }
            dispatchCommands("controller.no-target", player, "");
        }
    }

    private void dispatchCommands(final String path, final Player player, final String targetName) {
        for (String message : plugin.getFileUtils().language.getStringList(path)) {
            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', plugin.getPlaceholderManager().replacePlaceholders(player, message).replace("{target}", targetName)));
        }
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

    public void updateFollowPath() {
        plugin.getCalculateManager().x.clear();
        plugin.getCalculateManager().y.clear();
        plugin.getCalculateManager().z.clear();
        for (String add : plugin.getFileUtils().config.getConfigurationSection("follow.locations").getKeys(false)) {
            final int points = plugin.getFileUtils().config.getInt("follow.locations." + add + ".distance");
            final double radius = plugin.getFileUtils().config.getDouble("follow.locations." + add + ".radius");
            final double yoffset = plugin.getFileUtils().config.getDouble("follow.locations." + add + ".y-offset");
            for (int i = 0; i < points; i++) {
                final double angle = 2 * Math.PI * i / points;
                plugin.getCalculateManager().x.add((double) Math.round(radius * Math.sin(angle)));
                plugin.getCalculateManager().y.add(yoffset);
                plugin.getCalculateManager().z.add((double) Math.round(radius * Math.cos(angle)));
            }
        }
    }
}