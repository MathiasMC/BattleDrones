package me.MathiasMC.BattleDrones.data;

import me.MathiasMC.BattleDrones.BattleDrones;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

public class PlayerConnect {

    public ArmorStand head;
    public ArmorStand name;

    public int AItaskID;
    public int ShootTaskID;
    public int RegenTaskID;

    private final String uuid;
    private String active;
    private long coins;
    private String group;
    private boolean regen;

    public PlayerConnect(final String uuid) {
        this.uuid = uuid;
        final String[] data = BattleDrones.call.database.getPlayers(uuid);
        this.active = data[0];
        this.coins = Long.parseLong(data[1]);
        this.group = data[2];
    }

    public void setActive(String set) {
        this.active = set;
        BattleDrones.call.drone_amount.add(uuid);
    }

    public void setCoins(long set) {
        this.coins = set;
    }

    public void setGroup(String set) {
        this.group = set;
    }

    public void setRegen(boolean set) {
        this.regen = set;
    }

    public String getActive() {
        return this.active;
    }

    public boolean hasActive() {
        return !this.active.isEmpty();
    }

    public long getCoins() {
        return this.coins;
    }

    public String getGroup() {
        return this.group;
    }

    public boolean canRegen() {
        return this.regen;
    }

    public void remove() {
        if (head != null) {
            head.remove();
        }
        if (name != null) {
            name.remove();
        }
        for (ArmorStand armorStand : BattleDrones.call.rocket.rockets) {
            armorStand.remove();
        }
        head = null;
        name = null;
    }

    public void stopAI() {
        BattleDrones.call.getServer().getScheduler().cancelTask(this.AItaskID);
    }

    public void stopShoot() {
        BattleDrones.call.getServer().getScheduler().cancelTask(this.ShootTaskID);
    }

    public void stopRegen() {
        BattleDrones.call.getServer().getScheduler().cancelTask(this.RegenTaskID);
    }

    public void stopDrone() {
        remove();
        stopAI();
        stopShoot();
        stopRegen();
        setActive("");
        BattleDrones.call.drone_amount.remove(uuid);
    }

    public void saveDrone(DroneHolder droneHolder) {
        droneHolder.save();
    }

    public void spawn(final Player player, final String drone) {
        final Location location = player.getLocation();
        final ArmorStand armorStand = BattleDrones.call.armorStandManager.getArmorStand(location.add(0, 2, 0), false, true);
        final ArmorStand armorStandName = BattleDrones.call.armorStandManager.getArmorStand(location.add(0, 2.3, 0), false, true);
        armorStand.setHelmet(BattleDrones.call.drone_heads.get(drone));
        armorStand.setCustomName(" ");
        armorStand.setCustomNameVisible(true);
        armorStandName.setCustomName(" ");
        armorStandName.setCustomNameVisible(true);
        head = armorStand;
        name = armorStandName;
        armorStand.getPersistentDataContainer().set(new NamespacedKey(BattleDrones.call, "drone_uuid"), PersistentDataType.STRING, player.getUniqueId().toString());
        armorStandName.getPersistentDataContainer().set(new NamespacedKey(BattleDrones.call, "drone_uuid"), PersistentDataType.STRING, player.getUniqueId().toString());
    }

    public void save() {
        BattleDrones.call.database.setPlayers(this.uuid, this.active, this.coins, this.group);
    }
}