package me.MathiasMC.BattleDrones;

import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class BattleDronesAPI {

    public DroneHolder getDroneHolder(String uuid, String drone) {
        return BattleDrones.call.getDroneHolder(uuid, drone);
    }

    public Set<String> getDroneholders() {
        return BattleDrones.call.listDroneHolder();
    }

    public PlayerConnect getPlayerConnect(String uuid) {
        return BattleDrones.call.get(uuid);
    }

    public Set<String> getPlayerConnects() {
        return BattleDrones.call.list();
    }

    public ItemStack getHeadFromTexture(String texture) {
        return BattleDrones.call.setTexture(texture);
    }

    public ItemStack getHead(String name) {
        return BattleDrones.call.drone_heads.get(name);
    }

    public int getDroneHealth(PlayerConnect playerConnect, String uuid) {
        return BattleDrones.call.internalPlaceholders.getDroneHealth(playerConnect, uuid);
    }

    public int getDroneMaxHealth(PlayerConnect playerConnect, String uuid) {
        return BattleDrones.call.internalPlaceholders.getDroneMaxHealth(playerConnect, uuid);
    }

    public int getDroneAmmo(PlayerConnect playerConnect, String uuid) {
        return BattleDrones.call.internalPlaceholders.getDroneAmmo(playerConnect, uuid);
    }

    public int getDroneMaxAmmo(PlayerConnect playerConnect, String uuid) {
        return BattleDrones.call.internalPlaceholders.getDroneMaxAmmo(playerConnect, uuid);
    }
}