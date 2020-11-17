package me.MathiasMC.BattleDrones.data;

import me.MathiasMC.BattleDrones.BattleDrones;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;

public class PlayerConnect {

    private final BattleDrones plugin;

    public ArmorStand head;
    public ArmorStand name;
    public Location dronePoint;

    private boolean automatic = true;

    public int follow;
    public int find;
    public int ability;
    public int healing;

    private final String uuid;
    private String active;
    private long coins;
    private String group;
    private boolean isHealing;
    private String lastActive;

    public PlayerConnect(final String uuid) {
        this.plugin = BattleDrones.getInstance();
        this.uuid = uuid;
        final String[] data = plugin.database.getPlayers(uuid);
        this.active = data[0];
        this.coins = Long.parseLong(data[1]);
        this.group = data[2];
        this.lastActive = "";
    }

    public void setActive(final String droneName) {
        this.active = droneName;
        plugin.drone_amount.add(uuid);
    }

    public void setAutomatic(final boolean set) {
        this.automatic = set;
    }

    public void setCoins(final long set) {
        this.coins = set;
    }

    public void setGroup(String set) {
        this.group = set;
    }

    public void setHealing(final boolean set) {
        this.isHealing = set;
    }

    public String getActive() {
        return this.active;
    }

    public boolean isAutomatic() {
        return this.automatic;
    }

    public boolean isActive() {
        return !this.active.isEmpty();
    }

    public long getCoins() {
        return this.coins;
    }

    public String getGroup() {
        return this.group;
    }

    public String getUniqueId() {
        return this.uuid;
    }

    public boolean isHealing() {
        return this.isHealing;
    }

    public void setLastActive(final String set) {
        this.lastActive = set;
    }

    public String getLastActive() {
        return this.lastActive;
    }

    public boolean isLastActive() {
        return !this.lastActive.isEmpty();
    }

    private void remove() {
        if (head != null) {
            head.remove();
        }
        if (name != null) {
            name.remove();
        }
        for (ArmorStand armorStand : plugin.projectiles) {
            armorStand.remove();
        }
        head = null;
        name = null;
    }

    public void stopAI() {
        plugin.getServer().getScheduler().cancelTask(this.follow);
        plugin.getServer().getScheduler().cancelTask(this.ability);
        plugin.getServer().getScheduler().cancelTask(this.find);
    }

    public void stopHealing() {
        plugin.getServer().getScheduler().cancelTask(this.healing);
    }

    public void stopDrone(final boolean removeTarget, final boolean removePark) {
        remove();
        stopAI();
        stopHealing();
        setActive("");
        plugin.drone_amount.remove(uuid);
        if (removePark) {
            plugin.park.remove(uuid);
        }
        if (removeTarget) {
            plugin.drone_targets.remove(uuid);
        }
    }

    public void saveDrone(final DroneHolder droneHolder) {
        droneHolder.save();
    }

    public void save() {
        plugin.database.setPlayers(this.uuid, this.active, this.coins, this.group);
    }
}