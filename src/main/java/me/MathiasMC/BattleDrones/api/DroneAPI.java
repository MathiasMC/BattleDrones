package me.MathiasMC.BattleDrones.api;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import me.MathiasMC.BattleDrones.managers.*;
import me.MathiasMC.BattleDrones.support.Support;
import me.MathiasMC.BattleDrones.utils.FileUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DroneAPI {

    private static DroneAPI instance;

    private final BattleDrones plugin;

    private DroneAPI() {
        this.plugin = BattleDrones.getInstance();
    }

    public PlayerConnect getPlayerConnect(String uuid) {
        return plugin.getPlayerConnect(uuid);
    }

    public DroneHolder getDroneHolder(String uuid, String droneName) {
        return plugin.getDroneHolder(uuid, droneName);
    }

    public void unloadPlayerConnect(String uuid) {
        plugin.unloadPlayerConnect(uuid);
    }

    public void unloadDroneHolder(String uuid) {
        plugin.unloadDroneHolder(uuid);
    }

    public Set<String> listPlayerConnect() {
        return plugin.listPlayerConnect();
    }

    public Set<String> listDroneHolder() {
        return plugin.listDroneHolder();
    }

    public Support getSupport() {
        return plugin.getSupport();
    }

    public FileUtils getFileUtils() {
        return plugin.getFileUtils();
    }

    public EntityManager getEntityManager() {
        return plugin.getEntityManager();
    }

    public PlaceholderManager getPlaceholderManager() {
        return plugin.getPlaceholderManager();
    }

    public ItemStackManager getItemStackManager() {
        return plugin.getItemStackManager();
    }

    public DroneControllerManager getDroneControllerManager() {
        return plugin.getDroneControllerManager();
    }

    public CalculateManager getCalculateManager() {
        return plugin.getCalculateManager();
    }

    public ParticleManager getParticleManager() {
        return plugin.getParticleManager();
    }

    public DroneManager getDroneManager() {
        return plugin.getDroneManager();
    }

    public TaskManager getTaskManager() {
        return plugin.getTaskManager();
    }

    public DronePruneManager getDronePruneManager() {
        return plugin.getDronePruneManager();
    }

    public HashMap<String, LivingEntity> getDroneTargets() {
        return plugin.drone_targets;
    }

    public HashMap<String, FileConfiguration> getDroneFiles() {
        return plugin.droneFiles;
    }

    public HashMap<String, FileConfiguration> getGUIFiles() {
        return plugin.guiFiles;
    }

    public HashMap<String, ItemStack> getDroneHeads() {
        return plugin.drone_heads;
    }

    public HashSet<String> getWaitPlayers() {
        return plugin.drone_wait;
    }

    public HashSet<ArmorStand> getProjectiles() {
        return plugin.projectiles;
    }

    public HashSet<String> getFollowingDrones() {
        return plugin.drone_follow;
    }

    public HashSet<String> getParkedDrones() {
        return plugin.park;
    }

    public HashMap<String, String> getDronesByName() {
        return plugin.drones;
    }

    public HashMap<String, List<String>> getDronesByCategory() {
        return plugin.category;
    }

    public void setupMenu(String displayName,
                          List<String> lores,
                          String material,
                          String head,
                          int amount,
                          int position,
                          String droneCategory
    ) {
        setItem("player", displayName, lores, material, head, amount, position, droneCategory);
    }

    public void setupShop(String displayName,
                          List<String> lores,
                          String material,
                          String head,
                          int amount,
                          int position,
                          String droneCategory
    ) {
        setItem("shop", displayName, lores, material, head, amount, position, droneCategory);
    }

    private void setItem(
            String type,
            String displayName,
            List<String> lores,
            String material,
            String head,
            int amount,
            int position,
            String droneCategory
    ) {
        final File fileF = plugin.getFileUtils().guiFiles.get(type);
        final FileConfiguration file = YamlConfiguration.loadConfiguration(fileF);

        final String path = position + ".";

        file.set(path + "NAME", displayName);
        file.set(path + "LORES", lores);

        if (head != null) {
            file.set(path + "HEAD", head);
        } else {
            file.set(path + "MATERIAL", material != null ? material : "DIRT");
        }

        file.set(path + "AMOUNT", amount);
        file.set(path + "CATEGORY", droneCategory);

        try {
            file.save(fileF);
            plugin.guiFiles.put(type, file);
        } catch (IOException ignored) {

        }
    }

    public static DroneAPI getInstance() {
        if (instance == null) {
            instance = new DroneAPI();
        }
        return instance;
    }

    public ItemStack getHeadTexture(String texture) {
        return plugin.getItemStackManager().getHeadTexture(texture);
    }
}
