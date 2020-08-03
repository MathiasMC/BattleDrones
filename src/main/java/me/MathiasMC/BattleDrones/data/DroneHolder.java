package me.MathiasMC.BattleDrones.data;

import me.MathiasMC.BattleDrones.BattleDrones;

import java.util.*;

public class DroneHolder {

    private final String uuid;
    private final String drone;

    private int unlocked;
    private int level;
    private int ammo;
    private int monsters;
    private int animals;
    private int players;
    private List<String> exclude;
    private int health;
    private int left;

    public DroneHolder(final String uuid, final String drone) {
        this.uuid = uuid;
        this.drone = drone;
        final String[] data = BattleDrones.call.database.getDrone(uuid, drone);
        this.unlocked = Integer.parseInt(data[0]);
        this.level = Integer.parseInt(data[1]);
        this.ammo = Integer.parseInt(data[2]);
        this.monsters = Integer.parseInt(data[3]);
        this.animals = Integer.parseInt(data[4]);
        this.players = Integer.parseInt(data[5]);
        if (data[6].isEmpty()) {
            this.exclude = new ArrayList<>();
        } else {
            this.exclude = new LinkedList<>(Arrays.asList(data[6].split("\\s*:\\s*")));
        }
        this.health = Integer.parseInt(data[7]);
        this.left = Integer.parseInt(data[8]);
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

    public void setLeft(final int set) {
        this.left = set;
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

    public int getLeft() {
        return this.left;
    }

    public String exclude() {
        final StringJoiner stringJoiner = new StringJoiner(":");
        for (String player : this.exclude) {
            stringJoiner.add(player);
        }
        return stringJoiner.toString();
    }

    public void save() {
        BattleDrones.call.database.setDrone(uuid, drone, unlocked, level, ammo, monsters, animals, players, exclude(), health, left);
    }
}
