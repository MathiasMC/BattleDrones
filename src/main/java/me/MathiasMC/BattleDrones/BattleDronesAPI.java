package me.MathiasMC.BattleDrones;

import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class BattleDronesAPI {

    public DroneHolder getDroneHolder(final String uuid, final String drone) {
        return BattleDrones.call.getDroneHolder(uuid, drone);
    }

    public Set<String> getDroneholders() {
        return BattleDrones.call.listDroneHolder();
    }

    public PlayerConnect getPlayerConnect(final String uuid) {
        return BattleDrones.call.get(uuid);
    }

    public Set<String> getPlayerConnects() {
        return BattleDrones.call.list();
    }

    public ItemStack getHeadFromTexture(final String texture) {
        return BattleDrones.call.setTexture(texture);
    }

    public ItemStack getHead(final String name) {
        return BattleDrones.call.drone_heads.get(name);
    }

    public int getDroneHealth(final String uuid) {
        return BattleDrones.call.internalPlaceholders.getDroneHealth(uuid);
    }

    public int getDroneMaxHealth(final String uuid) {
        return BattleDrones.call.internalPlaceholders.getDroneMaxHealth(uuid);
    }

    public String getDroneHealthBar(final String uuid) {
        return BattleDrones.call.internalPlaceholders.getDroneHealthBar(uuid);
    }

    public int getDroneHealthPercent(final String uuid) {
        return BattleDrones.call.calculateManager.getPercent(BattleDrones.call.internalPlaceholders.getDroneHealth(uuid), BattleDrones.call.internalPlaceholders.getDroneMaxHealth(uuid));
    }

    public int getDroneAmmo(final String uuid) {
        return BattleDrones.call.internalPlaceholders.getDroneAmmo(uuid);
    }

    public int getDroneMaxAmmo(final String uuid) {
        return BattleDrones.call.internalPlaceholders.getDroneMaxAmmo(uuid);
    }

    public String getDroneAmmoBar(final String uuid) {
        return BattleDrones.call.internalPlaceholders.getDroneAmmoBar(uuid);
    }

    public int getDroneAmmoPercent(final String uuid) {
        return BattleDrones.call.calculateManager.getPercent(BattleDrones.call.internalPlaceholders.getDroneAmmo(uuid), BattleDrones.call.internalPlaceholders.getDroneMaxAmmo(uuid));
    }
}