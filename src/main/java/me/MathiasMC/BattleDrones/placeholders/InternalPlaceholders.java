package me.MathiasMC.BattleDrones.placeholders;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;

public class InternalPlaceholders {

    private final BattleDrones plugin;

    public InternalPlaceholders(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    public String getActiveDrone(PlayerConnect playerConnect) {
        String active = playerConnect.getActive();
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
        }
        return "";
    }

    public int getDroneHealth(PlayerConnect playerConnect, String uuid) {
        if (playerConnect.hasActive() && plugin.listDroneHolder().contains(uuid) && plugin.getDroneHolderUUID(uuid).containsKey(playerConnect.getActive())) {
            return plugin.getDroneHolder(uuid, playerConnect.getActive()).getHealth();
        }
        return 0;
    }

    public int getDroneMaxHealth(PlayerConnect playerConnect, String uuid) {
        if (playerConnect.hasActive()  && plugin.listDroneHolder().contains(uuid) && plugin.getDroneHolderUUID(uuid).containsKey(playerConnect.getActive())) {
            DroneHolder droneHolder = plugin.getDroneHolder(uuid, playerConnect.getActive());
            return plugin.droneFiles.get(playerConnect.getActive()).getInt(playerConnect.getGroup() + "." + droneHolder.getLeft() + ".health");
        }
        return 0;
    }

    public int getDroneMaxAmmo(PlayerConnect playerConnect, String uuid) {
        if (playerConnect.hasActive()  && plugin.listDroneHolder().contains(uuid) && plugin.getDroneHolderUUID(uuid).containsKey(playerConnect.getActive())) {
            DroneHolder droneHolder = plugin.getDroneHolder(uuid, playerConnect.getActive());
            return plugin.droneFiles.get(playerConnect.getActive()).getInt(playerConnect.getGroup() + "." + droneHolder.getLevel() + ".max-ammo-slots") * 64;
        }
        return 0;
    }

    public int getDroneAmmo(PlayerConnect playerConnect, String uuid) {
        if (playerConnect.hasActive()  && plugin.listDroneHolder().contains(uuid) && plugin.getDroneHolderUUID(uuid).containsKey(playerConnect.getActive())) {
            DroneHolder droneHolder = plugin.getDroneHolder(uuid, playerConnect.getActive());
            return droneHolder.getAmmo();
        }
        return 0;
    }

    public String getDroneHealthBar(PlayerConnect playerConnect, String uuid) {
        if (playerConnect.hasActive()  && plugin.listDroneHolder().contains(uuid) && plugin.getDroneHolderUUID(uuid).containsKey(playerConnect.getActive())) {
            DroneHolder droneHolder = plugin.getDroneHolder(uuid, playerConnect.getActive());
            return plugin.calculateManager.getHealthBarPlaceholder(droneHolder.getHealth(), plugin.droneFiles.get(playerConnect.getActive()).getLong(playerConnect.getGroup() + "." + droneHolder.getLevel() + ".health"));
        }
        return "";
    }
}