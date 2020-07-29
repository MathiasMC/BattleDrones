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

    public int getDroneHealth(String uuid) {
        return BattleDrones.call.internalPlaceholders.getDroneHealth(uuid);
    }

    public int getDroneMaxHealth(String uuid) {
        return BattleDrones.call.internalPlaceholders.getDroneMaxHealth(uuid);
    }

    public int getDroneHealthPercent(String uuid) {
        return BattleDrones.call.calculateManager.getPercent(BattleDrones.call.internalPlaceholders.getDroneHealth(uuid), BattleDrones.call.internalPlaceholders.getDroneMaxHealth(uuid));
    }

    public int getDroneAmmo(String uuid) {
        return BattleDrones.call.internalPlaceholders.getDroneAmmo(uuid);
    }

    public int getDroneMaxAmmo(String uuid) {
        return BattleDrones.call.internalPlaceholders.getDroneMaxAmmo(uuid);
    }

    public String getDroneHealthBar(String uuid) {
        return BattleDrones.call.internalPlaceholders.getDroneHealthBar(uuid);
    }
}