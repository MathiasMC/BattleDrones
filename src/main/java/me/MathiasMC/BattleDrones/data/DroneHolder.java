package me.MathiasMC.BattleDrones.data;

import me.MathiasMC.BattleDrones.BattleDrones;

import java.util.*;

public class DroneHolder {

    private final String uuid;
    private final String droneName;

    private int unlocked;
    private int level;
    private int ammo;
    private int monsters;
    private int animals;
    private int players;
    private List<String> exclude;
    private int health;
    private int wear;

    public DroneHolder(final String uuid, final String droneName) {
        this.uuid = uuid;
        this.droneName = droneName;
        final String[] data = BattleDrones.getInstance().database.getDrone(uuid, droneName);
        this.unlocked = Integer.parseInt(data[0]);
        this.level = Integer.parseInt(data[1]);
        this.ammo = Integer.parseInt(data[2]);
        this.monsters = Integer.parseInt(data[3]);
        this.animals = Integer.parseInt(data[4]);
        this.players = Integer.parseInt(data[5]);
        this.exclude = data[6].isEmpty()
                ? new LinkedList<>()
                : new LinkedList<>(Arrays.asList(data[6].split("\\s*:\\s*")));
        this.health = Integer.parseInt(data[7]);
        this.wear = Integer.parseInt(data[8]);
    }

    public String getUniqueId() {
        return this.uuid;
    }

    public String getDrone() {
        return this.droneName;
    }

    public void setUnlocked(final int set) {
        this.unlocked = set;
    }

    public void setLevel(final int set) {
        this.level = set;
    }

    public void setAmmo(final int set) {
        this.ammo = set;
    }

    public void setMonsters(final int set) {
        this.monsters = set;
    }

    public void setAnimals(final int set) {
        this.animals = set;
    }

    public void setPlayers(final int set) {
        this.players = set;
    }

    public void setExclude(final List<String> set) {
        this.exclude = set;
    }

    public void setHealth(final int set) {
        this.health = set;
    }

    public void setWear(final int set) {
        this.wear = set;
    }

    public int getUnlocked() {
        return this.unlocked;
    }

    public int getLevel() {
        return this.level;
    }

    public int getAmmo() {
        return this.ammo;
    }

    public int getMonsters() {
        return this.monsters;
    }

    public int getAnimals() {
        return this.animals;
    }

    public int getPlayers() {
        return this.players;
    }

    public List<String> getExclude() {
        return this.exclude;
    }

    public int getHealth() {
        return this.health;
    }

    public int getWear() {
        return this.wear;
    }

    public boolean isParked() {
        return BattleDrones.getInstance().park.contains(uuid);
    }

    public boolean isStationary() {
        return BattleDrones.getInstance().getFileUtils().config.getStringList("stationary-mode").contains(droneName);
    }

    private String exclude() {
        return String.join(":", this.exclude);
    }

    public void save() {
        BattleDrones.getInstance().database.setDrone(uuid, droneName, unlocked, level, ammo, monsters, animals, players, exclude(), health, wear);
    }
}
