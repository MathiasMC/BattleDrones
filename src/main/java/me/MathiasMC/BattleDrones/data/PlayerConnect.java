package me.MathiasMC.BattleDrones.data;

import me.MathiasMC.BattleDrones.BattleDrones;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class PlayerConnect {

    public ArmorStand head;
    public ArmorStand name;

    public int AItaskID;
    public int AIfindTargetID;
    public int ShootTaskID;
    public int RegenTaskID;

    private final String uuid;
    private String active;
    private long coins;
    private String group;
    private boolean regen;
    private String last_active;

    public PlayerConnect(final String uuid) {
        this.uuid = uuid;
        final String[] data = BattleDrones.call.database.getPlayers(uuid);
        this.active = data[0];
        this.coins = Long.parseLong(data[1]);
        this.group = data[2];
        this.last_active = "";
    }

    public void setActive(final String set) {
        this.active = set;
        BattleDrones.call.drone_amount.add(uuid);
    }

    public void setCoins(final long set) {
        this.coins = set;
    }

    public void setGroup(String set) {
        this.group = set;
    }

    public void setRegen(final boolean set) {
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

    public void setLast_active(final String set) {
        this.last_active = set;
    }

    public String getLast_active() {
        return this.last_active;
    }

    public boolean hasLast_active() {
        return !this.last_active.isEmpty();
    }

    public void remove() {
        if (head != null) {
            head.remove();
        }
        if (name != null) {
            name.remove();
        }
        for (ArmorStand armorStand : BattleDrones.call.projectiles) {
            armorStand.remove();
        }
        head = null;
        name = null;
    }

    public void stopAI() {
        BattleDrones.call.getServer().getScheduler().cancelTask(this.AItaskID);
    }

    public void stopFindTargetAI() {
        BattleDrones.call.getServer().getScheduler().cancelTask(this.AIfindTargetID);
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
        stopFindTargetAI();
        stopShoot();
        stopRegen();
        setActive("");
        BattleDrones.call.drone_amount.remove(uuid);
        BattleDrones.call.manual.remove(uuid);
    }

    public void saveDrone(DroneHolder droneHolder) {
        droneHolder.save();
    }

    public void spawn(final Player player, final ItemStack itemStack, final boolean hasName) {
        final Location location = player.getLocation();
        final ArmorStand armorStand = BattleDrones.call.armorStandManager.getArmorStand(location.add(0, 2, 0), false, true);
        armorStand.setHelmet(itemStack);
        armorStand.setCustomName(" ");
        armorStand.setCustomNameVisible(true);
        head = armorStand;
        armorStand.getPersistentDataContainer().set(new NamespacedKey(BattleDrones.call, "drone_uuid"), PersistentDataType.STRING, uuid);
        if (hasName) {
            final ArmorStand armorStandName = BattleDrones.call.armorStandManager.getArmorStand(location.add(0, 2.3, 0), false, true);
            armorStandName.setCustomName(" ");
            armorStandName.setCustomNameVisible(true);
            name = armorStandName;
            armorStandName.getPersistentDataContainer().set(new NamespacedKey(BattleDrones.call, "drone_uuid"), PersistentDataType.STRING, uuid);
        }
        if (!BattleDrones.call.config.get.getBoolean("controller.automatic")) {
            BattleDrones.call.manual.add(uuid);
        }
    }

    public void save() {
        BattleDrones.call.database.setPlayers(this.uuid, this.active, this.coins, this.group);
    }
}