package me.MathiasMC.BattleDrones.managers;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Objects;

public class PlaceholderManager {

    private final BattleDrones plugin;

    public PlaceholderManager(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    public String getActiveDrone(final String active) {
        return ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(plugin.droneFiles.get(active).getString("drone-name")));
    }

    public int getDroneHealth(final String uuid) {
        final PlayerConnect playerConnect = plugin.getPlayerConnect(uuid);
        if (playerConnect.isActive()) {
            return plugin.getDroneHolder(uuid, playerConnect.getActive()).getHealth();
        }
        return 0;
    }

    public int getDroneMaxHealth(final String uuid) {
        final PlayerConnect playerConnect = plugin.getPlayerConnect(uuid);
        if (playerConnect.isActive()) {
            DroneHolder droneHolder = plugin.getDroneHolder(uuid, playerConnect.getActive());
            return plugin.droneFiles.get(playerConnect.getActive()).getInt(playerConnect.getGroup() + "." + droneHolder.getLevel() + ".health");
        }
        return 0;
    }

    public String getDroneHealthBar(final String uuid) {
        final PlayerConnect playerConnect = plugin.getPlayerConnect(uuid);
        if (playerConnect.isActive()) {
            DroneHolder droneHolder = plugin.getDroneHolder(uuid, playerConnect.getActive());
            return plugin.getCalculateManager().getBar(droneHolder.getHealth(), plugin.droneFiles.get(playerConnect.getActive()).getInt(playerConnect.getGroup() + "." + droneHolder.getLevel() + ".health"), "health", "-placeholder");
        }
        return "";
    }

    public int getDroneMaxAmmo(final String uuid) {
        final PlayerConnect playerConnect = plugin.getPlayerConnect(uuid);
        if (playerConnect.isActive()) {
            DroneHolder droneHolder = plugin.getDroneHolder(uuid, playerConnect.getActive());
            return plugin.droneFiles.get(playerConnect.getActive()).getInt(playerConnect.getGroup() + "." + droneHolder.getLevel() + ".max-ammo-slots") * 64;
        }
        return 0;
    }

    public int getDroneAmmo(final String uuid) {
        final PlayerConnect playerConnect = plugin.getPlayerConnect(uuid);
        if (playerConnect.isActive()) {
            DroneHolder droneHolder = plugin.getDroneHolder(uuid, playerConnect.getActive());
            return droneHolder.getAmmo();
        }
        return 0;
    }

    public String getDroneAmmoBar(final String uuid) {
        final PlayerConnect playerConnect = plugin.getPlayerConnect(uuid);
        if (playerConnect.isActive()) {
            DroneHolder droneHolder = plugin.getDroneHolder(uuid, playerConnect.getActive());
            return plugin.getCalculateManager().getBar(droneHolder.getAmmo(), plugin.droneFiles.get(playerConnect.getActive()).getInt(playerConnect.getGroup() + "." + droneHolder.getLevel() + ".max-ammo-slots") * 64, "ammo", "-placeholder");
        }
        return "";
    }

    public String getAvailability(final String uuid, final String type) {
        final PlayerConnect playerConnect = plugin.getPlayerConnect(uuid);
        if (playerConnect.isActive()) {
            DroneHolder droneHolder = plugin.getDroneHolder(uuid, playerConnect.getActive());
            int get = 0;
            if (type.equalsIgnoreCase("monsters")) {
                get = droneHolder.getMonsters();
            } else if (type.equalsIgnoreCase("animals")) {
                get = droneHolder.getAnimals();
            } else if (type.equalsIgnoreCase("players")) {
                get = droneHolder.getPlayers();
            }
            if (get == 1) {
                return ChatColor.translateAlternateColorCodes('&', String.valueOf(plugin.getFileUtils().language.getString("enabled")));
            } else {
                return ChatColor.translateAlternateColorCodes('&', String.valueOf(plugin.getFileUtils().language.getString("disabled")));
            }
        }
        return "";
    }

    public String getQuestion(final String uuid, final String type) {
        final PlayerConnect playerConnect = plugin.getPlayerConnect(uuid);
        if (playerConnect.isActive()) {
            DroneHolder droneHolder = plugin.getDroneHolder(uuid, playerConnect.getActive());
            boolean get = false;
            if (type.equalsIgnoreCase("parked")) {
                get = droneHolder.isParked();
            } else if (type.equalsIgnoreCase("stationary")) {
                get = droneHolder.isStationary();
            } else if (type.equalsIgnoreCase("move")) {
                if (droneHolder.isStationary() || !droneHolder.isStationary() && droneHolder.isParked()) {
                    get = true;
                }
            }
            if (get) {
                return ChatColor.translateAlternateColorCodes('&', String.valueOf(plugin.getFileUtils().language.getString("enabled-yes")));
            } else {
                return ChatColor.translateAlternateColorCodes('&', String.valueOf(plugin.getFileUtils().language.getString("disabled-no")));
            }
        }
        return "";
    }

