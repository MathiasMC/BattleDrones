package me.MathiasMC.BattleDrones.placeholders;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.ChatColor;

public class InternalPlaceholders {

    private final BattleDrones plugin;

    public InternalPlaceholders(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    public String getActiveDrone(String active) {
        if (active.equalsIgnoreCase("laser")) {
            return ChatColor.translateAlternateColorCodes('&', plugin.language.get.getString("drone-name.laser"));
        } else if (active.equalsIgnoreCase("rocket")) {
            return ChatColor.translateAlternateColorCodes('&', plugin.language.get.getString("drone-name.rocket"));
        } else if (active.equalsIgnoreCase("faf_missile")) {
            return ChatColor.translateAlternateColorCodes('&', plugin.language.get.getString("drone-name.faf-missile"));
        } else if (active.equalsIgnoreCase("mortar")) {
            return ChatColor.translateAlternateColorCodes('&', plugin.language.get.getString("drone-name.mortar"));
        } else if (active.equalsIgnoreCase("machine_gun")) {
            return ChatColor.translateAlternateColorCodes('&', plugin.language.get.getString("drone-name.machine-gun"));
        } else if (active.equalsIgnoreCase("shield_generator")) {
            return ChatColor.translateAlternateColorCodes('&', plugin.language.get.getString("drone-name.shield-generator"));
        } else if (active.equalsIgnoreCase("healing")) {
            return ChatColor.translateAlternateColorCodes('&', plugin.language.get.getString("drone-name.healing"));
        } else if (active.equalsIgnoreCase("flamethrower")) {
            return ChatColor.translateAlternateColorCodes('&', plugin.language.get.getString("drone-name.flamethrower"));
        }
        return "";
    }

    public int getDroneHealth(String uuid) {
        final PlayerConnect playerConnect = plugin.get(uuid);
        if (playerConnect.hasActive() && plugin.listDroneHolder().contains(uuid) && plugin.getDroneHolderUUID(uuid).containsKey(playerConnect.getActive())) {
            return plugin.getDroneHolder(uuid, playerConnect.getActive()).getHealth();
        }
        return 0;
    }

    public int getDroneMaxHealth(String uuid) {
        final PlayerConnect playerConnect = plugin.get(uuid);
        if (playerConnect.hasActive()  && plugin.listDroneHolder().contains(uuid) && plugin.getDroneHolderUUID(uuid).containsKey(playerConnect.getActive())) {
            DroneHolder droneHolder = plugin.getDroneHolder(uuid, playerConnect.getActive());
            return plugin.droneFiles.get(playerConnect.getActive()).getInt(playerConnect.getGroup() + "." + droneHolder.getLevel() + ".health");
        }
        return 0;
    }

    public String getDroneHealthBar(String uuid) {
        final PlayerConnect playerConnect = plugin.get(uuid);
        if (playerConnect.hasActive()  && plugin.listDroneHolder().contains(uuid) && plugin.getDroneHolderUUID(uuid).containsKey(playerConnect.getActive())) {
            DroneHolder droneHolder = plugin.getDroneHolder(uuid, playerConnect.getActive());
            return plugin.calculateManager.getBar(droneHolder.getHealth(), plugin.droneFiles.get(playerConnect.getActive()).getInt(playerConnect.getGroup() + "." + droneHolder.getLevel() + ".health"), "health", "-placeholder");
        }
        return "";
    }

    public int getDroneMaxAmmo(String uuid) {
        final PlayerConnect playerConnect = plugin.get(uuid);
        if (playerConnect.hasActive()  && plugin.listDroneHolder().contains(uuid) && plugin.getDroneHolderUUID(uuid).containsKey(playerConnect.getActive())) {
            DroneHolder droneHolder = plugin.getDroneHolder(uuid, playerConnect.getActive());
            return plugin.droneFiles.get(playerConnect.getActive()).getInt(playerConnect.getGroup() + "." + droneHolder.getLevel() + ".max-ammo-slots") * 64;
        }
        return 0;
    }

    public int getDroneAmmo(String uuid) {
        final PlayerConnect playerConnect = plugin.get(uuid);
        if (playerConnect.hasActive()  && plugin.listDroneHolder().contains(uuid) && plugin.getDroneHolderUUID(uuid).containsKey(playerConnect.getActive())) {
            DroneHolder droneHolder = plugin.getDroneHolder(uuid, playerConnect.getActive());
            return droneHolder.getAmmo();
        }
        return 0;
    }

    public String getDroneAmmoBar(String uuid) {
        final PlayerConnect playerConnect = plugin.get(uuid);
        if (playerConnect.hasActive()  && plugin.listDroneHolder().contains(uuid) && plugin.getDroneHolderUUID(uuid).containsKey(playerConnect.getActive())) {
            DroneHolder droneHolder = plugin.getDroneHolder(uuid, playerConnect.getActive());
            return plugin.calculateManager.getBar(droneHolder.getAmmo(), plugin.droneFiles.get(playerConnect.getActive()).getInt(playerConnect.getGroup() + "." + droneHolder.getLevel() + ".max-ammo-slots") * 64, "ammo", "-placeholder");
        }
        return "";
    }
}