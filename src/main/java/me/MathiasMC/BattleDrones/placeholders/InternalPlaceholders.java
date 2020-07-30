package me.MathiasMC.BattleDrones.placeholders;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;

public class InternalPlaceholders {

    private final BattleDrones plugin;

    public InternalPlaceholders(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    public String getActiveDrone(String active) {
        if (active.equalsIgnoreCase("laser")) {
            return "Laser";
        } else if (active.equalsIgnoreCase("rocket")) {
            return "Rocket";
        } else if (active.equalsIgnoreCase("machine_gun")) {
            return "Machine Gun";
        } else if (active.equalsIgnoreCase("shield_generator")) {
            return "Shield Generator";
        } else if (active.equalsIgnoreCase("healing")) {
            return "Healing";
        } else if (active.equalsIgnoreCase("flamethrower")) {
            return "Flamethrower";
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