    public String replacePlaceholders(final Player player, String message) {
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            message = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, message);
        }
        final Location location = player.getLocation();
        final PlayerConnect playerConnect = plugin.getPlayerConnect(player.getUniqueId().toString());
        message = message
                .replace("{player}", player.getName())
                .replace("{coins}", String.valueOf(playerConnect.getCoins()))
                .replace("{world}", player.getWorld().getName())
                .replace("{x}", String.valueOf(location.getBlockX()))
                .replace("{y}", String.valueOf(location.getBlockY()))
                .replace("{z}", String.valueOf(location.getBlockZ()));
        if (playerConnect.isActive()) {
            message = message.replace("{drone}", getActiveDrone(playerConnect.getActive()));
        }
        return message;
    }

    public String onPlaceholderRequest(final PlayerConnect playerConnect, final DroneHolder droneHolder, final String placeholder) {
        final FileConfiguration file = plugin.droneFiles.get(droneHolder.getDrone());
        final String path = playerConnect.getGroup() + "." + droneHolder.getLevel();
        final String path_next = playerConnect.getGroup() + "." + (droneHolder.getLevel() + 1);
        if (placeholder.equals("parked")) {
            if (droneHolder.isParked()) {
                return ChatColor.translateAlternateColorCodes('&', String.valueOf(plugin.getFileUtils().language.getString("enabled-yes")));
            } else {
                return ChatColor.translateAlternateColorCodes('&', String.valueOf(plugin.getFileUtils().language.getString("disabled-no")));
            }
        }
        if (placeholder.equals("stationary")) {
            if (droneHolder.isStationary()) {
                return ChatColor.translateAlternateColorCodes('&', String.valueOf(plugin.getFileUtils().language.getString("enabled-yes")));
            } else {
                return ChatColor.translateAlternateColorCodes('&', String.valueOf(plugin.getFileUtils().language.getString("disabled-no")));
            }
        }
        if (placeholder.equals("move")) {
            if (droneHolder.isStationary() || !droneHolder.isStationary() && droneHolder.isParked()) {
                return ChatColor.translateAlternateColorCodes('&', String.valueOf(plugin.getFileUtils().language.getString("enabled-yes")));
            } else {
                return ChatColor.translateAlternateColorCodes('&', String.valueOf(plugin.getFileUtils().language.getString("disabled-no")));
            }
        }
        if (placeholder.equals("mobs_current")) {
            if (droneHolder.getMonsters() == 1) {
                return ChatColor.translateAlternateColorCodes('&', String.valueOf(plugin.getFileUtils().language.getString("enabled")));
            } else {
                return ChatColor.translateAlternateColorCodes('&', String.valueOf(plugin.getFileUtils().language.getString("disabled")));
            }
        }
        if (placeholder.equals("animals_current")) {
            if (droneHolder.getAnimals() == 1) {
                return ChatColor.translateAlternateColorCodes('&', String.valueOf(plugin.getFileUtils().language.getString("enabled")));
            } else {
                return ChatColor.translateAlternateColorCodes('&', String.valueOf(plugin.getFileUtils().language.getString("disabled")));
            }
        }
        if (placeholder.equals("players_current")) {
            if (droneHolder.getPlayers() == 1) {
                return ChatColor.translateAlternateColorCodes('&', String.valueOf(plugin.getFileUtils().language.getString("enabled")));
            } else {
                return ChatColor.translateAlternateColorCodes('&', String.valueOf(plugin.getFileUtils().language.getString("disabled")));
            }
        }
        if (placeholder.equals("whitelist")) {
            return String.valueOf(droneHolder.getExclude().size());
        }
        if (placeholder.equals("health")) {
            return String.valueOf(droneHolder.getHealth());
        }
        if (placeholder.equals("health_bar")) {
            return plugin.getCalculateManager().getBar(droneHolder.getHealth(), file.getInt(path + ".health"), "health", "");
        }
        if (placeholder.equals("health_percentage")) {
            return String.valueOf(plugin.getCalculateManager().getPercent(droneHolder.getHealth(), file.getInt(path + ".health")));
        }
        if (placeholder.equals("ammo")) {
            return String.valueOf(droneHolder.getAmmo());
        }
        if (placeholder.equals("ammo_bar")) {
            return plugin.getCalculateManager().getBar(droneHolder.getAmmo(), file.getInt(path + ".max-ammo-slots") * 64, "ammo", "");
        }
        if (placeholder.equals("ammo_percentage")) {
            return String.valueOf(plugin.getCalculateManager().getPercent(droneHolder.getAmmo(), file.getInt(path + ".max-ammo-slots") * 64));
        }
        if (placeholder.equals("cost")) {
            return file.getString(playerConnect.getGroup() + "." + (droneHolder.getLevel() + 1) + ".cost");
        }
        if (placeholder.equals("max_ammo_slots")) {
            return file.getString(path + ".max-ammo-slots");
        }
        if (placeholder.equals("level")) {
            return String.valueOf(droneHolder.getLevel());
        }
        if (placeholder.equals("min_max")) {
            return file.getString(path + ".min") + "-" + file.getString(path + ".max");
        }
        if (placeholder.equals("shield_generator_damage")) {
            return plugin.getCalculateManager().getProcentFromDouble(file.getDouble(path + ".min")) + "-" + plugin.getCalculateManager().getProcentFromDouble(file.getDouble(path + ".max"));
        }
        if (placeholder.equals("range")) {
            return file.getString(playerConnect.getGroup() + "." + droneHolder.getLevel() + ".range");
        }
        if (placeholder.equals("firerate")) {
            return plugin.getCalculateManager().getFirerate(file.getDouble(playerConnect.getGroup() + "." + droneHolder.getLevel() + ".cooldown"));
        }
        if (placeholder.equals("cooldown")) {
            return file.getString(path + ".cooldown");
        }
        if (placeholder.equals("chance")) {
            return String.valueOf(plugin.getCalculateManager().getProcentFromDouble(file.getDouble(path + ".chance")));
        }
        if (placeholder.equals("radius")) {
            return file.getString(path + ".radius");
        }
        if (placeholder.equals("setfire_chance")) {
            return String.valueOf(plugin.getCalculateManager().getProcentFromDouble(file.getDouble(path + ".setfire-chance")));
        }
        if (placeholder.equals("explosion_chance")) {
            return String.valueOf(plugin.getCalculateManager().getProcentFromDouble(file.getDouble(path + ".explosion-chance")));
        }
        if (placeholder.equals("explosion_power")) {
            return file.getString(path + ".explosion-power");
        }
        if (placeholder.equals("burning_time")) {
            return file.getString(path + ".burning-time");
        }
        if (placeholder.equals("knockback")) {
            return file.getString(path + ".knockback");
        }
        if (placeholder.equals("rocket_speed")) {
            return file.getString(path + ".rocket-speed");
        }
        if (placeholder.equals("rocket_radius")) {
            return file.getString(path + ".rocket-radius");
        }
        if (placeholder.equals("rocket_time")) {
            return file.getString(path + ".rocket-time");
        }
        if (placeholder.equals("healing_health")) {
            return file.getString(path + ".healing.health");
        }
        if (placeholder.equals("healing_delay")) {
            return file.getString(path + ".healing.delay");
        }
        if (placeholder.equals("max_ammo_slots_next")) {
            return file.getString(path_next + ".max-ammo-slots");
        }
        if (placeholder.equals("level_next")) {
            return String.valueOf((droneHolder.getLevel() + 1));
        }
        if (placeholder.equals("min_max_next")) {
            return file.getString(path_next + ".min") + "-" + file.getString(path_next + ".max");
        }
        if (placeholder.equals("shield_generator_damage_next")) {
            return plugin.getCalculateManager().getProcentFromDouble(file.getDouble(path_next + ".min")) + "-" + plugin.getCalculateManager().getProcentFromDouble(file.getDouble(path_next + ".max"));
        }
        if (placeholder.equals("range_next")) {
            return file.getString(path_next + ".range");
        }
        if (placeholder.equals("firerate_next")) {
            return plugin.getCalculateManager().getFirerate(file.getDouble(path_next + ".cooldown"));
        }
        if (placeholder.equals("cooldown_next")) {
            return file.getString(path_next + ".cooldown");
        }
        if (placeholder.equals("chance_next")) {
            return String.valueOf(plugin.getCalculateManager().getProcentFromDouble(file.getDouble(path_next + ".chance")));
        }
        if (placeholder.equals("radius_next")) {
            return file.getString(path_next + ".radius");
        }
        if (placeholder.equals("setfire_chance_next")) {
            return String.valueOf(plugin.getCalculateManager().getProcentFromDouble(file.getDouble(path_next + ".setfire-chance")));
        }
        if (placeholder.equals("explosion_chance_next")) {
            return String.valueOf(plugin.getCalculateManager().getProcentFromDouble(file.getDouble(path_next + ".explosion-chance")));
        }
        if (placeholder.equals("explosion_power_next")) {
            return file.getString(path_next + ".explosion-power");
        }
        if (placeholder.equals("burning_time_next")) {
            return file.getString(path_next + ".burning-time");
        }
        if (placeholder.equals("knockback_next")) {
            return file.getString(path_next + ".knockback");
        }
        if (placeholder.equals("rocket_speed_next")) {
            return file.getString(path_next + ".rocket-speed");
        }
        if (placeholder.equals("rocket_radius_next")) {
            return file.getString(path_next + ".rocket-radius");
        }
        if (placeholder.equals("rocket_time_next")) {
            return file.getString(path_next + ".rocket-time");
        }
        if (placeholder.equals("healing_health_next")) {
            return file.getString(path_next + ".healing.health");
        }
        if (placeholder.equals("healing_delay_next")) {
            return file.getString(path_next + ".healing.delay");
        }
        if (placeholder.equals("teleport_ammo")) {
            return String.valueOf(file.getInt(path + ".ammo"));
        }
        if (placeholder.equals("teleport_distance")) {
            return String.valueOf(file.getDouble(path + ".teleport"));
        }
        if (placeholder.equals("teleport_ammo_next")) {
            return String.valueOf(file.getInt(path_next + ".ammo"));
        }
        if (placeholder.equals("teleport_distance_next")) {
            return String.valueOf(file.getDouble(path_next + ".teleport"));
        }
        return null;
    }
}
